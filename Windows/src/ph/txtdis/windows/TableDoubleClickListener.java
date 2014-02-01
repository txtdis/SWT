package ph.txtdis.windows;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class TableDoubleClickListener implements MouseListener {
	private Data data;

	public TableDoubleClickListener(Data data) {
		this.data = data;
	}

	@Override
    public void mouseDoubleClick(MouseEvent e) {
		Table table = (Table) e.widget; 
		TableItem tableItem = null;
		Point pt = new Point(e.x, e.y);
		int index = table.getTopIndex();
		int rowIdx = -1;
		int colIdx = -1;
		while (index < table.getItemCount()) {
			tableItem = table.getItem(index);
			int columnCount = table.getColumnCount();
			for (int i = 0; i < columnCount; i++) {
				Rectangle rect = tableItem.getBounds(i);
				if (rect.contains(pt)) {
					colIdx = i;
					rowIdx = index;
				}
			}
			index++;
		}
		if (colIdx < 0 || rowIdx < 0)
			return;
		new ModuleLauncher(data, rowIdx, colIdx);
    }

	@Override
    public void mouseDown(MouseEvent e) {
    }

	@Override
    public void mouseUp(MouseEvent e) {
    }
}
