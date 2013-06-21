package ph.txtdis.windows;

import java.util.ArrayList;
import java.util.Arrays;

public class Channel {
	private int id;
	private String name;
	private String[] channels;

	public Channel() {
		Object[] objects = new SQL().getData("" +
				"SELECT	name " +
				"FROM	channel " +
				"ORDER BY name " +
				"");
		channels = Arrays.copyOf(objects, objects.length, String[].class);
	}

	public Channel(ArrayList<String> usedChannels) {
		String notIn = "$$";
		for (int i = 0; i < usedChannels.size(); i++) {
			if(i > 0) notIn += "$$, $$"; 
			notIn += usedChannels.get(i);
		}
		Object[] objects = new SQL().getData("" +
				"SELECT	name " +
				"FROM	channel " +
				"WHERE name NOT IN ( " +
				notIn +
				"$$) " +
				"ORDER BY id ; " +
				""
				);	
		channels = Arrays.copyOf(objects, objects.length, String[].class);
	}

	public Channel(String name) {
		id = (int) new SQL().getDatum(name, "" +
				"SELECT	id " +
				"FROM	channel " +
				"WHERE 	name = ? " +
				"");
	}

	public Channel(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String[] getChannels() {
		return channels;
	}

	public String getDefault() {
		return (String) new SQL().getDatum("" +
				"select	value\n" +
				"from	default_text\n" +
				"where	name = 'CHANNEL'\n" +
				";\n" +
				""); 
	}
}
