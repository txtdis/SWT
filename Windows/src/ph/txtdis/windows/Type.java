package ph.txtdis.windows;

public enum Type {
	// @sql:on
	ICON,
	INFO,
	MIN,
	MAX,

	// Modules
	BACKUP("Backup"),
	BANK_LIST("Bank List"), 
	BOM("Bill of Materials"),
	COUNT("Stock Take"),
	STOCKTAKE("Stock Take"), 
	CASH_SETTLEMENT("Cash Settlement"),
	COUNT_LIST("Stock Take List"), 
	COUNT_REPORT("Stock Take Report"),
	COUNT_VARIANCE("Stock Take Variance"),
	CUSTOMER("Customer"),
	CUSTOMER_LIST("Customer List"),
	DELIVERY("Delivery/Credit/Debit Order"),
	DSR("Daily Sales Report"),
	FINANCE("Financial Reports"),
	INVENTORY("Inventory"), 
	INVOICE("Invoice"),
	INVOICE_BOOKLET("Issued Invoice Booklet", "Dialog"),
	INVOICE_BOOKLET_LIST("Issued Invoice Booklet List"),
	INVOICE_DELIVERY_LIST("Invoice/Delivery Report"),
	ITEM("Item Data"), 
	ITEM_LIST("Item List"),
	ITEM_LOG("Item Movement Log"),
	LOAD_SETTLEMENT("Load Settlement"),
	MAIN_MENU("Main Menu"),
	OUTLET_LIST("Outlet List"),
	OVERDUE("Overdue"),
	PRICE_LIST("Price List"),
	PURCHASE("Purchase/Return Order"),
	PURCHASE_TARGET("Purchase Target"),
	REMIT("Remittance"),
	REMIT_SETTLEMENT("Remittance Settlement"),
	RECEIVABLES("Aging Receivables"),
	RECEIVING("Receiving Report"), 
	RECEIVING_LIST("Receiving Report List"),
	RESTORE("Restore"),
	SETTLEMENT("Route Report"),
	SALES("Sales/Bad Order"),
	SALES_LIST("Sales Order List"),
	SALES_REPORT("Sales Report"),
	SALES_TARGET("Sales Target"), 
	SALES_TARGET_LIST("Sales Target List"), 
	TRANSMIT("Transmittal"), 
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
	ADJUST("Adjust data", "adjust"),
	BACKWARD("Previous", "goPrevious"), 
	BOM_BTN("Show BOM"), 
	BOOKLET, 
	CALENDAR("Select date/s", "selectReportDate"), 
	CANCEL, 
	CLOSE("Tag as closed", "closeTransaction"),
	DUMP("Dump data to a .xls file"), 
	EDIT("Edit data", "edit"), 
	ERROR, 
	EXCEL("Write data to a .xls file", "saveAsExcel"), 
	IMPORT, 
	FORWARD("Next", "goNext"), 
	LIST("List"), 
	NEW("Create a new file"), 
	OPEN("Open a saved file", "open"), 
	PRINT("Print"), 
	REPORT("Report"), 
	SAVE("Post data to server", "post"), 
	SEARCH("Search List"), 
	SEARCH16("Search List"), 
	SMS("Send text"), 
	STOCK, 
	STOCK_PRICE, 
	TARGET, 
	OPTION("Pick an option"), 
	VARIANCE("Show variances"),
	WARNING, 
	WIZARD, 
	
	// Report Generation Options
	PURCHASING, 
	SALES_REPORTING,

	// Option Selections
	SALES_OPTION, 
	LOAD_OPTION,
	
	// Tables
	ACCOUNT, 
	
	// Quality
	BAD,
	GOOD,
	ONHOLD,
	
	// OUM
	PK,
	KG,
	L,
	TE,
	${
		@Override
        public String toString() {
			return DIS.$;
        }
	}

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
