package ph.txtdis.windows;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class InvoiceView extends DeliveryView {

	private String series;
	private Text idInput, seriesInput;

	public InvoiceView() {
		this(0);
	}

	public InvoiceView(int id) {
		this(id, " ");
	}

	public InvoiceView(int id, String series) {
		super(new InvoiceData(id, series));
	}

	public InvoiceView(OrderData data) {
		super(data);
	}

	@Override
    protected void display() {
		type = Type.INVOICE;
	    super.display();
    }

	@Override
	protected void addHeader() {
		new Header(this, data) {
			@Override
			protected void layButtons() {
				new ImgButton(buttons, Type.NEW, type);
				new ImgButton(buttons, Type.OPEN, view);
				if (id == 0)
					postButton = new ImgButton(buttons, Type.SAVE, view).getButton();
				new ImageButton(buttons, module, "Booklet", "Issue/List Invoice Booklet/s") {
					@Override
					protected void proceed() {
						new InvoiceBookletListView("");
					}
				};
			}
		};
	}

	@Override
    protected void addSubheader() {
		new Subheader(this, (OrderData) data){
			@Override
            protected void setOrderGroup(OrderView view, OrderData data, Group group) {
				referenceIdInput = new TextInputBox(group, "S/O(P/O)#", data.getReferenceId()).getText();
				seriesInput = new TextInputBox(group, "SERIES", ((InvoiceData) data).getSeries(), 1).getText();
				idInput = new TextInputBox(group, "INVOICE #", data.getId()).getText();
				enteredTotalInput = new TextInputBox(group, "S/I AMOUNT", ((DeliveryData) data).getEnteredTotal()).getText();
            }
		};
    }

	@Override
	protected void addListener() {
		super.addListener();
		// Booklet Series Input Listener
		new DataInputter(seriesInput, idInput) {

			@Override
			protected Boolean isBlankNot() {
				series = " ";
				return null;
			}

			@Override
			protected Boolean isNonBlank() {
				if (OrderControl.isOnFile(series))
					return null;
				new ErrorDialog("Booklet Series " + series + "\nhas yet to be issued");
				return false;
			}

			@Override
			protected boolean isAnyInput() {
				((InvoiceData) data).setSeries(series);
				idInput.setEnabled(true);
				return true;
			}
		};

		new DataInputter(idInput, enteredTotalInput) {

			@Override
			protected Boolean isPositive() {
				id = number.intValue();
				if (OrderControl.isOnFile(id, series)) {
					new ErrorDialog("Invoice ID " + id + "\nhas been used.");
					idInput.setText("");
					seriesInput.setEnabled(true);
					setNext(seriesInput);
					return true;
				}

				int lastId = OrderControl.getLastId(id, series);
				if (lastId == 0) {
					new ErrorDialog("Invoice ID " + id + "\nis not in any issued\ninvoice booklet.");
					idInput.setText("");
					seriesInput.setEnabled(true);
					setNext(seriesInput);
					return true;
				}

				if (id - lastId > 1) {
					new ErrorDialog("Invoice ID " + (lastId + 1) + "\nmust be used first.");
					idInput.setText("");
					seriesInput.setEnabled(true);
					setNext(seriesInput);
					return true;
				}

				((InputData) data).setId(id);
				setNext(enteredTotalInput);
				return true;
			}
		};
	}

	public void open() {
		new InputDialog(type.getName()) {
			private Combo combo;

			@Override
			protected void setRightPane() {
				Composite right = new Composite(header, SWT.NONE);
				right.setLayout(new GridLayout(2, false));
				Label label = new Label(right, SWT.CENTER);
				label.setText("Select invoice booklet\n" + "series and enter its ID#");
				label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER, GridData.VERTICAL_ALIGN_CENTER,
				        true, true, 2, 1));
				combo = new Combo(right, SWT.READ_ONLY);
				String[] comboItems = OrderControl.getSeries();
				if (comboItems != null)
					combo.setItems(comboItems);
				combo.select(0);
				combo.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
				text = new Text(right, SWT.BORDER);
				text.setLayoutData(new GridData(GridData.FILL_BOTH));
				text.setBackground(UI.YELLOW);
			}

			@Override
			protected void setOkButtonAction() {
				String strId = text.getText();
				if (StringUtils.isBlank(strId))
					return;
				// retrieve report from id input
				id = Integer.parseInt(strId);
				// check if id is in the system
				series = combo.getText();
				boolean hasId = OrderControl.isOnFile(id, series);
				if (!hasId) {
					new ErrorDialog(module + " #" + id + series + "\n" + "is not in our system.");
					text.setText("");
					combo.setFocus();
					return;
				} else {
					image.getImage().dispose();
					shell.close();
					new InvoiceView(id, series);
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

	@Override
	protected void setFocus() {
		seriesInput.setTouchEnabled(true);
		seriesInput.setFocus();
	}

	public Text getSeriesInput() {
		return seriesInput;
	}

	public void setTxtSeries(Text seriesInput) {
		this.seriesInput = seriesInput;
	}
}
