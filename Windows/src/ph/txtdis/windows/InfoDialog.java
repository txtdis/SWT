package ph.txtdis.windows;

public class InfoDialog extends DialogView {

	public InfoDialog(String msg) {
		super();
		setName("Information");
		setMessage(msg);
		open();
	}
}
