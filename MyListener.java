public class MyListener implements LaunchpadListener {
	private Launchpad l;
	
	public MyListener(Launchpad launchpad) {
		l = launchpad;
		l.setListener(this);
	}
	
	public void padPressed(int x, int y) {
		l.setPadColor(x, y, Color.RED);
	}
	
	public void padReleased(int x, int y) {
		l.setPadColor(x, y, Color.BLACK);
	}
	
	public void start() {
		l.setPadColor(0, 0, Color.GREEN);
	}
	
	public void stop() {
		l.close();
	}
}
