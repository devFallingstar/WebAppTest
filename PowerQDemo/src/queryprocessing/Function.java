package queryprocessing;

public class Function
{
	private String name;
	private int kwIndex;
	private int fnIndex;
	private String invalidWord;
	
	public Function(String name)
	{
		this.name = name;
		this.kwIndex = -1;
		this.fnIndex = -1;
		this.invalidWord = "";
	}
	
	public void setKwIndex(int index)
	{
		this.kwIndex = index;
	}
	
	public void setFnIndex(int index)
	{
		this.fnIndex = index;
	}
	
	public int getKwIndex()
	{
		return this.kwIndex;
	}
	
	public int getFnIndex()
	{
		return this.fnIndex;
	}
	
	public boolean isSimpleFn()
	{
		if(this.fnIndex == -1)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	public String getFnName()
	{
		return this.name;
	}
	
	public String getInvalidWord()
	{
		return this.invalidWord;
	}
	
	public void setInvalidWord(String word)
	{
		this.invalidWord = word;
	}
}
