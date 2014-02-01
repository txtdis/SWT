package ph.txtdis.windows;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

public class CountData extends ReceivingData implements Subheaded, Closeable {
	protected boolean isDataEntryClosureSuccessful;

	private int takerId, checkerId;
	private String[] checkers, takers;

	public CountData() {
		super();
    }

	public CountData(int id) {
		this();
		type = Type.COUNT;
		this.id = id;
		if (id != 0) {
			headerData = sql.getList(id,"" +
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

			checkers = new String[] { Contact.getName(checkerId) };
			locations = new String[] { new Location(locationId).getName() };
			takers = new String[] { Contact.getName(takerId) };

			tableData = sql.getTableData(id,"" +
					// @sql:on
					"  SELECT cd.line_id,\n" 
					+ "         cd.item_id,\n"
					+ "         im.short_id,\n"
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
					// @sql:o
					);
		} else {
			date = DIS.TODAY;
			checkers = takers = Employee.getNames();
			if(takers == null)
				System.out.println("null");
			else 
			    System.out.println("not null");
			locations = new Location().getNames();
			
			checkerId = Contact.getId(checkers[0]);
			takerId = Contact.getId(takers[0]);
			locationId = new Location(locations[0]).getId();
		}
	}		

	@Override
    protected void setProperties() {
		type = Type.COUNT;
    }

	public CountData(Date countDate) {
		this();
		type = Type.COUNT;
		date = countDate;
		tableData = sql.getTableData(date, "" +
				// @sql:on
				  "SELECT CAST (row_number() over(ORDER BY cd.item_id) AS int),\n" 
				+ "		  cd.item_id,\n"
				+ "		  im.short_id,\n"
				+ "		  'PK' AS pk,\n"
				+ "		  q.name,\n"
				+ "		  cd.expiry,\n"
				+ "		  sum (cd.qty * qp.qty) AS qty\n"
				+ "  FROM count_header AS ch,\n"
				+ "		  count_detail AS cd,\n"
				+ "		  item_header AS im,\n"
				+ "		  qty_per AS qp,\n"
				+ "		  quality AS q\n"
				+ " WHERE	  ch.count_id = cd.count_id\n"
				+ "	      AND cd.item_id = im.id\n"
				+ "	      AND cd.item_id = qp.item_id\n"
				+ "	      AND cd.uom = qp.uom\n"
				+ "	      AND cd.qc_id = q.id\n"
				+ "	      AND ch.count_date = ?\n"
				+ " GROUP BY cd.item_id,\n"
				+ "		     im.short_id,\n"
				+ "		     pk,\n"
				+ "		     q.name,\n"
				+ "		     expiry\n"
				+ " ORDER BY cd.item_id\n"
				// @sql:off
		        );
	}

	@Override
    public void closeTransaction() {
		new Posting(this) {
			@Override
			protected void postData() throws SQLException {
				ps = conn.prepareStatement("INSERT INTO count_completion (count_date) VALUES (?); ");
				ps.setDate(1, date);
				ps.executeUpdate();
			}
		}.save();
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
	
	public int getTakerId() {
		return takerId;
	}

	public void setTakerId(int takerId) {
		this.takerId = takerId;
	}

	public String[] getTakers() {
		return takers;
	}

	@Override
    public String getSubheading() {
		return "Summary of Count Conducted on " + DIS.LONG_DATE.format(date);  
	}
	
	@Override
	public boolean isEnteredItemQuantityValid(String qty){
		return true;
	}
}
