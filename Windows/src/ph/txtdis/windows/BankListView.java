package ph.txtdis.windows;

public class BankListView extends ListView {

	public BankListView(String string) {
		super(string);
	}

	@Override
	protected void runClass() {
		report = new BankList(string);
	}
}
