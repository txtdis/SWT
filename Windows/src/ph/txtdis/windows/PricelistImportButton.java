package ph.txtdis.windows;

import java.io.FileInputStream;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.eclipse.swt.widgets.Composite;

public class PricelistImportButton extends ImportButton {

	public PricelistImportButton(Composite parent, String module) {
		super(parent, module);
	}

	@Override
	protected void setStrings() {
		msg = new String[] {
				"Import new Dry " + module,
				"Import new Refrigerated Meat " + module
		};
		prefix = new String[] {
				"GT Pricelist as of ",
				"GT PHC RM Pricelist As of "
		};
		info = "RM and Dry" + "\n" + module + "s ";
	}

	@Override
	protected void setDate(String fileName, String prefix)  {
		String strDate = fileName.substring(
				fileName.indexOf(prefix) + prefix.length(), fileName.length() - 4);
		try {
			date = new Date(
					new SimpleDateFormat("MMM dd, yyyy").parse(strDate).getTime());
		} catch (ParseException e) {
			new ErrorDialog(e);
		}
	}

	@Override
	protected void extractData(FileInputStream[] is) {
		new PriceReader().set(is);
	}
}

