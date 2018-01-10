package queryprocessing;

//import java.sql.ResultSet;
//import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class Pattern
{
private int[] kwGroup;//the kw index for this tag group
	
	private int[] kw4condition;//index of this keyword in the query
	private int[] kwtag4condition;//tag of this keyword
	private int[] kwtag4index;//tag of this keyword
	private int[] kwlabel4condition;//relation label for this tag
	
	private int[] fn4condition;//index of this function in the query or function array
	private int[] fntag4condition; //the tag of the keyword after this function
	
	//temporal related
	private boolean tpAnnotate;//tp index in tp array
	private boolean tpInfer;//tp index in tp array
	private int tp4condition;//tp index in TPconstant
	private String tp4targetRel;
	private String tp4targetAttr;
	private int tpKw4condition;//kw index in the query
	private int tptag4condition;//which tag for this kw
	private int[] tpAttr;//???
	private int idFirst;//to find the second kw
	
	//temporal_secKw
//	private int tpSec4condition;//index of tp
	private boolean tpSecInfer;//tp index in tp array
	private int tpSecKw4condition;//index of this kw in the query
	private int tpSectag4condition;//tag of the kw
	
	private String tpSec4targetRel;//relation name for the second kw
	private String tpSec4targetAttr;
	private int[] tpSecAttr;//start,end for the second kw
	private int idSec;//to find the first kw
	
	//kw4output
	private int[] kw4output;//
	private int[] kwtag4output;
	
	//group by
	private int[] gp4condition;
	private int[] gptag4condition;
	private boolean groupbyID;//
	
	public Pattern()
	{
		this.kwGroup = null;
		this.kw4condition = null;
		this.kwtag4condition = null;
		this.kwtag4index = null;
		this.kwlabel4condition = null
				;
		this.kw4output = null;
		this.kwtag4output = null;
		
		this.fn4condition = null;
		this.fntag4condition = null;
		
		this.tpAnnotate = false;
		this.tpInfer = false;
		this.tpSecInfer = false;
		this.tp4condition = -1;
		this.tpKw4condition = -1;
		this.tptag4condition = -1;
		
		this.tpSecKw4condition = -1;
		this.tpSectag4condition = -1;
		this.tpAttr = new int[3];//index of (from,to,attr)
		this.tpSecAttr = new int[3];
		
		this.tp4targetRel = null;
		this.tpSec4targetRel = null;
		this.tp4targetAttr = null;
		this.tpSec4targetAttr = null;
		
		this.gp4condition = null;
		this.gptag4condition = null;
		this.groupbyID = false;
		
		
	}
	
//For temporal predicate
//	public Pattern(int[] combTag, int[] kwGroup, Keyword[] kwArray, Tpredicate[] tpArray)//combTag - for each kw the index of the tag in the corresponding tagList
//	{
//		this.kwGroup = kwGroup;
//		int tpNum = tpArray.length;
//		
//		//set the default value
//		this.tp4condition = -1;
//		this.tpKw4condition = -1;
//		this.tptag4condition = -1;
////		this.tpSec4condition = -1;
//		this.tpSecKw4condition = -1;
//		this.tpSectag4condition = -1;
//		this.tpAttr = new int[3];
////		this.tempAttrName = "";
//		
//		for (int i = 0; i < tpNum; i++ )
//		{
//			Tpredicate tp = tpArray[i];
//			int kwIndex = isTpredicateApply(tp);
//			if(kwIndex != -1) 
//			{
//				this.tp4condition = i;
//				this.tpKw4condition = kwIndex;
//				this.tptag4condition = combTag[kwIndex];
//				
//				//whether this tp have a second kw
//				if(tp.getKwIndexSec()!=-1)//some problem
//				{
//					//get the second kw index
//					int secKwIndex = tp.getKwIndexSec();
//					this.tpSec4condition = i;//the index of the tp
//					this.tpSecKw4condition = secKwIndex;
//					this.tpSectag4condition = combTag[tp.getKwIndexSec()];
//				}
//				break;
//			}
//			
//		}
//	}
	
	//for temporal predicate 
	public Pattern(int[] combTag, int[] kwGroup, Keyword[] kwArray, Tpredicate tp, int index)//combTag - for each kw the index of the tag in the corresponding tagList
	{
		this.kwGroup = kwGroup;
//		int tpNum = tpArray.length;
		
		//set the default value
		this.tpAnnotate = false;
		this.tpInfer = false;
		this.tpSecInfer = false;
		this.tp4condition = -1;
		this.tpKw4condition = -1;
		this.tptag4condition = -1;
		this.tpSecKw4condition = -1;
		this.tpSectag4condition = -1;
		this.tpAttr = new int[3];
		this.tpSecAttr = new int[3];
		this.tp4targetRel = null;
		this.tpSec4targetRel = null;
		this.tp4targetAttr = null;
		this.tpSec4targetAttr = null;
		
		int kwIndex = isTpredicateApply(tp);
		if(kwIndex != -1) 
		{
//			this.tp4condition = index;
			this.tp4condition = 0;//set the tp name
			this.tpKw4condition = kwIndex;
			this.tptag4condition = combTag[kwIndex];
			
			//whether this tp have a second kw
			if(tp.getKwIndexSec()!=-1)//some problem
			{
				//get the second kw index
//				int secKwIndex = tp.getKwIndexSec();
//				this.tpSec4condition = index;//the index of the tp
				this.tpSecKw4condition = tp.getKwIndexSec();//set the second kw
//				this.tpSecKw4condition = secKwIndex;
				this.tpSectag4condition = combTag[tp.getKwIndexSec()];
			}
			this.tpAnnotate = true;
		}
	}
	
	public Pattern augmentPattern()
	{
		Pattern augptn = new Pattern();
		augptn.kwGroup = this.kwGroup;
		augptn.kw4condition = this.kw4condition;
		augptn.kwtag4condition = this.kwtag4condition;
		augptn.kwtag4index = this.kwtag4index;
		augptn.kwlabel4condition = this.kwlabel4condition;
		
		
		augptn.kw4output = this.kw4output;
		augptn.kwtag4output = this.kwtag4output;
		
		augptn.fn4condition = this.fn4condition;
		augptn.fntag4condition = this.fntag4condition;
		
		augptn.tpAnnotate = this.tpAnnotate;
		augptn.tpInfer = this.tpInfer;
		augptn.tpSecInfer = this.tpSecInfer;
		augptn.tp4condition = this.tp4condition;
		augptn.tpKw4condition = this.tpKw4condition;
		augptn.tptag4condition = this.tptag4condition;
		
		augptn.idFirst = this.idFirst;
		augptn.idSec = this.idSec;
		
//		augptn.tpSec4condition = this.tpSec4condition;
		augptn.tpSecKw4condition = this.tpSecKw4condition;
		augptn.tpSectag4condition = this.tpSectag4condition;
		
		augptn.tpAttr = this.tpAttr;
		augptn.tpSecAttr = this.tpSecAttr;
		
		augptn.tp4targetRel = this.tp4targetRel;
		augptn.tpSec4targetRel = this.tpSec4targetRel;
		augptn.tp4targetAttr = this.tp4targetAttr;
		augptn.tpSec4targetAttr = this.tpSec4targetAttr;
		
		augptn.gp4condition = this.gp4condition;
		augptn.gptag4condition = this.gptag4condition;
		augptn.groupbyID = true;
		return augptn;
	}
	
	public Pattern(int[] combTag, int[] kwGroup, Keyword[] kwArray, Function[] fnArray, Groupby[] gpArray, ORMGraph ormgraph)
	{
		this.kwGroup = kwGroup;
		this.groupbyID = false;
		
		//set the default value
		this.tpAnnotate = false;
		this.tpInfer = false;
		this.tpSecInfer = false;
		this.tpKw4condition = -1;
		this.tptag4condition = -1;
		this.tpSecKw4condition = -1;
		this.tpSectag4condition = -1;
		this.tpAttr = new int[3];
		this.tpSecAttr = new int[3];
		this.tp4targetRel = null;
		this.tpSec4targetRel = null;
		this.tp4targetAttr = null;
		this.tpSec4targetAttr = null;
		
		analyzeKwArray(combTag, kwGroup, kwArray, ormgraph);
		analyzeKwCondition(kwArray);
		analyzeFnGpArray(combTag, kwGroup, kwArray, fnArray, gpArray);
		updateOutputKw(fnArray);
	}
	
	private void analyzeKwCondition( Keyword[] kwArray)
	{
		int condNum = this.kw4condition.length;
		this.kwtag4index = new int[condNum];//initialize
		//create a map to store the attr_name
		HashMap<String, Integer> hmap = new HashMap<String, Integer>();
		
		for(int i=0; i<condNum; i++)//count the appearance of each attribute
		{
			int kwIndex = this.kw4condition[i];
			int tagIndex = this.kwtag4condition[i]; 
			Keyword kw = kwArray[kwIndex];
			String tagAttr = kw.getTagAttr(tagIndex);
			if(hmap.get(tagAttr)!=null)
			{
				int index = hmap.get(tagAttr);
				hmap.put(tagAttr, index+1);
			}
			else
			{
				hmap.put(tagAttr, 1);
			}
			
		}
		//set the index, the attr appears 1 time, index=0, for attr appears more than 1 times, index starts from 1.
		Set keySet = hmap.keySet();
		for(Object attr:keySet)//each attribute name
		{
			int count=1;
			if(hmap.get(attr)>1)//appears more than 1 times
			{
				for(int i=0; i<condNum; i++)
				{
					int kwIndex = this.kw4condition[i];
					int tagIndex = this.kwtag4condition[i]; 
					Keyword kw = kwArray[kwIndex];
					String tagAttr = kw.getTagAttr(tagIndex);
					if(tagAttr.equals(attr))//find the corresponding tag
					{
						this.kwtag4index[i] = count++;//set the index
					}
				}
			}
		}		
	}
	
	private void updateOutputKw(Function[] fnArray)
	{
		HashSet<Integer> kwIndexSet = new HashSet<Integer>();
		int conditionFnLen = this.fn4condition.length;
		
		for(int i = 0; i < conditionFnLen; i++)
		{
			int fnIndex = this.fn4condition[i];
			Function fn = fnArray[fnIndex];
			if(fn.isSimpleFn())
			{
				kwIndexSet.add(fn.getKwIndex());
			}
		}
		
		int conditionGpLen = this.gp4condition.length;
		
		for(int i = 0; i < conditionGpLen; i++)
		{
			kwIndexSet.add(this.gp4condition[i]);
		}
		
		deleteParaList(kwIndexSet);
	}
	
	private void deleteParaList(HashSet<Integer> kwIndexSet)
	{
		ArrayList<Integer> kw4outputList = new ArrayList<Integer>();
		ArrayList<Integer> kwtag4outputList = new ArrayList<Integer>();
		
		int arrayLen = this.kw4output.length;
		for(int i = 0; i < arrayLen; i++)
		{
			int kwIndex = this.kw4output[i];
			int kwTag = this.kwtag4output[i];
			if(!kwIndexSet.contains(kwIndex))
			{
				kw4outputList.add(kwIndex);
				kwtag4outputList.add(kwTag);
			}
		}
		
		int updateSize = kw4outputList.size();
		int[] updateKw4output = new int[updateSize];
		int[] updateKwTag4output = new int[updateSize];
		
		for(int i = 0; i < updateSize; i++)
		{
			updateKw4output[i] = kw4outputList.get(i);
			updateKwTag4output[i] = kwtag4outputList.get(i);
		}
		
		this.kw4output = updateKw4output;
		this.kwtag4output = updateKwTag4output;
	}
	
	private void analyzeFnGpArray(int[] combTag, int[] kwGroup, Keyword[] kwArray, Function[] fnArray, Groupby[] gpArray)
	{
		int fnNum = fnArray.length;
		int[] conditionFn = new int[fnNum];
		int[] conditionFnTag = new int[fnNum];
		int  conditionFnLen = 0;
		
		for(int i = 0; i < fnNum; i++)
		{
			Function fn = fnArray[i];
			if(this.isFunctionApply(fn, fnArray))
			{
				conditionFn[conditionFnLen] = i;
				if(fn.isSimpleFn())
				{
					conditionFnTag[conditionFnLen] = combTag[fn.getKwIndex()];
				}
				conditionFnLen++;
			}
		}
		
		int gpNum = gpArray.length;
		int[] conditionGp = new int[gpNum];
		int[] conditionGpTag = new int[gpNum];
		int conditionGpLen = 0;
		
		for(int i = 0; i < gpNum; i++)
		{
			int kwIndex = gpArray[i].getKwIndex();
			if(this.isGROUPBYApply(kwIndex))
			{
				conditionGp[conditionGpLen] = kwIndex;
				conditionGpTag[conditionGpLen] = combTag[kwIndex];
				conditionGpLen++;
			}
		}
		
		this.fn4condition = new int[conditionFnLen];
		this.fntag4condition = new int[conditionFnLen];
		this.gp4condition = new int[conditionGpLen];
		this.gptag4condition = new int[conditionGpLen];
		
		System.arraycopy(conditionFn, 0, this.fn4condition, 0, conditionFnLen);
		System.arraycopy(conditionFnTag, 0, this.fntag4condition, 0, conditionFnLen);
		System.arraycopy(conditionGp, 0, this.gp4condition, 0, conditionGpLen);
		System.arraycopy(conditionGpTag, 0, this.gptag4condition, 0, conditionGpLen);
	}
	
	//for the normal Kw
	private void analyzeKwArray(int[] combTag, int[] kwGroup, Keyword[] kwArray, ORMGraph ormgraph)
	{
		int kwNumInGroup = kwGroup.length;
		int[] outputKw = new int[kwNumInGroup];
		int[] outputKwTag = new int[kwNumInGroup];
		int outputKwLen = 0;
		int[] conditionKw = new int[kwNumInGroup];
		int[] conditionKwTag = new int[kwNumInGroup];
		int conditionKwLen = 0;
		
		
		for(int i = 0; i < kwNumInGroup; i++)//each KW
		{
			int kwIndex = kwGroup[i];
			Keyword kw = kwArray[kwIndex];
			String tagVal = kw.getTagVal(combTag[kwIndex]);
			
			if(tagVal != null)//this tag indicates a value condition
			{
				conditionKw[conditionKwLen] = kwIndex;
				conditionKwTag[conditionKwLen] = combTag[kwIndex];
				conditionKwLen++;
			}
			else
			{
				String tagAttr = kw.getTagAttr(combTag[kwIndex]);
				if(tagAttr != null)//attr not NULL, output KW
				{
					int j = 0;
					int groupNum = kwGroup.length;
					for(j = i + 1; j < groupNum; j++)
					{
						int subseqKwIndex = kwGroup[j];
						Keyword subseqKw = kwArray[subseqKwIndex];
						String subseqTagAttr = subseqKw.getTagAttr(combTag[subseqKwIndex]);
						String subseqTagVal = subseqKw.getTagVal(combTag[subseqKwIndex]);
						if(ormgraph.isSameAttr(tagAttr, subseqTagAttr) && subseqTagVal != null)
						{
							break;
						}
					}
					if(j == groupNum)
					{
						outputKw[outputKwLen] = kwIndex;
						outputKwTag[outputKwLen] = combTag[kwIndex];
						outputKwLen++;
					}
				}
			}
		}
		//if no keyword indicates condition or output attribute, then output everything of the object/relationship
		if(outputKwLen == 0 && conditionKwLen == 0)
		{
			int kwIndex = kwGroup[0];
			outputKw[outputKwLen] = kwIndex;
			outputKwTag[outputKwLen] = combTag[kwIndex];
			outputKwLen++;
		}
		
		this.kw4output = new int[outputKwLen];
		this.kwtag4output = new int[outputKwLen];
		this.kw4condition = new int[conditionKwLen];
		this.kwtag4condition = new int[conditionKwLen];
		this.kwlabel4condition = new int[kwNumInGroup];
		
		System.arraycopy(outputKw, 0, this.kw4output, 0, outputKwLen);
		System.arraycopy(outputKwTag, 0, this.kwtag4output, 0, outputKwLen);
		System.arraycopy(conditionKw, 0, this.kw4condition, 0, conditionKwLen);
		System.arraycopy(conditionKwTag, 0, this.kwtag4condition, 0, conditionKwLen);
	}
	
	private boolean isFunctionApply(Function fn, Function[] fnArray)
	{
		if(fn.isSimpleFn())
		{
			int kwIndex = fn.getKwIndex();
			for(int kwIndexInGroup : this.kwGroup)
			{
				if(kwIndex == kwIndexInGroup)
				{
					return true;
				}
			}
			return false;
		}
		else 
		{
			int subFnIndex = fn.getFnIndex();
			Function nestedFn = fnArray[subFnIndex]; 
			return isFunctionApply(nestedFn, fnArray);
		}
	}
	
	private boolean isGROUPBYApply(int kwIndex)
	{
		for(int kwIndexInGroup : this.kwGroup)
		{
			if(kwIndex == kwIndexInGroup)
			{
				return true;
			}
		}
		return false;
	}
	
	public int getKw4OutputNum()
	{
		if(this.kw4condition == null)
			return 0;
		return this.kw4output.length;
	}
	
	public int getKwIndex4Output(int index)
	{
		return this.kw4output[index];
	}
	
	public int getTagIndex4Output(int index)
	{
		return this.kwtag4output[index];
	}
	
	public int getKw4ConditionNum()
	{
		if(this.kw4condition == null)
			return 0;
		return this.kw4condition.length;
	}
	
	public int getKwIndex4Condition(int index)
	{
		return this.kw4condition[index];
	}
	
	public int getKwTagIndex4Condition(int index)
	{
		return this.kwtag4condition[index];
	}
	
	public int getFn4ConditionNum()
	{
		if(this.fn4condition == null)
			return 0;
		return this.fn4condition.length;
	}
	
	public int getFnIndex4Condition(int index)
	{
		return this.fn4condition[index];
	}
	
	public int getFnTagIndex4Condition(int index)
	{
		return this.fntag4condition[index];
	}
	
	public int getGp4ConditionNum()
	{
		if(this.gp4condition == null)
			return 0;
		return this.gp4condition.length;
	}
	
	public int getGpIndex4Condition(int index)
	{
		return this.gp4condition[index];
	}
	
	public int getGpTagIndex4Condition(int index)
	{
		return this.gptag4condition[index];
	}
	
	public int getKwNum()
	{
		return this.kwGroup.length;
	}
	
	public int getKwIndex(int index)
	{
		return this.kwGroup[index];
	}

	public boolean getGroupbyID()
	{
		return this.groupbyID;
	}
	
//	public boolean isTpPattern()
//	{
//		if (this.tp4condition == -1)
//		{
//			return false;
//		}
//		else
//		{
//			return true;
//		}
//	}
	
	private int isTpredicateApply(Tpredicate tp)//return the index of the obj kw in KwArray
	{
		int kwIndex = tp.getKwIndex();//the object kw
		for(int kwIndexInGroup : this.kwGroup)//for each element in this.kwGroup, called kwIndexInGroup
		{
			if(kwIndex == kwIndexInGroup)//this temporal predicate is applied to this tag group
			{
				return kwIndex;
			}
		}
		return -1;//not apply to this tag group
	}
	
	public int getTpIndex()
	{
		return this.tp4condition;
	}
	
	public int [] getKwgroup()
	{
		
		return this.kwGroup;
		
	}
	
	public void setTp(Pattern pattern)
	{
		this.tp4condition = pattern.tp4condition;
		this.tpKw4condition = pattern.tpKw4condition;
		this.tptag4condition = pattern.tptag4condition;
		
		this.tp4targetRel = pattern.tp4targetRel;
		this.tpSec4targetRel = pattern.tpSec4targetRel;
		this.tp4targetAttr = pattern.tp4targetAttr;
		this.tpSec4targetAttr = pattern.tpSec4targetAttr;
		
//		this.tpSec4condition = pattern.tpSec4condition;
		this.tpSecKw4condition = pattern.tpSecKw4condition;
		this.tpSectag4condition = pattern.tpSectag4condition;
	}
	
	public Pattern getCopy()
	{
		Pattern augptn = new Pattern();
		augptn.kwGroup = this.kwGroup;
		augptn.kw4condition = this.kw4condition;
		augptn.kwtag4condition = this.kwtag4condition;
		augptn.kwtag4index = this.kwtag4index;
		augptn.kwlabel4condition = this.kwlabel4condition;
		
		augptn.kw4output = this.kw4output;
		augptn.kwtag4output = this.kwtag4output;
		augptn.fn4condition = this.fn4condition;
		augptn.fntag4condition = this.fntag4condition;
		
		augptn.tpAnnotate = this.tpAnnotate;
		augptn.tpInfer = this.tpInfer;
		augptn.tpSecInfer = this.tpSecInfer;
		augptn.tp4condition = this.tp4condition;
		augptn.tpKw4condition = this.tpKw4condition;
		augptn.tptag4condition = this.tptag4condition;
		
		augptn.idFirst = this.idFirst;
		augptn.idSec = this.idSec;
		
//		augptn.tpSec4condition = this.tpSec4condition;
		augptn.tpSecKw4condition = this.tpSecKw4condition;
		augptn.tpSectag4condition = this.tpSectag4condition;
		
		augptn.tpAttr = new int[3];
		augptn.tpAttr[0] = this.tpAttr[0];
		augptn.tpAttr[1] = this.tpAttr[1];
		augptn.tpAttr[2] = this.tpAttr[2];
		
		augptn.tpSecAttr = new int[3];
		augptn.tpSecAttr[0] = this.tpSecAttr[0];
		augptn.tpSecAttr[1] = this.tpSecAttr[1];
		augptn.tpSecAttr[2] = this.tpSecAttr[2];
		
		augptn.tp4targetRel = this.tp4targetRel;
		augptn.tpSec4targetRel = this.tpSec4targetRel;
		augptn.tp4targetAttr = this.tp4targetAttr;
		augptn.tpSec4targetAttr = this.tpSec4targetAttr;
		
		augptn.gp4condition = this.gp4condition;
		augptn.gptag4condition = this.gptag4condition;
		augptn.groupbyID = this.groupbyID;
		return augptn;
	}
	
//	public void setTp4TargetRel(String name)
//	{
//		this.tp4targetRel = name;
//	}
	
//	public String getTempAttrName()
//	{
//		return this.tempAttrName;
//	}
	
//	public void setTpAttr(int[] AttrList, DBinfo dbinfo)
//	{
//		this.tpAttr[0] = AttrList[0];//from
//		this.tpAttr[1] = AttrList[1];//to
//		this.tpAttr[2] = AttrList[2];//tempAttr
//		
//		int nodeId = dbinfo.getRelId(this.tp4targetRel);
//		if (this.tpAttr[2] != -1)
//		{
//			this.tempAttrName = dbinfo.getAttr(nodeId, tpAttr[2]);
//		}
//	}
	
	public void setTpAttr(int[] AttrList, boolean SecKw, DBinfo dbinfo)
	{
		if(SecKw)
		{
			this.tpSecAttr[0] = AttrList[0];//from
			this.tpSecAttr[1] = AttrList[1];//to
			this.tpSecAttr[2] = AttrList[2];//attr
			
			if (this.tpSecAttr[2] != -1)//temporal attribute
			{
				int nodeId = dbinfo.getRelId(this.tpSec4targetRel);
				this.tpSec4targetAttr = dbinfo.getAttr(nodeId, tpSecAttr[2]);
			}
			
		}
		else
		{
			this.tpAttr[0] = AttrList[0];//from
			this.tpAttr[1] = AttrList[1];//to
			this.tpAttr[2] = AttrList[2];//attr
			
			if (this.tpAttr[2] != -1)//temporal attribute
			{
				int nodeId = dbinfo.getRelId(this.tp4targetRel);
				this.tp4targetAttr = dbinfo.getAttr(nodeId, tpAttr[2]);
			}
		}
	}
	
	
	public int getTpKwIndex()
	{
		return this.tpKw4condition;
	}
	
	public int getTpTagIndex()
	{
		return this.tptag4condition;
	}
	
	public String getDirectTpRelName(Keyword[] kwArray)
	{
		int kwIndex = this.getTpKwIndex();
		int kwtagIndex = this.getTpTagIndex();
		Keyword kw = kwArray[kwIndex];
		
		return kw.getTagName(kwtagIndex);
	}
	
	public int[] getTpAttr()//Nov23
	{
		return this.tpAttr;
	} 
	
	public String[] getTpInfo(Tpredicate[] tpArray, Keyword[] kwArray)
	{
		String[] tpInfo = new String[3];
		int tpIndex = getTpIndex();
		Tpredicate tp = tpArray[tpIndex];
		
		//tpInfo[0] = the first operand of the TP
		//tpInfo[1] = the name of TP
		//tpInfo[2] = the 2nd operand of TP
			
		//whether this TP has two KW operands
		if(tp.isTpTwoKW())//two KW
		{
			//check id
			if(this.idFirst>0)//the 1st kw, set tpInfo[0]
			{
				if (this.tpAttr[2] != -1)//change this
				{
					tpInfo[0] = getTpInfoDetail(false);
				}
				else//temporal relation
				{
					tpInfo[0] = this.tp4targetRel;
				}

			}
			else//the 2nd kw, set tpInfo[2]
			{
				tpInfo[0] = "X" +  Integer.toString(this.idSec);
			}
			
			if(this.idSec>0)//the 2nd kw
			{
				if (this.tpSecAttr[2] != -1)
				{
					tpInfo[2] = getTpInfoDetail(true);
				}
				else//temporal relation
				{
					tpInfo[2] = this.tpSec4targetRel;
				}
			}
			else//the 1st kw
			{
				tpInfo[2] = "X" +  Integer.toString(this.idFirst);
			}
		}
		else//one KW, one period
		{
			//whether period is the first operand
			if(tp.getPeriodIndex()==0)//period is the 1st operand
			{
				tpInfo[0] = tp.getPeriodDesc();
				if (this.tpAttr[2] != -1)
				{
					tpInfo[2] = getTpInfoDetail(true);
				}
				else
				{
					tpInfo[2] = this.tpSec4targetRel;
				}
				
			}
			else
			{
				if (this.tpAttr[2] != -1)
				{
					tpInfo[0] = getTpInfoDetail(false);
				}
				else
				{
					tpInfo[0] = this.tp4targetRel;
				}
				tpInfo[2] = tp.getPeriodDesc();
			}
		}
		
		tpInfo[1] = tp.getTpName();//temporal predicate name
		
		return tpInfo;
	}
	
	
	public String getTpInfoDetail(boolean Sec)
	{
		//get the index
		int kwIndex;
		if(!Sec)
		{
			kwIndex = this.tpKw4condition;
		}
		else
		{
			kwIndex = this.tpSecKw4condition;
		}
		
		
		for(int i=0; i<this.kw4condition.length; i++)
		{
			if(kwIndex == this.kw4condition[i])
			{
				if(!Sec)
				{
					return this.tp4targetAttr + "_" + String.valueOf(this.kwtag4index[i]);
				}
				else
				{
					return this.tpSec4targetAttr + "_" + String.valueOf(this.kwtag4index[i]);
				}
				
			}
		}
		
		if(!Sec)
		{
			return this.tp4targetAttr;
		}
		else
		{
			return this.tpSec4targetAttr;
		}
	}
	
	public int getTpSecKwIndex()
	{
		return this.tpSecKw4condition;
	}
	
	public int getTpSecKw()//return the kw index for the second kw
	{
		return tpSecKw4condition;
	}
	
//	public boolean isTpSequence()
//	{
//		if (this.tpSec4condition == -1)
//		{
//			return false;
//		}
//		else
//		{
//			return true;
//		}
//	}
	

	public boolean isTpSequence()
	{
		if (this.tpSecKw4condition == -1)
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	public String getTp4TargetRel()
	{
		return this.tp4targetRel;
	}
	
	public int getKw4ConditioNum()
	{
		if (this.kw4condition == null)
		{
			return 0;
		}
		return this.kw4condition.length;
	}
	
	public void setKwLabel(int index, int label)
	{
		kwlabel4condition[index] = label;
	}
	
	public int getKwLabel(int kwIndex)
	{
		int kwNum = this.getKwNum();
		for(int i = 0; i < kwNum; i++)
		{
			if(this.kwGroup[i]==kwIndex)
				return kwlabel4condition[i];//null value
		}
		
		return -1;
	}
	
	public void setTp(Pattern pattern, boolean Sec)
	{
		this.tpAnnotate = pattern.tpAnnotate;
		if(!Sec)
		{
			this.tpKw4condition = pattern.tpKw4condition;
			this.tptag4condition = pattern.tptag4condition;
			this.tp4targetRel = pattern.tp4targetRel;
		}
		else
		{
			this.tpSecKw4condition = pattern.tpSecKw4condition;
			this.tpSectag4condition = pattern.tpSectag4condition;
		}	
	}
	
	public boolean isTpPattern()
	{
		return this.tpAnnotate;
	}
	
	public void setTpInfer(boolean Sec,boolean infer)
	{
		if(!Sec)
		{
			this.tpInfer = infer;
		}
		else
		{
			this.tpSecInfer = infer;
		}
		
	}
	
	public boolean getTpInfer(boolean Sec)
	{
		if(!Sec)
		{
			return this.tpInfer;
		}
		else
		{
			return this.tpSecInfer;
		}
		
	}
	
	public int getKwTag4Index(int index)
	{
		return this.kwtag4index[index];
	}
	
	public void setTp4TargetRel(String name, boolean SecKw)
	{
		if (SecKw)
		{
			this.tpSec4targetRel = name;
		}
		else
		{
			this.tp4targetRel = name;
		}
		
	}
	
	public void setId(int id, boolean SecKw)
	{
		if(SecKw)
		{
			this.idSec = id;
		}
		else
		{
			this.idFirst = id;
		}
	}
	
	//this is the first kw of tp relationship
	public boolean isTpSequenceFirst()
	{
		if (this.idFirst >0)
		{
			return true;
		}
		return false;
	}
	
	public int getIdFirst()
	{
		return this.idFirst;
	}
	
	public int getIdSec()
	{
		return this.idSec;
	}
	
	public int getTpFirstKw()//return the kw index for the first kw
	{
		return tpKw4condition;
	}
	
	public String getDirectTpFirstRelName(Keyword[] kwArray)//get the rel name in tag
	{
		int kwIndex = this.getTpKwIndex();//kw index for first kw
		Keyword kw = kwArray[kwIndex];//find kw
		int kwtagIndex = this.getTpTagIndex();//tag index
		
		return kw.getTagName(kwtagIndex);//find the relName in tag
	}
	
	public boolean isTpSequenceSec()
	{
		if (this.idSec >0)
		{
			return true;
		}
		return false;
	}
	
	public int[] getTpSecAttr()
	{
		return this.tpSecAttr;
	}
	
	public int getTpSecTagIndex()
	{
		return this.tpSectag4condition;
	}
}
