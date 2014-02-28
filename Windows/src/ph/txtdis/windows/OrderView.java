package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public abstract class OrderView extends InputView implements UomSelectable, ItemQuantifiable {

	public static final int ITEM_COLUMN = 2;
	public static final int ITEM_ID_COLUMN = 1;
	public static final int PRICE_COLUMN = 5;
	public static final int TOTAL_COLUMN = 6;
	public static final int UOM_COLUMN = 3;

	protected int qtyColumnIdx = 4;
	protected OrderData data;
	protected Text partnerIdInput, partnerDisplay, addressDisplay, dateInput, itemIdInput, referenceIdInput;

	private Button tableListButton;
	private Combo routeCombo, uomCombo;
	private TextDisplayBox discount1Box, discount2Box;
	private Text computedTotalDisplay, dueDisplay, qtyInput, totalDiscountDisplay, totalVatableDisplay,
	        totalVatDisplay;

	public OrderView() {
		super();
	}

	public OrderView(OrderData data) {
		super(data);
		this.data = data;
	}

	protected void display() {
		addHeader();
		addSubheader();
		addTable();
		setFooter();
		if (id == 0)
			addListener();
		setFocus();
		show();
	}

	@Override
	protected void addHeader() {
		new Header(this, data) {
			@Override
			protected void layButtons() {
				new ImgButton(buttons, Type.NEW, type);
				new BackwardButton(buttons, data);
				new ImgButton(buttons, Type.OPEN, view);
				new ForwardButton(buttons, data);
				if (((InputData) data).getId() == 0)
					postButton = new ImgButton(buttons, Type.SAVE, view).getButton();
			}
		};
	}

	@Override
	protected abstract void addSubheader();

	protected void setFooter() {
		new Footer(this, data);
		new EncodingDataFooter(shell, this, data);
	}

	@Override
	protected void addListener() {
		new OrderPartnerIdEntry(this, data);
		if (data.isSI())
			new InvoiceDateEntry(this, data);
		else if (data.isSO())
			new SalesDateEntry(this, data);
		else
			new OrderDateEntry(this, data);
	}

	@Override
	protected void setFocus() {
	}

	public void disposeAllTableWidgets(int rowIdx) {
		if (itemIdInput != null)
			itemIdInput.dispose();
		if (routeCombo != null)
			routeCombo.dispose();
		if (uomCombo != null)
			uomCombo.dispose();
		if (qtyInput != null)
			qtyInput.dispose();
		if (tableListButton != null)
			tableListButton.dispose();

		int oldRowIdx = getRowIdx();
		TableItem oldTableItem = table.getItem(oldRowIdx);
		if (rowIdx != oldRowIdx && oldTableItem.getText(TOTAL_COLUMN).isEmpty()) {
			oldTableItem.dispose();

			ArrayList<Integer> itemIds = data.getItemIds();
			ArrayList<Type> uoms = data.getUoms();
			if (itemIds.size() > oldRowIdx) {
				itemIds.remove(oldRowIdx);
				uoms.remove(oldRowIdx);
			}

			ArrayList<BigDecimal> qtys = data.getQtys();
			if (qtys.size() > oldRowIdx)
				qtys.remove(oldRowIdx);

			int i = 0;
			if (table.getItemCount() > oldRowIdx)
				for (TableItem tableItem : table.getItems())
					tableItem.setText(i, String.valueOf(++i));
		}
	}

	@Override
	public void processUomSelection(String selection) {
		int itemId = Math.abs(data.getItemId());
		Date date = data.getDate();

		VolumeDiscount volumeDiscount = new VolumeDiscount();
		BigDecimal volumeDiscountQty = volumeDiscount.getQty(itemId, date);
		BigDecimal volumeDiscountValue = volumeDiscount.getValue(itemId, date);
		BigDecimal qtyPerUOM = QtyPerUOM.getQty(itemId, Type.valueOf(selection));
		BigDecimal price = data.getPrice();
		BigDecimal pricePerUomOfOrder = price.multiply(qtyPerUOM);
		BigDecimal countQtyPerUomIsDiscounted = qtyPerUOM.divideToIntegralValue(volumeDiscountQty);
		BigDecimal discountPerUom = volumeDiscountValue.multiply(countQtyPerUomIsDiscounted);
		BigDecimal discountedPricePerUomOfOrder = pricePerUomOfOrder.subtract(discountPerUom);
		data.setPrice(discountedPricePerUomOfOrder);
		data.setVolumeDiscountQty(volumeDiscountQty);
		data.setVolumeDiscountValue(volumeDiscountValue);
		tableItem.setText(OrderView.PRICE_COLUMN, DIS.formatTo2Places(discountedPricePerUomOfOrder));
		new ItemQtyInput(this, data);
	}

	@Override
	public boolean isEnteredItemQuantityValid(String quantity) {
		return true;
	}

	@Override
	public void processQuantityInput(String quantity, int rowIdx) {

		Label discount1Label = discount1Box.getLabel();
		discount1Label.setText(DIS.formatTo2Places(data.getDiscount1Percent()) + "%");
		discount1Box.getText().setText(DIS.formatTo2Places(data.getDiscount1Total()));

		Label discount2Label = discount2Box.getLabel();
		discount2Label.setText(DIS.formatTo2Places(data.getDiscount2Percent()) + "%");
		discount2Box.getText().setText(DIS.formatTo2Places(data.getDiscount2Total()));

		if (data.isDifferenceOfTotalsAcceptable() || data.isSO() || data.isA_PO() || data.isMaterialTransfer())
			postButton.setEnabled(true);

		tableItem.setText(4, DIS.formatTo2Places(data.getQty()));
		tableItem.setText(6, DIS.formatTo2Places(data.getSubtotal()));
	}

	public Text getAddressDisplay() {
		return addressDisplay;
	}

	public void setAddressDisplay(Text addressDisplay) {
		this.addressDisplay = addressDisplay;
	}

	public Text getComputedTotalDisplay() {
		return computedTotalDisplay;
	}

	public void setComputedTotalDisplay(Text computedTotalDisplay) {
		this.computedTotalDisplay = computedTotalDisplay;
	}

	public Text getDateInput() {
		return dateInput;
	}

	public void setDateInput(Text dateInput) {
		this.dateInput = dateInput;
	}

	public TextDisplayBox getDiscount1Box() {
		return discount1Box;
	}

	public void setDiscount1Box(TextDisplayBox discount1Box) {
		this.discount1Box = discount1Box;
	}

	public TextDisplayBox getDiscount2Box() {
		return discount2Box;
	}

	public void setDiscount2Box(TextDisplayBox discount2Box) {
		this.discount2Box = discount2Box;
	}

	public Text getDueDisplay() {
		return dueDisplay;
	}

	public void setDueDisplay(Text dueDisplay) {
		this.dueDisplay = dueDisplay;
	}

	public Text getItemIdInput() {
		return itemIdInput;
	}

	public void setItemIdInput(Text itemIdInput) {
		this.itemIdInput = itemIdInput;
	}

	public Text getPartnerIdInput() {
		return partnerIdInput;
	}

	public void setPartnerIdInput(Text partnerIdInput) {
		this.partnerIdInput = partnerIdInput;
	}

	public Text getPartnerDisplay() {
		return partnerDisplay;
	}

	public void setPartnerDisplay(Text partnerDisplay) {
		this.partnerDisplay = partnerDisplay;
	}

	public int getQtyColumnIdx() {
		return qtyColumnIdx;
	}

	public void setQtyColumnIdx(int qtyColumnIdx) {
		this.qtyColumnIdx = qtyColumnIdx;
	}

	public void setTableListButton(Button tableListButton) {
		this.tableListButton = tableListButton;
	}

	public Text getTotalDiscountDisplay() {
		return totalDiscountDisplay;
	}

	public void setTotalDiscountDisplay(Text totalDiscountDisplay) {
		this.totalDiscountDisplay = totalDiscountDisplay;
	}

	public Text getTotalVatableDisplay() {
		return totalVatableDisplay;
	}

	public void setTotalVatableDisplay(Text totalVatableDisplay) {
		this.totalVatableDisplay = totalVatableDisplay;
	}

	public Text getTotalVatDisplay() {
		return totalVatDisplay;
	}

	public void setTotalVatDisplay(Text totalVatDisplay) {
		this.totalVatDisplay = totalVatDisplay;
	}
}
