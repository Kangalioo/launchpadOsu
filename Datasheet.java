import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Comparator;

public class Datasheet {
	public class Note {
		public final int x, y;
		public final int tick;
		
		public Note(int x, int y, int tick) {
			this.x = x;
			this.y = y;
			this.tick = tick;
		}
		
		/**
		 * Converts the tick to the beat, using the ticksPerBeat
		 * field of the parent Datasheet object.
		 */
		public float getBeat() {
			return tick / (float) ticksPerBeat;
		}
		
		/**
		 * Converts the tick to the second.
		 */
		public float getSecond() {
			return getBeat() / tempo;
		}
		
		/**
		 * Returns the index of this note's type in the given
		 * array of note types (First index if there's the
		 * same note type twice). -1 if it is not found.
		 * A note type is the position a note starts on in beats,
		 * e.g. 1 for notes that are on a beat,
		 * 0.5 for notes that are between two beats and so on.
		 */
		public int getNoteType(List<Float> noteTypes) {
			for (int i = 0; i < noteTypes.size(); i++) {
				if ((getBeat() % noteTypes.get(i)) < 1e-5f) {
					return i;
				}
			}
			return -1;
		}
	}
	
	private static class Parser {
		static final String KEY_VALUE_SEPARATOR = ": ";
		static final String
			NAME_KEY = "Name",
			TEMPO_KEY = "Tempo";
		
		private File file;
		private BufferedReader reader;
		private int currentLine;
		
		private Datasheet sheet;
		
		public Parser(File file) throws IOException {
			this.file = file;
			reader = new BufferedReader(new FileReader(file));
			sheet = new Datasheet();
		}
		
		public Datasheet parse() throws IOException {
			String line;
			currentLine = 0;
			while ((line = reader.readLine()) != null) {
				processLine(line);
				currentLine++;
			}
			
			return sheet;
		}
		
		private void warn(String msg) {
			System.err.println("Warning ("
					+ file.getName()
					+ ":"
					+ (currentLine + 1)
					+ "): "
					+ msg);
		}
		
		private void processLine(String line) {
			int separatorIndex = line.indexOf(KEY_VALUE_SEPARATOR);
			if (separatorIndex == -1) {
				warn("Separator \"" + KEY_VALUE_SEPARATOR + "\" not found.");
				return;
			}
			
			String key = line.substring(0, separatorIndex);
			int valueStartIndex = separatorIndex + KEY_VALUE_SEPARATOR.length();
			String value = line.substring(valueStartIndex);
			
			switch (key) {
				case NAME_KEY: parseName(value); break;
				case TEMPO_KEY: parseTempo(value); break;
				default: warn("Unrecognized key: \"" + key + "\"."); break;
			}
		}
		
		private void parseName(String string) {
			sheet.setName(string);
		}
		
		private void parseTempo(String string) {
			sheet.setTempo(Float.parseFloat(string));
		}
	}
	
	private static class Writer {
		private File target;
		private PrintWriter writer;
		
		private Datasheet sheet;
		
		public Writer(Datasheet sheet, File target) throws IOException {
			this.sheet = sheet;
			this.target = target;
			writer = new PrintWriter(new BufferedWriter(new FileWriter(target)));
		}
		
		public void write() throws IOException {
			writeKeyValuePair(Parser.NAME_KEY, sheet.getName());
			writeKeyValuePair(Parser.TEMPO_KEY, String.valueOf(sheet.getTempo()));
			writer.close();
		}
		
		private void writeKeyValuePair(String key, String value) {
			writer.print(key);
			writer.print(Parser.KEY_VALUE_SEPARATOR);
			writer.print(value);
			writer.println();
		}
	}
	
	private String name = null;
	private float tempo = 120;
	private int ticksPerBeat = 48;
	private List<Note> notes = new ArrayList<>();
	
	public static Datasheet fromFile(File file) throws IOException {
		return new Parser(file).parse();
	}
	
	public static void writeToFile(Datasheet sheet, File target)
									throws IOException {
		new Writer(sheet, target).write();
	}
	
	public void writeToFile(File target) throws IOException {
		writeToFile(this, target);
	}
	
	public String getName() {return name;}
	public void setName(String name) {this.name = name;}
	public float getTempo() {return tempo;}
	public void setTempo(float tempo) {this.tempo = tempo;}
	public List<Note> getNotes() {return notes;}
	public void addNote(Note note) {notes.add(note);}
	
	/**
	 * Sorts the notes in the order that they appear.
	 */
	public void sortNotes() {
		Collections.sort(notes, new Comparator<Note>() {
			public int compare(Note a, Note b) {
				return a.tick - b.tick;
			}
		});
	}
}
