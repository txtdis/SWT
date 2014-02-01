package ph.txtdis.windows;

import java.sql.SQLException;

public class RemitCancellationPosting extends Posting {

	public RemitCancellationPosting(RemitData data) {
		super(data);
	}

	@Override
    protected void postData() throws SQLException {
		id = data.getId();

		ps = conn.prepareStatement("UPDATE remit_detail SET payment = NULL WHERE remit_id = ?");
		ps.setInt(1, id);
		ps.execute();

		ps = conn.prepareStatement("INSERT INTO remit_cancellation (remit_id) VALUES (?);");
		ps.setInt(1, id);
		ps.execute();
    }
}
