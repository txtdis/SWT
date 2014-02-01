package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;

import org.apache.commons.lang3.StringUtils;

public class CustomerData extends OrderData {
	private boolean isCreditChanged, isDiscountChanged, isRouteChanged;
	private int creditTerm, familyId, gracePeriod, hqId;
	private long phone;
	private BigDecimal creditLimit;
	private Date creditStartDate, discountStartDate, routeStartDate;
	private Object[][] creditData, discountData, routeData;
	private String smsId, name, street, firstName, surname, designation, district, city, province, route, channel;
	private String[] districts, cities, provinces, routes, channels;
	private String[][] creditHeaders, discountHeaders, routeHeaders;

	public CustomerData() {
		this(0);
	}

	public CustomerData(int id) {
		super();
		this.id = id;
		setHeaderData();
		setDetails();
	}

	private void setDetails() {
		setTableHeaders();
		if (id != 0)
			setProperties();
		else
			setDefaults();
	}

	@Override
    protected void setProperties() {
		type = Type.CUSTOMER;
    }

	private void setDefaults() {
		channels = Channel.getList();
		provinces = new Area(0).getAreas();
		province = provinces[0];
		cities = new Area(provinces[0]).getAreas();
		districts = new Area(cities[0]).getAreas();
	}

	protected void setHeaderData() {
		headerData = getHeaderData(type, id);
		setInfo();
		setAddress();
		setContact();
		creditData = Credit.getData(id);
		routeData = Route.getData(id);
		discountData = PartnerDiscount.getData(id);
		channels = new String[] { channel };
		provinces = new String[] { province };
		cities = new String[] { city };
		districts = new String[] { district };
	}

	private void setContact() {
		Contact contact = new Contact(id);
		firstName = contact.getName();
		surname = contact.getSurname();
		designation = contact.getDesignation();
		phone = new Phone(contact.getId()).getNumber();
	}

	private void setAddress() {
		Address addy = new Address(id);
		street = addy.getStreet();
		district = addy.getDistrict();
		city = addy.getCity();
		province = addy.getProvince();
	}

	private void setInfo() {
		smsId = headerData[0].toString();
		name = headerData[1].toString();
		channel = headerData[2].toString();
		hqId = (int) headerData[3];
	}

	private void setTableHeaders() {
		setCreditHeaders();
		setDiscountHeaders();
		setRouteHeaders();
	}

	// @sql:on
	private void setRouteHeaders() {
		routeHeaders = new String[][] { 
				{ StringUtils.center("#", 3), "Line" },
				{ StringUtils.center("ROUTE", 13), "String" }, 
				{ StringUtils.center("SINCE", 10), "DATE" },
				{ StringUtils.center("ENCODER", 9), "String" } };
	}

	private void setDiscountHeaders() {
		discountHeaders = new String[][] { 
				{ StringUtils.center("#", 3), "Line" },
				{ StringUtils.center("ID", 4), "ID" }, 
				{ StringUtils.center("ITEM FAMILY", 28), "String" },
				{ StringUtils.center("LEVEL 1", 6), "BigDecimal" }, 
				{ StringUtils.center("LEVEL 2", 6), "BigDecimal" },
				{ StringUtils.center("SINCE", 10), "Date" }, 
				{ StringUtils.center("ENCODER", 9), "String" } };
	}

	private void setCreditHeaders() {
		creditHeaders = new String[][] { 
				{ StringUtils.center("#", 3), "Line" },
				{ StringUtils.center("LIMIT", 13), "Quantity" }, 
				{ StringUtils.center("TERM", 10), "Quantity" },
				{ StringUtils.center("GRACE", 10), "Quantity" }, 
				{ StringUtils.center("SINCE", 10), "Date" },
				{ StringUtils.center("ENCODER", 9), "String" } };
	}
	// @sql:off

	@Override
	public Object[] getHeaderData(Type type, Object id) {
		return sql.getList(id,"" +
				// @sql:on
				"SELECT	cm.sms_id, " +
				"		cm.name, " +
				"		ch.name, " +
				" 		CASE WHEN cm.branch_of IS NULL THEN 0 ELSE cm.branch_of END " +
				"FROM	customer_header AS cm " +
				"INNER JOIN channel AS ch " +
				"ON cm.type_id = ch.id " +
				"WHERE	cm.id = ? "
				// @sql:off
				);
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
