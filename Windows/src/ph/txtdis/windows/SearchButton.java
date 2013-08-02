package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class SearchButton extends ImageButton {

	public SearchButton(Composite parent, String module) {
		super(parent, module, "Search32", "Search " + module);
	}

	@Override
	public void doWhenSelected(){
		parent.getShell().dispose();
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
					case "Irregular Activities": 
						new IrregularListView(string); 
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
			@Override
			public void setName(String name) {
				this.name = "Search";
			}
			@Override
			public void setMessage(String message) {
				switch (module) {
					case "Issued Invoice Booklet List":
						this.message = "Enter Invoice # or\npart of the person's name\n" +
								"who had received booklet/s";
						break;
					case "Inventory":
						this.message = "Enter\na Part of the Needed\nItem Name";
						break;
					default:
						this.message = "Enter\na Part of the Needed\n" + module + " Name";
						break;
				}
			}

		};
	}
}

