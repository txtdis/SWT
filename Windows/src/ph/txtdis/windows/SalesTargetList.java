package ph.txtdis.windows;

import org.apache.commons.lang3.StringUtils;

public class SalesTargetList extends Data implements Listed {

	public SalesTargetList() {
		type = Type.SALES_TARGET_LIST;
		tableHeaders = new String[][] {
				{StringUtils.center("TYPE", 18), "String"},
				{StringUtils.center("ID", 4), "ID"},
				{StringUtils.center("CATEGORY", 4), "String"},
				{StringUtils.center("START", 10), "Date"},
				{StringUtils.center("END", 10), "Date"}
		};
		tableData = new Query().getTableData("" +
				"SELECT	tt.name, " +
				"		th.target_id, " +
				"		if.name, " +
				"		th.start_date, " +
				"		th.end_date " +
				"FROM 	target_header AS th " +
				"INNER JOIN item_family AS if " +
				"	ON	th.category_id = if.id " +
				"INNER JOIN target_type AS tt " +
				"	ON	th.type_id = tt.id " +
				"ORDER BY th.target_id " 
				);
	}

	@Override
    public Type getListedType() {
	    return Type.SALES_TARGET;
    }
}
