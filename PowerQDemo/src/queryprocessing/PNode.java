package queryprocessing;

import java.util.ArrayList;

import queryprocessing.Constant.NodeType;

public class PNode
{
	private int ormNodeId;
	private Pattern pattern;
	
	public PNode(int ormNodeId, Pattern pattern)
	{
		this.ormNodeId = ormNodeId;
		this.pattern = pattern;
	}
	
	public NodeType getType(ORMGraph ormgraph)
	{
		Node ormNode = ormgraph.getNode(this.ormNodeId);
		return ormNode.getNodeType();
	}
	
	public PNode getCopy()
	{
		return new PNode(this.ormNodeId, this.pattern);
	}
	
	
	public int getKwNum()
	{
		return this.pattern.getKwNum();
	}
	
	public int getKwIndex(int index)
	{
		return this.pattern.getKwIndex(index);
	}
	
	public boolean isTrival()
	{
		if(this.pattern == null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean isTargetNode()
	{
		if (this.isTrival())
		{
			return false;
		}
		int outputNum = this.getOutputNum();
		int fnNum = this.getFnNum();
		boolean tp = this.isTemporalNode();
		if((outputNum > 1 && !tp)|| fnNum > 0 ||  outputNum ==1)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean isConditionNode()
	{
		if (this.isTrival())
		{
			return false;
		}
		int condNum = this.getConditionNum();
		int gpNum = this.getGpNum();
		if(condNum > 0 || gpNum > 0 || this.isTemporalNode())
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public int getORMNodeId()
	{
		return this.ormNodeId;
	}
	
	public int getOutputNum()
	{
		return this.pattern.getKw4OutputNum();
	}
	
	public String[] getOutput(int index, Keyword[] kwArray)
	{
		String[] output = new String[2];
		int kwIndex = this.pattern.getKwIndex4Output(index);
		int tagIndex = this.pattern.getTagIndex4Output(index);
		
		Keyword kw = kwArray[kwIndex];
		String name = kw.getTagName(tagIndex);
		String attr = kw.getTagAttr(tagIndex);
		String kwContent = kw.getContent();
		
		if(kwContent.equalsIgnoreCase(name))
		{
			output[0] = "name";
			output[1] = name;
		}
		else
		{
			output[0] = "attr";
			output[1] = attr;
		}
		return output;
	}
	
	public boolean hasAttrOutput(Keyword[] kwArray)
	{
		int targetNum = this.getOutputNum();
		for(int i = 0; i < targetNum; i++)
		{
			String[] target = this.getOutput(i, kwArray);
			if(target[0].equals("attr"))
			{
				return true;
			}
		}
		return false;
	}
	
	public int getConditionNum()
	{
		return this.pattern.getKw4ConditionNum();
	}
	
	public String[] getConditionInfo(int index, Keyword[] kwArray, boolean quotePhrase)
	{
		String[] cond = new String[3];
		int kwIndex = this.pattern.getKwIndex4Condition(index);
		int tagIndex = this.pattern.getKwTagIndex4Condition(index);
		Keyword kw = kwArray[kwIndex];
		
		cond[0] = kw.getTagAttr(tagIndex);//attr
		cond[2] = String.valueOf(this.pattern.getKwTag4Index(index));//tag index
		if(kw.isPhrase() && quotePhrase)
		{
			cond[1] = "\"" + kw.getTagVal(tagIndex) + "\"";
		}
		else
		{
			cond[1] = kw.getTagVal(tagIndex);
		}
		return cond;
	}
	
	public int getFnNum()
	{
		return this.pattern.getFn4ConditionNum();
	}
	
	public int getFnIndex(int index)
	{
		return this.pattern.getFnIndex4Condition(index);
	}
	
	
	public int getFnTagIndex(int index)
	{
		return this.pattern.getFnTagIndex4Condition(index);
	}
	
	public String getCascadeFnName(int index, Keyword[] kwArray, Function[] fnArray)
	{
		int fnIndex = this.getFnIndex(index);
		Function fn = fnArray[fnIndex];
		
		int fnNum = this.getFnNum();
		for(int i = 0; i < fnNum; i++)
		{
			int sfnIndex = this.getFnIndex(i);
			Function sfn = fnArray[sfnIndex];
			if(!sfn.isSimpleFn() && sfn.getFnIndex() == fnIndex)
			{
				return getCascadeFnName(i, kwArray, fnArray) + " " + fn.getFnName();
			}
		}
		return fn.getFnName();
	}
	
	public String[] getSimpleFnInfo(Function func, Keyword[] kwArray, Function[] fnArray)
	{
		for(int i = 0; i < this.getFnNum(); i++)
		{
			int fnIndex = this.getFnIndex(i);
			Function fn = fnArray[fnIndex];
			if(fn == func)
			{
				return getSimpleFnInfo(i, kwArray, fnArray);
			}
		}
		return getSimpleFnInfo(0, kwArray, fnArray);
	}
	
	public String[] getSimpleFnInfo(int index, Keyword[] kwArray, Function[] fnArray)
	{
		String[] fnInfo = new String[2];
		int fnIndex = this.getFnIndex(index);
		Function fn = fnArray[fnIndex];
		int kwIndex = fn.getKwIndex();
		Keyword kw = kwArray[kwIndex];
		int tagIndex = this.getFnTagIndex(index);
		String name = kw.getTagName(tagIndex);
		String attr = kw.getTagAttr(tagIndex);
		String kwContent = kw.getContent();
		
		if(kwContent.equalsIgnoreCase(name))
		{
			fnInfo[0] = "name";
			fnInfo[1] = name;
		}
		else
		{
			fnInfo[0] = "attr";
			fnInfo[1] = attr;
		}
		return fnInfo;
	}
	
	public int getGpNum()
	{
		return this.pattern.getGp4ConditionNum();
	}
	
	public int getGpKwIndex(int index)
	{
		return this.pattern.getGpIndex4Condition(index);
	}
	
	public int getGpKwTagIndex(int index)
	{
		return this.pattern.getGpTagIndex4Condition(index);
	}
	
	public String[] getGpInfo(int index, Keyword[] kwArray)
	{
		String[] gpInfo = new String[2];
		int kwIndex = this.getGpKwIndex(index);
		int tagIndex = this.getGpKwTagIndex(index);
		Keyword kw = kwArray[kwIndex];
		String name = kw.getTagName(tagIndex);
		String attr = kw.getTagAttr(tagIndex);
		String kwContent = kw.getContent();
		
		if(kwContent.equalsIgnoreCase(name))
		{
			gpInfo[0] = "name";
			gpInfo[1] = name;
		}
		else
		{
			gpInfo[0] = "attr";
			gpInfo[1] = attr;
		}
		return gpInfo;
	}

	public boolean isMatchKw(String str, Keyword[] kwArray)
	{
		int kwNum = this.getKwNum();
		for(int i = 0; i < kwNum; i++)
		{
			int kwIndex = this.getKwIndex(i);
			Keyword kw = kwArray[kwIndex];
			String kwContent = kw.getContent();
			if(str.equalsIgnoreCase(kwContent))
			{
				return true;
			}
		}
		return false;
	}

	public void setPattern(Pattern ptn)
	{
		this.pattern = ptn;
	}
	
	public boolean getGroupbyID()
	{
		return this.pattern.getGroupbyID();
	}
	
	public int [] getKwgroup()
	{
		if (this.pattern==null)
		{
			return null;
		}
		return this.pattern.getKwgroup();
	}
	
	public boolean isTemporalNode(DBinfo dbinfo, ORMGraph ormgraph)//whether this node is temporal or not?
	{
		//if (this.isTrival()) return false;
		Node ormNode = ormgraph.getNode(this.ormNodeId);
		
		if(ormNode.isTemporalRel(dbinfo))//relId
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean isTemporalNode()
	{
		if (this.isTrival()) 
			return false;
		else
		    return this.pattern.isTpPattern();
	}
	
//	public void setPatternTp(Pattern pattern,String Name)
//	{
//		if (this.pattern == null)
//		{
//			this.pattern = pattern.getCopy();
//		}
//		else
//		{
//			this.pattern.setTp(pattern);
//		}
//		this.pattern.setTp4TargetRel(Name);
//	}
	
//	public void setPatternTp(Pattern pattern,boolean Sec)
//	{
//		if (this.pattern == null)
//		{
//			this.pattern = pattern.getCopy();
//		}
//		else
//		{
//			this.pattern.setTp(pattern,Sec);
//		}
//		
//	}
	
	public void setPatternTp(Pattern pattern,boolean Sec)
	{
		if (this.pattern == null)
		{
			this.pattern = pattern.getCopy();
		}
		else
		{
			this.pattern.setTp(pattern,Sec);
		}
		
	}
	
	public void setPatternTp(Pattern pattern,String Name,int[] AttrList, int id, boolean SecKw, boolean infer, DBinfo dbinfo)
	{
		if (this.pattern == null)//copy info for both first and second kw
		{
			this.pattern = pattern.getCopy();//copy all things from this parameter
		}
		else
		{
			this.pattern.setTp(pattern,SecKw);//set the tp part
		}
		this.pattern.setTpInfer(SecKw,infer);
		this.pattern.setTp4TargetRel(Name, SecKw);//set the relation name
		this.pattern.setTpAttr(AttrList, SecKw, dbinfo);
		this.pattern.setId(id,SecKw);
	}
	
	public void setTpAttr(int[] AttrList, DBinfo dbinfo)
	{
		this.pattern.setTpAttr(AttrList, false, dbinfo);
	}
	
//	public void setTpAttr(int[] AttrList,DBinfo dbinfo)
//	{
//		this.pattern.setTpAttr(AttrList, dbinfo);
//	}
	
	public PNode getDeepCopy()
	{
		if (this.pattern == null)
		{
			return new PNode(this.ormNodeId, null);
		}
		else
		{
			return new PNode(this.ormNodeId, this.pattern.getCopy());
		}
	}
	
	public Relation[] getTempRelList(ORMGraph ormgraph)
	{
		Node ormNode = ormgraph.getNode(this.ormNodeId);
		return ormNode.getTempRelList();
	}

	public String[] getTpInfo(Keyword[] kwArray, Tpredicate[] tpArray)
	{
		return this.pattern.getTpInfo(tpArray, kwArray);
	}
	
	public int getTpIndex(Tpredicate[] tpArray)
	{
		return this.pattern.getTpIndex();
	}
	
	public boolean isAggregateNode()
	{
		int fnLen = this.pattern.getFn4ConditionNum();
		int gpLen = this.pattern.getGp4ConditionNum();
		boolean groupbyID = this.pattern.getGroupbyID();
		if (fnLen>0 || gpLen>0 || groupbyID)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean isTemporalConditionNode()
	{
		if (this.isTrival()) return false;
		return this.pattern.isTpPattern();
	}
	
	public String getTp4TargetRel()
	{
		return this.pattern.getTp4TargetRel();
	}
	
	public int getTpKw4condition()
	{
		return this.pattern.getTpKwIndex();
	}
	
	public boolean isKw4condContains(String relName,ORMGraph ormgraph,Keyword[] kwArray, DBinfo dbinfo)
	{
		int kw4condLen = this.pattern.getKw4ConditioNum();
		Node ormNode = ormgraph.getNode(this.ormNodeId);
		for (int i=0;i<kw4condLen;i++)
		{
			String[] condDesc = this.getConditionInfo(i, kwArray, true);
			String attr = condDesc[0];
			if(!ormNode.isSingleValuedAttr(attr))
			{
				int compRelIndex = ormNode.getCompRel4Attr(attr);
				if(ormNode.getCompRelName(compRelIndex) == relName)
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public int getConditionKw(int index)
	{
		return this.pattern.getKwIndex4Condition(index);
	}
	
	public boolean isTemporalSequenceNode()
	{
		return this.pattern.isTpSequence();
	}
	
	public int getTpIndex()
	{
		return this.pattern.getTpIndex();
	}
	
	public int getTpSecKw4condition()
	{
		return this.pattern.getTpSecKwIndex();
	}
	
	public ArrayList<StringBuffer> getTpredicateInfo(Tpredicate[] tpArray, DBinfo dbinfo, int nodeId)
	{
		ArrayList<StringBuffer> TemporalStringList =  new ArrayList<StringBuffer>();
		
		//for temporal constraints
		int tpIndex =-1 ;
		//get the temporal attr
//			int nodeId = this.getORMNodeId();//table name
		int[] tremporalAttr = this.pattern.getTpAttr();//temporal attribute
		String[] tAttrString = new String[2];
		String[] period = new String[2];
		String[][] tpInterpretation = new String[2][2];
//			StringBuffer tWhere = new StringBuffer("");
		tAttrString[0] = dbinfo.getAttr(nodeId,tremporalAttr[0]);
		tAttrString[1] = dbinfo.getAttr(nodeId,tremporalAttr[1]);
		//get the operator & time
		Tpredicate tp = tpArray[this.getTpIndex()];
		tpIndex = tp.getTpIndex();
		tpInterpretation = Constant.getTpInterpretation(tpIndex);
		period[0] = tp.getFrom();
		period[1] = tp.getTo();
		//show the strings
		for (int m = 0;m<2;m++)//generate the temporal string.
		{
			for (int n = 0;n<2;n++)
			{
				if (tpInterpretation[m][n]!=null && tAttrString[m]!=null)
				{
					StringBuffer TemporalString = new StringBuffer("");
					TemporalString.append("`").append(tAttrString[m]).append("`").append(tpInterpretation[m][n]).append("'").append(period[n]).append("' ");
					TemporalStringList.add(TemporalString);
				}
			}
		}
		return TemporalStringList;
	}
	
	public ArrayList<StringBuffer> getSequenceTpInfo(Tpredicate tp,DBinfo dbinfo, int nodeId, int compLabel)
	{
		ArrayList<StringBuffer> TemporalStringList =  new ArrayList<StringBuffer>();
		int tpIndex =-1 ;
		int compSecLabel = compLabel+1;
		//get the temporal attr
		int[] tremporalAttr = this.pattern.getTpAttr();//temporal attribute
		String[] tAttrString = new String[2];
		String[][] tpInterpretation = new String[2][2];
//		StringBuffer tWhere = new StringBuffer("");
		tAttrString[0] = dbinfo.getAttr(nodeId,tremporalAttr[0]);
		tAttrString[1] = dbinfo.getAttr(nodeId,tremporalAttr[1]);
		//get the operator & time
		tpIndex = tp.getTpIndex();
		tpInterpretation = Constant.getTpInterpretation(tpIndex);
		//show the strings
		for (int m = 0;m<2;m++)//generate the temporal string.
		{
			for (int n = 0;n<2;n++)
			{
				if (tpInterpretation[m][n]!=null && tAttrString[m]!=null)
				{
					StringBuffer TemporalString = new StringBuffer("");
					TemporalString.append("R").append(compLabel).append(".`").append(tAttrString[m]).append("`").append(tpInterpretation[m][n]).append(" R").append(compSecLabel).append(".`").append(tAttrString[n]).append("` ");
					TemporalStringList.add(TemporalString);
				}
			}
		}
		return TemporalStringList;
	}
	
	public void setPatternSimpleTp(Pattern pattern,String Name, boolean infer)//tp with time condition
	{
		if (this.pattern == null)//copy info for both first and second kw
		{
			this.pattern = pattern.getCopy();//copy all things from this parameter
		}
		else
		{
			this.pattern.setTp(pattern,false);//set the tp part
		}
		this.pattern.setTpInfer(false,infer);
		this.pattern.setTp4TargetRel(Name, false);//set the relation name
//		this.pattern.setTpAttr(AttrList, SecKw);
		this.pattern.setId(-1,false);
	}
	
	public boolean isTpSequenceFirstNode()
	{
		return this.pattern.isTpSequenceFirst();
	}
	
	public boolean isTpSequenceSecNode()
	{
		return this.pattern.isTpSequenceSec();
	}
	
	public Pattern getPattern()
	{
		return this.pattern;
	}
	
	//find the temporal attribute name for this node
	public String[] getTpAttrName(DBinfo dbinfo, int coreRelId, boolean Sec)
	{
		int[] tpAttr;
		if (!Sec)
		{
			tpAttr= this.pattern.getTpAttr();//get the index
		}
		else
		{
			tpAttr= this.pattern.getTpSecAttr();//get the index
		}
		String[] tpAttrString = new String[2];
		//node id, 
		tpAttrString[0] = dbinfo.getAttr(coreRelId,tpAttr[0]);
		tpAttrString[1] = dbinfo.getAttr(coreRelId,tpAttr[1]);
		return tpAttrString;
	}
	
	//get the tag info of the kw near the tp.
	public String[] getTpConditionInfo(Keyword[] kwArray, boolean Sec)
	{
		String[] cond = new String[3];//0-RelName,1-AttrName,2-AttrValue
		int kwIndex;
		int tagIndex;
		if(!Sec)
		{
			kwIndex = this.getTpKw4condition();
			tagIndex = this.getTpTagIndex();
		}
		else
		{
			kwIndex = this.getTpSecKw4condition();
			tagIndex = this.getTpSecTagIndex();
		}
		Keyword kw = kwArray[kwIndex];
		cond[0] = kw.getTagName(tagIndex);
		cond[1] = kw.getTagAttr(tagIndex);
		cond[2] = kw.getTagVal(tagIndex);
		
		return cond;
	}
	
	public int getTpTagIndex()
	{
		return this.pattern.getTpTagIndex();
	}
	
	public int getTpSecTagIndex()
	{
		return this.pattern.getTpSecTagIndex();
	}
	
	public int getKwLabel(int kwIndex)
	{
		return this.pattern.getKwLabel(kwIndex);
	}
	
	public boolean getTpInfer(boolean Sec)
	{
		return this.pattern.getTpInfer(Sec);
	}
	
	public void setKwLabel(int index, int label)
	{
		this.pattern.setKwLabel(index, label);
	}
	
	public int getCondKwPosition(int index)
	{
		int kwIndex = this.pattern.getKwIndex4Condition(index);
		int[] kwGroup = this.pattern.getKwgroup();
		for(int i=0; i<kwGroup.length; i++)
		{
			if(kwGroup[i]==kwIndex) return i;
		}
		return -1;
	}
	
	public int getOutputKwPosition(int index)
	{
		int kwIndex = this.pattern.getKwIndex4Output(index);
		int[] kwGroup = this.pattern.getKwgroup();
		for(int i=0; i<kwGroup.length; i++)
		{
			if(kwGroup[i]==kwIndex) return i;
		}
		return -1;
	}
}
