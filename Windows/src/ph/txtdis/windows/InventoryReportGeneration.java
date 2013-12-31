package ph.txtdis.windows;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class InventoryReportGeneration extends Report {

	public InventoryReportGeneration(Report report) {
		module = report.getModule();
		Date date = new Date(Calendar.getInstance().getTimeInMillis());
		Object[][] data;
		if (module.contains("Stock Take")) { 
			date = ((StockTake) report).getDate();
			data = new Data().getDataArray(date, "" +
					"SELECT im.unspsc_id, " +
					"		im.name, " +
					"		SUM(cd.qty * qp.qty) AS qty " +
					"FROM	count_header AS ch, " +
					"		count_detail AS cd, " +
					"		item_master AS im, " +
					"		qty_per AS qp " +
					"WHERE	ch.count_id = cd.count_id " +
					"	AND	cd.item_id = im.id " +
					"	AND	cd.item_id = qp.item_id " +
					"	AND cd.uom = qp.uom " +
					"	AND cd.qc_id = 0 " +
					"	AND im.unspsc_id >= 0 " +
					"	AND ch.count_date = ? " +
					"GROUP BY im.unspsc_id," +
					"		im.name " +
					"");
		} else {
			data = new Data().getDataArray("" +
					"WITH " + SQL.addInventoryStmt() +
					"SELECT im.unspsc_id, " +
					"		im.name, " +
					"		i.good\n" +
					"    FROM inventory AS i, item_master AS im\n" +
					"   WHERE i.id = im.id\n" +
					"	  AND im.unspsc_id > 0 " +
					"	  AND i.good > 0" +
					"");
		}
		if (data == null) {
			return;
		}
		HashMap<Long, Integer> hm = new HashMap<>();
		int line;
		Connection conn = null; 
		ResultSet rs = null;
		InputStream is = null;
		PreparedStatement ps = null;
		FileOutputStream fos = null;
		try {
			conn = Database.getInstance().getConnection();
			ps = conn.prepareStatement("" +
					"SELECT file " +
					"FROM template " +
					"WHERE name = ? " +
					"ORDER BY time_stamp DESC " +
					"LIMIT 1"
					);
			ps.setString(1, module.contains("Stock") ? "Inventory" : module);
			rs = ps.executeQuery();
			if(rs.next()) is = rs.getBinaryStream(1);
			Workbook wb = new HSSFWorkbook(is);
			is.close();
			rs.close();
			ps.close();
			Sheet sheet  = wb.getSheet("MAGNUM");
			for (Row row : sheet) {
				Cell cell = row.getCell(8);
				line = cell.getRowIndex();
				if(line < 2) continue;
				if(cell.getCellType() == Cell.CELL_TYPE_BLANK) break;
				hm.put((long) cell.getNumericCellValue(), line);
			}
			int newSkuRow = hm.size() + 4;
			Row row;
			for (Object[] objects : data) {
				Integer i = hm.get(objects[0]);
				if (i == null) {
					row = sheet.getRow(newSkuRow++);
					row.getCell(8).setCellValue((long) objects[0]);
					row.getCell(9).setCellValue((String) objects[1]); 
				} else {
					row = sheet.getRow(i);
				}
				if(objects[2] != null) {
					Class<?> objClass = objects[2].getClass();
					if(objClass.equals(Integer.class)) {
						row.getCell(12).setCellValue((int) objects[2]);
					} else {
						BigDecimal bd = (BigDecimal) objects[2];
						row.getCell(12).setCellValue(bd.doubleValue());
					}
				}
			}
			wb.getCreationHelper().createFormulaEvaluator().evaluateAll();
			String fileOut = "" +
					System.getProperty("user.home") + 
					System.getProperty("file.separator") + 
					"MAGNUM.INV." + DIS.POSTGRES_DATE.format(date) + ".xls";
			fos = new FileOutputStream(fileOut);
			wb.write(fos);
			fos.close();
			//Open file
			String[] cmd;
			if (System.getProperty("os.name").contains("Windows")) {
				cmd = new String[] {"cmd.exe", "/C", fileOut };
			} else {
				cmd = new String[] {"xdg-open", fileOut};
			}
			Runtime.getRuntime().exec(cmd);

		} catch (IOException | SQLException e) {
			e.printStackTrace();
			new ErrorDialog(e);
		} finally {
			try {
				if(fos != null) fos.close();
				if(is != null) is.close();
				if(rs != null) rs.close();
				if(ps != null) ps.close();
			} catch (SQLException | IOException e) {
				e.printStackTrace();
				new ErrorDialog(e);
			}
		}
	}
}