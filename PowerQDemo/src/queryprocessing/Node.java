package queryprocessing;


import java.util.ArrayList;

import queryprocessing.Constant.NodeType;

public class Node
{
	private String name;
	private Relation rel;
	private ArrayList<Relation> comprel;
	private ArrayList<Relation> tempComprel;
	private NodeType type;
	
	public Node(Relation rel)
	{
		this.rel = rel;
		this.name = rel.getRelAlias();
		this.comprel = null;
		this.tempComprel = null;
		switch(rel.getRelType())
		{
			case Object:
				this.type = NodeType.Object;
				break;
			case Relationship:
				this.type = NodeType.Relationship;
				break;
			case Mix:
				this.type = NodeType.Mix;
				break;
			default:
				break;
		}
	}
	
	public void addCompRel(Relation rel)
	{
		if(this.comprel == null)
		{
			this.comprel = new ArrayList<Relation>();
		}
		this.comprel.add(rel);
	}
	
	public void addTempCompRel(Relation rel)
	{
		if(this.tempComprel == null)
		{
			this.tempComprel = new ArrayList<Relation>();
		}
		this.tempComprel.add(rel);
	}
	
	public String getNodeName()
	{
		return this.name;
	}
	public Relation getCoreRelation()
	{
		return this.rel;
	}
	
	public int getCompRelNum()
	{
		if(this.comprel != null)
		{
			return this.comprel.size();
		}
		return 0;
	}
	
	public Relation getCompRelation(int index)
	{
		if(this.comprel != null)
		{
			return this.comprel.get(index);
		}
		return null;
	}
	
	public NodeType getNodeType()
	{
		return this.type;
	}
	
	public int getCoreRelIndex(DBinfo dbinfo)
	{
		int relNum = dbinfo.getRelNum();
		for(int i = 0; i < relNum; i++)
		{
			Relation rel = dbinfo.getRel(i);
			if(rel == this.rel)
			{
				return i;
			}
		}
		return -1;
	}
	
	public int getCompRelIndex(DBinfo dbinfo, int index)
	{
		Relation compRel = this.comprel.get(index);
		int relNum = dbinfo.getRelNum();
		for(int i = 0; i < relNum; i++)
		{
			Relation rel = dbinfo.getRel(i);
			if(rel == compRel)
			{
				return i;
			}
		}
		return -1;
	}
	
	public boolean isSingleValuedAttr(String attr)
	{
		if(this.rel.containAttr(attr))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public int getCompRel4Attr(String attr)
	{
		int compRelNum = this.comprel.size();
		for(int i = 0; i < compRelNum; i++)
		{
			Relation compRel = this.comprel.get(i);
			if(compRel.containAttr(attr))
			{
				return i;
			}
		}
		return -1;
	}
	
	public boolean isTemporalRel(DBinfo dbinfo)
	{
		int relId = getCoreRelId(dbinfo);
		if (dbinfo.getTemporalType(relId)!= Constant.TemporalType.GeneralRelation)
		{
			return true;
		}
		else
		{
			int compRelNum = getCompRelNum();
			if(compRelNum != 0)//contains component relation
			{
				for (int i=0;i<compRelNum;i++)
				{
					int compRelId = getCompRelId(dbinfo,i);
					if (dbinfo.getTemporalType(compRelId)!= Constant.TemporalType.GeneralRelation)
					{
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public int getCompRelId(DBinfo dbinfo, int index)
	{
		Relation compRel = this.comprel.get(index);
		int relNum = dbinfo.getRelNum();
		for(int i = 0; i < relNum; i++)
		{
			Relation rel = dbinfo.getRel(i);
			if(rel == compRel)
			{
				return i;
			}
		}
		return -1;
	}
	
	public int getCoreRelId(DBinfo dbinfo)
	{
		int relNum = dbinfo.getRelNum();
		for(int i = 0; i < relNum; i++)
		{
			Relation rel = dbinfo.getRel(i);
			if(rel == this.rel)
			{
				return i;
			}
		}
		return -1;
	}
	
	public int getCompRel4Name(String name)//return the index of the component relation (which contains the "attr" as the attribute name) in the array
	{
		if(this.comprel != null)
		{
			int compRelNum = this.comprel.size();
			for(int i = 0; i < compRelNum; i++)//for each component relation
			{
				Relation compRel = this.comprel.get(i);
				if(compRel.getRelName()== name)
				{
					return i;
				}
			}
		}
		return -1;
	}
	
	public Relation getCompRel(int index)
	{
		return this.comprel.get(index);
	}
	
	public Relation[] getTempRelList()//get a list of temporal relation of this node 
	{
		int relNum = this.getCompRelNum()+1;
		Relation[] tempRelList = new Relation[relNum];
		int tempRelNum = 0;
		
		if (this.rel.getRelTempType()!= Constant.TemporalType.GeneralRelation)
		{
			tempRelList[tempRelNum] = this.rel;
			tempRelNum++;
		}
		int tempCompRelNum = this.getTempCompRelNum();
		for (int i=0; i<tempCompRelNum; i++)
		{
			tempRelList[tempRelNum]  = this.getCompRelation(i);
			tempRelNum++;
		}
		
		Relation[] tempRelNameList = new Relation[tempRelNum];
		System.arraycopy(tempRelList, 0, tempRelNameList, 0, tempRelNum);
		return tempRelList;
	}
	
	public int getTempCompRelNum()
	{
		if(this.tempComprel == null)
		{
			return 0;
		}
		else
		{
			return this.tempComprel.size();
		}
	}
	
	public String getTempCompRelName(int index)
	{
		return this.tempComprel.get(index).getRelName();
	}
	
	public String getCompRelName(int index)
	{
		return this.comprel.get(index).getRelName();
	}
	
	public Relation getCoreRel()
	{
		return this.rel;
	}
	
//	public String getCompRelNameDebug(int index)
//	{
//		return this.comprel.get(index).getRelName();
//	}
}
