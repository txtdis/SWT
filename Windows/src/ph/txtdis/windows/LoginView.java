package ph.txtdis.windows;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class LoginView extends View {

	private Label lblLogo, lblInfo, lblUser, lblPass;
	private Button btnLogin, btnCancel;
	private Text txtUser, txtPass;
	private String user, pass;

	public LoginView() {
		super();

		shell.setLayout(new GridLayout(2, false));

		lblLogo = new Label (shell, SWT.NONE);
		lblLogo.setImage(new Image(DIS.DISPLAY, this.getClass().getResourceAsStream("images/txt.png")));
		Composite compo = new Composite(shell, SWT.NO_TRIM);
		compo.setLayout(new GridLayout(2, true));

		lblInfo = new Label(compo, SWT.CENTER);
		lblInfo.setText("WELCOME!");
		lblInfo.setFont(DIS.BOLD);
		lblInfo.setForeground(DIS.GREEN);
		GridData gdInfo = new GridData();
		gdInfo.horizontalSpan = 2;
		gdInfo.horizontalAlignment = GridData.CENTER;
		lblInfo.setLayoutData(gdInfo);

		// User Name Input
		lblUser = new Label(compo, SWT.NONE);
		lblUser.setText("Username");

		txtUser = new Text(compo, SWT.BORDER);
		GridData gdtUser = new GridData();
		gdtUser.horizontalSpan = 2;
		gdtUser.horizontalAlignment = GridData.FILL;
		txtUser.setLayoutData(gdtUser);	

		// Password Input
		lblPass = new Label(compo, SWT.NONE);
		lblPass.setText("Password");

		txtPass = new Text(compo, SWT.BORDER);
		GridData gdtPass = new GridData();
		gdtPass.horizontalSpan = 2;
		gdtPass.horizontalAlignment = SWT.FILL;
		txtPass.setLayoutData(gdtPass);
		txtPass.setEchoChar('*');

		// Login Button
		btnLogin = new Button(compo, SWT.NONE);
		GridData gdLogin = new GridData();
		gdLogin.horizontalAlignment = GridData.FILL;
		btnLogin.setLayoutData(gdLogin);
		btnLogin.setText("Login");

		// Cancel Button
		btnCancel = new Button(compo, SWT.NONE);
		GridData gdCancel = new GridData();
		gdCancel.horizontalAlignment = GridData.FILL;
		btnCancel.setLayoutData(gdCancel);
		btnCancel.setText("Cancel");

		txtUser.addListener (SWT.DefaultSelection, new Listener () {
			@Override
			public void handleEvent (Event e) {
				user = txtUser.getText();
				if (user.isEmpty()) {
					lblLogo.getImage().dispose();
					shell.close();
				} else {
					txtPass.setFocus();
				}
			}
		});

		txtPass.addListener (SWT.DefaultSelection, new Listener () {
			@Override
			public void handleEvent (Event e) {
				pass = txtPass.getText();
				if (pass.isEmpty()) {
					lblLogo.getImage().dispose();
					shell.close();
				} else {
					process();
				}
			}
		});

		btnLogin.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				process();
			}
		});	 		

		btnCancel.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				lblLogo.getImage().dispose();
				shell.close();
			}
		});

		txtUser.setFocus();
		show();
	}

	private void process() {
		user = txtUser.getText();
		pass = txtPass.getText();
		ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
		lblLogo.getImage().dispose();
		shell.dispose();
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor pm) {
				pm.beginTask("Connecting to Server...", IProgressMonitor.UNKNOWN);
				new Login(user, pass);
				pm.done();
			}
		};
		try {
			pmd.run(true, false, runnable);
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
		if(!Login.getGroup().isEmpty()) 
			new MainMenu(); 
		else if(Database.error.contains("role") || Database.error.contains("name"))
			new ErrorDialog("\nIncorrect Username\nand/or Password"); 
		else
			new ErrorDialog("\nNo Connection\nto Server"); 
	}
}
