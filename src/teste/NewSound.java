package teste;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class NewSound implements LineListener {

	private List<AudioFormat> formats;
	private List<Integer> bufferSizes;
	private List<DataLine.Info> infos;
	private List<byte[]> inputs;
	
	public NewSound() {
		formats = new ArrayList<AudioFormat>(0);
		bufferSizes = new ArrayList<Integer>(0);
		infos = new ArrayList<DataLine.Info>(0);
		inputs = new ArrayList<byte[]>(0);
		
	}

	public void addSound(String path) throws UnsupportedAudioFileException, IOException {

		AudioInputStream input = AudioSystem.getAudioInputStream(new File(path));
		AudioFormat format = input.getFormat();
		int bufferSize = (int) (format.getFrameSize() * input.getFrameLength());
		byte[] audio = new byte[bufferSize];
		DataLine.Info info = new DataLine.Info(Clip.class, format, bufferSize);
		input.read(audio, 0, bufferSize);

		formats.add(format);
		bufferSizes.add(new Integer(bufferSize));
		infos.add(info);
		inputs.add(audio);

	}

	public void playSound(int x) throws LineUnavailableException { 

		Integer offset = 0;
		Clip clip = (Clip) AudioSystem.getLine((DataLine.Info)infos.get(x));
		
		clip.addLineListener(this);
		if (!clip.isOpen()) {
			clip.open(formats.get(x), inputs.get(x), offset, bufferSizes.get(x));
		}
		
		if (!clip.isRunning()) {
			clip.start();
		}
		

	}
	
	public static void main(String[] args) {
		
		try {
			
			
			
			NewSound s = new NewSound();
			s.addSound("sounds/sound1.wav");
			
			
			s.playSound(0);
			
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	@Override
	public void update(LineEvent lineEvent) {
		System.out.print("LineEvent Listener: ");
		System.out.println(lineEvent.getType());
	}

} 