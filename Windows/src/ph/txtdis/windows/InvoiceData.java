package ph.txtdis.windows;

public class InvoiceData extends DeliveryData {
	private String series;

	public InvoiceData(int id) {
		this(id, " ");
	}

	public InvoiceData(int id, String series) {
		super(id);
		this.series = series;
	}
	
	@Override
    protected void setProperties() {
		type = Type.INVOICE;
    }

	@Override
    protected void processId(Object id) {
		Object[] ids = (Object[]) id;
		this.id = (int) ids[0];
		series = (String) ids[1];
    }

	public String getSeries() {
		return series;
	}

	public void setSeries(String series) {
		this.series = series;
	}
}
