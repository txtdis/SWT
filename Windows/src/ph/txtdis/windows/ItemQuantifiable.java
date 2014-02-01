package ph.txtdis.windows;

public interface ItemQuantifiable {
	public boolean isEnteredItemQuantityValid(String quantity);
	public void processQuantityInput(String quantity, int rowIdx);
}
