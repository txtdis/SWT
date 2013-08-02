package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

public class LoadedMaterialBalance extends Report {
	private Data sql;
	private Date[] dates;
	private Date startDate, endDate;
	private int routeId;
	private String stmt;

	public LoadedMaterialBalance(Date[] dates, int routeId) {
		module = "Loaded Material Balance";
		sql = new Data();
		if (dates == null) {
			startDate = DIS.TODAY;
			endDate = DIS.TODAY;
			dates = new Date[] {
			        startDate, endDate };
		}
		startDate = dates[0];
		endDate = dates[1];
		this.dates = dates;
		this.routeId = routeId;
		
		headers = new String[][] {
		        {
		                StringUtils.center("#", 2), "Line" }, {
		                StringUtils.center("ID", 4), "ID" }, {
		                StringUtils.center("PRODUCT NAME", 40), "String" }, {
		                StringUtils.center("LOADED", 10), "Quantity" }, {
		                StringUtils.center("SOLD", 10), "Quantity" }, {
		                StringUtils.center("RETURNED", 10), "Quantity" }, {
		                StringUtils.center("GAIN(LOSS)", 10), "Quantity" }, {
		                StringUtils.center(DIS.CURRENCY_SIGN + " VALUE", 14), "BigDecimal" } };

		// @sql:on
		stmt =	"WITH "
				+ "parameter AS ( "
				+ " SELECT CAST (? AS DATE) AS start_date, "
				+ "        CAST (? AS DATE) AS end_date, "
				+ "        ? AS route_id "
				+ "), "
				+ SQL.addLatestPriceStmt(true) + ", "
				+ SQL.addBookedQtyStmt(true) + ", "
				+ SQL.addSoldQtyStmt(true) + ", "
				+ SQL.addReceivedQtyStmt(true) + ", "
				+ "combined AS (SELECT im.id, "
				+ "	       			   im.name, "
				+ "	       			   CASE WHEN beginning.qty IS NULL "
				+ "	          				THEN 0 ELSE beginning.qty END AS beginning_qty, "
				+ "	       			   CASE WHEN sold.qty IS NULL "
				+ "	          				THEN 0 ELSE sold.qty END AS sold_qty, "
				+ "	       			   CASE WHEN ending.qty IS NULL "
				+ "	          		   		THEN 0 ELSE ending.qty END AS ending_qty "
				+ "	  			  FROM item_master AS im "
				+ "	        		   LEFT JOIN salesd AS beginning "
				+ "	          			 	ON im.id = beginning.item_id "
				+ "	       			   LEFT JOIN sold      "
				+ "	          				ON im.id = sold.item_id "
				+ "	       			   LEFT JOIN receivingd AS ending "
				+ "	          				ON im.id = ending.item_id ), "
				+ "computed AS (SELECT id, "
				+ "	       			   name, "
				+ "	       			   beginning_qty, "
				+ "	       			   sold_qty, "
				+ "	       			   ending_qty, "
				+ "	       			   sold_qty + ending_qty - beginning_qty AS variance "
				+ "	  			  FROM combined "
				+ "	 			 WHERE sold_qty + ending_qty + beginning_qty > 0 ) "
				;
		data = sql.getDataArray(new Object[] { startDate, endDate, routeId }, ""
				+ stmt 
				+ "SELECT row_number() over(ORDER BY variance), "
				+ "       id, "
				+ "       name, "
				+ "       beginning_qty, "
				+ "	      sold_qty, "
				+ "	      ending_qty, "
				+ "	      variance, "
				+ "       variance * price AS value "
				+ "  FROM computed "
				+ "       INNER JOIN latest_price "
				+ "          ON computed.id = latest_price.item_id "
				+ " WHERE variance <> 0 "
				+ "ORDER BY variance "
				);
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

	public Date[] getDates() {
		return dates;
	}

	public int getRouteId() {
		return routeId;
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene", "ayin");
		Calendar cal = Calendar.getInstance();
		cal.set(2013, Calendar.JULY, 1);
		Date first = new Date(cal.getTimeInMillis());
		Date last = first;
		LoadedMaterialBalance smb = new LoadedMaterialBalance(new Date[] {
		        first, last }, 2);
		Object[][] smbData = smb.getData();
		for (Object[] os : smbData) {
			for (Object o : os) {
				System.out.print(o + ", ");
			}
			System.out.println();
		}
		Database.getInstance().closeConnection();
	}

}
