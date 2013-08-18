package ph.txtdis.windows;

import java.sql.Date;

public class StockTake extends Receiving {
	private int takerId, checkerId;
	private String[] checkers, takers;

	public StockTake() {
		super();
		module = "Stock Take";
		type = "count";
	}

	public StockTake(int tagId) {
		this();
		id = tagId;
		if (id != 0) {
			objects = sql.getData(id, "" +
					// @sql:on
					"  SELECT count_date,\n" 
					+ "		  location_id,\n"
					+ "		  taker_id,\n"
					+ "		  CASE WHEN checker_id IS NULL THEN 0 ELSE checker_id END\n"
					+ "  FROM count_header\n"
					+ " WHERE count_id = ?\n"
					// @sql:off
					);
		}
		if (objects != null) {
			date = (Date) objects[0];
			locationId = (int) objects[1];
			takerId = (int) objects[2];
			checkerId = (int) objects[3];

			checkers = new String[] {new Employee(checkerId).getName()};
			locations = new String[] {new Location(locationId).getName()};
			takers = new String[] {new Employee(takerId).getName()};
			
			data = sql.getDataArray(id, "" +
					// @sql:on
					"  SELECT cd.line_id,\n" 
					+ "         cd.item_id,\n"
					+ "         im.name,\n"
					+ "         u.unit,\n"
					+ "         q.name,\n"
					+ "         cd.expiry,\n"
					+ "         cd.qty\n"
					+ "    FROM count_detail AS cd,\n"
					+ "         item_master AS im,\n"
					+ "         uom AS u,\n"
					+ "         quality AS q\n"
					+ "   WHERE     cd.item_id = im.id\n"
					+ "         AND cd.uom = u.id\n"
					+ "         AND cd.qc_id = q.id\n"
					+ "         AND cd.count_id = ?\n"
					+ "ORDER BY cd.line_id\n"
					// @sql:off
					);
		} else {
			date = DIS.TODAY;
			checkers = takers = new Employee().getNames();
			locations = new Location().getNames();
			
			checkerId = new Employee(checkers[0]).getId();
			takerId = new Employee(takers[0]).getId();
			locationId = new Location(locations[0]).getId();
		}
	}		

	public StockTake(Date stockTakeDate) {
		this();
		date = stockTakeDate;
		data = sql.getDataArray(date, "" +
				// @sql:on
				"SELECT row_number() over() AS line,\n" 
				+ "		cd.item_id,\n"
				+ "		im.name,\n"
				+ "		'PK' AS pk,\n"
				+ "		q.name,\n"
				+ "		cd.expiry,\n"
				+ "		SUM(cd.qty * qp.qty) AS qty\n"
				+ "FROM	count_header AS ch,\n"
				+ "		count_detail AS cd,\n"
				+ "		item_master AS im,\n"
				+ "		qty_per AS qp,\n"
				+ "		quality AS q\n"
				+ "WHERE	ch.count_id = cd.count_id\n"
				+ "	AND	cd.item_id = im.id\n"
				+ "	AND	cd.item_id = qp.item_id\n"
				+ "	AND cd.uom = qp.uom\n"
				+ "	AND cd.qc_id = q.id\n"
				+ "	AND ch.count_date = ?\n"
				+ "GROUP BY\n"
				+ "		cd.item_id,\n"
				+ "		im.name,\n"
				+ "		pk,\n"
				+ "		q.name,\n"
				+ "		expiry\n"
				+ "ORDER BY line\n"
				// @sql:off
				);
	}

	public boolean isOnFile(int id) {
		object = sql.getDatum(id, "" + 
				"SELECT count_id  " +
				"  FROM count_header " +
				" WHERE count_id = ? " 
				);
		return object == null ? false : true;
	}

	public int getTakerId() {
		return takerId;
	}

	public void setTakerId(int takerId) {
		this.takerId = takerId;
	}

	public String[] getTakers() {
		return takers;
	}

	public int getCheckerId() {
		return checkerId;
	}

	public void setCheckerId(int checkerId) {
		this.checkerId = checkerId;
	}

	public String[] getCheckers() {
		return checkers;
	}
}
