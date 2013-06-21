package ph.txtdis.windows;

import java.sql.Date;

public class ReceivingHelper {

	public ReceivingHelper() {
	}

	public boolean hasId(int rrId) {
		Object o = new SQL().getDatum(rrId, "" + 
				"SELECT rr_id  " +
				"FROM 	receiving_header " +
				"WHERE 	rr_id = ? " 
				);
		return (o == null ? false : true);
	}

	public boolean hasOpenPO(Date receiveDate, int vendorId) {
		Object o = true;
		if (new CustomerHelper().isVendor(vendorId))
			o = new SQL().getDatum(new Object[] {receiveDate, vendorId}, "" +
					"SELECT purchase_id\n" +
					"  FROM purchase_header AS ph\n" +
					"INNER JOIN vendor_specific AS vs\n" +
					"        ON ph.customer_id = vs.vendor_id\n" +
					"WHERE 	purchase_date + lead_time = ?\n" +
					"   AND vs.vendor_id = ?" +
					"LIMIT 1;" 
					);
		return (o == null ? false : true);		
	}

	public Object[][] getReceivedReturnedMaterials(int soId) {
		Object[][] aao = new SQL().getDataArray(soId, "" + 
				"SELECT rd.item_id,  " +
				"		sum(rd.qty * qp.qty) " +
				"FROM 	receiving_detail AS rd " +
				"INNER JOIN receiving_header AS rh " +
				"	ON rd.rr_id = rh.rr_id " +
				"INNER JOIN qty_per AS qp " +
				"	ON rd.item_id = qp.item_id " +
				"	AND rd.uom = qp.uom " +
				(soId < 0 ? " WHERE ref_id = ? " :
					"INNER JOIN sales_header AS sh " +
					"	ON rh.ref_id = sh.sales_id " +
					"	AND sh.sales_id =  ? " +
					"INNER JOIN sales_detail AS sd " +
					"	ON rd.item_id = -sd.item_id " +
					"	AND sh.sales_id = sd.sales_id " 
						) +
						"GROUP BY rd.item_id " +
						"ORDER BY rd.item_id " +
				"");
		return aao;
	}
}
