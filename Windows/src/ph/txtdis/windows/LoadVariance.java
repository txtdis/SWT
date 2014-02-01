package ph.txtdis.windows;

import java.sql.Date;

public class LoadVariance {
	
	private LoadSettlement loadSettlement;
	private String[] routes;
	private Date[] dates;

	public LoadVariance() {
		routes = Route.getList();
		dates = new Date[] {DIS.CLOSED_DSR_BEFORE_SO_CUTOFF, DIS.YESTERDAY};
	}

	public boolean isSettled() {
        for (final String routeName : routes) {
            new ProgressDialog() {
				@Override
				public void proceed() {
					int routeId = Route.getId(routeName);
					loadSettlement = new LoadSettlement(dates, routeId);						}
			};
            if(!DIS.isZero(loadSettlement.getTotalVariance())) {
				new ErrorDialog("Incomplete load settlements\ngive inaccurate report;\nthus, none will be generated.");
				new SettlementView(loadSettlement);
				return false;
            }
        }
        return true;
	}
}
