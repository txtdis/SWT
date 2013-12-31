package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;

public class Receiving extends Order {
	private ArrayList<String> qualityStates;
	private ArrayList<Date> expiries;
	private Date expiry;
	private String qualityState;
	private HashMap<Integer, BigDecimal> itemIdsAndQtysOnList;
	
	protected int locationId;
	protected String[] locations;

	public Receiving() {
		super();
		isAnRR = true;
		headers = new String[][] {
		        {
		                StringUtils.center("#", 3), "Integer" }, {
		                StringUtils.center("ID", 4), "Integer" }, {
		                StringUtils.center("PRODUCT NAME", 40), "String" }, {
		                StringUtils.center("UOM", 5), "String" }, {
		                StringUtils.center("QUALITY", 7), "String" }, {
		                StringUtils.center("EXPIRY", 10), "Date" }, {
		                StringUtils.center("QUANTITY", 10), "BigDecimal" } };
    }

	public Receiving(int id) {
		this();
		this.id = id;
		module = "Receiving Report";
		type = "receiving";
		
		if (id != 0) {
			// @sql:on
			objects = sql.getData(id, "" 
					+ "SELECT receiving_date, " 
					+ "		  partner_id, " 
					+ " 	  ref_id,"
					+ "       user_id, "
					+ "       time_stamp\n"
			        + "  FROM receiving_header " 
					+ " WHERE receiving_id = ? ");
			// @sql:off
			if (objects != null) {
				date = (Date) objects[0];
				setPartnerId((int) objects[1]);
				referenceId = objects[2] == null ? 0 : (int) objects[2];
				inputter = ((String) objects[3]).toUpperCase();
				timestamp = ((Timestamp) objects[4]).getTime();
				inputDate = new Date(timestamp);
				inputTime = new Time(timestamp);
				// @sql:on
				data = sql.getDataArray(id, "" 
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
		} else {
			locations = new Location().getNames();
		}
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

	public Date getExpiry() {
		return expiry;
	}

	public void setExpiry(Date expiry) {
		this.expiry = expiry;
	}

	public HashMap<Integer, BigDecimal> getItemIdsAndQtysOnList() {
		if (itemIdsAndQtysOnList == null)
			itemIdsAndQtysOnList = new HashMap<>();
		return itemIdsAndQtysOnList;
	}

	public int getLocationId() {
		return locationId;
	}

	public void setLocationId(int locationId) {
		this.locationId = locationId;
	}

	public String[] getLocations() {
		if(locations == null)
			locations = new Location().getNames();
		return locations;
	}

	public String getQualityState() {
		return qualityState;
	}

	public void setQualityState(String qualityState) {
		this.qualityState = qualityState;
	}
}
