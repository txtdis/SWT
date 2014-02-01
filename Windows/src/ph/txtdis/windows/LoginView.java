package ph.txtdis.windows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class LoginView extends View {

	private Label logo;
	private Button login;
	private Combo netCombo, siteCombo;
	private Text userInput, passwordInput;
	private String username, password, site, network;

	public LoginView() {
		super();
		shell.setLayout(new GridLayout(2, false));

		Label welcome = new Label(shell, SWT.CENTER);
		welcome.setText("WELCOME!");
		welcome.setFont(UI.BOLD);
		welcome.setForeground(UI.GREEN);
		welcome.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, true, false, 2, 2));

		logo = new Label(shell, SWT.NONE);
		logo.setImage(new Image(UI.DISPLAY, this.getClass().getResourceAsStream("buttons/txt.png")));
		
		Composite parent = new Composite(shell, SWT.NO_TRIM);
		parent.setLayout(new GridLayout(2, false));
		
		netCombo = new ComboBox(parent, Site.NETWORKS, "NETWORK", 1).getCombo();
		siteCombo = new ComboBox(parent, Site.DATABASES, "DATABASE", 1).getCombo();

		Label userLabel = new Label(parent, SWT.NONE);
		userLabel.setText("USERNAME");
		userLabel.setFont(UI.MONO);
		userLabel.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, true, 1, 1));
		userInput = new Text(parent, SWT.BORDER);
		userInput.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		Label passwordLabel = new Label(parent, SWT.NONE);
		passwordLabel.setText("PASSWORD");
		passwordLabel.setFont(UI.MONO);
		passwordLabel.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, true, 1, 1));
		passwordInput = new Text(parent, SWT.BORDER);
		passwordInput.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		passwordInput.setEchoChar('*');

		Composite buttons = new Composite(parent, SWT.NO_TRIM);
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		buttons.setLayout(new GridLayout(2, true));

		login = new Button(buttons, SWT.NONE);
		login.setFont(UI.MONO);
		login.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		login.setText("LOGIN");

		userInput.addListener(SWT.DefaultSelection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				passwordInput.setFocus();
			}
		});

		passwordInput.addListener(SWT.DefaultSelection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				proceed();
			}
		});

		login.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				proceed();
			}
		});

		userInput.setFocus();
		show();
	}

	@Override
    protected void proceed() {
		username = userInput.getText();
		password = passwordInput.getText();
		site = Site.SERVERS[siteCombo.getSelectionIndex()];
		network = Site.ADDRESSES[netCombo.getSelectionIndex()];
		logo.getImage().dispose();
		shell.dispose();
		new Login(username, password, site, network);
	}

	@Override
    protected void createShell() {
		shell = new Shell(UI.DISPLAY, SWT.SYSTEM_MODAL | SWT.CLOSE);
    }
}
