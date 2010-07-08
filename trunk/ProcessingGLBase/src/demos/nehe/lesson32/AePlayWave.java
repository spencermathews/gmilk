/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package demos.nehe.lesson32;

import demos.common.ResourceRetriever;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Java play WAV sound file (should proberly live in "demos.common")
 * Original example code from: http://www.anyexample.com/programming/java/java_play_wav_sound_file.xml
 * form the example documentation:
 * <p>
 * abstract<br/>
 * There is source code of simple class AePlayWave in this article, which
 * can play WAV(AUFF, SND, AU might also be supported) sound files
 * asynchronously (in a separate thread, without interruption of main
 * program)
 * </p>
 * Chages:<br/>
 * 20071112 (rainss) - Modified exmaple code to use "ResourceLoader" and the loaded the sould file during construction.
 * <p>
 * Must be created and started (as in Renderer:698) as holding a singleton version
 * causes a openGL threading exception of some discription. I may come back to this later and
 * "wrap-up" the thread inside the class, i.e. create a class that loads the sound
 * file and creates threads each time a "play" event I actioned.
 * </p>
 */
class AePlayWave extends Thread {
    private AudioInputStream audioInputStream = null;                       // rainss 20071112 - moved from run()
    private int curPosition;
    private final int EXTERNAL_BUFFER_SIZE = 524288; // 128Kb

    private static final int LEFT = 1;
    private static final int RIGHT = 2;
    private static final int NORMAL = 3;

    public AePlayWave(String wavfile) {
        this(wavfile, NORMAL);
    }

    public AePlayWave(String wavfile, int p) {
        curPosition = p;
        loadFile(wavfile);
    }

    private void loadFile(String fileName) {                                // rainss 20071112 - moved all out of run()
        File soundFile = new File(fileName);
        if (!soundFile.exists()) {
            System.err.println("Wave file not found: " + fileName);
            return;
        }

        try {
            //audioInputStream = AudioSystem.getAudioInputStream(soundFile); rainss 20071112 - removed
            audioInputStream = AudioSystem.getAudioInputStream(ResourceRetriever.getResourceAsStream(fileName)); // rainss 20071112 - added
        } catch (UnsupportedAudioFileException e1) {
            throw new RuntimeException(e1);
        } catch (IOException e1) {
            throw new RuntimeException(e1);
        }
    }

    public void run() {
        // rainss 20071112 - loading of file moved to "loadFile()"
        AudioFormat format = audioInputStream.getFormat();
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        SourceDataLine auline;
        try {
            auline = (SourceDataLine) AudioSystem.getLine(info);
            auline.open(format);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (auline.isControlSupported(FloatControl.Type.PAN)) {
            FloatControl pan = (FloatControl) auline.getControl(FloatControl.Type.PAN);
            if (curPosition == RIGHT) {
                pan.setValue(1.0f);
            } else if (curPosition == LEFT) {
                pan.setValue(-1.0f);
            }
        }

        auline.start();
        int nBytesRead = 0;
        byte[] abData = new byte[EXTERNAL_BUFFER_SIZE];

        try {
            while (nBytesRead != -1) {
                nBytesRead = audioInputStream.read(abData, 0, abData.length);
                if (nBytesRead >= 0) {
                    auline.write(abData, 0, nBytesRead);
                }
            }
        } catch (IOException e) {
            // Silently ignored
        } finally {
            auline.drain();
            auline.close();
        }
    }
}
