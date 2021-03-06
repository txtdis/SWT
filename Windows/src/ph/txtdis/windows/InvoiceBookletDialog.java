package ph.txtdis.windows;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class InvoiceBookletDialog extends DialogView {
	private Text txtStartId, txtEndId, txtSeries, txtDate;
	private Combo cmbName;
	private int startId, endId;
	private String series;

	public InvoiceBookletDialog() {
		super(Type.BOOKLET, "");
		display();
	}

	@Override
	protected void setRightPane() {
		Composite right = new Composite(header, SWT.NONE);
		right.setLayout(new GridLayout(2, false));
		String[] employees = Employee.getNames();
		txtStartId 	= new TextInputBox(right, "START ID#", 0).getText();
		txtEndId 	= new TextInputBox(right, "END ID#", 0).getText();
		txtSeries 	= new TextInputBox(right, "SERIES", "", 1).getText();
		cmbName = new ComboBox(right, employees, "ISSUED TO").getCombo();
		txtDate 	= new TextInputBox(right, "DATE", DIS.TODAY).getText();
	}

	@Override
	protected void setOkButtonAction() {
		if(startId != 0) {
			Connection conn = null;
			PreparedStatement ps = null;
			try {
				int employeeId = Contact.getId(cmbName.getText());
				Date date = new Date(DIS.POSTGRES_DATE.parse(txtDate.getText()).getTime());
				conn = DBMS.getInstance().getConnection();
				conn.setAutoCommit(false);
				startId = Integer.parseInt(txtStartId.getText().trim());
				endId = Integer.parseInt(txtEndId.getText().trim());
				series = txtSeries.getText().trim();
				ps = conn.prepareStatement("" +
						"INSERT INTO invoice_booklet " +
						"	(start_id, end_id, series, rep_id, issue_date) " +
						"	VALUES (?, ?, ?, ?, ?)");
				ps.setInt(1, startId);
				ps.setInt(2, endId);
				ps.setString(3, series.isEmpty() ? " " : series);
				ps.setInt(4, employeeId);
				ps.setDate(5, date);
				ps.executeUpdate();
				conn.commit();
			} catch (SQLException | ParseException e) {
				if (conn != null) {
					try {
						conn.rollback();
					} catch (SQLException er) {
						er.printStackTrace();
						new ErrorDialog(er);
					}
				}
				e.printStackTrace();
				new ErrorDialog(e);
			} finally {
				try {
					if (ps != null ) ps.close();
					conn.setAutoCommit(true);
				} catch (SQLException e) {
					e.printStackTrace();
					new ErrorDialog(e);
				}
			}	
		}
		shell.dispose();
		new InvoiceBookletListView("");
	}

	@Override
	protected void setListener() {
		new DataInputter(txtStartId, txtEndId) {
			@Override
			protected Boolean isPositive() {
				startId = number.intValue();
				return true;
			}
		};
		
		new DataInputter(txtEndId, txtSeries) {
			@Override
			protected Boolean isPositive() {
				endId = number.intValue();
				if (endId > startId)
					return true;
				new ErrorDialog("End# must be\ngreater than start#.");
				return false;
			}
		};
		new DataInputter(txtSeries, cmbName) {
			
			@Override
            protected Boolean isBlankNot() {
				series = " ";
	            return null;
            }
			
			@Override
            protected boolean isAnyInput() {
				Object[][] aao = new Query().getTableData(""+
						"WITH id AS (" +
						"	SELECT 	" + startId + " AS start_id, " +
						"		 	" + endId + " AS end_id, " +
						"			cast ('" + series + "' AS text) AS series" +
						")\n" +
						"SELECT ib.start_id,\n" +
						"       ib.end_id,\n" +
						"       ib.series,\n" +
						"       name,\n" +
						"       issue_date\n" +
						"  FROM invoice_booklet AS ib\n" +
						"       INNER JOIN contact_detail AS cd ON ib.rep_id = cd.id\n" +
						"       INNER JOIN id ON ib.series = id.series\n" +
						" WHERE     customer_id = 0\n" +
						"       AND ib.series = id.series\n" +
						"       AND (   (ib.start_id <= id.start_id " +
						"				AND ib.end_id >= id.start_id)\n" +
						"            OR (ib.start_id <= id.end_id " +
						"				AND ib.end_id >= id.end_id)\n" +
						"            OR (ib.start_id >= id.start_id " +
						"				AND ib.end_id <= id.end_id))\n" +						
						"");
				if(aao != null) {
					new ErrorDialog("#s " + startId + "-" + endId +	" are included\n" +
							"in " + aao.length + " previously issued booklet/s.");
					shell.dispose();
					new InvoiceBookletDialog();
				}
				return true;
			}
		};
		new ComboSelector(cmbName, txtDate);
		new DataInputter(txtDate, btnOK);
	}

	@Override
    protected void setFocus() {
		txtStartId.setFocus();
    }
}
