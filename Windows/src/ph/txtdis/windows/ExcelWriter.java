package ph.txtdis.windows;

import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

public class ExcelWriter {
	public ExcelWriter(Report report) {
		this(report, null, null, false);
	}

	public ExcelWriter(String[]header, Object[][] data) {
		this(null, header, data, false);
	}

	public ExcelWriter(Report report, String[] header, Object[][] data, boolean withCheckBox) {
		String module = "Data Dump";
		String title = null;
		String[][] headers = null;
		int colCount;		

		if (report != null) {
			module = report.getModule();
			headers = report.getHeaders();
			data = report.getData();
			title = report.getHeader();
			colCount = headers.length;
		} else if (data != null) { 
			colCount = data[0].length;
		} else {
			return;
		}

		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet(module.replace("/", "."));

		DataFormat format = wb.createDataFormat();
		CreationHelper createHelper = wb.getCreationHelper();

		// Create title font
		Font fontTitle = wb.createFont();
		fontTitle.setFontName("Calibri");
		fontTitle.setBoldweight(Font.BOLDWEIGHT_BOLD);
		fontTitle.setFontHeightInPoints((short) 12);

		// Create bold font
		Font fontBold = wb.createFont();
		fontBold.setFontName("Consolas");
		fontBold.setBoldweight(Font.BOLDWEIGHT_BOLD);
		fontBold.setFontHeightInPoints((short) 10);

		// Create normal font
		Font font = wb.createFont();
		font.setFontName("Consolas");
		font.setFontHeightInPoints((short) 10);

		// Create Title Style
		CellStyle styleTitle = wb.createCellStyle(); 
		styleTitle.setFont(fontTitle);
		styleTitle.setAlignment(CellStyle.ALIGN_CENTER);
		styleTitle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		styleTitle.setWrapText(true);

		// Create Header Style
		CellStyle style = wb.createCellStyle(); 
		style.setBorderRight(CellStyle.BORDER_THIN);
		style.setBorderLeft(CellStyle.BORDER_THIN);
		style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
		style.setFillPattern(CellStyle.SOLID_FOREGROUND);
		style.setFont(fontBold);
		style.setAlignment(CellStyle.ALIGN_CENTER);

		// Create Integer Style
		CellStyle styleInt = wb.createCellStyle(); 
		styleInt.setFont(font);
		styleInt.setDataFormat(format.getFormat("#,##0"));
		styleInt.setAlignment(CellStyle.ALIGN_RIGHT);

		// Create ID Style
		CellStyle styleId = wb.createCellStyle(); 
		styleId.setFont(font);
		styleId.setDataFormat(format.getFormat("###0"));
		styleId.setAlignment(CellStyle.ALIGN_RIGHT);

		// Create Decimal Style
		CellStyle styleNum = wb.createCellStyle(); 
		styleNum.setFont(font);
		styleNum.setDataFormat(format.getFormat("#,##0.00"));
		styleNum.setAlignment(CellStyle.ALIGN_RIGHT);

		// Create String Style
		CellStyle styleStr = wb.createCellStyle(); 
		styleStr.setFont(font);
		styleStr.setAlignment(CellStyle.ALIGN_LEFT);

		// Create Date Style
		CellStyle styleDate = wb.createCellStyle(); 
		styleDate.setFont(font);
		styleDate.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd"));
		styleDate.setAlignment(CellStyle.ALIGN_CENTER);

		Row row = sheet.createRow(0);
		Cell cell;
		int idx = 1;
		if (title != null) {
			cell = row.createCell(0);
			cell.setCellValue(title);
			cell.setCellStyle(styleTitle);
			sheet.addMergedRegion(new CellRangeAddress(
					0,
					0,	  
					0,			  
					colCount - 1
					));
			float newLineCount = StringUtils.countMatches(title, "\n");
			if (newLineCount == 0) newLineCount = 1;
			row.setHeightInPoints(24 * newLineCount);
			row = sheet.createRow(idx++);
		}
		if(headers != null) {
			for (int i = 0; i < colCount; i++) {
				cell = row.createCell(i);
				cell.setCellValue(headers[i][0]);
				cell.setCellStyle(style);
			}
		} else if(header != null){
			for (int i = 0; i < colCount; i++) {
				cell = row.createCell(i);
				cell.setCellValue(header[i]);
			}			
		}
		sheet.createFreezePane(
				0, 
				idx, 
				0, 
				idx
				);
		style = wb.createCellStyle(); 
		int length = data == null ? 0 : data.length;
		for (int y = 0; y < length; y++) {
			row = sheet.createRow(y + idx);
			int b = withCheckBox ? 1 : 0;
			String colDataType;
			for (int x = b;  x < colCount; x++ ) {
				cell = row.createCell(x-b);
				if (data[y][x] == null) {
					cell.setCellValue("");
					continue;
				}
				if(headers != null) 
					colDataType = headers[x-b][1];
				else 
					colDataType = data[y][x].getClass().getSimpleName();
				switch (colDataType) {
					case "Line":
					case "ID":
						if(data[y][x].getClass().equals(Long.class)) {
							cell.setCellValue((Long) data[y][x]);
						} else {
							cell.setCellValue((Integer) data[y][x]);
						}
						cell.setCellStyle(styleId);
						break;
					case "Long":
						cell.setCellStyle(styleInt);
						break;
					case "Integer":
						cell.setCellValue((Integer) data[y][x]);
						cell.setCellStyle(styleId);
						break;
					case "Quantity":
					case "BigDecimal":
						cell.setCellValue(((BigDecimal) data[y][x]).doubleValue());
						cell.setCellStyle(styleNum);
						break;
					case "String":
						cell.setCellValue((String) data[y][x]);
						cell.setCellStyle(styleStr);
						break;
					case "Date":
						cell.setCellValue((Date) data[y][x]);
						cell.setCellStyle(styleDate);
						break;
					default:
						cell.setCellValue(colDataType);
						cell.setCellStyle(styleStr);
				}
			}
		}

		for (int i = 0; i < colCount; i++) {
			sheet.autoSizeColumn(i);
		}

		//Save file in system temporary directory
		try {
			String file = System.getProperty("user.home") 
					+ System.getProperty("file.separator") 
					+ module.replace("/", ".") + ".xls";	
			FileOutputStream fileOut = new FileOutputStream(file);
			wb.write(fileOut);
			fileOut.close();
			//Open file
			String[] cmd;
			if (System.getProperty("os.name").contains("Windows")) {
				cmd = new String[] {"cmd.exe", "/C", file };
			} else {
				cmd = new String[] {"xdg-open", file};
			}
			Runtime.getRuntime().exec(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}