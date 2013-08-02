package ph.txtdis.windows;

import java.sql.Date;
import java.util.ArrayList;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class Program  extends Report{
	private int programId, typeId, categoryId;
	private Date startDate, endDate;
	private ArrayList<Integer> outletList;
	private ArrayList<Rebate> rebateList;
	private ArrayList<Target> targetList;
	private String[] productLines;
	private Object[][] rebateData, targetData;
	private ItemHelper helper;
	private int size;
	private int[] productLineIds;

	public Program() {
	}

	public Program(int programId) {
		super();
		this.programId = programId;
		module = "Sales Target";
		helper = new ItemHelper();
		startDate = new DateAdder().plus(1);
		endDate = new DateAdder(startDate).plus(28);
		outletList = new ArrayList<>();
		rebateData = new Object[1][3 + size];
		Object[][] rebateArray = new Object[1][3];
		rebateArray[0][0] = 1;
		rebateArray[0][1] = 0;
		rebateArray[0][2] = "AMOUNT IN PHP";
		if (programId != 0) {
			Object[] ao = new Data().getData(programId, "" +
					"SELECT	type_id, " +
					"		category_id, " +
					"		start_date," +
					"		end_date " +
					"FROM	target_header " +
					"WHERE	target_id = ? " +
					""
					);
			if(ao != null) {
				typeId		= (int) ao[0];
				categoryId	= (int) ao[1];
				startDate 	= (Date) ao[2];
				endDate 	= (Date) ao[3];
			} else {
				this.programId = 0;
				return;
			}
		} else {
			categoryId = -10;
			typeId = 1;
		}
		productLines = helper.getProductLines(categoryId);
		size = productLines.length;
		Data sql = new Data();
		Object[][] rebateValues = sql.getDataArray(getRebateSelect());
		rebateData[0] = ArrayUtils.addAll(rebateArray[0], rebateValues[0]);
		setHeaders(categoryId);
		if (programId == 0) {
			targetData = new Object[1][3 + size];			
		} else {
			targetData = sql.getDataArray(getTargetSelect());			
		}
	}

	public void setHeaders(int categoryId) {
		headers = new String[size + 3][];
		headers[0] = new String[]{StringUtils.center("#", 2), "Line"};
		headers[1] = new String[]{StringUtils.center("ID", 4), "ID"};
		headers[2] = new String[]{StringUtils.center("NAME", 29), "String"};
		for (int i = 0; i < size; i++) {
			headers[i + 3] = new String[]{StringUtils.center(
					productLines[i], 8), "BigDecimal"};
		}
	}

	public String getRebateSelect() {
		String cteString = "" +
				"p AS (\n" +
				"SELECT " + programId + " AS target_id,\n" +
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
					"WHERE	target_id = " + programId + "\n" +
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
				"INNER JOIN customer_master AS cm\n" +
				"	ON t.outlet_id = cm.id\n" +
				"WHERE t.target_id = " + programId + "\n" +
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
					"WHERE	target_id = " + programId + "\n" +
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
				"SELECT ROW_NUMBER() OVER(),\n" +
				"		p.outlet_id,\n" +
				"		p.name,\n" +
				"	" + selString +
				"FROM " + tblString;
	}

	public int getProgramId() {
		return programId;
	}

	public void setProgramId(int programId) {
		this.programId = programId;
	}

	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
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

	public ArrayList<Rebate> getRebateList() {
		return rebateList;
	}

	public void setRebateList(ArrayList<Rebate> rebateList) {
		this.rebateList = rebateList;
	}

	public ArrayList<Target> getTargetList() {
		return targetList;
	}

	public void setTargetList(ArrayList<Target> targetList) {
		this.targetList = targetList;
	}

	public ArrayList<Integer> getOutletList() {
		return outletList;
	}

	public void addToList(Integer outletId ) {
		outletList.add(outletId);
	}

	public int[] getProductLineIds() {
		return productLineIds;
	}

	public Object[][] getRebateData() {
		return rebateData;
	}

	public Object[][] getTargetData() {
		return targetData;
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		Program i = new Program(2);
		//System.out.println(i.getRebateSelect("select"));
		for (Object[] os : i.getRebateData()) {
			for (Object o : os) {
				System.out.print(o + ", ");
			}
			System.out.println();
		}
		Database.getInstance().closeConnection();
	}
}
