package ph.txtdis.windows;

import java.sql.Date;

import org.apache.commons.lang3.StringUtils;

public class CountVariance extends CountData {

	public CountVariance(Date[] dates, int bizUnit) {
		this.dates = dates;
		type = Type.COUNT_VARIANCE;

		tableHeaders = new String[][] { 
				{ StringUtils.center("#", 3), "Line" }, 
				{ StringUtils.center("ID", 4), "ID" },
		        { StringUtils.center("NAME", 18), "String" }, 
		        { StringUtils.center("QC", 4), "String" },
		        { StringUtils.center("START", 7), "Quantity" }, 
		        { StringUtils.center("IN", 7), "Quantity" },
		        { StringUtils.center("OUT", 7), "Quantity" }, 
		        { StringUtils.center("COUNT", 7), "Quantity" },
		        { StringUtils.center("ADJUST", 7), "Quantity" }, 
		        { StringUtils.center("END", 7), "Quantity" },
		        { StringUtils.center("GAIN/(LOSS)", 10), "Quantity" },
		        { StringUtils.center("JUSTIFICATION", 30), "String" } };
		
		tableData = new Query().getTableData(new Object[] { bizUnit, dates[0], dates[1] },""
				// @sql:on
				+ Item.addParentChildCTE() + ",\n"
				+ "	 biz_unit_item AS (SELECT DISTINCT child_id FROM parent_child WHERE parent_id = ?),\n" 
				+ "  dates AS (SELECT cast (? AS date) AS start, cast (? AS date) AS end),\n" 
				+ "	 beginning AS\n" 
				+ "		 (	SELECT max(ch.location_id) AS loc_id, cd.item_id, cd.qc_id, sum (cd.qty * qp.qty) AS qty\n" 
				+ "			  FROM count_header AS ch\n" 
				+ "				   INNER JOIN count_detail AS cd ON ch.count_id = cd.count_id\n" 
				+ "				   INNER JOIN qty_per AS qp ON cd.uom = qp.uom AND cd.item_id = qp.item_id\n" 
				+ "				   INNER JOIN dates ON ch.count_date = dates.start\n" 
				+ "		  GROUP BY cd.item_id, cd.qc_id),\n" 
				+ "	 last_adjust AS\n" 
				+ "		 (  SELECT ca.item_id, ca.qc_id, ca.qty, ca.reason\n" 
				+ "			FROM count_adjustment AS ca\n" 
				+ "				 INNER JOIN dates ON ca.count_date = dates.start\n" 
				+ "				 INNER JOIN item_header AS im ON ca.item_id = im.id),\n" 
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
				+ "				   INNER JOIN customer_header AS cm ON sh.customer_id = cm.id\n" 
				+ "		  GROUP BY sd.item_id, qc_id),\n" 
				+ "	 new_adjust AS\n" 
				+ "		 (  SELECT ca.item_id, ca.qc_id, ca.qty, ca.reason\n" 
				+ "			FROM count_adjustment AS ca\n" 
				+ "				 INNER JOIN dates ON ca.count_date = dates.end\n" 
				+ "				 INNER JOIN item_header AS im ON ca.item_id = im.id),\n" 
				+ "	 ending AS\n" 
				+ "		 (	SELECT max(ch.location_id) AS loc_id, cd.item_id, cd.qc_id, sum (cd.qty * qp.qty) AS qty\n" 
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
				+ "				 CASE WHEN last_adjust.qty IS NULL THEN 0 ELSE last_adjust.qty END AS last_adjust,\n" 
				+ "				 CASE WHEN brought_in.qty IS NULL THEN 0 ELSE brought_in.qty END AS brought_in,\n" 
				+ "				 CASE WHEN sent_out.qty IS NULL THEN 0 ELSE sent_out.qty END AS sent_out,\n" 
				+ "				 CASE WHEN new_adjust.qty IS NULL THEN 0 ELSE new_adjust.qty END AS new_adjust,\n" 
				+ "				 CASE WHEN ending.qty IS NULL THEN 0 ELSE ending.qty END AS ending\n" 
				+ "			FROM item_header AS im\n" 
				+ "				 LEFT JOIN beginning ON im.id = beginning.item_id AND beginning.qc_id = 0\n" 
				+ "				 LEFT JOIN last_adjust ON im.id = last_adjust.item_id AND last_adjust.qc_id = 0\n" 
				+ "				 LEFT JOIN brought_in ON im.id = brought_in.item_id AND brought_in.qc_id = 0\n" 
				+ "				 LEFT JOIN sent_out ON im.id = sent_out.item_id AND sent_out.qc_id = 0\n" 
				+ "				 LEFT JOIN new_adjust ON im.id = new_adjust.item_id AND new_adjust.qc_id = 0\n" 
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
				+ "				 CASE WHEN last_adjust.qty IS NULL THEN 0 ELSE last_adjust.qty END AS last_adjust,\n" 
				+ "				 CASE WHEN brought_in.qty IS NULL THEN 0 ELSE brought_in.qty END AS brought_in,\n" 
				+ "				 CASE WHEN sent_out.qty IS NULL THEN 0 ELSE sent_out.qty END AS sent_out,\n" 
				+ "				 CASE WHEN new_adjust.qty IS NULL THEN 0 ELSE new_adjust.qty END AS new_adjust,\n" 
				+ "				 CASE WHEN ending.qty IS NULL THEN 0 ELSE ending.qty END AS ending\n" 
				+ "			FROM item_header AS im\n" 
				+ "				 LEFT JOIN beginning ON im.id = beginning.item_id AND beginning.qc_id = 2\n" 
				+ "				 LEFT JOIN last_adjust ON im.id = last_adjust.item_id AND last_adjust.qc_id = 2\n" 
				+ "				 LEFT JOIN brought_in ON im.id = brought_in.item_id AND brought_in.qc_id = 2\n" 
				+ "				 LEFT JOIN sent_out ON im.id = sent_out.item_id AND sent_out.qc_id = 2\n" 
				+ "				 LEFT JOIN new_adjust ON im.id = new_adjust.item_id AND new_adjust.qc_id = 2\n" 
				+ "				 LEFT JOIN ending ON im.id = ending.item_id AND ending.qc_id = 2\n" 
				+ "		   WHERE	beginning.qty IS NOT NULL\n" 
				+ "				 OR brought_in.qty IS NOT NULL\n" 
				+ "				 OR sent_out.qty IS NOT NULL\n" 
				+ "				 OR ending.qty IS NOT NULL),\n" 
				+ "	 combined AS\n" 
				+ "		 (SELECT * FROM good\n" 
				+ "		  UNION\n" 
				+ "		  SELECT * FROM bad),\n" 
				+ "	 summary AS\n" 
				+ "  	 (SELECT id,\n" 
				+ "		 		 short_id,\n" 
				+ "		 		 qc_id,\n" 
				+ "		 		 beginning + last_adjust AS beginning,\n" 
				+ "		 		 brought_in,\n" 
				+ "		 		 sent_out,\n" 
				+ "		 		 ending,\n" 
				+ "		 		 new_adjust,\n" 
				+ "		 		 ending - beginning - brought_in + sent_out + new_adjust AS variance\n" 
				+ "			FROM combined)\n"
				+  "  SELECT CAST(row_number () OVER (ORDER BY s.id, name DESC) AS int),\n" 
				+ " 	    s.id,\n" 
				+ "		    short_id,\n" 
				+ "		    name,\n" 
				+ "		    beginning,\n" 
				+ "		    brought_in,\n" 
				+ "		    sent_out,\n" 
				+ "		    ending,\n" 
				+ "		    new_adjust,\n" 
				+ "		    ending + new_adjust AS final,\n" 
				+ "		    variance,\n" 
				+ "		    ' '\n" 
				+ "	   FROM summary AS s\n"
				+ "         INNER JOIN quality AS q ON s.qc_id = q.id\n" 
				+ "         INNER JOIN biz_unit_item ON child_id = s.id\n"
				+ "ORDER BY s.id, name DESC;\n");
		// @sql:off
	}

	private String addAdjustmentSQL() {
		// @sql:on
		return    ", cased AS\n" 
				+ "      (SELECT s.id,\n" 
				+ "				 short_id,\n" 
				+ "				 name,\n" 
				+ "		 		 beginning,\n" 
				+ "		 		 brought_in,\n" 
				+ "		 		 sent_out,\n" 
				+ "		 		 ending,\n" 
				+ "		 		 CASE WHEN child_id IS NULL\n"
				+ "					THEN CASE WHEN variance > 0 THEN 0.0 ELSE -variance END\n "
				+ "					ELSE 0.0 END AS adjust,\n" 
				+ "		 		 CASE WHEN child_id IS NULL\n"
				+ "					THEN CASE WHEN variance > 0 THEN variance ELSE -variance END\n "
				+ "					ELSE ending END AS final,\n" 
				+ "		 		 CASE WHEN child_id IS NULL THEN 0 ELSE variance END AS variance,\n" 
				+ "		 		 CASE WHEN child_id IS NULL THEN 'NOT COUNTED' ELSE NULL END AS reason\n" 
				+ "			FROM summary AS s\n"
				+ "              INNER JOIN quality AS q ON s.qc_id = q.id\n" 
				+ "              LEFT JOIN biz_unit_item ON child_id = s.id\n"
				+ "         	 LEFT JOIN new_adjust AS a\n"
				+ "               ON      s.id = a.item_id\n"
				+ "					  AND s.qc_id = a.qc_id\n"
				+ "        WHERE variance <> 0 AND a.reason IS NULL) \n" 
				+ "  SELECT CAST(row_number () OVER (ORDER BY reason DESC, variance, id, name DESC) AS int), * FROM cased\n" 
				+ "ORDER BY reason DESC, variance, id, name DESC;\n";
		// @sql:off
	}

	@Override
	public String getSubheading() {
		return "Variance of System Inventory vs. Stock Take\n" + DIS.LONG_DATE.format(dates[0]) + " and "
		        + DIS.LONG_DATE.format(dates[1]);
	}
}
