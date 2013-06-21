package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class TimeVerifier {
		
	public TimeVerifier (final Text text) {
		text.addListener(SWT.Verify, new Listener() {
			boolean ignore;
			public void handleEvent(Event e) {
				if (ignore) return;
				e.doit = false;
				StringBuffer buffer = new StringBuffer(e.text);
				char[] chars = new char[buffer.length()];
				buffer.getChars(0, chars.length, chars, 0);
				if (e.character == '\b') {
					text.setSelection(e.start, e.start + buffer.length());
					ignore = true;
					text.insert(buffer.toString());
					ignore = false;
					text.setSelection(e.start, e.start);
					return;
				}
			
				int start = e.start;
				if (start > 4) return;
				int index = 0;
				for (int i = 0; i < chars.length; i++) {
					if (start + index == 2) {
						if (chars[i] == ':') {
							index++;
							continue;
						}
						buffer.insert(index++, ':');
					}
					if (chars[i] < '0' || '9' < chars[i]) return;
					if (start + index == 0 &&  '2' < chars[i]) return; /* [H]H */
					if (start + index == 3 &&  '5' < chars[i]) return; /* [M]M */
					index++;
				}
				String newText = buffer.toString();
				int length = newText.length();
				StringBuffer date = new StringBuffer(text.getText());
				date.replace(e.start, e.start + length, newText);
				String hh = date.substring(0, 1);
				if (hh.indexOf('0') == -1) {
					int hour = Integer.parseInt(hh);
					if (0 > hour || hour > 23) return;
				}
				String mm = date.substring(3, 4);
				if (mm.indexOf('0') == -1) {
					int minute =  Integer.parseInt(mm);
					if (0 > minute || minute > 59) return;
				}
				text.setSelection(e.start, e.start + length);
				ignore = true;
				text.insert(newText);
				ignore = false;
			}
		});
	}
}
