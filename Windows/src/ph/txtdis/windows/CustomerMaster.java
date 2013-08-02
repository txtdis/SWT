package ph.txtdis.windows;

import java.util.ArrayList;

public class CustomerMaster extends Report {
	private String smsId, name, street;
	private String firstName, surname, designation;
	private String district, city, province, route, channel;
	private String[] districts, cities, provinces, routes, channels;
	private long phone;
	private Object[][] creditData, discountData;
	private ArrayList<Credit> creditList;
	private ArrayList<PartnerDiscount> discountList;

	public CustomerMaster(int id)  {
		super();
		this.id = id;
		module = "Customer Data";
		if (id == 0) {
			smsId = "";
			name = "";
			street = "";
			firstName = "";
			surname = "";
			designation = "";
			channel = new Channel().getDefault();
		} else {
			Object[] ao = new Data().getData(id, "" +
					"SELECT	cm.sms_id, " +
					"		cm.name," +
					"		ch.name " +
					"FROM	customer_master AS cm " +
					"INNER JOIN channel AS ch " +
					"ON cm.type_id = ch.id " +
					"WHERE	cm.id = ? " +
					""
					);
			smsId 		= (String) ao[0];
			name 		= (String) ao[1];
			channel 	= (String) ao[2];

			Address addy = new Address(id);
			street = addy.getStreet();
			district =  addy.getDistrict();
			city =  addy.getCity();
			province =  addy.getProvince();
			route = new Route(new Account(id).getRouteId()).getName();

			Contact cd = new Contact(id);
			firstName = cd.getName();
			surname = cd.getSurname();
			designation = cd.getDesignation();
			
			int contactId = cd.getId();
			phone = new Phone(contactId).getNumber();
		}
		
		provinces = new Area(0).getAreas();
		cities = new Area(provinces[0]).getAreas();
		districts = new Area(cities[0]).getAreas();
		routes = new Route().getRoutes();
		channels = new Channel().getChannels();
		creditData = new Credit(id).getData();
		if(creditData == null) creditData = new Object[0][0];
		discountData = new PartnerDiscount(id).getData();	
		if(discountData == null) discountData = new Object[0][0];
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSmsId() {
		return smsId;
	}

	public void setSmsId(String smsId) {
		this.smsId = smsId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public String getDesignation() {
		return designation;
	}
	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public long getPhone() {
		return phone;
	}

	public void setPhone(long phone) {
		this.phone = phone;
	}

	public String[] getDistricts() {
		return districts;
	}

	public String[] getCities() {
		return cities;
	}

	public String[] getProvinces() {
		return provinces;
	}

	public String[] getRoutes() {
		return routes;
	}

	public String[] getChannels() {
		return channels;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public Object[][] getCreditData() {
		return creditData;
	}

	public void setCreditData(Object[][] creditData) {
		this.creditData = creditData;
	}

	public Object[][] getDiscountData() {
		return discountData;
	}

	public void setDiscountData(Object[][] discountData) {
		this.discountData = discountData;
	}

	public ArrayList<Credit> getCreditList() {
		return creditList;
	}

	public void setCreditList(ArrayList<Credit> creditList) {
		this.creditList = creditList;
	}

	public ArrayList<PartnerDiscount> getDiscountList() {
		return discountList;
	}

	public void setDiscountList(ArrayList<PartnerDiscount> discountList) {
		this.discountList = discountList;
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		new CustomerMaster(449);
		Database.getInstance().closeConnection();
	}
}
