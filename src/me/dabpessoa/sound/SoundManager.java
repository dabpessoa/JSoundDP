/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.dabpessoa.sound;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author diegopessoa
 */
public class SoundManager {
    
    public static final int BUFFER_SIZE = 64*1024;  // 64 KB
    
    public static Sound loadSound(String path) throws UnsupportedAudioFileException, LineUnavailableException, IOException {
        return new Sound(new File(path));
    }
    
}
