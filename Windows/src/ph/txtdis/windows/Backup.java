package ph.txtdis.windows;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Backup extends View{

	public Backup() {
		super();
		shell.setVisible(false);
		String database = Database.getDbase();
		String timeStamp = new SimpleDateFormat("-yyyy.MM.dd-HH.mm").format(Calendar.getInstance().getTime());
		String fileName = new DirectoryChooser(shell).toString() + database + timeStamp + ".backup";
		try {
			Database.getInstance().closeConnection();
			final ArrayList<String> baseCmds = new ArrayList<>();
			baseCmds.add("c:\\Program Files\\PostgreSQL\\9.2\\bin\\pg_dump.exe");
			baseCmds.add("-h");
			baseCmds.add("localhost");
			baseCmds.add("-p");
			baseCmds.add("5432");
			baseCmds.add("-U");
			baseCmds.add("txtdis");
			baseCmds.add("-f");
			baseCmds.add(fileName);
			baseCmds.add("-F");
			baseCmds.add("t");
			baseCmds.add(database);
			ProcessBuilder pb = new ProcessBuilder(baseCmds);
			pb.environment().put("PGPASSWORD", "txtdis");
			Process p = pb.start();
			p.waitFor();
			if (new File(fileName).length() == 0) {
				new ErrorDialog("Backup File is\nEMPTY");
			} else {
				new InfoDialog("Backup saved in" + "\n" + fileName);
			}
			Database.getInstance().getConnection();
			shell.dispose();
			new SystemsMenu();
		} catch (IOException | InterruptedException ex) {
			ex.printStackTrace();
			new ErrorDialog(ex);
		}
	}
}
