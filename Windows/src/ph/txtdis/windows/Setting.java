package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.SQLException;

public class Setting {
		
	public BigDecimal getVat() throws SQLException {
		return ((BigDecimal) new SQL().getDatum("SELECT vat FROM settings"));
	}
}
