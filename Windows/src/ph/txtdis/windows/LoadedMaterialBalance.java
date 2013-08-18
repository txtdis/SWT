package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

public class LoadedMaterialBalance extends Report {
	private Data sql;
	private Date startDate, endDate;
	private String stmt;
	
	public LoadedMaterialBalance() {
		sql = new Data();
	}
	

	public LoadedMaterialBalance(Date[] loadingDates, int loadedRouteId) {
		this();
		module = "Loaded Material Balance";
		dates = loadingDates;
		if (dates == null) {
			startDate = DIS.TODAY;
			endDate = DIS.TODAY;
			dates = new Date[] {
			        startDate, endDate };
		}
		startDate = dates[0];
		endDate = dates[1];
		dates = loadingDates;
		routeId = loadedRouteId;

		headers = new String[][] {
		        {
		                StringUtils.center("#", 2), "Line" }, {
		                StringUtils.center("ID", 4), "ID" }, {
		                StringUtils.center("PRODUCT NAME", 40), "String" }, {
		                StringUtils.center("LOADED", 10), "Quantity" }, {
		                StringUtils.center("SOLD", 10), "Quantity" }, {
		                StringUtils.center("RETURNED", 10), "Quantity" }, {
		                StringUtils.center("KEPT", 10), "Quantity" }, {
		                StringUtils.center("GAIN(LOSS)", 10), "Quantity" }, {
		                StringUtils.center(DIS.CURRENCY_SIGN + " VALUE", 14), "BigDecimal" } };

		// @sql:on
		stmt =	"WITH\n"
				+ "parameter AS (\n"
				+ " SELECT CAST (? AS date) AS start_date,\n"
				+ "        CAST (? AS date) AS end_date,\n"
				+ "        CAST (? AS int) AS route_id\n"
				+ "),\n"
				+ SQL.addLatestPriceStmt() + ",\n"
				+ SQL.addLatestRouteStmt() + ",\n"
				+ SQL.addBookedQtyStmt() + ",\n"
				+ SQL.addSoldQtyStmt() + ",\n"
				+ SQL.addReceivedQtyStmt() + ",\n"
				+ SQL.addKeptQtyStmt() + ",\n"
				+ "combined AS (SELECT im.id,\n"
				+ "	       			   im.name,\n"
				+ "	       			   CASE WHEN beginning.qty IS NULL\n"
				+ "	          				THEN 0 ELSE beginning.qty END AS beginning_qty,\n"
				+ "	       			   CASE WHEN sold.qty IS NULL\n"
				+ "	          				THEN 0 ELSE sold.qty END AS sold_qty,\n"
				+ "	       			   CASE WHEN ending.qty IS NULL\n"
				+ "	          		   		THEN 0 ELSE ending.qty END AS ending_qty,\n"
				+ "	       			   CASE WHEN kept.qty IS NULL\n"
				+ "	          		   		THEN 0 ELSE kept.qty END AS kept_qty\n"
				+ "	  			  FROM item_master AS im\n"
				+ "	        		   LEFT JOIN salesd AS beginning\n"
				+ "	          			 	ON im.id = beginning.item_id\n"
				+ "	       			   LEFT JOIN sold\n"
				+ "	          				ON im.id = sold.item_id\n"
				+ "	       			   LEFT JOIN receivingd AS ending\n"
				+ "	          				ON im.id = ending.item_id\n"
				+ "	       			   LEFT JOIN countd AS kept\n"
				+ "	          				ON im.id = kept.item_id ),\n"
				+ "computed AS (SELECT id,\n"
				+ "	       			   name,\n"
				+ "	       			   beginning_qty,\n"
				+ "	       			   sold_qty,\n"
				+ "	       			   ending_qty,\n"
				+ "	       			   kept_qty,\n"
				+ "	       			   sold_qty + ending_qty + kept_qty - beginning_qty AS variance\n"
				+ "	  			  FROM combined)\n";
		//data = sql.getDataArray(new Object[] { startDate, endDate, routeId }, ""
		String string = ""
				+ stmt 
				+ "SELECT row_number() over(ORDER BY variance),\n"
				+ "       id,\n"
				+ "       name,\n"
				+ "       beginning_qty,\n"
				+ "	      sold_qty,\n"
				+ "	      ending_qty,\n"
				+ "	      kept_qty,\n"
				+ "	      variance,\n"
				+ "       variance * price AS value\n"
				+ "  FROM computed\n"
				+ "       INNER JOIN latest_price\n"
				+ "          ON computed.id = latest_price.item_id\n"
				+ " WHERE variance <> 0\n"
				+ "ORDER BY variance\n"
				;
		data = new Data().getDataArray(new Object[] { startDate, endDate, routeId }, string);
		// @sql:off
	}

	public BigDecimal getTotalVariance() {
		// @sql:on
		Object variance  = sql.getDatum(new Object[] { startDate, endDate, routeId }, ""
				+ stmt
				+ "SELECT sum (variance * price) AS value "
				+ "  FROM computed INNER JOIN latest_price ON computed.id = latest_price.item_id "
				+ " WHERE variance <> 0 "
				);
		// @sql:off
		return variance == null ? BigDecimal.ZERO : (BigDecimal) variance;
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene", "ayin", "192.168.1.100");
//		Database.getInstance().getConnection("irene","ayin","localhost");
		Calendar cal = Calendar.getInstance();
		cal.set(2013, Calendar.AUGUST, 8);
		Date first = new Date(cal.getTimeInMillis());
		Date last = first;
		LoadedMaterialBalance smb = new LoadedMaterialBalance(new Date[] {
		        first, last }, 1);
		Object[][] smbData = smb.getData();
		if (smbData != null) {
			for (Object[] os : smbData) {
				for (Object o : os) {
					System.out.print(o + ", ");
				}
				System.out.println();
			}
		} else {
			System.err.println("No data");
		}
		Database.getInstance().closeConnection();
	}
}
