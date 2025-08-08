package pl.julkot1.game.map;

import com.badlogic.gdx.graphics.Color;
import java.util.Random;

public class HexMap {
    /**
     * Represents a tile in the hexagonal map with color and height.
     */
    public static interface HexMapIterator{
        void iterate(int row, int col, Tile tile);
    }

    public void iterate(HexMapIterator iterator) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Tile tile = tiles[row][col];
                iterator.iterate(row, col, tile);
            }
        }
    }


    public static class Colors {
        float baseR;
        float baseG;
        float BaseB;

        float randomFactorR;
        float randomFactorG;
        float randomFactorB;
        /**
         * Constructs a Colors object with specified base colors and random factors.
         *
         * @param baseR Base red component (0.0 to 1.0).
         * @param baseG Base green component (0.0 to 1.0).
         * @param baseB Base blue component (0.0 to 1.0).
         * @param randomFactorR Random factor for red component (0.0 to 1.0).
         * @param randomFactorG Random factor for green component (0.0 to 1.0).
         * @param randomFactorB Random factor for blue component (0.0 to 1.0).
         */
        public Colors(float baseR, float baseG, float baseB, float randomFactorR, float randomFactorG, float randomFactorB) {
            this.baseR = baseR;
            this.baseG = baseG;
            this.BaseB = baseB;
            this.randomFactorR = randomFactorR;
            this.randomFactorG = randomFactorG;
            this.randomFactorB = randomFactorB;
        }
        public Color getRandomColor() {
            Random rand = new Random();
            float r = (1-randomFactorR)*baseR + randomFactorR * rand.nextFloat();
            float g = (1-randomFactorG)*baseG + randomFactorG * rand.nextFloat();
            float b = (1-randomFactorG)*BaseB + randomFactorB * rand.nextFloat();
            return new Color(r, g, b, 1f);
        }
        public static Colors randomColors() {
            Random rand = new Random();
            float baseR = rand.nextFloat();
            float baseG = rand.nextFloat();
            float baseB = rand.nextFloat();
            float randomFactorR = 0;
            float randomFactorG = 0;
            float randomFactorB = 0;
            int randChanel = rand.nextInt(3);
            switch (randChanel) {
                case 0:
                    randomFactorR = 0.6f + rand.nextFloat() * 0.4f;
                    break;
                case 1:
                    randomFactorG = 0.6f + rand.nextFloat() * 0.4f;
                    break;
                case 2:
                    randomFactorB = 0.6f + rand.nextFloat() * 0.4f;
                    break;
            }

            return new Colors(baseR, baseG, baseB, randomFactorR, randomFactorG, randomFactorB);
        }

    }
    private final int rows;
    private final int cols;
    private final Tile[][] tiles;

    /**
     * Creates a hexagonal map with the specified number of rows and columns.
     * Each tile is initialized with a random color based on the provided Colors object.
     *
     * @param rows Number of rows in the hex map.
     * @param cols Number of columns in the hex map.
     * @param colors Colors object to generate random colors for tiles.
     */
    public HexMap(int rows, int cols, Colors colors) {
        this.rows = rows;
        this.cols = cols;
        this.tiles = new Tile[rows][cols];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Tile tile = new Tile(colors.getRandomColor());
                //tile.setHeight(0.3f + rand.nextFloat() * 2.0f); // Height between 0.3 and 2.3
                tiles[row][col] = tile;
            }
        }
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public Color getColor(int row, int col) {
        return tiles[row][col].getColor();
    }

    public Tile getTile(int row, int col) {
        return tiles[row][col];
    }

    public float getHeight(int row, int col) {
        return tiles[row][col].getHeight();
    }

}
