package main;

import entity.NPC_Lady;
import obj.OBJ_Boots;
import obj.OBJ_Post;
import obj.SuperObject;
import java.util.Random;
import java.util.ArrayList;

public class AssetSetter {
    GamePanel gp;
    Random random;

    private int[] pathwayTileNumbers = {12, 13, 16, 17, 18, 19, 23, 24};

    // Tile number for tile_012.png (from TileManager setup)
    private final int POST_TILE_TYPE = 12; // tile_012.png

    public AssetSetter(GamePanel gp) {
        this.gp = gp;
        this.random = new Random();
    }

    public void setObject() {
        // Clear existing objects
        for (int i = 0; i < gp.obj.length; i++) {
            gp.obj[i] = null;
        }

        System.out.println("=== Setting up Level " + gp.currentLevel + " ===");

        setupSimpleLevel();
    }

    private void setupSimpleLevel() {
        // First, collect all positions of tile_012.png for the post
        ArrayList<Position> postTilePositions = new ArrayList<>();

        // Also collect pathway positions for tools and boots
        ArrayList<Position> pathwayPositions = new ArrayList<>();

        for (int col = 0; col < gp.maxWorldCol; col++) {
            for (int row = 0; row < gp.maxWorldRow; row++) {
                int tileNum = gp.tileM.mapTileNum[col][row];

                // Check if this is tile_012.png (post tile)
                if (tileNum == POST_TILE_TYPE && !gp.tileM.tile[tileNum].collision && isValidPosition(col, row)) {
                    postTilePositions.add(new Position(col, row, tileNum));
                }

                // Also collect all pathway tiles for tools
                if (isPathwayTile(tileNum) && !gp.tileM.tile[tileNum].collision && isValidPosition(col, row)) {
                    pathwayPositions.add(new Position(col, row, tileNum));
                }
            }
        }

        System.out.println("Available tile_012.png (post) tiles: " + postTilePositions.size());
        System.out.println("Available pathway tiles for tools: " + pathwayPositions.size());

        // Shuffle positions
        java.util.Collections.shuffle(postTilePositions);
        java.util.Collections.shuffle(pathwayPositions);

        int objIndex = 0;

        // Place 4 tools (one of each type) on pathway tiles
        for (int toolType = 1; toolType <= 4 && objIndex < pathwayPositions.size(); toolType++) {
            Position pos = pathwayPositions.get(objIndex);

            SuperObject tool = new SuperObject();
            tool.name = "Tool";
            tool.worldX = pos.col * gp.tileSize;
            tool.worldY = pos.row * gp.tileSize;
            tool.toolPostGroup = toolType;

            // Load image
            try {
                String imageName = "objects/tool" + toolType + ".png";
                tool.image = javax.imageio.ImageIO.read(
                        getClass().getClassLoader().getResourceAsStream(imageName));
            } catch (Exception e) {
                e.printStackTrace();
            }

            gp.obj[objIndex] = tool;
            System.out.println("Placed Tool" + toolType + " at (" + pos.col + "," + pos.row + ") on tile_" +
                    String.format("%03d", pos.tileNum));
            objIndex++;
        }

        // Place 1 post on a random tile_012.png
        if (!postTilePositions.isEmpty()) {
            Position postPos = postTilePositions.get(0);

            OBJ_Post post = new OBJ_Post();
            post.worldX = postPos.col * gp.tileSize;
            post.worldY = postPos.row * gp.tileSize;
            post.name = "Post";
            post.collision = true;

            gp.obj[objIndex] = post;
            System.out.println("Placed Post at (" + postPos.col + "," + postPos.row + ") on tile_012.png");
            objIndex++;
        } else {
            System.out.println("WARNING: No tile_012.png found for post placement!");
            // Fallback to any valid position
            for (int i = objIndex; i < pathwayPositions.size(); i++) {
                Position pos = pathwayPositions.get(i);

                boolean tooCloseToTool = false;
                for (int j = 0; j < objIndex; j++) {
                    if (gp.obj[j] != null) {
                        int toolX = gp.obj[j].worldX / gp.tileSize;
                        int toolY = gp.obj[j].worldY / gp.tileSize;
                        double distance = Math.sqrt(Math.pow(pos.col - toolX, 2) + Math.pow(pos.row - toolY, 2));
                        if (distance < 5) {
                            tooCloseToTool = true;
                            break;
                        }
                    }
                }

                if (!tooCloseToTool) {
                    OBJ_Post post = new OBJ_Post();
                    post.worldX = pos.col * gp.tileSize;
                    post.worldY = pos.row * gp.tileSize;
                    post.name = "Post";
                    post.collision = true;

                    gp.obj[objIndex] = post;
                    System.out.println("Placed Post (fallback) at (" + pos.col + "," + pos.row + ")");
                    objIndex++;
                    break;
                }
            }
        }

        // Place boots on remaining pathway tile
        for (int i = objIndex; i < pathwayPositions.size(); i++) {
            Position pos = pathwayPositions.get(i);
            boolean positionUsed = false;

            for (int j = 0; j < objIndex; j++) {
                if (gp.obj[j] != null &&
                        gp.obj[j].worldX == pos.col * gp.tileSize &&
                        gp.obj[j].worldY == pos.row * gp.tileSize) {
                    positionUsed = true;
                    break;
                }
            }

            if (!positionUsed) {
                gp.obj[objIndex] = new OBJ_Boots();
                gp.obj[objIndex].worldX = pos.col * gp.tileSize;
                gp.obj[objIndex].worldY = pos.row * gp.tileSize;
                System.out.println("Placed Boots at (" + pos.col + "," + pos.row + ")");
                break;
            }
        }

        System.out.println("=== Level " + gp.currentLevel + " Setup Complete ===");
    }

    private boolean isPathwayTile(int tileNum) {
        for (int pathwayNum : pathwayTileNumbers) {
            if (tileNum == pathwayNum) return true;
        }
        return false;
    }

    private boolean isValidPosition(int col, int row) {
        if (col == 2 && row == 43) return false;
        return col >= 5 && col < gp.maxWorldCol - 5 &&
                row >= 5 && row < gp.maxWorldRow - 5;
    }

    private class Position {
        int col, row, tileNum;
        Position(int col, int row, int tileNum) {
            this.col = col;
            this.row = row;
            this.tileNum = tileNum;
        }
    }

    public void setNPC() {
        System.out.println("Setting up NPC...");
        gp.npc[0] = new NPC_Lady(gp);

        int npcCol = 25;
        int npcRow = 25;

        int[] walkableTiles = {0, 3, 12, 13, 16, 17, 18, 19, 23, 24, 29, 34, 35, 36, 44, 45, 46, 47, 48, 49};

        for (int attempt = 0; attempt < 1000; attempt++) {
            npcCol = 10 + random.nextInt(gp.maxWorldCol - 20);
            npcRow = 10 + random.nextInt(gp.maxWorldRow - 20);

            int tileNum = gp.tileM.mapTileNum[npcCol][npcRow];

            for (int walkableTile : walkableTiles) {
                if (tileNum == walkableTile) {
                    if (gp.tileM.tile[tileNum] != null && !gp.tileM.tile[tileNum].collision) {
                        if (isValidPosition(npcCol, npcRow)) {
                            gp.npc[0].worldX = npcCol * gp.tileSize;
                            gp.npc[0].worldY = npcRow * gp.tileSize;
                            System.out.println("NPC placed at (" + npcCol + "," + npcRow + ")");
                            return;
                        }
                    }
                }
            }
        }

        gp.npc[0].worldX = npcCol * gp.tileSize;
        gp.npc[0].worldY = npcRow * gp.tileSize;
        System.out.println("NPC at fallback position");
    }
}