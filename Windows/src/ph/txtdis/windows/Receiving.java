package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

public class Receiving extends Order {
	private ArrayList<String> qualityStates;
	private ArrayList<Date> expiries;
	private HashMap<Integer, BigDecimal> itemIdsAndQtys;
	private int refId;

	public Receiving(int id) {
		this.id = id;
		module = "Receiving Report";
		headers = new String[][] {
		        {
		                StringUtils.center("#", 2), "Integer" }, {
		                StringUtils.center("ID", 4), "Integer" }, {
		                StringUtils.center("PRODUCT NAME", 40), "String" }, {
		                StringUtils.center("UOM", 5), "String" }, {
		                StringUtils.center("QUALITY", 7), "String" }, {
		                StringUtils.center("EXPIRY", 10), "Date" }, {
		                StringUtils.center("QTY", 7), "BigDecimal" } };
		if (id != 0) {
			// @sql:on
			objects = new Data().getData(id, "" 
					+ "SELECT receiving_date, " 
					+ "		  partner_id, " 
					+ " 	  ref_id "
			        + "  FROM receiving_header " 
					+ " WHERE receiving_id = ? ");
			// @sql:off
			if (objects != null) {
				postDate = (Date) objects[0];
				partnerId = (int) objects[1];
				refId = objects[2] == null ? 0 : (int) objects[2];
				// @sql:on
				data = new Data().getDataArray(id, "" 
						+ "SELECT rd.line_id, " 
						+ "       rd.item_id, " 
						+ "		  im.name, "
						+ "		  u.unit, " 
						+ "		  q.name, "
						+ "		  CASE WHEN rd.expiry IS NULL "
						+ "         THEN '9999-12-31' ELSE rd.expiry END AS expiry, " 
						+ "		  rd.qty "
						+ "  FROM receiving_detail AS rd, " 
						+ "		  item_master AS im, " 
						+ "		  uom AS u, " 
						+ "       quality AS q "
						+ " WHERE 	  rd.item_id = im.id " 
						+ "		  AND rd.uom = u.id " 
						+ "		  AND rd.qc_id = q.id "
						+ "		  AND rd.receiving_id = ? " 
						+ " ORDER BY line_id ");
				// @sql:off
			}
		}
	}

	public int getRefId() {
		return refId;
	}

	public void setRefId(int refId) {
		this.refId = refId;
	}

	public ArrayList<String> getQualityStates() {
		if (qualityStates == null)
			qualityStates = new ArrayList<>();
		return qualityStates;
	}

	public ArrayList<Date> getExpiries() {
		if (expiries == null)
			expiries = new ArrayList<>();
		return expiries;
	}

	public HashMap<Integer, BigDecimal> getItemIdsAndQtys() {
		if (itemIdsAndQtys == null)
			itemIdsAndQtys = new HashMap<>();
		return itemIdsAndQtys;
	}
}
