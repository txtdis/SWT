package ph.txtdis.windows;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class PurchaseOrderGeneration {

	public PurchaseOrderGeneration(int id) {
		boolean hasOrder, isExcelOpen = false;
		HSSFWorkbook wb;
		Cell cell;
		Connection conn = null; 
		ResultSet rs = null;
		InputStream is = null;
		PreparedStatement ps = null;
		FileOutputStream fos = null;
		SQL sql = new SQL();
		Date deliveryDate  = (Date) sql.getDatum(id, "" +
				"SELECT purchase_date + lead_time\n" +
				"	 FROM purchase_header AS ph\n" +
				"		  INNER JOIN vendor_specific AS vs\n" +
				"			  ON ph.customer_id = vs.vendor_id\n" +
				"	WHERE ph.purchase_id = ? " +
				"");
		Object[] vendor_specifics = sql.getData("" +
				"SELECT self_id, note FROM vendor_specific;");
		Object[] categories = sql.getData("" +
				"SELECT * FROM purchase_category;");
		HashMap<Long, BigDecimal> data = sql.getMap(id, "" +
				"SELECT im.unspsc_id, pd.qty\n" +
				"    FROM item_master AS im\n" +
				"         INNER JOIN purchase_detail AS pd ON im.id = pd.item_id\n" +
				"         INNER JOIN purchase_header AS ph " +
				"		  	  ON ph.purchase_id = pd.purchase_id\n" +
				"   WHERE ph.purchase_id = ?\n" +
				"ORDER BY im.unspsc_id;\n");
		try {
			conn = Database.getInstance().getConnection();
			for (int i = 0; i < categories.length; i++) {
				ps = conn.prepareStatement("" +
						"SELECT file\n" +
						"    FROM template\n" +
						"   WHERE name = ?\n" +
						"ORDER BY time_stamp DESC\n" +
						"   LIMIT 1\n",
						ResultSet.TYPE_SCROLL_INSENSITIVE,
						ResultSet.CONCUR_READ_ONLY);
				ps.setString(1, "Purchase Order - " + categories[i]);
				rs = ps.executeQuery();
				rs.last();
				if(rs.getRow() != 1) {
					new ErrorDialog("GT " + categories[i] + " TEMPLATE.xls\n" +
							"is missing.\nRe-import all templates before continuing");
					break;
				}
				rs.beforeFirst();
				if(rs.next()) is = rs.getBinaryStream(1);
				wb = new HSSFWorkbook(is);
				is.close();
				rs.close();
				ps.close();
				Sheet sheet = wb.getSheet("UploadSheet");
				hasOrder = false;
				String self_id = (String) vendor_specifics[0];
				String note = (String) vendor_specifics[1];
				for (Row row : sheet) {
					if(row.getRowNum() == 0) continue;
					if(row.getRowNum() == 1) {
						row.getCell(0).setCellValue(self_id);
						row.getCell(1).setCellValue(id);
						row.getCell(3).setCellValue(deliveryDate);
						row.getCell(4).setCellValue(note);
					}
					cell = row.getCell(5); 
					if(cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) break;
					long unspscId = Long.parseLong(cell.getStringCellValue().trim());
					BigDecimal qty = data.get(unspscId); 
					if(qty != null) {
						cell = row.getCell(8);
						if (cell == null) cell = row.createCell(8);
						cell.setCellValue(qty.doubleValue());
						if(!hasOrder) hasOrder = true;							
					}
				}
				if (hasOrder) {
					// Finish up
					wb.getCreationHelper().createFormulaEvaluator().evaluateAll();
					String fileOut = "" +
							System.getProperty("user.home") + 
							System.getProperty("file.separator") + 
							"MAGNUM." + categories[i] + "." + id + ".xls";
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
					if(!isExcelOpen) {
						Thread.sleep(3000);
						isExcelOpen = true;
					}
				}
			}
		} catch (IOException | SQLException | InterruptedException e) {
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