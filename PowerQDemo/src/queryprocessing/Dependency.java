package queryprocessing;

public class Dependency
{
	private Relation relL;
	private Relation relR;
	private int attrL;
	private int attrR;
	
	public Dependency(Relation relL, Relation relR, int attrL, int attrR)
	{
		this.relL = relL;
		this.relR = relR;
		this.attrL = attrL;
		this.attrR = attrR;
	}
	
	public Relation getRelL()
	{
		return relL;
	}
	
	public Relation getRelR()
	{
		return relR;
	}
	
	public String getAttrL()
	{
		return this.relL.getAttrName(attrL);
	}
	public String getAttrR()
	{
		return this.relR.getAttrName(attrR);
	}
}
