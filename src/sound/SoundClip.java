package sound;

import javax.sound.sampled.*;
import java.io.*;

public class SoundClip {

	private AudioInputStream sample;
	private Clip clip;
	public Clip getClip() { return clip; }
	
	private boolean looping = false;
	public void setLooping(boolean _looping) {
		looping = _looping;
	}
	public boolean getLooping() {
		return looping;
	}
	
	private int repeat = 0;
	public void setRepeat(int _repeat) { repeat = _repeat; }
	public int getRepeat() { return repeat; }
	
	private String filename = "";
	public void setFilename(String _filename) { filename = _filename; }
	public String getFilename() { return filename; }
	
	public boolean isLoaded() {
		return (boolean)(sample != null);
	}
	
	public SoundClip() {
		try {
			clip = AudioSystem.getClip();
		} catch (LineUnavailableException e) {}
	}
	
	public SoundClip(String audioFile) {
		this();
		load(audioFile);
	}
	
	public boolean load(String audioFile) {
		try {
			setFilename(audioFile);
			sample = AudioSystem.getAudioInputStream(new File(filename));
			clip.open(sample);
			return true;
		} catch(IOException e) {
			return false;
		} catch(LineUnavailableException e) {
			return false;
		} catch(UnsupportedAudioFileException e) {
			return false;
		} catch(Exception e) {
			return false;
		}
	}
	
	public void play() {
		if(!isLoaded()) return;
		clip.setFramePosition(0);
		if(looping) {
			clip.loop(Clip.LOOP_CONTINUOUSLY);
		} else {
			clip.loop(repeat);
		}
	}
	
	public void stop() {
		clip.stop();
	}
	
}
