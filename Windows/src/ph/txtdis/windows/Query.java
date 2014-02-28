package ph.txtdis.windows;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.postgresql.util.PSQLException;

public class Query {
	private boolean bool;
	private ArrayList<Object[]> arrayList;
	private Connection conn;
	private HashMap<Long, BigDecimal> map;
	private InputStream file;
	private ResultSet rs;
	private Object[][] arrays;
	private Object[] array, parameters;
	private Object object;
	private String[] list;

	public Query() {
		conn = DBMS.getInstance().getConnection();
	}

	public Query(String user, String pword, String server, String network) throws SQLException, PSQLException {
		conn = DBMS.getInstance().getConn(user, pword, server, network);
	}

	public boolean getBoolean (Object parameter, String sql) {
		setBoolean(parameter, sql);
		return bool;
	}

	public InputStream getFile (Object parameter, String sql) {
		setFile(parameter, sql);
		return file;
	}

	public Object getDatum(String sql) {
		return getDatum(null, sql);
	}

	public Object getDatum (Object parameter, String sql) {
		setObject(parameter, sql);
		return object;
	}

	public Object[] getData(String sql) {
		return getData(null, sql);
	}

	public Object[] getData(Object parameter, String sql) {
		setArray(parameter, sql);
		return array;
	}

	public String[] getList(String sql) {
		return getList(null, sql);
	}

	public String[] getList(Object parameter, String sql) {
		setList(parameter, sql);
		return list;
	}

	public Object[][] getTableData(String sql) {
		return getTableData(null, sql);
	}

	public Object[][] getTableData(final Object parameter, final String sql) {
		setArrays(parameter, sql);
		return arrays;
	}

	public ArrayList<Object[]> getDataList(String sql) {
		return getDataList(null, sql);
	}

	public ArrayList<Object[]> getDataList(final Object parameter, final String sql) {
		setArrayList(parameter, sql);
		return arrayList;
	}

	public HashMap<Long, BigDecimal> getMap(String sql) {
		return getMap(null, sql);
	}

	public HashMap<Long, BigDecimal> getMap(Object parameter, String sql) {
		setMap(parameter, sql);
		return map;
	}

	private void setMap(Object parameter, String sql) {
		new Result(parameter, sql) {
			@Override
			protected void setDataPerType() throws SQLException, PSQLException {
				map = new HashMap<>(getResultSize());
				while (rs.next())
					map.put(rs.getLong(1), rs.getBigDecimal(2));
			}
		};
	}

	private void setArray(Object parameter, String sql) {
		new Result(parameter, sql) {
			@Override
			protected void setDataPerType() throws SQLException, PSQLException {
				int size = rs.getMetaData().getColumnCount();
				array = getResultSize() == 0 ? null : populate(new Object[size]);
			}

			private Object[] populate(Object[] array) throws SQLException, PSQLException {
				if (rs.next())
	                for (int i = 0; i < array.length; i++)
	                    array[i] = rs.getObject(i + 1);
	            return array;
            }
		};
	}

	private void setList(Object parameter, String sql) {
		new Result(parameter, sql) {
			@Override
			protected void setDataPerType() throws SQLException, PSQLException {
				int size = getResultSize();
				list = size == 0 ? null : populate(new String[size]);
			}

			private String[] populate(String[] array) throws SQLException, PSQLException {
				int i = 0; 
				while (rs.next()) {
					array[i++] = rs.getString(1);
				}
	            return array;
            }
		};
	}

	private void setBoolean(Object parameter, String sql) {
		new Result(parameter, sql) {
			@Override
			protected void setDataPerType() throws SQLException, PSQLException {
				if (rs.next())
					bool = rs.getBoolean(1);
			}
		};
	}

	private void setFile(Object parameter, String sql) {
		new Result(parameter, sql) {
			@Override
			protected void setDataPerType() throws SQLException, PSQLException {
				if (rs.next())
					file = rs.getBinaryStream(1);
			}
		};
	}

	private void setObject(Object parameter, String sql) {
		new Result(parameter, sql) {
			@Override
			protected void setDataPerType() throws SQLException, PSQLException {
				if (rs.next())
					object = rs.getObject(1);
			}
		};
	}

	private void setArrays(Object parameter, String sql) {
		new Result(parameter, sql) {
			@Override
			protected void setDataPerType() throws SQLException, PSQLException {
				int row = getResultSize();
				int column = rs.getMetaData().getColumnCount();
				arrays = row == 0 ? null : createArrays(row, column);
			}

			private Object[][] createArrays(int row, int column) throws SQLException, PSQLException {
	            arrays = new Object[row][column];
	            populateRow(column);
	            return arrays;
            }

			private void populateRow(int column) throws SQLException, PSQLException {
	            int rowIdx = 0;
	            while (rs.next())
	            	populateColumn(rowIdx++, column);
            }

			private void populateColumn(int rowIdx, int columnSize) throws SQLException, PSQLException {
	            for (int j = 0; j < columnSize; j++)
	            	arrays[rowIdx][j] = rs.getObject(j + 1);
            }
		};
	}

	private void setArrayList(Object parameter, String sql) {
		new Result(parameter, sql) {
			@Override
			protected void setDataPerType() throws SQLException, PSQLException {
				int row = getResultSize();
				arrayList = row == 0 ? null : populateRow(new ArrayList<Object[]>());
			}

			private ArrayList<Object[]> populateRow(ArrayList<Object[]> arrayList) throws SQLException, PSQLException {
				int column = rs.getMetaData().getColumnCount();
	            while (rs.next())
	            	arrayList.add(populateColumn(column));
	            return arrayList;
            }

			private Object[] populateColumn(int column) throws SQLException, PSQLException {
				Object[] array = new Object[column];
	            for (int j = 0; j < column; j++)
	            	array[j] = rs.getObject(j + 1);
	            return array;
            }
		};
	}

	abstract class Result {

		private Result(Object parameter, String sql) {
			try (PreparedStatement preparedStatement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
			        ResultSet.CONCUR_READ_ONLY)) {
				setResult(parameter, preparedStatement);
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				closeResultSet();
			}
		}

		protected abstract void setDataPerType() throws SQLException, PSQLException;

		protected int getResultSize() throws SQLException, PSQLException {
			rs.last();
			int size = rs.getRow();
			rs.beforeFirst();
			return size;
		}

		private void setResult(Object parameter, PreparedStatement preparedStatement) throws SQLException, PSQLException {
			setParameters(parameter);
			setResultSet(preparedStatement);
			setDataPerType();
		}

		private void setParameters(Object parameter) {
			try {
				parameters = parameter.getClass().isArray() ? (Object[]) parameter : new Object[] { parameter };
            } catch (NullPointerException e) {
				parameters = new Object[0];
            }
		}

		private void setResultSet(PreparedStatement ps) throws SQLException, PSQLException {
			for (int i = 0; i < parameters.length; i++) {
				ps.setObject(i + 1, parameters[i]);
			}
			rs = ps.executeQuery();
		}

		private void closeResultSet() {
			try {
				if (rs != null)
					rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
}