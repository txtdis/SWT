package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class SearchButton extends ImageButton {

	public SearchButton(Composite parent, Type type) {
		super(parent, type.getName(), Type.SEARCH.toString(), "Search");
	}

	@Override
	public void proceed(){
		parent.getShell().close();
		new InputDialog(module) {
			@Override
			protected void setOkButtonAction() {
				String string = text.getText();
				shell.dispose();
				switch (module) {
					case "Bank List": 
						new BankListView(string); 
						break;
					case "Customer List": 
						new CustomerListView(string); 
						break;
					case "Inventory": 
						new InventoryView(string); 
						break;
					case "Issued Invoice Booklet List": 
						new InvoiceBookletListView(string); 
						break;
					case "Item List": 
						new ItemListView(string); 
						break;
					default: 
						new ErrorDialog("No option for\n" + module); 
						break;
				}
			}
		};
	}

	private String setMessage() {
		String message;
		switch (module) {
			case "Issued Invoice Booklet List":
				message = "Enter Invoice # or\npart of the person's name\n" +
						"who had received booklet/s";
				break;
			case "Inventory":
				message = "Enter\na Part of the Needed\nItem Name";
				break;
			default:
				message = "Enter\na Part of the Needed\n" + module + " Name";
				break;
		}
		return message;
    }	
}

