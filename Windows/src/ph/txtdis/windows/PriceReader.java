package ph.txtdis.windows;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class PriceReader extends Query{
	private Connection conn;
	private PreparedStatement psPrice, psOldPrice, psItemTree, psMaxID;
	private PreparedStatement psQtyPer, psItemMaster;

	public PriceReader() {
		super();
	}

	public boolean set(FileInputStream[] is){
		int matCodeColumn = -1;
		int itemNameColumn = -1;
		int quantityPerColumn = -1;
		int purchasePriceColumn = -1;
		int wholesalePriceColumn = -1;
		int retailPriceColumn = -1;
		int superListPriceColumn = -1; 
		int superSrpPriceColumn = -1; 

		ArrayList<Long> allMatCode = new ArrayList<>();
		ArrayList<String> alsItemName = new ArrayList<>();
		ArrayList<Integer> aliQtyPer = new ArrayList<>();
		ArrayList<Double> aldPurchasePrice = new ArrayList<>();
		ArrayList<Double> aldWholesalePrice = new ArrayList<>();
		ArrayList<Double> aldRetailPrice = new ArrayList<Double>();
		ArrayList<Double> aldSuperListPrice = new ArrayList<>();
		ArrayList<Double> aldSuperSrpPrice = new ArrayList<>();
		ArrayList<Integer> aliCategory = new ArrayList<>();
		ArrayList<Date> aldDate = new ArrayList<>();

		final int PANCAKE = -236;
		final int COFFEE = -235;
		final int MAYO = -234;
		final int NUTRIOIL = -233;
		final int JELLYACE = -232;
		final int RTD = -231;
		final int CHEESE = -224;
		final int NON_REF_MARG = -233;
		final int REF_MARG = -222;
		final int BUTTER = -221;
		final int OTHER_GP = -216;
		final int ULAM = -215;
		final int SISIG = -214;
		final int SAUSAGES = -213;
		final int LUNCHEON = -212;
		final int CORNED = -211;
		final int OTHER_HD = -15;
		final int STAR_HD = -14;
		final int VIDA_HD = -13;
		final int BEEFIES_HD = -12;
		final int TJ_HD = -11;

		int nextID = 0;
		String asOf = "As of ";
		Date date = null;
		try {
			conn = DBMS.getInstance().getConnection();
			//Extract data
			Workbook wb = null;
			for (int j = 0; j < 2; j++) {

				wb = new HSSFWorkbook(is[j]);
			}
			int sheetNum = wb.getNumberOfSheets();
			for (int i = 0; i < sheetNum; i++) {
				Sheet sheet = wb.getSheetAt(i);
				String sheetName = wb.getSheetName(i);
				if (!(	sheetName.contains("NLUZON") || 
						sheetName.contains("VIS") || 
						sheetName.contains("MIN") || 
						sheetName.contains("Sheet"))) {
					matCodeColumn = -1;
					itemNameColumn = -1;
					quantityPerColumn = -1;
					purchasePriceColumn = -1;
					wholesalePriceColumn = -1;
					retailPriceColumn = -1;
					superListPriceColumn = -1;
					superSrpPriceColumn = -1;

					//Get column numbers of needed data
					for (Row row : sheet) {
						for (Cell cell : row) {
							if (cell.getCellType() == Cell.CELL_TYPE_FORMULA
									&& cell.getColumnIndex() == 0
									&& cell.getStringCellValue().contains(asOf)) {
								try {
									date = new Date((new SimpleDateFormat(
											"MMM dd, yyyy").parse(cell
													.getStringCellValue().replace(asOf,
															""))).getTime());
								} catch (ParseException e) {
									new ErrorDialog(e);
								}
							}
							if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
								// Get column index of needed data
								switch (cell.getStringCellValue()) {
									// material code
									case "UNSPSC CODE":
										matCodeColumn = cell.getColumnIndex();
										break;
										// item name
									case "SAP MATERIAL DESCRIPTION":
										itemNameColumn = cell.getColumnIndex();
										break;
										// quantity per
									case "PC":
										if (quantityPerColumn == -1) {
											quantityPerColumn = cell
													.getColumnIndex();
										}
										break;
										// purchase price
									case "PRICE TO DISTRIBUTOR":
										purchasePriceColumn = cell.getColumnIndex();
										break;
										// for PHC-GP tab where there are no SRP group
									case "SUPERMARKET/DOWNLINE PRICE":
									case "SUPERMARKET PRICE":
										wholesalePriceColumn = cell
										.getColumnIndex();
										retailPriceColumn = cell.getColumnIndex() + 2;
										break;
										// for FOODSERVICES PHC
									case "WHOLESALE PRICE W/ VAT ":
										wholesalePriceColumn = cell
										.getColumnIndex();
										retailPriceColumn = cell.getColumnIndex() + 1;
										break;
										// for RM
									case "SUPERMARKET":
										superListPriceColumn = cell
										.getColumnIndex();
										superSrpPriceColumn = cell.getColumnIndex() + 1;
										break;
									case "WET MARKET":
										wholesalePriceColumn = cell
										.getColumnIndex();
										retailPriceColumn = cell.getColumnIndex() + 1;
										break;
								}
							}
						}
					}
					//Get the values
					for (Row row : sheet) {
						for (Cell cell : row) {
							if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC
									&& cell.getColumnIndex() == matCodeColumn) {
								Row currentRow = sheet.getRow(row.getRowNum());
								// Save Date
								aldDate.add(date);
								// save Material Code
								allMatCode.add((long) currentRow.getCell(
										matCodeColumn).getNumericCellValue());
								// save item name
								alsItemName.add(currentRow.getCell(
										itemNameColumn).getStringCellValue());
								// Save Quantity Per (Defaults to 1)
								if (quantityPerColumn < 0) {
									aliQtyPer.add(1);
								} else {
									aliQtyPer.add((int) currentRow.getCell(
											quantityPerColumn)
											.getNumericCellValue());
								}
								// save purchase price
								aldPurchasePrice.add(currentRow.getCell(
										purchasePriceColumn)
										.getNumericCellValue());
								// save wholesale price if present else use market dealer and vice-versa
								if (superListPriceColumn < 0) {
									aldWholesalePrice.add(currentRow.getCell(
											wholesalePriceColumn)
											.getNumericCellValue());
									aldRetailPrice.add(currentRow.getCell(
											retailPriceColumn)
											.getNumericCellValue());
									aldSuperListPrice.add(currentRow.getCell(
											wholesalePriceColumn)
											.getNumericCellValue());
									aldSuperSrpPrice.add(currentRow.getCell(
											retailPriceColumn)
											.getNumericCellValue());
								} else {
									aldSuperListPrice.add(currentRow.getCell(
											superListPriceColumn)
											.getNumericCellValue());
									aldSuperSrpPrice.add(currentRow.getCell(
											superSrpPriceColumn)
											.getNumericCellValue());
									aldWholesalePrice.add(currentRow.getCell(
											superListPriceColumn)
											.getNumericCellValue());
									aldRetailPrice.add(currentRow.getCell(
											superSrpPriceColumn)
											.getNumericCellValue());
								}
								//Determine BU codes via sheet name and item name
								String sCell = currentRow.getCell(
										itemNameColumn).getStringCellValue();
								if (sheetName.equals("PANCAKE")) {
									aliCategory.add(PANCAKE);
								} else if (sheetName.equals("smscci_coffee")) {
									aliCategory.add(COFFEE);
								} else if (sheetName.equals("nutrioil")) {
									aliCategory.add(NUTRIOIL);
								} else if (sheetName.equals("milk_ja")) {
									if (sCell.contains("JA")) {
										aliCategory.add(JELLYACE);
									} else {
										aliCategory.add(RTD);
									}
								} else if (sheetName.equals("bmc_retail")) {
									if (sCell.contains("STAR")
											|| sCell.contains("NON-REF")
											|| sCell.contains("MAG LITE")
											|| sCell.contains("DELICIOUS")) {
										aliCategory.add(NON_REF_MARG);
									} else if (sCell.contains("CHEDDAR")
											|| sCell.contains("MAG CHEEZEE")
											|| sCell.contains("QUICKMELT")
											|| sCell.contains("CHEESE")
											|| sCell.contains("QUESO")
											|| sCell.contains("QUEZO")
											|| sCell.contains("DELICIOUS")) {
										aliCategory.add(CHEESE);
									} else if (sCell.contains("SHORTENING")) {
										aliCategory.add(NUTRIOIL);
									} else if (sCell.contains("MAYONNAISE")
											|| sCell.contains("SANDWICH SPREAD")
											|| sCell.contains("DRESSING")) {
										aliCategory.add(MAYO);
									} else if (sCell.contains("DC")
											|| sCell.contains("DARI CREME")
											|| sCell.contains("QUICKMELT")
											|| sCell.contains("BAKER'S BEST")
											|| sCell.contains("BUTTERCUP")) {
										aliCategory.add(REF_MARG);
									} else {
										aliCategory.add(BUTTER);
									}
								} else if (sheetName.equals("PHC-GP")) {
									if (sCell.contains("CB")
											|| sCell.contains("CORNED")
											|| sCell.contains("CARNE")) {
										aliCategory.add(CORNED);
									} else if (sCell.contains("LUNCHEON")
											|| sCell.contains("LOAF")) {
										aliCategory.add(LUNCHEON);
									} else if (sCell.contains("VIENNA")) {
										aliCategory.add(SAUSAGES);
									} else if (sCell.contains("DELIGHTS")) {
										aliCategory.add(SISIG);
									} else if (sCell.contains("ULAM")) {
										aliCategory.add(ULAM);
									} else {
										aliCategory.add(OTHER_GP);
									}
								} else {
									if (sCell.contains("BEEFIES")) {
										aliCategory.add(BEEFIES_HD);
									} else if (sCell.contains("STAR")) {
										aliCategory.add(STAR_HD);
									} else if (sCell.contains("TJ")) {
										aliCategory.add(TJ_HD);
									} else if (sCell.contains("VIDA")) {
										aliCategory.add(VIDA_HD);
									} else {
										aliCategory.add(OTHER_HD);
									}
								}
							}
						}
					}
				}
			}
			ResultSet rs = null;
			//Get the max item ID number
			String sMaxID = "" + "SELECT MAX(id) + 1 AS nextid "
					+ "FROM item_header";
			psMaxID = conn.prepareStatement(sMaxID);
			rs = psMaxID.executeQuery();
			while (rs.next())
				nextID = rs.getInt("NextID");
			// Add new Items to Item Master
			String sItemMaster = "" + "INSERT INTO item_header "
					+ "(id, name, unspsc_id) " + "VALUES (?, ?, ?)";
			String iQtyPer = "" + "INSERT INTO qty_per "
					+ "(item_id, qty, uom) " + "VALUES (?, ?, ?)";
			String iItemTree = "" + "INSERT INTO item_tree "
					+ "(child_id, parent_id) " + "VALUES (?, ?)";
			String sOldPrice = "" + "WITH t as ( " + "	SELECT 	item_id, "
					+ "			tier_id, " + "			max(start_date) as max_date "
					+ "	FROM price " + "	WHERE start_date <= current_date "
					+ "		AND tier_id = ? " + "	GROUP BY item_id, tier_id "
					+ ")" + "SELECT	im.unspsc_id, " + "		p.price " + "FROM	t, "
					+ "		price AS p, " + "		item_header AS im "
					+ "WHERE	p.item_id = t.item_id "
					+ "	AND	p.item_id = im.id "
					+ "	AND p.start_date = t.max_date "
					+ "	AND p.tier_id = t.tier_id ";
			psOldPrice = conn.prepareStatement(sOldPrice);
			HashMap<Long, Double> hmPrice;
			ArrayList<HashMap<Long, Double>> hms = new ArrayList<>();
			for (int i = 0; i < 5; i++) {
				psOldPrice.setInt(1, i);
				rs = psOldPrice.executeQuery();
				hmPrice = new HashMap<Long, Double>();
				while (rs.next()) {
					hmPrice.put(rs.getLong(1), rs.getDouble(2));
				}
				hms.add(hmPrice);
			}
			String iPrice = "" + "INSERT INTO price "
					+ "(item_id, tier_id, price, start_date) " + "VALUES ("
					+ "	(SELECT id " + "	FROM item_header "
					+ "	WHERE unspsc_id = ?)," + "	?," + "	?," + "	?)";
			HashMap<Long, Integer> hmPurchase = new HashMap<>(allMatCode.size());
			HashMap<Long, Integer> hmWholesale = new HashMap<>(
					allMatCode.size());
			HashMap<Long, Integer> hmRetail = new HashMap<>(allMatCode.size());
			HashMap<Long, Integer> hmSuperList = new HashMap<>(
					allMatCode.size());
			HashMap<Long, Integer> hmSuperSrp = new HashMap<>(allMatCode.size());
			for (int i = 0; i < allMatCode.size(); i++) {
				if (!hms.get(0).containsKey(allMatCode.get(i))) {
					psItemMaster = conn
							.prepareStatement(sItemMaster);
					psItemMaster.setInt(1, ++nextID);
					psItemMaster.setString(2, alsItemName.get(i));
					psItemMaster.setLong(3, allMatCode.get(i));
					psItemMaster.execute();

					psQtyPer = conn.prepareStatement(iQtyPer);
					int qty = aliQtyPer.get(i);
					psQtyPer.setInt(1, nextID);
					psQtyPer.setInt(2, qty);
					psQtyPer.setInt(3, qty == 1 ? 0 : 1);
					psQtyPer.execute();
					if (qty > 1) {
						psQtyPer.setInt(2, 1);
						psQtyPer.setInt(3, 0);
						psQtyPer.execute();
					}

					psItemTree = conn.prepareStatement(iItemTree);
					psItemTree.setInt(1, nextID);
					psItemTree.setInt(2, aliCategory.get(i));
					psItemTree.execute();
				}
				// Post updated prices
				psPrice = conn.prepareStatement(iPrice);
				double price = 0;
				for (int j = 0; j < 5; j++) {
					switch (j) {
						case 0:
							price = Math.round(aldPurchasePrice.get(i) * 100.00) / 100.00;
							if (hmPurchase.containsKey(allMatCode.get(i))) {
								price = 0;
							} else {
								hmPurchase.put(allMatCode.get(i), j);
							}
							break;
						case 1:
							price = Math.round(aldWholesalePrice.get(i) * 100.00) / 100.00;
							if (hmWholesale.containsKey(allMatCode.get(i))) {
								price = 0;
							} else {
								hmWholesale.put(allMatCode.get(i), j);
							}
							break;
						case 2:
							price = Math.round(aldRetailPrice.get(i) * 100.00) / 100.00;
							if (hmRetail.containsKey(allMatCode.get(i))) {
								price = 0;
							} else {
								hmRetail.put(allMatCode.get(i), j);
							}
							break;
						case 3:
							price = Math.round(aldSuperListPrice.get(i) * 100.00) / 100.00;
							if (hmSuperList.containsKey(allMatCode.get(i))) {
								price = 0;
							} else {
								hmSuperList.put(allMatCode.get(i), j);
							}
							break;
						case 4:
							price = Math.round(aldSuperSrpPrice.get(i) * 100.00) / 100.00;
							if (hmSuperSrp.containsKey(allMatCode.get(i))) {
								price = 0;
							} else {
								hmSuperSrp.put(allMatCode.get(i), j);
							}
							break;
					}
					if (price != 0
							&& !(hms.get(j).get(allMatCode.get(i)) != null && price == hms
							.get(j).get(allMatCode.get(i)))) {
						// unspsc_id
						psPrice.setLong(1, allMatCode.get(i));
						// tier_id
						psPrice.setInt(2, j);
						// price
						psPrice.setDouble(3, price);
						// date
						psPrice.setDate(4, aldDate.get(i));
						psPrice.execute();
					}
				}
			}
		} catch (SQLException | IOException e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException er) {
					new ErrorDialog(er);
					return false;
				}
			}
			new ErrorDialog(e);
			return false;
		} finally {
			try {
				if (psPrice != null ) psPrice.close();
				if (psOldPrice != null ) psOldPrice.close();
				if (psItemTree != null ) psItemTree.close();
				if (psMaxID != null ) psMaxID.close();
				if (psQtyPer != null ) psQtyPer.close();
				if (psItemMaster != null ) psItemMaster.close();
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				new ErrorDialog(e);
				return false;
			}
		}
		return true;
	}
}




