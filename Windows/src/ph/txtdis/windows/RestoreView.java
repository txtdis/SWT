package ph.txtdis.windows;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class RestoreView extends View {

	public RestoreView() {
		super();
		if (Login.group().equals("super_user"))
			display();
	}

	@Override
    protected void display() {
		String database = Login.server();
		String fileName = new FileChooser(shell, "Import restore file", database + "*.backup").toString();
		if (fileName != null) {
			if (new File(fileName).length() == 0) {
				new ErrorDialog("Backup File is\n EMPTY.\nChoose Another.");
			} else {
				try {
					DBMS.getInstance().closeConnection();
					final ArrayList<String> baseCmds = new ArrayList<>();
					baseCmds.add("c:\\Program Files\\PostgreSQL\\9.2\\bin\\pg_restore");
					baseCmds.add("-h");
					baseCmds.add("localhost");
					baseCmds.add("-p");
					baseCmds.add("5432");
					baseCmds.add("-U");
					baseCmds.add("txtdis");
					baseCmds.add("-f");
					baseCmds.add(fileName);
					baseCmds.add("-c");
					baseCmds.add("-C");
					baseCmds.add("-d");
					baseCmds.add(database);
					ProcessBuilder pb = new ProcessBuilder(baseCmds);
					pb.environment().put("PGPASSWORD", "txtdis");
					Process p = pb.start();
					p.waitFor();
					if (p.exitValue() == 0) {
						new InfoDialog("Restored database from\n" + fileName);
					} else {
						new ErrorDialog("Database was NOT\nrestored\n" + p.exitValue());
					}
				} catch (IOException | InterruptedException ex) {
					ex.printStackTrace();
					new ErrorDialog(ex);
				}
			}
		} else {
			new ErrorDialog("Database was\nNOT restored.");
		}
    }
}
