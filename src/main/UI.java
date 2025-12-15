package main;

import obj.OBJ_Heart;
import obj.SuperObject;

import java.awt.*;
import java.awt.image.BufferedImage;


import java.awt.BasicStroke;
import java.awt.FontMetrics;

public class UI {
    GamePanel gp;
    Graphics2D g2;
    BufferedImage heart_full, heart_half, heart_blank;
    BufferedImage tool1Image, tool2Image, tool3Image, tool4Image;
    BufferedImage postIcon;
    public int commandNum = 0;

    public boolean inLevelSelect = false;
    public int selectedLevel = 1;
    public UI(GamePanel gp) {
        this.gp = gp;

        try {
            tool1Image = javax.imageio.ImageIO.read(getClass().getClassLoader().getResourceAsStream("objects/tool1.png"));
            tool2Image = javax.imageio.ImageIO.read(getClass().getClassLoader().getResourceAsStream("objects/tool2.png"));
            tool3Image = javax.imageio.ImageIO.read(getClass().getClassLoader().getResourceAsStream("objects/tool3.png"));
            tool4Image = javax.imageio.ImageIO.read(getClass().getClassLoader().getResourceAsStream("objects/tool4.png"));

            postIcon = javax.imageio.ImageIO.read(getClass().getClassLoader().getResourceAsStream("objects/post.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // CREATE HUD OBJECTS
        SuperObject heart = new OBJ_Heart(gp);
        heart_full = heart.image;
        heart_half = heart.image2;
        heart_blank = heart.image3;
    }

    public void draw(Graphics2D g2) {
        this.g2 = g2;

        // TITLE SCREEN
        if(gp.gameState == gp.titleState) {
            if (inLevelSelect) {
                drawLevelSelectScreen(); // ADD THIS LINE
            } else {
                drawTitleScreen();
            }
        }

        // Draw HUD for all gameplay states except title
        if(gp.gameState == gp.playState ||
                gp.gameState == gp.pauseState ||
                gp.gameState == gp.settingsState ||
                gp.gameState == gp.gameOverState) {

            drawPlayerLife();
            drawToolInventory(g2);
            drawPostIndicator(g2);
        }

        // PAUSE STATE overlay
        if(gp.gameState == gp.pauseState) {
            drawPauseScreen(g2);
        }
    }

    private void drawPlayerLife() {
        int x = gp.tileSize/2;
        int y = gp.tileSize/2;

        // Calculate full and half hearts based on life points
        int fullHearts = gp.player.life / 2;
        int halfHearts = gp.player.life % 2;
        int blankHearts = (gp.player.maxLife / 2) - fullHearts - halfHearts;

        // Draw full hearts
        for (int i = 0; i < fullHearts; i++) {
            g2.drawImage(heart_full, x, y, null);
            x += gp.tileSize;
        }

        // Draw half heart if needed
        if (halfHearts > 0) {
            g2.drawImage(heart_half, x, y, null);
            x += gp.tileSize;
        }

        // Draw blank hearts
        for (int i = 0; i < blankHearts; i++) {
            g2.drawImage(heart_blank, x, y, null);
            x += gp.tileSize;
        }
    }

    public void drawTitleScreen() {
        // Draw background image if available
        try {
            BufferedImage backgroundImage = javax.imageio.ImageIO.read(
                    getClass().getClassLoader().getResourceAsStream("objects/The Bukidnon Blackout.png"));
            if (backgroundImage != null) {
                // Scale image to fit screen
                Image scaledImage = backgroundImage.getScaledInstance(gp.screenWidth, gp.screenHeight, Image.SCALE_SMOOTH);
                g2.drawImage(scaledImage, 0, 0, gp.screenWidth, gp.screenHeight, null);
            } else {
                // Fallback to black background if image not found
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
            }
        } catch (Exception e) {
            // Fallback to black background
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        }

        // TITLE NAME - Keep but make it more visible on top of background
        g2.setFont(new Font("Arial", Font.BOLD, 50));
        String text = "THE BUKIDNON BLACKOUT";
        int x = getXforCenteredText(text);
        int y = gp.tileSize * 2;

        // Text shadow for better visibility
        g2.setColor(Color.BLACK);
        g2.drawString(text, x + 5, y + 5);

        // Main text color
        g2.setColor(Color.yellow);
        g2.drawString(text, x, y);

        // PLAYER IMAGE
        x = gp.screenWidth/2 - (gp.tileSize*2)/2;
        y += gp.tileSize*1;
        g2.drawImage(gp.player.down1, x , y, gp.tileSize * 2, gp.tileSize * 2, null);

        // MENU - Add semi-transparent background for better readability
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(gp.screenWidth/2 - 180, y + gp.tileSize*2 - -15, 350, 250, 20, 15);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 40));

        text = "START";
        x = getXforCenteredText(text);
        y += gp.tileSize*3.5;
        g2.drawString(text, x, y);
        if(commandNum == 0) {
            g2.setColor(Color.YELLOW);
            g2.drawString(">", x-gp.tileSize, y);
        }

        text = "LOAD GAME";
        x = getXforCenteredText(text);
        y += gp.tileSize;
        g2.setColor(Color.WHITE);
        g2.drawString(text, x, y);
        if(commandNum == 1) {
            g2.setColor(Color.YELLOW);
            g2.drawString(">", x-gp.tileSize, y);
        }

        text = "QUIT";
        x = getXforCenteredText(text);
        y += gp.tileSize;
        g2.setColor(Color.WHITE);
        g2.drawString(text, x, y);
        if(commandNum == 2) {
            g2.setColor(Color.YELLOW);
            g2.drawString(">", x-gp.tileSize, y);
        }
    }

    public void drawPauseScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        g2.setFont(new Font("Arial", Font.BOLD, 80));
        g2.setColor(Color.YELLOW);
        String text = "PAUSED";
        int x = getXforCenteredText(text);
        int y = gp.screenHeight/2;
        g2.drawString(text, x, y);

        g2.setFont(new Font("Arial", Font.PLAIN, 24));
        g2.setColor(Color.WHITE);
        String instruction = "Press P to RESUME";
        int x2 = getXforCenteredText(instruction);
        g2.drawString(instruction, x2, y + 60);
    }

    public int getXforCenteredText(String text) {
        int length = (int)g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        return gp.screenWidth/2 - length/2;
    }

    private void drawToolInventory(Graphics2D g2) {
        int iconSize = 32;
        int spacing = 40;

        int totalWidth = (4 * spacing) - (spacing - iconSize);
        int startX = (gp.screenWidth - totalWidth) / 2;
        int startY = 60;

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        String label = "TOOLS: " + gp.player.toolsCollected + "/4";
        int labelWidth = g2.getFontMetrics().stringWidth(label);
        g2.drawString(label, (gp.screenWidth - labelWidth) / 2, startY - 3);

        for (int toolType = 1; toolType <= 4; toolType++) {
            int x = startX + ((toolType - 1) * spacing);

            BufferedImage toolImage = getToolImage(toolType);
            g2.drawImage(toolImage, x, startY, iconSize, iconSize, null);

            boolean hasTool = gp.player.isToolCollected(toolType);
            g2.setColor(hasTool ? Color.GREEN : Color.RED);
            g2.fillOval(x + 25, startY + 25, 10, 10);

            g2.setColor(Color.BLACK);
            g2.setFont(new Font("Arial", Font.BOLD, 10));
            String toolTypeStr = String.valueOf(toolType);
            int typeWidth = g2.getFontMetrics().stringWidth(toolTypeStr);
            g2.drawString(toolTypeStr, x + (iconSize - typeWidth) / 2, startY + 15);
        }
    }

    private void drawPostIndicator(Graphics2D g2) {
        int iconSize = 32;
        int x = gp.screenWidth - iconSize - 20;
        int y = 20;

        if (postIcon != null) {
            // Draw the post icon
            g2.drawImage(postIcon, x, y, iconSize, iconSize, null);

            // Draw green glow effect when player has all tools
            if (gp.player.toolsCollected >= 4) {
                // Pulsing green glow effect
                float pulse = (float)(Math.sin(System.currentTimeMillis() * 0.005) * 0.3 + 0.7);
                g2.setColor(new Color(0, 255, 0, (int)(100 * pulse)));

                // Draw multiple circles for glow effect
                for (int i = 0; i < 4; i++) {
                    g2.drawOval(x - i, y - i, iconSize + (i * 2), iconSize + (i * 2));
                }

                // Draw "READY!" text
                g2.setColor(Color.GREEN);
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                String readyText = "READY!";
                int textWidth = g2.getFontMetrics().stringWidth(readyText);
                g2.drawString(readyText, x + (iconSize - textWidth) / 2, y + iconSize + 15);
            } else {
                // Draw how many tools still needed
                g2.setColor(Color.YELLOW);
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                String neededText = "Need " + (4 - gp.player.toolsCollected);
                int textWidth = g2.getFontMetrics().stringWidth(neededText);
                g2.drawString(neededText, x + (iconSize - textWidth) / 2, y + iconSize + 15);
            }

            // REMOVED THE WHITE BORDER - Now it's just the transparent icon
        }
    }

    private BufferedImage getToolImage(int toolType) {
        switch(toolType) {
            case 1: return tool1Image;
            case 2: return tool2Image;
            case 3: return tool3Image;
            case 4: return tool4Image;
            default: return tool1Image;
        }
    }

    private String message = "";
    private long messageTime = 0;

    public void showMessage(String msg) {
        message = msg;
        messageTime = System.currentTimeMillis();
    }

    public void drawMessages(Graphics2D g2) {
        if (message.isEmpty() || System.currentTimeMillis() - messageTime > 3000) {
            message = "";
            return;
        }

        g2.setFont(new Font("Arial", Font.BOLD, 24));
        g2.setColor(Color.YELLOW);

        g2.setColor(Color.BLACK);
        g2.drawString(message, 302, 102);
        g2.drawString(message, 298, 102);
        g2.drawString(message, 300, 98);
        g2.drawString(message, 300, 102);

        g2.setColor(Color.YELLOW);
        g2.drawString(message, 300, 100);
    }

    public void drawLevelSelectScreen() {
        // Draw semi-transparent background
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // Title
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        g2.setColor(Color.YELLOW);
        String title = "SELECT LEVEL";
        int titleX = getXforCenteredText(title);
        g2.drawString(title, titleX, 100);

        // Level grid (2 rows x 5 columns)
        g2.setFont(new Font("Arial", Font.BOLD, 32));

        int levelBoxSize = 80;
        int spacing = 20;
        int startX = (gp.screenWidth - (5 * levelBoxSize + 4 * spacing)) / 2;
        int startY = 180;

        for (int level = 1; level <= 10; level++) {
            int col = (level - 1) % 5;
            int row = (level - 1) / 5;

            int boxX = startX + col * (levelBoxSize + spacing);
            int boxY = startY + row * (levelBoxSize + spacing);

            // Draw level box
            if (selectedLevel == level) {
                g2.setColor(new Color(255, 215, 0)); // Gold for selected
            } else {
                g2.setColor(new Color(100, 100, 120)); // Dark for unselected
            }
            g2.fillRoundRect(boxX, boxY, levelBoxSize, levelBoxSize, 15, 15);

            // Draw border
            g2.setColor(selectedLevel == level ? Color.YELLOW : Color.GRAY);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(boxX, boxY, levelBoxSize, levelBoxSize, 15, 15);

            // Draw level number
            g2.setColor(Color.WHITE);
            String levelText = String.valueOf(level);
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(levelText);
            g2.drawString(levelText, boxX + (levelBoxSize - textWidth) / 2,
                    boxY + levelBoxSize/2 + 10);

            // Draw time for level
            g2.setFont(new Font("Arial", Font.PLAIN, 12));
            int levelTime = 420 - ((level - 1) * 30); // Calculate time for this level
            String timeText = String.format("%d:%02d", levelTime / 60, levelTime % 60);
            int timeWidth = g2.getFontMetrics().stringWidth(timeText);
            g2.drawString(timeText, boxX + (levelBoxSize - timeWidth) / 2,
                    boxY + levelBoxSize - 15);
            g2.setFont(new Font("Arial", Font.BOLD, 32));
        }

        // Selected level info
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        g2.setColor(Color.WHITE);
        String selectedText = "Selected: Level " + selectedLevel;
        int selectedX = getXforCenteredText(selectedText);
        g2.drawString(selectedText, selectedX, 400);

        // Instructions
        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        g2.setColor(Color.YELLOW);

        String[] instructions = {
                "Use ARROW KEYS or WASD to Navigate",
                "Press ENTER to Start level",
                "Press ESC to go back"
        };

        for (int i = 0; i < instructions.length; i++) {
            int instX = getXforCenteredText(instructions[i]);
            g2.drawString(instructions[i], instX, 450 + i * 30);
        }
    }
}