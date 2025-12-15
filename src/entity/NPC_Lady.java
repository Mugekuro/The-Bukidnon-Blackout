package entity;

import main.GamePanel;
import java.awt.Rectangle;
import java.util.Random;

public class NPC_Lady extends Entity {

    private Random random;
    private int moveCounter = 0;
    private int directionChangeCounter = 0;
    private int stuckCounter = 0;
    private int lastWorldX, lastWorldY;

    public NPC_Lady(GamePanel gp) {
        super(gp);

        direction = "down";
        speed = 1;
        random = new Random();
        lastWorldX = worldX;
        lastWorldY = worldY;

        getImage();

        solidArea = new Rectangle(8, 16, 32, 32);
        solidAreaDefaultX = solidArea.x;
        solidAreaDefaultY = solidArea.y;

        System.out.println("NPC_Lady: Collision-aware AI initialized");
    }

    public void getImage() {
        try {
            up1 = setup("/npc/Manager_up_1");
            up2 = setup("/npc/Manager_up_2");
            down1 = setup("/npc/Manager_down_1");
            down2 = setup("/npc/Manager_down_2");
            left1 = setup("/npc/Manager_left_1");
            left2 = setup("/npc/Manager_left_2");
            right1 = setup("/npc/Manager_right_1");
            right2 = setup("/npc/Manager_right_2");
        } catch (Exception e) {
            System.err.println("Error loading NPC images");
        }
    }

    @Override
    public void update() {
        // Store position before moving
        lastWorldX = worldX;
        lastWorldY = worldY;

        // Update counters
        moveCounter++;
        directionChangeCounter++;

        // Change direction every 3-5 seconds (180-300 frames at 60 FPS)
        if (directionChangeCounter > 180 + random.nextInt(120)) {
            changeDirection();
            directionChangeCounter = 0;
        }

        // Try to move
        attemptMovement();

        // Check if stuck (not moving)
        if (worldX == lastWorldX && worldY == lastWorldY) {
            stuckCounter++;
            if (stuckCounter > 5) { // Stuck for 5 frames
                System.out.println("NPC stuck! Forcing direction change.");
                changeDirection();
                directionChangeCounter = 0;
                stuckCounter = 0;
            }
        } else {
            stuckCounter = 0; // Reset if moving
        }

        // Update animation
        updateAnimation();
    }

    private void changeDirection() {
        String[] directions = {"up", "down", "left", "right"};
        String newDirection;

        // Try to pick a valid direction
        int attempts = 0;
        do {
            newDirection = directions[random.nextInt(4)];
            attempts++;

            if (attempts > 10) {
                // If too many attempts, pick any direction
                break;
            }

        } while (newDirection.equals(direction));

        direction = newDirection;
        // System.out.println("NPC changed direction to: " + direction);
    }

    private void attemptMovement() {
        // Reset collision flag
        collisionOn = false;

        // Check for collisions BEFORE moving
        if (gp != null && gp.cChecker != null) {
            // Check tile collision
            gp.cChecker.checkTile(this);

            // Check object collision (false = not player, so NPC won't pick up objects)
            gp.cChecker.checkObject(this, false);
        }

        // If no collision, move in current direction
        if (!collisionOn) {
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
        } else {
            // Collision detected! Don't move and change direction
            System.out.println("NPC collision detected! Changing direction.");
            changeDirection();
            directionChangeCounter = 0;

            // Revert to previous position
            worldX = lastWorldX;
            worldY = lastWorldY;
        }

        // Keep within world boundaries
        enforceWorldBoundaries();
    }

    private void enforceWorldBoundaries() {
        // Simple boundary checking
        if (worldX < 0) {
            worldX = 0;
            direction = "right";
        }
        if (worldX > gp.worldWidth - gp.tileSize) {
            worldX = gp.worldWidth - gp.tileSize;
            direction = "left";
        }
        if (worldY < 0) {
            worldY = 0;
            direction = "down";
        }
        if (worldY > gp.worldHeight - gp.tileSize) {
            worldY = gp.worldHeight - gp.tileSize;
            direction = "up";
        }
    }

    private void updateAnimation() {
        spriteCounter++;
        if (spriteCounter > 12) { // Moderate animation speed
            spriteNum = (spriteNum == 1) ? 2 : 1;
            spriteCounter = 0;
        }
    }
}