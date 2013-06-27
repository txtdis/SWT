package ph.txtdis.windows;

import java.sql.Date;


public class RouteHelper {

	public RouteHelper() {
	}
	
	public boolean isBalanced(int partnerId, Date salesDate) {
		Object object = new SQL().getDatum(new Object[] {partnerId, salesDate}, "" +
				"SELECT rb.user_id " +
				"FROM	route_balance AS rb " +
				"INNER JOIN account AS a " +
				"ON rb.route_id = a.route_id " +
				"WHERE a.customer_id = ? " +
				"AND rb.route_date + 1 = ? " +
				"");
		return object == null ? false : true;
	}
}
