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
import org.eclipse.swt.widgets.Text;

public class LoginView extends View {

	private Label logo;
	private Button login, cancel;
	private Combo siteCombo;
	private Text userInput, passwordInput;
	private String username, password, site;
	private boolean isChecklistOK;

	public LoginView() {
		super();

		shell.setLayout(new GridLayout(2, false));

		logo = new Label(shell, SWT.NONE);
		logo.setImage(new Image(UI.DISPLAY, this.getClass().getResourceAsStream("images/txt.png")));
		Composite parent = new Composite(shell, SWT.NO_TRIM);
		parent.setLayout(new GridLayout(2, false));

		Label welcome = new Label(parent, SWT.CENTER);
		welcome.setText("WELCOME!");
		welcome.setFont(UI.BOLD);
		welcome.setForeground(UI.GREEN);
		welcome.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, true, false, 2, 1));

		Label siteLabel = new Label(parent, SWT.RIGHT);
		siteLabel.setText("SITE");
		siteLabel.setFont(UI.MONO);
		siteLabel.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, true, 1, 1));
		siteCombo = new Combo(parent, SWT.READ_ONLY);
		siteCombo.setFont(UI.MONO);
		siteCombo.setItems(Site.SITES);
		siteCombo.select(1);

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
		login.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		login.setText("LOGIN");

		cancel = new Button(buttons, SWT.NONE);
		cancel.setFont(UI.MONO);
		cancel.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		cancel.setText("CANCEL");

		userInput.addListener(SWT.DefaultSelection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				username = userInput.getText();
				if (username.isEmpty()) {
					logo.getImage().dispose();
					shell.close();
				} else {
					passwordInput.setFocus();
				}
			}
		});

		passwordInput.addListener(SWT.DefaultSelection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				password = passwordInput.getText();
				if (password.isEmpty()) {
					logo.getImage().dispose();
					shell.close();
				} else {
					process();
				}
			}
		});

		login.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				process();
			}
		});

		cancel.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				logo.getImage().dispose();
				shell.close();
			}
		});

		userInput.setFocus();
		show();
	}

	private void process() {
		username = userInput.getText();
		password = passwordInput.getText();
		site = Site.SERVERS[siteCombo.getSelectionIndex()];
		logo.getImage().dispose();
		shell.dispose();
		new ProgressDialog("Connecting to Server...") {
			@Override
			public void proceed() {
				new Login(username, password, site);
				isChecklistOK = new DatabasePreConnectionChecklist().isOK();
			}
		};
		
		if (!Login.getGroup().isEmpty() && isChecklistOK)
			new MainMenu();
		else if (Database.error.contains("password authentication failed"))
			new ErrorDialog("Incorrect username\nand/or password.");
		else if (Database.error.contains("Update successful"))
			new InfoDialog(Database.error);
		else if (Database.error.contains("Update rollbacked"))
			new ErrorDialog(Database.error);
		else if (!Database.error.isEmpty())
			new ErrorDialog("No server connection.");
	}
}
