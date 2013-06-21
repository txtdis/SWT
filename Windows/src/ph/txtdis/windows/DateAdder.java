package ph.txtdis.windows;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateAdder {
	private static Calendar due = Calendar.getInstance();

	public DateAdder(String date) {
		try {
			due.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(date));
		} catch (ParseException e) {
			System.err.println(e);
		}
	}
	
	public DateAdder(Date date){
		due.setTime(date);
	}
	
	public DateAdder(){
		due = Calendar.getInstance();
	}
	
	public Date plus(int days){
		due.add(Calendar.DATE, days);
		return new Date(due.getTimeInMillis());
	}
	
	public String add(int days){
		due.add(Calendar.DATE, days);
		return new SimpleDateFormat("yyyy-MM-dd").format(due.getTime());
	}
}
