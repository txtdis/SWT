package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class ImgButton {

	private Button button;
	
	public ImgButton(Composite composite, Type button) {
		createButton(composite, button);		
		addSelectionListener(button);		
	}

	public ImgButton(Composite composite, Type button, ReportView view) {
		createButton(composite, button);		
		addSelectionListener(button, view);		
	}

	public ImgButton(Composite composite, Type button, Type module) {
		createButton(composite, button);		
		addSelectionListener(button, module);		
	}

	private void addListeners(Type buttonType) {
	    addFocusListener(buttonType);
		addDisposeListener();
    }

	private void addFocusListener(Type buttonType) {
	    addFocusInListener(buttonType);
		addFocusOutListener(buttonType);
    }

	private void createButton(Composite composite, Type type) {
	    button = new Button(composite, SWT.FLAT);
		setProperties(type);
		addListeners(type);	    
    }

	private void setProperties(Type type) {
	    button.setImage(createUnfocusedImage(type));
		button.setToolTipText(type.getName());
    }

	private void addDisposeListener() {
	    button.addListener(SWT.Dispose, new Listener() {
			@Override
			public void handleEvent(Event event) {
				button.getImage().dispose();
			}
		});
    }

	private void addFocusOutListener(Type type) {
		final Image image = createUnfocusedImage(type);
	    button.addListener(SWT.FocusOut, new Listener() {
			@Override
			public void handleEvent(Event event) {
				button.setImage(image);
			}
		});
    }

	private void addFocusInListener(final Type buttonType) {
	    button.addListener(SWT.FocusIn, new Listener() {
			@Override
			public void handleEvent(Event event) {
				button.setImage(createFocusedImage(buttonType));
			}
		});
    }

	private void addSelectionListener(final Type type, final View view) {
	    button.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				String method = type.getMethod();
				DIS.invokeMethod(view, method);
			}
		});
    }

	private void addSelectionListener(final Type buttonType, final Type moduleType) {
	    button.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				button.getShell().close();
				String suffix = moduleType.getMethod();
				suffix = suffix == null ? "View" : suffix;
				String name = DIS.extractClassName(moduleType) + suffix;
				DIS.instantiateClass(name);
			}
		});
    }

	private void addSelectionListener(final Type module) {
	    button.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				String name = DIS.extractClassName(module) + "View";
				DIS.instantiateClass(name);
			}
		});
    }

	private Image createUnfocusedImage(Type type) {
		return UI.createImage("buttons", type, "");
	}

	private Image createFocusedImage(Type type) {
		return UI.createImage("buttons", type, "inFocus");
	}

	public Button getButton() {
		return button;
	}
}
