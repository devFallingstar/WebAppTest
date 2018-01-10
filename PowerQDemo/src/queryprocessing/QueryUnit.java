package queryprocessing;

public class QueryUnit
{

	private Keyword[] kwArray;
	private Function[] fnArray;
	private Groupby[] gpArray;
	private Tpredicate[] tpArray;
	private InvalidQuery[] iqArray;
	String prePD;
	
	QueryUnit(Keyword[] kwArray, Function[] fnArray, Groupby[] gpArray, Tpredicate[] tpArray, InvalidQuery[] iqArray, String prePD)
	{
		this.kwArray = kwArray;
		this.fnArray = fnArray;
		this.gpArray = gpArray;
		this.tpArray = tpArray;
		this.iqArray = iqArray;
		this.prePD = prePD;
	}
	
	Keyword[] getKwArray()
	{
		return this.kwArray;
	}
	
	Function[] getFnArray()
	{
		return this.fnArray;
	}
	
	Groupby[] getGpArray()
	{
		return this.gpArray;
	}
	
	Tpredicate[] getTpArray()
	{
		return this.tpArray;
	}
	
	InvalidQuery[] getIqArray()
	{
		return this.iqArray;
	}
	
	String getPrePD()
	{
		return this.prePD;
	}
}
