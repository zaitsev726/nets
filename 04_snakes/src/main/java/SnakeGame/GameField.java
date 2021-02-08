package SnakeGame;

import me.ippolitov.fit.snakes.SnakesProto;

import java.util.List;

public class GameField {

    private static int[][] gameField = null;
    private static int width = 0;
    private static int height = 0;

    public static int[][] getGameField() {
        return gameField;
    }

    public static void setGameField(int[][] gameField) {
        GameField.gameField = gameField;
    }

    public GameField(int width, int height) {
        GameField.width = width;
        GameField.height = height;

        gameField = new int[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                gameField[i][j] = 0;
            }
        }
    }

    //отрисовка змеи на поле
    private static void paintSnake(SnakesProto.GameState.Snake snake) {
        List coords = snake.getPointsList();
        int ID = snake.getPlayerId();

        int tail = ID + 1;
        int head = -tail;


        SnakesProto.GameState.Coord coord = (SnakesProto.GameState.Coord) coords.get(0);
        int currentX = coord.getX();
        int currentY = coord.getY();

        gameField[currentX][currentY] = head;

        for (int i = 1; i < coords.size(); i++) {
            coord = (SnakesProto.GameState.Coord) coords.get(i);
            int x = coord.getX();
            int y = coord.getY();
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
                gameField[currentX][currentY] = tail;
            }
        }

    }

    //рандомим координаты для первого игрока
    public static int[] randomCoord() {
        if (height == 0 || width == 0)
            return null;
        int[] a = new int[2];
        a[0] = (int) (Math.random() * width);
        a[1] = (int) (Math.random() * height);
        return a;
    }

    //ищем квадрат 5 на 5 для нового игрока
    public static int[] getCoordForSpawn() {
        if (gameField != null) {
            synchronized (GameField.class) {
                int[] a = new int[2];
                boolean canSpawn = true;
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        canSpawn = true;
                        if (gameField[i][j] == 0) {
                            for (int k = 0; k < 5; k++) {
                                for (int m = 0; m < 5; m++) {
                                    if (gameField[i+k][j+m] != 0 && canSpawn) {
                                        canSpawn = false;
                                    }
                                }
                            }
                            if (canSpawn) {
                                a[0] = i + 3;
                                a[1] = j + 2;
                                return a;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    //обновляем поле
    public static int[][] updateField(SnakesProto.GameState state) {

        synchronized (GameField.class) {
                gameField = new int[state.getConfig().getWidth()][state.getConfig().getHeight()];
                width = state.getConfig().getWidth();
                height = state.getConfig().getHeight();
                for (int i = 0; i < state.getConfig().getWidth(); i++) {
                        for (int j = 0; j < state.getConfig().getHeight(); j++) {
                            gameField[i][j] = 0;
                        }
                }

            List<SnakesProto.GameState.Snake> snakes = state.getSnakesList();

            for (int i = 0; i < snakes.size(); i++) {
                SnakesProto.GameState.Snake snake = snakes.get(i);
                paintSnake(snake);
            }

            for (int i = 0; i < state.getFoodsCount(); i++) {
                SnakesProto.GameState.Coord foodCoord = state.getFoods(i);
                if (gameField[foodCoord.getX()][foodCoord.getY()] != 0) {
                    System.out.println("ERRROR");
                } else
                    gameField[foodCoord.getX()][foodCoord.getY()] = 1;
            }

            return gameField;
        }
    }

}
