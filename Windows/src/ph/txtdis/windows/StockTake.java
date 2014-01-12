package ph.txtdis.windows;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

public class StockTake extends Receiving implements Startable, DataEntryClosable {
	protected boolean isDataEntryClosureSuccessful;
	
	private int takerId, checkerId;
	private String[] checkers, takers;

	public StockTake() {}

	public StockTake(int tagId) {
		super();
		type = "count";
		module = "Stock Take Tag";
		id = tagId;
		
		if (id != 0) {
			objects = sql.getData(id, "" +
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
		if (objects != null) {
			date = (Date) objects[0];
			locationId = (int) objects[1];
			takerId = (int) objects[2];
			checkerId = (int) objects[3];
			inputter = ((String) objects[4]).toUpperCase();
			timestamp = ((Timestamp) objects[5]).getTime();
			inputDate = new Date(timestamp);
			inputTime = new Time(timestamp);

			checkers = new String[] {new Employee(checkerId).getName()};
			locations = new String[] {new Location(locationId).getName()};
			takers = new String[] {new Employee(takerId).getName()};
			
			data = sql.getDataArray(id, "" +
					// @sql:on
					"  SELECT cd.line_id,\n" 
					+ "         cd.item_id,\n"
					+ "         im.short_id,\n"
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
					// @sql:o
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
		super();
		type = "count";
		module = "Stock Take";
		date = stockTakeDate;
		data = sql.getDataArray(date, "" +
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
				+ "		im.short_id,\n"
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
		Object o = new Data().getDatum(date, "" +
				"SELECT count_id " +
				"  FROM count_header " +
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

	private Date getLatestReconciledCountDate() {
		object = sql.getDatum("SELECT max(count_date) FROM count_adjustment;");		
	    return object == null ? null : (Date) object;
    }

	@Override
    public void closeDataEntry() {
		new Posting(this) {
			@Override
			protected void postData() throws SQLException {
				ps = conn.prepareStatement("" 
						//  @sql:on
						+ "INSERT INTO count_completion " 
						+ "	(count_date) VALUES (?); " 
						//  @sql:off
				        );
				ps.setDate(1, date);
				ps.executeUpdate();
			}
		};
    }

	@Override
    public boolean isDataEntryClosed(Date date) {
		Object object = new Data().getDatum(date, "" +
				"SELECT count_date " +
				"  FROM count_completion " +
				" WHERE	count_date = ? " +
				"");
		return object == null ? false : true;
    }

	@Override
    public void start() {
		new StockTakeView(DIS.YESTERDAY);
    }
}
