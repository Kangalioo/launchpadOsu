import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;

public class MyListener implements LaunchpadListener {
	private static final File DATASHEET = new File("sheet.sht");
	
	private static final List<Float> NOTE_TYPES = Arrays.asList(2f, 1f, .5f);
	private static final Color[] NOTE_COLORS = {Color.YELLOW, Color.RED, Color.GREEN};
	
	private Datasheet sheet;
	private EventMap eventMap;
	private Launchpad l;
	
	public MyListener(Launchpad launchpad) {
		l = launchpad;
		l.setListener(this);
	}
	
	public void start() {
		try {
			readDatasheet(DATASHEET);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		long startTime = System.nanoTime();
		for (EventMap.Event event : eventMap.getEvents()) {
			if (event instanceof EventMap.NoteEvent) {
				EventMap.NoteEvent noteEvent = (EventMap.NoteEvent) event;
				try {
					noteEvent.sleepUntilThisHappens(startTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Datasheet.Note note = noteEvent.note;
				l.setPadColor(note.x, note.y, colorFromNote(note));
			}
		}
	}
	
	private Color colorFromNote(Datasheet.Note note) {
		int tpb = sheet.getTicksPerBeat();
		int noteType = note.getNoteType(NOTE_TYPES);
		if (noteType == -1) {
			// Faint yellow
			return new Color(1, 1);
		}
		return NOTE_COLORS[noteType];
	}
	
	private void readDatasheet(File sheetFile) throws IOException {
		sheet = Datasheet.fromFile(sheetFile);
		eventMap = new EventMap(sheet);
	}
	
	public void stop() {
		l.close();
	}
	
	public void padPressed(int x, int y) {
		l.setPadColor(x, y, Color.RED);
	}
	
	public void padReleased(int x, int y) {
		l.setPadColor(x, y, Color.BLACK);
	}
}
