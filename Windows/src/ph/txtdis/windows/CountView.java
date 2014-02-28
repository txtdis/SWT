package ph.txtdis.windows;

import java.sql.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class CountView extends OrderView implements Dateable {

	private Combo takerCombo, checkerCombo, locationCombo;

	public CountView() {
		this(0);
	}

	public CountView(int id) {
		this(new CountData(id));
	}

	public CountView(CountData data) {
		super(data);
		this.data = data;
		type = Type.COUNT;
		display();
	}

	@Override
	protected void addHeader() {
		new Header(this, data) {
			@Override
			protected void layButtons() {
				new ImgButton(buttons, Type.NEW, type);
				new ImgButton(buttons, Type.OPEN, view);
				new ImgButton(buttons, Type.CALENDAR, view);
				if (id == 0)
					postButton = new ImgButton(buttons, Type.SAVE, view).getButton();
			}
		};
	}

	@Override
	protected void addSubheader() {
		Composite info = new Compo(shell, 3, GridData.FILL_HORIZONTAL).getComposite();

		Composite tag = new Compo(info, 2, GridData.VERTICAL_ALIGN_BEGINNING).getComposite();
		idDisplay = new TextDisplayBox(tag, "TAG", ((InputData) data).getId()).getText();

		Group left = new Grp(info, 4, "").getGroup();
		takerCombo = new ComboBox(left, ((CountData) data).getTakers(), "TAKER").getCombo();
		checkerCombo = new ComboBox(left, ((CountData) data).getCheckers(), "CHECKER").getCombo();
		new Label(left, SWT.NONE);
		locationCombo = new ComboBox(left, ((CountData) data).getLocations(), "LOCATION").getCombo();
		locationCombo.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, true, 2, 1));

		Composite detail = new Compo(info, 2, GridData.VERTICAL_ALIGN_BEGINNING).getComposite();
		dateInput = new TextInputBox(detail, "DATE", data.getDate()).getText();
	}

	@Override
	protected void setFooter() {
		new EncodingDataFooter(shell, this, data);
	}

	@Override
	protected void addListener() {
		if (id == 0)
			setTagListener();
	}

	private void setTagListener() {
		new ComboSelector(takerCombo, checkerCombo) {
			@Override
			protected void processSelection() {
				((CountData) data).setTakerId(Contact.getId(selection));
			}
		};

		new ComboSelector(checkerCombo, locationCombo) {
			@Override
			protected void processSelection() {
				((CountData) data).setCheckerId(Contact.getId(selection));
			}
		};

		new ComboSelector(locationCombo, dateInput) {
			@Override
			protected void processSelection() {
				((CountData) data).setLocationId(new Location(selection).getId());
			}
		};

		final CountView view = this;
		new DataInputter(dateInput, itemIdInput) {
			private Date date;

			@Override
			protected Boolean isNonBlank() {
				date = DIS.parseDate(textInput);
				if (!Count.isClosed(date)) {
					System.out.println("was here");
					data.setDate(date);
					new CountItemIdEntry(view, (OrderData) data);
					return true;
				} else {
					String countDate = textInput;
					new ErrorDialog("Data entry has been closed for\nStock Take on " + countDate);
					return false;
				}
			}
		};
	}

	@Override
	protected void setFocus() {
		if (id == 0) {
			takerCombo.setEnabled(true);
			takerCombo.setFocus();
		}
	}

	@Override
	public boolean isEnteredItemQuantityValid(String quantity) {
		return true;
	}

	@Override
	public void processQuantityInput(String quantity, int rowIdx) {
		tableItem.setText(6, quantity);
		postButton.setEnabled(true);
		new CountItemIdEntry(this, data);
	}

	@Override
	public Posting getPosting() {
		return new CountPosting((CountData) data);
	}

	@Override
	public void processUomSelection(String selection) {
		new ItemQualitySelector(this, (Expirable) data);
	}

	@Override
	public void selectReportDate() {
		 new CountReportSelectedDateAction(shell, (CountData) data);
	}
}