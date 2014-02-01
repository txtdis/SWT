package ph.txtdis.windows;

import java.util.ArrayList;
import java.util.Arrays;

public class Channel {

	public Channel() {}

	public static int getId(String name) {
		return (int) new Query().getDatum(name, "" +
				"SELECT	id " +
				"FROM	channel " +
				"WHERE 	name = ? "
				);
	}

	public static String getName(int id) {
		return  (String) new Query().getDatum(id, "" +
				"SELECT	name " +
				"FROM	channel " +
				"WHERE 	id = ? "
				);
	}

	public static String[] getUnused(ArrayList<String> usedChannels) {
		String notIn = "$$";
		for (int i = 0; i < usedChannels.size(); i++) {
			if(i > 0) notIn += "$$, $$"; 
			notIn += usedChannels.get(i);
		}
		Object[] unused = new Query().getList("" +
				"SELECT	name " +
				"FROM	channel " +
				"WHERE name NOT IN ( " + notIn + "$$) " +
				"ORDER BY id ;"
				);	
		return Arrays.copyOf(unused, unused.length, String[].class);
	}

	public static String[] getList() {
		Object[] channels = new Query().getList("" +
				"SELECT	name " +
				"  FROM	channel " +
				" ORDER BY id ;"
				);	
		return Arrays.copyOf(channels, channels.length, String[].class);
	}

	public static String get(int customerId) {
		return (String) new Query().getDatum(customerId,"" 
				// @sql:on
				+ "SELECT ch.name " 
				+ "  FROM customer_header AS cm "
				+ "       INNER JOIN channel AS ch "
				+ "          ON cm.type_id = ch.id " 
				+ " WHERE     cm.id = ? "
				// @sql:off
				);
	}
	
	public static boolean isSpecial(int partnerId) {
        return get(partnerId).equals("OTHERS") || get(partnerId).equals("INTERNAL");
    }
}
