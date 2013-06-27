package ph.txtdis.windows;

import java.sql.Date;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

public class StockTake extends Order {
	private int locationId, takerId, checkerId;
	private ArrayList<ItemCount> itemCount;

	public StockTake(int tagId) {
		this();
		id = tagId;
		if (id == 0) {
			postDate = new DateAdder().plus(0);
		} else {
			Object[] ao = new SQL().getData(id, "" +
					"SELECT	count_date, " +
					"		location_id, " +
					"		taker_id," +
					"		CASE WHEN checker_id IS NULL THEN 0 ELSE checker_id END " +
					"FROM	count_header " +
					"WHERE	count_id = ? "
					);
			postDate = (Date) ao[0];
			locationId = (int) ao[1];
			takerId = (int) ao[2];
			checkerId = (int) ao[3];
			data = new SQL().getDataArray(id, "" +
					"SELECT cd.line_id, " +
					"		cd.item_id, " +
					"		im.name, " +
					"		u.unit, " +
					"		cd.qty," +
					"		q.name, " +
					"		cd.expiry " +
					"FROM	count_detail AS cd, " +
					"		item_master AS im, " +
					"		uom AS u," +
					"		quality AS q " +
					"WHERE	cd.item_id = im.id " +
					"	AND	cd.uom = u.id " +
					"	AND cd.qc_id = q.id " +
					"	AND cd.count_id = ? " +
					"ORDER BY cd.line_id " +
					"" );
		}		
	}

	public StockTake(Date stockTakeDate) {
		this();
		postDate = stockTakeDate;
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

	public StockTake() {
		module = "Stock Take";
		headers = new String[][] {
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("ID", 4), "ID"},
				{StringUtils.center("PRODUCT NAME", 40), "String"},
				{StringUtils.center("UOM", 5), "String"},
				{StringUtils.center("QTY", 5), "Quantity"},
				{StringUtils.center("QUALITY", 7), "String"},
				{StringUtils.center("EXPIRY", 10), "Date"}

		};
	}

	public int getLocationId() {
		return locationId;
	}
	
	public void setLocationId(int locationId) {
		this.locationId = locationId;
	}

	public int getTakerId() {
		return takerId;
	}

	public void setTakerId(int takerId) {
		this.takerId = takerId;
	}

	public int getCheckerId() {
		return checkerId;
	}

	public void setCheckerId(int checkerId) {
		this.checkerId = checkerId;
	}

	public ArrayList<ItemCount> getItemCount() {
		return itemCount;
	}

	public void setItemCount(ArrayList<ItemCount> itemCount) {
		this.itemCount = itemCount;
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		StockTake st = new StockTake(0);
		if(st.getData() != null) {
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
