package ph.txtdis.windows;

import java.util.Arrays;

public class Employee {

	public Employee() {}

	public static String[] getNames() {
		Object[] names = new Query().getList("" +
				"SELECT	name " +
				"  FROM	contact_detail " +
				" WHERE	customer_id = 0 " + 
				" ORDER BY name "
				);
		return Arrays.copyOf(names, names.length, String[].class);
	}
}
