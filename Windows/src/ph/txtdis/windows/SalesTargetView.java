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

	private Combo targetTypeCombo, categoryCombo, productLineCombo, gatekeeperCombo;
	private Composite gatekeeper;
	private Group rebateGroup, additionalRebateGroup, targetGroup;
	private ItemHelper item;
	private SalesTarget target;
	private String[] productLines;
	private String[][] headers;
	private Table rebateTable, additionalRebateTable, targetTable;
	private Text txtProgramId, txtStartDate, txtEndDate, thisText, nextText;

	public SalesTargetView(int targetId) {
		super();
		item = new ItemHelper();
		setProgress();
		setTitleBar();
		setHeader();
		getTable();
		setListener();
		setFocus();
		showReport();
	}

	@Override
	protected void runClass() {
		report = order = target = new SalesTarget(id);
	}

	@Override
	protected void setTitleBar() {
		new ListTitleBar(this, target) {
			@Override
			protected void layButtons() {
				new NewButton(buttons, module).getButton();
				new RetrieveButton(buttons, report).getButton();
				if (id == 0)
					postButton = new PostButton(buttons, target).getButton();
				new ExitButton(buttons, module).getButton();
			}
		};
	}

	@Override
	protected void setHeader() {
		Composite header = new Composite(shell, SWT.NO_TRIM);
		header.setLayout(new GridLayout(10, false));
		header.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		txtProgramId = new TextDisplayBox(header, "ID", id).getText(); // Type Selector
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
	public Table getTable() {
		headers = target.getHeaders();
		rebateTable = new ReportTable(rebateGroup, target.getRebateData(), headers, "", 30, true).getTable();
		additionalRebateTable = new ReportTable(additionalRebateGroup, target.getAdditionalRebateData(), headers, "",
		        50, true).getTable();
		gatekeeperCombo = new ComboBox(gatekeeper, target.getProductLines(), "GATEKEEPER").getCombo();
		targetTable = new ReportTable(targetGroup, target.getTargetData(), headers, "", 160, true).getTable();
		return null;
	}

	@Override
	protected void setListener() {

		new ComboSelector(targetTypeCombo, categoryCombo) {
			@Override
			protected void doAfterSelection() {
				targetTypeId = new Target(selection).getId();
				target.setTargetTypeId(targetTypeId);
				categoryCombo.setFocus();
			}
		};

		new ComboSelector(categoryCombo, txtStartDate) {
			@Override
			protected void doAfterSelection() {
				int categoryId = item.getFamilyId(selection);
				productLines = item.getProductLines(categoryId);
				if (categoryId != target.getCategoryId()) {
					target.setCategoryId(categoryId);
					getNewTable();
					centerShell();
				}
				target.setCategory(selection);
			}
		};

		new DateInputter(txtStartDate, txtEndDate) {
			@Override
			protected boolean isTheDataInputValid() {
				if (hasDateBeenUsed(date))
					return false;
				target.setStartDate(date);
				return true;
			}
		};

		new DateInputter(txtEndDate, thisText) {
			@Override
			protected boolean isTheDataInputValid() {
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
				thisText = new TableTextInput(tableItem, rowIdx, columnIdx, BigDecimal.ZERO).getText();
				setNext(thisText);
				setTableItemListener();
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

	private void setTableItemListener() {
		thisText.setTouchEnabled(true);
		thisText.setFocus();
		if (rowIdx > TABLE_ROW_COUNT)
			tableItem.getParent().setTopIndex(rowIdx - 9);
		if (productLines == null)
			productLines = target.getProductLines();

		new TextInputter(thisText, nextText) {
			@Override
			protected boolean isInputValid() {
				if (rowIdx > 0 && columnIdx == PARTNER_ID_COLUMN)
					postButton.setEnabled(false);
				return super.isInputValid();
			}

			@Override
			protected boolean isABlankInputNotValid() {
				if (isAtRebateTable) {
					if (productLines.length == 1) {
						new ErrorDialog("At least one column must\nhave data");
						shell.dispose();
						new SalesTargetView(0);
						return super.isABlankInputNotValid();
					}
					int dataIdx = columnIdx - NON_DATA_COLUMN_COUNT;
					productLines = ArrayUtils.remove(productLines, dataIdx);
					target.setProductLines(productLines);
					rebateTable.getColumn(columnIdx).dispose();
					targetTable.getColumn(columnIdx).dispose();
					centerShell();

					int productLineCount = productLines.length;
					isAtLastColumn = dataIdx == productLineCount;
					isAtLastColumn();
					goToNextTextInput();
				} else if (isAtTargetTable) {
					postButton.setEnabled(true);
				}
				return super.isABlankInputNotValid();
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
						productLineCombo = new TableCombo(tableItem, columnIdx, target.getProductLines())
						        .getCombo();
						columnIdx = PARTNER_ID_COLUMN;
					}
					return true;
				}
				return false;
			}

			private void goToNextTextInput() {
				thisText.dispose();
				thisText = new TableTextInput(tableItem, rowIdx, columnIdx, BigDecimal.ZERO).getText();
				setTableItemListener();
			}

			@Override
			protected boolean isThePositiveNumberValid() {
				int dataIdx = columnIdx - NON_DATA_COLUMN_COUNT;
				int productLineCount = productLines.length;
				int productLineId;
				tableItem.setText(columnIdx, textInput);
				if (!isAtRebateTable) { // Target Table
					int partnerId = numericInput.intValue();
					if (columnIdx == PARTNER_ID_COLUMN) {
						String partner = new Customer().getName(partnerId);
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
						productLineId = item.getFamilyId(productLines[dataIdx]);
						isAtLastColumn = dataIdx == productLineCount - 1;
						if (isAtLastColumn) {
							postButton.setEnabled(true);
							columnIdx = PARTNER_ID_COLUMN;
							tableItem = new TableItem(targetTable, SWT.NONE);
							listButton = new TableButton(tableItem, ++rowIdx, 0, "Customer List").getButton();
						} else {
							++columnIdx;
						}
						target.getTargets().add(new Target(partnerId, productLineId, numericInput));
					}
				} else { // Rebate Table
					productLineId = item.getFamilyId(productLines[dataIdx]);
					target.getRebates().add(new Rebate(productLineId, numericInput));
					isAtLastColumn = dataIdx == productLineCount - 1;
					if (!isAtLastColumn())
						++columnIdx;
				}
				System.out.println("row: " + rowIdx);
				goToNextTextInput();
				return true;
			}
		};
		// new TableDataInput(txtRebate, 3, rowIdx, txtOutlet, tblRebate, btnPost, tblTarget, target);
	}

	private void setGatekeeperSelector() {
		new ComboSelector(gatekeeperCombo, thisText) {
			@Override
			protected void doAfterSelection() {
				int categoryId = item.getFamilyId(selection);
				productLines = item.getProductLines(categoryId);
				if (categoryId != target.getCategoryId()) {
					target.setCategoryId(categoryId);
					getNewTable();
					centerShell();
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

	private void getNewTable() {
		rebateTable.removeAll();
		rebateTable.dispose();
		targetTable.removeAll();
		targetTable.dispose();
		target.setHeaders(productLines);
		getTable();
	}

	public static void main(String[] args) {
		Database.getInstance().getConnection("irene", "ayin", "localhost");
		new SalesTargetView(0);
		Database.getInstance().closeConnection();
	}
}
