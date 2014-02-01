package ph.txtdis.windows;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class CountAdjustment extends CountData {
	private int takerId, checkerId;
	private String[] checkers, takers;
	private Object object;

	public CountAdjustment() {
		super();
		type = Type.COUNT;
		tableHeaders = ArrayUtils.add(tableHeaders, new String[] {StringUtils.center("JUSTIFICATION", 48), "String" });
	}

	public CountAdjustment(int tagId) {
		this();
		id = tagId;
		date = getLatestReconciledDate();
		if (date == null) ;
		//date = getLatest
		
		if (id != 0) {
			headerData = sql.getList(id, "" +
					// @sql:on
					"  SELECT count_date,\n" 
					+ "		  location_id,\n"
					+ "		  taker_id,\n"
					+ "		  CASE WHEN checker_id IS NULL THEN 0 ELSE checker_id END,\n"
					+ "		  user_id,\n"
					+ "		  time_stamp\n"
					+ "  FROM count_header\n"
					+ " WHERE count_id = ?\n"
					// @sql:off
					);
		}
		if (headerData != null) {
			date = (Date) headerData[0];
			locationId = (int) headerData[1];
			takerId = (int) headerData[2];
			checkerId = (int) headerData[3];
			inputter = ((String) headerData[4]).toUpperCase();
			timestamp = ((Timestamp) headerData[5]).getTime();
			inputDate = new Date(timestamp);
			inputTime = new Time(timestamp);

			checkers = new String[] {Contact.getName(checkerId)};
			locations = new String[] {new Location(locationId).getName()};
			takers = new String[] {Contact.getName(takerId)};
			
			tableData = sql.getTableData(id, "" +
					// @sql:on
					"  SELECT cd.line_id,\n" 
					+ "         cd.item_id,\n"
					+ "         im.name,\n"
					+ "         u.unit,\n"
					+ "         q.name,\n"
					+ "         cd.expiry,\n"
					+ "         cd.qty\n"
					+ "    FROM count_detail AS cd,\n"
					+ "         item_header AS im,\n"
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
			checkers = takers = Employee.getNames();
			locations = new Location().getNames();
			
			checkerId = Contact.getId(checkers[0]);
			takerId = Contact.getId(takers[0]);
			locationId = new Location(locations[0]).getId();
		}
	}		

	public Date getLatestReconciledDate() {
		object = sql.getDatum("SELECT max(count_date) FROM count_adjustment;");		
	    return object == null ? null : (Date) object;
    }

	public CountAdjustment(Date stockTakeDate) {
		this();
		date = stockTakeDate;
		tableData = sql.getTableData(date, "" +
				// @sql:on
				"SELECT CAST (row_number() over() as int) AS line,\n" 
				+ "		cd.item_id,\n"
				+ "		im.short_id,\n"
				+ "		'PK' AS pk,\n"
				+ "		q.name,\n"
				+ "		cd.expiry,\n"
				+ "		SUM(cd.qty * qp.qty) AS qty\n"
				+ "FROM	count_header AS ch,\n"
				+ "		count_detail AS cd,\n"
				+ "		item_header AS im,\n"
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

	public boolean isDone(Date date) {
		Object o = new Query().getDatum(date, "" +
				"SELECT count_id " +
				"  FROM count_header " +
				" WHERE	count_date = ? " +
				"");
		return (o == null ? false : true);
	}
	
	public boolean isClosed(Date date) {
		Object o = new Query().getDatum(date, "" +
				"SELECT count_date " +
				"  FROM count_completion " +
				" WHERE	count_date = ? " +
				"");
		return (o == null ? false : true);
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
