package me.dabpessoa.sound;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundManagerTeste
{
       private javax.sound.sampled.Line.Info lineInfo;

    private Vector afs;
    private Vector sizes;
    private Vector infos;
    private Vector audios;
    private int num=0;

    public SoundManagerTeste()
    {
            afs=new Vector();
            sizes=new Vector();
            infos=new Vector();
            audios=new Vector();
    }

    public void addClip(String s)
        throws IOException, UnsupportedAudioFileException, LineUnavailableException
    {
        //InputStream inputstream = url.openStream();
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(s));
            AudioFormat af = audioInputStream.getFormat();
            int size = (int) (af.getFrameSize() * audioInputStream.getFrameLength());
            byte[] audio = new byte[size];
            DataLine.Info info = new DataLine.Info(Clip.class, af, size);
            audioInputStream.read(audio, 0, size);

            afs.add(af);
            sizes.add(new Integer(size));
            infos.add(info);
            audios.add(audio);

            num++;
    }

    private ByteArrayInputStream loadStream(InputStream inputstream)
              throws IOException
      {
            ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
            byte data[] = new byte[1024];
            for(int i = inputstream.read(data); i != -1; i = inputstream.read(data))
                  bytearrayoutputstream.write(data, 0, i);

            inputstream.close();
            bytearrayoutputstream.close();
            data = bytearrayoutputstream.toByteArray();
            return new ByteArrayInputStream(data);
    }

    public void playSound(int x)
          throws UnsupportedAudioFileException, LineUnavailableException
    {
            if(x>num)
            {
                  System.out.println("playSound: sample nr["+x+"] is not available");
            }
            else
            {
                  Clip clip = (Clip) AudioSystem.getLine((DataLine.Info)infos.elementAt(x));
                  clip.open((AudioFormat)afs.elementAt(x), (byte[])audios.elementAt(x), 0, ((Integer)sizes.elementAt(x)).intValue());
                  clip.start();
            }
      }
    
    
    public static void main(String[] args) {
		
    	
    	try {
    		
			SoundManagerTeste sou = new SoundManagerTeste();
			sou.addClip("sounds/sound1.wav");
			sou.addClip("sounds/sound2.mp3");
			
			
			sou.playSound(0);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			sou.playSound(1);
			
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
    	
	}
    
}
