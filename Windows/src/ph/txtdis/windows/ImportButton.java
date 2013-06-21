package ph.txtdis.windows;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import org.eclipse.swt.widgets.Composite;

public abstract class ImportButton extends ImageButton {
	protected String[] prefix;
	protected String[] msg;
	protected String info;
	protected Date date;
	protected FileInputStream[] is;

	public ImportButton(Composite parent, String module) {
		super(parent, module, "Download", "Import Template");
		setStrings();
	}

	@Override
	protected void open() {
		PreparedStatement psu = null;
		try {
			Connection conn = Database.getInstance().getConnection();
			int size = prefix.length;
			is = new FileInputStream[size];
			for (int i = 0; i < size; i++) {
				String fileName = new FileChooser(
						parent.getShell(), msg[i], prefix[i] + "*.xls").toString();
				if (fileName == null ) return;
				setDate(fileName, prefix[i]);
				setModule(i);
				File file = new File(fileName);
				is[i] = new FileInputStream(file);
				psu = conn.prepareStatement(
						"INSERT INTO template (name, file, start_date) " +
						"VALUES (?, ?, ?)");
				psu.setString(1, module);
				psu.setBinaryStream(2, is[i], file.length());
				psu.setDate(3, date);
				psu.executeUpdate();
			}
			extractData(is);
			new InfoDialog("" +
					info + "updated to\n" + 
					new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH).format(date) + 
					"\nversion"
					);
		} catch (Exception e) {
			e.printStackTrace();
			new ErrorDialog(e);
		} finally {
			try {
				if (is != null) 
					for (InputStream fis : is) 
						if (fis != null) fis.close();
				if (psu != null) psu.close();
			} catch (Exception e2) {
				e2.printStackTrace();
				new ErrorDialog(e2);
			}			
		}
	}

	protected void extractData(FileInputStream[] is) {}
	protected void setStrings() {}
	protected void setDate(String fileName, String prefix) throws ParseException {}
	protected void setModule(int i) {};
}