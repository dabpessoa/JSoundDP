/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.dabpessoa.sound;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author diegopessoa
 */
public class MusicPlayer implements SoundListener {
    
    private Map<String, Sound> playList;
    private Thread loopThread;
    private boolean globalLooping;

    public MusicPlayer() {
        playList = new HashMap<String, Sound>();
    }
    
    public void addToPlayList(String name, Sound sound) {
        playList.put(name, sound);
    }
    
    public void removeFromPlayList(String name) {
        playList.remove(name);
    }
    
    public void playAll() {
       Iterator<Sound> sounds = playList.values().iterator();
       while (sounds.hasNext()) {
           play(sounds.next());
       }
    }
    
    public void stopAll() {
    	Iterator<String> names = playList.keySet().iterator();
        while (names.hasNext()) {
            this.stop(names.next());
        }
    }
    
    public void play(String name) {
        play(playList.get(name));
    }
    
    public void play(final Sound sound) {
        if (!sound.containsListener(this)) {
            sound.addSoundListener(this);
        }
        Thread thread = new Thread(new Runnable() {
            public void run() {
                try {
                    sound.play();
                } catch (LineUnavailableException ex) {
                    Logger.getLogger(MusicPlayer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(MusicPlayer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnsupportedAudioFileException ex) {
                    Logger.getLogger(MusicPlayer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }
    
    public void loop(final String name, final long loopInterval, final int times) {
        
        loopThread = new Thread(new Runnable() {
            
            int loops = times;
            
            public void run() {
            	globalLooping = true;
            	
                Sound sound = playList.get(name);
                
                while (globalLooping) {
                    try {
                        if (loops != -1) {
                            loops--;
                            if (loops == 0) globalLooping = false;
                        }
                        
                        if (!sound.isRunning())
                            play(sound);
                        
                        if (!globalLooping) {
                        	sound.stop();
                        }
                        
                        Thread thread = loopThread;
                        synchronized (thread) {
                            thread.wait(loopInterval);
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MusicPlayer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                }
            }
        });
        loopThread.start();
        
    }
    
    public Sound getSound(String name) {
        return playList.get(name);
    }
    
    public void pause(String name) {
    	getSound(name).pause();
    }
    
    public void stop(String name) {
    	getSound(name).stop();
    	globalLooping = false;
    }

    public void updateSoundFinished() {
        System.out.println("Música stop...");
    }
    
}
