package ph.txtdis.windows;

public class WarningDialog extends DialogView {

	public WarningDialog(String msg) {
		super();
		setName("Warning");
		setMessage(msg);
		open();
	}
}
