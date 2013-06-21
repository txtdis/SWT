package ph.txtdis.windows;

import java.math.BigDecimal;
import java.util.ArrayList;

import org.eclipse.swt.widgets.TableItem;

public class ProgramSaving {
	private ProgramView view;
	private Program program;

	public ProgramSaving(ProgramView view, Program program) {
		this.view = view;
		this.program = program;
	}

	public Program get() {
		try {
			// Category
			String type = view.getCmbType().getText();
			int typeId = new Target(type).getId();
			program.setTypeId(typeId);
			// Category
			int categoryId = view.getCategoryId();
			program.setCategoryId(categoryId);
			// StartDate
			program.setStartDate(DIS.parseDate(view.getTxtStartDate().getText()));
			// EndDate
			program.setEndDate(DIS.parseDate(view.getTxtEndDate().getText()));
			// ProductLineIds
			String[] productLines = new ItemHelper().getProductLines(categoryId);
			// Rebates
			TableItem rebateItem = view.getTblRebate().getItems()[0]; 
			ArrayList<Rebate> rebateList = new ArrayList<>();
			for (int i = 0; i < productLines.length; i++) {
				//[3-n]value
				int productLineId = new ItemHelper().getFamilyId(productLines[i]);
				String bd = rebateItem.getText(i+3).trim();
				if (!bd.isEmpty()) {
					BigDecimal value = new BigDecimal(bd);
					rebateList.add(new Rebate(productLineId, value));
				}
			}
			program.setRebateList(rebateList);
			// Targets
			TableItem[] targetItems = view.getTblTarget().getItems(); 
			ArrayList<Target> targetList = new ArrayList<>();
			for (int i = 0; i < targetItems.length; i++) {
				//[1]outlet_id, [3-n]qty
				String id = targetItems[i].getText(1);
				if (id.isEmpty()) continue;
				int outletId = Integer.parseInt(id);	
				for (int j = 0; j < productLines.length; j++) {
					int productLineId = new ItemHelper().getFamilyId(productLines[i]);
					String bd = targetItems[i].getText(j + 3).trim();
					if(!bd.isEmpty()) {
						BigDecimal qty = new BigDecimal(bd);
						targetList.add(new Target(outletId, productLineId, qty));
					}
				}
			}
			program.setTargetList(targetList);
			return program;
		} catch (Exception e) {
			e.printStackTrace();
			new ErrorDialog(e);
			return null;
		}
	}
}
