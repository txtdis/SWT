package ph.txtdis.windows;

import java.math.BigDecimal;

import java.sql.Date;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class ProgramView extends ReportView {
	private Program program;
	private Button btnPost;
	private int programId, categoryId;
	private String type, category;
	private String[] types, categories;
	private Combo cmbType, cmbCategory;
	private Text txtProgramId, txtStartDate, txtEndDate, txtRebate, txtOutlet;
	private Table tblRebate, tblTarget;
	private ItemHelper iHelper;
	private ProgramHelper pHelper;
	private TableItem rebateTableItem;
	private int rowIdx;

	public ProgramView(int programId) {
		super();
		this.programId = programId;
		iHelper = new ItemHelper();
		pHelper = new ProgramHelper();
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
		report = program = new Program(programId);
	}

	@Override
	protected void setTitleBar() {
		new ListTitleBar(this, report) {
			@Override
			protected void layButtons() {
				new NewButton(buttons, module).getButton();
				new RetrieveButton(buttons, report).getButton();	
				btnPost = new PostButton(buttons, reportView, report).getButton();
				new ExitButton(buttons, module).getButton();
			}		
		};
	}

	@Override
	protected void setHeader() {		
		Composite header = new Composite(shell, SWT.NO_TRIM);
		header.setLayout(new GridLayout(10, false));
		header.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		// Program ID Input
		txtProgramId = new DataDisplay(header, "ID", programId).getText();
		// Type Selector
		types = new Target().getTargets();
		type = new Target(program.getTypeId()).getType();
		cmbType = new DataSelection(header, types, "TYPE", type).getCombo();
		// Category Selector
		categories = iHelper.getFamilies(2);
		category = new ItemHelper().getFamilyName(program.getCategoryId());
		cmbCategory = new DataSelection(
				header, categories, "CATEGORY", category).getCombo();
		// Start Date Input
		Date startDate = program.getStartDate();
		txtStartDate = new DataEntry(header, "START", startDate).getText();
		// End Date Input
		Date endDate = program.getEndDate();
		txtEndDate = new DataEntry(header, "END", endDate).getText();
	}

	@Override
	protected void setTableBar() {
		String[][] headers = program.getHeaders();

		Group grpRebate = new Group(shell, SWT.NONE);
		grpRebate.setText("REBATE SCHEDULE");
		grpRebate.setLayout(new GridLayout(1, false));
		Object[][] rebateData = program.getRebateData();
		tblRebate = new ReportTable(
				grpRebate, rebateData, headers, "", 22, true).getTable();
		tblRebate.setToolTipText("" +
				"Enter rebate in PHP\n" +
				"per unit of measure.");

		Group grpTarget = new Group(shell, SWT.NONE);
		grpTarget.setText("TARGET PER OUTLET");
		grpTarget.setLayout(new GridLayout(1, false));
		Object[][] targetData = program.getTargetData();
		tblTarget = new ReportTable(
				grpTarget, targetData, headers, "", 210, true).getTable();
		tblTarget.setToolTipText("" +
				"Enter target in reporting\n" +
				"unit of measure.");
	}

	@Override
	protected void setListener() {
		new DataSelector(cmbType, cmbCategory);
		new DataSelector(cmbCategory, txtStartDate) {
			@Override
			protected void act() {
				category = cmbCategory.getText();
				categoryId = iHelper.getFamilyId(category);
				String[] newHeaders = iHelper.getProductLines(categoryId);
				String string;
				int headerLength = newHeaders.length;
				int columnLength = tblRebate.getColumnCount() - 3;
				int longer, shorter;
				if(headerLength > columnLength) {
					longer = headerLength;
					shorter = columnLength;
				} else {
					longer = columnLength;
					shorter = headerLength;
				}
				TableColumn rebateTblCol, targetTblCol;
				for (int i = 0; i < longer; i++) {
					if(shorter == longer) {
						string = newHeaders[i];
						tblRebate.getColumn(i+3).setText(
								StringUtils.center(string, 8));
						tblTarget.getColumn(i+3).setText(
								StringUtils.center(string, 8));
					} else {
						if(shorter == headerLength) {
							if(i < shorter) {
								string = newHeaders[i];
								tblRebate.getColumn(i+3).setText(
										StringUtils.center(string, 8));
								tblTarget.getColumn(i+3).setText(
										StringUtils.center(string, 8));
							} else {
								tblRebate.getColumn(shorter+3).dispose();
								tblTarget.getColumn(shorter+3).dispose();
							}
						} else {
							string = newHeaders[i];
							if(i < shorter) {
								rebateTblCol = tblRebate.getColumn(i+3);
								targetTblCol = tblTarget.getColumn(i+3);
								rebateTblCol.setText(StringUtils.center(string, 8));
								targetTblCol.setText(StringUtils.center(string, 8));
							} else {
								rebateTblCol = new TableColumn(tblRebate, SWT.NONE, i+3);
								targetTblCol = new TableColumn(tblTarget, SWT.NONE, i+3);
								rebateTblCol.setText(StringUtils.center(string, 8));
								targetTblCol.setText(StringUtils.center(string, 8));
								rebateTblCol.pack();
								targetTblCol.pack();
							}
						}
					}
				}
			}
		};
		new DataInput(txtStartDate, txtEndDate){
			@Override
			protected boolean act() {
				String strDate = txtStartDate.getText();
				if(pHelper.hasDateBeenUsed(categoryId, strDate)) {
					new ErrorDialog(strDate + pHelper.ERROR);
					return false;
				}
				return true;
			}
		};
		new DataInput(txtEndDate, txtRebate) {
			@Override
			protected boolean act() {
				String strDate = txtEndDate.getText();
				if(pHelper.hasDateBeenUsed(categoryId, strDate)) {
					new ErrorDialog(strDate + pHelper.ERROR);
					return false;
				}
				cmbCategory.setEnabled(false);
				cmbCategory.setBackground(View.white());
				rebateTableItem = tblRebate.getItem(0);
				txtRebate = new TableInput(
						rebateTableItem, 0, 3, BigDecimal.ZERO).getText();
				setNext(txtRebate);
				new TableEntry(txtRebate, 3, rowIdx, txtOutlet, tblRebate, btnPost, 
						tblTarget, program);
				return true;
			}			
		};
	}

	@Override
	protected void setFocus() {
		if(programId == 0) {
			cmbType.setFocus();			
		} else {
			cmbType.setEnabled(false);
			cmbType.setBackground(View.white());
			cmbCategory.setEnabled(false);
			cmbCategory.setBackground(View.white());
			txtStartDate.setEnabled(false);
			txtEndDate.setEnabled(false);
		}
	}

	public Button getBtnPost() {
		return btnPost;
	}

	public Text getTxtProgramId() {
		return txtProgramId;
	}

	public Combo getCmbType() {
		return cmbType;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public Text getTxtStartDate() {
		return txtStartDate;
	}

	public Text getTxtEndDate() {
		return txtEndDate;
	}

	public Table getTblRebate() {
		return tblRebate;
	}

	public Table getTblTarget() {
		return tblTarget;
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene","ayin");
		new ProgramView(0);
		Database.getInstance().closeConnection();
	}
}
