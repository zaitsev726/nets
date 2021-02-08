import org.xbill.DNS.ARecord;
import org.xbill.DNS.DClass;
import org.xbill.DNS.Message;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.Section;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

public class Handler {
    private int bufferSize = 1024 * 8;
    private static final byte VERSION = 0x05;
    private static final byte AUTHENTICATION = 0x00;
    private static final byte COMMAND = 0x01;
    private static final byte IPv4 = 0x01;
    private static final byte DOMAIN = 0x03;
    private DatagramChannel dnsChannel;

    private int port;

    private static final int ERROR = -1;
    private static final int ACCEPTED = 0;
    private static final int CONNECTED = 1;

    Handler(int port){
        this.port = port;
    }

    static class Connection {
        int status = ACCEPTED;
        /**
         * Буфер для чтения, в момент проксирования становится буфером для
         * записи для ключа хранимого в peer
         */
        ByteBuffer in;

        /**
         * Буфер для записи, в момент проксирования равен буферу для чтения для
         * ключа хранимого в peer
         */
        ByteBuffer out;

        /**
         * Куда проксируем
         */
        SelectionKey peer;
        InetAddress remoteAddress = null;
        Integer remotePort = null;
    }

    void execute() throws IOException {

        Selector selector = SelectorProvider.provider().openSelector();
        dnsChannel = DatagramChannel.open();
        dnsChannel.configureBlocking(false);
        dnsChannel.register(selector, SelectionKey.OP_READ);
        dnsChannel.connect(new InetSocketAddress("8.8.8.8", 53));

        try (ServerSocketChannel serverSocket = ServerSocketChannel.open()) {
            serverSocket.configureBlocking(false);
            serverSocket.socket().bind(new InetSocketAddress("localhost", port));
            serverSocket.register(selector, SelectionKey.OP_ACCEPT);

            while (selector.select() > -1) {

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();

                    if (key.isValid()) {
                        try {
                            if (key.isAcceptable()) {
                                accept(key);
                            } else if (key.isConnectable()) {
                                connect(key);
                            } else if (key.isReadable()) {
                                read(key);
                            } else if (key.isWritable()) {
                                write(key);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            close(key);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Функция принимает соединение, регистрирует ключ с интересуемым действием
     * чтение данных (OP_READ)
     */
    private void accept(SelectionKey key) throws IOException {
        SocketChannel client = ((ServerSocketChannel) key.channel()).accept();
        client.configureBlocking(false);
        client.register(key.selector(), SelectionKey.OP_READ);
    }

    /**
     * Читаем данные доступные в данный момент. Функция бывает в двух состояних -
     * чтение заголовка запроса и непосредственного проксирование
     */
    private void read(SelectionKey key) throws IOException {

        Connection connection = ((Connection) key.attachment());

        if (key.channel() instanceof DatagramChannel) {
            System.out.println("Reading resolve");
            DatagramChannel channel = (DatagramChannel) key.channel();
            if (channel.read(connection.in) < 1) {
                // -1 - разрыв 0 - нету места в буфере, такое может быть только если
                // заголовок превысил размер буфера
                System.out.println("Cannot read DNS response");
            }
            resolve(key, connection);
            return;
        }

        SocketChannel channel = ((SocketChannel) key.channel());
        if (connection == null) {
            connection = new Connection();
            key.attach(connection);
            connection.in = ByteBuffer.allocate(bufferSize);
        }
        int countBytes = channel.read(connection.in);
        if (countBytes < 1) {
            // -1 - разрыв 0 - нету места в буфере, такое может быть только если
            // заголовок превысил размер буфера
            close(key);
        } else if (connection.status == ACCEPTED) {
            checkFirstMeet(key, connection);
        } else if (connection.status == CONNECTED) {
            if (connection.peer == null) {
                checkConnection(key, connection);
            } else {
                connection.peer.interestOps(connection.peer.interestOps() | SelectionKey.OP_WRITE);
                key.interestOps(key.interestOps() ^ SelectionKey.OP_READ);
                connection.in.flip();
            }
        }
    }

    private void resolve(SelectionKey key, Connection connection) throws IOException {
        byte[] msg = connection.in.array();
        System.out.println("DNS response " + Arrays.toString(msg));

        Message response = new Message(msg);
        Record[] sectionArray = response.getSectionArray(Section.ANSWER);
        InetAddress address = null;
        for (Record r : sectionArray) {
            if (r.getType() == Type.A) {
                System.out.println("Resolved " + r.getName() + " to " + ((ARecord) r).getAddress().toString());
                address = ((ARecord) r).getAddress();
                break;
            }
        }

        if (address == null) {
            System.out.println("Cannot resolve domain, send error");
            return;
        }

        SelectionKey source = connection.peer;
        Connection sourceAttachment = (Connection) source.attachment();

        // Если работаем сразу с ip адресом - создаём соединение
        SocketChannel peer = SocketChannel.open();
        peer.configureBlocking(false);
        // Получаем из пакета адрес и порт
        sourceAttachment.remoteAddress = address;
        // Порт уже проставлен при получении connect
        System.out.println("Connecting to " + sourceAttachment.remoteAddress.toString() + ":" + sourceAttachment.remotePort);
        peer.connect(new InetSocketAddress(sourceAttachment.remoteAddress, sourceAttachment.remotePort));
        // Регистрация в селекторе
        SelectionKey peerKey = peer.register(source.selector(), SelectionKey.OP_CONNECT);
        // Обмен ключами
        sourceAttachment.peer = peerKey;
        Connection peerAttachment = new Connection();
        peerAttachment.peer = source;
        peerAttachment.status = CONNECTED;
        peerKey.attach(peerAttachment);
        // Очищаем буфер с заголовками
        sourceAttachment.in.clear();
    }

    private void checkFirstMeet(SelectionKey key, Connection connection) {
        byte[] msg = connection.in.array();
        if (msg[0] != VERSION) {
            throw new IllegalStateException("Bad Request");
        }
        int numMethods = msg[1];
        boolean haveMethod = false;
        for (int i = 0; i < numMethods; ++i) {
            if (msg[i + 2] == AUTHENTICATION) {
                haveMethod = true;
                break;
            }
        }
        connection.out = ByteBuffer.allocate(bufferSize);
        connection.out.put(VERSION);
        if (haveMethod) {
            connection.out.put(AUTHENTICATION);
        } else {
            connection.out.put((byte) (0xFF));
            connection.status = ERROR;
        }
        connection.out.flip();
        key.interestOps(SelectionKey.OP_WRITE);
    }

    private void checkConnection(SelectionKey key, Connection connection) throws IOException {
        byte[] msg = connection.in.array();

        if (msg[0] != VERSION || msg[1] != COMMAND || connection.in.position() < 8) {
            throw new IllegalStateException("Bad Request");
        }

        if (msg[3] == IPv4) {
            SocketChannel peer = SocketChannel.open();
            peer.configureBlocking(false);

            byte[] addr = new byte[]{msg[4], msg[5], msg[6], msg[7]};
            int port = (((0xFF & msg[8]) << 8) + (0xFF & msg[9]));

            connection.remoteAddress = InetAddress.getByAddress(addr);
            connection.remotePort = port;

            peer.connect(new InetSocketAddress(connection.remoteAddress, port));
            SelectionKey peerKey = peer.register(key.selector(), SelectionKey.OP_CONNECT);
            key.interestOps(0);

            connection.peer = peerKey;
            Connection peerConnection = new Connection();
            peerConnection.peer = key;
            peerConnection.status = CONNECTED;
            peerKey.attach(peerConnection);
            connection.in.clear();
        } else if (msg[3] == DOMAIN) {
            System.out.println("DNS");

            // Глушим запрашивающее соединение
            key.interestOps(0);
            connection.remotePort = (((0xFF & (msg[5 + msg[4]])) << 8) + (0xFF & (msg[6 + msg[4]])));

            Connection dnsAttachment = new Connection();
            dnsAttachment.status = CONNECTED;
            dnsAttachment.peer = key;
            SelectionKey dnsKey = dnsChannel.register(key.selector(), SelectionKey.OP_READ);
            dnsKey.attach(dnsAttachment);
            connection.in.clear();

            String domainName = new String(Arrays.copyOfRange(msg, 5, 5 + msg[4]));
            System.out.println("Looking up domain " + domainName);

            Record question = Record.newRecord(Name.fromString(domainName + "."), Type.A, DClass.IN);
            Message query = Message.newQuery(question);

            dnsAttachment.out = ByteBuffer.allocate(bufferSize);
            dnsAttachment.out.put(query.toWire()).flip();

            dnsChannel.write(dnsAttachment.out);

            System.out.println("Sent dns request");

            dnsAttachment.in = ByteBuffer.allocate(bufferSize);
        }

    }

    /**
     * Запись данных из буфера
     */
    private void write(SelectionKey key) throws IOException {
        SocketChannel channel = ((SocketChannel) key.channel());
        Connection connection = ((Connection) key.attachment());

        if (channel.write(connection.out) == -1) {
            close(key);
        } else if (connection.out.remaining() == 0) {
            if (connection.status == ERROR) {
                close(key);
                return;
            }
            if (connection.status == ACCEPTED) {
                connection.status = CONNECTED;
                connection.in.clear();
                connection.out.clear();
                key.interestOps(SelectionKey.OP_READ);
                return;
            }
            if (connection.peer == null) {
                close(key);
                return;
            }
            connection.out.clear();
            connection.peer.interestOps(connection.peer.interestOps() | SelectionKey.OP_READ);
            key.interestOps(key.interestOps() ^ SelectionKey.OP_WRITE);
        }
    }

    /**
     * Завершаем соединение
     */
    private void connect(SelectionKey key) throws IOException {
        SocketChannel channel = ((SocketChannel) key.channel());
        Connection connection = ((Connection) key.attachment());

        channel.finishConnect();

        connection.in = makeAnswer(connection);
        connection.in.flip();
        connection.out = ((Connection) connection.peer.attachment()).in;
        ((Connection) connection.peer.attachment()).out = connection.in;

        connection.peer.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
        key.interestOps(0);
    }

    private ByteBuffer makeAnswer(Connection connection) {
        Connection peerConnection = (Connection) connection.peer.attachment();
        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        buffer.put(VERSION);
        buffer.put((byte) 0x00);
        buffer.put((byte) 0x00);
        buffer.put(IPv4);
        buffer.put(peerConnection.remoteAddress.getAddress());
        buffer.put((byte) ((peerConnection.remotePort >> 8) & 0xFF));
        buffer.put((byte) (peerConnection.remotePort & 0xFF));
        return buffer;
    }

    private void close(SelectionKey key) throws IOException {
        key.cancel();
        key.channel().close();
        SelectionKey peerKey = ((Connection) key.attachment()).peer;
        if (peerKey != null) {
            ((Connection) peerKey.attachment()).peer = null;
            if ((peerKey.interestOps() & SelectionKey.OP_WRITE) == 0 && ((Connection) peerKey.attachment()).out != null) {
                ((Connection) peerKey.attachment()).out.flip();
            }
            peerKey.interestOps(SelectionKey.OP_WRITE);
        }
    }
}