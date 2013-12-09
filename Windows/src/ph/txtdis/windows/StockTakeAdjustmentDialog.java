package ph.txtdis.windows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class StockTakeAdjustmentDialog extends DialogView {
	private Text txtQty, txtReason;
	private int itemId;
	//private StockTakeVariance stv;
	//private Object[][] data;

	public StockTakeAdjustmentDialog(StockTakeVariance stv, int itemId) {
		super();
		//this.stv = stv;
		//data = stv.getData();
		this.itemId = itemId;
		setName("Adjustment");
		open();
	}

	@Override
	protected void setHeader() {
		super.setHeader();
		Label label = new Label(header, SWT.CENTER);
		String string; 
		if(Login.getGroup().equals("sys_admin")) {
			string = "Approve or not inventory adjustment for";
		} else {
			string = "Enter adjustment quantity and its justification for\n";
		}
		string += new ItemHelper().getName(itemId);
		label.setText(string);
		label.setLayoutData(new GridData(
				GridData.CENTER, 
				GridData.CENTER, 
				true, 
				true, 
				2, 
				1
				));
	}

	@Override
	public void setRightPane() {
		txtQty = new TextInputBox(header, "", itemId).getText();

		txtReason = new Text(header, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		txtReason.setFont(UI.MONO);
		txtReason.setText("\n\n");
	}

	@Override
	protected void setOkButtonAction() {
		//BigDecimal qty = new BigDecimal(txtQty.getText().trim());
		String reason = txtReason.getText().trim();
		if(reason.isEmpty()) {
			new ErrorDialog("" +
					"There should be a justification\n" +
					"for the adjustment.");
		} else {
			Connection conn = null;
			PreparedStatement ps = null;
			try {
				conn = Database.getInstance().getConnection();
				conn.setAutoCommit(false);
				ps = conn.prepareStatement("" +
						"INSERT INTO sales_cancellation\n" +
						"	(sales_id, reason)\n" +
						"	VALUES (?, ?)");
				ps.setInt(1, 2);
				ps.setString(2, reason);
				ps.executeUpdate();
				conn.commit();
			} catch (SQLException e) {
				if (conn != null) {
					try {
						conn.rollback();
					} catch (SQLException er) {
						er.printStackTrace();
						new ErrorDialog(er);
					}
				}
				e.printStackTrace();
				new ErrorDialog(e);
			} finally {
				try {
					if (ps != null ) ps.close();
					conn.setAutoCommit(true);
				} catch (SQLException e) {
					e.printStackTrace();
					new ErrorDialog(e);
				}
			}	
			shell.dispose();
		}
	}

	@Override
	public void setName(String name) {
		this.name = "Adjustment";
	}					

	@Override
	protected void setListener() {
		txtQty.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				txtReason.setFocus();
				txtReason.selectAll();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				txtReason.setFocus();
				txtReason.selectAll();
			}
		});
		txtReason.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				setOkButtonAction();
			}
		});
	}

	@Override
	protected void setFocus() {
		txtQty.setTouchEnabled(true);
		txtQty.setFocus();
	}
}

