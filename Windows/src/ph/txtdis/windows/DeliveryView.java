package ph.txtdis.windows;

public class DeliveryView extends OrderView {
	
	public DeliveryView(Order soPo) {
		super();
		order = soPo;
		order.setModule("Delivery Report");
		order.setId(0);
		setProgress();
		setTitleBar();
		setHeader();
		getTable();
		setFooter();
		setListener();
		setFocus();
		showReport();
	}

	public DeliveryView(int orderId) {
		super(orderId);
	}
	
	@Override
	protected void runClass() {
		if (order == null) order = new Delivery(orderId);
		report = order;
	}
	
	public static void main(String[] args) {
		//Database.getInstance().getConnection("irene","ayin");
		Database.getInstance().getConnection("badette","013094");
		new DeliveryView(0);
		Database.getInstance().closeConnection();
	}
}
