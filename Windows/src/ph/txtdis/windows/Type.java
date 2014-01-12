package ph.txtdis.windows;

public enum Type {
	// @sql:on

	// Modules
	BACKUP("Backup", "start"),
	BANK_LIST("Bank List"), 
	BOM("Bill of Materials"),
	COUNT("Stock Take", "start"),
	COUNT_LIST("Stock Take List"),
	COUNT_VARIANCE("Stock Take Variance"),
	CUSTOMER("Customer Data"),
	CUSTOMER_LIST("Customer List", "start"),
	DELIVERY("Delivery Report", "start"),
	DSR("Daily Sales Report"),
	FINANCE("Financial Reports", "start"),
	INVENTORY("Inventory", "start"),
	INVOICE("Invoice", "start"),
	INVOICE_BOOKLET("Issued Invoice Booklets"),
	ITEM("Item Data"), 
	ITEM_LIST("Item List"),
	ITEM_LOG("Item Movement Log"),
	LOAD_BALANCE("Loaded Material Balance"),
	MAIN_MENU("Main Menu"),
	NO_RMA_RECEIVING_LIST, 
	OUTLET_LIST("Outlet List"),
	OVERDUE("Overdue"),
	PRICE_LIST("Price List"),
	PURCHASE("Purchase Order", "start"),
	REMIT("Remittance", "start"),
	RECEIVABLES("Aging Receivables", "start"),
	RECEIVING("Receiving Report", "start"), 
	RECEIVING_LIST("Receiving Report List"),
	RESTORE("Restore", "start"),
	ROUTE_REPORT("Route Report", "start"),
	SALES("Sales Order", "start"),
	SALES_LIST("Sales Order List"),
	SALES_REPORT("Sales Report", "start"),
	SI_DR_LIST("Invoice/Delivery Report"),
	SIV("Sales-in Volume Target List"),
	SOA("Statement of Account"),
	SALES_TARGET("Sales Target"), 
	TARGET_LIST("Sales Target List"), 
	VAT("Value-Added Tax"),

	// Items
	BUNDLED, DERIVED, MONETARY, PURCHASED, REPACKED, VIRTUAL,

	// Product Lines
	REF_MEAT,

	// User Group
	SUPER_FINANCE, SUPER_SUPPLY, SUPER_USER, USER_FINANCE, USER_SALES, USER_SUPPLY, 

	// Sales Report
	STT, CALL,

	// Pricing
	WHOLESALE, RETAIL, 

	// Text inputs
	NAME("NAME"), 
	DESCRIPTION("DESCRIPTION"), 
	UNSPSC("UNSPSC ID #"), 
	NULL, 
	DETAILS("DETAILS"), 
	ITEM_CODE("ITEM CODE #"), 
	TYPE("TYPE"),
	LINE("LINE"),
	QTY_PER("QUANTITY PER UOM RELATIVE TO \"PK\""),
	VOLUME_DISCOUNT("VOLUME DISCOUNT"),
	UNIT_PRICE("PRICE PER PK"),
	PARTNER("PARTNER"), 
	ID("ID"),
	SMS_ID("SMS ID"),
	BRANCH("BRANCH-OF ID"),
	HEAD_OFFICE("HEAD OFFICE"),
	CHANNEL("CHANNEL"), 
	ADDRESS("ADDRESS"),
	PROVINCE("PROVINCE"),
	CITY("CITY"),
	DISTRICT("DISTRICT"),
	STREET("STREET"),
	CONTACT("CONTACT"),
	SURNAME("SURNAME"),
	DESIGNATION("DESIGNATION"),
	PHONE("PHONE"),
	ROUTE("ROUTE"),
	CREDIT("CREDIT"),
	DISCOUNT("DISCOUNT"),
	RR("R/R #"),
	LOCATION("LOCATION"),
	PARTNER_INFO("PARTNER INFORMATION"),
	RECEIPT("RECEIPT DETAILS"),
	SO_PO("S/O(P/O) #"),
	DATE("DATE"),
	CATEGORY("CATEGORY"),
	START("START"),
	END("END"),
	REBATE("REBATE SCHEDULE"),
	EXTRA("EXTRA INCENTIVE"),
	OUTLET_TARGET("TARGET PER OUTLET"),
	GATEKEEPER("GATEKEEPER"),
	SERIES("SERIES"),
	ISSUEE("ISSUED TO"),
	METRIC("METRIC"),
	PRODUCT_LINE("PRODUCT LINE"),
	GROUPING("GROUPING"),
	TAKER("TAKER"),
	CHECKER("CHECKER"),
	TAG("TAG"),
	DUE("DUE"),
	INVOICE_ID("INVOICE #"),
	ENTERED_TOTAL("S/I AMOUNT"),
	SO_ID("S/O #"),
	DR_ID("D/R #"),
	DR_AMT("D/R AMT"),
	LIMIT("LIMIT"),
	PO_ID("P/O #"),
	BANK_INFO("BANK INFORMATION"),
	TIMESTAMP("TIMESTAMP"),
	TIME("TIME"), REF_ID,
	REMIT_ID("REMIT ID"), 
	AMOUNT("AMOUNT"),
	OR_ID("O/R #"), 
	STATUS("STATUS"),
	TAGGER("PER"),
	ENCODER("ENCODER"), 
	PERCENT("0.00%"),
	VATABLE("VATABLE"), 
	VAT_TOTAL("VAT"),
	TOTAL("TOTAL"),
	NO_DISCOUNT("NOT DISCOUNTED"),
	ORDER_INFO("ORDER INFO"),

	// Parts
	HEADER, 
	INPUT, 
	TABLE, 
	MODULE, 
	GROUP, 
	COMPOSITE, 
	TABLE_ITEM, 
	TABLE_TEXT, 

	// Buttons
	ADD("Add"), 
	BACKWARD("Previous"), 
	BOM_BTN("Show BOM"), 
	BOOKLET, 
	CALENDAR, 
	CANCEL, 
	DUMP("Dump data"), 
	EDIT, 
	EXCEL("Save to .xls"), 
	EXIT("Exit"), 
	IMPORT, 
	FORWARD("Next"), 
	LIST("List"), 
	NEW("New"), 
	NEW16("New"), 
	OPEN("Open"), 
	PRINT("Print"), 
	REPORT("Report"), 
	SAVE("Post"), 
	SEARCH("Search"), 
	SMS("Send text"), 
	STOCK, 
	STOCK_PRICE, 
	TARGET, 
	OPTION("Pick an option"), 
	VARIANCE("Compare"), 
	WIZARD, 
	
	// Report Generation Options
	PURCHASING, 
	SALES_REPORTING,

	// Option Selections
	SALES_OPTION, 
	LOAD_OPTION
	
	// @sql:off
	;

	private String name, method;

	Type() {
	}

	Type(String name) {
		this.name = name;
	}

	Type(String name, String method) {
		this.name = name;
		this.method = method;
	}

	public String getMethod() {
		return method;
	}

	public String getName() {
		return name;
	}
}
