package SnakeGame;

import NetworkPart.NetSocket.SteerMsgQueue;
import me.ippolitov.fit.snakes.SnakesProto;
import me.ippolitov.fit.snakes.SnakesProto.GamePlayer;
import me.ippolitov.fit.snakes.SnakesProto.GameState;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GameLogic {

    private static int width;
    private static int height;
    private int foodStatic;
    private float foodPerPlayer;
    private float deadFoodProb;

    private static int[][] gameField;
    private int stateOrder = 1;

    private List<Event> crashHeads = new ArrayList<>();
    private List<Event> appleHeads = new ArrayList<>();
    private List<Integer> deadSnakes = new ArrayList<>();
    private List<Event> apples = new ArrayList<>();

    public GameLogic(int width, int height, int foodStatic, float foodPerPlayer,
                     float deadFoodProb, int stateOrder) {
        GameLogic.width = width;
        GameLogic.height = height;
        this.foodStatic = foodStatic;
        this.foodPerPlayer = foodPerPlayer;
        this.deadFoodProb = deadFoodProb;
        this.stateOrder = stateOrder;
        gameField = new int[width][height];
    }
    //новый шаг
    public void makeNextStep() {
        crashHeads.clear();
        appleHeads.clear();
        deadSnakes.clear();

        gameField = GameField.getGameField();

        for (Event apple : apples) gameField[apple.getX()][apple.getY()] = 1;

        Players.getInstance().updatePlayers();
        ArrayList<GamePlayer> players = Players.getInstance().getPlayers();
        List<GameState.Snake> snakes = Players.getInstance().getSnakes();
        List<GameState.Snake> updatedSnakes = new ArrayList<>();
        Map<Integer, SnakesProto.Direction> newDirections = SteerMsgQueue.getInstance().getMap();

        GameState.Snake snake = null;

        for (int k = 0; k < snakes.size(); k++) {
            snake = snakes.get(k);
            if (snake == null)
                continue;
            int tail = snake.getPlayerId() + 1;
            int head = -tail;

            int i = snake.getPoints(0).getX();
            int j = snake.getPoints(0).getY();

            gameField[i][j] = tail;
            SnakesProto.Direction direction;
            if (newDirections.containsKey(snake.getPlayerId())) {
                //если повернули то надо добавить "угол"
                GameState.Snake.Builder builder = snake.toBuilder();
                List<GameState.Coord> coords = builder.getPointsList();
                coords = new ArrayList<>(coords);
                direction = newDirections.get(snake.getPlayerId());
                switch (direction) {
                    case UP:
                        coords.add(1, GameState.Coord.newBuilder().setX(0).setY(1).build());
                        break;
                    case DOWN:
                        coords.add(1, GameState.Coord.newBuilder().setX(0).setY(-1).build());
                        break;
                    case LEFT:
                        coords.add(1, GameState.Coord.newBuilder().setX(1).setY(0).build());
                        break;
                    case RIGHT:
                        coords.add(1, GameState.Coord.newBuilder().setX(-1).setY(0).build());
                        break;
                }
                builder.setHeadDirection(newDirections.get(snake.getPlayerId()));
                newDirections.remove(snake.getPlayerId());
                builder.clearPoints();
                for (int p = 0; p < coords.size(); p++) {
                    builder.addPoints(coords.get(p));
                }
                snake = builder.build();
            } else {
                direction = snake.getHeadDirection();
                int x = snake.getPoints(1).getX();
                int y = snake.getPoints(1).getY();
                switch (direction) {
                    case UP:
                        y++;
                        break;
                    case RIGHT:
                        x--;
                        break;
                    case DOWN:
                        y--;
                        break;
                    case LEFT:
                        x++;
                        break;
                }
                snake = snake.toBuilder().setPoints(1, GameState.Coord.newBuilder().setX(x).setY(y).build()).build();
            }
            switch (direction) {
                case UP:
                    j--;
                    if (j < 0)
                        j = height - 1;
                    break;
                case LEFT:
                    i--;
                    if (i < 0)
                        i = width - 1;
                    break;
                case DOWN:
                    j++;
                    if (j >= height)
                        j = 0;
                    break;
                case RIGHT:
                    i++;
                    if (i >= width)
                        i = 0;
                    break;
            }

            if (gameField[i][j] > 1 || gameField[i][j] < -1) {
                crashHeads.add(new Event(i, j, head));
                GameState.Snake.Builder builder = snake.toBuilder();
                builder.setPoints(0, GameState.Coord.newBuilder().setX(i).setY(j).build());
                snake = builder.build();
            }
            if (gameField[i][j] == 1) {
                appleHeads.add(new Event(i, j, head));
                Iterator<Event> iterator = apples.iterator();
                while (iterator.hasNext()) {
                    Event e = iterator.next();
                    if (e.getX() == i && e.getY() == j)
                        iterator.remove();
                }

                GameState.Snake.Builder builder = snake.toBuilder();
                builder.setPoints(0, GameState.Coord.newBuilder().setX(i).setY(j).build());
                snake = builder.build();
                gameField[i][j] = head;
            }
            if (gameField[i][j] == 0) {
                GameState.Snake.Builder builder = snake.toBuilder();
                builder.setPoints(0, GameState.Coord.newBuilder().setX(i).setY(j).build());
                snake = builder.build();
                gameField[i][j] = head;
            }
            updatedSnakes.add(snake);
        }

        checkDeadHeadToHead();
        checkDeadHeadToTail();
        updatedSnakes = moveTail(updatedSnakes, newDirections);
        updatedSnakes = checkLastDead(updatedSnakes);
        removeDeadSnakes(updatedSnakes, players);
        addApples(players);

        Players.getInstance().setSnakes(updatedSnakes);
        Players.getInstance().updateAllRoles(); //проверить что правильно и корректно вызывается
        GameField.setGameField(gameField);
    }

    //проверяем голову с головой
    private void checkDeadHeadToHead() {
        Iterator<Event> iterator = crashHeads.iterator();
        while (iterator.hasNext()) {
            Event e = iterator.next();
            int i = e.getX();
            int j = e.getY();
            int head = e.getHost_head();

            if (gameField[i][j] < 0 && gameField[i][j] != head) {
                int head2 = gameField[i][j];
                deadSnakes.add(head);
                if (!deadSnakes.contains(head2))
                    deadSnakes.add(head2);
                iterator.remove();
            }
        }
    }

    //проверяем голову с туловищем
    private void checkDeadHeadToTail() {
        Iterator<Event> iterator = crashHeads.iterator();
        while (iterator.hasNext()) {
            Event e = iterator.next();
            int i = e.getX();
            int j = e.getY();
            int head = e.getHost_head();

            if (gameField[i][j] > 1) {
                int tail2 = gameField[i][j];
                if (deadSnakes.contains(-tail2)) {
                    deadSnakes.add(head);
                    iterator.remove();
                }
            }
        }
    }

    //перемещаем тела
    private List<GameState.Snake> moveTail(List<GameState.Snake> snakes, Map<Integer, SnakesProto.Direction> map) {
        List<GameState.Snake> updatedSnakes = new ArrayList<>();
        for (int k = 0; k < snakes.size(); k++) {
            GameState.Snake snake = snakes.get(k);
            if (snake == null)
                continue;
            int tail = snake.getPlayerId() + 1;
            int head = -tail;
            boolean appleHead = false;
            if (!deadSnakes.contains(head)) {

                for (int i = 0; i < appleHeads.size(); i++) {
                    if (head == appleHeads.get(i).getHost_head()) {
                        appleHead = true;

                        SnakesProto.Direction direction = snake.getHeadDirection();
                       /* if (!map.containsKey(snake.getPlayerId())) {
                            //хвост остается на месте после съедения яблока
                            switch (direction) {
                                case UP:
                                    snake = snake.toBuilder().setPoints(1, GameState.Coord.newBuilder()
                                            .setX(0)
                                            .setY(snake.getPoints(1).getY() + 1).build()).build();
                                case DOWN:
                                    snake = snake.toBuilder().setPoints(1, GameState.Coord.newBuilder()
                                            .setX(0)
                                            .setY(snake.getPoints(1).getY() - 1).build()).build();
                                case RIGHT:
                                    snake = snake.toBuilder().setPoints(1, GameState.Coord.newBuilder()
                                            .setX(snake.getPoints(1).getX() - 1)
                                            .setY(0).build()).build();
                                case LEFT:
                                    snake = snake.toBuilder().setPoints(1, GameState.Coord.newBuilder()
                                            .setX(snake.getPoints(1).getX() + 1)
                                            .setY(0).build()).build();
                            }
                        }*/
                    }
                }
                if (!appleHead) {
                    //если не скушали яблоко
                    List<GameState.Coord> coords = snake.getPointsList();
                    GameState.Coord lastPointOfTail = coords.get(coords.size() - 1);
                    //!(x == 0 && y == 0)
                    int currentX = coords.get(0).getX();
                    int currentY = coords.get(0).getY();
                    for (int i = 1; i < coords.size(); i++) {
                        GameState.Coord coord = (SnakesProto.GameState.Coord) coords.get(i);
                        int x = coord.getX();
                        int y = coord.getY();
                        //!(x == 0 && y == 0)
                        while (x != 0 || y != 0) {
                            if (x != 0) {
                                if (x > 0) {
                                    currentX++;
                                    if (currentX >= width)
                                        currentX = 0;
                                    x--;
                                }
                                if (x < 0) {
                                    currentX--;
                                    if (currentX < 0)
                                        currentX = width - 1;
                                    x++;
                                }
                            }
                            if (y != 0) {
                                if (y > 0) {
                                    currentY++;
                                    if (currentY >= height)
                                        currentY = 0;
                                    y--;
                                }
                                if (y < 0) {
                                    currentY--;
                                    if (currentY < 0)
                                        currentY = height - 1;
                                    y++;
                                }
                            }
                        }
                    }
                    gameField[currentX][currentY] = 0;

                    currentX = lastPointOfTail.getX();
                    currentY = lastPointOfTail.getY();
                    if (currentX > 0)
                        currentX--;
                    if (currentX < 0)
                        currentX++;
                    if (currentY > 0)
                        currentY--;
                    if (currentY < 0)
                        currentY++;
                    coords = new ArrayList<>(coords);
                    coords.remove(coords.size() - 1);
                    if (!(currentX == 0 && currentY == 0))
                        coords.add(GameState.Coord.newBuilder().setX(currentX).setY(currentY).build());




                    GameState.Snake.Builder builder = snake.toBuilder();
                    builder.clearPoints();
                    for (int i = 0; i < coords.size(); i++) {
                        builder.addPoints(coords.get(i));
                    }
                    snake = builder.build();
                }
            }
            updatedSnakes.add(snake);
        }
        return updatedSnakes;
    }

    //еще раз проверяем мертвые головы
    private List<GameState.Snake> checkLastDead(List<GameState.Snake> snakes) {
        snakes = new ArrayList<>(snakes);
        ArrayList<GameState.Snake> updateSnake = new ArrayList<>();
        Iterator<Event> iterator = crashHeads.iterator();
        while (iterator.hasNext()) {
            Event e = iterator.next();
            int i = e.getX();
            int j = e.getY();
            int head = e.getHost_head();
            if (gameField[i][j] > 1 || gameField[i][j] < -1) {//
            //if (gameField[i][j] > 1 || gameField[i][j] < -1) {
                deadSnakes.add(head);

            } else {
            /*
            добавить постановку головы на поле
             */
                Iterator<GameState.Snake> iter = snakes.iterator();
                while (iter.hasNext()) {
                    GameState.Snake snake = iter.next();
                    if (snake.getPlayerId() == (Math.abs(head) - 1)) {
                        iter.remove();
                        snake = snake.toBuilder().setPoints(0, GameState.Coord.newBuilder()
                                .setX(i)
                                .setY(j)
                                .build()).build();
                        updateSnake.add(snake);
                    }

                }
            }
            iterator.remove();
        }

        for (GameState.Snake snake : updateSnake) {
            snakes.add(snake);
        }
        return snakes;
    }

    //удаляем мертвых змей
    private void removeDeadSnakes(List<GameState.Snake> snakes, ArrayList<GamePlayer> players) {
        ArrayList<GamePlayer> updatedPlayers = new ArrayList<>();
        //восстановление яблок
        Iterator<Event> eventIterator = appleHeads.iterator();
        while(eventIterator.hasNext()) {
            Event appleHead = eventIterator.next();
            if (deadSnakes.contains(appleHead.getHost_head())) {
                gameField[appleHead.getX()][appleHead.getY()] = 1;
                apples.add(new Event(appleHead.getX(), appleHead.getY(), 0));
                eventIterator.remove();
            }
        }

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                for (int k = 0; k < deadSnakes.size(); k++) {
                    if (gameField[i][j] == deadSnakes.get(k) ||
                            gameField[i][j] == (-deadSnakes.get(k))) {
                        if (Math.random() < deadFoodProb) {
                            gameField[i][j] = 1;
                            apples.add(new Event(i, j, 0));
                        } else
                            gameField[i][j] = 0;
                    }

                }
            }
        }

        Iterator<GameState.Snake> iterator = snakes.iterator();
        boolean isMasterDead = false;
        boolean isDeputyDead = false;
        while (iterator.hasNext()) {
            GameState.Snake snake = iterator.next();
            int ID = snake.getPlayerId() + 1;
            for (int i = 0; i < deadSnakes.size(); i++) {
                if (ID == Math.abs(deadSnakes.get(i))) {
                    iterator.remove();
                 //   snakes.add(snake.toBuilder().setState(GameState.Snake.SnakeState.ZOMBIE).build());
                    Iterator<GamePlayer> iter = players.iterator();
                    while (iter.hasNext()) {
                        GamePlayer player = iter.next();
                        if ((player.getId() + 1) == ID) {
                            if(!player.getRole().equals(SnakesProto.NodeRole.MASTER)) {
                                Players.getInstance().getController().sendRoleChange(
                                        SnakesProto.NodeRole.VIEWER,
                                        SnakesProto.NodeRole.MASTER,
                                        player.getId());            //перевыбор зама
                                //player = player.toBuilder().setRole(SnakesProto.NodeRole.VIEWER).build();
                                if(player.getRole().equals(SnakesProto.NodeRole.DEPUTY)){
                                    isDeputyDead = true;
                                }
                                updatedPlayers.add(player.toBuilder().setRole(SnakesProto.NodeRole.VIEWER).build());
                            }
                            else{
                                isMasterDead = true;
                            }
                            iter.remove();
                        }
                    }
                }
            }
        }


        players.addAll(updatedPlayers);

        Iterator<GamePlayer> iter = players.iterator();
        while (iter.hasNext()) {
            GamePlayer player = iter.next();
            eventIterator = appleHeads.iterator();
            while (eventIterator.hasNext()){
                Event event = eventIterator.next();
                if(player.getId() == (Math.abs(event.getHost_head())-1)){
                    iter.remove();
                    updatedPlayers.add(player.toBuilder().setScore(player.getScore() + 1).build());
                    eventIterator.remove();
                }
            }
        }
        players.addAll(updatedPlayers);
        Players.getInstance().setPlayers(players);
        if(isDeputyDead){
            Players.getInstance().selectNewDeputy(players,false);//
        }
        if(isMasterDead) {
            for (GamePlayer gamePlayer : players) {
                if (gamePlayer.getRole().equals(SnakesProto.NodeRole.DEPUTY)){
                    Players.getInstance().getController().sendRoleChange();
                    Players.getInstance().getController().masterDead(gamePlayer);
                }
            }
        }
    }

    //добавляем яблоки
    private void addApples(List<GamePlayer> players) {
        while (apples.size() < (players.size() * foodPerPlayer + foodStatic)) {
            int x = (int) (Math.random() * width), y = (int) (Math.random() * height);
            while (gameField[x][y] != 0) {
                x = (int) (Math.random() * width);
                y = (int) (Math.random() * height);
            }
            gameField[x][y] = 1;
            apples.add(new Event(x, y, 0));
        }
    }

    //создаем новый стейт
    public GameState createNewState(int delay, int pingDelay, int nodeTimeout) {
        SnakesProto.GameConfig config = SnakesProto.GameConfig.newBuilder()
                .setWidth(width)
                .setHeight(height)
                .setFoodStatic(foodStatic)
                .setFoodPerPlayer(foodPerPlayer)
                .setStateDelayMs(delay)
                .setDeadFoodProb(deadFoodProb)
                .setPingDelayMs(pingDelay)
                .setNodeTimeoutMs(nodeTimeout)
                .build();

        List<GamePlayer> list = Players.getInstance().getPlayers();
        SnakesProto.GamePlayers.Builder playersBuilder = SnakesProto.GamePlayers.newBuilder();
        for (int i = 0; i < list.size(); i++) {
            playersBuilder.addPlayers(list.get(i));
        }

        List<GameState.Snake> list2 = Players.getInstance().getSnakes();
        GameState.Builder stateBuilder = GameState.newBuilder();

        for (int i = 0; i < list2.size(); i++) {
            stateBuilder.addSnakes(list2.get(i));
        }

        stateBuilder.setStateOrder(stateOrder)
                .setConfig(config)
                .setPlayers(playersBuilder.build());
        stateOrder++;
        for (int i = 0; i < apples.size(); i++) {
            stateBuilder.addFoods(GameState.Coord.newBuilder()
                    .setX(apples.get(i).getX())
                    .setY(apples.get(i).getY()));
        }

        return stateBuilder.build();
    }
}