package ph.txtdis.windows;

import org.apache.commons.lang3.StringUtils;

public class OrderList extends Report {

	public OrderList(String string) {
		module = "Customer List";
		headers = new String[][] {
				{StringUtils.center("ROUTE", 12), "String"},
				{StringUtils.center("ID", 4), "ID"},
				{StringUtils.center("CUSTOMER NAME", 30), "String"},
				{StringUtils.center("ADDRESS", 64), "String"}
		};
		data = new Data().getDataArray("" +
				"WITH " +
				"route_table AS ( " +
				"	SELECT	ac.customer_id, " +
				"			r.name AS route " +
				"	FROM 	route AS r " +
				"	INNER JOIN account ac " +
				"	ON		r.id = ac.route_id " +
				"), " +
				"address_table AS ( " +
				"SELECT " +
				"		CASE WHEN prov.id IS null THEN 0 ELSE prov.id END, " +
				"		CASE WHEN prov.name IS null " +
				"			THEN '' ELSE prov.name END AS province, " +
				"		CASE WHEN city.id IS null THEN 0 ELSE city.id END, " +
				"		CASE WHEN city.name IS null " +
				"			THEN '' ELSE city.name || ', ' END AS city, " +
				"		CASE WHEN dist.id IS null THEN 0 ELSE dist.id END, " +
				"		CASE WHEN dist.name IS null " +
				"			THEN '' ELSE dist.name || ', ' END AS district, " +
				"		CASE WHEN street IS null " +
				"			THEN '' ELSE street || ', ' END AS street, " +
				"		a.customer_id " +
				"FROM 	address AS a " +
				"LEFT OUTER JOIN area AS prov " +
				"	ON prov.id = a.province " +
				"LEFT OUTER JOIN area AS city " +
				"	ON city.id = a.city " +
				"LEFT OUTER JOIN area AS dist " +
				"	ON dist.id = a.district " +
				") " +
				"SELECT	rt.route, " +
				"		cm.id, " +
				"		cm.name, " +
				"		at.street || at.district || at.city || at.province " +
				"FROM 	customer_master AS cm " +
				"LEFT OUTER JOIN address_table AS at " +
				"	ON	cm.id = at.customer_id " +
				"LEFT OUTER JOIN route_table AS rt " +
				"	ON	cm.id = rt.customer_id " +
				"WHERE cm.name LIKE '%" + string.toUpperCase() + "%' " +
				"	AND rt.route IS NOT NULL " +
				"ORDER BY cm.name" 
				);
	}
}
