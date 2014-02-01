package ph.txtdis.windows;

public class ItemListView extends ListView {

	public ItemListView() {
		this("");
	}

	public ItemListView(String text) {
		this(new ItemList(text));
	}

	public ItemListView(Data data) {
		super(data);
	}
}
