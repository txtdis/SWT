package ph.txtdis.windows;

import java.math.BigDecimal;
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
	private LoadSettlement loadSettlement;
	
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

	private void proceed() {
		setProgress();
		setTitleBar();
		setHeader();
		getTable();
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
			report = stockTakeVariance = new StockTakeVariance(dates);
		else
			report = order = stockTake = new StockTake(id);
	}

	@Override
	protected void setTitleBar() {
		new ReportTitleBar(this, report) {
			@Override
			protected void layButtons() {
				if (dates == null) {
					new NewButton(buttons, module);
					new RetrieveButton(buttons, report);
					new ReportGenerationButton(buttons, report);
					if(stockTake.isStockCounted(date) && !stockTake.isCountCompleted(date))
						new CompletionButton(buttons, report);					
				} 
				new CalendarButton(buttons, report);
				new VarianceButton(buttons, report);
				if (id == 0) {
					postButton = new PostButton(buttons, stockTake).getButton();
				}
				new ExcelButton(buttons, report);
				new ExitButton(buttons, module);
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
					final Route route = new Route();
					final String[] routes = route.getList();
					final Date[] dates = new Date[] {DIS.CLOSED_DSR_BEFORE_SO_CUTOFF, date};
	                for (final String routeName : routes) {
		                new ProgressDialog() {
							@Override
							public void proceed() {
								int routeId = route.getId(routeName);
								loadSettlement = new LoadSettlement(dates, routeId);						}
						};
		                if(loadSettlement.getTotalVariance().compareTo(BigDecimal.ZERO) != 0) {
							new ErrorDialog("Complete all Load Settlements\nbefore starting Stock Take");
							new SettlementView(loadSettlement);
							return false;
		                }
	                }

					if (!stockTake.isCountCompleted(date)) {
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
		 Database.getInstance().getConnection("sheryl", "10-8-91", "mgdc_smis");
		//Login.setGroup("super_supply");
		new StockTakeView(0);
		Database.getInstance().closeConnection();
	}

}