package ph.txtdis.windows;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class InvoiceView extends OrderView {
	private String series;

	public InvoiceView(Order soPo) {
		super(soPo);
	}

	public InvoiceView(Integer orderId) {
		this(orderId, " ");
	}

	public InvoiceView(Integer orderId, String series) {
		super(orderId, series);
	}

	@Override
	protected void setTitleBar() {
		new ReportTitleBar(this, order) {
			@Override
			protected void layButtons() {
				btnNew = new NewButton(buttons, module).getButton();
				// Get Saved Invoice Button
				new RetrieveButton(buttons, report) {
					@Override
					public void doWhenSelected() {
						new RetrieveDialog(module) {
							private Combo combo;

							@Override
							protected void setRightPane() {
								Composite right = new Composite(header, SWT.NONE);
								right.setLayout(new GridLayout(2, false));
								Label label = new Label(right, SWT.CENTER);
								label.setText("Select invoice booklet\n" + "series and enter its ID#");
								label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER,
								        GridData.VERTICAL_ALIGN_CENTER, true, true, 2, 1));
								combo = new Combo(right, SWT.READ_ONLY);
								String[] comboItems = new OrderHelper().getSeries();
								if (comboItems != null)
									combo.setItems(comboItems);
								combo.select(0);
								combo.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
								text = new Text(right, SWT.BORDER);
								text.setLayoutData(new GridData(GridData.FILL_BOTH));
								text.setBackground(DIS.YELLOW);
							}

							@Override
							protected void setOkButtonAction() {
								String strId = text.getText();
								if (StringUtils.isBlank(strId))
									return;
								// retrieve report from id input
								orderId = Integer.parseInt(strId);
								// check if id is in the system
								series = combo.getText();
								boolean hasId = new OrderHelper(orderId).isOnFile(series);
								if (!hasId) {
									new ErrorDialog("" + module + " #" + orderId + series + "\n"
									        + "is not in our system.");
									text.setText("");
									combo.setFocus();
									return;
								} else {
									image.getImage().dispose();
									for (Shell shell : DIS.DISPLAY.getShells())
										shell.dispose();
									new InvoiceView(orderId, series);
								}
							}

							@Override
							protected void setListener() {
								super.setListener();
								SelectionListener listener = new SelectionListener() {
									@Override
									public void widgetSelected(SelectionEvent e) {
										text.setTouchEnabled(true);
										text.setFocus();
									}

									@Override
									public void widgetDefaultSelected(SelectionEvent e) {
										text.setTouchEnabled(true);
										text.setFocus();
									}
								};
								combo.addSelectionListener(listener);
								combo.setFocus();
							}
						};
					}
				};
				// Post Invoice Button
				if (orderId == 0)
					btnPost = new PostButton(buttons, reportView, report).getButton();
				// List/New Issued Invoice Booklet Button
				new ImageButton(buttons, module, "Booklet", "Issue/List Invoice Booklet/s") {
					@Override
					protected void doWhenSelected() {
						new InvoiceBookletListView("");
					}
				};
				new ExitButton(buttons, module);
			}
		};
	}

	@Override
	protected void setListener() {
		super.setListener();
		// Booklet Series Input Listener
		new DataInput(txtSeries, txtOrderId) {
			@Override
			protected boolean isInputValid() {
				series = txtSeries.getText().trim();
				if (series.isEmpty()) {
					series = " ";
				}
				if (!new OrderHelper().hasSeries(series)) {
					new ErrorDialog("Booklet Series " + series + "\nhas yet to be issued");
					return false;
				}
				order.setSeries(series);
				txtOrderId.setEnabled(true);
				return true;
			}
		};
		// Invoice # Input Listener
		new DataInput(txtOrderId, txtActual) {
			@Override
			protected boolean isDataInputValid() {
				orderId = Integer.parseInt(string);
				OrderHelper invoice = new OrderHelper(orderId);
				if (invoice.isOnFile(series)) {
					new ErrorDialog("Invoice ID " + orderId + "\nhas been used.");
					txtOrderId.setText("");
					txtSeries.setEnabled(true);
					setNext(txtSeries);
					return true;
				}
				int lastId = invoice.getLastId(series);
				if (lastId == 0) {
					new ErrorDialog("Invoice ID " + orderId + "\nis not in any issued\ninvoice booklet.");
					txtOrderId.setText("");
					txtSeries.setEnabled(true);
					setNext(txtSeries);
					return true;
				}
				if (orderId - lastId > 1) {
					new ErrorDialog("Invoice ID " + (lastId + 1) + "\nmust be used first.");
					txtOrderId.setText("");
					txtSeries.setEnabled(true);
					setNext(txtSeries);
					return true;
				}
				order.setId(orderId);
				setNext(txtActual);
				return true;
			}
		};
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene", "ayin");
		new InvoiceView(0);
		Database.getInstance().closeConnection();
	}
}
