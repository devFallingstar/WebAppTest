package queryprocessing;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class Tpredicate {
	private String name;//the temporal predicate
	private int kwIndex;//the index of obj kw
	private int tpIndex;//the index of the predicate in "TP" array
	private int kwIndexSec;//the index of the second obj kw
	private int periodIndex;//-1: no period,0-first,1-second
//	private ArrayList<String> targetRelName;
	
	private String period;//the time period
	private String From;
	private String To;
	
	private String invalidWord;//invalid word for this predicate
	
	public Tpredicate(String name)
	{
		this.name = name;
		this.kwIndex = -1;
		this.periodIndex = -1;
		this.tpIndex = -1;
		this.period = "";
		this.From = "";
		this.To = "";
		this.kwIndexSec = -1;
		this.invalidWord = "";
//		this.targetRelName = null;
	}
	
//	public void addTargetRelName(String relName)
//	{
//		if(this.targetRelName == null)
//		{
//			this.targetRelName = new ArrayList<String>();
//		}
//		this.targetRelName.add(relName);
//	}
	
	public void setKwIndex(int index)
	{
		this.kwIndex = index;
	}
	
	public void setPeriodIndex(int index)
	{
		this.periodIndex = index;
	}
	
	public int getPeriodIndex()
	{
		return this.periodIndex;
	}
	
	public void setKwIndexSec(int index)
	{
		this.kwIndexSec = index;
	}
	
	public void setTpIndex(int index)
	{
		this.tpIndex = index;
	}
	
	public int getKwIndex()
	{
		return this.kwIndex;
	}
	
	public int getKwIndexSec()
	{
		return this.kwIndexSec;
	}
	
	public int getTpIndex()
	{
		return this.tpIndex;
	}
	
	public String getTpName()
	{
		return this.name;
	}
	
	public String getFrom()
	{
		return this.From;
	}
	
	public String getTo()
	{
		return this.To;
	}
	
	public String getInvalidWord()
	{
		return this.invalidWord;
	}
	
	public void setInvalidWord(String word)
	{
		this.invalidWord = word;
	}
	
//	public void extractPeriod(String period)
//	{
//		int periodLen = period.length();
//		this.period = period;
//		int comma = this.period.indexOf(',');
//		this.From = this.period.substring(1,comma);
//		this.To = this.period.substring(comma+1, periodLen-1);
//	}
	
	public boolean isTwoKw()
	{
		if (this.kwIndexSec == -1)
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	public boolean isSimpleTp()
	{
		if(this.kwIndexSec != -1)
		{
			return false;
		}
		else
		{
			return true;
		}
	}
//	public String getPeriod()
//	{
//		return this.period;
//	}
	
	public boolean extractPeriod(String period)
	{
		this.period = period;
		int strLen = period.length();
		int index = period.indexOf(",");
		if(index == -1)
		{
			this.From = this.To = parseDate(period.substring(1, strLen - 1).trim(), true);
		}
		else
		{
			this.From = parseDate(period.substring(1, index).trim(), true);
			this.To =  parseDate(period.substring(index + 1, strLen - 1).trim(), false);
		}
		if(this.From == null || this.To == null)
			return false;
		return true;
	}
	
	private static String parseDate(String input, boolean defaultminimum)
	{
		ArrayList<SimpleDateFormat> dateformats = new ArrayList<SimpleDateFormat>();
		dateformats.add(new SimpleDateFormat("yyyy-MM-dd"));
		dateformats.add(new SimpleDateFormat("yyyyMMdd"));
		dateformats.add(new SimpleDateFormat("MM/dd/yyyy"));
		Date date = null;
		for(SimpleDateFormat format : dateformats)
		{
			format.setLenient(false);
			try
			{
				date = format.parse(input);
			} 
			catch (ParseException e){}
			if(date != null)
				return getDateString(date, 3, defaultminimum);
		}
		SimpleDateFormat incompleteformat = new SimpleDateFormat("yyyy-MM");
		incompleteformat.setLenient(false);
		try
		{
			date = incompleteformat.parse(input);
		} 
		catch (ParseException e){}
		if(date != null)
			return getDateString(date, 2, defaultminimum);
		
		incompleteformat = new SimpleDateFormat("yyyy");
		incompleteformat.setLenient(false);
		try
		{
			date = incompleteformat.parse(input);
		} 
		catch (ParseException e){}
		if(date != null)
			return getDateString(date, 1, defaultminimum);
		return null;
	}
	
	private static String getDateString(Date date, int component, boolean defaultminimum)
	{
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		
		int year = cal.get(Calendar.YEAR);
		if(component == 1)
		{
			if(defaultminimum)
				cal.set(year, cal.getMinimum(Calendar.MONTH), 1);
			else
				cal.set(year, cal.getMaximum(Calendar.MONTH), 31);
		}
		if(component == 2)
		{
			int month = cal.get(Calendar.MONTH);
			if(defaultminimum)
				cal.set(year,  month, 1);
			else
				cal.set(year,  month, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dateFormat.format(cal.getTime());
	}
	
	public String getPeriodDesc()
	{
		return "[" + this.From + "," + this.To + "]";
	}
	
	public boolean isPeriodExists()
	{
		if(this.period.length()>0)//period exists
		{
			return true;
		}
		return false;
	}
	
	//whether this TP has two KW operands
	public boolean isTpTwoKW()
	{
		if(this.periodIndex==-1)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
