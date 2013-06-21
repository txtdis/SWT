package ph.txtdis.windows;

import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

public class DirectoryChooser {
	
	private DirectoryDialog dialog;
	private String fileSeparator = System.getProperty("file.separator");

	public DirectoryChooser(Shell shell) {
		dialog = new DirectoryDialog(shell);
	    dialog.setFilterPath(System.getProperty("user.dir")); 
	}
		
	@Override
	public String toString() {
		return dialog.open() + fileSeparator;
	}
}
