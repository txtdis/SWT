package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class Query {
	private Connection conn;
	private ResultSet rs;
	private Object[][] result;

	public Query() {
		conn = DBMS.getInstance().getConnection();
	}

	public Query(String user, String pword, String server, String network) {
		conn = DBMS.getInstance().getConn(user, pword, server, network);
	}

	public BigDecimal getBigDecimal(String sql) {
		BigDecimal bd = (BigDecimal) getDatum(new Object[0], sql);
		return bd == null ? BigDecimal.ZERO : bd;
	}

	public BigDecimal getBigDecimal(Object o, String sql) {
		BigDecimal bd = (BigDecimal) getDatum(new Object[] { o }, sql);
		return bd == null ? BigDecimal.ZERO : bd;
	}

	public Object getDatum(String sql) {
		return getDatum(null, sql);
	}

	public Object getDatum(Object o, String sql) {
		Object[][] aao = getResult(o, sql);
		return aao != null ? aao[0][0] : null;
	}

	public Object[] getList(String sql) {
		return getList(null, sql);
	}

	public Object[] getList(Object o, String sql) {
		Object[][] aao = getResult(o, sql);
		return aao != null ? aao[0] : null;
	}

	public Object[][] getTableData(String sql) {
		return getTableData(null, sql);
	}

	public Object[][] getTableData(final Object parameter, final String sql) {
		new ProgressDialog() {
			@Override
			public void proceed() {
				result = getResult(parameter, sql);
			}
		};
		return result;
	}

	private Object[][] getResult(Object parameter, String sql) {
		Object[][] tableData = null;

		Object[] parameters = new Object[] { parameter };
		if (parameter == null)
			parameters = new Object[0];
		else if (parameter.getClass().isArray())
			parameters = (Object[]) parameter;

		if (conn == null)
			return tableData;

		try (PreparedStatement ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
		        ResultSet.CONCUR_READ_ONLY)) {

			for (int i = 0; i < parameters.length; i++)
				ps.setObject(1 + i, parameters[i]);
			rs = ps.executeQuery();
			ResultSetMetaData metaData = rs.getMetaData();
			int column = metaData.getColumnCount();
			rs.last();
			int rowSize = rs.getRow();
			rs.beforeFirst();

			// ArrayList
			ArrayList<Object[]> alao = new ArrayList<>();
			if (column == 1) {
				ArrayList<Object> alo = new ArrayList<>();
				while (rs.next()) {
					alo.add(rs.getObject(1));
				}
				int size = alo.size();
				alao.add(size == 0 ? new Object[1] : alo.toArray(new Object[size]));
			} else {
				Object[] as;
				while (rs.next()) {
					as = new Object[column];
					for (int j = 0; j < column; j++) {
						as[j] = (rs.getObject(j + 1));
					}
					alao.add(as);
				}
			}
			int size = alao.size();
			tableData = size == 0 ? null : alao.toArray(new Object[size][]);
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return tableData;
	}

	public HashMap<Long, BigDecimal> getMap(String select) {
		return getMap(null, select);
	}

	public HashMap<Long, BigDecimal> getMap(Integer id, String select) {
		int size;
		HashMap<Long, BigDecimal> map = null;
		ResultSet rs = null;
		try (PreparedStatement ps = conn.prepareStatement(select, ResultSet.TYPE_SCROLL_INSENSITIVE,
		        ResultSet.CONCUR_READ_ONLY)) {
			if (id != null)
				ps.setInt(1, id);
			rs = ps.executeQuery();
			rs.last();
			size = rs.getRow();
			map = new HashMap<>(size);
			rs.beforeFirst();
			while (rs.next())
				map.put(rs.getLong(1), rs.getBigDecimal(2));
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return map;
	}
}