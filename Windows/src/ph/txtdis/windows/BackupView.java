package ph.txtdis.windows;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class BackupView extends View {
	private Process process;

	public BackupView() {
		super();
		shell.setVisible(false);
		String database = Login.getSite();
		String timeStamp = new SimpleDateFormat("-yyyy.MM.dd-HH.mm").format(Calendar.getInstance().getTime());
		String path = new DirectoryChooser(shell).toString();
		if (!path.contains("null")) {
			Database.getInstance().closeConnection();
			String fileName = path + database + timeStamp + ".backup";
			final ArrayList<String> baseCmds = new ArrayList<>();
			baseCmds.add("c:\\Program Files\\PostgreSQL\\9.2\\bin\\pg_dump.exe");
			baseCmds.add("-h");
			baseCmds.add("localhost");
			baseCmds.add("-p");
			baseCmds.add("5432");
			baseCmds.add("-U");
			baseCmds.add("postgres");
			baseCmds.add("-f");
			baseCmds.add(fileName);
			baseCmds.add("-F");
			baseCmds.add("t");
			baseCmds.add(database);
			final ProcessBuilder pb = new ProcessBuilder(baseCmds);
			pb.environment().put("PGPASSWORD", "postgres");
			new ProgressDialog("Backing " + Login.getSite().toUpperCase() + " up...") {
				@Override
				public void proceed() {
					try {
						process = pb.start();
						process.waitFor();
					} catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
			if (new File(fileName).length() == 0) {
				new ErrorDialog("Backup File is\nEMPTY");
				new BackupView();
			} else {
				new InfoDialog("Backup saved in\n" + fileName);
				new LoginView();
			}
		}
	}
}
