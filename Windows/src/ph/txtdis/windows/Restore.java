package ph.txtdis.windows;

public class Restore extends Report implements Startable {

	public Restore() {}

	@Override
	public void start() {
		new RestoreView();
	}
}
