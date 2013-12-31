package ph.txtdis.windows;

import java.sql.Date;

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
				{StringUtils.center("QC", 4), "String"},
				{StringUtils.center("BEGIN", 8), "Quantity"},
				{StringUtils.center("IN", 8), "Quantity"},
				{StringUtils.center("OUT", 8), "Quantity"},
				{StringUtils.center("END", 8), "Quantity"},
				{StringUtils.center("GAIN/(LOSS)", 10), "Quantity"},
				{StringUtils.center("ADJUSTMENT", 10), "Quantity"}
		};
		data = new Data().getDataArray(dates, "" +
				"WITH dates\n" +
				"     AS (SELECT cast (? AS date) AS start,\n" +
				"                cast (? AS date) AS end),\n" +
				"     beginning\n" +
				"     AS (  SELECT cd.item_id, cd.qc_id, sum (cd.qty * qp.qty) AS qty\n" +
				"             FROM count_header AS ch\n" +
				"                  INNER JOIN count_detail AS cd " +
				"					  ON ch.count_id = cd.count_id\n" +
				"                  INNER JOIN qty_per AS qp\n" +
				"                     ON cd.uom = qp.uom AND cd.item_id = qp.item_id\n" +
				"                  INNER JOIN dates " +
				"					  ON ch.count_date = dates.start\n" +
				"                  INNER JOIN item_master AS im\n" +
				"                     ON cd.item_id = im.id\n" +
				"         GROUP BY cd.item_id, cd.qc_id),\n" +
				"     brought_in\n" +
				"     AS (  SELECT rd.item_id, rd.qc_id, sum (rd.qty * qp.qty) AS qty\n" +
				"             FROM receiving_header AS rh\n" +
				"                  INNER JOIN receiving_detail AS rd " +
				"					  ON rh.receiving_id = rd.receiving_id\n" +
				"                  INNER JOIN qty_per AS qp\n" +
				"                     ON rd.uom = qp.uom AND rd.item_id = qp.item_id\n" +
				"                  INNER JOIN dates\n" +
				"                     ON     rh.receiving_date > dates.start\n" +
				"                        AND rh.receiving_date <= dates.end\n" +
				"         GROUP BY rd.item_id, rd.qc_id),\n" +
				"     sent_out\n" +
				"     AS (  SELECT sd.item_id,\n" +
				"                  CASE WHEN cm.name LIKE '%DISPOSAL%' THEN 2 ELSE 0 END AS qc_id,\n" +
				"                  sum (sd.qty * qp.qty) AS qty\n" +
				"             FROM sales_header AS sh\n" +
				"                  INNER JOIN sales_detail AS sd\n" +
				"                     ON sh.sales_id = sd.sales_id " +
				"                  INNER JOIN qty_per AS qp\n" +
				"                     ON sd.uom = qp.uom AND sd.item_id = qp.item_id\n" +
				"                  INNER JOIN dates\n" +
				"                     ON     sh.sales_date > dates.start\n" +
				"                        AND sh.sales_date <= dates.end\n" +
				"                  INNER JOIN customer_master AS cm\n" +
				"                     ON sh.customer_id = cm.id\n" +
				"         GROUP BY sd.item_id, qc_id),\n" +
				"     ending\n" +
				"     AS (  SELECT cd.item_id, cd.qc_id, sum (cd.qty * qp.qty) AS qty\n" +
				"             FROM count_header AS ch\n" +
				"                  INNER JOIN count_detail AS cd " +
				"					  ON ch.count_id = cd.count_id\n" +
				"                  INNER JOIN qty_per AS qp\n" +
				"                     ON cd.uom = qp.uom AND cd.item_id = qp.item_id\n" +
				"                  INNER JOIN dates ON ch.count_date = dates.end\n" +
				"         GROUP BY cd.item_id, cd.qc_id),\n" +
				"     good\n" +
				"     AS (SELECT im.id,\n" +
				"                im.name,\n" +
				"				 0 AS qc_id,\n" + 
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
				"                LEFT JOIN beginning " +
				"					ON     im.id = beginning.item_id\n" +
				"					   AND beginning.qc_id = 0\n" +
				"                LEFT JOIN brought_in " +
				"					ON     im.id = brought_in.item_id\n" +
				"					   AND brought_in.qc_id = 0\n" +
				"                LEFT JOIN sent_out " +
				"					ON     im.id = sent_out.item_id\n" +
				"					   AND sent_out.qc_id = 0\n" +
				"                LEFT JOIN ending " +
				"                   ON     im.id = ending.item_id\n" +
				"					   AND ending.qc_id = 0\n" +
				"          WHERE    beginning.qty IS NOT NULL\n" +
				"                OR brought_in.qty IS NOT NULL\n" +
				"                OR sent_out.qty IS NOT NULL\n" +
				"                OR ending.qty IS NOT NULL),\n" +
				"     bad\n" +
				"     AS (SELECT im.id,\n" +
				"                im.name,\n" +
				"				 2 AS qc_id,\n" + 
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
				"                LEFT JOIN beginning " +
				"					ON     im.id = beginning.item_id\n" +
				"					   AND beginning.qc_id = 2\n" +
				"                LEFT JOIN brought_in " +
				"					ON     im.id = brought_in.item_id\n" +
				"					   AND brought_in.qc_id = 2\n" +
				"                LEFT JOIN sent_out " +
				"					ON     im.id = sent_out.item_id\n" +
				"					   AND sent_out.qc_id = 2\n" +
				"                LEFT JOIN ending " +
				"                   ON     im.id = ending.item_id\n" +
				"					   AND ending.qc_id = 2\n" +
				"          WHERE    beginning.qty IS NOT NULL\n" +
				"                OR brought_in.qty IS NOT NULL\n" +
				"                OR sent_out.qty IS NOT NULL\n" +
				"                OR ending.qty IS NOT NULL),\n" +
				"     summary\n" +
				"     AS (SELECT * FROM good\n" +
				"         UNION\n" +
				"         SELECT * FROM bad)\n" +
				"  SELECT ROW_NUMBER() OVER(ORDER BY s.id, s.qc_id),\n" +
				"		  s.id,\n" +
				"         s.name,\n" +
				"         q.name,\n" +
				"         beginning,\n" +
				"         brought_in,\n" +
				"         sent_out,\n" +
				"         ending,\n" +
				"         ending - beginning - brought_in + sent_out AS variance," +
				"		  0.0\n" +
				"    FROM summary AS s\n" +
				"         INNER JOIN quality AS q\n" +
				"			 ON s.qc_id = q.id\n" +
				"   WHERE ending - beginning - brought_in + sent_out <> 0\n" +
				"ORDER BY s.id, s.qc_id\n" +
				"");
	}

	public Date[] getDates() {
		return dates;
	}
}
