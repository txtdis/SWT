package ph.txtdis.windows;


public class ReceivingDialogView extends ReceivingView {

	public ReceivingDialogView(int rrId) {
		super(rrId);
	}

	@Override
	protected void runClass() {
		report = receiving = new ReceivingDialog(rrId);
	}
	
	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		new ReceivingDialogView(0);
		Database.getInstance().closeConnection();
	}
}
