import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class EventMap {
	public static class Event {
		private double second;
		
		public Event(double second) {
			this.second = second;
		}
		
		public double getSecond() {
			return second;
		}
		
		/**
		 * songStart has to be in nanoseconds. I used nanoseconds and
		 * long instead of seconds and double because I think that
		 * double's precision is not enough here.
		 */
		public void sleepUntilThisHappens(long songStart)
										throws InterruptedException {
			double currentSecond = (System.nanoTime() - songStart) / 1e9;
			int sleepTimeMs = (int) (1000 * (second - currentSecond));
			Thread.sleep(sleepTimeMs);
		}
	}
	
	public static class NoteEvent extends Event {
		public final Datasheet.Note note;
		
		public NoteEvent(double second, Datasheet.Note note) {
			super(second);
			this.note = note;
		}
	}
	
	final List<Event> events = new ArrayList<>();
	
	public EventMap(Datasheet sheet) {
		sheet.sortNotes();
		for (Datasheet.Note note : sheet.getNotes()) {
			events.add(new NoteEvent(note.getSecond(), note));
		}
	}
	
	public List<Event> getEvents() {
		return Collections.unmodifiableList(events);
	}
}
