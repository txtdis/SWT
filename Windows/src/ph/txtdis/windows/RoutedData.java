package ph.txtdis.windows;

import java.sql.Date;

public abstract class RoutedData extends Data implements Routed, Subheaded {
	protected Integer routeId;
	protected Date start, end;

	public RoutedData() {
	}
		
	public RoutedData(Date[] dates, int routeId) {
		this.routeId = routeId;
		this.dates = dates == null ? new Date[] { DIS.TODAY, DIS.TODAY } : dates;
		start = this.dates[0];
		end = this.dates[1];
	}

	@Override
	public int getRouteId() {
		return routeId;
	}

	@Override
    public String getSubheading() {
		return Route.getName(routeId) + "\nfrom " + DIS.LONG_DATE.format(dates[0]) + " to " + DIS.LONG_DATE.format(dates[1]);
    }
}
