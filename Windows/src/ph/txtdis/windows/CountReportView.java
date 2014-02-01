package ph.txtdis.windows;

import java.sql.Date;

import org.eclipse.swt.widgets.Shell;

public class CountReportView extends ReportView implements Subheaderable, Dateable, Closeable {

	public CountReportView() {
		this(Count.getLatestDate());
	}

	public CountReportView(Date date) {
		this(new CountData(date));
    }

	public CountReportView(CountData data) {
		super(data);
		type = Type.COUNT_REPORT;
		proceed();
	}

	@Override
    protected void proceed() {
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
				new ImgButton(buttons, Type.NEW, type);
				new ImgButton(buttons, Type.OPEN, view);
				new ImgButton(buttons, Type.CALENDAR, view);
				if (!Count.isClosed(data.getDate()))
					new ImgButton(buttons, Type.CLOSE, view);
				new ImgButton(buttons, Type.VARIANCE, Type.COUNT_VARIANCE);
				if (Count.isReconciled(data.getDate()))
					new ImgButton(buttons, Type.EXCEL, view);
			}
		};
	}

	@Override
	public void addSubheader() {
		new Subheading(shell, (Subheaded) data);
	}

	@Override
	public void selectReportDate() {
		 new CountReportSelectedDateAction(shell, (CountData) data);
	}

	@Override
    public void closeTransaction() {
		final Shell parent = shell;
		new WarningDialog("Tagging this date as closed\nmeans no more count input.\n\nAre you sure?") {
			@Override
			protected void setOkButtonAction() {
				((Closeable) data).closeTransaction();
				date = data.getDate();
				if (Count.isClosed(date)) {
					parent.close();
					shell.close();
					new CountReportView(date);
				} else {
					new Error("Tagging failed;\nPlease try again.");
				}
			}
		};
    }
}