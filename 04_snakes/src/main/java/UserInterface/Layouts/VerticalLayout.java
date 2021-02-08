package UserInterface.Layouts;
import java.awt.*;

public class VerticalLayout implements LayoutManager
{
    private int sizeX;
    private int sizeY;

    public VerticalLayout(int sizeX, int sizeY){
        this.sizeX = sizeX;
        this.sizeY = sizeY;
    }
    private VerticalLayout(){}
    private Dimension size = new Dimension();

    // Следующие два метода не используются
    public void addLayoutComponent   (String name, Component comp) {}
    public void removeLayoutComponent(Component comp) {}

    // Метод определения минимального размера для контейнера
    public Dimension minimumLayoutSize(Container c) {
        return calculateBestSize(c);
    }
    // Метод определения предпочтительного размера для контейнера
    public Dimension preferredLayoutSize(Container c) {
        return calculateBestSize(c);
    }
    // Метод расположения компонентов в контейнере
    public void layoutContainer(Container container)
    {
        // Список компонентов
        Component list[] = container.getComponents();
        int currentY = 5;
        for (int i = 0; i < list.length; i++) {
            // Определение предпочтительного размера компонента
            Dimension pref = list[i].getPreferredSize();
            // Размещение компонента на экране
            list[i].setBounds(5, currentY,sizeX,sizeY);
            // Учитываем промежуток в 5 пикселов
            currentY += 5;
            // Смещаем вертикальную позицию компонента
            currentY += sizeY;
        }
    }
    // Метод вычисления оптимального размера контейнера
    private Dimension calculateBestSize(Container c)
    {
        // Вычисление длины контейнера
        Component[] list = c.getComponents();
        int maxWidth = 0;
        for (int i = 0; i < list.length; i++) {
            int width = list[i].getWidth();
            // Поиск компонента с максимальной длиной
            if ( width > maxWidth )
                maxWidth = width;
        }
        // Размер контейнера в длину с учетом левого отступа
        size.width = maxWidth + 5;
        // Вычисление высоты контейнера
        int height = 0;
        for (int i = 0; i < list.length; i++) {
            height += 5;
            height += list[i].getHeight();
        }
        size.height = height;
        return size;
    }
}
