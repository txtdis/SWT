package ph.txtdis.windows;

public class VersionChecker {

	public VersionChecker() {
		ensureLatestVersion();
	}

	private void ensureLatestVersion() {
		String onFile = getVersionOnFile();
		int inUse = Integer.parseInt(DIS.VERSION.replace(".", ""));
//		if (onFile.replace("txtDIS", "") < inUse)
//			upload()
//			
		
	}

	private String getVersionOnFile() {
		return (String) new Data().getDatum(""
				+ "SELECT name "
				+ "FROM template "
				+ "WHERE name LIKE '%txtDIS%'"
				);
	}
	
	

}
