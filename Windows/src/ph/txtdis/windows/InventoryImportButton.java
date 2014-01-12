package ph.txtdis.windows;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.eclipse.swt.widgets.Composite;

public class InventoryImportButton extends ImporterButton {

	public InventoryImportButton(Composite parent, String module) {
		super(parent, module);
	}

	@Override
	protected void setStrings() {
		msg = new String[] {"Import new " + module + " Template"};
		prefix = new String[] {"GMA-GT INV TEMPLATE "};
		info = module + "\n" + "template ";
	}

	@Override
	protected void setDate(String fileName, String prefix) throws ParseException  {
		String strDate = fileName.substring(fileName.indexOf(prefix) + prefix.length(), fileName.length() - 4);
		date = new Date(new SimpleDateFormat("MMddyyyy").parse(strDate).getTime());
	}
}

