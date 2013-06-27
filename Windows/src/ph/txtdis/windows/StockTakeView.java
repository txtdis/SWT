package ph.txtdis.windows;

import java.sql.Date;
import java.util.Calendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class StockTakeView extends ReportView {
	private StockTakeView view;
	private StockTake stockTake;
	private StockTakeVariance stockTakeVariance;
	private Combo cmbLocation, cmbTaker, cmbChecker, cmbUom, cmbQc;
	private Text txtOrderId, txtItemId, txtDate, txtQty, txtExpiry;
	private Button btnPost, btnList, btnItemId;
	private Date date;
	private Date[] dates;
	private int id, rowIdx;

	public StockTakeView(int id) {
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
		setTableBar();
		setListener();
		setFocus();
		showReport();
	}

	@Override
	protected void runClass() {
		if(date != null) 
			report = stockTake = new StockTake(date);
		else if (dates != null)
			report = stockTakeVariance = new StockTakeVariance(dates);
		else
			report = stockTake = new StockTake(id); 			
	}

	@Override
	protected void setTitleBar() {
		new ListTitleBar(this, report) {
			@Override
			protected void layButtons() {
				if(dates == null) {
					new NewButton(buttons, module);
					new RetrieveButton(buttons, report);
					new ReportGenerationButton(buttons, report);
				}
				new CalendarButton(buttons, report);
				new VarianceButton(buttons, report);
				if(id == 0) 
					btnPost = new PostButton(buttons, reportView, report).getButton();
				new ExcelButton(buttons, report);
				new ExitButton(buttons, module);
			}
		};
	}

	@Override
	protected void setHeader() {
		if(date != null) {
			new ReportHeaderBar(shell, stockTake);
		} else if(dates != null) {
			new ReportHeaderBar(shell, stockTakeVariance);
		} else {
			Composite cmpInfo = new Composite(shell, SWT.NO_TRIM);
			cmpInfo.setLayout(new GridLayout(2, false));
			cmpInfo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			/// LEFT GROUP
			Group grpLeft = new Group(cmpInfo, SWT.NONE);
			grpLeft.setLayout(new GridLayout(4, false));
			grpLeft.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			Employee emp = new Employee();
			// Stock Taker Selector
			cmbTaker = new DataSelection(
					grpLeft, emp.getEmployees(), "TAKER", null).getCombo();
			// Warehouse Location Selector
			String[] locations = new Location().getLocations();
			cmbLocation = new DataSelection(
					grpLeft, locations, "LOCATION", new Location(stockTake.getLocationId()).getName()).getCombo();
			// Stock Checker Selector
			cmbChecker = new DataSelection(
					grpLeft, emp.getEmployees(), "CHECKER", null).getCombo();

			/// DETAIL SUBGROUP
			Group grpDetail = new Group(cmpInfo, SWT.NONE);
			grpDetail.setLayout(new GridLayout(2, false));
			grpDetail.setLayoutData(new GridData(GridData.FILL_VERTICAL));
			txtDate = new DataEntry(
					grpDetail, "DATE", stockTake.getPostDate()).getText();
			txtOrderId = new DataDisplay(grpDetail, "TAG", stockTake.getId()).getText();
		}
	}

	@Override
	protected void setListener() {
		if(id >= 0) {
			view = this;
			new DataSelector(cmbTaker, cmbChecker);
			new DataSelector(cmbChecker, cmbLocation);
			new DataSelector(cmbLocation, txtDate);
			new DataInput(txtDate, txtItemId) {
				@Override
				protected boolean act() {
					boolean test = true;
					if(test) {
						new TableItem(table, SWT.NONE, rowIdx); 
						new StockTakeLineItem(view, stockTake, rowIdx);
						setNext(txtItemId);
						return true; 
					} else {
						String countDate = string;
						new ErrorDialog("" +
								"Data entry has been closed for\n" +
								"stock take conducted on " + countDate + "\n" +
								"by " + "user" + " on " +  "date");
						return false;
					}
				}
			};
		}
	}

	@Override
	protected void setFocus() {
		if(id == 0) cmbTaker.setFocus();
	}

	public TableItem getTableItem(int rowIdx) {
		return table.getItem(rowIdx);
	}

	public Button getBtnPost() {
		return btnPost;
	}

	public Button getBtnList() {
		return btnList;
	}
	public Combo getCmbLocation() {
		return cmbLocation;
	}

	public Combo getCmbTaker() {
		return cmbTaker;
	}

	public Combo getCmbChecker() {
		return cmbChecker;
	}

	public Text getTxtDate() {
		return txtDate;
	}

	public Text getTxtOrderId() {
		return txtOrderId;
	}

	public Button getBtnItemId() {
		return btnItemId;
	}

	public void setBtnItemId(Button btnItemId) {
		this.btnItemId = btnItemId;
	}

	public Text getTxtItemId() {
		return txtItemId;
	}

	public void setTxtItemId(Text txtItemId) {
		this.txtItemId = txtItemId;
	}

	public Combo getCmbUom() {
		return cmbUom;
	}

	public void setCmbUom(Combo cmbUom) {
		this.cmbUom = cmbUom;
	}

	public Combo getCmbQc() {
		return cmbQc;
	}

	public void setCmbQc(Combo cmbQc) {
		this.cmbQc = cmbQc;
	}

	public Text getTxtQty() {
		return txtQty;
	}

	public void setTxtQty(Text txtQty) {
		this.txtQty = txtQty;
	}

	public Text getTxtExpiry() {
		return txtExpiry;
	}

	public void setTxtExpiry(Text txtExpiry) {
		this.txtExpiry = txtExpiry;
	}

	public static void main(String[] args) {
//		Database.getInstance().getConnection("irene","ayin");
		Database.getInstance().getConnection("sheryl","10-8-91");
		Login.group = "super_supply";
		Date[] dates = new Date[2];
		Calendar cal = Calendar.getInstance();
		cal.set(2013, Calendar.MAY, 9);
		dates[0] = new Date(cal.getTimeInMillis());
		cal.set(2013, Calendar.MAY, 11);
		dates[1]= new Date(cal.getTimeInMillis());
		new StockTakeView(dates);
		Database.getInstance().closeConnection();
	}

}