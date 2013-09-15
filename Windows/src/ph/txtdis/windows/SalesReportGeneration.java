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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class SalesReportGeneration {
	final private static boolean PER_ROUTE = true;
	final private static boolean PER_OUTLET = false;
	final private static String STT = "SALES TO TRADE";
	final private static String CALL = "PRODUCTIVE CALLS";
	final private static int RM  = -10;

	public SalesReportGeneration(SalesReport sr) {
		Date[] dates = sr.getDates();
		Date[] datesThs = new Date[2];
		Calendar calThs = Calendar.getInstance();
		calThs.setTime(dates[1]);
		calThs.set(Calendar.DAY_OF_MONTH, 1);
		datesThs[0] = new Date(calThs.getTimeInMillis());
		calThs.set(
				Calendar.DAY_OF_MONTH, calThs.getActualMaximum(Calendar.DAY_OF_MONTH));
		datesThs[1] = new Date(calThs.getTimeInMillis());

		Date[] datesLst = new Date[2];
		Calendar calLst = Calendar.getInstance();
		calLst.setTime(dates[1]);
		calLst.add(Calendar.MONTH, -1);
		calLst.set(Calendar.DAY_OF_MONTH, 1);
		datesLst[0] = new Date(calLst.getTimeInMillis());
		calLst.set(
				Calendar.DAY_OF_MONTH, calLst.getActualMaximum(Calendar.DAY_OF_MONTH));
		datesLst[1] = new Date(calLst.getTimeInMillis());

		int cat = sr.getCategoryId();
		String bu = cat == RM ? "RM" : "Dry";

		Connection conn = null; 
		ResultSet rs = null;
		InputStream is = null;
		PreparedStatement ps = null;
		FileOutputStream fos = null;

		try {
			conn = Database.getInstance().getConnection();
			String string = sr.getModule() + " - " + bu;
			ps = conn.prepareStatement("" +
					"SELECT file " +
					"FROM 	template " +
					"WHERE 	name = ? " +
					"ORDER BY time_stamp DESC " +
					"LIMIT 1");
			ps.setString(1, string);
			rs = ps.executeQuery();
			if(rs.next())
				is = rs.getBinaryStream(1);
			Workbook wb = new HSSFWorkbook(is);
			is.close();
			rs.close();
			ps.close();
			Sheet sheet;
			Row row;
			Integer idx, col;
			Object[][] sttPerRouteThisMonth, sttPerRouteLastMonth; 
			Object[][] sttPerOutletThisMonth, sttperOutletLastMonth;
			Object[][] productivityThisMonth, productivityLastMonth;
			HashMap<Integer, Integer> hmRoute, hmCust, hmPro;

			for (int k = 0; k < wb.getNumberOfSheets(); k++) {
				sheet  = wb.getSheetAt(k);

				row = sheet.getRow(1);
				row.getCell(1).setCellValue(datesThs[0]);
				if (cat != -10) 
					cat = -20 - k - 1;
				
				sttPerRouteThisMonth = new SalesReport(datesThs, STT, cat, PER_ROUTE).
						getData();
				sttPerRouteLastMonth = new SalesReport(datesLst, STT, cat, PER_ROUTE).
						getData();					
				sttPerOutletThisMonth = new SalesReport(datesThs, STT, cat, PER_OUTLET).
						getData();
				sttperOutletLastMonth = new SalesReport(datesLst, STT, cat, PER_OUTLET).
						getData();
				productivityThisMonth = new SalesReport(datesThs, CALL, cat, PER_ROUTE).
						getData();
				productivityLastMonth = new SalesReport(datesLst, CALL, cat, PER_ROUTE).
						getData();

				hmRoute = new HashMap<>(sttPerRouteLastMonth.length);
				hmCust = new HashMap<>(sttperOutletLastMonth.length);
				hmPro = new HashMap<>(productivityLastMonth.length);

				if (sttPerRouteLastMonth != null && sttPerRouteLastMonth[0].length > 2) {
					// populate map of last month's data
					for (int i = 0; i < productivityLastMonth.length; i++) {
						hmRoute.put((Integer) sttPerRouteLastMonth[i][1], i);
						hmPro.put((Integer) productivityLastMonth[i][1], i);
					}
					for (int i = 0; i < sttperOutletLastMonth.length; i++) {
						hmCust.put((Integer) sttperOutletLastMonth[i][1], i);
					}	
					// check if there are data
					if(sttPerRouteThisMonth != null && 
							sttPerRouteThisMonth[0].length > 2) {
						// STT
						for (int i = 0; i < sttPerRouteThisMonth.length; i++) {
							idx = hmRoute.get((Integer) sttPerRouteThisMonth[i][1]);
							if (idx == null)
								continue;
							row = sheet.getRow(i + 4);
							row.getCell(0).setCellValue(
									(String) sttPerRouteThisMonth[i][2]);
							row.getCell(1).setCellValue(
									((BigDecimal) sttPerRouteThisMonth[i][3]).
									doubleValue());
							row.getCell(2).setCellValue(
									((BigDecimal) sttPerRouteLastMonth[idx][3]).
									doubleValue());
							col = 6;
							for (int j = 0; j < sttPerRouteThisMonth[i].length - 4; 
									j++) {
								row.getCell(col).setCellValue(
										((BigDecimal) sttPerRouteThisMonth[i][j + 4]).
										doubleValue());
								row.getCell(col + 1).setCellValue(
										((BigDecimal) sttPerRouteLastMonth[idx][j + 4]).
										doubleValue());
								col += 5;
							}
						}
						// Productivity
						for (int i = 0; i < productivityThisMonth.length; i++) {
							idx = hmPro.get((Integer) productivityThisMonth[i][1]);
							if (idx == null)
								continue;
							row = sheet.getRow(i + 16);
							row.getCell(0).setCellValue(
									(String) productivityThisMonth[i][2]);
							row.getCell(1).setCellValue(
									(Long) productivityThisMonth[i][3]);
							row.getCell(2).setCellValue(
									(Long) productivityLastMonth[idx][3]);
							col = 6;
							for (int j = 0; j < productivityThisMonth[i].length - 4; 
									j++) {
								row.getCell(col).setCellValue(
										(Long) productivityThisMonth[i][j + 4]);
								row.getCell(col + 1).setCellValue(
										(Long) productivityLastMonth[idx][j + 4]);
								col += 5;
							}
						}
						// Top 10 Customer
						for (int i = 0; i < 10; i++) {
							row = sheet.getRow(i + 28);
							row.getCell(0).setCellValue(
									(String) sttPerOutletThisMonth[i][2]);
							row.getCell(1).setCellValue(
									((BigDecimal) sttPerOutletThisMonth[i][3]).
									doubleValue());
							col = 6;
							for (int j = 0; j < sttPerOutletThisMonth[i].length - 4; 
									j++) {
								row.getCell(col).setCellValue(
										((BigDecimal) sttPerOutletThisMonth[i][j + 4]).
										doubleValue());
								col += 5;
							}
							if (hmCust.get(sttPerOutletThisMonth[i][1]) != null) {
								idx = hmCust.get(sttPerOutletThisMonth[i][1]);
								row.getCell(2).setCellValue(
										((BigDecimal) sttperOutletLastMonth[idx][3]).
										doubleValue());
								col = 6;
								for (int j = 0; j < sttPerOutletThisMonth[i].length - 4; 
										j++) {
									row.getCell(col + 1).setCellValue(((BigDecimal) 
											sttperOutletLastMonth[idx][j + 4]).
											doubleValue());
									col += 5;
								}
							}
						}
					}
				}
			}
			// Finish up
			wb.getCreationHelper().createFormulaEvaluator().evaluateAll();
			String fileOut = "" +
					System.getProperty("user.home") + 
					System.getProperty("file.separator") + 
					"MAGNUM.MOR." + bu.toUpperCase() + "." +  
					new SimpleDateFormat("MMM.yyyy").format(datesThs[0]) + ".xls";
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
			//new ErrorDialog(e);
		} finally {
			try {
				if(fos != null) fos.close();
				if(is != null) is.close();
				if(rs != null) rs.close();
				if(ps != null) ps.close();
			} catch (Exception e) {
				new ErrorDialog(e);
			}
		}
	}
}