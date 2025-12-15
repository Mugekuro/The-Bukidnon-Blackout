package main;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {
    GamePanel gp;
    public boolean upPressed, downPressed, leftPressed, rightPressed;
    boolean checkDrawTime = false;

    public KeyHandler(GamePanel gp) {
        this.gp = gp;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        // TITLE STATE
        if(gp.gameState == gp.titleState) {
            if (gp.ui.inLevelSelect) {
                // Level selection navigation
                if(code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                    gp.ui.selectedLevel -= 5;
                    if(gp.ui.selectedLevel < 1) {
                        gp.ui.selectedLevel += 10;
                    }
                    gp.sound.play(1); // Menu navigation sound
                }
                if(code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                    gp.ui.selectedLevel += 5;
                    if(gp.ui.selectedLevel > 10) {
                        gp.ui.selectedLevel -= 10;
                    }
                    gp.sound.play(1); // Menu navigation sound
                }
                if(code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
                    gp.ui.selectedLevel--;
                    if(gp.ui.selectedLevel < 1) {
                        gp.ui.selectedLevel = 10;
                    }
                    gp.sound.play(1); // Menu navigation sound
                }
                if(code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
                    gp.ui.selectedLevel++;
                    if(gp.ui.selectedLevel > 10) {
                        gp.ui.selectedLevel = 1;
                    }
                    gp.sound.play(1); // Menu navigation sound
                }
                if(code == KeyEvent.VK_ENTER) {
                    // Play selection sound
                    gp.sound.play(3); // Selection sound

                    // Start selected level
                    gp.currentLevel = gp.ui.selectedLevel;
                    gp.loadRandomMap();
                    gp.aSetter.setObject();
                    gp.aSetter.setNPC();
                    gp.gameState = gp.playState;
                    gp.ui.inLevelSelect = false;

                    // Reset player for new level
                    gp.player.setDefaultValues();
                    gp.player.resetTools();

                    // Initialize timer for selected level
                    gp.initializeLevelTimer();

                    // Stop and restart background music
                    gp.sound.stop(0);
                    gp.sound.loop(0);

                    System.out.println("Starting Level " + gp.currentLevel);
                }
                if(code == KeyEvent.VK_ESCAPE) {
                    // Play back sound
                    gp.sound.play(1); // Back sound

                    // Go back to main menu
                    gp.ui.inLevelSelect = false;
                    gp.ui.commandNum = 0;
                }
                return; // IMPORTANT: Return after handling level select
            }

            // Original title screen navigation
            if(code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                gp.ui.commandNum--;
                if(gp.ui.commandNum < 0) {
                    gp.ui.commandNum = 2;
                }
                gp.sound.play(1); // Menu navigation sound (coin.wav)
            }
            if(code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                gp.ui.commandNum++;
                if(gp.ui.commandNum > 2) {
                    gp.ui.commandNum = 0;
                }
                gp.sound.play(1); // Menu navigation sound (coin.wav)
            }
            if(code == KeyEvent.VK_ENTER) {
                // Play unlock.wav for ALL menu selections
                gp.sound.play(3); // Selection sound (unlock.wav)

                if(gp.ui.commandNum == 0) { // START
                    gp.gameState = gp.playState;
                    // Stop and restart background music from beginning
                    gp.sound.stop(0); // Stop background music
                    gp.sound.loop(0); // Restart background music from beginning
                }
                if(gp.ui.commandNum == 1) { // LOAD GAME
                    // Enter level selection
                    gp.ui.inLevelSelect = true;
                    gp.ui.selectedLevel = 1;
                }
                if(gp.ui.commandNum == 2) { // QUIT
                    System.exit(0);
                }
            }
        }

        // Handle P key for pause
        if(code == KeyEvent.VK_P) {
            if(gp.gameState == gp.playState) {
                gp.gameState = gp.pauseState;
                gp.sound.play(1); // Pause - coin.wav
            } else if(gp.gameState == gp.pauseState) {
                gp.gameState = gp.playState;
                gp.sound.play(3); // Resume - unlock.wav
            }
            return;
        }

        // Handle ESC key for settings
        if(code == KeyEvent.VK_ESCAPE) {
            if(gp.gameState == gp.playState || gp.gameState == gp.pauseState) {
                // Go to settings from play or pause state
                gp.gameState = gp.settingsState;
                gp.sound.play(1); // Menu navigation sound
            } else if(gp.gameState == gp.settingsState) {
                // Return to previous state
                gp.gameState = gp.playState;
                gp.sound.play(3); // Selection sound
            }
            return; // Important: prevent other key processing
        }

        // Handle R key for restart (in game over state)
        if(code == KeyEvent.VK_R && gp.gameState == gp.gameOverState) {
            gp.sound.play(3); // Restart - unlock.wav
            gp.restartGame();
            return;
        }

        // Handle settings state keys
        if(gp.gameState == gp.settingsState) {
            if(code == KeyEvent.VK_R) {
                // Restart level
                gp.restartGame();
                gp.sound.play(3); // Selection sound
            } else if(code == KeyEvent.VK_L) {
                // Go back to lobby/title
                gp.gameState = gp.titleState;
                gp.sound.play(3); // Selection sound
            } else if(code == KeyEvent.VK_Q) {
                // Quit game
                System.exit(0);
            }
            return;
        }

        // Movement keys - only if not in pause/puzzle/game over state
        if(gp.gameState == gp.playState) {
            if(code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
                upPressed = true;
            }
            if(code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
                downPressed = true;
            }
            if(code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
                leftPressed = true;
            }
            if(code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
                rightPressed = true;
            }
        }

        // DEBUG key - works in any state
        if(code == KeyEvent.VK_T) {
            checkDrawTime = !checkDrawTime;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if(code == KeyEvent.VK_W || code == KeyEvent.VK_UP) {
            upPressed = false;
        }
        if(code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) {
            downPressed = false;
        }
        if(code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) {
            leftPressed = false;
        }
        if(code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) {
            rightPressed = false;
        }
    }
}