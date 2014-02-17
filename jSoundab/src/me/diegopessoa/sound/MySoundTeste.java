/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package me.diegopessoa.sound;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author diegopessoa
 */
public class MySoundTeste implements LineListener, MetaEventListener {

	public static final int BUFFER_SIZE = 4096;

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
	
	boolean midiEOM, audioEOM;
	
	public MySoundTeste() {}

	public MySoundTeste(File soundFile) throws UnsupportedAudioFileException, LineUnavailableException, IOException {
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
		sampledData = new byte[BUFFER_SIZE];

	}

	Object sound = null;
	Sequencer sequencer = null;
	Synthesizer synthesizer;
	MidiChannel channels[];
	boolean bump;
	public Object loadSound(Object object) {

		try {

			sequencer = MidiSystem.getSequencer();

			if (sequencer instanceof Synthesizer) {
				synthesizer = (Synthesizer)sequencer;
				channels = synthesizer.getChannels();
			} 

		} catch (Exception ex) { ex.printStackTrace(); throw new RuntimeException("[ERROR]"); }
		sequencer.addMetaEventListener(this);


		if (object instanceof URL) {
			try {
				sound = AudioSystem.getAudioInputStream((URL) object);
			} catch(Exception e) {
				try { 
					sound = MidiSystem.getSequence((URL) object);
				} catch (InvalidMidiDataException imde) {
					System.out.println("Unsupported audio file.");
					throw new RuntimeException("[ERROR]");
				} catch (Exception ex) { 
					ex.printStackTrace(); 
					sound = null;
					throw new RuntimeException("[ERROR]");
				}
			}
		} else if (object instanceof File) {
			try {
				sound = AudioSystem.getAudioInputStream((File) object);
			} catch(Exception e1) { 
				try { 
					FileInputStream is = new FileInputStream((File) object);
					sound = new BufferedInputStream(is, 1024);
				} catch (Exception e3) { 
					e3.printStackTrace(); 
					sound = null;
					throw new RuntimeException("[ERROR]");
				}
				//}
			}
		} else {
			throw new RuntimeException("Unsupported path.");
		}


		//		// user pressed stop or changed tabs while loading
		//		if (sequencer == null) {
		//			currentSound = null;
		//			return false;
		//		}

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
				clip.addLineListener(this);
				clip.open(stream);
				sound = clip;
			} catch (Exception ex) { 
				ex.printStackTrace(); 
				sound = null;
				throw new RuntimeException("[ERROR]");
			}
		} else if (sound instanceof Sequence || sound instanceof BufferedInputStream) {
			try {
				sequencer.open();
				if (sound instanceof Sequence) {
					sequencer.setSequence((Sequence) sound);
				} else {
					sequencer.setSequence((BufferedInputStream) sound);
				}

			} catch (InvalidMidiDataException imde) { 
				System.out.println("Unsupported audio file.");
				sound = null;
				throw new RuntimeException("[ERROR]");
			} catch (Exception ex) { 
				ex.printStackTrace(); 
				sound = null;
				throw new RuntimeException("[ERROR]");
			}
		}

		return sound;

	}
	
	
	public void playSound() {
        setGain();
        setPan();
        midiEOM = audioEOM = bump = false;
        if (sound instanceof Sequence || sound instanceof BufferedInputStream) {
            sequencer.start();
            while (!midiEOM && thread != null && !bump) {
                try { thread.sleep(99); } catch (Exception e) {break;}
            }
            sequencer.stop();
            sequencer.close();
        } else if (sound instanceof Clip && thread != null) {
            Clip clip = (Clip) sound;
            clip.start();
            try { thread.sleep(99); } catch (Exception e) { }
            while ((paused || clip.isActive()) && thread != null && !bump) {
                try { thread.sleep(99); } catch (Exception e) {break;}
            }
            clip.stop();
            clip.close();
        }
        sound = null;
    }
	
	public static void main(String[] args) {
		
		InputStream sound;
		File file = new File("sounds/sound2.mp3");
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
		
//		setGain();
//	    setPan();
//	    
//	    midiEOM = audioEOM = bump = false;
//        if (sound instanceof Sequence || sound instanceof BufferedInputStream) {
//            sequencer.start();
//            while (!midiEOM && thread != null && !bump) {
//                try { thread.sleep(99); } catch (Exception e) {break;}
//            }
//            sequencer.stop();
//            sequencer.close();
//        } else if (sound instanceof Clip && thread != null) {
//            Clip clip = (Clip) sound;
//            clip.start();
//            try { thread.sleep(99); } catch (Exception e) { }
//            while ((paused || clip.isActive()) && thread != null && !bump) {
//                try { thread.sleep(99); } catch (Exception e) {break;}
//            }
//            clip.stop();
//            clip.close();
//        }
//        sound = null;
		
		
		
		
		
	}
	
	public void setPan() {

        int value = panSlider.getValue();

        if (sound instanceof Clip) {
            try {
                Clip clip = (Clip) sound;
                FloatControl panControl = 
                    (FloatControl) clip.getControl(FloatControl.Type.PAN);
                panControl.setValue(value/100.0f);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (sound instanceof Sequence || sound instanceof BufferedInputStream) {
            for (int i = 0; i < channels.length; i++) {                
				channels[i].controlChange(10, (int)(((double)value + 100.0) / 200.0 *  127.0));
            }										 
        }
    }


    public void setGain() {
        double value = gainSlider.getValue() / 100.0;

        if (sound instanceof Clip) {
            try {
                Clip clip = (Clip) sound;
                FloatControl gainControl = 
                  (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) 
                  (Math.log(value==0.0?0.0001:value)/Math.log(10.0)*20.0);
                gainControl.setValue(dB);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (sound instanceof Sequence || sound instanceof BufferedInputStream) {
            for (int i = 0; i < channels.length; i++) {                
				channels[i].controlChange(7, (int)(value * 127.0));

			}
        }
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

	@Override
	public void meta(MetaMessage message) {
		if (message.getType() == 47) {  // 47 is end of track
			midiEOM = true;
		}
	}

	@Override
	public void update(LineEvent event) {
		if (event.getType() == LineEvent.Type.STOP && !paused) { 
			audioEOM = true;
		}
	}

}
