package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

public class StockTakeVariance extends Report {
	private Date[] dates;

	public StockTakeVariance(Date[] dates) {
		this.dates = dates;

		module = "Stock Take ";
		headers = new String[][] {
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("ID", 4), "ID"},
				{StringUtils.center("NAME", 40), "String"},
				{StringUtils.center(DIS.SDF.format(dates[0]), 8), "Quantity"},
				{StringUtils.center("IN", 8), "Quantity"},
				{StringUtils.center("OUT", 8), "Quantity"},
				{StringUtils.center("ACTUAL", 8), "Quantity"},
				{StringUtils.center("UNDR/(OVR)", 10), "Quantity"},
				{StringUtils.center("ADJUSTMENT", 10), "Quantity"},
				{StringUtils.center("APPROVER", 10), "String"},
				{StringUtils.center("DATE", 10), "Date"}
		};
		data = new SQL().getDataArray(dates, "" +
				"WITH dates\n" +
				"     AS (SELECT cast (? AS date) AS start,\n" +
				"                cast (? AS date) AS end),\n" +
				"     beginning\n" +
				"     AS (  SELECT id.item_id, sum (id.qty * qp.qty) AS qty\n" +
				"             FROM count_header AS ih\n" +
				"                  INNER JOIN count_detail AS id " +
				"					  ON ih.count_id = id.count_id\n" +
				"                  INNER JOIN qty_per AS qp\n" +
				"                     ON id.uom = qp.uom AND id.item_id = qp.item_id\n" +
				"                  INNER JOIN dates " +
				"					  ON ih.count_date = dates.start\n" +
				"                  INNER JOIN item_master AS im\n" +
				"                     ON id.item_id = im.id AND im.type_id <> 2\n" +
				"         GROUP BY id.item_id),\n" +
				"     brought_in\n" +
				"     AS (  SELECT id.item_id, sum (id.qty * qp.qty) AS qty\n" +
				"             FROM receiving_header AS ih\n" +
				"                  INNER JOIN receiving_detail AS id " +
				"					  ON ih.rr_id = id.rr_id\n" +
				"                  INNER JOIN qty_per AS qp\n" +
				"                     ON id.uom = qp.uom AND id.item_id = qp.item_id\n" +
				"                  INNER JOIN dates\n" +
				"                     ON ih.rr_date BETWEEN dates.start AND dates.end\n"+
				"            WHERE partner_id = 488 OR ref_id < 0 OR qc_id <> 0\n" +
				"         GROUP BY id.item_id),\n" +
				"     ending\n" +
				"     AS (  SELECT id.item_id, sum (id.qty * qp.qty) AS qty\n" +
				"             FROM count_header AS ih\n" +
				"                  INNER JOIN count_detail AS id " +
				"					  ON ih.count_id = id.count_id\n" +
				"                  INNER JOIN qty_per AS qp\n" +
				"                     ON id.uom = qp.uom AND id.item_id = qp.item_id\n" +
				"                  INNER JOIN dates ON ih.count_date = dates.end\n" +
				"                  INNER JOIN item_master AS im\n" +
				"                     ON id.item_id = im.id AND im.type_id <> 2\n" +
				"         GROUP BY id.item_id),\n" +
				"     sold_bundled\n" +
				"     AS (  SELECT bom.part_id AS item_id,\n" +
				"                  sum (id.qty * bom.qty * qp.qty) AS qty\n" +
				"             FROM invoice_header AS ih\n" +
				"                  INNER JOIN invoice_detail AS id\n" +
				"                     ON ih.invoice_id = id.invoice_id " +
				"						  AND ih.series = id.series\n" +
				"                  INNER JOIN dates\n" +
				"                     ON ih.invoice_date BETWEEN dates.start " +
				"						  AND dates.end\n" +
				"                  INNER JOIN bom ON id.item_id = bom.item_id\n" +
				"                  INNER JOIN qty_per AS qp\n" +
				"                     ON bom.uom = qp.uom " +
				"						  AND bom.part_id = qp.item_id\n" +
				"         GROUP BY bom.part_id),\n" +
				"     sold_as_is\n" +
				"     AS (  SELECT id.item_id, sum (id.qty * qp.qty) AS qty\n" +
				"             FROM invoice_header AS ih\n" +
				"                  INNER JOIN invoice_detail AS id\n" +
				"                     ON ih.invoice_id = id.invoice_id " +
				"						  AND ih.series = id.series\n" +
				"                  INNER JOIN qty_per AS qp\n" +
				"                     ON id.uom = qp.uom AND id.item_id = qp.item_id\n" +
				"                  INNER JOIN dates\n" +
				"                     ON ih.invoice_date " +
				"						  BETWEEN dates.start AND dates.end\n" +
				"                  INNER JOIN item_master AS im\n" +
				"                     ON id.item_id = im.id AND im.type_id <> 2\n" +
				"         GROUP BY id.item_id),\n" +
				"     sold_combined\n" +
				"     AS (SELECT * FROM sold_bundled\n" +
				"         UNION\n" +
				"         SELECT * FROM sold_as_is),\n" +
				"     sold\n" +
				"     AS (  SELECT item_id, sum (qty) AS qty\n" +
				"             FROM sold_combined\n" +
				"         GROUP BY item_id),\n" +
				"     delivered\n" +
				"     AS (  SELECT id.item_id, sum (id.qty * qp.qty) AS qty\n" +
				"             FROM delivery_header AS ih\n" +
				"                  INNER JOIN delivery_detail AS id\n" +
				"                     ON ih.delivery_id = id.delivery_id\n" +
				"                  INNER JOIN qty_per AS qp\n" +
				"                     ON id.uom = qp.uom AND id.item_id = qp.item_id\n" +
				"                  INNER JOIN dates\n" +
				"                     ON ih.delivery_date " +
				"						  BETWEEN dates.start AND dates.end\n" +
				"         GROUP BY id.item_id),\n" +
				"	  sent_out_combined\n" +
				"     AS (SELECT * FROM sold\n" +
				"         UNION\n" +
				"         SELECT * FROM delivered),\n" +
				"     sent_out\n" +
				"     AS (  SELECT item_id, sum (qty) AS qty\n" +
				"             FROM sent_out_combined\n" +
				"         GROUP BY item_id),\n" +
				"     summary\n" +
				"     AS (SELECT im.id,\n" +
				"                im.name,\n" +
				"                CASE WHEN beginning.qty IS NULL THEN 0 " +
				"					ELSE beginning.qty END\n" +
				"                   AS beginning,\n" +
				"                CASE\n" +
				"                   WHEN brought_in.qty IS NULL THEN 0\n" +
				"                   ELSE brought_in.qty\n" +
				"                END\n" +
				"                   AS brought_in,\n" +
				"                CASE WHEN sent_out.qty IS NULL THEN 0 " +
				"					ELSE sent_out.qty END\n" +
				"                   AS sent_out,\n" +
				"                CASE WHEN ending.qty IS NULL THEN 0 " +
				"					ELSE ending.qty END\n" +
				"                   AS ending\n" +
				"           FROM item_master AS im\n" +
				"                LEFT OUTER JOIN beginning " +
				"					ON im.id = beginning.item_id\n" +
				"                LEFT OUTER JOIN brought_in " +
				"					ON im.id = brought_in.item_id\n" +
				"                LEFT OUTER JOIN sent_out " +
				"					ON im.id = sent_out.item_id\n" +
				"                LEFT OUTER JOIN ending ON im.id = ending.item_id\n" +
				"          WHERE    beginning.qty IS NOT NULL\n" +
				"                OR brought_in.qty IS NOT NULL\n" +
				"                OR sent_out.qty IS NOT NULL\n" +
				"                OR ending.qty IS NOT NULL)\n" +
				"  SELECT ROW_NUMBER() OVER(ORDER BY " +
				"			(beginning + brought_in - sent_out - ending) DESC),\n" +
				"		  id,\n" +
				"         name,\n" +
				"         beginning,\n" +
				"         brought_in,\n" +
				"         sent_out,\n" +
				"         ending,\n" +
				"         beginning + brought_in - sent_out - ending AS variance," +
				"		  0.0,\n" +
				"		  ''," +
				"		  current_date " +
				"    FROM summary\n" +
				"   WHERE beginning + brought_in - sent_out - ending <> 0\n" +
				"ORDER BY variance DESC\n" +
				"");
	}

	public Date[] getDates() {
		return dates;
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		Calendar first = Calendar.getInstance();
		Calendar last = Calendar.getInstance();
		first.set(2013, Calendar.MAY, 4);
		last.set(2013, Calendar.MAY, 11);
		Date start = new Date(first.getTimeInMillis());
		Date end = new Date(last.getTimeInMillis());
		StockTakeVariance i = new StockTakeVariance(new Date[] {start, end});
		for (Object[] os : i.getData()) {
			for (Object o : os) {
				System.out.print(o + ", ");
			}
			System.out.println();
		}
		Database.getInstance().closeConnection();
	}
}
