package ph.txtdis.windows;

public class StockTakeHelper {
	
	public boolean hasId(int id) {
		Object o = new Data().getDatum(id, "" + 
				"SELECT count_id  " +
				"FROM 	count_header " +
				"WHERE 	count_id = ? " 
				);
		return (o == null ? false : true);
	}
}
