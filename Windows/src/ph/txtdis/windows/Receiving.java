package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

public class Receiving extends Report {
	private int partnerId, rrId, refId;
	private Date date;
	private ArrayList<Integer> itemIds, uoms, qcs;
	private ArrayList<BigDecimal> qtys;
	private String[] qcStates, units;
	private Object[] os;
	private String name, address, type;

	public Receiving(int rrId) {
		this.rrId = rrId;
		module = "Receiving Report";
		itemIds = new ArrayList<>();
		uoms = new ArrayList<>();
		qtys = new ArrayList<>();
		qcs = new ArrayList<>();
		headers = new String[][] {
				{StringUtils.center("#", 2), "Integer"},
				{StringUtils.center("ID", 4), "Integer"},
				{StringUtils.center("PRODUCT NAME", 40), "String"},
				{StringUtils.center("UOM", 5), "String"},
				{StringUtils.center("QUALITY", 7), "String"},
				{StringUtils.center("EXPIRY", 10), "Date"},
				{StringUtils.center("QTY", 7), "BigDecimal"}
		};

		os = new SQL().getData("" +
				"SELECT	unit " +
				"FROM	uom ");
		units = Arrays.copyOf(os, os.length, String[].class);

		os = new SQL().getData("" +
				"SELECT	name " +
				"FROM	quality " +
				"ORDER BY id;");
		qcStates = Arrays.copyOf(os, os.length, String[].class);

		if (rrId == 0) {
			date = new DateAdder().plus(0);
		} else {
			os = new SQL().getData(rrId, "" +
					"SELECT	rr_date, " +
					"		partner_id, " +
					"		ref_id " +
					"FROM	receiving_header " +
					"WHERE	rr_id = ? "
					);
			date = (Date) os[0];
			partnerId = (int) os[1];
			refId = (int) os[2];
			data = new SQL().getDataArray(rrId, "" +
					"SELECT rd.line_id, " +
					"		rd.item_id, " +
					"		im.name, " +
					"		u.unit, " +
					"		q.name, " +
					"		CASE WHEN rd.expiry IS NULL THEN '9999-12-31' ELSE rd.expiry END AS expiry, " +
					"		rd.qty " +
					"FROM	receiving_detail AS rd, " +
					"		item_master AS im, " +
					"		uom AS u," +
					"		quality AS q " +
					"WHERE	rd.item_id = im.id " +
					"	AND	rd.uom = u.id " +
					"	AND rd.qc_id = q.id " +
					"	AND rd.rr_id = ? " +
					"ORDER BY line_id " +
					"");
		}
	}

	public int getRrId() {
		return rrId;
	}

	public void setRrId(int rrId) {
		this.rrId = rrId;
	}

	public int getPartnerId() {
		return partnerId;
	}

	public void setPartnerId(int partnerId) {
		this.partnerId = partnerId;
	}

	public int getRefId() {
		return refId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setRefId(int refId) {
		this.refId = refId;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public ArrayList<Integer> getItemIds() {
		return itemIds;
	}

	public ArrayList<Integer> getUoms() {
		return uoms;
	}

	public ArrayList<BigDecimal> getQtys() {
		return qtys;
	}

	public ArrayList<Integer> getQcs() {
		return qcs;
	}

	public String getName() {
		return name;
	}

	public String getAddress() {
		return address;
	}

	public String[] getQcStates() {
		return qcStates;
	}

	public String[] getUnits() {
		return units;
	}
}
