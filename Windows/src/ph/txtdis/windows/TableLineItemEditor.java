package ph.txtdis.windows;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class TableLineItemEditor {
	private Text text;
	private String oldText, newText;
	
	public TableLineItemEditor (final Table table, final int[] columns) {
		final TableEditor tblEditor = new TableEditor(table);
		table.addListener(SWT.MouseDoubleClick, new Listener() {
			@Override
			public void handleEvent(Event event) {
				Point point = new Point(event.x, event.y);
				final TableItem tblItem = table.getItem(point);
				int column = -1;
				for (int i = 0; i < table.getColumnCount(); i++) {
					Rectangle rect = tblItem.getBounds(i);
					if (rect.contains(point)) {
						column = i;
					}
				}
				// Quantities
				if (Arrays.binarySearch(columns, column) < 0) return;
				
				oldText = tblItem.getText(column);
				text = new Text(table, SWT.RIGHT);
				text.setText(oldText);
				text.setFont(new Font(table.getDisplay(), "Consolas", 10, SWT.NORMAL));
				text.setBackground(View.yellow());
				text.selectAll();
				text.addDisposeListener(new DisposeListener() {
					@Override
					public void widgetDisposed(DisposeEvent e) {
						text.getFont().dispose();
					}
				});
				new IntegerVerifier(text);

				tblEditor.horizontalAlignment = SWT.RIGHT;
				tblEditor.grabHorizontal = true;
				tblEditor.setEditor(text, tblItem, column);

				final int selectedColumn = column;
				Listener textListener = new Listener() {
					@Override
					public void handleEvent(final Event e) {
						switch (e.type) {
						case SWT.FocusOut :
							text.dispose();
							e.doit = false;
							break;
						case SWT.Traverse :
							switch (e.detail) {
							case SWT.TRAVERSE_RETURN :
								newText = text.getText();
								if(!newText.equals(oldText)) {
										tblItem.setText(selectedColumn, newText);
										tblItem.setForeground(View.red());
										table.getColumn(selectedColumn);
								}
							case SWT.TRAVERSE_ARROW_NEXT:
							case SWT.TRAVERSE_ARROW_PREVIOUS:
							case SWT.TRAVERSE_ESCAPE :
								text.dispose();
								e.doit = false;
							}
						}
					}
				};
				text.addListener(SWT.FocusOut, textListener);
				text.addListener(SWT.Traverse, textListener);
				text.setFocus();
			}
		});
	}
	
	public Text getText() {
		return text;
	}
}