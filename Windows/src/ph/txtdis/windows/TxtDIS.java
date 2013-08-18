package ph.txtdis.windows;

public class TxtDIS {

	public static void main(String[] args) {
		new LoginView();
		Database.getInstance().closeConnection();
	}
}
