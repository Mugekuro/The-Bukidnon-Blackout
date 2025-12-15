package main;

import entity.Entity;
import entity.Player;
import obj.SuperObject;
import tile.TileManager;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.*;

public class GamePanel extends JPanel implements Runnable {

    //Screen Settings
    final int originalTileSize = 16;
    final int scale = 3;

    public final int tileSize = originalTileSize * scale;
    public final int maxScreenCol = 16;
    public final int maxScreenRow = 12;
    public final int screenWidth = tileSize * maxScreenCol;
    public final int screenHeight = tileSize * maxScreenRow;

    // World Settings
    public final int maxWorldCol = 50;
    public final int maxWorldRow = 50;
    public final int worldWidth = tileSize * maxWorldCol;
    public final int worldHeight = tileSize * maxWorldRow;

    // SOUND INDICES
    public static final int SOUND_BACKGROUND = 0;
    public static final int SOUND_TOOL_PICKUP = 1;
    public static final int SOUND_SPEED_BOOST = 2;
    public static final int SOUND_PUZZLE_SUCCESS = 3;
    public static final int SOUND_LEVEL_COMPLETE = 4;

    // FPS
    int FPS = 60;

    // SYSTEM
    public TileManager tileM;
    public KeyHandler keyH = new KeyHandler(this);
    public CollisionChecker cChecker;
    public AssetSetter aSetter;
    public UI ui;
    public Sound sound = new Sound();
    Thread gameThread;

    // ENTITY AND OBJECT
    public Player player;
    public SuperObject obj[] = new SuperObject[10];
    public Entity npc[] = new Entity[10];

    // Speed boost timer
    private long speedBoostEndTime = 0;
    private int originalSpeed = 4;

    // Level progression
    public int currentLevel = 1;
    public final int MAX_LEVEL = 10; // Maximum level limit
    public String[] availableMaps = {"worldmap001", "worldmap002", "worldmap02"};
    public String currentMap = "";

    // Level timer
    private long levelStartTime;
    private int levelTimeLimit; // in milliseconds
    private boolean levelTimerActive = true;

    // GAME STATE
    public final int titleState = 0;
    public final int playState = 1;
    public final int pauseState = 2;
    public final int wirePuzzleState = 3;
    public final int levelCompleteState = 4;
    public final int gameOverState = 5; // New state for time out
    public final int settingsState = 6; // Settings state

    public int gameState = playState;

    // Minigame instance
    public WirePuzzleMinigame wirePuzzle;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);

        keyH = new KeyHandler(this);
        this.addKeyListener(keyH);
        this.setFocusable(true);

        cChecker = new CollisionChecker(this);
        aSetter = new AssetSetter(this);
        ui = new UI(this);
        sound = new Sound(); // Initialize sound

        loadRandomMap();

        player = new Player(this, keyH);
        wirePuzzle = new WirePuzzleMinigame(this);

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (gameState == wirePuzzleState) {
                    wirePuzzle.handleMousePress(e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (gameState == wirePuzzleState) {
                    wirePuzzle.handleMouseRelease(e.getX(), e.getY());
                }
            }
        });

        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (gameState == wirePuzzleState) {
                    wirePuzzle.handleMouseDrag(e.getX(), e.getY());
                }
            }
        });

        originalSpeed = player.speed;

        // Initialize level timer
        initializeLevelTimer();

        // ADD THIS LINE AT THE END OF CONSTRUCTOR:
        sound.loop(0); // Start background music
    }

    void initializeLevelTimer() {
        // Calculate time for current level
        // Level 1: 7 minutes = 420 seconds
        // Each level decreases by 30 seconds
        // Level 10: 420 - (9 * 30) = 420 - 270 = 150 seconds = 2.5 minutes
        int seconds = 420 - ((currentLevel - 1) * 30);
        levelTimeLimit = seconds * 1000; // Convert to milliseconds
        levelStartTime = System.currentTimeMillis();
        levelTimerActive = true;

        System.out.println("Level " + currentLevel + " Timer: " + seconds + " seconds");
    }

    public long getRemainingLevelTime() {
        if (!levelTimerActive || gameState != playState) {
            return 0;
        }

        long elapsed = System.currentTimeMillis() - levelStartTime;
        long remaining = levelTimeLimit - elapsed;

        return Math.max(0, remaining);
    }

    public String getFormattedTime() {
        long remainingMillis = getRemainingLevelTime();
        if (remainingMillis <= 0) {
            return "00:00";
        }

        int totalSeconds = (int)(remainingMillis / 1000);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    private void checkLevelTimer() {
        if (levelTimerActive && gameState == playState) {
            if (getRemainingLevelTime() <= 0) {
                // Time's up! Go to game over state
                levelTimerActive = false;
                gameState = gameOverState;
                System.out.println("TIME'S UP! Level " + currentLevel + " failed!");
            }
        }
    }

    private void drawLevelTimer(Graphics2D g2) {
        // Only draw in play state
        if (gameState == playState) {
            // Position timer vertically centered between tools and post icon
            int timerX = screenWidth - 220; // Adjusted from 220 to 120
            int timerY = 25; // Between post icon (y=20) and tools (y=60)

            // Timer background
            g2.setColor(new Color(30, 30, 40, 200));
            g2.fillRoundRect(timerX, timerY, 100, 40, 10, 10);

            // Timer border
            g2.setColor(new Color(255, 215, 0, 150));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(timerX, timerY, 100, 40, 10, 10);

            // Timer text
            String timeText = getFormattedTime();
            g2.setFont(new Font("Arial", Font.BOLD, 16));

            // Color based on remaining time
            long remainingMillis = getRemainingLevelTime();
            int totalSeconds = (int)(remainingMillis / 1000);

            if (totalSeconds > 60) {
                g2.setColor(Color.GREEN);
            } else if (totalSeconds > 30) {
                g2.setColor(Color.YELLOW);
            } else {
                // Blinking red effect when time is low
                boolean blinkOn = (System.currentTimeMillis() / 500) % 2 == 0;
                g2.setColor(blinkOn ? Color.RED : new Color(255, 100, 100));
            }

            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(timeText);
            g2.drawString(timeText, timerX + (100 - textWidth) / 2, timerY + 25);

            // Draw "TIME" label above timer
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 10));
            String label = "LEVEL TIME";
            int labelWidth = g2.getFontMetrics().stringWidth(label);
            g2.drawString(label, timerX + (100 - labelWidth) / 2, timerY + 12);
        }
    }

    void loadRandomMap() {
        int mapIndex = (int)(Math.random() * availableMaps.length);
        currentMap = availableMaps[mapIndex];
        tileM = new TileManager(this, currentMap);

        System.out.println("Loading Level " + currentLevel + " with map: " + currentMap);
    }

    public void advanceToNextLevel() {
        if (currentLevel >= MAX_LEVEL) {
            // Game completed!
            System.out.println("CONGRATULATIONS! You completed all " + MAX_LEVEL + " levels!");
            gameState = gameOverState;
            return;
        }

        currentLevel++;
        loadRandomMap();
        aSetter.setObject();
        aSetter.setNPC();
        gameState = playState;

        // Reset player
        player.setDefaultValues();
        player.resetTools();

        // Reset the wire puzzle for new level
        wirePuzzle.resetPuzzle();

        // Reset level timer for new level
        initializeLevelTimer();

        System.out.println("Advanced to Level " + currentLevel);
    }

    public void postRepaired() {
        sound.play(4); // Play level complete sound

        gameState = levelCompleteState;

        System.out.println("Level " + currentLevel + " complete! Advancing to next level...");
        gameState = levelCompleteState;

        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        advanceToNextLevel();
                    }
                },
                3000
        );
    }

    public void setupGame() {
        aSetter.setObject();
        aSetter.setNPC();

        gameState = titleState;
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000/FPS;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;
        long timer = 0;
        int drawCount = 0;

        while(gameThread != null) {
            currentTime = System.nanoTime();

            delta += (currentTime - lastTime) / drawInterval;
            timer += (currentTime - lastTime);
            lastTime = currentTime;

            if(delta >= 1) {
                update();
                repaint();
                delta--;
                drawCount++;
            }
        }
    }

    public void update() {
        checkSpeedBoost();
        checkLevelTimer(); // Check if time's up

        switch(gameState) {
            case playState:
                player.update();

                for (int i = 0; i < npc.length; i++) {
                    if (npc[i] != null) {
                        npc[i].update();
                    }
                }
                break;

            case wirePuzzleState:
                wirePuzzle.update();
                break;

            case levelCompleteState:
                // Nothing to update
                break;

            case gameOverState:
                // Game over screen
                break;

            case settingsState:
                // Settings screen
                break;
        }
    }

    private void checkSpeedBoost() {
        if (speedBoostEndTime > 0 && System.currentTimeMillis() > speedBoostEndTime) {
            player.speed = originalSpeed;
            speedBoostEndTime = 0;
            System.out.println("Speed boost expired!");
        }
    }

    public void activateSpeedBoost(int durationSeconds) {
        player.speed += 2;
        speedBoostEndTime = System.currentTimeMillis() + (durationSeconds * 1000);
        System.out.println("Speed boost activated for " + durationSeconds + " seconds!");
    }

    public int getRemainingBoostTime() {
        if (speedBoostEndTime == 0) return 0;
        long remaining = speedBoostEndTime - System.currentTimeMillis();
        return (int) Math.max(0, remaining / 1000);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;

        // TITLE SCREEN
        if(gameState == titleState) {
            ui.draw(g2);
            return;
        }

        long drawStart = 0;
        if(keyH.checkDrawTime == true) {
            drawStart = System.nanoTime();
        }

        switch(gameState) {
            case playState:
            case pauseState:
                tileM.draw(g2);

                for (int i = 0; i < obj.length; i++) {
                    if (obj[i] != null) {
                        obj[i].draw(g2, this);
                    }
                }

                for (int i = 0; i < npc.length; i++) {
                    if (npc[i] != null) {
                        npc[i].draw(g2);
                    }
                }

                player.draw(g2);
                break;

            case wirePuzzleState:
                wirePuzzle.draw(g2);
                break;

            case levelCompleteState:
                drawLevelCompleteScreen(g2);
                break;

            case gameOverState:
                // Draw game behind game over
                tileM.draw(g2);
                for (int i = 0; i < obj.length; i++) {
                    if (obj[i] != null) {
                        obj[i].draw(g2, this);
                    }
                }
                for (int i = 0; i < npc.length; i++) {
                    if (npc[i] != null) {
                        npc[i].draw(g2);
                    }
                }
                player.draw(g2);
                ui.draw(g2);
                drawGameOverScreen(g2); // Draw game over overlay
                break;

            case settingsState:
                // Draw game behind settings
                tileM.draw(g2);
                for (int i = 0; i < obj.length; i++) {
                    if (obj[i] != null) {
                        obj[i].draw(g2, this);
                    }
                }
                for (int i = 0; i < npc.length; i++) {
                    if (npc[i] != null) {
                        npc[i].draw(g2);
                    }
                }
                player.draw(g2);
                ui.draw(g2);
                drawSettingsScreen(g2); // Draw settings overlay
                break;
        }

        ui.draw(g2);

        if(gameState == pauseState) {
            ui.drawPauseScreen(g2);
        }

        // Draw level timer only if not in wire puzzle state
        if (gameState != wirePuzzleState && gameState != settingsState && gameState != gameOverState) {
            drawLevelTimer(g2);
        }

        drawLevelInfo(g2);

        if (speedBoostEndTime > 0 && gameState != wirePuzzleState && gameState != settingsState && gameState != gameOverState) {
            int remaining = getRemainingBoostTime();
            if (remaining > 0) {
                g2.setColor(Color.YELLOW);
                g2.setFont(new Font("Arial", Font.BOLD, 14));
                g2.drawString("Speed Boost: " + remaining + "s", 330, 500);
            }
        }

        if(keyH.checkDrawTime == true) {
            long drawEnd = System.nanoTime();
            long passed = drawEnd - drawStart;
            g2.setColor(Color.white);
            g2.drawString("Draw Time: " + passed, 10, 400);
        }
    }

    private void drawLevelInfo(Graphics2D g2) {
        // Only draw level info if not in title state
        if (gameState != titleState) {
            // Level display at top center
            g2.setColor(Color.YELLOW);
            g2.setFont(new Font("Arial", Font.BOLD, 24));
            String levelText = "LEVEL " + currentLevel + "/" + MAX_LEVEL;
            int levelWidth = g2.getFontMetrics().stringWidth(levelText);
            g2.drawString(levelText, (screenWidth - levelWidth) / 2, 30);
        }
    }

    private void drawLevelCompleteScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setColor(Color.YELLOW);
        g2.setFont(new Font("Arial", Font.BOLD, 60));
        String text = "LEVEL " + currentLevel + " COMPLETE!";
        int textWidth = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, (screenWidth - textWidth) / 2, screenHeight / 2 - 30);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.PLAIN, 24));

        if (currentLevel < MAX_LEVEL) {
            String nextLevelText = "Advancing to Level " + (currentLevel + 1) + "...";
            int nextWidth = g2.getFontMetrics().stringWidth(nextLevelText);
            g2.drawString(nextLevelText, (screenWidth - nextWidth) / 2, screenHeight / 2 + 30);

            // Show next level time
            int nextLevelSeconds = 420 - (currentLevel * 30); // Next level time
            String timeText = "Next level time: " + (nextLevelSeconds / 60) + ":" +
                    String.format("%02d", nextLevelSeconds % 60);
            int timeWidth = g2.getFontMetrics().stringWidth(timeText);
            g2.drawString(timeText, (screenWidth - timeWidth) / 2, screenHeight / 2 + 60);
        } else {
            String completeText = "CONGRATULATIONS! GAME COMPLETE!";
            int completeWidth = g2.getFontMetrics().stringWidth(completeText);
            g2.drawString(completeText, (screenWidth - completeWidth) / 2, screenHeight / 2 + 30);
        }
    }

    private void drawGameOverScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        // Check if it's time out or game completed
        boolean isTimeOut = getRemainingLevelTime() <= 0 && levelTimerActive;

        if (isTimeOut) {
            // Time out screen
            g2.setColor(Color.RED);
            g2.setFont(new Font("Arial", Font.BOLD, 60));
            String text = "TIME'S UP!";
            int textWidth = g2.getFontMetrics().stringWidth(text);
            g2.drawString(text, (screenWidth - textWidth) / 2, screenHeight / 2 - 30);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.PLAIN, 24));
            String levelText = "Level " + currentLevel + " Failed";
            int levelWidth = g2.getFontMetrics().stringWidth(levelText);
            g2.drawString(levelText, (screenWidth - levelWidth) / 2, screenHeight / 2 + 30);

            // Show retry and quit options
            g2.setFont(new Font("Arial", Font.PLAIN, 20));
            String retryText = "Press R to Retry Level";
            int retryWidth = g2.getFontMetrics().stringWidth(retryText);
            g2.drawString(retryText, (screenWidth - retryWidth) / 2, screenHeight / 2 + 70);

            String quitText = "Press Q to Quit";
            int quitWidth = g2.getFontMetrics().stringWidth(quitText);
            g2.drawString(quitText, (screenWidth - quitWidth) / 2, screenHeight / 2 + 100);
        } else if (currentLevel > MAX_LEVEL) {
            // Game completed screen
            g2.setColor(Color.YELLOW);
            g2.setFont(new Font("Arial", Font.BOLD, 60));
            String text = "VICTORY!";
            int textWidth = g2.getFontMetrics().stringWidth(text);
            g2.drawString(text, (screenWidth - textWidth) / 2, screenHeight / 2 - 30);

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.PLAIN, 24));
            String completeText = "You completed all " + MAX_LEVEL + " levels!";
            int completeWidth = g2.getFontMetrics().stringWidth(completeText);
            g2.drawString(completeText, (screenWidth - completeWidth) / 2, screenHeight / 2 + 30);

            String restartText = "Press R to Restart Game";
            int restartWidth = g2.getFontMetrics().stringWidth(restartText);
            g2.drawString(restartText, (screenWidth - restartWidth) / 2, screenHeight / 2 + 70);
        }
    }

    private void drawSettingsScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setFont(new Font("Arial", Font.BOLD, 60));
        g2.setColor(Color.YELLOW);
        String text = "SETTINGS";
        int textWidth = g2.getFontMetrics().stringWidth(text);
        g2.drawString(text, (screenWidth - textWidth) / 2, screenHeight / 2 - 100);

        g2.setFont(new Font("Arial", Font.PLAIN, 30));
        g2.setColor(Color.WHITE);

        // Menu options
        String[] options = {
                "R - Restart Level",
                "L - Back to Lobby",
                "Q - Quit Game",
                "ESC - Back to Game"
        };

        for (int i = 0; i < options.length; i++) {
            int optionWidth = g2.getFontMetrics().stringWidth(options[i]);
            g2.drawString(options[i], (screenWidth - optionWidth) / 2,
                    screenHeight / 2 - 20 + (i * 50));
        }
    }

    public void handleKeyPress(int keyCode) {
        // First handle wire puzzle state
        if (gameState == wirePuzzleState) {
            wirePuzzle.handleKeyPress(keyCode);
            return;
        }

        // Handle ESC key for settings
        if (keyCode == java.awt.event.KeyEvent.VK_ESCAPE) {
            if (gameState == playState || gameState == pauseState) {
                // Go to settings from play or pause state
                gameState = settingsState;
            } else if (gameState == settingsState) {
                // Return to previous state
                gameState = playState;
            }
        }

        // Handle game over state
        if (gameState == gameOverState) {
            // Handle game over state keys
            if (keyCode == java.awt.event.KeyEvent.VK_R) {
                restartGame();
            } else if (keyCode == java.awt.event.KeyEvent.VK_Q) {
                System.exit(0);
            }
        }

        // Handle settings state
        if (gameState == settingsState) {
            handleSettingsKey(keyCode);
        }
    }

    private void handleSettingsKey(int keyCode) {
        switch(keyCode) {
            case java.awt.event.KeyEvent.VK_R:
                // Restart level
                restartGame();
                break;
            case java.awt.event.KeyEvent.VK_L:
                // Go back to lobby/title
                gameState = titleState;
                break;
            case java.awt.event.KeyEvent.VK_Q:
                // Quit game
                System.exit(0);
                break;
            case java.awt.event.KeyEvent.VK_ESCAPE:
                // Go back to game
                gameState = playState;
                break;
        }
    }

    void restartGame() {
        // Reset to level 1
        currentLevel = 1;
        loadRandomMap();
        aSetter.setObject();
        aSetter.setNPC();
        gameState = playState;

        // Reset player
        player.setDefaultValues();
        player.resetTools();

        // Reset wire puzzle
        wirePuzzle.resetPuzzle();

        // Reset timer
        initializeLevelTimer();

        System.out.println("Game restarted to Level 1");
    }

    public void loadLevel(int level) {
        currentLevel = level;
        loadRandomMap();
        aSetter.setObject();
        aSetter.setNPC();
        player.setDefaultValues();
        player.resetTools();
        wirePuzzle.resetPuzzle();
        initializeLevelTimer();
        gameState = playState;
        System.out.println("Loaded Level " + currentLevel);
    }

    public void playSound(int soundToolPickup) {
    }

    public void playBackgroundMusic() {
    }
}