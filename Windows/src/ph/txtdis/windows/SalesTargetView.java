package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class SalesTargetView extends OrderView {
	private final static int DEALER_INCENTIVE_ID = 1;
	private final static int FIRST_DATA_COLUMN = 3;
	private final static int FIRST_ROW = 0;
	private final static int NON_DATA_COLUMN_COUNT = 3;
	private final static int PARTNER_ID_COLUMN = 1;
	private final static int PRODUCT_LINE_ID_COLUMN = 1;
	private final static int TABLE_ROW_COUNT = 8;

	private boolean isAtRebateTable, isAtAdditionalRebateTable, isAtTargetTable, isAtLastColumn;
	private int targetTypeId, rowIdx, columnIdx;

	private Combo targetTypeCombo, categoryCombo, gatekeeperCombo;
	private Composite gatekeeper;
	private Group rebateGroup, additionalRebateGroup, targetGroup;
	private SalesTarget target;
	private String[] productLines;
	private String[][] headers;
	private Table rebateTable, additionalRebateTable, targetTable;
	private Text txtProgramId, txtStartDate, txtEndDate, thisText, nextText;

	public SalesTargetView() {
		this(0);
	}

	public SalesTargetView(int id) {
		this(new SalesTarget(id));
	}

	public SalesTargetView(SalesTarget salesTarget) {
		super(salesTarget);
		type = Type.SALES_TARGET;
		display();
	}

	@Override
	protected void addHeader() {
		new Header(this, target) {
			@Override
			protected void layButtons() {
				new ImgButton(buttons, Type.NEW, type);
				new ImgButton(buttons, Type.OPEN, view);
				if (id == 0)
					postButton = new ImgButton(buttons, Type.SAVE, view).getButton();
			}
		};
	}

	@Override
	protected void addSubheader() {
		Composite header = new Composite(shell, SWT.NO_TRIM);
		header.setLayout(new GridLayout(10, false));
		header.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		txtProgramId = new TextDisplayBox(header, "ID", id).getText(); // Type
																	   // Selector
		targetTypeCombo = new ComboBox(header, target.getTargetTypes(), "TYPE").getCombo();
		categoryCombo = new ComboBox(header, target.getCategories(), "CATEGORY").getCombo();
		txtStartDate = new TextInputBox(header, "START", target.getStartDate()).getText();
		txtEndDate = new TextInputBox(header, "END", target.getEndDate()).getText();
		rebateGroup = new Grp(shell, 1, "REBATE SCHEDULE", GridData.FILL_BOTH).getGroup();
		additionalRebateGroup = new Grp(shell, 1, "EXTRA INCENTIVE", GridData.FILL_BOTH).getGroup();
		gatekeeper = new Compo(shell, 2, GridData.BEGINNING).getComposite();
		targetGroup = new Grp(shell, 1, "TARGET PER OUTLET", GridData.FILL_BOTH).getGroup();
	}

	@Override
	public void addTable() {
		headers = target.getTableHeaders();
		rebateTable = new ReportTable(rebateGroup, target.getRebateData(), headers, 30).getTable();
		additionalRebateTable = new ReportTable(additionalRebateGroup, target.getAdditionalRebateData(), headers, 50)
		        .getTable();
		gatekeeperCombo = new ComboBox(gatekeeper, target.getProductLines(), "GATEKEEPER").getCombo();
		targetTable = new ReportTable(targetGroup, target.getTargetData(), headers, 160).getTable();
	}

	@Override
	protected void addListener() {
		new ComboSelector(targetTypeCombo, categoryCombo) {
			@Override
			protected void processSelection() {
				targetTypeId = new Target(selection).getId();
				target.setTargetTypeId(targetTypeId);
				categoryCombo.setFocus();
			}
		};

		new ComboSelector(categoryCombo, txtStartDate) {
			@Override
			protected void processSelection() {
				int categoryId = Item.getFamilyId(selection);
				productLines = Item.getProductLines(categoryId);
				if (categoryId != target.getCategoryId()) {
					target.setCategoryId(categoryId);
					refreshTables();
					center();
				}
				target.setCategory(selection);
			}
		};

		new DataInputter(txtStartDate, txtEndDate) {
			@Override
			protected Boolean isNonBlank() {
				Date date = DIS.parseDate(textInput);
				if (hasDateBeenUsed(date))
					return false;
				target.setStartDate(date);
				return true;
			}
		};

		new DataInputter(txtEndDate, thisText) {
			@Override
			protected Boolean isNonBlank() {
				Date date = DIS.parseDate(textInput);
				if (target.getStartDate().after(date)) {
					new ErrorDialog("Start date cannot be after end");
					return false;
				}
				if (hasDateBeenUsed(date))
					return false;
				target.setEndDate(date);
				tableItem = rebateTable.getItem(0);
				isAtRebateTable = true;
				columnIdx = FIRST_DATA_COLUMN;
				thisText = new TableTextInput(tableItem, columnIdx, BigDecimal.ZERO).getText();
				setNext(thisText);
				addTableItemListener();
				return true;
			}
		};
	}

	private boolean hasDateBeenUsed(Date date) {
		if (target.getTargetTypeId() == DEALER_INCENTIVE_ID) {
			Object[] dates = target.getDatesThatThisFallsWithin(target.getCategoryId(), date);
			if (dates != null) {
				String start = dates[0].toString();
				String end = dates[1].toString();
				new ErrorDialog(date.toString() + " is within a\nDealer's Incentive Program\nfor "
				        + target.getCategory() + "\nfrom " + start + " to " + end);
				return true;
			}
		}
		return false;
	}

	private void addTableItemListener() {
		thisText.setTouchEnabled(true);
		thisText.setFocus();
		if (rowIdx > TABLE_ROW_COUNT)
			tableItem.getParent().setTopIndex(rowIdx - 9);
		if (productLines == null)
			productLines = target.getProductLines();

		new DataInputter(thisText, nextText) {
			@Override
			protected boolean isAnyInput() {
				if (rowIdx > 0 && columnIdx == PARTNER_ID_COLUMN)
					postButton.setEnabled(false);
				return true;
			}

			@Override
			protected Boolean isBlankNot() {
				if (isAtRebateTable) {
					if (productLines.length == 1) {
						new ErrorDialog("At least one column must\nhave data");
						shell.dispose();
						new SalesTargetView();
						return false;
					}
					int dataIdx = columnIdx - NON_DATA_COLUMN_COUNT;
					productLines = ArrayUtils.remove(productLines, dataIdx);
					target.setProductLines(productLines);
					rebateTable.getColumn(columnIdx).dispose();
					targetTable.getColumn(columnIdx).dispose();
					center();

					int productLineCount = productLines.length;
					isAtLastColumn = dataIdx == productLineCount;
					isAtLastColumn();
					goToNextTextInput();
				} else if (isAtTargetTable) {
					postButton.setEnabled(true);
				}
				return false;
			}

			private boolean isAtLastColumn() {
				if (isAtLastColumn) {
					if (isAtAdditionalRebateTable) {
						tableItem = targetTable.getItem(FIRST_ROW);
						isAtAdditionalRebateTable = false;
						columnIdx = PARTNER_ID_COLUMN;
						listButton = new TableButton(tableItem, rowIdx, 0, "Customer List").getButton();
					} else if (isAtRebateTable) {
						tableItem = additionalRebateTable.getItem(FIRST_ROW);
						columnIdx = PRODUCT_LINE_ID_COLUMN;
						new TableCombo(tableItem, columnIdx, target.getProductLines()).getCombo();
						columnIdx = PARTNER_ID_COLUMN;
					}
					return true;
				}
				return false;
			}

			private void goToNextTextInput() {
				thisText.dispose();
				thisText = new TableTextInput(tableItem, columnIdx, BigDecimal.ZERO).getText();
				addTableItemListener();
			}

			@Override
			protected Boolean isPositive() {
				int dataIdx = columnIdx - NON_DATA_COLUMN_COUNT;
				int productLineCount = productLines.length;
				int productLineId;
				tableItem.setText(columnIdx, textInput);
				if (!isAtRebateTable) { // Target Table
					int partnerId = number.intValue();
					if (columnIdx == PARTNER_ID_COLUMN) {
						String partner = Customer.getName(partnerId);
						if (partner == null) {
							new ErrorDialog("Partner #" + partnerId + "\nis not on file");
							return false;
						}
						ArrayList<Integer> partnerIds = target.getOutletIds();
						int lineIdWithThisPartner = partnerIds.indexOf(partnerId) + 1;
						if (lineIdWithThisPartner > 0) {
							new ErrorDialog(partner + "\nis already on line #" + lineIdWithThisPartner);
							return false;
						}
						partnerIds.add(partnerId);
						listButton.dispose();
						postButton.setEnabled(false);
						tableItem.setText(2, partner);
						columnIdx = FIRST_DATA_COLUMN;
					} else {
						productLineId = Item.getFamilyId(productLines[dataIdx]);
						isAtLastColumn = dataIdx == productLineCount - 1;
						if (isAtLastColumn) {
							postButton.setEnabled(true);
							columnIdx = PARTNER_ID_COLUMN;
							tableItem = new TableItem(targetTable, SWT.NONE);
							listButton = new TableButton(tableItem, ++rowIdx, 0, "Customer List").getButton();
						} else {
							++columnIdx;
						}
						target.getTargets().add(new Target(partnerId, productLineId, number));
					}
				} else { // Rebate Table
					productLineId = Item.getFamilyId(productLines[dataIdx]);
					target.getRebates().add(new Rebate(productLineId, number));
					isAtLastColumn = dataIdx == productLineCount - 1;
					if (!isAtLastColumn())
						++columnIdx;
				}
				goToNextTextInput();
				return true;
			}
		};
		// new TableDataInput(txtRebate, 3, rowIdx, txtOutlet, tblRebate,
		// btnPost, tblTarget, target);
	}

	public void addGatekeeperSelector() {
		new ComboSelector(gatekeeperCombo, thisText) {
			@Override
			protected void processSelection() {
				int categoryId = Item.getFamilyId(selection);
				productLines = Item.getProductLines(categoryId);
				if (categoryId != target.getCategoryId()) {
					target.setCategoryId(categoryId);
					refreshTables();
					center();
				}
				target.setCategory(selection);
			}
		};
	}

	@Override
	protected void setFocus() {
		targetTypeCombo.setEnabled(true);
		targetTypeCombo.setFocus();
	}

	public Button getPostButton() {
		return postButton;
	}

	public Text getTxtProgramId() {
		return txtProgramId;
	}

	public Combo getCmbType() {
		return targetTypeCombo;
	}

	public Text getTxtStartDate() {
		return txtStartDate;
	}

	public Text getTxtEndDate() {
		return txtEndDate;
	}

	public Table getTblRebate() {
		return rebateTable;
	}

	public Table getTblTarget() {
		return targetTable;
	}

	private void refreshTables() {
		disposeTables();
		target.setHeaders(productLines);
		addTable();
	}

	private void disposeTables() {
		disposeTable(rebateTable);
		disposeTable(targetTable);
	}

	private void disposeTable(Table table) {
		table.removeAll();
		table.dispose();
	}

	@Override
	public Posting getPosting() {
		return new SalesTargetPosting(target);
	}
}
