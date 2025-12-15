package main;

import javax.sound.sampled.*;
import java.io.InputStream;

public class Sound {

    Clip[] clips = new Clip[10];
    String[] soundPaths = {
            "/sounds/bgm.wav",  // 0 - Background music (looping)
            "/sounds/coin.wav",              // 1 - Tool pickup, puzzle success, menu nav
            "/sounds/powerup.wav",           // 2 - Speed boost
            "/sounds/unlock.wav",            // 3 - Menu selection, resume, restart
            "/sounds/fanfare.wav"            // 4 - Level complete
    };

    public Sound() {
        loadSounds();
    }


    // encap
    private void loadSounds() {
        try {
            for (int i = 0; i < soundPaths.length; i++) {
                InputStream is = getClass().getResourceAsStream(soundPaths[i]);
                if (is != null) {
                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(is);
                    clips[i] = AudioSystem.getClip();
                    clips[i].open(audioIn);
                    System.out.println("✓ Loaded: " + soundPaths[i]);
                }
            }
        } catch (Exception e) {
            System.out.println("Sound error: " + e.getMessage());
        }
    }

    public void play(int i) {
        if (i < 0 || i >= clips.length || clips[i] == null) return;

        clips[i].setFramePosition(0); // Always start from beginning
        clips[i].start();
    }

    public void loop(int i) {
        if (i < 0 || i >= clips.length || clips[i] == null) return;

        clips[i].setFramePosition(0); // Start from beginning when looping
        clips[i].loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stop(int i) {
        if (i < 0 || i >= clips.length || clips[i] == null) return;

        clips[i].stop();
    }
}