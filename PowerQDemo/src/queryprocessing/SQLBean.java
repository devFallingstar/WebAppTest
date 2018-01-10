package queryprocessing;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLBean
{
	private Connection conn = null;
	private ResultSet rs = null;
	private Statement stmt = null;

	private String DatabaseDriver = "com.mysql.jdbc.Driver";
	
	private String url = "jdbc:mysql://localhost/";
	private String usr = "root";
//	private String psw = "5417";
	private String psw = "Gqjy5417@";
	
	private static final int timeOut = 60;

	public SQLBean()
	{
		try
		{
			Class.forName(DatabaseDriver);
		} 
		catch (java.lang.ClassNotFoundException e)
		{
			Constant.writeLog("add driver error:" + e.getMessage());
			System.err.println("add driver error:" + e.getMessage());
		}
	}
	
	public boolean isValid()
	{
		try
		{
			if(this.conn.isValid(0))
			{
				return true;
			}
			else
			{
				return false;
			}
		} 
		catch (SQLException e)
		{
			Constant.writeLog("open database error:" + e.getMessage());
			e.printStackTrace();
		}
		return false;
	}

	public void openDataBase(String dbname)
	{
		try
		{
			this.conn = DriverManager.getConnection(this.url + dbname, this.usr, this.psw);
		} 
		catch (SQLException ex)
		{
			Constant.writeLog("open database error:" + ex.getMessage());
			System.err.println("open database error:" + ex.getMessage());
		}

	}

	public Connection getConn()
	{
		return this.conn;
	}

	public void closeDataBase()
	{
		try
		{
			if(this.stmt != null)
			{
				this.stmt.close();
			}
			this.conn.close();
		} 
		catch (SQLException end)
		{
			Constant.writeLog("close Connection error:" + end.getMessage());
			System.err.println("close Connection error:" + end.getMessage());
		}
	}
	
	public void updateQueries(String[] sqls)
	{
		try
		{
			this.stmt = this.conn.createStatement();
			for(String sql : sqls)
			{
				if(sql != null)
				{
					this.stmt.executeUpdate(sql);
				}
			}
		}
		catch (SQLException ex)
		{
			Constant.writeLog("execute sql error:" + ex.getMessage());
			System.err.println("execute sql error:" + ex.getMessage());
		}
	}
	
	public void updateQuery(String sql)
	{
		try
		{
			this.stmt = this.conn.createStatement();
//			this.stmt.setQueryTimeout(timeOut);
			stmt.executeUpdate(sql);
		}
		catch (SQLException ex)
		{
			Constant.writeLog("execute sql error:" + ex.getMessage());
			System.err.println("execute sql error:" + ex.getMessage());
		}
	}

	public ResultSet executeQuery(String sql)
	{
		this.rs = null;
		try
		{
			this.stmt = this.conn.createStatement();
			this.stmt.setQueryTimeout(timeOut);
			this.rs = stmt.executeQuery(sql);
			
		} 
		catch (SQLException ex)
		{
			Constant.writeLog("execute sql error:" + ex.getMessage());
			System.err.println("execute sql error:" + ex.getMessage());
		}
		return this.rs;
	}
	
	public ResultSet executeQueryScroll(String sql)
	{
		this.rs = null;
		try
		{
			this.stmt = this.conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			this.stmt.setQueryTimeout(timeOut);
			this.rs = stmt.executeQuery(sql);
			
		} 
		catch (SQLException ex)
		{
			Constant.writeLog("execute sql error:" + ex.getMessage());
			System.err.println("execute sql error:" + ex.getMessage());
		}
		return this.rs;
	}
	
	
}
