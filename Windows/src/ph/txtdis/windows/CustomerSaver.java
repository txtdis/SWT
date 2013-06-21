package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;

import org.eclipse.swt.widgets.TableItem;

public class CustomerSaver {
	private CustomerView view;
	private CustomerMaster cm;

	public CustomerSaver(CustomerView view, CustomerMaster cm) {
		this.view = view;
		this.cm = cm;
	}

	public CustomerMaster get() {
		try {
			cm.setSmsId(view.getTxtSmsId().getText());
			cm.setName(view.getTxtName().getText());
			// Street
			String street = view.getTxtStreet().getText().trim();
			if (street.isEmpty()) street = null;
			cm.setStreet(street);
			//
			cm.setDistrict(view.getCmbDistrict().getText());
			cm.setCity(view.getCmbCity().getText());
			cm.setProvince(view.getCmbProvince().getText());
			cm.setRoute(view.getCmbRoute().getText());
			cm.setChannel(view.getCmbChannel().getText());
			cm.setFirstName(view.getTxtFirstName().getText().trim());
			cm.setSurname(view.getTxtSurname().getText().trim());
			cm.setDesignation(view.getTxtJob().getText().trim());
			// Phone
			String phone = view.getTxtPhone().getText().trim(); 
			if(!phone.isEmpty()) cm.setPhone(Long.parseLong(phone));
			//
			TableItem[] creditItems = view.getTblCredit().getItems(); 
			ArrayList<Credit> creditList = new ArrayList<>();
			for (int i = cm.getCreditData().length; i < creditItems.length; i++) {
				//[1]credit_limit, [2]term, [3]grace_period, [4]start_date
				BigDecimal creditLimit = new BigDecimal(creditItems[i].getText(1));	
				int term = Integer.parseInt(creditItems[i].getText(2));	
				int gracePeriod = Integer.parseInt(creditItems[i].getText(3));
				Date creditStart = new Date(
						DIS.SDF.parse(creditItems[i].getText(4)).getTime());
				creditList.add(new Credit(creditLimit, term, gracePeriod, creditStart));
			}
			cm.setCreditList(creditList);

			TableItem[] discountItems = view.getTblDiscount().getItems();
			ArrayList<PartnerDiscount> discountList = new ArrayList<>();
			for (int i = cm.getDiscountData().length; i < discountItems.length; i++) {
				//[1]family_id, [2]level_1, [3]level_2, [4]start_date
				int familyId = Integer.parseInt(discountItems[i].getText(1));	
				BigDecimal rate1 = new BigDecimal(discountItems[i].getText(2));	
				BigDecimal rate2 = new BigDecimal(discountItems[i].getText(3));		
				Date discountStart = new Date(
						DIS.SDF.parse(discountItems[i].getText(4)).getTime());
				discountList.add(new PartnerDiscount(familyId, rate1, rate2, discountStart));
			}
			cm.setDiscountList(discountList);
			return cm;
		} catch (Exception e) {
			e.printStackTrace();
			new ErrorDialog(e);
			return null;
		}
	}
}
