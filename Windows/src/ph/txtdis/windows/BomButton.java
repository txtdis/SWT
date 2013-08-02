package ph.txtdis.windows;

import org.eclipse.swt.widgets.Composite;

public class BomButton extends ReportButton {
	private ItemMaster im;

	public BomButton(Composite parent, ItemMaster im) {
		super(parent, im, "Graph", "Open Bill of Materials");
		this.im = im;
	}

	@Override
	protected void doWhenSelected(){
		new BomView(im);
	}
}
