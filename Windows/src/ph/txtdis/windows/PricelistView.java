package ph.txtdis.windows;

public class PricelistView extends ReportView {
	private Pricelist pricelist;
		
	public PricelistView() {
		setProgress();
		setTitleBar();
		setHeader();
		setTableBar();
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
		Database.getInstance().getConnection("irene","ayin");
		new PricelistView();
		Database.getInstance().closeConnection();
	}
}