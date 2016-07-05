package me.dabpessoa.run;

import java.io.IOException;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import me.dabpessoa.sound.MusicPlayer;
import me.dabpessoa.sound.Sound;
import me.dabpessoa.sound.SoundManager;

public class Main {

    public static void main(String[] args) throws UnsupportedAudioFileException, LineUnavailableException, IOException {

    	MusicPlayer player = new MusicPlayer();
    	
        Sound sound_1 = SoundManager.loadSound("sounds/sound1.wav");
//        Sound sound_2 = SoundManager.loadSound("sounds/sound2.mp3");
        
        player.addToPlayList("id_1", sound_1);
//        player.addToPlayList("id_2", sound_1);
        
        
        player.play("id_1");
//        player.play("id_2");
        
        
//        player.playAll();
//        player.loop("id_1", 5000, 3);
//        player.loop("id_2", 2000, -1);
//        player.play("id_2");
//        
//        player.loop("chomp", 1, -1);
//        
//        try {
//			Thread.sleep(3000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
//        
//        player.stopAll();
        
    }
	
}
