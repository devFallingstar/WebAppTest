package queryprocessing;


public class SQLUnit
{
//	private String countSql;
	private String sql;
	private int[] verifyCol;
	private int outputCol;

	public SQLUnit()
	{
//		this.countSql = null;
		this.sql = null;
		this.verifyCol = null;
		this.outputCol = 0;
	}
	
//	public void setCountSQL(String countSql)
//	{
//		this.countSql = countSql;
//	}
	
	public void setSQL(String sql)
	{
		this.sql = sql;
	}
	
	public void setOutputCol(int count)
	{
		this.outputCol = count;
	}
	
//	public String getCountSQL()
//	{
//		return this.countSql;
//	}
	
	public int getOuputCol()
	{
		return this.outputCol;
	}
	
	public String getSQL()
	{
		return this.sql;
	}
	
//	public boolean isColMapEmpty()
//	{
//		return verifycolMap.isEmpty();
//	}
	
	public int getVerifyCol(int index)
	{
		return verifyCol[index];
	}
	
	public void setVerifyCol(int[] verifyCol)
	{
		this.verifyCol = verifyCol;
	}
	
	public void resetVerifyCol()
	{
		int len = this.verifyCol.length;
		for(int i = 0; i < len; i++)
		{
			this.verifyCol[i] = 0;
		}
	}
	
}
