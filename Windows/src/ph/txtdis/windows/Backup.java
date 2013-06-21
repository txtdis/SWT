package ph.txtdis.windows;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class Backup extends View{

	public Backup() {
		super();
		shell.setVisible(false);
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmm").format(Calendar.getInstance().getTime());
		String fileName = new DirectoryChooser(shell).toString() + "Backup" + timeStamp + ".tar";
		try {
			Database.getInstance().closeConnection();
			String ip = InetAddress.getLocalHost().getHostAddress();
//			ip = " 192.168.1.100 ";
			ip = "localhost";
			final ArrayList<String> baseCmds = new ArrayList<>();
			baseCmds.add("c:\\Program Files\\PostgreSQL\\9.2\\bin\\pg_dump.exe");
			baseCmds.add("-h");
			baseCmds.add(ip);
			baseCmds.add("-p");
			baseCmds.add("5432");
			baseCmds.add("-U");
			baseCmds.add("txtdis");
			baseCmds.add("-f");
			baseCmds.add(fileName);
			baseCmds.add("-F");
			baseCmds.add("t");
			baseCmds.add("txtdis");
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
