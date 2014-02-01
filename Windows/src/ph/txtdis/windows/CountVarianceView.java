package ph.txtdis.windows;

import java.sql.Date;

import org.eclipse.swt.widgets.Text;

public class CountVarianceView extends PostView implements Subheaderable, Adjustable, Dateable, Postable {

	private Date begin, end;
	private Text adjustment, reason;
	private int bizUnit, tableItemCount;

	public final static int VIEW_ONLY = 1;
	public final int ITEM_ID_COLUMN = 1;
	public final int QC_COLUMN = 3;
	public final int START_COLUMN = 4;
	public final int IN_COLUMN = 5;
	public final int OUT_COLUMN = 6;
	public final int COUNT_COLUMN = 7;
	public final int ADJUSTMENT_COLUMN = 8;
	public final int END_COLUMN = 9;
	public final int VARIANCE_COLUMN = 10;
	public final int REASON_COLUMN = 11;

	public CountVarianceView() {
        Date latest = Count.getLatestDate();
        if(!Count.isClosed(latest)) {
        	new ErrorDialog("Tag " + latest + " as closed\nbefore making any comparisons.");
        	shell.close();
        	new CountReportView(latest);
        	return;
        }
		bizUnit = Count.getLatestCountedBizUnit();
		begin = Count.getLatestReconciledDate(bizUnit);
		end = Count.getLatestDate();
		type = Type.COUNT_VARIANCE;
		data = new CountVariance(new Date[] { begin, end }, bizUnit);
		addHeader();
		addSubheader();
		addTable();
		show();
	}
	
	

	@Override
	protected void addHeader() {
		new Header(this, data) {
			@Override
			protected void layButtons() {
				if (User.isFinance() && !Count.isReconciled(end))
					new ImgButton(buttons, Type.ADJUST, view);
				new ImgButton(buttons, Type.CALENDAR, view);
				if (Count.isReconciled(end))
					new ImgButton(buttons, Type.EXCEL, view);
				else {
					postButton = new ImgButton(buttons, Type.SAVE, view).getButton();
					//postButton.setEnabled(false);
				}
			}
		};
	}

	@Override
	public void addSubheader() {
		new Subheading(shell, (Subheaded) data);
	}

	@Override
	public void adjust() {
		reportTable.disableSorting();
		tableItem = table.getItem(rowIdx);
		adjustment = new TableTextInput(tableItem, ADJUSTMENT_COLUMN, 0).getText();
		new DataInputter(adjustment, reason) {

			@Override
			protected Boolean isNonBlank() {
				setAdjustedNumbers();
				adjustment.dispose();
				setReasonListener();
				return true;
			}

			private void setAdjustedNumbers() {
				tableItem.setText(ADJUSTMENT_COLUMN, textInput);

				int adjustment = Integer.parseInt(textInput);
				int start = UI.extractInt(tableItem, START_COLUMN);
				int in = UI.extractInt(tableItem, IN_COLUMN);
				int out = UI.extractInt(tableItem, OUT_COLUMN);
				int count = UI.extractInt(tableItem, COUNT_COLUMN);
				int end = count + adjustment;
				int variance = end - start - in + out;

				UI.setTableItemText(tableItem, END_COLUMN, end);
				UI.setTableItemText(tableItem, VARIANCE_COLUMN, variance);
			}
		};
		adjustment.setFocus();
	}

	private void setReasonListener() {
		reason = new TableTextInput(tableItem, REASON_COLUMN, "").getText();
		new DataInputter(reason, adjustment) {
			@Override
			protected Boolean isNonBlank() {
				tableItem.setText(REASON_COLUMN, textInput);
				reason.dispose();
				if (tableItemCount == ++rowIdx) {
					postButton.setEnabled(true);
					postButton.setFocus();
				} else {
					table.setTopIndex(rowIdx - 9);
					adjust();
				}
				return true;
			}
		};
		reason.setFocus();
	}

	public Date getEndDate() {
		return end;
	}

	@Override
	public void selectReportDate() {
		 new CountReportSelectedDateAction(shell, (CountData) data);
	}

	@Override
    public Posting getPosting() {
	    return new CountVariancePosting(this);
    }
}