package tile;

import main.GamePanel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class TileManager {

    GamePanel gp;
    public tile[] tile;
    public int mapTileNum[][];

    public TileManager(GamePanel gp) {
        this(gp, "worldmap001"); // Default constructor for backward compatibility
    }

    public TileManager(GamePanel gp, String mapName) {
        this.gp = gp;

        tile = new tile[60];
        mapTileNum = new int[gp.maxWorldCol][gp.maxWorldRow];

        getTileImage();
        loadMap("/maps/" + mapName + ".txt");
    }

    public void getTileImage() {
        // Setup all tiles with scaling
        setup(0, "tiles01/tile_000.png", false);
        setup(1, "tiles01/tile_001.png", true);
        setup(2, "tiles01/tile_002.png", true);
        setup(3, "tiles01/tile_003.png", false);
        setup(4, "tiles01/tile_004.png", true);
        setup(5, "tiles01/tile_005.png", true);
        setup(6, "tiles01/tile_006.png", true);
        setup(7, "tiles01/tile_007.png", true);
        setup(8, "tiles01/tile_008.png", true);
        setup(9, "tiles01/tile_009.png", true);

        setup(10, "tiles01/tile_010.png", true);
        setup(11, "tiles01/tile_011.png", true);
        setup(12, "tiles01/tile_012.png", false);
        setup(13, "tiles01/tile_013.png", false);
        setup(14, "tiles01/tile_014.png", true);
        setup(15, "tiles01/tile_015.png", true);
        setup(16, "tiles01/tile_016.png", false);
        setup(17, "tiles01/tile_017.png", false);
        setup(18, "tiles01/tile_018.png", false);
        setup(19, "tiles01/tile_019.png", false);

        setup(20, "tiles01/tile_020.png", true);
        setup(21, "tiles01/tile_021.png", true);
        setup(22, "tiles01/tile_022.png", true);
        setup(23, "tiles01/tile_023.png", false);
        setup(24, "tiles01/tile_024.png", false);
        setup(25, "tiles01/tile_025.png", true);
        setup(26, "tiles01/tile_026.png", true);
        setup(27, "tiles01/tile_027.png", true);
        setup(28, "tiles01/tile_028.png", true);
        setup(29, "tiles01/tile_029.png", false);

        setup(30, "tiles01/tile_030.png", true);
        setup(31, "tiles01/tile_031.png", true);
        setup(32, "tiles01/tile_032.png", true);
        setup(33, "tiles01/tile_033.png", true);
        setup(34, "tiles01/tile_034.png", false);
        setup(35, "tiles01/tile_035.png", false);
        setup(36, "tiles01/tile_036.png", false);
        setup(37, "tiles01/tile_037.png", true);
        setup(38, "tiles01/tile_038.png", true);
        setup(39, "tiles01/tile_039.png", true);

        setup(40, "tiles01/tile_040.png", true);
        setup(41, "tiles01/tile_041.png", true);
        setup(42, "tiles01/tile_042.png", true);
        setup(43, "tiles01/tile_043.png", true);
        setup(44, "tiles01/tile_044.png", false);
        setup(45, "tiles01/tile_045.png", false);
        setup(46, "tiles01/tile_046.png", false);
        setup(47, "tiles01/tile_047.png", false);
        setup(48, "tiles01/tile_048.png", false);
        setup(49, "tiles01/tile_049.png", false);
    }

    public void setup(int index, String filePath, boolean collision) {
        try {
            tile[index] = new tile();

            // Load the original image
            BufferedImage originalImage = ImageIO.read(getClass().getClassLoader().getResourceAsStream(filePath));

            // Check if image was loaded successfully
            if (originalImage != null) {
                // Scale the image to tileSize (48x48)
                Image scaledImage = originalImage.getScaledInstance(gp.tileSize, gp.tileSize, Image.SCALE_SMOOTH);

                // Convert scaled Image back to BufferedImage
                BufferedImage bufferedScaledImage = new BufferedImage(gp.tileSize, gp.tileSize, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2d = bufferedScaledImage.createGraphics();

                // Apply rendering hints for better quality
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Draw the scaled image
                g2d.drawImage(scaledImage, 0, 0, null);
                g2d.dispose();

                // Store the scaled image
                tile[index].image = bufferedScaledImage;
            } else {
                System.err.println("Failed to load image: " + filePath);
                // Create a placeholder image if loading fails
                tile[index].image = createPlaceholderTile();
            }

            tile[index].collision = collision;

        } catch (IOException e) {
            e.printStackTrace();
            // Create a placeholder image on error
            tile[index] = new tile();
            tile[index].image = createPlaceholderTile();
            tile[index].collision = collision;
        }
    }

    // Helper method to create a placeholder tile if image fails to load
    private BufferedImage createPlaceholderTile() {
        BufferedImage placeholder = new BufferedImage(gp.tileSize, gp.tileSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = placeholder.createGraphics();

        // Draw checkerboard pattern
        for (int y = 0; y < gp.tileSize; y += 8) {
            for (int x = 0; x < gp.tileSize; x += 8) {
                if ((x / 8 + y / 8) % 2 == 0) {
                    g2d.setColor(Color.MAGENTA);
                } else {
                    g2d.setColor(Color.BLACK);
                }
                g2d.fillRect(x, y, 8, 8);
            }
        }

        // Draw cross for visibility
        g2d.setColor(Color.RED);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(0, 0, gp.tileSize, gp.tileSize);
        g2d.drawLine(gp.tileSize, 0, 0, gp.tileSize);

        g2d.dispose();
        return placeholder;
    }

    public void loadMap(String filePath) {
        try {
            InputStream is = getClass().getResourceAsStream(filePath);
            if (is == null) {
                System.err.println("Map file not found: " + filePath);
                // Load a default map or create an empty one
                createDefaultMap();
                return;
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            int col = 0;
            int row = 0;

            while (col < gp.maxWorldCol && row < gp.maxWorldRow) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }

                while (col < gp.maxWorldCol) {
                    String numbers[] = line.split(" ");  //split the String at a space

                    int num = Integer.parseInt(numbers[col]); //col as an index number[] array

                    //store the extracted number in the mapTileNum[]
                    mapTileNum[col][row] = num;
                    col++;
                }
                if (col == gp.maxWorldCol) {
                    col = 0;
                    row++;
                }
            }
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
            createDefaultMap();
        }
    }

    private void createDefaultMap() {
        // Create a simple default map if loading fails
        for (int row = 0; row < gp.maxWorldRow; row++) {
            for (int col = 0; col < gp.maxWorldCol; col++) {
                // Create a checkerboard pattern
                if ((col + row) % 2 == 0) {
                    mapTileNum[col][row] = 0; // Grass
                } else {
                    mapTileNum[col][row] = 1; // Wall
                }
            }
        }
    }


    // abstract
    public void draw(Graphics2D g2) {
        int worldCol = 0;
        int worldRow = 0;

        while (worldCol < gp.maxWorldCol && worldRow < gp.maxWorldRow) {
            int tileNum = mapTileNum[worldCol][worldRow]; //extract a tile number which is store in mapTileNum[0][0]

            int worldX = worldCol * gp.tileSize;
            int worldY = worldRow * gp.tileSize;
            int screenX = worldX - gp.player.worldX + gp.player.screenX;
            int screenY = worldY - gp.player.worldY + gp.player.screenY;

            // Stop moving the camera at the edge
            if (gp.player.screenX > gp.player.worldX) {
                screenX = worldX;
            }
            if (gp.player.screenY > gp.player.worldY) {
                screenY = worldY;
            }

            int rightOffset = gp.screenWidth - gp.player.screenX;
            if (rightOffset > gp.worldWidth - gp.player.worldX) {
                screenX = gp.screenWidth - (gp.worldWidth - worldX);
            }
            int bottomOffset = gp.screenHeight - gp.player.screenY;
            if (bottomOffset > gp.worldHeight - gp.player.worldY) {
                screenY = gp.screenHeight - (gp.worldHeight - worldY);
            }

            if (worldX + gp.tileSize > gp.player.worldX - gp.player.screenX &&
                    worldX - gp.tileSize < gp.player.worldX + gp.player.screenX &&
                    worldY + gp.tileSize > gp.player.worldY - gp.player.screenY &&
                    worldY - gp.tileSize < gp.player.worldY + gp.player.screenY) {

                g2.drawImage(tile[tileNum].image, screenX, screenY, gp.tileSize, gp.tileSize, null);

            } else if (gp.player.screenX > gp.player.worldX ||
                    gp.player.screenY > gp.player.worldY ||
                    rightOffset > gp.worldWidth - gp.player.worldX ||
                    bottomOffset > gp.worldHeight - gp.player.worldY) {
                g2.drawImage(tile[tileNum].image, screenX, screenY, gp.tileSize, gp.tileSize, null);
            }

            worldCol++;

            if (worldCol == gp.maxWorldCol) {
                worldCol = 0;
                worldRow++;
            }
        }
    }
}