package ph.txtdis.windows;

public class BankListView extends ListView {

	public BankListView() {
		this("");
	}

	public BankListView(String text) {
		this(new BankList(text));
	}

	public BankListView(Data data) {
		super(data);
		display();
	}
}
