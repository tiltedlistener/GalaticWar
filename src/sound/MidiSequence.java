package sound;

import javax.sound.midi.*;
import java.io.*;
import java.net.*;

public class MidiSequence {

	private Sequencer sequencer;
	
	private Sequence song;
	public Sequence getSong() { return song; }
	
	private String filename;
	public String getFilename() { return filename; }
	
	private boolean looping = false;
	public boolean getLooping() { return looping; }
	public void setLooping(boolean _looping) { looping = _looping; }
	
	private int repeat = 0;
	public int getRepeat() { return repeat; }
	public void setRepeat(int _repeat) { repeat = _repeat; }
	
	public boolean isLoaded() { 
		return (boolean)(sequencer.isOpen());
	}
	
	public MidiSequence() { 
		try {
			sequencer = MidiSystem.getSequencer();
		} catch (MidiUnavailableException e) {}
	}
	
	public MidiSequence(String midiFile) {
		this();
		load(midiFile);
	}
	
	public boolean load(String midiFile) {
		try {
			filename = midiFile;
			song = MidiSystem.getSequence(new File(filename));
			sequencer.setSequence(song);
			sequencer.open();
			return true;
		} catch(InvalidMidiDataException e) {
			return false;
		} catch(MidiUnavailableException e) {
			return false;
		} catch(IOException e) {
			return false;
		}
	}
	
	public void play() {
		if (!sequencer.isOpen()) return;
		if (looping) {
			sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
			sequencer.start();
		} else {
			sequencer.setLoopCount(repeat);
			sequencer.start();
		}
	}
	
	public void stop() {
		sequencer.stop();
	}
	
	
}
