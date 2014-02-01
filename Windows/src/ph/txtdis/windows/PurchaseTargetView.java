package ph.txtdis.windows;

import java.sql.Date;

public class PurchaseTargetView extends OrderView {

	public PurchaseTargetView(Date date) {
		super(new PurchaseTarget(date));
		type = Type.PURCHASE_TARGET;
		proceed();
	}

	@Override
	protected void addHeader() {
		new Header(this, data){
			@Override
			protected void layButtons() {
				new BackwardButton(buttons, data);
				new ForwardButton(buttons, data);
				postButton = new ImgButton(buttons, Type.SAVE, view).getButton();
				new ImgButton(buttons, Type.EXCEL, view);
			}
		};
	}

	@Override
    protected void addSubheader() {
	    // TODO Auto-generated method stub
    }

	@Override
    public Posting getPosting() {
	    return new OrderPosting((OrderData) data);
    }
}
