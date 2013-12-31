package ph.txtdis.windows;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;

public abstract class ProgressDialog {
	
	public ProgressDialog() {
		this("Preparing data...");
	}

	public ProgressDialog(final String message) {
		ProgressMonitorDialog pmd = new ProgressMonitorDialog(UI.DISPLAY.getActiveShell());
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor pm) {
				pm.beginTask(message, IProgressMonitor.UNKNOWN);
				proceed();
				pm.done();
			}
		};
		try {
			pmd.run(true, false, runnable);
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public abstract void proceed();
}
