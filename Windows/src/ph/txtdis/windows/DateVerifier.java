package ph.txtdis.windows;


import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class DateVerifier {

	private Calendar newcal, endcal;
	private String end;

	public DateVerifier (final Text text) {
		end = text.getText();
		String[] maxi = end.split("-"); 
		int yyyy = Integer.parseInt(maxi[0]);
		int mm = Integer.parseInt(maxi[1]) - 1;
		int dd = Integer.parseInt(maxi[2]);
		endcal = DateUtils.truncate(Calendar.getInstance(), Calendar.DAY_OF_MONTH);
		endcal.set(yyyy, mm, dd);
		newcal = DateUtils.truncate(Calendar.getInstance(), Calendar.DAY_OF_MONTH);
		if (endcal.after(newcal)) 
			endcal.set(9999, 11, 31);
		newcal = (Calendar) endcal.clone();
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		text.addListener(SWT.Verify, new Listener() {
			boolean ignore;
			public void handleEvent(Event e) {

				if (ignore) return;
				e.doit = false;
				StringBuffer sb = new StringBuffer(e.text);
				char[] ch = new char[sb.length()];
				sb.getChars(0, ch.length, ch, 0);
				String txt = text.getText();
				int start = e.start;
				if (start > 9)
					return;
				int index = 0;
				for (int i = 0; i < ch.length; i++) {
					if (start + index == 4 || start + index == 7) {
						if (ch[i] == '-') {
							index++;
							continue;
						}
						sb.insert(index++, '-');
					}
					if (ch[i] < '0' || '9' < ch[i])
						return;
					if (start + index == 0 && '2' != ch[i]) {
						return; 
					} else if (start + index == 1 && '0' != ch[i]) {
						return;

						// YY[Y]Y-MM-DD //
					} else if (start + index == 2) {
						// new decade cannot be higher than current 
						if (ch[i] > end.toCharArray()[2]) {
							return;
						} else {
							int newyr = Integer.parseInt(txt.substring(0,2) + ch[i] + txt.substring(3,4));
							newcal.set(Calendar.YEAR, newyr);
							// reset to current date if new is higher
							if (endcal.before(newcal)) {
								newcal.set(Calendar.YEAR, endcal.get(Calendar.YEAR));
								newcal.set(Calendar.MONTH, endcal.get(Calendar.MONTH));
								newcal.set(Calendar.DAY_OF_MONTH, endcal.get(Calendar.DAY_OF_MONTH));
								// copy new year if not
							} else {
								newcal.set(Calendar.YEAR, newyr);
							}
							// replace text from ***Y-MM-DD
							sb.append(sdf.format(newcal.getTime()).substring(3));
							continue;
						}

						// YYY[Y]-MM-DD //
					} else if (start + index == 3) {
						// get **YY
						int newyr = Integer.parseInt(txt.substring(0,3) + ch[i]);
						// update new calendar to enable comparison 
						newcal.set(Calendar.YEAR, newyr);
						if (endcal.before(newcal)) {
							// reset to current year if new is higher
							newcal.set(Calendar.YEAR, endcal.get(Calendar.YEAR));
							newcal.set(Calendar.MONTH, endcal.get(Calendar.MONTH));
							newcal.set(Calendar.DAY_OF_MONTH, endcal.get(Calendar.DAY_OF_MONTH));
							return;
						} else {
							// replace text from ****-MM-DD
							sb.append(sdf.format(newcal.getTime()).substring(4));
						}
						continue;

						// YYYY-[M]M-DD //
					} else if (start + index == 5) {
						// get YYYY-[M]M-DD //
						if (ch[i] > '1')
							// month cannot be higher than teens
							return;
						// get YYYY-[MM]-DD //
						int newmth = Integer.parseInt(ch[i] + txt.substring(6,7));
						// specifically 12
						if (newmth > 12) newmth = 12;
						// but not 0
						if (newmth < 1) newmth = 1;
						// get YYYY-MM-[DD]
						int day = Integer.parseInt(txt.substring(8));;
						// make day to lowest value to prevent carry over when month is changed
						newcal.set(Calendar.DATE, 1);
						newcal.set(Calendar.MONTH, newmth - 1); // java's 0 for January 
						int maxDay = newcal.getActualMaximum(Calendar.DAY_OF_MONTH);
						if (day > maxDay) {
							// use max day for the month if current day is higher
							newcal.set(Calendar.DAY_OF_MONTH, maxDay);
							sb.append(txt.substring(6,8) + maxDay);
						} else {
							// new date is within the max of the month
							newcal.set(Calendar.DAY_OF_MONTH, day);
							if (endcal.before(newcal)) {
								// reset date if new is higher than current
								newcal.set(Calendar.MONTH, endcal.get(Calendar.MONTH));
								newcal.set(Calendar.DAY_OF_MONTH, endcal.get(Calendar.DAY_OF_MONTH));
								// allow to edit the month's ones if the tens are the same  
								if (ch[i] == end.toCharArray()[5]) {
									sb.append(end.substring(6));
								}
								return;
							} else {
								sb.append(sdf.format(newcal.getTime()).substring(6));
							}
						}
						continue;
					} 

					// YYYY-M[M]-DD //
					else if (start + index == 6) {
						// get YYYY-[MM]-DD //
						int newmth = Integer.parseInt(txt.substring(5,6) + ch[i]);
						// reject if higher than 12  and 0//
						if (12 < newmth || newmth < 1 ) return;
						// get YYYY-MM-[DD] //
						int day = Integer.parseInt(txt.substring(8));
						newcal.set(Calendar.DAY_OF_MONTH, 1);
						newcal.set(Calendar.MONTH, newmth - 1);
						int maxDay = newcal.getActualMaximum(Calendar.DAY_OF_MONTH);
						// use max date if new is higher
						if (day > maxDay) {
							newcal.set(Calendar.DAY_OF_MONTH, maxDay);
							sb.append(end.substring(7,8) + maxDay);
							// if not, use new date 
						} else {
							newcal.set(Calendar.DAY_OF_MONTH, day);
							// reset if new is higher than current
							if (endcal.before(newcal)) {
								newcal.set(Calendar.MONTH, endcal.get(Calendar.MONTH));
								newcal.set(Calendar.DAY_OF_MONTH, endcal.get(Calendar.DAY_OF_MONTH));
								return;
							} else {
								sb.append(sdf.format(newcal.getTime()).substring(7));
							}
						}
						continue;
					}

					// YYYY-MM-[D]D //
					else if (start + index == 8) {
						// get YYYY-MM-[DD] //
						int day = Integer.parseInt(ch[i] + txt.substring(9));
						// compare current versus max day for the month 
						int maxDay = newcal.getActualMaximum(Calendar.DAY_OF_MONTH);						
						if (day > maxDay) {
							if (newcal.get(Calendar.MONTH) == Calendar.FEBRUARY) {
								// reject if higher than 2/29
								if (day > 29) { 
									return;
									// update with max day if not
								} else {
									sb.append(maxDay - 20);  // February
								}
								// also for higher than 30's	
							} else if (day > 39) {
								return;
								// use the max date
							} else {
								sb.append(maxDay - 30);  // non-February
							}
							// add 1 to make day non-zero
						} else if (day < 1){
							sb.append(1);
							// if day is within the max of the month, check if it's not later than current
						} 
						newcal.set(Calendar.DAY_OF_MONTH, day);
						// reset if new date is later than current
						if (endcal.before(newcal)){
							newcal.set(Calendar.DAY_OF_MONTH, endcal.get(Calendar.DAY_OF_MONTH));
							// is the tens part the same for both?
							if (ch[i] == end.toCharArray()[8]) {
								// if so, allow edit for the day's ones
								sb.append(end.substring(9));
							} else {
								return;
							}
						}
						continue;
					} 

					// YYYY-MM-D[D] //
					else if (start + index == 9) {
						// get YYYY-MM-[DD] //
						int day = Integer.parseInt(txt.substring(8,9) + ch[i]);
						// compare current versus max day for the month 
						int maxDay = newcal.getActualMaximum(Calendar.DAY_OF_MONTH);						
						if (day > maxDay || day < 1) {
							return;
						} else {
							newcal.set(Calendar.DAY_OF_MONTH, day);
							if (endcal.before(newcal)) {
								newcal.set(Calendar.DAY_OF_MONTH, endcal.get(Calendar.DAY_OF_MONTH));
								return;
							}
						}
						continue;
					} 
					index++;
				}
				String s = sb.toString();
				int lth = s.length();
				text.setSelection(e.start, e.start + lth);
				ignore = true;
				text.insert(s);
				ignore = false;
				if (s.startsWith("-")) {
					text.setSelection(start + 2, start + 2);
				} else {
					text.setSelection(start + 1, start + 1);
				}
			}
		});
	}
}

