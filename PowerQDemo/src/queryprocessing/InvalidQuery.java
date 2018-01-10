package queryprocessing;

public class InvalidQuery {
	Constant.TYPE preType;//type of the previous kw
	Constant.TYPE kwType;//type of the current kw
	String preKw;
	String kw;
	String otherInfo;//other info should be shown
	
	public InvalidQuery(Constant.TYPE preType, Constant.TYPE kwType, String preKw, String kw, String otherInfo)
	{
		this.preType = preType;
		this.kwType = kwType;
		this.preKw = preKw;
		this.kw = kw;
		this.otherInfo = otherInfo;
	}
	
	public Constant.TYPE getPreType()
	{
		return this.preType;
	}
	
	public Constant.TYPE getKwType()
	{
		return this.kwType;
	}
	
	public String getPreKw()
	{
		return this.preKw;
	}
	
	public String getKw()
	{
		return this.kw;
	}
	
	public String getOtherInfo()
	{
		return this.otherInfo;
	}
}
