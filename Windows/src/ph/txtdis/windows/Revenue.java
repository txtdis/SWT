package ph.txtdis.windows;

import java.sql.Date;

import org.apache.commons.lang3.StringUtils;

public class Revenue extends Order {
	
	public Revenue(Date date) {
		this();
		postDate = date;
		data = new SQL().getDataArray(postDate, "" +
				"SELECT row_number() OVER() AS line, " +
				"		cd.item_id, " +
				"		im.name, " +
				"		'PK' AS pk, " +
				"		SUM(cd.qty * qp.qty) AS qty, " +
				"		q.name, " +
				"		cd.expiry " +
				"FROM	count_header AS ch, " +
				"		count_detail AS cd, " +
				"		item_master AS im, " +
				"		qty_per AS qp, " +
				"		quality AS q " +
				"WHERE	ch.count_id = cd.count_id " +
				"	AND	cd.item_id = im.id " +
				"	AND	cd.item_id = qp.item_id " +
				"	AND cd.uom = qp.uom " +
				"	AND cd.qc_id = q.id " +
				"	AND ch.count_date = ? " +
				"GROUP BY " +
				"		cd.item_id, " +
				"		im.name, " +
				"		pk, " +
				"		q.name," +
				"		expiry " +
				"ORDER BY line ");
	}

	public Revenue() {
		module = "Revenue Report";
		headers = new String[][] {
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("ID", 4), "ID"},
				{StringUtils.center("ROUTE NAME", 10), "String"},
				{StringUtils.center(" ", 1), "String"},
				{StringUtils.center("SYS S/I(D/R)", 11), "Quantity"},
				{StringUtils.center("ACT S/I(D/R)", 11), "Quantity"},
				{StringUtils.center("REMITTANCE", 11), "Quantity"},
				{StringUtils.center("DUE DSP(CO.)", 11), "Quantity"}
		};
	}


	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		Revenue st = new Revenue();
		if(st.getData() !=null) {
			for (Object[] os : st.getData()) {
				for (Object o : os) {
					System.out.print(o + ", ");
				}
				System.out.println();
			}
		} else {
			System.out.println("No data");
		}
		Database.getInstance().closeConnection();
	}
}
