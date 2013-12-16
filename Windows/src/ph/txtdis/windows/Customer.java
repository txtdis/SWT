package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;

import org.apache.commons.lang3.StringUtils;

public class Customer extends Order {
	private boolean isCreditChanged, isDiscountChanged, isRouteChanged;
	private int creditTerm, familyId, gracePeriod, hqId;
	private long phone;
	private BigDecimal creditLimit;
	private Data sql;
	private Date creditStartDate, discountStartDate, routeStartDate;
	private Object object;
	private Object[] objects;
	private Object[][] creditData, discountData, routeData;
	private String smsId, name, street, firstName, surname, designation, district, city, province, route, channel;
	private String[] districts, cities, provinces, routes, channels;
	private String[][] creditHeaders, discountHeaders, routeHeaders;

	public Customer() {
		sql = new Data();
	}

	public Customer(int id, boolean isEditable) {
		super();
		this.id = id;
		this.isEditable = isEditable;
		sql = new Data();
		module = "Customer Data";
		type = "customer";
		creditHeaders = new String[][] {
		        {
		                StringUtils.center("#", 3), "Line" }, {
		                StringUtils.center("LIMIT", 13), "Integer" }, {
		                StringUtils.center("TERM", 10), "Integer" }, {
		                StringUtils.center("GRACE", 10), "Integer" }, {
		                StringUtils.center("SINCE", 10), "Date" }, {
		                StringUtils.center("ENCODER", 9), "String" } };
		discountHeaders = new String[][] {
		        {
		                StringUtils.center("#", 3), "Line" }, {
		                StringUtils.center("ID", 4), "ID" }, {
		                StringUtils.center("ITEM FAMILY", 28), "String" }, {
		                StringUtils.center("LEVEL 1", 6), "BigDecimal" }, {
		                StringUtils.center("LEVEL 2", 6), "BigDecimal" }, {
		                StringUtils.center("SINCE", 10), "Date" }, {
		                StringUtils.center("ENCODER", 9), "String" } };
		routeHeaders = new String[][] {
		        {
		                StringUtils.center("#", 3), "Line" }, {
		                StringUtils.center("ROUTE", 13), "String" }, {
		                StringUtils.center("SINCE", 10), "DATE" }, {
		                StringUtils.center("ENCODER", 9), "String" } };
		if (id != 0) {
			// @sql:on
			objects = sql.getData(id, "" +
					"SELECT	cm.sms_id, " +
					"		cm.name, " +
					"		ch.name, " +
					" 		CASE WHEN cm.branch_of IS NULL THEN 0 ELSE cm.branch_of END " +
					"FROM	customer_master AS cm " +
					"INNER JOIN channel AS ch " +
					"ON cm.type_id = ch.id " +
					"WHERE	cm.id = ? ");
			// @sql:off
		}
		if (objects != null) {
			smsId = (String) objects[0];
			name = (String) objects[1];
			channel = (String) objects[2];
			hqId = (int) objects[3];

			Address addy = new Address(id);
			street = addy.getStreet();
			district = addy.getDistrict();
			city = addy.getCity();
			province = addy.getProvince();

			Contact contact = new Contact(id);
			firstName = contact.getName();
			surname = contact.getSurname();
			designation = contact.getDesignation();
			phone = new Phone(contact.getId()).getNumber();

			creditData = new Credit(id).getData();
			routeData = new Route().getData(id);
			discountData = new PartnerDiscount(id).getData();
			// @sql:on
			if (isEditable) {
				channels = new String[] { "AMBULANT", channel };
				provinces = new Area(0).getAreas();
				cities = new Area(province).getAreas();
				districts = new Area(city).getAreas();
			} else {
				channels = new String[] { channel };
				provinces = new String[] { province };
				cities = new String[] { city };
				districts = new String[] { district };
			// @sql:off			
			}
		} else {
			channels = new Channel().getChannels();
			provinces = new Area(0).getAreas();
			province = provinces[0];
			cities = new Area(provinces[0]).getAreas();
			districts = new Area(cities[0]).getAreas();
		}
	}

	public String getBankName(int id) {
		// @sql:on
		object = sql.getDatum(id, "" 
				+ "SELECT name " 
				+ "  FROM customer_master " 
				+ " WHERE id = ? AND type_id = 10 ");
		return object == null ? "" : (String) object;		
		// @sql:off
	}

	public boolean isContactPhoneOnFile(int contactId) {
		return new Phone(contactId).getNumber() == 0L ? false : true;
	}

	public boolean isCreditStartDateOnFile(Date date, int partnerId) {
		// @sql:on
		object = sql.getDatum(new Object[] { date, partnerId }, "" 
				+ "SELECT start_date "
				+ "  FROM credit_detail "
				+ " WHERE start_date = ? "
				+ "   AND customer_id = ? ");
		// @sql:off
		return object == null ? false : true;
	}

	public boolean isCustomerContactOnFile(int partnerId) {
		// @sql:on
		object = sql.getDatum(partnerId, ""
				+ "SELECT name "
				+ "  FROM contact_detail "
				+ "  WHERE customer_id = ?; ");
		// @sql:off
		return object == null ? false : true;
	}

	public boolean isDiscountStartDateOnFile(Date date, int partnerId) {
		// @sql:on
		object = sql.getDatum(new Object[] { date, partnerId }, "" 
				+ "SELECT start_date "
				+ "  FROM discount "
				+ " WHERE start_date = ? "
				+ "   AND customer_id = ? ");
		// @sql:off
		return object == null ? false : true;
	}

	public boolean isForAnExTruck(int id) {
		// @sql:on
		object = sql.getDatum(id, "" 
				+ "SELECT name "
				+ "  FROM customer_master "
				+ " WHERE id = ? AND name like '%EX-TRUCK%';");
		// @sql:off
		return object == null ? false : true;
	}

	public int getIdfromSms(String smsId) {
		// @sql:on
		object = sql.getDatum(smsId, "" 
				+ "SELECT id " 
				+ "  FROM customer_master "
				+ " WHERE sms_id = ?");
		return object == null ? 0 : (int) object;		
		// @sql:off
	}

	public boolean isInternalOrOthers(int id) {
		// @sql:on
		object = sql.getDatum(id, "" 
				+ "SELECT cm.name "
				+ "  FROM customer_master AS cm "
				+ "       INNER JOIN channel AS ch ON cm.type_id = ch.id "
				+ " WHERE     cm.id = ? "
				+ "       AND (ch.name = 'INTERNAL' OR ch.name = 'OTHERS');");
		// @sql:off
		return object == null ? false : true;
	}

	public String getName(int partnerId) {
		// @sql:on
		object = sql.getDatum(partnerId, "" 
				+ "SELECT name " 
				+ "  FROM customer_master " 
				+ " WHERE id = ?");
		return object == null ? "" : (String) object;		
		// @sql:off
	}

	public boolean isOnFile(int id) {
		// @sql:on
		object = sql.getDatum(id, "" 
				+ "SELECT max(id) "
				+ "  FROM customer_master ");
		// @sql:off
		

		// @sql:on
		object = sql.getDatum(id, "" 
				+ "SELECT name "
				+ "  FROM customer_master "
				+ " WHERE id = ? ");
		// @sql:off
		return object == null ? false : true;
	}

	public boolean isRouteStartDateOnFile(Date date, int partnerId) {
		// @sql:on
		object = sql.getDatum(new Object[] { date, partnerId }, "" 
				+ "SELECT start_date "
				+ "  FROM account "
				+ " WHERE start_date = ? "
				+ "   AND customer_id = ? ");
		// @sql:off
		return object == null ? false : true;
	}

	public boolean hasSmsId(String smsId) {
		// @sql:on
		object = sql.getDatum(smsId, "" 
				+ "SELECT name " 
				+ "  FROM customer_master " 
				+ " WHERE sms_id = ? ");
		// @sql:off
		return object == null ? false : true;
	}

	public boolean isVendor(int id) {
		// @sql:on
		object = sql.getDatum(id, "" 
				+ "SELECT cm.name " 
				+ "  FROM customer_master AS cm "
				+ "       INNER JOIN channel AS ch "
				+ "          ON cm.type_id = ch.id " 
				+ " WHERE cm.id = ? AND (ch.name = 'VENDOR');");
		// @sql:off
		return object == null ? false : true;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public boolean isCreditChanged() {
		return isCreditChanged;
	}

	public String[][] getCreditHeaders() {
		return creditHeaders;
	}

	public String[] getChannels() {
		return channels;
	}

	public String[] getCities() {
		return cities;
	}

	public Object[][] getCreditData() {
		return creditData;
	}

	public BigDecimal getCreditLimit() {
		return creditLimit;
	}

	public void setCreditLimit(BigDecimal creditLimit) {
		this.creditLimit = creditLimit;
	}

	public int getCreditTerm() {
		return creditTerm;
	}

	public void setCreditTerm(int creditTerm) {
		this.creditTerm = creditTerm;
	}

	public Date getCreditStartDate() {
		return creditStartDate;
	}

	public void setCreditStartDate(Date creditStartDate) {
		this.creditStartDate = creditStartDate;
		isCreditChanged = true;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public boolean isDiscountChanged() {
		return isDiscountChanged;
	}

	public Object[][] getDiscountData() {
		return discountData;
	}

	public String[][] getDiscountHeaders() {
		return discountHeaders;
	}

	public Date getDiscountStartDate() {
		return discountStartDate;
	}

	public void setDiscountStartDate(Date discountStartDate) {
		this.discountStartDate = discountStartDate;
		isDiscountChanged = true;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String[] getDistricts() {
		return districts;
	}

	public int getFamilyId() {
		return familyId;
	}

	public void setFamilyId(int familyId) {
		this.familyId = familyId;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public int getGracePeriod() {
		return gracePeriod;
	}

	public void setGracePeriod(int gracePeriod) {
		this.gracePeriod = gracePeriod;
	}

	public int getHqId() {
		return hqId;
	}

	public void setHqId(int hqId) {
		this.hqId = hqId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getPhone() {
		return phone;
	}

	public void setPhone(long phone) {
		this.phone = phone;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String[] getProvinces() {
		return provinces;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public boolean isRouteChanged() {
		return isRouteChanged;
	}

	public Object[][] getRouteData() {
		return routeData;
	}

	public void setRouteData(Object[][] routeData) {
		this.routeData = routeData;
	}

	public String[][] getRouteHeaders() {
		return routeHeaders;
	}

	public String[] getRoutes() {
		return routes;
	}

	public Date getRouteStartDate() {
		return routeStartDate;
	}

	public void setRouteStartDate(Date routeStartDate) {
		this.routeStartDate = routeStartDate;
		isRouteChanged = true;
	}

	public String getSmsId() {
		return smsId;
	}

	public void setSmsId(String smsId) {
		this.smsId = smsId;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}
}
