import javax.sound.midi.MidiUnavailableException;
import java.io.File;

public class Main {
	public static void main(String[] args) throws Exception {
		Datasheet sheet = Datasheet.fromFile(new File("sheet.sht"));
		sheet.writeToFile(new File("target.sht"));
		
		
		if(true)return;
		try {
			LaunchpadS lowLevel = new LaunchpadS();
			Launchpad l = new Launchpad(lowLevel);
			l.open(Utils.findLaunchpad(lowLevel));
			MyListener listener = new MyListener(l);
			listener.start();
			
			System.out.println("Connection is ready. Press enter to stop.");
			System.console().readLine();
			listener.stop();
			System.exit(0);
		} catch (MidiUnavailableException e) {
			e.printStackTrace();
		}
	}
}
