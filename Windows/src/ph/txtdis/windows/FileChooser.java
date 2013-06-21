package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class FileChooser {
	
	private FileDialog fd;

	public FileChooser(Shell shell, String msg, String ext) {
		fd = new FileDialog(shell, SWT.OPEN);
		fd.setText(msg);
		fd.setFilterPath(System.getProperty("user.home") + System.getProperty("file.separator"));
		fd.setFilterExtensions(new String[] {ext});
	}
	
	@Override
	public String toString() {
		return fd.open();
	}

}
