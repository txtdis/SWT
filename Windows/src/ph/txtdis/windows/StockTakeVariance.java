package ph.txtdis.windows;

import java.sql.Date;

import org.apache.commons.lang3.StringUtils;

public class StockTakeVariance extends StockTake {

	public StockTakeVariance(Date[] dates) {
		this.dates = dates;
		
		module = "Stock Take Reconciliation";
		headers = new String[][] {
				{StringUtils.center("#", 3), "Line"},
				{StringUtils.center("ID", 4), "ID"},
				{StringUtils.center("NAME", 18), "String"},
				{StringUtils.center("QC", 4), "String"},
				{StringUtils.center("BEGINNING", 10), "Quantity"},
				{StringUtils.center("BROUGHT IN", 10), "Quantity"},
				{StringUtils.center("SENT OUT", 10), "Quantity"},
				{StringUtils.center("ENDING", 10), "Quantity"},
				{StringUtils.center("GAIN/(LOSS)", 10), "Quantity"},
				{StringUtils.center("ADJUSTMENT", 10), "Quantity"},
				{StringUtils.center("FINAL", 10), "Quantity"},
				{StringUtils.center("JUSTIFICATION", 13), "String"}
		};

		data = new Data().getDataArray(dates, ""
				// @sql:on
				+ "WITH dates AS (SELECT cast (? AS date) AS start, cast (? AS date) AS end),\n" 
				+ "	 beginning AS\n" 
				+ "		 (SELECT ca.item_id, ca.qc_id, ca.qty\n" 
				+ "			FROM count_adjustment AS ca\n" 
				+ "				 INNER JOIN dates ON ca.count_date = dates.start\n" 
				+ "				 INNER JOIN item_master AS im ON ca.item_id = im.id),\n" 
				+ "	 brought_in AS\n" 
				+ "		 (	SELECT rd.item_id, rd.qc_id, sum (rd.qty * qp.qty) AS qty\n" 
				+ "			  FROM receiving_header AS rh\n" 
				+ "				   INNER JOIN receiving_detail AS rd ON rh.receiving_id = rd.receiving_id\n" 
				+ "				   INNER JOIN qty_per AS qp ON rd.uom = qp.uom AND rd.item_id = qp.item_id\n" 
				+ "				   INNER JOIN dates\n" 
				+ "					   ON rh.receiving_date > dates.start AND rh.receiving_date <= dates.end\n" 
				+ "		  GROUP BY rd.item_id, rd.qc_id),\n" 
				+ "	 sent_out AS\n" 
				+ "		 (	SELECT sd.item_id,\n" 
				+ "				   CASE WHEN cm.name LIKE '%DISPOSAL%' THEN 2 ELSE 0 END AS qc_id,\n" 
				+ "				   sum (sd.qty * qp.qty) AS qty\n" 
				+ "			  FROM sales_header AS sh\n" 
				+ "				   INNER JOIN sales_detail AS sd ON sh.sales_id = sd.sales_id\n" 
				+ "				   INNER JOIN qty_per AS qp ON sd.uom = qp.uom AND sd.item_id = qp.item_id\n" 
				+ "				   INNER JOIN dates ON sh.sales_date > dates.start AND sh.sales_date <= dates.end\n" 
				+ "				   INNER JOIN customer_master AS cm ON sh.customer_id = cm.id\n" 
				+ "		  GROUP BY sd.item_id, qc_id),\n" 
				+ "	 ending AS\n" 
				+ "		 (	SELECT cd.item_id, cd.qc_id, sum (cd.qty * qp.qty) AS qty\n" 
				+ "			  FROM count_header AS ch\n" 
				+ "				   INNER JOIN count_detail AS cd ON ch.count_id = cd.count_id\n" 
				+ "				   INNER JOIN qty_per AS qp ON cd.uom = qp.uom AND cd.item_id = qp.item_id\n" 
				+ "				   INNER JOIN dates ON ch.count_date = dates.end\n" 
				+ "		  GROUP BY cd.item_id, cd.qc_id),\n" 
				+ "	 good AS\n" 
				+ "		 (SELECT im.id,\n" 
				+ "				 im.short_id,\n" 
				+ "				 0 AS qc_id,\n" 
				+ "				 CASE WHEN beginning.qty IS NULL THEN 0 ELSE beginning.qty END AS beginning,\n" 
				+ "				 CASE WHEN brought_in.qty IS NULL THEN 0 ELSE brought_in.qty END AS brought_in,\n" 
				+ "				 CASE WHEN sent_out.qty IS NULL THEN 0 ELSE sent_out.qty END AS sent_out,\n" 
				+ "				 CASE WHEN ending.qty IS NULL THEN 0 ELSE ending.qty END AS ending\n" 
				+ "			FROM item_master AS im\n" 
				+ "				 LEFT JOIN beginning ON im.id = beginning.item_id AND beginning.qc_id = 0\n" 
				+ "				 LEFT JOIN brought_in ON im.id = brought_in.item_id AND brought_in.qc_id = 0\n" 
				+ "				 LEFT JOIN sent_out ON im.id = sent_out.item_id AND sent_out.qc_id = 0\n" 
				+ "				 LEFT JOIN ending ON im.id = ending.item_id AND ending.qc_id = 0\n" 
				+ "		   WHERE	beginning.qty IS NOT NULL\n" 
				+ "				 OR brought_in.qty IS NOT NULL\n" 
				+ "				 OR sent_out.qty IS NOT NULL\n" 
				+ "				 OR ending.qty IS NOT NULL),\n" 
				+ "	 bad AS\n" 
				+ "		 (SELECT im.id,\n" 
				+ "				 im.short_id,\n" 
				+ "				 2 AS qc_id,\n" 
				+ "				 CASE WHEN beginning.qty IS NULL THEN 0 ELSE beginning.qty END AS beginning,\n" 
				+ "				 CASE WHEN brought_in.qty IS NULL THEN 0 ELSE brought_in.qty END AS brought_in,\n" 
				+ "				 CASE WHEN sent_out.qty IS NULL THEN 0 ELSE sent_out.qty END AS sent_out,\n" 
				+ "				 CASE WHEN ending.qty IS NULL THEN 0 ELSE ending.qty END AS ending\n" 
				+ "			FROM item_master AS im\n" 
				+ "				 LEFT JOIN beginning ON im.id = beginning.item_id AND beginning.qc_id = 2\n" 
				+ "				 LEFT JOIN brought_in ON im.id = brought_in.item_id AND brought_in.qc_id = 2\n" 
				+ "				 LEFT JOIN sent_out ON im.id = sent_out.item_id AND sent_out.qc_id = 2\n" 
				+ "				 LEFT JOIN ending ON im.id = ending.item_id AND ending.qc_id = 2\n" 
				+ "		   WHERE	beginning.qty IS NOT NULL\n" 
				+ "				 OR brought_in.qty IS NOT NULL\n" 
				+ "				 OR sent_out.qty IS NOT NULL\n" 
				+ "				 OR ending.qty IS NOT NULL),\n" 
				+ "	 summary AS\n" 
				+ "		 (SELECT * FROM good\n" 
				+ "		  UNION\n" 
				+ "		  SELECT * FROM bad)\n" 
				+ "  SELECT row_number () OVER (ORDER BY s.id, s.qc_id),\n" 
				+ "		 s.id,\n" 
				+ "		 s.short_id,\n" 
				+ "		 q.name,\n" 
				+ "		 beginning,\n" 
				+ "		 brought_in,\n" 
				+ "		 sent_out,\n" 
				+ "		 ending,\n" 
				+ "		 ending - beginning - brought_in + sent_out AS variance,\n" 
				+ "		 0.0 AS adjustment,\n" 
				+ "		 ending AS final,\n" 
				+ "		 ' ' AS justification\n" 
				+ "	FROM summary AS s INNER JOIN quality AS q ON s.qc_id = q.id\n" 
				+ "   WHERE ending - beginning - brought_in + sent_out <> 0\n" 
				+ "ORDER BY s.id, s.qc_id;\n" 
				// @sql:off
				);

	}

	public boolean isComplete(Date date) {
		Object o = new Data().getDatum(date, "" +
				"SELECT count_date " +
				"  FROM count_completion " +
				" WHERE	count_date = ? " +
				"");
		return (o == null ? false : true);
	}
}
