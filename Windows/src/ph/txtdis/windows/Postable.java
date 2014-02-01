package ph.txtdis.windows;

import org.eclipse.swt.widgets.Button;

public interface Postable {
	public Button getPostButton();
	public void post();
	public Posting getPosting();
	public void updateView(int id);
}
