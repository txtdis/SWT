package ph.txtdis.windows;

public class Address {
	private String street, district, city, province, stmt;

	public Address() {
		this(0);
	}

	public Address(int id) {
		stmt =	"SELECT " +
				"		CASE WHEN prov.name IS null " +
				"			THEN '' ELSE prov.name END AS province, " +
				"		CASE WHEN city.name IS null " +
				"			THEN '' ELSE city.name END AS city, " +
				"		CASE WHEN dist.name IS null " +
				"			THEN '' ELSE dist.name END AS district, " +
				"		CASE WHEN street IS null " +
				"			THEN '' ELSE street END AS street, " +
				"		a.customer_id " +
				"FROM 	address AS a " +
				"LEFT OUTER JOIN area AS prov " +
				"	ON prov.id = a.province " +
				"LEFT OUTER JOIN area AS city " +
				"	ON city.id = a.city " +
				"LEFT OUTER JOIN area AS dist " +
				"	ON dist.id = a.district " +
				"WHERE a.customer_id = ? " +
				"";
		Object[] ao;
		ao = new SQL().getData(id, stmt);
		if (ao != null) {
			province 	= (String) ao[0];
			city 		= (String) ao[1];
			district 	= (String) ao[2];
			street 		= (String) ao[3];
		}
	}

	public String getAddress() {
		return street + district + city + province; 
	}

	public String getCityDistrict() {
		return district + city;
	}

	public String getStreet() {
		return street;
	}

	public String getDistrict() {
		return district;
	}

	public String getCity() {
		return city;
	}

	public String getProvince() {
		return province;
	}

	public String getStmt() {
		return stmt;
	}
}
