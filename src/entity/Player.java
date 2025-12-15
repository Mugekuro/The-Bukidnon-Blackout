package entity;

import main.GamePanel;
import main.KeyHandler;
import main.UtilityTool;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class Player extends Entity {

    GamePanel gp;
    KeyHandler keyH;
    UtilityTool uTool = new UtilityTool();

    public final int screenX;
    public final int screenY;

    // Simple tool tracking
    public int toolsCollected = 0;
    public boolean[] collectedTools = new boolean[5]; // Index 1-4 for tool types

    public Player(GamePanel gp, KeyHandler keyH) {
        super(gp);

        this.gp = gp;
        this.keyH = keyH;

        screenX = gp.screenWidth/2 - (gp.tileSize/2);
        screenY = gp.screenHeight/2 - (gp.tileSize/2);

        solidArea = new Rectangle();
        solidArea.x = 8;
        solidArea.y = 16;
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;
        solidArea.width = 32;
        solidArea.height = 32;

        resetTools();

        setDefaultValues();
        getPlayerImage();
    }

    public void setDefaultValues() {
        if (gp != null && gp.tileSize > 0) {
            worldX = gp.tileSize * 6;
            worldY = gp.tileSize * 43;
        } else {
            worldX = 48 * 6;
            worldY = 48 * 43;
        }
        speed = 4;
        direction = "down";

        // PLAYER STATUS - 3 full hearts (6 life points)
        maxLife = 6; // 3 hearts * 2 points each
        life = maxLife; // Start with full life
    }

    public void getPlayerImage() {
        up1 = setup("player_up1");
        up2 = setup("player_up2");
        down1 = setup("player_down1");
        down2 = setup("player_down2");
        left1 = setup("player_left1");
        left2 = setup("player_left2");
        right1 = setup("player_right1");
        right2 = setup("player_right2");
    }

    public BufferedImage setup(String imageName) {
        UtilityTool uTool = new UtilityTool();
        BufferedImage image = null;

        try {
            image = ImageIO.read(getClass().getResourceAsStream("/player/" + imageName + ".png"));
            if (gp != null) {
                image = uTool.scaleImage(image, gp.tileSize, gp.tileSize);
            } else {
                image = uTool.scaleImage(image, 48, 48);
            }

        } catch(IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            image = new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setColor(Color.RED);
            g2d.fillRect(0, 0, 48, 48);
            g2d.setColor(Color.WHITE);
            g2d.drawString("P", 20, 30);
            g2d.dispose();
        }
        return image;
    }

    public void update() {
        if (keyH.upPressed == true || keyH.downPressed == true ||
                keyH.leftPressed == true || keyH.rightPressed) {

            if (keyH.upPressed == true) {
                direction = "up";
            } else if (keyH.downPressed == true) {
                direction = "down";
            } else if (keyH.leftPressed == true) {
                direction = "left";
            } else if (keyH.rightPressed == true) {
                direction = "right";
            }

            collisionOn = false;
            if (gp != null && gp.cChecker != null) {
                gp.cChecker.checkTile(this);
            }

            int objIndex = 999;
            if (gp != null && gp.cChecker != null) {
                objIndex = gp.cChecker.checkObject(this, true);
            }
            pickUpObject(objIndex);

            if (collisionOn == false) {
                switch (direction) {
                    case "up":
                        worldY -= speed;
                        break;
                    case "down":
                        worldY += speed;
                        break;
                    case "left":
                        worldX -= speed;
                        break;
                    case "right":
                        worldX += speed;
                        break;
                }
            }

            spriteCounter++;
            if (spriteCounter > 10) {
                if (spriteNum == 1) {
                    spriteNum = 2;
                } else if (spriteNum == 2) {
                    spriteNum = 1;
                }
                spriteCounter = 0;
            }
        }
    }

    public void pickUpObject(int i) {
        if(i != 999 && gp != null && gp.obj != null && i < gp.obj.length) {

            String objectName = gp.obj[i].name;

            switch(objectName) {
                case "Post":
                    if (toolsCollected >= 4) {
                        gp.gameState = gp.wirePuzzleState;
                        if (gp.wirePuzzle != null) {
                            gp.wirePuzzle.start();
                        }
                    } else {
                        int needed = 4 - toolsCollected;
                        if (gp.ui != null) {
                            gp.ui.showMessage("Need " + needed + " more tools to repair!");
                        }
                        System.out.println("Need " + needed + " more tools to repair the post!");
                    }
                    break;

                case "Tool":
                    obj.SuperObject toolObj = gp.obj[i];
                    int toolType = toolObj.toolPostGroup;

                    System.out.println("Picking up Tool" + toolType);

                    if (!collectedTools[toolType]) {
                        collectedTools[toolType] = true;
                        toolsCollected++;

                        // ADD THIS LINE:
                        gp.sound.play(1); // Play tool pickup sound

                        gp.obj[i] = null;

                        if (toolsCollected >= 4) {
                            System.out.println("🎉 Ready to repair the post!");
                        }
                    } else {
                        System.out.println("Already have Tool" + toolType);
                        gp.obj[i] = null;
                    }
                    break;

                case "Boots":
                    if (gp != null) {
                        gp.activateSpeedBoost(10);

                        // ADD THIS LINE:
                        gp.sound.play(2); // Play speed boost sound
                    }
                    gp.obj[i] = null;
                    System.out.println("Speed boost activated!");
                    break;
            }
        }
    }

    public void draw(Graphics2D g2) {
        BufferedImage image = null;

        switch (direction) {
            case "up":
                if(spriteNum == 1) {
                    image = up1;
                }
                if(spriteNum == 2) {
                    image = up2;
                }
                break;
            case "down":
                if(spriteNum == 1) {
                    image = down1;
                }
                if(spriteNum == 2) {
                    image = down2;
                }
                break;
            case "left":
                if(spriteNum == 1) {
                    image = left1;
                }
                if(spriteNum == 2) {
                    image = left2;
                }
                break;
            case "right":
                if(spriteNum == 1) {
                    image = right1;
                }
                if(spriteNum == 2) {
                    image = right2;
                }
                break;
        }

        if (image == null) return;

        int x = screenX;
        int y = screenY;

        if (gp == null) {
            g2.drawImage(image, x, y, null);
            return;
        }

        if(worldX < screenX) {
            x = worldX;
        }
        if(worldY < screenY) {
            y = worldY;
        }

        int rightOffset = gp.screenWidth - screenX;
        if(rightOffset > gp.worldWidth - worldX) {
            x = gp.screenWidth - (gp.worldWidth - worldX);
        }
        int bottomOffset = gp.screenHeight - screenY;
        if(bottomOffset > gp.worldHeight - worldY) {
            y = gp.screenHeight - (gp.worldHeight - worldY);
        }

        g2.drawImage(image, x, y, null);
    }

    public boolean isToolCollected(int toolType) {
        if (toolType >= 1 && toolType <= 4) {
            return collectedTools[toolType];
        }
        return false;
    }

    public void resetTools() {
        toolsCollected = 0;

        for (int i = 0; i < collectedTools.length; i++) {
            collectedTools[i] = false;
        }

        System.out.println("Tools reset for new level");
    }
}