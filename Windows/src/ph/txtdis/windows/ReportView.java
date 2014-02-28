package ph.txtdis.windows;

import java.sql.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;

public abstract class ReportView extends View implements ExcelSavable {
	protected Data data;
	protected Date date;
	protected ReportTable reportTable;
	protected Table table;
	
	public ReportView() {
	    super();
	    shell.addListener(SWT.Close, new Listener() {
			@Override
			public void handleEvent(Event event) {
				shell.dispose();
			}
		});
    }

	public ReportView(Data data) {
		this();
		this.data = data;
    }

	@Override
    protected void display() {
		addHeader();
		addTable();
    }

	protected abstract void addHeader();
	
	public void addTable() {
		reportTable = new ReportTable(this, data); 
		table = reportTable.getTable();
	}

	protected void addTotalBar() {
		new ReportTotal(this, data);
	}

	@Override
    public void saveAsExcel() {
		new ProgressDialog() {
			@Override
			public void display() {
				new ExcelWriter(data);
			}
		};
    }

	public Table getTable() {
		return table;
	}
}
