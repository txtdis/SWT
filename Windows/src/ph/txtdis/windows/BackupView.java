package ph.txtdis.windows;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class BackupView extends View {
	private String error, database, timeStamp, path;

	
	public BackupView() {
		super();
		shell.setVisible(false);
		database = Login.server();
		timeStamp = new SimpleDateFormat("-yyyy.MM.dd-HH.mm").format(Calendar.getInstance().getTime());
		path = new DirectoryChooser(shell).toString();
		if (!path.contains("null"))
			proceed();
	}

	@Override
    protected void proceed() {
		DBMS.getInstance().closeConnection();
		String fileName = path + database + timeStamp + ".backup";
		final ArrayList<String> baseCmds = new ArrayList<>();
		baseCmds.add("c:\\Program Files\\PostgreSQL\\9.3\\bin\\pg_dump.exe");
		baseCmds.add("-h");
		baseCmds.add(Login.network());
		baseCmds.add("-p");
		baseCmds.add("5432");
		baseCmds.add("-U");
		baseCmds.add("postgres");
		baseCmds.add("-f");
		baseCmds.add(fileName);
		baseCmds.add("-F");
		baseCmds.add("c");
		baseCmds.add(database);
		final ProcessBuilder pb = new ProcessBuilder(baseCmds);
		pb.environment().put("PGPASSWORD", "postgres");

		new ProgressDialog("Backing " + Login.server().toUpperCase() + " up...") {
			@Override
			public void proceed() {
					try {
                        Process process = pb.start();
                        process.waitFor();
                    } catch (IOException | InterruptedException e) {
                    	error = e.getMessage().replace(". ", ".\n").replace(": ", ":\n");
                    }
			}
		};
					
		if (new File(fileName).length() == 0)
			new ErrorDialog("Backup File is EMPTY;\n" + DBMS.error() + "\n" + error);
		else
			new InfoDialog("Backup saved in\n" + fileName + "\nLog-in again");
		
		UI.closeApp();
		new LoginView();
    }
}
