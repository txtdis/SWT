package ph.txtdis.windows;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class SQL {
	private Connection conn;

	public SQL() {
		conn = Database.getInstance().getConnection();
	}

	public SQL(String u, String p) {
		conn = Database.getInstance().getConnection(u, p);
	}

	public Object getDatum(String s) {
		return getDatum(null, s);
	}

	public Object getDatum(Object o, String s) {
		return getDatum(new Object[] {o}, s);
	} 

	public Object getDatum(Object[] ao, String s) {
		Object[][] aao = getObjectArray(ao, s);
		return aao != null ? aao[0][0] : null;
	} 

	public Object[] getData(String s) {
		return getData(null, s);
	} 

	public Object[] getData(Object o, String s) {
		return getData(new Object[] {o}, s);
	} 

	public Object[] getData(Object[] ao, String s) {
		Object[][] aao = getObjectArray(ao, s);
		return aao != null ? aao[0] : null;
	} 

	public Object[][] getDataArray(String s) {
		return getObjectArray(null, s);
	}

	public Object[][] getDataArray(Object o, String s) {
		return getObjectArray(new Object[] {o}, s);
	}

	public Object[][] getDataArray(Object[] o, String s) {
		return getObjectArray(o, s);
	}

	public Object[][] getObjectArray(Object[] objects, String select) {
		Object[][] aas = null;
		if(conn != null) {
			ResultSet rs = null;
			try (PreparedStatement ps = conn.prepareStatement(select)) { 
				if(objects != null) {
					for (int i = 0; i < objects.length; i++)	{
						ps.setObject(1 + i, objects[i]);
					}
				}
				rs = ps.executeQuery();
				int column = rs.getMetaData().getColumnCount();

				//ArrayList
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
				aas = size == 0 ? null : alao.toArray(new Object[size][]);
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				try {
					if(rs != null) rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} 
		return aas;
	}

	public HashMap<Long, BigDecimal> getMap(String select) {
		return getMap(null, select);
	}

	public HashMap<Long, BigDecimal> getMap(Integer id, String select) {
		int size;
		HashMap<Long, BigDecimal> map = null;
		ResultSet rs = null;
		try (PreparedStatement ps = conn.prepareStatement(select, 
				ResultSet.TYPE_SCROLL_INSENSITIVE,
				ResultSet.CONCUR_READ_ONLY)
				){ 
			if (id != null) ps.setInt(1, id);
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
				if(rs != null) rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return map;
	}
}