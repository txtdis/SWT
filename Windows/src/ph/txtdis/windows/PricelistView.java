package ph.txtdis.windows;

public class PricelistView extends ReportView {
	private Pricelist pricelist;
		
	public PricelistView() {
		setProgress();
		setTitleBar();
		setHeader();
		getTable();
		setFooter();
		setListener();
		setFocus();
		showReport();
	}
	
	@Override
	protected void runClass() {
		report = pricelist = new Pricelist();
	}
	
	@Override
	protected void setTitleBar() {
		new PricelistBar(this, pricelist);
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin","localhost");
		new PricelistView();
		Database.getInstance().closeConnection();
	}
}