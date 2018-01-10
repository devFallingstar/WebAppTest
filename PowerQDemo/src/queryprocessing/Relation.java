package queryprocessing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import queryprocessing.Constant.RelType;

public class Relation
{
	private String name;
	private String alias;
	private String[] attrlist;
	private int[] outputlist;
	private int[] verifylist;
	private int[] textlist;
	private int[] key;
	
	private ArrayList<Dependency> outfk;
	private ArrayList<Dependency> infk;
	private HashSet<String> attrset;
	private RelType type;
//	private String remark;//sql for view
	
	private int[][]tempAttrList;
	private Constant.TemporalType tempType;
	
	public Relation(String name, String alias, String[] attrlist, int[] outputlist, int[] verifylist, int[] textlist, int[] key, RelType type,  Constant.TemporalType tempType,int [][] temporalDetail, int index)
	{
		this.name = name;
		this.alias = alias;
		this.attrlist = attrlist;
		this.outputlist = outputlist;
		this.verifylist = verifylist;
		this.textlist = textlist;
		this.key = key;
		this.type = type;
		this.tempType = tempType;
//		this.remark = null;
		this.outfk = null;
		this.infk = null;
		this.attrset = new HashSet<String>(Arrays.asList(this.attrlist));
		
		int detailLen = temporalDetail.length;
		int count = 0;
		for (int i = 0; i < detailLen; i++)
		{
			if (temporalDetail[i][0] == index)
			{
				count++;
			}
		}
		tempAttrList = new int[count][3];
		count = 0;
		for (int i = 0; i < detailLen; i++)
		{
			if (temporalDetail[i][0] == index)
			{
				tempAttrList[count][0] = temporalDetail[i][2];
				tempAttrList[count][1] = temporalDetail[i][3];
				tempAttrList[count][2] = temporalDetail[i][1]; 
				count++;
			}
		}
	}
	
//	public void setRemark(String remark)
//	{
//		this.remark = remark;
//	}
//	
//	public String getRemark()
//	{
//		return this.remark;
//	}
	
	public void addInFK(Dependency fk)
	{
		if(this.infk == null)
		{
			this.infk = new ArrayList<Dependency>();
		}
		this.infk.add(fk);
	}
	
	public void addOutFK(Dependency fk)
	{
		if(this.outfk == null)
		{
			this.outfk = new ArrayList<Dependency>();
		}
		this.outfk.add(fk);
	}
	
	public Dependency getOutFK(int index)
	{
		if(this.outfk != null)
		{
			return this.outfk.get(index);
		}
		return null;
	}
	
	public Dependency getInFK(int index)
	{
		if(this.infk != null)
		{
			return this.infk.get(index);
		}
		return null;
	}
	
	
	public Relation[] getRefRel()
	{
		int len = this.getOutFKNum();
		Relation[] rels = new Relation[len];
		for(int i = 0; i < len; i++)
		{
			Dependency fk = this.getOutFK(i);
			rels[i] = fk.getRelR();
		}
		return rels;
	}
	
	public Relation[] getRefedRel()
	{
		int len = this.getInFKNum();
		Relation[] rels = new Relation[len];
		for(int i = 0; i < len; i++)
		{
			Dependency fk = this.getInFK(i);
			rels[i] = fk.getRelL();
		}
		return rels;
	}
	
	public int getOutFKNum()
	{
		if(this.outfk != null)
		{
			return this.outfk.size();
		}
		return 0;
	}
	
	public int getInFKNum()
	{
		if(this.infk != null)
		{
			return this.infk.size();
		}
		return 0;
	}
	
	public RelType getRelType()
	{
		return this.type;
	}
	
	public String getRelName()
	{
		return this.name;
	}
	
	public String getRelAlias()
	{
		return this.alias;
	}
	
	public int getAttrNum()
	{
		return this.attrlist.length;
	}
	
	public int getOutputAttrNum()
	{
		return this.outputlist.length;
	}
	
	public int getVerifyAttrNum()
	{
		return this.verifylist.length;
	}
	
	public int getTextAttrNum()
	{
		return this.textlist.length;
	}
	
	public int getKeyAttrNum()
	{
		return this.key.length;
	}
	
	public String getKeyAttrName(int index)
	{
		int keyAttrIndex = this.key[index];
		return this.attrlist[keyAttrIndex];
	}
	
	public String getOutputAttrName(int index)
	{
		int outputAttrIndex = this.outputlist[index];
		return this.attrlist[outputAttrIndex];
	}
	
	public String getVerifyAttrName(int index)
	{
		int verifyAttrIndex = this.verifylist[index];
		return this.attrlist[verifyAttrIndex];
	}
	
	public String getTextAttrName(int index)
	{
		int textAttrIndex = this.textlist[index];
		return this.attrlist[textAttrIndex];
	}
	
	public String getAttrName(int index)
	{
		return this.attrlist[index];
	}
	
	public boolean containAttr(String attr)
	{
		if(this.attrset.contains(attr))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean isTextAttr(String attr)
	{
		for(int i = 0; i < this.getTextAttrNum(); i++)
		{
			if(this.getTextAttrName(i).equals(attr))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isOutputAttr(String attr)
	{
		for(int i = 0; i < this.getOutputAttrNum(); i++)
		{
			if(this.getOutputAttrName(i).equals(attr))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isVerifyAttr(String attr)
	{
		for(int i = 0; i < this.getVerifyAttrNum(); i++)
		{
			if(this.getVerifyAttrName(i).equals(attr))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isKeyAttr(String attr)
	{
		for(int i = 0; i < this.getKeyAttrNum(); i++)
		{
			if(this.getKeyAttrName(i).equals(attr))
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean isFKAttr(String attr)
	{
		for(int i = 0; i < this.getOutFKNum(); i++)
		{
			Dependency fk = this.getOutFK(i);
			if(fk.getAttrL().equals(attr))
			{
				return true;
			}
		}
		return false;
	}
	
	public int[][] getTempAttrList()
	{
		return this.tempAttrList;
	}
	
	public Constant.TemporalType getRelTempType()
	{
		return this.tempType;
	}
}
