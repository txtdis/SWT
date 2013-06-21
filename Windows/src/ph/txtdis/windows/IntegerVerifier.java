package ph.txtdis.windows;

import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Text;

public class IntegerVerifier {

	public IntegerVerifier(Text text){
		text.addVerifyListener(new VerifyListener() {
			
			@Override
			public void verifyText(VerifyEvent e) {
				String string = e.text;
				Text text = (Text) e.getSource();
				String str = text.getText();
				char[] chars = new char[string.length()];
				string.getChars(0, chars.length, chars, 0);
				for (int i = 0; i < chars.length; i++) {
					if (!((chars[i] == '-' && !str.contains("-") && str.length() == 0) ||
							('0' <= chars[i] && chars[i] <= '9'))) {
						e.doit = false;
						return;
					}
				}
			}
		});
	}
}
