/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.diegopessoa.sound;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author diegopessoa
 */
public class Sound {

	// some lock somewhere...
	private Object lock = new Object();

	private File soundFile;
	private AudioInputStream inputStream;
	private SourceDataLine soundLine;
	private AudioFormat audioFormat;
	private int nBytesRead;
	private byte[] sampledData;
	private boolean running;
	private List<SoundListener> listeners;
	private volatile boolean paused;
	private volatile boolean stoped;

	public Sound(File soundFile) throws UnsupportedAudioFileException, LineUnavailableException, IOException {
		this.soundFile = soundFile;
		this.listeners = new ArrayList<SoundListener>();
	}

	private void configSound() throws UnsupportedAudioFileException, LineUnavailableException, IOException {

		AudioInputStream in = AudioSystem.getAudioInputStream(soundFile);
		AudioFormat baseFormat = in.getFormat();
		audioFormat = new AudioFormat(
				AudioFormat.Encoding.PCM_SIGNED,
				baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
				baseFormat.getChannels() * 2, baseFormat.getSampleRate(),
				false);
		inputStream = AudioSystem.getAudioInputStream(audioFormat, in);

		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		soundLine = (SourceDataLine) AudioSystem.getLine(info);
		nBytesRead = 0;
		sampledData = new byte[SoundManager.BUFFER_SIZE];

	}
	
	public void addSoundListener(SoundListener soundListener) {
		listeners.add(soundListener);
	}

	public boolean containsListener(SoundListener soundListener) {
		return listeners.contains(soundListener);
	}

	public void play() throws LineUnavailableException, IOException, UnsupportedAudioFileException {
		try {

			running = true;
			stoped = false;
			paused = false;

			configSound();
			
			soundLine.open(audioFormat);
			soundLine.start();

			synchronized (lock) {
				musicLaco:
				while (nBytesRead != -1) {
					nBytesRead = inputStream.read(sampledData, 0, sampledData.length);
					if (nBytesRead >= 0) {
						while (paused) {
							if(soundLine.isRunning()) {
								soundLine.stop();
							}
							try {
								lock.wait();
							}
							catch(InterruptedException e) {
								System.out.println("Erro de interrupção da thread do tocado de som.");
							}
						}
						if(!soundLine.isRunning()) {
							soundLine.start();
						}
						// Writes audio data to the mixer via this source data line.
						soundLine.write(sampledData, 0, nBytesRead);
						if (stoped) break musicLaco;
					}
				}
			}


		} finally {
			if (soundLine != null) {
				soundLine.drain();
				soundLine.close();
			}
			if (inputStream != null) {
				inputStream.close();
			}
			running = false;
		}

		Iterator<SoundListener> iterator = listeners.iterator();
		while (iterator.hasNext()) {
			((SoundListener) iterator.next()).updateSoundFinished();
		}

	}

	public void stop() {
		stoped = true;
	}

	public void pause() {
		if (!paused) paused = true;
		else {
			synchronized(lock) {
				paused = false;
				lock.notifyAll();
			}
		}
	}

	public boolean isRunning() {
		return running;
	}

}
