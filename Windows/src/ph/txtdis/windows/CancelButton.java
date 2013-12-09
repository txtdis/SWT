package ph.txtdis.windows;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class CancelButton extends ImageButton {
	private Report report;

	public CancelButton(Composite parent, Report report) {
		super(parent, report.getModule(), "Cancel", "Cancel this" + report.getModule());
	}

	@Override
	protected void doWhenSelected() {
		switch (module) {
			case "Sales Order":
				new InputDialog(module) {
					private String reason; 

					@Override
					protected void setRightPane() {
						Composite cmp = new Composite(header, SWT.NONE);
						cmp.setLayout(new GridLayout(1, true));

						Label label = new Label(cmp, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.CENTER);
						label.setText("" +
								"Enter reason for cancelling this S/O\n");
						label.setFont(UI.REG);
						text = new Text(cmp, SWT.BORDER | SWT.V_SCROLL);
						text.setFont(UI.MONO);
						text.setLayoutData(new GridData(GridData.FILL_BOTH));
						text.setText("\n\n");
					}

					@Override
					protected void setOkButtonAction() {
						reason = text.getText().trim();
						if(reason.isEmpty()) {
							return;
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
								ps.setInt(1, ((SalesOrder) report).getId());
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
						}
						shell.dispose();
					}

					@Override
					public void setName(String name) {
						this.name = "Cancel";
					}					
				};
				break;
			default:
				new ErrorDialog("" +
						"Cancel Button is not an option\n" +
						"for " + module);
				break;
		}
	}

}
