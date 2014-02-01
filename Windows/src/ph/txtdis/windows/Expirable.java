package ph.txtdis.windows;

import java.sql.Date;

public interface Expirable extends QualityTaggable {
	public Date getExpiry();
	public void setExpiry(Date expiry);
}
