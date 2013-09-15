package ph.txtdis.windows;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public abstract class ReportButton extends ImageButton {

	protected Report report;

	public ReportButton(Composite parent, Report report, String icon, String tooltip) {
		super(parent, report.getModule(), icon, tooltip);
		this.report = report;
	}

	@Override
	protected void doWhenSelected() {
		Shell shell = parent.getShell();
		ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor pm) {
				pm.beginTask("Preparing data...", IProgressMonitor.UNKNOWN);
				try {
					doWithProgressMonitorWhenSelected();
				} catch (Exception e) {
					e.printStackTrace();
					//new ErrorDialog(e);
				}
				pm.done();
			}
		};
		try {
			pmd.run(true, false, runnable);
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
			//new ErrorDialog(e);
		}
	}

	protected void doWithProgressMonitorWhenSelected() {
	}
}