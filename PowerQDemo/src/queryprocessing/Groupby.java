package queryprocessing;

public class Groupby
{
	private int kwIndex;
	private String invalidWord;
	
	public Groupby()
	{
		this.kwIndex = -1;
		this.invalidWord = "";
	}
	
	public Groupby(int kwIndex)
	{
		this.kwIndex = kwIndex;
	}
	
	public void setKwIndex(int kwIndex)
	{
		this.kwIndex = kwIndex;
	}
	
	public int getKwIndex()
	{
		return this.kwIndex;
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
