package ph.txtdis.windows;

import java.sql.Date;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

public class StockTakeView extends ReceivingView {
	private Combo takerCombo, checkerCombo;
	private Date date;
	private Date[] dates;
	private StockTake stockTake;
	private StockTakeVariance stockTakeVariance;
	private StockTakeView stockTakeView;
	
	public StockTakeView(int id) { 
		super();
		this.id = id;
		proceed();
	}

	public StockTakeView(Date date) {
		this.date = date;
		id = -1;
		proceed();
	}

	public StockTakeView(Date[] dates) {
		this.dates = dates;
		id = -1;
		proceed();
	}

	public StockTakeView(StockTakeVariance stockTakeVariance) {
		dates = stockTakeVariance.getDates();
		id = -1;
		proceed();
	}

	private void proceed() {
		setProgress();
		setTitleBar();
		setHeader();
		getTable();
		setFooter();
		if (id == 0)
			setListener();
		setFocus();
		showReport();
	}

	@Override
	protected void runClass() {
		if (date != null)
			report = order = stockTake = new StockTake(date);
		else if (dates != null)
			report = order = stockTake = stockTakeVariance = new StockTakeVariance(dates);
		else if (id < 0)
			report = order = stockTake = new StockTakeAdjustment(0);
		else
			report = order = stockTake = new StockTake(id);
		System.out.println("report" + report.getData().length);
		System.out.println("order" + order.getData().length);
		System.out.println("stock" + stockTake.getData().length);
	}

	@Override
	protected void setTitleBar() {
		new ReportTitleBar(this, report) {
			@Override
			protected void layButtons() {
				if (dates == null) {
					new NewButton(buttons, module);
					new OpenButton(buttons, report);
				} else if (User.isFinance()) {
					new ImageButton(buttons, module, "Adjustment", "Adjust final count") {
						@Override
                        protected void doWhenSelected() {
							final int ADJUSTMENT = -1; 
							new StockTakeView(ADJUSTMENT);
                        }
					};
				} 
				new CalendarButton(buttons, report);
				if (module.equals("Stock Take")) {
					new ReportGenerationButton(buttons, report);
					new VarianceButton(buttons, report);
				}
				if (module.equals("Stock Take") && (stockTake.isDone(date) && !stockTake.isDataEntryClosed(date)) || 
						(module.contains("Reconciliation") && !stockTakeVariance.isComplete(date)))
					new CompletionButton(buttons, stockTake);				
				if (id == 0)
					postButton = new PostButton(buttons, stockTake).getButton();
				new ExcelButton(buttons, report);
			}
		};
	}

	@Override
	protected void setHeader() {
		if (date != null) {
 			new ReportHeaderBar(shell, stockTake);
		} else if (dates != null) {
			new ReportHeaderBar(shell, stockTakeVariance);
		} else {
			Composite info = new Compo(shell, 2, GridData.FILL_HORIZONTAL).getComposite();

			Composite left = new Compo(info, 4, GridData.FILL_HORIZONTAL).getComposite();
			takerCombo = new ComboBox(left, stockTake.getTakers(), "TAKER").getCombo();
			locationCombo = new ComboBox(left, stockTake.getLocations(), "LOCATION").getCombo();
			checkerCombo = new ComboBox(left, stockTake.getCheckers(), "CHECKER").getCombo();

			Composite detail = new Compo(info, 2, GridData.FILL_VERTICAL).getComposite();
			dateInput = new TextInputBox(detail, "DATE", stockTake.getDate()).getText();
			new TextDisplayBox(detail, "TAG", stockTake.getId()).getText();
		}
	}
	
	@Override
    protected void setFooter() {
		if(dates == null && date == null)
			super.setFooter();
    }

	@Override
	protected void setListener() {
		stockTakeView = this;
		
		if (id == 0) {
			new ComboSelector(takerCombo, checkerCombo) {
				@Override
				protected void doAfterSelection() {
					stockTake.setTakerId(new Employee(selection).getId());
				}
			};

			new ComboSelector(checkerCombo, locationCombo) {
				@Override
				protected void doAfterSelection() {
					stockTake.setCheckerId(new Employee(selection).getId());
				}
			};

			new ComboSelector(locationCombo, dateInput) {
				@Override
				protected void doAfterSelection() {
					stockTake.setLocationId(new Location(selection).getId());
				}
			};

			new DateInputter(dateInput, itemIdInput) {
				@Override
				protected boolean isTheDataInputValid() {
					if (!stockTake.isDataEntryClosed(date)) {
						order.setDate(date);
						new StockTakeItemIdEntry(stockTakeView, stockTake);
						return true;
					} else {
						String countDate = textInput;
						new ErrorDialog("Data entry has been closed for\nStock Take on " + countDate);
						return false;
					}
				}
			};
		}
	}

	@Override
	protected void setFocus() {
		if (id == 0) {
			takerCombo.setEnabled(true);
			takerCombo.setFocus();
		}
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("marivic", "marvic", "mgdc_smis");
		Login.setGroup("user_finance");
		new StockTakeView(DIS.YESTERDAY);
		Database.getInstance().closeConnection();
	}
}