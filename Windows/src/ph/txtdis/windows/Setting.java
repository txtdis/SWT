package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.SQLException;

public class Setting {
		
	public BigDecimal getVat() throws SQLException {
		return ((BigDecimal) new Data().getDatum("SELECT vat FROM settings"));
	}
}
