package ph.txtdis.windows;

public class RemittanceDialog extends DialogView {

	public RemittanceDialog() {
		// TODO Auto-generated constructor stub
	}

	public RemittanceDialog(String name, String message) {
		super(name, message);
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		new RemittanceDialog();
		Database.getInstance().closeConnection();
	}

}
