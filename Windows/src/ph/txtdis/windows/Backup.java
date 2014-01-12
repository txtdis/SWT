package ph.txtdis.windows;

public class Backup extends Report implements Startable {

	public Backup() {}

	@Override
	public void start() {
		new BackupView();
	}
}
