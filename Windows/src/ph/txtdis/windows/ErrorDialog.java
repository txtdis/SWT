package ph.txtdis.windows;

public class ErrorDialog extends DialogView {

	public ErrorDialog(String msg) {
		super();
		setName("Error");
		setMessage(msg);
		open();
	}

	public ErrorDialog(Exception e) {
		this(e.toString().replace(": ", ":\n").replace(". ", "\n"));
	}
}
