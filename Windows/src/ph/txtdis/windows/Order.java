package ph.txtdis.windows;

public class Order {
	
	private static boolean isDataType(Type type) {
		return type == Type.ITEM || type == Type.CUSTOMER;
	}
	
	public static Object getMinId(Type type) {
		if (type == Type.INVOICE)
			return new Query().getList("SELECT invoice_id, series FROM invoice_header ORDER BY 1, 2 LIMIT 1;");
		else if (isDataType(type))
			return new Query().getDatum("SELECT min(id) FROM " + type + "_header");
		else
			return new Query().getDatum("SELECT min(" + type + "_id) FROM " + type + "_header");
	}

	public static Object getMaxId(Type type) {
		if (type == Type.INVOICE)
			return new Query().getList("SELECT invoice_id, series FROM invoice_header ORDER BY 1 DESC, 2 DESC LIMIT 1;");
		else if (isDataType(type))
			return new Query().getDatum("SELECT max(id) FROM " + type + "_header");
		else
			return new Query().getDatum("SELECT max(" + type + "_id) FROM " + type + "_header");
	}

}
