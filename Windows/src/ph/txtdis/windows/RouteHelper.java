package ph.txtdis.windows;

import java.util.Arrays;

public class RouteHelper {

	public RouteHelper() {
	}
	
	public String getRoute(int routeId) {
		return (String) new SQL().getDatum(routeId, "" +
				"SELECT name " +
				"FROM 	route " +
				"WHERE 	id = ? "
				);
	}
	
	public String[] getRoutes() {
		Object[] os = new SQL().getData("" +
				"SELECT name " +
				"FROM 	route " +
				"ORDER BY id "
				);
		return Arrays.copyOf(os, os.length, String[].class);
	}


}
