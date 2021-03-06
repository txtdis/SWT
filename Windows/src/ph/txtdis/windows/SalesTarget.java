package ph.txtdis.windows;

import java.sql.Date;
import java.util.ArrayList;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class SalesTarget extends OrderData {
	private int targetTypeId, size, categoryId;
	private ArrayList<Integer> outletIds;
	private ArrayList<Rebate> rebates;
	private ArrayList<Target> targets;
	private Date startDate, endDate;
	private Integer[] productLineIds;
	private Object[][] rebateData, additionalRebateData, targetData;
	private String category;
	private String[] productLines, categories, targetTypes;

	public SalesTarget(int targetId) {
		super();
		type = Type.SALES_TARGET;
		startDate = DIS.TOMORROW;
		endDate = DIS.addDays(startDate, 28);
		rebateData = new Object[1][3 + size];
		Object[][] rebateArray = new Object[][] {{0, 0, "AMOUNT IN PHP"}};
		if (targetId != 0) {
			Object[] ao = new Query().getList(targetId, "" +
					"SELECT	type_id, " +
					"		category_id, " +
					"		start_date," +
					"		end_date " +
					"FROM	target_header " +
					"WHERE	target_id = ? " +
					""
					);
			if(ao != null) {
				targetTypeId = (int) ao[0];
				categoryId	 = (int) ao[1];
				startDate 	 = (Date) ao[2];
				endDate 	 = (Date) ao[3];
				
				categories = new String[] {Item.getFamily(categoryId)};
				category = categories[0];
				targetTypes = new String[] {new Target(targetTypeId).getType()};				
			} else {
				id = 0;
				return;
			}
		} else {
			categoryId = -10;
			targetTypeId = 1;
			categories = Item.getFamilies(2);
			targetTypes = new Target().getTargets();
		}
		productLines = Item.getProductLines(categoryId);
		productLineIds = Item.getProductLineIds(categoryId);
		size = productLines.length;
		Query sql = new Query();
		Object[][] rebateValues = sql.getTableData(getRebateSelect());
		rebateData[0] = ArrayUtils.addAll(rebateArray[0], rebateValues[0]);
		setHeaders(productLines);
		if (targetId == 0) {
			targetData = new Object[1][3 + size];			
		} else {
			targetData = sql.getTableData(getTargetSelect());			
		}
	}

	public void setHeaders(String[] newProductLines) {
		int productLineSize = newProductLines.length;
		tableHeaders = new String[productLineSize + 3][];
		tableHeaders[0] = new String[]{StringUtils.center("#", 2), "Line"};
		tableHeaders[1] = new String[]{StringUtils.center("ID", 4), "ID"};
		tableHeaders[2] = new String[]{StringUtils.center("NAME", 29), "String"};
		for (int i = 0; i < productLineSize; i++) {
			tableHeaders[i + 3] = new String[]{StringUtils.center(newProductLines[i], 6), "BigDecimal"};
		}
	}

	public String getRebateSelect() {
		String cteString = "" +
				"p AS (\n" +
				"SELECT " + id + " AS target_id,\n" +
				"		1 AS value\n" +
				"), " ;
		String selString = "";
		String tblString = "" +
				"p LEFT OUTER JOIN p0\n" +
				"ON p.target_id = p0.target_id\n";
		for (int i = 0; i < size; i++) {
			cteString += "" +
					"p" + i + " AS (\n" + 
					"SELECT	target_id,\n" +
					"		value\n" +
					"FROM	target_rebate\n" +
					"WHERE	target_id = " + id + "\n" +
					"	AND	product_line_id = " + productLineIds[i] + "\n" +
					(i == size-1 ? ")\n" : "),\n") +
					"";
			selString += "" +
					"p" + i + ".value AS p" + i + "_value" +
					(i == size-1 ? " \n" : ", \n") +
					"";
			tblString += (i == size-1 ? " \n" : 
				"LEFT OUTER JOIN p" + (i + 1) + "\n" +
				"ON p.target_id = p" + (i + 1) +".target_id \n" +
					"");

		}
		return	"WITH " + cteString +
				"SELECT " + selString +
				"FROM " + tblString;
	}

	public String getTargetSelect() {
		String cteString = "" +
				"p AS (\n" +
				"SELECT DISTINCT\n" +
				"		t.target_id,\n" +
				"		t.outlet_id," +
				"		cm.name\n" +
				"FROM	target_outlet AS t\n" +
				"INNER JOIN customer_header AS cm\n" +
				"	ON t.outlet_id = cm.id\n" +
				"WHERE t.target_id = " + id + "\n" +
				"),\n" ;
		String selString = "";
		String tblString = "" +
				"p LEFT OUTER JOIN p0\n" +
				"ON p.target_id = p0.target_id\n" +
				"AND p.outlet_id = p0.outlet_id\n";
		for (int i = 0; i < size; i++) {
			cteString += "" +
					"p" + i + " AS (\n" + 
					"SELECT	target_id,\n" +
					"		outlet_id,\n" +
					"		qty\n" +
					"FROM	target_outlet\n" +
					"WHERE	target_id = " + id + "\n" +
					"	AND	product_line_id = " + productLineIds[i] + "\n" +
					(i == size-1 ? ")\n" : "),\n") +
					"";
			selString += "" +
					"p" + i + ".qty AS p" + i + "_qty" +
					(i == size-1 ? " \n" : ", \n") +
					"";
			tblString += (i == size-1 ? " \n" : 
				"LEFT OUTER JOIN p" + (i + 1) + "\n" +
				"ON p.target_id = p" + (i + 1) + ".target_id\n" +
				"AND p.outlet_id = p" + (i + 1) + ".outlet_id\n" +
					"");

		}
		return	"WITH " + cteString +
				"SELECT CAST (row_number() over() AS int),\n" +
				"		p.outlet_id,\n" +
				"		p.name,\n" +
				"	" + selString +
				"FROM " + tblString;
	}

	public Object[] getDatesThatThisFallsWithin(int categoryId, Date date) {
		return new Query().getList(new Object[] {categoryId, date}, ""
				+ "SELECT start_date,"
				+ "		  end_date " 
				+ "  FROM target_header " 
				+ " WHERE     category_id = ? "
				+ "       AND ? BETWEEN start_date AND end_date " 
				);
	}
	
	
	public Object[][] getIncentiveData(int customerId, Date date) {
		return new Query().getTableData(new Object[] {customerId, date}, ""
				// @sql:on
				+ Item.addParentChildCTE() + ",\n"
				+ "     latest_incentive " 
				+ "     AS (  SELECT outlet_id, "
				+ "                  max (end_date) AS end_date "
				+ "             FROM target_header AS thr "
				+ "                  INNER JOIN target_outlet AS tot "
				+ "                     ON thr.target_id = tot.target_id "
				+ "            WHERE outlet_id = ? AND end_date <= ? "
				+ "         GROUP BY outlet_id), "
				+ "     item_product_line "
				+ "     AS (SELECT child_id AS item_id, "
				+ "                parent_id AS product_line_id "
				+ "           FROM parent_child AS ipt), "
				+ "     main_branch "
				+ "     AS (  SELECT id AS branch, "
				+ "                  CASE WHEN branch_of IS NULL THEN id ELSE branch_of END AS main "
				+ "             FROM customer_header "
				+ "         ORDER BY id) "
				+ "  SELECT CAST (-row_number() over() AS line_id AS int), "
				+ "         tot.product_line_id, "
				+ "            rpad (itf.name, 8) "
				+ "         || ' - ' "
				+ "         || lpad (cast (tot.qty AS text), 7) "
				+ "         || ' @ P' "
				+ "         || lpad (cast (tre.value AS text), 5) "
				+ "         || '/' "
				+ "         || uom.unit "
				+ "            AS name, "
				+ "         uom.unit, "
				+ "         sum (idl.qty * unit.qty * report.qty) AS qty, "
				+ "         CASE "
				+ "            WHEN sum (idl.qty * unit.qty * report.qty) < tot.qty THEN 0 "
				+ "            ELSE -tre.value "
				+ "         END "
				+ "            AS value, "
				+ "         CASE "
				+ "            WHEN sum (idl.qty * unit.qty * report.qty) < tot.qty THEN 0 "
				+ "            ELSE -tre.value * sum (idl.qty * unit.qty * report.qty) "
				+ "         END "
				+ "            AS rebate "
				+ "    FROM target_header AS thr "
				+ "         INNER JOIN target_outlet AS tot ON thr.target_id = tot.target_id "
				+ "         INNER JOIN item_family AS itf ON tot.product_line_id = itf.id "
				+ "         INNER JOIN latest_incentive AS lie "
				+ "            ON tot.outlet_id = lie.outlet_id AND thr.end_date = lie.end_date "
				+ "         INNER JOIN target_rebate AS tre "
				+ "            ON     thr.target_id = tre.target_id "
				+ "               AND tot.product_line_id = tre.product_line_id "
				+ "         INNER JOIN main_branch AS mbh ON tot.outlet_id = mbh.main "
				+ "         INNER JOIN invoice_header AS ihr "
				+ "            ON     ihr.invoice_date BETWEEN thr.start_date AND thr.end_date "
				+ "               AND ihr.customer_id = mbh.branch "
				+ "         INNER JOIN invoice_detail AS idl "
				+ "            ON ihr.invoice_id = idl.invoice_id AND ihr.series = idl.series "
				+ "         INNER JOIN item_product_line AS ipl "
				+ "            ON     tot.product_line_id = ipl.product_line_id "
				+ "               AND idl.item_id = ipl.item_id "
				+ "         INNER JOIN qty_per AS unit "
				+ "            ON idl.item_id = unit.item_id AND idl.uom = unit.uom "
				+ "         INNER JOIN qty_per AS report "
				+ "            ON idl.item_id = report.item_id AND report.report IS TRUE "
				+ "         INNER JOIN uom ON uom.id = report.uom "
				+ "GROUP BY tot.product_line_id, "
				+ "         itf.name, "
				+ "         uom.unit, "
				+ "         tot.qty, "
				+ "         tre.value "
				);
		// @sql:off
	}
	
	public int getTargetTypeId() {
		return targetTypeId;
	}

	public void setTargetTypeId(int targetTypeId) {
		this.targetTypeId = targetTypeId;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public ArrayList<Rebate> getRebates() {
		if (rebates == null)
			rebates = new ArrayList<>();
		return rebates;
	}

	public ArrayList<Target> getTargets() {
		if (targets == null)
			targets = new ArrayList<>();
		return targets;
	}

	public ArrayList<Integer> getOutletIds() {
		if (outletIds == null)
			outletIds = new ArrayList<>();
		return outletIds;
	}

	public Object[][] getRebateData() {
		return rebateData;
	}

	public Object[][] getAdditionalRebateData() {
		return additionalRebateData;
	}

	public Object[][] getTargetData() {
		return targetData;
	}

	public String[] getCategories() {
		return categories;
	}

	public String[] getTargetTypes() {
		return targetTypes;
	}

	public String[] getProductLines() {
		return productLines;
	}
	
	public void setProductLines(String[] productLines) {
		this.productLines = productLines;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}
}
