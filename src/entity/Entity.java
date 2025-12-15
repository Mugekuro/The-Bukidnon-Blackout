package entity;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import main.GamePanel;
import main.UtilityTool;

import javax.imageio.ImageIO;

public class Entity {
    public GamePanel gp;
    public int worldX, worldY;
    public int speed;
    public BufferedImage up1, up2, down1, down2, left1, left2, right1, right2;
    public String direction;
    public int spriteCounter = 0;
    public int spriteNum = 1;
    public Rectangle solidArea = new Rectangle(0, 0, 48, 48);     //
    public boolean collisionOn = false;
    public int solidAreaDefaultY;
    public int solidAreaDefaultX;

    // CHARACTER STATUS
    public int maxLife;
    public int life;

    public Entity(GamePanel gp) {
        this.gp = gp;
    }

    public void draw(Graphics2D g2) {
        BufferedImage image = getCurrentImage();

        if (image == null) {
            image = createPlaceholderImage();
        }

        int screenX = worldX - gp.player.worldX + gp.player.screenX;
        int screenY = worldY - gp.player.worldY + gp.player.screenY;

        // Only draw if on screen
        if (screenX + gp.tileSize > 0 && screenX < gp.screenWidth &&
                screenY + gp.tileSize > 0 && screenY < gp.screenHeight) {
            g2.drawImage(image, screenX, screenY, gp.tileSize, gp.tileSize, null);
        }
    }

    private BufferedImage getCurrentImage() {
        switch (direction) {
            case "up": return (spriteNum == 1) ? up1 : up2;
            case "down": return (spriteNum == 1) ? down1 : down2;
            case "left": return (spriteNum == 1) ? left1 : left2;
            case "right": return (spriteNum == 1) ? right1 : right2;
            default: return down1;
        }
    }

    // Base update - to be overridden
    public void update() {
        // Empty - subclasses implement their own logic
    }

    public BufferedImage setup(String imagePath) {
        UtilityTool uTool = new UtilityTool();
        BufferedImage image = null;

        try {
            image = ImageIO.read(getClass().getResourceAsStream(imagePath + ".png"));
            if (image == null) {
                image = ImageIO.read(getClass().getResourceAsStream(imagePath));
            }

            if (image != null && gp != null) {
                image = uTool.scaleImage(image, gp.tileSize, gp.tileSize);
            }

        } catch (Exception e) {
            System.err.println("Error loading image: " + imagePath);
        }

        return image;
    }

    private BufferedImage createPlaceholderImage() {
        BufferedImage placeholder = new BufferedImage(48, 48, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = placeholder.createGraphics();
        g2d.setColor(Color.CYAN);
        g2d.fillRect(0, 0, 48, 48);
        g2d.setColor(Color.BLACK);
        g2d.drawString("NPC", 10, 30);
        g2d.dispose();
        return placeholder;
    }
}