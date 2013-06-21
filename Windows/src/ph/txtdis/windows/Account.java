package ph.txtdis.windows;

import java.sql.Date;

public class Account {
	private int routeId;
	
	public Account(int id) {
		this(id, DIS.TODAY);
	}
	
	public Account(int id, Date date) {
		 Object o = new SQL().getDatum(new Object[] {id, date}, "" +
				"SELECT	route_id " +
				"FROM	account " +
				"WHERE	customer_id = ? " +
				"	AND start_date <= ? "  
			);
		 routeId = o == null ? 0 : (int) o;
	}

	public int getRouteId() {
		return routeId;
	}
}
