package ph.txtdis.windows;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class ImgButton {
	
	protected Report report;
	
	private Button button;
	private Image image, focused;
	private Type type;

	public ImgButton(Composite composite, Type t, Report r) {
		
		report = r;
		type = t;
		
		Display display = composite.getDisplay();
		image = new Image(display, createImage(type, ""));
		focused = new Image(display, createImage(type, "inFocus"));
		
		button = new Button(composite, SWT.FLAT);
		button.setImage(image);
		button.setToolTipText(type.getName());
		
		button.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				String name = type.getMethod();
				Class<?> cls = report.getClass();
				try {
	                Method method = cls.getMethod(name);
	                method.invoke(report);
                } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
	                e.printStackTrace();
                }
				
			}
		});
		
		button.addListener(SWT.FocusIn, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				button.setImage(focused);
			}
		});

		button.addListener(SWT.FocusOut, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				button.setImage(image);
			}
		});
		
		button.addListener(SWT.MouseHover, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				button.setFocus();
			}
		});
		
		button.addListener(SWT.Dispose, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				button.getImage().dispose();
			}
		});
	}

	private InputStream createImage(Type type, String suffix) {
	    return this.getClass().getResourceAsStream("buttons/" + type + suffix + ".png");
    }
	
	public Button getButton() {
		return button;
	}
}
