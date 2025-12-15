package main;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Collections;
import java.util.List;

public class WirePuzzleMinigame {
    private GamePanel gp;
    private boolean active = false;
    private boolean completed = false;
    private boolean failed = false;

    // Puzzle components
    private ArrayList<WireNode> nodes;
    private ArrayList<Wire> wires;
    private Wire selectedWire = null;
    private Point dragPoint;

    // Undo/Redo stacks
    private Stack<PuzzleState> undoStack;
    private Stack<PuzzleState> redoStack;

    // Timer
    private long startTime;
    private final long TIME_LIMIT = 15000; // 15 seconds in milliseconds
    private long remainingTime;

    // Simplified 4x4 grid
    private int gridSize = 4;
    private int tileSize = 80;
    private int startX, startY;

    // Colors - one pair each
    private Color[] wireColors = {
            Color.RED,      // Pair 1
            new Color(0, 150, 255),     // Pair 2 - Brighter Blue
            new Color(0, 200, 0),    // Pair 3 - Brighter Green
            new Color(255, 255, 0)    // Pair 4 - Brighter Yellow
    };

    // Button dimensions
    private Rectangle undoButton, redoButton, exitButton, resetButton;
    private boolean undoHovered = false, redoHovered = false, exitHovered = false, resetHovered = false;

    // Sparkle animation
    private float sparkleOffset = 0;

    // Life deduction tracking
    private boolean lifeDeducted = false;

    public WirePuzzleMinigame(GamePanel gp) {
        this.gp = gp;
        initializePuzzle();
        initializeButtons();
    }

    private void initializePuzzle() {
        nodes = new ArrayList<>();
        wires = new ArrayList<>();
        undoStack = new Stack<>();
        redoStack = new Stack<>();

        // Create 8 nodes at fixed grid positions
        int[][] nodePositions = {
                {0, 0}, // Top-left
                {0, 3}, // Top-right
                {1, 0}, // Second row left
                {1, 3}, // Second row right
                {2, 0}, // Third row left
                {2, 3}, // Third row right
                {3, 0}, // Bottom-left
                {3, 3}  // Bottom-right
        };

        // Create nodes with fixed positions
        for (int i = 0; i < nodePositions.length; i++) {
            WireNode node = new WireNode();
            node.x = nodePositions[i][0];
            node.y = nodePositions[i][1];
            node.id = i;
            nodes.add(node);
        }

        // Randomize wire connections
        randomizeWireConnections();
    }

    private void randomizeWireConnections() {
        System.out.println("=== RANDOMIZING WIRE PUZZLE FOR LEVEL " + gp.currentLevel + " ===");

        // Clear any existing wires
        wires.clear();

        // Create a list of node IDs (0-7)
        List<Integer> nodeIds = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            nodeIds.add(i);
        }

        // Shuffle the nodes randomly
        Collections.shuffle(nodeIds);

        // Create 4 wires with random connections
        for (int colorIndex = 0; colorIndex < 4; colorIndex++) {
            Wire wire = new Wire();
            wire.color = wireColors[colorIndex];
            wire.startNodeId = nodeIds.get(colorIndex * 2);     // First node for this color
            wire.endNodeId = nodeIds.get(colorIndex * 2 + 1);   // Second node for this color
            wire.connected = false;

            // Assign color to nodes
            nodes.get(wire.startNodeId).colorIndex = colorIndex;
            nodes.get(wire.endNodeId).colorIndex = colorIndex;

            wires.add(wire);

            // Log the connection
            char colorLetter = (char)('A' + colorIndex);
            String startLabel = getTerminalLabel(wire.startNodeId, colorIndex);
            String endLabel = getTerminalLabel(wire.endNodeId, colorIndex);
            System.out.println(colorLetter + " wire: " + startLabel + " → " + endLabel);
        }

        // Save initial state
        saveState();
    }

    private String getTerminalLabel(int nodeId, int colorIndex) {
        char colorLetter = (char)('A' + colorIndex);
        WireNode node = nodes.get(nodeId);

        // Determine terminal number based on position
        // Left side (x = 0 or 1) = Terminal 1, Right side (x = 2 or 3) = Terminal 2
        int terminalNum = (node.x < 2) ? 1 : 2;

        return "" + colorLetter + terminalNum;
    }

    private void initializeButtons() {
        int buttonWidth = 100;
        int buttonHeight = 40;
        int buttonSpacing = 10;

        // NEW: Timer is now at timerX = 50, timerY = startY - 90
        // Move buttons down to accommodate timer
        int leftButtonsStartY = (gp.screenHeight - (3 * buttonHeight + 2 * buttonSpacing)) / 2 + 40;

        // Left side buttons (vertical stack) - moved down
        undoButton = new Rectangle(50, leftButtonsStartY, buttonWidth, buttonHeight);
        redoButton = new Rectangle(50, leftButtonsStartY + buttonHeight + buttonSpacing, buttonWidth, buttonHeight);
        resetButton = new Rectangle(50, leftButtonsStartY + 2 * (buttonHeight + buttonSpacing), buttonWidth, buttonHeight);

        // Right side exit button (aligned with redo button vertically)
        exitButton = new Rectangle(gp.screenWidth - buttonWidth - 50,
                redoButton.y, buttonWidth, buttonHeight);
    }

    public void start() {
        active = true;
        completed = false;
        failed = false;
        lifeDeducted = false; // Reset life deduction flag
        startTime = System.currentTimeMillis();
        remainingTime = TIME_LIMIT;
        calculateScreenPosition();
        initializeButtons();

        // Randomize the puzzle for this level
        resetPuzzle();

        System.out.println("Wire puzzle started for Level " + gp.currentLevel);
    }

    public void resetPuzzle() {
        // Reset all wires to unconnected
        for (Wire wire : wires) {
            wire.connected = false;
        }

        // Reset selection and state
        selectedWire = null;
        completed = false;
        failed = false;
        lifeDeducted = false;

        // Clear undo/redo stacks
        undoStack.clear();
        redoStack.clear();

        // Clear node color assignments
        for (WireNode node : nodes) {
            node.colorIndex = -1;
        }

        // Re-randomize connections for new level
        randomizeWireConnections();

        // Save initial state
        saveState();

        System.out.println("Wire puzzle reset and randomized for Level " + gp.currentLevel);
    }

    private void calculateScreenPosition() {
        int puzzleWidth = gridSize * tileSize;
        int puzzleHeight = gridSize * tileSize;
        startX = (gp.screenWidth - puzzleWidth) / 2;
        startY = (gp.screenHeight - puzzleHeight) / 2 - 30;
    }

    public void update() {
        // Update sparkle animation
        sparkleOffset = (float)(System.currentTimeMillis() % 2000) / 2000f;

        // Update timer
        if (!completed && !failed) {
            remainingTime = TIME_LIMIT - (System.currentTimeMillis() - startTime);

            if (remainingTime <= 0) {
                failed = true;
                remainingTime = 0;

                // DEDUCT LIFE IF NOT ALREADY DEDUCTED
                if (!lifeDeducted) {
                    deductPlayerLife();
                    lifeDeducted = true;
                }

                // Return to game after delay
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                exitPuzzle();
                            }
                        },
                        2000
                );
            }
        }

        // Check if all wires are connected
        if (!completed && !failed) {
            completed = true;
            for (Wire wire : wires) {
                if (!wire.connected) {
                    completed = false;
                    break;
                }
            }

            if (completed) {
                gp.sound.play(1); // Play coin.wav for puzzle success

                // Puzzle solved - give speed boost ONLY
                gp.activateSpeedBoost(10); // 10-second speed boost

                // Return to game after delay
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                active = false;
                                gp.gameState = gp.playState;
                                // Mark the post as repaired
                                gp.postRepaired();
                                // Remove the post from the game
                                for (int i = 0; i < gp.obj.length; i++) {
                                    if (gp.obj[i] != null && gp.obj[i].name.equals("Post")) {
                                        gp.obj[i] = null;
                                        break;
                                    }
                                }
                            }
                        },
                        2000
                );
            }
        }
    }

    private void deductPlayerLife() {
        if (gp != null && gp.player != null) {
            // Deduct half a heart (1 life point)
            gp.player.life = Math.max(0, gp.player.life - 1);

            System.out.println("Puzzle time out! Life deducted. Current life: " + gp.player.life);

            // Check if player is dead
            if (gp.player.life <= 0) {
                gp.gameState = gp.gameOverState;
            }
        }
    }

    public void draw(Graphics2D g2) {
        // Semi-transparent background (30% opacity) - shows main game behind
        g2.setColor(new Color(0, 0, 0, 80));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // Draw title with level info
        g2.setColor(new Color(255, 215, 0)); // Gold color
        g2.setFont(new Font("Arial", Font.BOLD, 25));
        String title = "POWER RESTORATION PANEL - LEVEL " + gp.currentLevel;
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, (gp.screenWidth - titleWidth) / 2, startY - 40);

        // Draw timer
        drawTimer(g2);

        // Draw instructions panel
        drawInstructionsPanel(g2);

        // Draw main puzzle panel with glass effect
        drawPuzzlePanel(g2);

        // Draw nodes (terminals)
        drawNodes(g2);

        // Draw wires on top of nodes
        drawWires(g2);

        // Draw buttons
        drawButtons(g2);

        // Draw completion/failure messages
        if (completed) {
            drawCompletionMessage(g2);
        } else if (failed) {
            drawFailureMessage(g2);
        }
    }

    private void drawTimer(Graphics2D g2) {
        int seconds = (int)(remainingTime / 1000);
        int milliseconds = (int)(remainingTime % 1000) / 10;

        // NEW: Move timer to left side, above undo button
        int timerX = 40; // Same X as undo button
        int timerY = startY - 20; // Above the puzzle, near top

        // Timer background
        g2.setColor(new Color(30, 30, 40, 200));
        g2.fillRoundRect(timerX, timerY, 150, 40, 20, 20);

        // Timer border
        g2.setColor(new Color(255, 215, 0, 150));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(timerX, timerY, 150, 40, 20, 20);

        // Timer text
        g2.setFont(new Font("Arial", Font.BOLD, 20));
        String timeText = String.format("%02d:%02d", seconds, milliseconds);

        // Color based on time remaining
        if (remainingTime > 10000) {
            g2.setColor(Color.GREEN);
        } else if (remainingTime > 5000) {
            g2.setColor(Color.YELLOW);
        } else {
            g2.setColor(Color.RED);
            // Pulsing effect when time is low
            float pulse = (float)(Math.sin(System.currentTimeMillis() * 0.01) * 0.3 + 0.7);
            g2.setColor(new Color(255, (int)(100 * pulse), (int)(100 * pulse)));
        }

        int timeWidth = g2.getFontMetrics().stringWidth(timeText);
        g2.drawString(timeText, timerX + (150 - timeWidth) / 2, timerY + 25);

        // Draw "PUZZLE TIME" label above timer
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 12));
        String label = "PUZZLE TIME";
        int labelWidth = g2.getFontMetrics().stringWidth(label);
        g2.drawString(label, timerX + (150 - labelWidth) / 2, timerY + 12);
    }

    private void drawInstructionsPanel(Graphics2D g2) {
        // Instructions panel
        g2.setColor(new Color(30, 30, 40, 200));
        g2.fillRoundRect(20, startY + gridSize * tileSize + 20,
                gp.screenWidth - 40, 140, 20, 20);

        g2.setColor(new Color(255, 215, 0, 150));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(20, startY + gridSize * tileSize + 20,
                gp.screenWidth - 40, 140, 20, 20);

        // Instructions text
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 14));

        String[] instructions = {
                "CONNECT ALL COLORED TERMINALS WITHIN 15 SECONDS!",
                "• Click and drag from a terminal to its matching pair",
                "• Wires can cross - just connect matching colors",
                "• Use Z/Y to undo/redo, R to reset, ESC to exit"
        };

        int textY = startY + gridSize * tileSize + 45;
        for (String line : instructions) {
            int lineWidth = g2.getFontMetrics().stringWidth(line);
            g2.drawString(line, (gp.screenWidth - lineWidth) / 2, textY);
            textY += 25;
        }
    }

    private void drawPuzzlePanel(Graphics2D g2) {
        // Main panel with glass effect
        g2.setColor(new Color(30, 30, 40, 220));
        g2.fillRoundRect(startX - 20, startY - 20,
                gridSize * tileSize + 40, gridSize * tileSize + 40, 30, 30);

        // Panel border with glow
        g2.setStroke(new BasicStroke(3));
        g2.setColor(new Color(255, 215, 0, 100));
        g2.drawRoundRect(startX - 20, startY - 20,
                gridSize * tileSize + 40, gridSize * tileSize + 40, 30, 30);

        // Grid background
        for (int y = 0; y < gridSize; y++) {
            for (int x = 0; x < gridSize; x++) {
                int cellX = startX + x * tileSize;
                int cellY = startY + y * tileSize;

                // Subtle grid pattern
                g2.setColor(new Color(40, 40, 50, 150));
                g2.fillRect(cellX, cellY, tileSize, tileSize);

                // Grid lines
                g2.setColor(new Color(60, 60, 70, 100));
                g2.drawRect(cellX, cellY, tileSize, tileSize);

                // Subtle center dot for each cell
                if ((x + y) % 2 == 0) {
                    g2.setColor(new Color(255, 255, 255, 20));
                    g2.fillOval(cellX + tileSize/2 - 2, cellY + tileSize/2 - 2, 4, 4);
                }
            }
        }
    }

    private void drawNodes(Graphics2D g2) {
        for (WireNode node : nodes) {
            int screenX = startX + node.x * tileSize + tileSize/2;
            int screenY = startY + node.y * tileSize + tileSize/2;

            // Terminal outer ring (metallic)
            GradientPaint gradient = new GradientPaint(
                    screenX - 25, screenY - 25, new Color(220, 220, 240),
                    screenX + 25, screenY + 25, new Color(180, 180, 200)
            );
            g2.setPaint(gradient);
            g2.fillOval(screenX - 25, screenY - 25, 50, 50);

            // Terminal inner ring (color coded)
            if (node.colorIndex >= 0 && node.colorIndex < wireColors.length) {
                g2.setColor(wireColors[node.colorIndex]);
                g2.fillOval(screenX - 20, screenY - 20, 40, 40);
            }

            // Terminal highlight (shine effect)
            g2.setColor(new Color(255, 255, 255, 150));
            g2.fillOval(screenX - 12, screenY - 12, 15, 15);

            // Terminal label - use actual color assignment
            if (node.colorIndex >= 0) {
                char colorLetter = (char)('A' + node.colorIndex);

                // Determine if it's terminal 1 or 2 based on position
                String terminalNum = (node.x < 2) ? "1" : "2";
                String label = "" + colorLetter + terminalNum;

                g2.setColor(Color.BLACK);
                g2.setFont(new Font("Arial", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                int labelWidth = fm.stringWidth(label);
                g2.drawString(label, screenX - labelWidth/2, screenY + 6);
            }

            // Pulsing glow effect for unconnected terminals
            if (node.colorIndex >= 0) {
                Wire wire = wires.get(node.colorIndex);
                if (!wire.connected) {
                    float pulse = (float)(Math.sin(System.currentTimeMillis() * 0.002 + node.id) * 0.3 + 0.7);
                    g2.setColor(new Color(wireColors[node.colorIndex].getRed(),
                            wireColors[node.colorIndex].getGreen(),
                            wireColors[node.colorIndex].getBlue(),
                            (int)(100 * pulse)));
                    g2.setStroke(new BasicStroke(3));
                    g2.drawOval(screenX - 28, screenY - 28, 56, 56);
                }
            }
        }
    }

    private void drawWires(Graphics2D g2) {
        for (Wire wire : wires) {
            if (wire.connected) {
                drawConnectedWire(g2, wire);
            } else if (selectedWire == wire) {
                drawDraggingWire(g2, wire);
            }
        }
    }

    private void drawConnectedWire(Graphics2D g2, Wire wire) {
        WireNode startNode = nodes.get(wire.startNodeId);
        WireNode endNode = nodes.get(wire.endNodeId);

        int x1 = startX + startNode.x * tileSize + tileSize/2;
        int y1 = startY + startNode.y * tileSize + tileSize/2;
        int x2 = startX + endNode.x * tileSize + tileSize/2;
        int y2 = startY + endNode.y * tileSize + tileSize/2;

        // Wire glow effect
        g2.setStroke(new BasicStroke(10, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(wire.color.getRed(), wire.color.getGreen(),
                wire.color.getBlue(), 80));
        g2.drawLine(x1, y1, x2, y2);

        // Main wire with gradient
        GradientPaint wireGradient = new GradientPaint(
                x1, y1, wire.color.brighter(),
                x2, y2, wire.color.darker()
        );
        g2.setPaint(wireGradient);
        g2.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x1, y1, x2, y2);

        // Sparkle animation along the wire
        if (completed) {
            drawSparkles(g2, x1, y1, x2, y2);
        }
    }

    private void drawDraggingWire(Graphics2D g2, Wire wire) {
        WireNode startNode = nodes.get(wire.startNodeId);
        int x1 = startX + startNode.x * tileSize + tileSize/2;
        int y1 = startY + startNode.y * tileSize + tileSize/2;

        // Dashed line effect for dragging wire
        float[] dashPattern = {10, 5};
        BasicStroke dashedStroke = new BasicStroke(4, BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10, dashPattern, 0);
        g2.setStroke(dashedStroke);
        g2.setColor(wire.color);
        g2.drawLine(x1, y1, dragPoint.x, dragPoint.y);

        // Dragging endpoint circle
        g2.setStroke(new BasicStroke(2));
        g2.setColor(new Color(255, 255, 255, 150));
        g2.drawOval(dragPoint.x - 15, dragPoint.y - 15, 30, 30);

        // Pulsing effect at drag point
        float pulse = (float)(Math.sin(System.currentTimeMillis() * 0.01) * 0.3 + 0.7);
        g2.setColor(new Color(wire.color.getRed(), wire.color.getGreen(),
                wire.color.getBlue(), (int)(200 * pulse)));
        g2.fillOval(dragPoint.x - 10, dragPoint.y - 10, 20, 20);
    }

    private void drawSparkles(Graphics2D g2, int x1, int y1, int x2, int y2) {
        g2.setStroke(new BasicStroke(2));
        g2.setColor(Color.WHITE);

        // Multiple sparkles moving along the wire
        for (int i = 0; i < 4; i++) {
            float t = (sparkleOffset + i * 0.25f) % 1.0f;
            int sparkX = (int)(x1 + (x2 - x1) * t);
            int sparkY = (int)(y1 + (y2 - y1) * t);

            // Sparkle size varies
            float size = (float)(Math.sin(System.currentTimeMillis() * 0.002 + i) * 2 + 4);
            g2.fillOval((int)(sparkX - size/2), (int)(sparkY - size/2), (int)size, (int)size);
        }
    }

    private void drawButtons(Graphics2D g2) {
        // Draw left side buttons (vertical stack)
        drawButton(g2, undoButton, "Undo", undoHovered, new Color(60, 60, 180));
        drawButton(g2, redoButton, "Redo", redoHovered, new Color(60, 180, 60));
        drawButton(g2, resetButton, "Reset", resetHovered, new Color(180, 120, 60));

        // Draw right side exit button
        drawButton(g2, exitButton, "Exit", exitHovered, new Color(180, 60, 60));
    }

    private void drawButton(Graphics2D g2, Rectangle rect, String text, boolean hovered, Color baseColor) {
        // Button background with hover effect
        Color buttonColor = hovered ? baseColor.brighter() : baseColor;
        g2.setColor(new Color(buttonColor.getRed(), buttonColor.getGreen(),
                buttonColor.getBlue(), 200));
        g2.fillRoundRect(rect.x, rect.y, rect.width, rect.height, 15, 15);

        // Button border
        g2.setColor(hovered ? Color.WHITE : new Color(255, 255, 255, 150));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(rect.x, rect.y, rect.width, rect.height, 15, 15);

        // Button text
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        g2.drawString(text, rect.x + (rect.width - textWidth)/2,
                rect.y + rect.height/2 + 5);

        // Button icon/key hint
        if (hovered) {
            g2.setColor(new Color(255, 255, 255, 50));
            g2.fillOval(rect.x + rect.width - 25, rect.y + 10, 15, 15);
        }
    }

    private void drawCompletionMessage(Graphics2D g2) {
        // Celebration overlay with gradient
        GradientPaint celebrationGradient = new GradientPaint(
                0, 0, new Color(0, 255, 0, 100),
                gp.screenWidth, gp.screenHeight, new Color(0, 200, 255, 100)
        );
        g2.setPaint(celebrationGradient);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // Success message
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        String successMsg = "POWER RESTORED!";
        int msgWidth = g2.getFontMetrics().stringWidth(successMsg);
        g2.drawString(successMsg, (gp.screenWidth - msgWidth) / 2, gp.screenHeight / 2 - 30);

        // Rewards message (ONLY speed boost)
        g2.setFont(new Font("Arial", Font.PLAIN, 24));
        String rewardMsg = "+10s Speed Boost";
        int rewardWidth = g2.getFontMetrics().stringWidth(rewardMsg);
        g2.drawString(rewardMsg, (gp.screenWidth - rewardWidth) / 2, gp.screenHeight / 2 + 30);

        // Particles for celebration
        drawCelebrationParticles(g2);
    }

    private void drawFailureMessage(Graphics2D g2) {
        // Failure overlay
        g2.setColor(new Color(255, 0, 0, 100));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        // Failure message
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 48));
        String failMsg = "TIME'S UP!";
        int msgWidth = g2.getFontMetrics().stringWidth(failMsg);
        g2.drawString(failMsg, (gp.screenWidth - msgWidth) / 2, gp.screenHeight / 2 - 30);

        // Life deduction message
        g2.setFont(new Font("Arial", Font.PLAIN, 24));
        String lifeMsg = "-1 Life";
        int lifeWidth = g2.getFontMetrics().stringWidth(lifeMsg);
        g2.drawString(lifeMsg, (gp.screenWidth - lifeWidth) / 2, gp.screenHeight / 2 + 10);

        // Try again message
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        String tryAgainMsg = "Returning to game...";
        int tryAgainWidth = g2.getFontMetrics().stringWidth(tryAgainMsg);
        g2.drawString(tryAgainMsg, (gp.screenWidth - tryAgainWidth) / 2, gp.screenHeight / 2 + 50);
    }

    private void drawCelebrationParticles(Graphics2D g2) {
        // Simple particle effect
        long currentTime = System.currentTimeMillis();
        for (int i = 0; i < 20; i++) {
            float angle = (float)(currentTime * 0.001 + i * 0.3);
            float radius = 100 + (float)Math.sin(currentTime * 0.001 + i) * 50;
            int x = gp.screenWidth / 2 + (int)(Math.cos(angle) * radius);
            int y = gp.screenHeight / 2 + (int)(Math.sin(angle) * radius);

            float size = (float)(Math.sin(currentTime * 0.002 + i) * 3 + 6);
            Color particleColor = new Color(
                    (int)(Math.sin(angle) * 127 + 128),
                    (int)(Math.cos(angle) * 127 + 128),
                    255,
                    200
            );

            g2.setColor(particleColor);
            g2.fillOval((int)(x - size/2), (int)(y - size/2), (int)size, (int)size);
        }
    }

    public void handleMousePress(int x, int y) {
        if (completed || failed) return;

        // Check button clicks
        if (undoButton.contains(x, y)) {
            undo();
            return;
        }
        if (redoButton.contains(x, y)) {
            redo();
            return;
        }
        if (resetButton.contains(x, y)) {
            resetAllWires();
            return;
        }
        if (exitButton.contains(x, y)) {
            exitPuzzle();
            return;
        }

        // Check if clicking on an unconnected start node
        for (Wire wire : wires) {
            if (!wire.connected) {
                WireNode startNode = nodes.get(wire.startNodeId);
                int nodeX = startX + startNode.x * tileSize + tileSize/2;
                int nodeY = startY + startNode.y * tileSize + tileSize/2;

                if (Math.abs(x - nodeX) < 25 && Math.abs(y - nodeY) < 25) {
                    selectedWire = wire;
                    dragPoint = new Point(x, y);
                    return;
                }
            }
        }
    }

    public void handleMouseMove(int x, int y) {
        // Update button hover states
        undoHovered = undoButton.contains(x, y);
        redoHovered = redoButton.contains(x, y);
        resetHovered = resetButton.contains(x, y);
        exitHovered = exitButton.contains(x, y);
    }

    public void handleMouseDrag(int x, int y) {
        if (selectedWire != null && !completed && !failed) {
            dragPoint = new Point(x, y);
        }
    }

    public void handleMouseRelease(int x, int y) {
        if (selectedWire != null && !completed && !failed) {
            // Check if released over the matching end node
            WireNode endNode = nodes.get(selectedWire.endNodeId);
            int nodeX = startX + endNode.x * tileSize + tileSize/2;
            int nodeY = startY + endNode.y * tileSize + tileSize/2;

            // Large hitbox for easier connection
            if (Math.abs(x - nodeX) < 40 && Math.abs(y - nodeY) < 40) {
                boolean wasConnected = selectedWire.connected;
                selectedWire.connected = true;

                // Save state if connection changed
                if (!wasConnected) {
                    saveState();
                    redoStack.clear(); // Clear redo stack on new action
                }
            }

            selectedWire = null;
        }
    }

    public void handleKeyPress(int keyCode) {
        if (keyCode == KeyEvent.VK_ESCAPE) {
            exitPuzzle();
        } else if (keyCode == KeyEvent.VK_R && selectedWire != null) {
            // Reset currently selected wire
            selectedWire.connected = false;
            selectedWire = null;
        } else if (keyCode == KeyEvent.VK_R) {
            resetAllWires();
        } else if (keyCode == KeyEvent.VK_ENTER) {
            update(); // Check solution
        } else if (keyCode == KeyEvent.VK_Z) {
            undo();
        } else if (keyCode == KeyEvent.VK_Y) {
            redo();
        }
    }

    private void resetAllWires() {
        boolean changed = false;
        for (Wire wire : wires) {
            if (wire.connected) {
                wire.connected = false;
                changed = true;
            }
        }
        selectedWire = null;

        if (changed) {
            saveState();
            redoStack.clear();
        }
    }

    private void saveState() {
        boolean[] connectionStates = new boolean[wires.size()];
        for (int i = 0; i < wires.size(); i++) {
            connectionStates[i] = wires.get(i).connected;
        }
        undoStack.push(new PuzzleState(connectionStates));
    }

    private void undo() {
        if (!undoStack.isEmpty() && undoStack.size() > 1) { // Keep at least initial state
            // Save current state to redo stack
            boolean[] currentStates = new boolean[wires.size()];
            for (int i = 0; i < wires.size(); i++) {
                currentStates[i] = wires.get(i).connected;
            }
            redoStack.push(new PuzzleState(currentStates));

            // Restore previous state
            PuzzleState previousState = undoStack.pop();
            for (int i = 0; i < wires.size(); i++) {
                wires.get(i).connected = previousState.connectionStates[i];
            }
        }
    }

    private void redo() {
        if (!redoStack.isEmpty()) {
            // Save current state to undo stack
            boolean[] currentStates = new boolean[wires.size()];
            for (int i = 0; i < wires.size(); i++) {
                currentStates[i] = wires.get(i).connected;
            }
            undoStack.push(new PuzzleState(currentStates));

            // Restore next state
            PuzzleState nextState = redoStack.pop();
            for (int i = 0; i < wires.size(); i++) {
                wires.get(i).connected = nextState.connectionStates[i];
            }
        }
    }

    private void exitPuzzle() {
        active = false;
        gp.gameState = gp.playState;
    }

    //encap

    public boolean isActive() { return active; }

    // Inner classes
    class WireNode {
        int x, y;           // Grid position (0-3)
        int id;             // Unique ID
        int colorIndex = -1; // Which color wire is assigned (-1 = not assigned)
    }

    class Wire {
        Color color;
        int startNodeId;    // Start node ID
        int endNodeId;      // End node ID
        boolean connected;  // Whether wire is properly connected
    }

    class PuzzleState {
        boolean[] connectionStates;

        PuzzleState(boolean[] states) {
            this.connectionStates = states.clone();
        }
    }
}