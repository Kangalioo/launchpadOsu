import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class Datasheet {
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
			System.err.println("Warning (" + file.getName() + ":" + (currentLine + 1) + "): " + msg);
		}
		
		private void processLine(String line) {
			int separatorIndex = line.indexOf(KEY_VALUE_SEPARATOR);
			if (separatorIndex == -1) {
				warn("Separator \"" + KEY_VALUE_SEPARATOR + "\" not found.");
				return;
			}
			
			String key = line.substring(0, separatorIndex);
			String value = line.substring(separatorIndex + KEY_VALUE_SEPARATOR.length());
			
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
	
	private String name;
	private float tempo;
	
	public Datasheet() {
		this("Unnamed", 120);
	}
	
	public Datasheet(String name, float tempo) {
		setName(name);
		setTempo(tempo);
	}
	
	public static Datasheet fromFile(File file) throws IOException {
		return new Parser(file).parse();
	}
	
	public static void writeToFile(Datasheet sheet, File target) throws IOException {
		new Writer(sheet, target).write();
	}
	
	public void writeToFile(File target) throws IOException {
		writeToFile(this, target);
	}
	
	public String getName() {return name;}
	public void setName(String name) {this.name = name;}
	public float getTempo() {return tempo;}
	public void setTempo(float tempo) {this.tempo = tempo;}
}
