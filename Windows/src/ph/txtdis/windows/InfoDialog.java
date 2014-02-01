package ph.txtdis.windows;

public class InfoDialog extends DialogView {

	public InfoDialog(String message) {
		super(Type.INFO, message);
		proceed();
	}
}
