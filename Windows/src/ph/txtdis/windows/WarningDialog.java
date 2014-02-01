package ph.txtdis.windows;

public class WarningDialog extends DialogView {

	public WarningDialog(String message) {
		super(Type.WARNING, message);
		proceed();
	}
}
