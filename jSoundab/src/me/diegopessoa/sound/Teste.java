package me.diegopessoa.sound;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

public class Teste {

	public static void main(String[] args) {
		
		final Teste sound = new Teste();
		
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				sound.execute();
			}
		});
		
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				sound.execute();
			}
		});
		
		t1.start(); 
		t2.start();
		
	}
	
	public void execute() {
		
		InputStream sound;
		File file = new File("sounds/sound1.wav");
		Clip clipSound = null;
		
		
		
		// PREPARE...
		
		try {
			sound = AudioSystem.getAudioInputStream(file);
		} catch(Exception e1) { 
			try { 
				FileInputStream is = new FileInputStream(file);
				sound = new BufferedInputStream(is, 1024);
			} catch (Exception e3) { 
				e3.printStackTrace(); 
				sound = null;
				throw new RuntimeException("[ERROR]");
			}
			//}
		}
		
		
		if (sound instanceof AudioInputStream) {
			try {
				AudioInputStream stream = (AudioInputStream) sound;
				AudioFormat format = stream.getFormat();

				/**
				 * we can't yet open the device for ALAW/ULAW playback,
				 * convert ALAW/ULAW to PCM
				 */
				if ((format.getEncoding() == AudioFormat.Encoding.ULAW) ||
						(format.getEncoding() == AudioFormat.Encoding.ALAW)) 
				{
					AudioFormat tmp = new AudioFormat(
							AudioFormat.Encoding.PCM_SIGNED, 
							format.getSampleRate(),
							format.getSampleSizeInBits() * 2,
							format.getChannels(),
							format.getFrameSize() * 2,
							format.getFrameRate(),
							true);
					stream = AudioSystem.getAudioInputStream(tmp, stream);
					format = tmp;
				}
				DataLine.Info info = new DataLine.Info(
						Clip.class, 
						stream.getFormat(), 
						((int) stream.getFrameLength() *
								format.getFrameSize()));

				Clip clip = (Clip) AudioSystem.getLine(info);
				clip.addLineListener(new MySoundTeste());
				clip.open(stream);
				clipSound = clip;
			} catch (Exception ex) { 
				ex.printStackTrace(); 
				sound = null;
				throw new RuntimeException("[ERROR]");
			}
		}
		
		
		
		// PLAY
		
		clipSound.start();
		
		System.out.println("teste1");
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("teste2");
		
		clipSound.start();
		
		System.out.println("teste3");
		
	}
	
}
