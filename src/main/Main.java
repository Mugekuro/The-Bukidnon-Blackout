package main;

import javax.swing.JFrame;

public class Main {
    private static long lastUpdateTime = System.currentTimeMillis();
    private static int updateCount = 0;

    public static void main (String [] args) {
        System.out.println("Game starting...");

        JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("The Bukidnon Blackout");

        GamePanel gamePanel = new GamePanel();
        window.add(gamePanel);

        window.pack();

        window.setLocationRelativeTo(null);
        window.setVisible(true);

        gamePanel.setupGame();
        gamePanel.startGameThread();

        System.out.println("Game loop started!");
    }
}