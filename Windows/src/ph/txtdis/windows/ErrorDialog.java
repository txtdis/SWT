package ph.txtdis.windows;

public class ErrorDialog extends DialogView {

	public ErrorDialog(String msg) {
		super(Type.ERROR, msg);
		display();
	}

	public ErrorDialog(Exception e) {
		this(e.toString().replace(": ", ":\n").replace(". ", "\n"));
	}
}
