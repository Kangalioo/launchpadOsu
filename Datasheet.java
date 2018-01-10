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
		public double getBeat() {
			return tick / (double) ticksPerBeat;
		}
		
		/**
		 * Converts the tick to the second.
		 */
		public double getSecond() {
			return getBeat() * 60d / tempo;
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
				if ((getBeat() % noteTypes.get(i)) < 1e-5d) {
					return i;
				}
			}
			return -1;
		}
		
		public String toString() {
			return "(" + x + "|" + y + ") at " + tick;
		}
	}
	
	private static class Parser {
		static final String KEY_VALUE_SEPARATOR = ": ";
		/**
		 * These keys can contain any character
		 * combination except the KEY_VALUE_SEPARATOR.
		 */
		static final String
			NAME_KEY = "Name",
			TEMPO_KEY = "Tempo",
			TICKS_PER_BEAT_KEY = "TicksPerBeat",
			NOTES_KEY = "Notes";
		
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
				try {
					processLine(line);
				} catch (Throwable t) {
					warn("An exception occured: ");
					t.printStackTrace();
				}
				currentLine++;
			}
			
			return sheet;
		}
		
		private void warn(String msg) {
			System.err.println("Warning while parsing ("
					+ file.getName()
					+ ":"
					+ (currentLine + 1)
					+ "): "
					+ msg);
		}
		
		private void processLine(String line) {
			// If line is empty or comment, skip
			if (line.matches(" *") || line.charAt(0) == '#') {
				return;
			}
			
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
				case TICKS_PER_BEAT_KEY: parseTicksPerBeat(value); break;
				case NOTES_KEY: parseNotes(value); break;
				default: warn("Unrecognized key: \"" + key + "\"."); return;
			}
		}
		
		private void parseName(String string) {
			sheet.setName(string);
		}
		
		private void parseTempo(String string) {
			sheet.setTempo(Float.parseFloat(string));
		}
		
		private void parseTicksPerBeat(String string) {
			sheet.setTicksPerBeat(Integer.parseInt(string));
		}
		
		private void parseNotes(String string) {
			String[] noteStrings = string.split(", ");
			for (String noteString : noteStrings) {
				parseNote(noteString);
			}
		}
		
		private void parseNote(String string) {
			String[] numberStrings = string.split(" ");
			if (numberStrings.length > 3) {
				warn("Note has more than the expected three attributes.");
			} else if (numberStrings.length < 3) {
				warn("Note has not the required three attributes. Skipping.");
				return;
			}
			
			try {
				int x = Integer.parseInt(numberStrings[0]);
				int y = Integer.parseInt(numberStrings[1]);
				int tick = Integer.parseInt(numberStrings[2]);
				sheet.addNote(x, y, tick);
			} catch (NumberFormatException e) {
				warn("Not a valid number: " + e.getMessage());
			}
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
			writeKeyValuePair(Parser.TICKS_PER_BEAT_KEY,
					String.valueOf(sheet.getTicksPerBeat()));
			writer.println();
			for (Note note : sheet.getNotes()) {
				writeKeyValuePair(Parser.NOTES_KEY, toShtFormatString(note));
			}
			writer.close();
		}
		
		private void writeKeyValuePair(String key, String value) {
			writer.print(key);
			writer.print(Parser.KEY_VALUE_SEPARATOR);
			writer.print(value);
			writer.println();
		}
		
		public static String toShtFormatString(Note... notes) {
			StringBuilder builder = new StringBuilder();
			
			for (int i = 0; i < notes.length; i++) {
				Note note = notes[i];
				builder.append(note.x);
				builder.append(" ");
				builder.append(note.y);
				builder.append(" ");
				builder.append(note.tick);
				
				if (i + 1 != notes.length) {
					builder.append(", ");
				}
			}
			
			return builder.toString();
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
	public int getTicksPerBeat() {return ticksPerBeat;}
	public void setTicksPerBeat(int ticksPerBeat) {
		this.ticksPerBeat = ticksPerBeat;
	}
	public List<Note> getNotes() {return notes;}
	public void addNote(Note note) {notes.add(note);}
	public void addNote(int x, int y, int tick) {addNote(new Note(x, y, tick));}
	
	/**
	 * Sorts the notes in the order that they are shown.
	 */
	public void sortNotes() {
		Collections.sort(notes, new Comparator<Note>() {
			public int compare(Note a, Note b) {
				return a.tick - b.tick;
			}
		});
	}
}
