package queryprocessing;



import java.util.ArrayList;
import java.util.HashSet;

import queryprocessing.Constant.NodeType;


public class Translator
{
	public static String getStatement(int index, Keyword kw, String style)
	{
		return "Keyword " + index + ":<span class=\"" + style + "\"> " + kw.getContent() + " </span> refers to";
	}
	
	public static String getStatement(int index, Function fn, String style)
	{
		return "Function " + index + ":<span class=\"" + style + "\"> " + fn.getFnName() + " </span> refers to";
	}
	
	public static String getStatement(int index, String style)
	{
		return "GROUPBY " + index + ":<span class=\"" + style + "\"> GROUPBY </span> refers to";
	}
	
	public static String getStatement(int index, Tpredicate pd, String style)
	{
		return "Predicate" + index + ":<span class=\"" + style + "\"> " + pd.getTpName() + " </span> refers to";
	}
	
	private static String findKwMatchNodeName(ORMGraph ormgraph, String tagName)
	{
		String nodeName = null;
		int nodeNum = ormgraph.getNodeNum();
		
		for(int i = 0; i < nodeNum; i++)
		{
			Node node = ormgraph.getNode(i);
			nodeName = node.getNodeName();
			if(isKwMatchNodeName(node, tagName))
			{
				break;
			}
		}
		return nodeName;
	}
	
	private static boolean isKwMatchNodeName(Node node, String tagName)
	{
		Relation rel = node.getCoreRelation();
		String relAlias = rel.getRelAlias();
		if(relAlias.equals(tagName))
		{
			return true;
		}
		int compRelNum = node.getCompRelNum();
		for(int j = 0; j < compRelNum; j++)
		{
			Relation comprel = node.getCompRelation(j);
			String comprelAlias = comprel.getRelAlias();
			if(comprelAlias.equals(tagName))
			{
				return true;
			}
		}
		return false;
	}
	
	//pattern meaning KW & META
	public static String[] getDesc(Keyword kw, ORMGraph ormgraph)
	{
		int descNum = kw.getTaglistLength();
		String[] descList = new String[descNum];
		
		for(int i = 0; i < descNum; i++)
		{
			String tagName = kw.getTagName(i);
			String tagAttr = kw.getTagAttr(i);
			String tagVal = kw.getTagVal(i);
			
			String nodeName = findKwMatchNodeName(ormgraph, tagName);
			if(tagVal == null)
			{
				if(tagAttr == null)
				{
					descList[i] = "Some " + nodeName;
				}
				else
				{
					descList[i] = "Some " + nodeName + " " + tagAttr;
				}
			}
			else
			{
				descList[i] = "Some " + nodeName + " with " + tagAttr + " matching " + tagVal;
			}
		}
		
		return descList;
	}
	
	//pattern meaning - FN
	public static String getDesc(Function fn, Keyword[] kwArray, Function[] fnArray)
	{
		if(fn.isSimpleFn())
		{
			int kwIndex = fn.getKwIndex();
			Keyword kw = kwArray[kwIndex];
			return "A " + fn.getFnName() + " function on the matches of the keyword " + kw.getContent();
		}
		else
		{
			int fnIndex = fn.getFnIndex();
			Function subfn = fnArray[fnIndex];
			return "A " + fn.getFnName() + " function on the result of the " + subfn.getFnName() + " function";
		}
	}
	
	//pattern meaning - GY
	public static String getDesc(int kwIndex, Keyword[] kwArray)
	{
		Keyword kw = kwArray[kwIndex];
		return "A " + Constant.groupby +" on the matches of the keyword " + kw.getContent(); 
	}

	
	//patttern meaning - TP
	public static String getDesc(Tpredicate pd, Keyword[] kwArray)
	{
		if(pd.isSimpleTp())//unchanged 
		{
			return "A " + pd.getTpName() + " predicate on the time period " + pd.getPeriodDesc(); 
		}
		else//revise
		{
			int kwIndexFrist = pd.getKwIndex();
			int kwIndexSec = pd.getKwIndexSec();
			Keyword kw1 = kwArray[kwIndexFrist];
			Keyword kw2 = kwArray[kwIndexSec];
			return "A " + pd.getTpName() + " predicate on the time period referred to by the keyword " + kw1.getContent() + " and " + kw2.getContent();
		}
	}
	
	public static String[] genTableSQL(Viewinfo viewinfo, AuxGraph[] graphList, DBinfo dbinfo)
	{
		String[] view = viewinfo.getView();
		String[][] att = viewinfo.getAtt();
		String[][] dbrel = viewinfo.getDBRel();
		String[][] dbatt = viewinfo.getDBAtt();
		int[][] key = viewinfo.getKey();
		int[][] text = viewinfo.getTextAtt();
		int[][] fk = viewinfo.getFK();
		int viewNum = view.length;
		String[] sql = new String[3*viewNum];
		for(int i = 0; i < viewNum; i++)
		{
			String tbl = view[i] + Constant.view;
			AuxGraph graph = graphList[i];
			int[] tmp = new int[fk.length];
			int refNum = 0;
			for(int j = 0; j < fk.length; j = j + 2)
			{
				if(fk[j][0] == i)
				{
					tmp[refNum++] = fk[j][1];
				}
			}
			int[] ref = new int[refNum];
			System.arraycopy(tmp, 0, ref, 0, refNum);
			sql[3*i] = "DROP TABLE IF EXISTS `" + tbl + "`";
			sql[3*i+1] = genSQL4Create(tbl, dbrel[i], dbatt[i], att[i], graph, dbinfo);
			sql[3*i+2] = genSQL4Alter(tbl, att[i], key[i], text[i], ref);
		}
		return sql;
	}
	
	private static String genSQL4Alter(String tbl, String[] att, int[] key, int[] text, int[] fk)
	{
		if(key.length == 0 && text.length == 0 && fk.length == 0)
		{
			return null;
		}
		else
		{
			StringBuffer buf = new StringBuffer();
			buf.append("ALTER TABLE `").append(tbl).append("` ");
			if(key.length > 0)
			{
				buf.append("ADD PRIMARY KEY (");
				for(int i = 0; i < key.length; i++)
				{
					buf.append("`").append(att[key[i]]).append("`, ");
				}
				int buflen = buf.length();
				buf.delete(buflen - 2, buflen);
				buf.append("), ");
			}
			
			for(int i = 0; i < text.length; i++)
			{
				buf.append("ADD FULLTEXT INDEX `ft_").append(att[text[i]]).append("` (`").append(att[text[i]]).append("` ASC), ");
			}
			
			for(int i = 0; i < fk.length; i++)
			{
				buf.append("ADD INDEX `").append(att[fk[i]]).append("` (`").append(att[fk[i]]).append("` ASC), ");
			}
			int buflen = buf.length();
			buf.delete(buflen - 2, buflen);
			return buf.toString();
		}
	}
	
	private static String genSQL4Create(String tbl, String[] dbrel, String[] dbatt, String[] att, AuxGraph graph, DBinfo dbinfo)
	{
		StringBuffer selectBuf = new StringBuffer("CREATE TABLE `");
		StringBuffer fromBuf = new StringBuffer(" FROM ");
		StringBuffer whereBuf = new StringBuffer(" WHERE ");
		int whereInitialSize = whereBuf.length();
		selectBuf.append(tbl).append("` ENGINE=MyISAM DEFAULT CHARSET=latin1 AS SELECT DISTINCT ");
		for(int i = 0; i < att.length; i++)
		{
			selectBuf.append("`").append(dbrel[i]).append("`.`").append(dbatt[i]).append("` AS `").append(att[i]).append("`, ");
		}
		for(int i = 0; i < graph.getNodeNum(); i++)
		{
			int relId = graph.getNode(i);
			Relation rel = dbinfo.getRel(relId);
			fromBuf.append("`").append(rel.getRelName()).append("`, ");
			
			int[] outEdges = graph.getOutEdges(i);
			for(int k = 0; k < outEdges.length; k++)
			{
				whereBuf.append(getSQL4Edge(graph, i, outEdges[k], dbinfo)).append(" and ");
			}
		}
		String sql = formSQL(selectBuf, fromBuf, whereBuf, whereInitialSize);
		return sql;
	}
	
	public static String[] genViewSQL(Viewinfo viewinfo, AuxGraph[] graphList, DBinfo dbinfo)
	{
		String[] view = viewinfo.getView();
		String[][] att = viewinfo.getAtt();
		String[][] dbrel = viewinfo.getDBRel();
		String[][] dbatt = viewinfo.getDBAtt();
		int viewNum = view.length;
		String[] sql = new String[viewNum];
		for(int i = 0; i < viewNum; i++)
		{
			StringBuffer selectBuf = new StringBuffer("CREATE OR REPLACE VIEW `");
			StringBuffer fromBuf = new StringBuffer(" FROM ");
			StringBuffer whereBuf = new StringBuffer(" WHERE ");
			int whereInitialSize = whereBuf.length();
			
			selectBuf.append(view[i]).append(Constant.view).append("` AS SELECT DISTINCT ");
			for(int j = 0; j < att[i].length; j++)
			{
				selectBuf.append("`").append(dbrel[i][j]).append("`.`").append(dbatt[i][j]).append("` AS `").append(att[i][j]).append("`, ");
			}
			
			AuxGraph graph = graphList[i];
			for(int j = 0; j < graph.getNodeNum(); j++)
			{
				int relId = graph.getNode(j);
				Relation rel = dbinfo.getRel(relId);
				fromBuf.append("`").append(rel.getRelName()).append("`, ");
				
				int[] outEdges = graph.getOutEdges(j);
				for(int k = 0; k < outEdges.length; k++)
				{
					whereBuf.append(getSQL4Edge(graph, j, outEdges[k], dbinfo)).append(" and ");
				}
			}
			sql[i] = formSQL(selectBuf, fromBuf, whereBuf, whereInitialSize);
		}
		return sql;
	}
	
	
	public static SQLUnit getSQLUnit(PGraph pgraph, ORMGraph ormgraph, Keyword[] kwArray, Function[] fnArray, Groupby[] gpArray, Tpredicate[] tpArray, DBinfo dbinfo)
	{
		SQLUnit sqlUnit = new SQLUnit();
		StringBuffer orderBuf = new StringBuffer(" ORDER BY ");
		int orderInitialSize = orderBuf.length();
		
		StringBuffer selectBuf = new StringBuffer("SELECT ");
		StringBuffer fromBuf = new StringBuffer(" FROM ");
		StringBuffer whereBuf = new StringBuffer(" WHERE ");
		StringBuffer groupBuf = new StringBuffer(" GROUP BY ");
		StringBuffer subSelectBuf = new StringBuffer(" Select *");
		StringBuffer subFromBuf = new StringBuffer(" From ");
		StringBuffer subWhereBuf = new StringBuffer(" Where ");
		
		StringBuffer From;
		StringBuffer Where;
		
		int selectInitialSize = selectBuf.length();
		int whereInitialSize = whereBuf.length();
		int groupInitialSize = groupBuf.length();
		int subFromInitialSize = subFromBuf.length();
		
		String parentFn = null;
		String parentFnLabel = null;
		String childFnLabel = null;
		
		int outputAttr = 0;
		int multivalueAttr = 0;
		
		int nodeNum = pgraph.getNodeNum();
		int[] verifyAttr = new int[nodeNum];
		StringBuffer verifyBuf = new StringBuffer();
		
		int[] targetId = pgraph.getTargetNode();
		int centricId=-1;
		if (targetId.length == 0)
		{
			centricId = pgraph.getCentriId();
		}
		
		for(int i = 0; i < nodeNum; i++)//for each node
		{
			PNode pNode = pgraph.getNode(i);
			int[] outEdges = pgraph.getOutEdges(i);
			
			int ormNodeId = pNode.getORMNodeId();
			Node ormNode = ormgraph.getNode(ormNodeId);
			int coreRelId = ormNode.getCoreRelId(dbinfo);
			Relation rel = ormNode.getCoreRelation();//the type of ORM node
			int label = getNodeLabel(i);
			int outEdgeNum = outEdges.length;
			HashSet<String> verifySet = new HashSet<String>();
			
			int compRelNum = ormNode.getCompRelNum();
			boolean[] isCompOutput = new boolean[compRelNum];//default=false
			boolean targetNode;
			
			for(int j = 0; j < outEdgeNum; j++)//for each out node
			{
				PNode fromNode = pgraph.getNode(i);
				PNode toNode = pgraph.getNode(outEdges[j]);

				Where = fromNode.isTargetNode() && toNode.isTargetNode()? whereBuf:subWhereBuf;
				Where.append(getSQL4Edge(pgraph, ormgraph, i, outEdges[j], dbinfo)).append(" AND ");//get edge relation for the join 
			}
			
			targetNode = !pNode.isTrival() && pNode.isTargetNode() || !pNode.isTrival() && pNode.isAggregateNode() || centricId == i;
			
			if(targetNode)//target node, output node
			{
				//select clause
				if(!pNode.isTrival() && pNode.getOutputNum() > 0)//select buffer
				{
					int outputNum = pNode.getOutputNum();
					for(int j = 0; j < outputNum; j++)
					{
						String[] outputDesc = pNode.getOutput(j, kwArray);
						int kwPosition = pNode.getOutputKwPosition(j);//get the kwIndex for the output
						if(outputDesc[0].equals("name"))//table name
						{
							int outputAttrNum = rel.getOutputAttrNum();
							for(int k = 0; k < outputAttrNum; k++)
							{
								selectBuf.append("R").append(label).append(".`").append(rel.getOutputAttrName(k)).append("`, ");
							}
							outputAttr += outputAttrNum;//nov25
							
						}
						else//attribute name
						{
							String attr = outputDesc[1];
							if(ormNode.isSingleValuedAttr(attr))//single value attribute
							{
								selectBuf.append("R").append(label).append(".`").append(attr).append("`, ");
							}
							else//attri does not belong to this relation, join the object component relationship
							{
								int compRelIndex = ormNode.getCompRel4Attr(attr);//component relation
								int compLabel = getNodeLabel(i, compRelIndex, multivalueAttr++);
								pNode.setKwLabel(kwPosition, compLabel);//set label for this relation
								isCompOutput[compRelIndex]=true;
								selectBuf.append("R").append(compLabel).append(".`").append(attr).append("`, ");//attri
								fromBuf.append("`").append(ormNode.getCompRelation(compRelIndex).getRelName()).append("`").append(" R").append(compLabel).append(", ");//table
								whereBuf.append(getSQL4Edge(ormNode, compRelIndex, compLabel, label, dbinfo)).append(" AND ");//if the obj is a target - main where; if not, subwhere
							}
							outputAttr++;//nov25
						}
					}
				}
				
				if (centricId == i)//no target node
				{
					int outputNum = rel.getOutputAttrNum();
					for(int n = 0; n < outputNum; n++)
					{
						selectBuf.append("R").append(label).append(".`").append(rel.getOutputAttrName(n)).append("`, ");
					}
					outputAttr += outputNum;//nov25
				}
				
				//check if it has the aggregate, add to "group by" and "select".
				if (!pNode.isTrival() && pNode.isAggregateNode())
				{
					if(pNode.getFnNum() > 0)
					{
						int fnNum = pNode.getFnNum();
						for(int j = 0; j < fnNum; j++)//for each fn
						{
							int fnIndex = pNode.getFnIndex(j);
							Function fn = fnArray[fnIndex];
							if(fn.isSimpleFn())
							{
								String[] fnDesc = pNode.getSimpleFnInfo(j, kwArray, fnArray);
								if(fnDesc[0].equals("name"))//fn(table_name)
								{
									selectBuf.append(fn.getFnName()).append("(*) AS `").append(fn.getFnName()).append("(").append(fnDesc[1]).append(")`, ");
								}
								else
								{
									String attr = fnDesc[1];
									if(ormNode.isSingleValuedAttr(attr))//single value attri
									{
										selectBuf.append(fn.getFnName()).append("(R").append(label).append(".`").append(attr).append("`) AS `").append(fn.getFnName()).append("(").append(attr).append(")`, ");
									}
									else//multi value attri, join the component relation
									{
										int compRelIndex = ormNode.getCompRel4Attr(attr);
										int compLabel = getNodeLabel(i, compRelIndex, multivalueAttr++);
										isCompOutput[compRelIndex]=true;
										selectBuf.append(fn.getFnName()).append("(R").append(compLabel).append(".`").append(attr).append("`) AS `").append(fn.getFnName()).append("(").append(attr).append(")`, ");
										fromBuf.append("`").append(ormNode.getCompRelation(compRelIndex).getRelName()).append("` R").append(compLabel).append(", ");
										whereBuf.append(getSQL4Edge(ormNode, compRelIndex, compLabel, label, dbinfo)).append(" AND ");
									}
								}
								outputAttr++;
							}
							else// only support single nested aggregate function for the moment
							{
								parentFn = fn.getFnName();
								Function childFn = fnArray[fn.getFnIndex()];
								String[] childFnDesc = pNode.getSimpleFnInfo(childFn, kwArray, fnArray);
								childFnLabel = childFn.getFnName() + "(" + childFnDesc[1] + ")";
								parentFnLabel = fn.getFnName() + "(" + childFnLabel + ")";
							}
						}
					}
					
					
					if(pNode.getGpNum() > 0)//add content to "group by", "select" and "order by"
					{
						int gpNum = pNode.getGpNum();
						for(int j = 0; j < gpNum; j++)
						{
							String[] gpDesc = pNode.getGpInfo(j, kwArray);
							if(gpDesc[0].equals("name"))//table_name
							{
								int keyAttrNum = rel.getKeyAttrNum();
								for(int k = 0; k < keyAttrNum; k++)
								{
									groupBuf.append("R").append(label).append(".`").append(rel.getKeyAttrName(k)).append("`, ");
									selectBuf.append("R").append(label).append(".`").append(rel.getKeyAttrName(k)).append("`, ");
								}
								outputAttr += keyAttrNum;
								for(int k = 0; k < rel.getVerifyAttrNum(); k++)
								{
									verifySet.add(addVerifyAttr(label, rel.getVerifyAttrName(k)));
								}
							}
							else
							{
								String attr = gpDesc[1];
								if(ormNode.isSingleValuedAttr(attr))//single value attri
								{
									groupBuf.append("R").append(label).append(".`").append(attr).append("`, ");
									selectBuf.append("R").append(label).append(".`").append(attr).append("`, ");
								}
								else//multi-value attri, join the component relation 
								{
									int compRelIndex = ormNode.getCompRel4Attr(attr);
									int compLabel = getNodeLabel(i, compRelIndex, multivalueAttr++);
									isCompOutput[compRelIndex]=true;
									fromBuf.append("`").append(ormNode.getCompRelation(compRelIndex).getRelName()).append("` R").append(compLabel).append(", ");
									whereBuf.append(getSQL4Edge(ormNode, compRelIndex, compLabel, label, dbinfo)).append(" AND ");
									groupBuf.append("R").append(compLabel).append(".`").append(attr).append("`, ");
									selectBuf.append("R").append(compLabel).append(".`").append(attr).append("`, ");
								}
								outputAttr++;
							}
						}
					}
					
					if(pNode.getGroupbyID())//group by primary key
					{
//						groupBuf.append("R").append(label).append(".`").append(Constant.search_id).append("`, ");
						int keyAttrNum = rel.getKeyAttrNum();//num of primary key
						for(int k = 0; k < keyAttrNum; k++)
						{
							groupBuf.append("R").append(label).append(".`").append(rel.getKeyAttrName(k)).append("`, ");
							selectBuf.append("R").append(label).append(".`").append(rel.getKeyAttrName(k)).append("`, ");
						}
						outputAttr += keyAttrNum;
						for(int k = 0; k < rel.getVerifyAttrNum(); k++)
						{
							verifySet.add(addVerifyAttr(label, rel.getVerifyAttrName(k)));
						}
					}
				}
				
				for(String attr: verifySet)
				{
					verifyBuf.append(attr).append(", ");
				}
				verifyAttr[i] = verifySet.size();
				
			}
			
			//choose the main-buf or sub-buf
			From = targetNode?fromBuf:subFromBuf;
			Where = targetNode?whereBuf:subWhereBuf;
			
			//FROM - all nodes in pattern
			From.append(getSQL4Node(pgraph, ormgraph, i, outEdges, dbinfo)).append(", ");//get table names for the from clause.
			
			//check if it has other condition, add to the where
			if (!pNode.isTrival() && pNode.getConditionNum() > 0)
			{
				int condNum = pNode.getConditionNum();
				for(int j = 0; j < condNum; j++)
				{
					String[] condDesc = pNode.getConditionInfo(j, kwArray, true);
					String attr = condDesc[0];
					String val = condDesc[1];
					int kwIndex = pNode.getConditionKw(j);
					if(ormNode.isSingleValuedAttr(attr))
					{
						Where.append("MATCH(R").append(label).append(".`").append(attr).append("`) AGAINST ('").append(val).append("' IN BOOLEAN MODE) AND ");
					}
					else//component Node
					{
						int compRelIndex = ormNode.getCompRel4Attr(attr);
						int compLabel = getNodeLabel(i, compRelIndex, multivalueAttr++);
						int compRelId = ormNode.getCompRelId(dbinfo,compRelIndex);//get the ormID
						int kwPosition = pNode.getCondKwPosition(j);
						pNode.setKwLabel(kwPosition, compLabel);//set label for this relation
						
						From = isCompOutput[compRelIndex]?fromBuf:subFromBuf;
						Where = isCompOutput[compRelIndex]?whereBuf:subWhereBuf;
						
//							if (isCompOutput[compRelIndex])
//							{
//							From=fromBuf;
//							Where=whereBuf;
//							}
//							else
//							{
//								From=subFromBuf;
//								Where=subWhereBuf;
//							}
						
						Where.append("MATCH(R").append(compLabel).append(".`").append(attr).append("`) AGAINST ('").append(val).append("' IN BOOLEAN MODE) AND ");
						From.append("`").append(ormNode.getCompRelation(compRelIndex).getRelName()).append("` R").append(compLabel).append(", ");
						Where.append(getSQL4Edge(ormNode, compRelIndex, compLabel, label, dbinfo)).append(" AND ");
					}
				}
			}	
			
			
			//TP condition
			if (!pNode.isTrival() && pNode.isTemporalConditionNode())
			{
				if(pNode.isTpSequenceFirstNode())//temporal relationship, first kw
				{
					//find the second node
					PNode pNodeSec = pgraph.findSecNode(pNode);
					int pNodeIdSec = pgraph.findSecNodeId(pNodeSec);
					int ormNodeIdSec = pNodeSec.getORMNodeId();
					Node ormNodeSec = ormgraph.getNode(ormNodeIdSec);
					
					//get the relation id & relation label
					int [] tpInfoFirst = getTpinfo(pNode, kwArray, ormNode ,dbinfo, fromBuf, whereBuf, i,false);//0-relId,1-label
					int [] tpInfoSec = getTpinfo(pNodeSec, kwArray, ormNodeSec ,dbinfo, fromBuf, whereBuf, pNodeIdSec, true);//0-relId,1-label
					
					//get the attribute list
					String[] tpAttrString = pNode.getTpAttrName(dbinfo,tpInfoFirst[0],false);
					String[] tpSecAttrString = pNodeSec.getTpAttrName(dbinfo,tpInfoSec[0],true);
					
					//get the tp interpretation
					int tpIndex = pNode.getTpIndex();
					int tpConsIndex = tpArray[tpIndex].getTpIndex();
					String[][] tpInterpretation = new String[2][2];
					tpInterpretation = Constant.getTpInterpretation(tpConsIndex);
					
					for (int m = 0;m<2;m++)//generate the temporal string.
					{
						for (int n = 0;n<2;n++)
						{
							if (tpInterpretation[m][n]!=null && tpAttrString[m]!=null)
							{
								StringBuffer TemporalString = new StringBuffer("");
								TemporalString.append("R").append(tpInfoFirst[1]).append(".`").append(tpAttrString[m]).append("`").append(tpInterpretation[m][n]).append(" R").append(tpInfoSec[1]).append(".`").append(tpSecAttrString[n]).append("` ");
								//TemporalStringList.add(TemporalString);
								Where.append(TemporalString).append(" AND ");
							}
						}
					}		
					
				}
				else if(!pNode.isTpSequenceSecNode())//temporal condition
				{
					//get the relation name and label for the tp
					int [] tpInfoFirst = getTpinfo(pNode, kwArray, ormNode ,dbinfo, fromBuf, whereBuf, i,false);//0-relId,1-label
					//get the attribute list
					String[] tpAttrString = pNode.getTpAttrName(dbinfo,tpInfoFirst[0],false);
					//get the tp interpretation
					int tpIndex = pNode.getTpIndex();
					int tpConsIndex = tpArray[tpIndex].getTpIndex();
					String[][] tpInterpretation = new String[2][2];
					tpInterpretation = Constant.getTpInterpretation(tpConsIndex);
					//get the time period
					String[] period = new String[2];
					period[0] = tpArray[tpIndex].getFrom();
					period[1] = tpArray[tpIndex].getTo();
					//translate
					for (int m = 0;m<2;m++)//generate the temporal string.
					{
						for (int n = 0;n<2;n++)
						{
							if (tpInterpretation[m][n]!=null && tpAttrString[m]!=null)
							{
								StringBuffer TemporalString = new StringBuffer("");
								TemporalString.append("R").append(tpInfoFirst[1]).append(".`").append(tpAttrString[m]).append("`").append(tpInterpretation[m][n]).append("'").append(period[n]).append("' ");
								//TemporalStringList.add(TemporalString);
								Where.append(TemporalString).append(" AND ");
							}
						}
					}
				}
			}
				
		}
//			else
//			{
//				//add to subFrom
//				subFromBuf.append(getSQL4Node(pgraph, ormgraph, i, outEdges, dbinfo)).append(", ");//get table names for the from clause.
//				//create edge in the subWhere,check whether is a neighbor of the target node,have been done out of this "if"
//				//check the temporal
//				if (pNode.isTemporalConditionNode())
//				{ 
//					String relName = pNode.getTp4TargetRel();
//					if (ormNode.getNodeName() == relName)//the tp is not attached to a compNode
//					{
//						ArrayList<StringBuffer> TemporalStringList =  pNode.getTpredicateInfo(tpArray,dbinfo,coreRelId);
//						int tStringLen = TemporalStringList.size();
//						for (int j = 0; j < tStringLen; j++)
//						{
//							subWhereBuf.append("R").append(label).append(".").append(TemporalStringList.get(j)).append(" AND ");
//						}
//					}
//					else if (!pNode.isKw4condContains(relName,ormgraph,kwArray,dbinfo))//tanslate for this relation
//					{
//						int compRelIndex = ormNode.getCompRel4Name(relName);
//						int compLabel;
//						int compRelId = ormNode.getCompRelId(dbinfo,compRelIndex);//get the ormID
//						
//						compLabel = getNodeLabel(i, compRelIndex, multivalueAttr++);
//						subFromBuf.append("`").append(ormNode.getCompRelation(compRelIndex).getRelName()).append("` R").append(compLabel).append(", ");
//						subWhereBuf.append(getSQL4Edge(ormNode, compRelIndex, compLabel, label, dbinfo)).append(" AND ");
//						
//						ArrayList<StringBuffer> TemporalStringList =  pNode.getTpredicateInfo(tpArray,dbinfo, compRelId);
//						int tStringLen = TemporalStringList.size();
//						for (int k = 0; k < tStringLen; k++)
//						{
//							whereBuf.append("R").append(compLabel).append(".").append(TemporalStringList.get(k)).append(" AND ");
//						}
//					}
//					//do not consider the component node
////					ArrayList<StringBuffer> TemporalStringList =  pNode.getTpredicateInfo(tpArray,dbinfo,coreRelId);
////					int tStringLen = TemporalStringList.size();
////					for (int j = 0; j < tStringLen; j++)
////					{
////						subWhereBuf.append("R").append(label).append(".").append(TemporalStringList.get(j)).append(" AND ");
////					}
//				}
//				//check the other condition
//				if (pNode.isConditionNode())
//				{
//					int condNum = pNode.getConditionNum();
//					for(int j = 0; j < condNum; j++)
//					{
//						String[] condDesc = pNode.getConditionInfo(j, kwArray, true);
//						String attr = condDesc[0];
//						String val = condDesc[1];
//						int kwIndex = pNode.getConditionKw(j);
//						if(ormNode.isSingleValuedAttr(attr))
//						{
//							subWhereBuf.append("MATCH(R").append(label).append(".`").append(attr).append("`) AGAINST ('").append(val).append("' IN BOOLEAN MODE) AND ");
//						}
//						else
//						{
//							int compRelIndex = ormNode.getCompRel4Attr(attr);
//							int compLabel = getNodeLabel(i, compRelIndex, multivalueAttr++);
//							int compRelId = ormNode.getCompRelId(dbinfo,compRelIndex);//get the ormID
//							subWhereBuf.append("MATCH(R").append(compLabel).append(".`").append(attr).append("`) AGAINST ('").append(val).append("' IN BOOLEAN MODE) AND ");
//							subFromBuf.append("`").append(ormNode.getCompRelation(compRelIndex).getRelName()).append("` R").append(compLabel).append(", ");
//							subWhereBuf.append(getSQL4Edge(ormNode, compRelIndex, compLabel, label, dbinfo)).append(" AND ");
//							
//							//check whether it is a sequence tp
//							
//							if (pNode.isTemporalConditionNode())
//							{
//								if (pNode.isTemporalSequenceNode()&& pNode.getTpKw4condition()==kwIndex)//get the target obj kw
//								{
//									int tpIndex = pNode.getTpIndex();
//									ArrayList<StringBuffer> TemporalStringList = pNode.getSequenceTpInfo(tpArray[tpIndex],dbinfo,compRelId,compLabel);
//									int tStringLen = TemporalStringList.size();
//									for (int k = 0; k < tStringLen; k++)
//									{
//										subWhereBuf.append(TemporalStringList.get(k)).append(" AND ");
//									}
//								}
//								else if (!pNode.isTemporalSequenceNode()|| pNode.getTpSecKw4condition() != kwIndex)
//								{
////									String relName = pNode.getTpConditionInfo(n,tpArray,kwArray);
//									String relName = pNode.getTp4TargetRel();
//									if(ormNode.getCompRelName(compRelIndex) == relName)//??
//									{
//										ArrayList<StringBuffer> TemporalStringList =  pNode.getTpredicateInfo(tpArray,dbinfo, compRelId);
//										int tStringLen = TemporalStringList.size();
//										for (int k = 0; k < tStringLen; k++)
//										{
//											subWhereBuf.append("R").append(compLabel).append(".").append(TemporalStringList.get(k)).append(" AND ");
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//			}
		
//		if(outputAttr == 0)
//		{
//			int centricId = pgraph.getCentriId();
//			PNode centric = pgraph.getNode(centricId);
//			int ormNodeId = centric.getORMNodeId();
//			Node ormNode = ormgraph.getNode(ormNodeId);
//			Relation rel = ormNode.getCoreRelation();
//			int label = getNodeLabel(centricId);
//			int outputNum = rel.getOutputAttrNum();
//			for(int i = 0; i < outputNum; i++)
//			{
//				selectBuf.append("R").append(label).append(".`").append(rel.getOutputAttrName(i)).append("`, ");
//			}
//			outputAttr = outputNum;
//		}
		
		int buflen = selectBuf.length();
		if(buflen == selectInitialSize)
		{
			int centricIdtemp = pgraph.getCentriId();
			PNode centric = pgraph.getNode(centricIdtemp);
			int ormNodeId = centric.getORMNodeId();
			Node ormNode = ormgraph.getNode(ormNodeId);
			Relation rel = ormNode.getCoreRelation();
			int label = getNodeLabel(centricId);
			int outputNum = rel.getOutputAttrNum();
			for(int i = 0; i < outputNum; i++)
			{
				selectBuf.append("R").append(label).append(".`").append(rel.getOutputAttrName(i)).append("`, ");
			}
			fromBuf.append("`").append(rel.getRelName()).append("` R").append(label).append(", ");
		}

		String sql = null;
		sqlUnit.setOutputCol(outputAttr);
		sqlUnit.setVerifyCol(verifyAttr);
		selectBuf.append(verifyBuf);
		sql = formSQL(selectBuf, fromBuf, whereBuf, groupBuf, orderBuf, subSelectBuf, subFromBuf, subWhereBuf, subFromInitialSize, whereInitialSize, groupInitialSize, orderInitialSize);
		
		if(parentFn != null)
		{
			sqlUnit.setOutputCol(1);
			sqlUnit.resetVerifyCol();
			StringBuffer wrapBuf = new StringBuffer();
			sql = wrapBuf.append("SELECT ").append(parentFn).append("(RR.`").append(childFnLabel).append("`) AS `").append(parentFnLabel).append("` FROM (").append(sql).append(") RR").toString();
		}
		sqlUnit.setSQL(sql);
//		StringBuffer wrapBuf = new StringBuffer();
//		sql = wrapBuf.append("SELECT COUNT(*) FROM (").append(sql).append(") Result").toString();
//		sqlUnit.setCountSQL(sql);
		return sqlUnit;
	}

	public static SQLUnit getSQLUnitNoExist(PGraph pgraph, ORMGraph ormgraph, Keyword[] kwArray, Function[] fnArray, Groupby[] gpArray, Tpredicate[] tpArray, DBinfo dbinfo)
	{
		SQLUnit sqlUnit = new SQLUnit();
		StringBuffer selectBuf = new StringBuffer("SELECT ");
		StringBuffer fromBuf = new StringBuffer(" FROM ");
		StringBuffer whereBuf = new StringBuffer(" WHERE ");
		StringBuffer groupBuf = new StringBuffer(" GROUP BY ");
		StringBuffer orderBuf = new StringBuffer(" ORDER BY ");
		int whereInitialSize = whereBuf.length();
		int groupInitialSize = groupBuf.length();
		int orderInitialSize = orderBuf.length();
		
		String parentFn = null;
		String parentFnLabel = null;
		String childFnLabel = null;
		
		int outputAttr = 0;
		int multivalueAttr = 0;
		
		int nodeNum = pgraph.getNodeNum();
		int[] verifyAttr = new int[nodeNum];
		StringBuffer verifyBuf = new StringBuffer();
		
		boolean aggQuery;
		if(fnArray.length == 0 && gpArray.length == 0)
		{
			aggQuery = false;
		}
		else
		{
			aggQuery = true;
		}
		
		for(int i = 0; i < nodeNum; i++)//for each node
		{
			PNode pNode = pgraph.getNode(i);
			int[] outEdges = pgraph.getOutEdges(i);
			int ormNodeId = pNode.getORMNodeId();
			Node ormNode = ormgraph.getNode(ormNodeId);
			int coreRelId = ormNode.getCoreRelId(dbinfo);
			HashSet<String> verifySet = new HashSet<String>();
			
			int compRelNum = ormNode.getCompRelNum();
			boolean[] isCompOutput = new boolean[compRelNum];//default=false
			
			fromBuf.append(getSQL4NodeNoExist(pgraph, ormgraph, i, outEdges, dbinfo)).append(", ");//get table names for the from clause.
			
			int outEdgeNum = outEdges.length;
			for(int j = 0; j < outEdgeNum; j++)//for each out node
			{
				whereBuf.append(getSQL4Edge(pgraph, ormgraph, i, outEdges[j], dbinfo)).append(" AND ");//get edge relation for the join 
			}
			
			if(!pNode.isTrival())//this node is annotated
			{
				Relation rel = ormNode.getCoreRelation();//the type of ORM node
				int label = getNodeLabel(i);
				if(pNode.getOutputNum() > 0)//select buffer
				{
					int outputNum = pNode.getOutputNum();
					for(int j = 0; j < outputNum; j++)
					{
						String[] outputDesc = pNode.getOutput(j, kwArray);
						if(outputDesc[0].equals("name"))//table name
						{
							int outputAttrNum = rel.getOutputAttrNum();
							for(int k = 0; k < outputAttrNum; k++)
							{
								selectBuf.append("R").append(label).append(".`").append(rel.getOutputAttrName(k)).append("`, ");
							}
							outputAttr += outputAttrNum;
							
						}
						else//attribute name
						{
							String attr = outputDesc[1];
							if(ormNode.isSingleValuedAttr(attr))//single value attribute
							{
								selectBuf.append("R").append(label).append(".`").append(attr).append("`, ");
							}
							else//attri does not belong to this relation, join the object component relationship
							{
								int compRelIndex = ormNode.getCompRel4Attr(attr);//index of component relation
								int compLabel = getNodeLabel(i, compRelIndex, multivalueAttr++);
								selectBuf.append("R").append(compLabel).append(".`").append(attr).append("`, ");//select the table.attri
								fromBuf.append("`").append(ormNode.getCompRelation(compRelIndex).getRelName()).append("`").append(" R").append(compLabel).append(", ");//add to from
								whereBuf.append(getSQL4Edge(ormNode, compRelIndex, compLabel, label, dbinfo)).append(" AND ");
							}
							outputAttr++;
						}
					}
				}
				
				if(aggQuery)//aggregate
				{
					if(pNode.getFnNum() > 0)
					{
						int fnNum = pNode.getFnNum();
						for(int j = 0; j < fnNum; j++)
						{
							int fnIndex = pNode.getFnIndex(j);
							Function fn = fnArray[fnIndex];
							if(fn.isSimpleFn())
							{
								String[] fnDesc = pNode.getSimpleFnInfo(j, kwArray, fnArray);
								if(fnDesc[0].equals("name"))//fn(table_name)
								{
//									selectBuf.append(fn.getFnName()).append("(R").append(label).append(".`").append(Constant.search_id).append("`) AS F").append(getFnlabel(fnIndex)).append(", ");
//									selectBuf.append(fn.getFnName()).append("(R").append(label).append(".`").append(Constant.search_id).append("`) AS `").append(fn.getFnName()).append("(").append(fnDesc[1]).append(")`, ");
									selectBuf.append(fn.getFnName()).append("(*) AS `").append(fn.getFnName()).append("(").append(fnDesc[1]).append(")`, ");
								}
								else
								{
									String attr = fnDesc[1];
									if(ormNode.isSingleValuedAttr(attr))//single value attri
									{
//										selectBuf.append(fn.getFnName()).append("(R").append(label).append(".`").append(attr).append("`) AS F").append(getFnlabel(fnIndex)).append(", ");
										selectBuf.append(fn.getFnName()).append("(R").append(label).append(".`").append(attr).append("`) AS `").append(fn.getFnName()).append("(").append(attr).append(")`, ");
									}
									else//multi value attri, join the component relation
									{
										int compRelIndex = ormNode.getCompRel4Attr(attr);
										int compLabel = getNodeLabel(i, compRelIndex, multivalueAttr++);
//										selectBuf.append(fn.getFnName()).append("(R").append(compLabel).append(".`").append(attr).append("`) AS F").append(getFnlabel(fnIndex)).append(", ");
										selectBuf.append(fn.getFnName()).append("(R").append(compLabel).append(".`").append(attr).append("`) AS `").append(fn.getFnName()).append("(").append(attr).append(")`, ");
										fromBuf.append("`").append(ormNode.getCompRelation(compRelIndex).getRelName()).append("` R").append(compLabel).append(", ");
										whereBuf.append(getSQL4Edge(ormNode, compRelIndex, compLabel, label, dbinfo)).append(" AND ");
									}
								}
								outputAttr++;
							}
							else// only support single nested aggregate function for the moment
							{
								parentFn = fn.getFnName();
								Function childFn = fnArray[fn.getFnIndex()];
								String[] childFnDesc = pNode.getSimpleFnInfo(childFn, kwArray, fnArray);
								childFnLabel = childFn.getFnName() + "(" + childFnDesc[1] + ")";
								parentFnLabel = fn.getFnName() + "(" + childFnLabel + ")";
							}
						}
					}
					
					if(pNode.getGpNum() > 0)//add content to group by, select and order by
					{
						int gpNum = pNode.getGpNum();
						for(int j = 0; j < gpNum; j++)
						{
							String[] gpDesc = pNode.getGpInfo(j, kwArray);
							if(gpDesc[0].equals("name"))//table_name
							{
//								groupBuf.append("R").append(label).append(".`").append(Constant.search_id).append("`, ");
								int keyAttrNum = rel.getKeyAttrNum();
								for(int k = 0; k < keyAttrNum; k++)
								{
									groupBuf.append("R").append(label).append(".`").append(rel.getKeyAttrName(k)).append("`, ");
									selectBuf.append("R").append(label).append(".`").append(rel.getKeyAttrName(k)).append("`, ");
									orderBuf.append("R").append(label).append(".`").append(rel.getKeyAttrName(k)).append("`, ");
//									verifySet.add(addVerifyAttr(label, rel.getKeyAttrName(k)));
								}
								outputAttr += keyAttrNum;
								for(int k = 0; k < rel.getVerifyAttrNum(); k++)
								{
									verifySet.add(addVerifyAttr(label, rel.getVerifyAttrName(k)));
								}
							}
							else
							{
								String attr = gpDesc[1];
								if(ormNode.isSingleValuedAttr(attr))//single value attri
								{
									groupBuf.append("R").append(label).append(".`").append(attr).append("`, ");
									selectBuf.append("R").append(label).append(".`").append(attr).append("`, ");
									orderBuf.append("R").append(label).append(".`").append(attr).append("`, ");
//									verifySet.add(addVerifyAttr(label, attr));
								}
								else//multi-value attri, join the component relation 
								{
									int compRelIndex = ormNode.getCompRel4Attr(attr);
									int compLabel = getNodeLabel(i, compRelIndex, multivalueAttr++);
									fromBuf.append("`").append(ormNode.getCompRelation(compRelIndex).getRelName()).append("` R").append(compLabel).append(", ");
									whereBuf.append(getSQL4Edge(ormNode, compRelIndex, compLabel, label, dbinfo)).append(" AND ");
									groupBuf.append("R").append(compLabel).append(".`").append(attr).append("`, ");
									selectBuf.append("R").append(compLabel).append(".`").append(attr).append("`, ");
									orderBuf.append("R").append(compLabel).append(".`").append(attr).append("`, ");
//									verifySet.add(addVerifyAttr(compLabel, attr));
								}
								outputAttr++;
							}
						}
					}
					
					if(pNode.getGroupbyID())//group by primary key
					{
//						groupBuf.append("R").append(label).append(".`").append(Constant.search_id).append("`, ");
						int keyAttrNum = rel.getKeyAttrNum();//num of primary key
						for(int k = 0; k < keyAttrNum; k++)
						{
							groupBuf.append("R").append(label).append(".`").append(rel.getKeyAttrName(k)).append("`, ");
							selectBuf.append("R").append(label).append(".`").append(rel.getKeyAttrName(k)).append("`, ");
							orderBuf.append("R").append(label).append(".`").append(rel.getKeyAttrName(k)).append("`, ");
//							verifySet.add(addVerifyAttr(label, rel.getKeyAttrName(k)));
						}
						outputAttr += keyAttrNum;
						for(int k = 0; k < rel.getVerifyAttrNum(); k++)
						{
							verifySet.add(addVerifyAttr(label, rel.getVerifyAttrName(k)));
						}
					}
				}
				
				//check if it is a temporal node add to the where
				if (pNode.isTemporalConditionNode())
				{
					String relName = pNode.getTp4TargetRel();
					int tpKwIndex = pNode.getTpKw4condition();
					if (ormNode.getNodeName() == relName)//the tp is not attached to a compNode
					{
						ArrayList<StringBuffer> TemporalStringList =  pNode.getTpredicateInfo(tpArray,dbinfo,coreRelId);
						int tStringLen = TemporalStringList.size();
						for (int j = 0; j < tStringLen; j++)
						{
							whereBuf.append("R").append(label).append(".").append(TemporalStringList.get(j)).append(" AND ");
						}
					}
					else if (!pNode.isKw4condContains(relName,ormgraph,kwArray,dbinfo))//tanslate for this relation
					{
						int compRelIndex = ormNode.getCompRel4Name(relName);
						int compLabel;
						int compRelId = ormNode.getCompRelId(dbinfo,compRelIndex);//get the ormID
						if (isCompOutput[compRelIndex])
						{
							compLabel = getNodeLabel(i, compRelIndex, multivalueAttr-1);
						}
						else
						{
							compLabel = getNodeLabel(i, compRelIndex, multivalueAttr++);
							fromBuf.append("`").append(ormNode.getCompRelation(compRelIndex).getRelName()).append("` R").append(compLabel).append(", ");
							whereBuf.append(getSQL4Edge(ormNode, compRelIndex, compLabel, label, dbinfo)).append(" AND ");
						}
						ArrayList<StringBuffer> TemporalStringList =  pNode.getTpredicateInfo(tpArray,dbinfo, compRelId);
						int tStringLen = TemporalStringList.size();
						for (int k = 0; k < tStringLen; k++)
						{
							whereBuf.append("R").append(compLabel).append(".").append(TemporalStringList.get(k)).append(" AND ");
						}
						
					}
				}
				
				if(pNode.getConditionNum() > 0)//condition
				{
					if(!aggQuery)
					{
						for(int j = 0; j < rel.getVerifyAttrNum(); j++)
						{
							verifySet.add(addVerifyAttr(label, rel.getVerifyAttrName(j)));
						}
					}
					
					int condNum = pNode.getConditionNum();
					for(int j = 0; j < condNum; j++)
					{
						String[] condDesc = pNode.getConditionInfo(j, kwArray, true);
						String attr = condDesc[0];
						String val = condDesc[1];
						int kwIndex = pNode.getConditionKw(j);
						if(ormNode.isSingleValuedAttr(attr))
						{
							whereBuf.append("MATCH(R").append(label).append(".`").append(attr).append("`) AGAINST ('").append(val).append("' IN BOOLEAN MODE) AND ");
							
							if(!aggQuery)
							{
								verifySet.add(addVerifyAttr(label, attr));
//								orderBuf.append("R").append(label).append(".`").append(attr).append("`, ");
							}
						}
						else
						{
							int compRelIndex = ormNode.getCompRel4Attr(attr);
//							Relation compRel = ormNode.getCompRelation(compRelIndex);
							int compLabel = getNodeLabel(i, compRelIndex, multivalueAttr++);
							int compRelId = ormNode.getCompRelId(dbinfo,compRelIndex);//get the ormID

							whereBuf.append("MATCH(R").append(compLabel).append(".`").append(attr).append("`) AGAINST ('").append(val).append("' IN BOOLEAN MODE) AND ");
							
							fromBuf.append("`").append(ormNode.getCompRelation(compRelIndex).getRelName()).append("` R").append(compLabel).append(", ");
							whereBuf.append(getSQL4Edge(ormNode, compRelIndex, compLabel, label, dbinfo)).append(" AND ");
							
							
							//check whether it is a sequence tp
							
							if (pNode.isTemporalConditionNode())
							{
								if (pNode.isTemporalSequenceNode()&& pNode.getTpKw4condition()==kwIndex)//get the target obj kw
								{
									int tpIndex = pNode.getTpIndex();
									ArrayList<StringBuffer> TemporalStringList = pNode.getSequenceTpInfo(tpArray[tpIndex],dbinfo,compRelId,compLabel);
									int tStringLen = TemporalStringList.size();
									for (int k = 0; k < tStringLen; k++)
									{
										whereBuf.append(TemporalStringList.get(k)).append(" AND ");
									}
								}
								else if (!pNode.isTemporalSequenceNode()|| pNode.getTpSecKw4condition() != kwIndex)
								{
//									String relName = pNode.getTpConditionInfo(n,tpArray,kwArray);
									String relName = pNode.getTp4TargetRel();
									if(ormNode.getCompRelName(compRelIndex) == relName)//??
									{
										ArrayList<StringBuffer> TemporalStringList =  pNode.getTpredicateInfo(tpArray,dbinfo, compRelId);
										int tStringLen = TemporalStringList.size();
										for (int k = 0; k < tStringLen; k++)
										{
											whereBuf.append("R").append(compLabel).append(".").append(TemporalStringList.get(k)).append(" AND ");
										}
									}
								}
							}
							if(!aggQuery)
							{
								verifySet.add(addVerifyAttr(compLabel, attr));
//								orderBuf.append("R").append(compLabel).append(".`").append(attr).append("`, ");
							}
						}
					}
				}
				
				for(String attr: verifySet)
				{
					verifyBuf.append(attr).append(", ");
				}
				verifyAttr[i] = verifySet.size();
			}
		}
		
		if(outputAttr == 0)//for Demo
		{
			int centricId = pgraph.getCentriId();
			PNode centric = pgraph.getNode(centricId);
			int ormNodeId = centric.getORMNodeId();
			Node ormNode = ormgraph.getNode(ormNodeId);
			Relation rel = ormNode.getCoreRelation();
			int label = getNodeLabel(centricId);
			int outputNum = rel.getOutputAttrNum();
			for(int i = 0; i < outputNum; i++)
			{
				selectBuf.append("R").append(label).append(".`").append(rel.getOutputAttrName(i)).append("`, ");
			}
			outputAttr = outputNum;
		}

		String sql = null;
		sqlUnit.setOutputCol(outputAttr);
		sqlUnit.setVerifyCol(verifyAttr);
		selectBuf.append(verifyBuf);
		sql = formSQL(selectBuf, fromBuf, whereBuf, groupBuf, orderBuf, whereInitialSize, groupInitialSize, orderInitialSize);
		
		if(parentFn != null)
		{
			sqlUnit.setOutputCol(1);
			sqlUnit.resetVerifyCol();
			StringBuffer wrapBuf = new StringBuffer();
			sql = wrapBuf.append("SELECT ").append(parentFn).append("(RR.`").append(childFnLabel).append("`) AS `").append(parentFnLabel).append("` FROM (").append(sql).append(") RR").toString();
		}
		sqlUnit.setSQL(sql);
//		StringBuffer wrapBuf = new StringBuffer();
//		sql = wrapBuf.append("SELECT COUNT(*) FROM (").append(sql).append(") Result").toString();
//		sqlUnit.setCountSQL(sql);
		return sqlUnit;
	}
	
	
	private static String addVerifyAttr(int label, String attr)
	{
		StringBuffer buf = new StringBuffer("R");
		buf.append(label).append(".`").append(attr).append("`");
		return buf.toString();
	}
	
	private static String formSQL(StringBuffer selectBuf, StringBuffer fromBuf, StringBuffer whereBuf, int whereInitialSize)
	{
		int buflen = selectBuf.length();
		selectBuf.delete(buflen - 2, buflen);
		buflen = fromBuf.length();
		fromBuf.delete(buflen - 2, buflen);
		buflen = whereBuf.length();
		if(buflen > whereInitialSize)
		{
			whereBuf.delete(buflen - 5, buflen);
		}
		else
		{
			whereBuf.delete(0, buflen);
		}
		return selectBuf.append(fromBuf).append(whereBuf).toString();
	}
	
	private static String formSQL(StringBuffer selectBuf, StringBuffer fromBuf, StringBuffer whereBuf, StringBuffer groupBuf, StringBuffer orderBuf, int whereInitialSize, int groupInitialSize, int orderInitialSize)
	{
		int buflen = selectBuf.length();
		selectBuf.delete(buflen - 2, buflen);
		buflen = fromBuf.length();
		fromBuf.delete(buflen - 2, buflen);
		buflen = whereBuf.length();
		if(buflen > whereInitialSize)
		{
			whereBuf.delete(buflen - 5, buflen);
		}
		else
		{
			whereBuf.delete(0, buflen);
		}
		buflen = groupBuf.length();
		if(buflen > groupInitialSize)
		{
			groupBuf.delete(buflen - 2, buflen);
		}
		else
		{
			groupBuf.delete(0, buflen);
		}
		buflen = orderBuf.length();
		if(buflen > orderInitialSize)
		{
			orderBuf.delete(buflen - 2, buflen);
		}
		else
		{
			orderBuf.delete(0, buflen);
		}
		return selectBuf.append(fromBuf).append(whereBuf).append(groupBuf).append(orderBuf).toString();
	}
	
	private static String formSQL(StringBuffer selectBuf, StringBuffer fromBuf, StringBuffer whereBuf, StringBuffer groupBuf, StringBuffer orderBuf, StringBuffer subSelectBuf, StringBuffer subFromBuf, StringBuffer subWhereBuf, int subFromInitialSize,  int whereInitialSize, int groupInitialSize, int orderInitialSize)
	{
		
		String sql = null;
		
		//combine clauses
		//Is there a "exist" condition?
		Boolean isExistClause;
		int buflen = subFromBuf.length();
		if (buflen != subFromInitialSize)
		{
			isExistClause = true;
		}
		else
		{
			isExistClause = false;
		}
		
		buflen = selectBuf.length();
		selectBuf.delete(buflen - 2, buflen);
		buflen = fromBuf.length();
		fromBuf.delete(buflen - 2, buflen);
		buflen = whereBuf.length();
		if (!isExistClause)//no exist clause
		{
			if (buflen == whereInitialSize)//no condition
			{
				whereBuf.delete(0, buflen);//delete the where clause
			}
			else
			{
				whereBuf.delete(buflen - 5, buflen);//delete the "AND" at the end
			} 
		}
		else//with "exist" clause
		{
			buflen = subFromBuf.length();
			subFromBuf.delete(buflen - 2, buflen);
			buflen = subWhereBuf.length();
			subWhereBuf.delete(buflen - 5, buflen);
		}
		
		buflen = groupBuf.length();
		if(buflen > groupInitialSize)
		{
			groupBuf.delete(buflen - 2, buflen);
		}
		else
		{
			groupBuf.delete(0, buflen);
		}
		
		if(isExistClause)
		{
			sql = selectBuf.append(fromBuf).append(whereBuf).append("exists (").append(subSelectBuf).append(subFromBuf).append(subWhereBuf).append(")").append(groupBuf).toString();
		}
		else
		{
			sql = selectBuf.append(fromBuf).append(whereBuf).append(groupBuf).toString();
		}
		
		return sql;
	}
	
//	private static String getSQL4Edge(Node ormNode, int compRelIndex, DBinfo dbinfo)
//	{
//		StringBuffer sqlbuf = new StringBuffer();
//		int relLid = ormNode.getCompRelIndex(dbinfo, compRelIndex);
//		int relRid = ormNode.getCoreRelIndex(dbinfo);
//		if(relLid != -1 && relRid != -1)
//		{
//			Dependency fk = dbinfo.getDependency(relLid, relRid);
//			sqlbuf.append("`").append(fk.getRelL().getRelName()).append("`.`").append(fk.getAttrL()).append("`=`").append(fk.getRelR().getRelName()).append("`.`").append(fk.getAttrR()).append("`");
//		}
//		return sqlbuf.toString();
//	}
	
	private static String getSQL4Edge(Node ormNode, int compRelIndex, int labelL, int labelR, DBinfo dbinfo)
	{
		StringBuffer sqlbuf = new StringBuffer();
		int relLid = ormNode.getCompRelIndex(dbinfo, compRelIndex);//compRelIndex=0
		int relRid = ormNode.getCoreRelIndex(dbinfo);
		if(relLid != -1 && relRid != -1)
		{
			Dependency fk = dbinfo.getDependency(relLid, relRid);//Lid=4.Rid=3
			sqlbuf.append("R").append(labelL).append(".`").append(fk.getAttrL()).append("`=R").append(labelR).append(".`").append(fk.getAttrR()).append("`");
		}
		return sqlbuf.toString();
	}
	
	private static String getSQL4Edge(AuxGraph graph, int fromNodeId, int toNodeId, DBinfo dbinfo)
	{
		StringBuffer sqlbuf = new StringBuffer();
		int relLid = graph.getNode(fromNodeId);
		int relRid = graph.getNode(toNodeId);
		if(relLid != -1 && relRid != -1)
		{
			Dependency fk = dbinfo.getDependency(relLid, relRid);
			sqlbuf.append("`").append(fk.getRelL().getRelName()).append("`.`").append(fk.getAttrL()).append("`=`").append(fk.getRelR().getRelName()).append("`.`").append(fk.getAttrR()).append("`");
		}
		return sqlbuf.toString();
	}
	
//	private static String getSQL4Edge(Node ormNode, int compRelIndex, int labelL, int labelR, DBinfo dbinfo)
//	{
//		StringBuffer sqlbuf = new StringBuffer();
//		int relLid = ormNode.getCompRelIndex(dbinfo, compRelIndex);
//		int relRid = ormNode.getCoreRelIndex(dbinfo);
//		if(relLid != -1 && relRid != -1)
//		{
//			Dependency fk = dbinfo.getDependency(relLid, relRid);
//			sqlbuf.append("R").append(labelL).append(".`").append(fk.getAttrL()).append("`=R").append(labelR).append(".`").append(fk.getAttrR()).append("`");
//		}
//		return sqlbuf.toString();
//	}
	
	private static String getSQL4Edge(PGraph pgraph, ORMGraph ormgraph, int fromNodeId, int toNodeId, DBinfo dbinfo)
	{
		StringBuffer sqlbuf = new StringBuffer();
		PNode fromNode = pgraph.getNode(fromNodeId);
		PNode toNode = pgraph.getNode(toNodeId);
		int fromORMNodeId = fromNode.getORMNodeId();
		int toORMNodeId = toNode.getORMNodeId();
		Node fromORMNode = ormgraph.getNode(fromORMNodeId);
		Node toORMNode = ormgraph.getNode(toORMNodeId);
		int relLid = fromORMNode.getCoreRelIndex(dbinfo);
		int relRid = toORMNode.getCoreRelIndex(dbinfo);
		if(relLid != -1 && relRid != -1)
		{
			Dependency fk = dbinfo.getDependency(relLid, relRid);
			int labelL = getNodeLabel(fromNodeId);
			int labelR = getNodeLabel(toNodeId);
			sqlbuf.append("R").append(labelL).append(".`").append(fk.getAttrL()).append("`=R").append(labelR).append(".`").append(fk.getAttrR()).append("`");
		}
		return sqlbuf.toString();
	}
	
	private static String getSQL4Node(PGraph pgraph, ORMGraph ormgraph, int nodeId, int[] outEdges, DBinfo dbinfo)
	{
		StringBuffer sqlbuf = new StringBuffer();
		int label = getNodeLabel(nodeId);
		PNode pnode = pgraph.getNode(nodeId);
		int ormNodeId = pnode.getORMNodeId();
		Node ormNode = ormgraph.getNode(ormNodeId);
		Relation rel = ormNode.getCoreRelation();
		int[] outEdges4ORM = ormgraph.getOutEdges(ormNodeId);
		
		int outEdgesLen = outEdges.length;
		int outEdges4ORMLen = outEdges4ORM.length;
		
		
//		if(ormNode.getNodeType() == NodeType.Relationship && outEdgesLen < outEdges4ORMLen)
//		{
//			sqlbuf.append("(SELECT DISTINCT ");
//			for(int i = 0; i < outEdgesLen; i++)
//			{
//				int toNodeId = outEdges[i];
//				PNode toNode = pgraph.getNode(toNodeId);
//				int toORMNodeId = toNode.getORMNodeId();
//				Node toORMNode = ormgraph.getNode(toORMNodeId);
//				int relLid = ormNode.getCoreRelIndex(dbinfo);
//				int relRid = toORMNode.getCoreRelIndex(dbinfo);
//				if(relLid != -1 && relRid != -1)
//				{
//					Dependency fk = dbinfo.getDependency(relLid, relRid);
//					String attrL = fk.getAttrL();
//					sqlbuf.append("`").append(attrL).append("`, ");
//				}
//			}
//			int len = sqlbuf.length();
//			sqlbuf.delete(len - 2, len);
//			sqlbuf.append(" FROM " ).append("`").append(rel.getRelName()).append("`) R").append(label);
//		}
//		else
//		{
//			sqlbuf.append("`").append(rel.getRelName()).append("` R").append(label);
//		}
		
		sqlbuf.append("`").append(rel.getRelName()).append("` R").append(label);//add the table name into "From"
		return sqlbuf.toString();
	}
	
	private static String getSQL4NodeNoExist(PGraph pgraph, ORMGraph ormgraph, int nodeId, int[] outEdges, DBinfo dbinfo)
	{
		StringBuffer sqlbuf = new StringBuffer();
		int label = getNodeLabel(nodeId);//nodeId+1
		PNode pnode = pgraph.getNode(nodeId);
		int ormNodeId = pnode.getORMNodeId();
		Node ormNode = ormgraph.getNode(ormNodeId);
		Relation rel = ormNode.getCoreRelation();
		int[] outEdges4ORM = ormgraph.getOutEdges(ormNodeId);
		
		int outEdgesLen = outEdges.length;//in the pattern
		int outEdges4ORMLen = outEdges4ORM.length;//in the ORM graph
		if(ormNode.getNodeType() == NodeType.Relationship && outEdgesLen < outEdges4ORMLen)//"outEdgesLen < outEdges4ORMLen"?
		{
			sqlbuf.append("(SELECT DISTINCT ");
			for(int i = 0; i < outEdgesLen; i++)//for each out edge of this node
			{
				int toNodeId = outEdges[i];
				PNode toNode = pgraph.getNode(toNodeId);
				int toORMNodeId = toNode.getORMNodeId();
				Node toORMNode = ormgraph.getNode(toORMNodeId);
				int relLid = ormNode.getCoreRelIndex(dbinfo);//index of this node in the relation array
				int relRid = toORMNode.getCoreRelIndex(dbinfo);//index of to_node in the relation array
				if(relLid != -1 && relRid != -1)
				{
					Dependency fk = dbinfo.getDependency(relLid, relRid);//get the dependency
					String attrL = fk.getAttrL();//this node attr
					sqlbuf.append("`").append(attrL).append("`, ");//"select distinct this_node_attr"
				}
			}
			int len = sqlbuf.length();
			sqlbuf.delete(len - 2, len);//delect the blank space
			sqlbuf.append(" FROM " ).append("`").append(rel.getRelName()).append("`) R").append(label);// add the table name to From
		}
		else
		{
			sqlbuf.append("`").append(rel.getRelName()).append("` R").append(label);//add the table name into "select"
		}
		return sqlbuf.toString();//select or a sub0query???
	}
	
//	private static int getFnlabel(int fnIndex)
//	{
//		return fnIndex + 1;
//	}
	
	private static int getNodeLabel(int nodeId, int compRelIndex, int multiValuedAttr)
	{
//		return (nodeId + 1)*10 + compRelIndex + 1 + multiValuedAttr;
		return (nodeId + 1)*100 + (compRelIndex + 1)*10 + multiValuedAttr;
	}
	
	private static int getNodeLabel(int nodeId)
	{
		return nodeId + 1;
	}
	
	public static String[] getQueryDesc(PGraph pgraph, ORMGraph ormgraph, Keyword[] kwArray, Function[] fnArray, Groupby[] gpArray, Tpredicate[] tpArray, String kwStyle, String fnStyle, String tpStyle)
	{
		String[] desc = new String[2];
		TNode root = graph2Tree(pgraph);
		desc[0] = traverseTree4Xml(ormgraph, pgraph, root, true, kwArray, fnArray, gpArray, tpArray);
		int[] targetId = pgraph.getTargetNode();
		int targetNodeNum = targetId.length;
		if(targetNodeNum > 1)
		{
			desc[1] = traverseTree4DescMultiTarget(ormgraph, pgraph, targetId, root, kwArray, fnArray, gpArray, tpArray, kwStyle, fnStyle, tpStyle);
		}
		else
		{
			desc[1] = traverseTree4DescSingleTarget(ormgraph, pgraph, root, true, kwArray, fnArray, gpArray, tpArray, kwStyle, fnStyle, tpStyle);
		}
		desc[1] += traverseGraph4CascadeTarget(ormgraph, pgraph, fnArray, fnStyle, tpStyle);
		return desc;
	}
	
	private static void setTNodeLabel(PGraph pgraph, int[] targetId, TNode node)
	{
		int targetNum = targetId.length;
		for(int i = 0; i < targetNum; i++)
		{
			int nodeId = targetId[i];
			TNode tnode = getTNode(node, nodeId);
			tnode.setNodeLabel("t" + (i + 1));
		}
	}
	
	private static TNode getTNode(TNode root, int pnodeId)
	{
		if(root.getPNodeId() == pnodeId)
		{
			return root;
		}
		else
		{
			int childNum = root.getChildNum();
			for(int i = 0; i < childNum; i++)
			{
				TNode tnode = getTNode(root.getChild(i), pnodeId);
				if(tnode != null)
				{
					return tnode;
				}
			} 
		}
		return null;
	}
	
	
	private static String traverseGraph4CascadeTarget(ORMGraph ormgraph, PGraph pgraph, Function[] fnArray, String fnStyle, String tpStyle)
	{
		StringBuffer descBuf = new StringBuffer();
		int nodeNum = pgraph.getNodeNum();
		for(int i = 0; i < nodeNum; i++)
		{
			PNode pNode = pgraph.getNode(i); 
			if(!pNode.isTrival())
			{
				int fnNum = pNode.getFnNum();
				for(int j = 0; j < fnNum; j++)
				{
					int fnIndex = pNode.getFnIndex(j);
					Function fn = fnArray[fnIndex];
					if(!fn.isSimpleFn())
					{
						int subFnIndex = fn.getFnIndex();
						Function subFn = fnArray[subFnIndex];
						descBuf.append("\nCompute the <span class=\"").append(fnStyle).append("\">").append(fn.getFnName()).append("</span> function on the result of the <span class=\"").append(fnStyle).append("\">").append(subFn.getFnName()).append("</span> function");
					}
				}
			}
		}
		return descBuf.toString();
	}
	
	private static String traverseTree4DescMultiTarget(ORMGraph ormgraph, PGraph pgraph, int[] targetId, TNode root,  Keyword[] kwArray, Function[] fnArray, Groupby[] gpArray, Tpredicate[] tpArray, String kwStyle, String fnStyle, String tpStyle)
	{
		setTNodeLabel(pgraph, targetId, root);
		StringBuffer descBuf = new StringBuffer();

		descBuf.append("Find");
		
		int targetNum = targetId.length;
		for(int i = 0; i < targetNum; i++)
		{
			int targetNodeId = targetId[i];
			PNode targetPNode = pgraph.getNode(targetNodeId);
			int targetORMNodeId = targetPNode.getORMNodeId();
			Node targetORMNode = ormgraph.getNode(targetORMNodeId);
			StringBuffer outputBuf = new StringBuffer();
			if(targetPNode.hasAttrOutput(kwArray))
			{
				outputBuf.append(" the ");
				int outputNum = targetPNode.getOutputNum();
				for(int j = 0; j < outputNum; j++)
				{
					String[] outputDesc = targetPNode.getOutput(j, kwArray);
					if(outputDesc[0].equals("attr"))
					{
						outputBuf.append("<span class=\"").append(kwStyle).append("\">").append(outputDesc[1]).append("</span> and ");
					}
				}
			}
			if(targetPNode.getFnNum() > 0)
			{
				outputBuf.append(" the ").append(getFuncStatement(targetPNode, kwArray, fnArray, kwStyle, fnStyle));
			}
			
			if(outputBuf.length() > 0)
			{
				descBuf.append(outputBuf);
				int len = descBuf.length();
				descBuf.delete(len - 5, len);
				descBuf.append(" of");
			}
			if(targetPNode.isMatchKw(targetORMNode.getNodeName(), kwArray))
			{
				descBuf.append(" the <span class=\"").append(kwStyle).append("\">").append(targetORMNode.getNodeName()).append("</span> <i>t").append(i + 1).append("</i> and ");
			}
			else
			{
				descBuf.append(" the ").append(targetORMNode.getNodeName()).append(" <i>t").append(i + 1).append("</i> and ");
			}
		}
		descBuf.delete(descBuf.length() - 5, descBuf.length());
		descBuf.append(" such that ");
		
		int pNodeId = root.getPNodeId();
		PNode pNode = pgraph.getNode(pNodeId);
		int ormNodeId = pNode.getORMNodeId();
		int childNum = root.getChildNum();
		
		for(int i = 0; i < childNum; i++)
		{
			descBuf.append(" <i>" + root.getNodeLabel() + "</i>").append(" ");
			TNode child = root.getChild(i);
			int childpNodeId = child.getPNodeId();
			PNode childpNode = pgraph.getNode(childpNodeId);
			if(childpNode.getType(ormgraph) != NodeType.Relationship)
			{
				int childORMNodeId = childpNode.getORMNodeId();
				descBuf.append(ormgraph.getLabel(ormNodeId, childORMNodeId)).append(traverseTree4DescSingleTarget(ormgraph, pgraph, child, false, kwArray, fnArray, gpArray, tpArray, kwStyle, fnStyle, tpStyle));
			}
			else
			{
				if(child.getChildNum() > 0)
				{
					TNode childChild = child.getChild(0);
					int childChildpNodeId = childChild.getPNodeId();
					PNode childChildpNode = pgraph.getNode(childChildpNodeId);
					
					int childChildORMNodeId = childChildpNode.getORMNodeId();
					descBuf.append(ormgraph.getLabel(ormNodeId, childChildORMNodeId)).append(traverseTree4DescSingleTarget(ormgraph, pgraph, childChild, false, kwArray, fnArray, gpArray, tpArray, kwStyle, fnStyle, tpStyle));
				}
			}
			descBuf.append(" and ");
		}
		descBuf.delete(descBuf.length() - 5, descBuf.length());
		return descBuf.toString();
	}
	private static String traverseTree4DescSingleTarget(ORMGraph ormgraph, PGraph pgraph, TNode node, boolean isRoot, Keyword[] kwArray, Function[] fnArray, Groupby[] gpArray, Tpredicate[] tpArray,  String kwStyle, String fnStyle, String tpStyle)
	{
		StringBuffer descBuf = new StringBuffer();
		
		int pNodeId = node.getPNodeId();
		PNode pNode = pgraph.getNode(pNodeId);
		int ormNodeId = pNode.getORMNodeId();
		Node ormNode = ormgraph.getNode(ormNodeId);
		
		if(isRoot)
		{
			descBuf.append("Find");
			if(!pNode.isTrival())
			{
				StringBuffer outputBuf = new StringBuffer();
				if(pNode.hasAttrOutput(kwArray))
				{
					outputBuf.append(" the ");
					int outputNum = pNode.getOutputNum();
					for(int i = 0; i < outputNum; i++)
					{
						String[] outputDesc = pNode.getOutput(i, kwArray);
						if(outputDesc[0].equals("attr"))
						{
							outputBuf.append("<span class=\"").append(kwStyle).append("\">").append(outputDesc[1]).append("</span> and ");
						}
					}
				}
				
				if(pNode.getFnNum() > 0)
				{
					outputBuf.append(" the ").append(getFuncStatement(pNode, kwArray, fnArray, kwStyle, fnStyle));
				}
				
				if(outputBuf.length() > 0)
				{
					descBuf.append(outputBuf);
					int len = descBuf.length();
					descBuf.delete(len - 5, len);
					descBuf.append(" of");
				}
			}
		}
				
		if(pNode.getType(ormgraph) != NodeType.Relationship)
		{
			descBuf.append(" the ");
			if(pNode.isTrival())
			{
				descBuf.append(ormNode.getNodeName());
			}
			else
			{
				if(pNode.isMatchKw(ormNode.getNodeName(), kwArray))
				{
					descBuf.append("<span class=\"").append(kwStyle).append("\">").append(ormNode.getNodeName()).append("</span>");
				}
				else
				{
					descBuf.append(ormNode.getNodeName());
				}
				if(node.hasLabel())
				{
					descBuf.append(" <i>" + node.getNodeLabel() + "</i>");
				}
				if(pNode.getConditionNum() > 0)
				{
					descBuf.append(" with ").append(getConditionStatement(pNode, kwArray, kwStyle));
				}
				if(pNode.isTemporalNode())
				{
					descBuf.append(" with ").append(getPredicateStatement(pNode, kwArray, tpArray,  kwStyle, tpStyle));
				}
				if(pNode.getGpNum() > 0 || pNode.getGroupbyID())
				{
					descBuf.append(" ").append(getGroupByStatement(pNode, kwArray, kwStyle, fnStyle));
				}
			}
			
			int childNum = node.getChildNum();
			if(childNum > 0)
			{
				descBuf.append(" that ");
				for(int i = 0; i < childNum; i++)
				{
					TNode child = node.getChild(i);
					int childpNodeId = child.getPNodeId();
					PNode childpNode = pgraph.getNode(childpNodeId);
					
					if(childpNode.getType(ormgraph) != NodeType.Relationship)
					{
						int childORMNodeId = childpNode.getORMNodeId();
						descBuf.append(ormgraph.getLabel(ormNodeId, childORMNodeId)).append(traverseTree4DescSingleTarget(ormgraph, pgraph, child, false, kwArray, fnArray, gpArray, tpArray, kwStyle, fnStyle, tpStyle));
					}
					else
					{
						if(child.getChildNum() > 0)
						{
							TNode childChild = child.getChild(0);
							int childChildpNodeId = childChild.getPNodeId();
							PNode childChildpNode = pgraph.getNode(childChildpNodeId);
							
							int childChildORMNodeId = childChildpNode.getORMNodeId();
							descBuf.append(ormgraph.getLabel(ormNodeId, childChildORMNodeId)).append(traverseTree4DescSingleTarget(ormgraph, pgraph, childChild, false, kwArray, fnArray, gpArray, tpArray, kwStyle, fnStyle, tpStyle));
							if(childpNode.isTemporalNode())
							{
								descBuf.append(" with ").append(getPredicateStatement(childpNode, kwArray, tpArray, kwStyle, tpStyle));
							}
						}
					}
					descBuf.append(" and ");
				}
				descBuf.delete(descBuf.length() - 5, descBuf.length());
			}
		}
		return descBuf.toString();
	}

	private static String getPredicateStatement(PNode pNode, Keyword[] kwArray, Tpredicate[] tpArray, String kwStyle, String tpStyle)
	{
		StringBuffer statBuf = new StringBuffer();
		
		String[] info = pNode.getTpInfo(kwArray, tpArray);
		String attr1 = info[0];
		String attr2 = info[2];//temporal attribute name
		if(pNode.isMatchKw(attr1, kwArray))
		{
			attr1 =  "<span class=\"" + kwStyle + "\">" + attr1 + "</span>";
		}
		if(pNode.isMatchKw(attr2, kwArray))
		{
			attr2 =  "<span class=\"" + kwStyle + "\">" + attr2 + "</span>";
		}
		String tp = "<span class=\"" + tpStyle + "\">" + info[1] + "</span>";
		statBuf.append(" the time period referred to by ").append(attr1).append(" ").append(tp).append(" the time period referred to by ").append(attr2);
		return statBuf.toString();
	}

	private static String getGroupByStatement(PNode pNode, Keyword[] kwArray, String kwStyle, String fnStyle)
	{
		StringBuffer statBuf = new StringBuffer();
		if(pNode.getGroupbyID())
		{
			statBuf.append("<span class=\"").append(fnStyle).append("\">").append(Constant.groupby).append("</span> ID and ");
		}
		
		int gpNum = pNode.getGpNum();
		for(int i = 0; i < gpNum; i++)
		{
			String[] info = pNode.getGpInfo(i, kwArray);
			if(info[0].equals("attr"))
			{
				statBuf.append("<span class=\"").append(fnStyle).append("\">").append(Constant.groupby).append("</span> <span class=\"").append(kwStyle).append("\">").append(info[1]).append("</span> and ");
			}
			else
			{
				statBuf.append("<span class=\"").append(fnStyle).append("\">").append(Constant.groupby).append("</span> ID and ");
			}
		}
		int bufLen = statBuf.length();
		String gpStat = statBuf.delete(bufLen - 5, bufLen).toString();
		return gpStat;
	}
	
	private static String getFuncStatement(PNode pNode, Keyword[] kwArray, Function[] fnArray, String kwStyle, String fnStyle)
	{
		StringBuffer statBuf = new StringBuffer();
		int fnNum = pNode.getFnNum();
		for(int i = 0; i < fnNum; i++)
		{
			int fnIndex = pNode.getFnIndex(i);
			Function fn = fnArray[fnIndex];
			if(fn.isSimpleFn())
			{
				String[] info = pNode.getSimpleFnInfo(i, kwArray, fnArray);
				if(info[0].equals("attr"))
				{
					statBuf.append("<span class=\"").append(fnStyle).append("\">").append(fn.getFnName()).append("</span> <span class=\"").append(kwStyle).append("\">").append(info[1]).append("</span> and ");
				}
				else
				{
					statBuf.append("<span class=\"").append(fnStyle).append("\">").append(fn.getFnName()).append("</span> and ");
				}
			}
		}
		return statBuf.toString();
	}
	
	private static String getConditionStatement(PNode pNode, Keyword[] kwArray, String kwStyle)
	{
		StringBuffer statBuf = new StringBuffer();
		int condNum = pNode.getConditionNum();
		
		for(int i = 0; i < condNum; i++)
		{
			String[] condInfo = pNode.getConditionInfo(i, kwArray, false);
			String attr = condInfo[0];
			String val = condInfo[1];
			String tagIndex = condInfo[2];
			if(pNode.isMatchKw(attr, kwArray))
			{
				attr = "<span class=\"" + kwStyle + "\">" + attr + "</span>";
			}
			if(pNode.isMatchKw(val, kwArray))
			{
				val = "<span class=\"" + kwStyle + "\">" + val + "</span>";
			}
			if(Integer.parseInt(tagIndex)!=0)
			{
				attr = attr + "_" + tagIndex;
			}
			statBuf.append(attr).append(" matching ").append(val).append(" and ");
		}
		int bufLen = statBuf.length();
		String condStat = statBuf.delete(bufLen - 5, bufLen).toString();
		return condStat;
	}

	private static String traverseTree4Xml(ORMGraph ormgraph, PGraph pgraph, TNode node, boolean isRoot, Keyword[] kwArray, Function[] fnArray, Groupby[] gpArray, Tpredicate[] tpArray)
	{
		StringBuffer varDescBuf = new StringBuffer();
		
		StringBuffer strBuf = new StringBuffer();
		int pNodeId = node.getPNodeId();
		PNode pNode = pgraph.getNode(pNodeId);
		int ormNodeId = pNode.getORMNodeId();
		Node ormNode = ormgraph.getNode(ormNodeId);
		
		strBuf.append("<").append(ormNode.getNodeName());
		if(isRoot)
		{
			strBuf.append(Constant.rootSymbol);
		}
		
		switch(ormNode.getNodeType())
		{
			case Object: 
				strBuf.append(Constant.objectNodeSymbol);
				break;
			case Relationship:
				strBuf.append(Constant.relationshipNodeSymbol);
				break;
			case Mix: default:
				strBuf.append(Constant.mixedNodeSymbol);
				break;
		}
		if(!pNode.isTrival())
		{
//			if(pNode.getFnNum() > 0 || pNode.getGpNum() > 0 || pNode.getGroupbyID())
//			{
//				strBuf.append(Constant.hightlightSymbol);
//			}
			if(pNode.hasAttrOutput(kwArray))
			{
				strBuf.append(" Attr=\"(");
				int outputNum = pNode.getOutputNum();
				for(int i = 0; i < outputNum; i++)
				{
					String[] outputDesc = pNode.getOutput(i, kwArray);
					if(outputDesc[0].equals("attr"))
					{
						strBuf.append(outputDesc[1]).append(",");
					}
				}
				strBuf.delete(strBuf.length() - 1, strBuf.length());
				strBuf.append(")\"");
			}
			if(pNode.getConditionNum() > 0)
			{
				strBuf.append(" Cond=\"").append(getConditionDesc(pNode, kwArray)).append("\"");
			}
			if(pNode.getFnNum() > 0)
			{
				strBuf.append(" Func=\"").append(getFunctionDesc(pNode, kwArray, fnArray)).append("\"");
			}
			if(pNode.getGpNum() > 0 || pNode.getGroupbyID())
			{
				strBuf.append(" Group=\"").append(getGroupByDesc(pNode, kwArray)).append("\"");
			}
			if(pNode.isTemporalNode())
			{
				strBuf.append(" Pred=\"").append(getPredicateDesc(pNode, kwArray, tpArray)).append("\"");
			}
		}
		strBuf.append(">");
		String xmltag = strBuf.toString();
		
		if(!pNode.isTrival())
		{
			xmltag = insertKwFormat(xmltag, pNode, kwArray);
			xmltag = insertFuncFormat(xmltag, pNode, fnArray);
			xmltag = insertPredFormat(xmltag, pNode, tpArray);
		}
		
		varDescBuf.append(xmltag);
		
		int childNum = node.getChildNum();
		if(childNum > 0)
		{
			for(int i = 0; i < childNum; i++)
			{
				TNode child = node.getChild(i);
				varDescBuf.append(traverseTree4Xml(ormgraph, pgraph, child, false, kwArray, fnArray, gpArray, tpArray));
			}
		}
		else
		{
			varDescBuf.append("leaf");
		}
		int stdIndex = xmltag.indexOf("<");
//		int endIndex = xmltag.indexOf(" ") == -1? xmltag.indexOf(">") : xmltag.indexOf(" ");
		int endIndex;
		if(xmltag.indexOf(" ") == -1)
		{
			endIndex = xmltag.indexOf(">");
		}
		else
		{
			endIndex = xmltag.indexOf(" ");
		}
		
		varDescBuf.append("</").append(xmltag.substring(stdIndex + 1, endIndex)).append(">");
		return varDescBuf.toString();
	}
	
	private static String insertPredFormat(String xmltag, PNode pNode, Tpredicate[] tpArray)
	{
		StringBuffer xmltagBuf = new StringBuffer(xmltag);
		if(pNode.isTemporalNode())
		{
			int tpIndex = pNode.getTpIndex(tpArray);
			Tpredicate tp = tpArray[tpIndex];
			String tpName = tp.getTpName();
			int index = -1;
			if((index = getCaseInsensitiveMatch(xmltagBuf.toString(), tpName, 1)) != -1)
			{
				index += tpName.length();
				xmltagBuf.insert(index, Constant.tpFontSymbol);
			}
		}
		return xmltagBuf.toString();
	}
	
	private static String insertFuncFormat(String xmltag, PNode pNode, Function[] fnArray)
	{
		StringBuffer xmltagBuf = new StringBuffer(xmltag);
		int fnNum = pNode.getFnNum();
		for(int i = 0; i < fnNum; i++)
		{
			int fnIndex = pNode.getFnIndex(i);
			Function fn = fnArray[fnIndex];
			String fnName = fn.getFnName();
			int occur = 1; 
			int index = -1;
			while((index = getCaseInsensitiveMatch(xmltagBuf.toString(), fnName, occur++)) != -1)
			{
				index += fnName.length();
				xmltagBuf.insert(index, Constant.fnFontSymbol);
			}
		}
		
		int occur = 1; 
		int index = -1;
		while((index = getCaseInsensitiveMatch(xmltagBuf.toString(), Constant.groupby, occur++)) != -1)
		{
			index += Constant.groupby.length();
			xmltagBuf.insert(index, Constant.fnFontSymbol);
		}
		
		return xmltagBuf.toString();
	}
	
	private static String insertKwFormat(String xmltag, PNode pNode, Keyword[] kwArray)
	{
		StringBuffer xmltagBuf = new StringBuffer(xmltag);
		int kwNum = pNode.getKwNum();
		for(int i = 0; i < kwNum; i++)
		{
			int kwIndex = pNode.getKwIndex(i);
			Keyword kw = kwArray[kwIndex];
			String kwContent = kw.getContent();
			if(kw.isPhrase())
			{
				String[] kwTerms = kwContent.split(" ");
				int termNum = kwTerms.length;
				for(int j = 0; j < termNum; j++)
				{
					int occur = 1; 
					int index = -1;
					while((index = getCaseInsensitiveMatch(xmltagBuf.toString(), kwTerms[j], occur++)) != -1)
					{
						index += kwTerms[j].length();
						xmltagBuf.insert(index, Constant.kwFontSymbol);
					}
				}
			}
			else
			{
				int occur = 1; 
				int index = -1;
				while((index = getCaseInsensitiveMatch(xmltagBuf.toString(), kwContent, occur++)) != -1)
				{
					index += kwContent.length();
					xmltagBuf.insert(index, Constant.kwFontSymbol);
				}
			}
		}
		return xmltagBuf.toString();
	}
	
	private static int getCaseInsensitiveMatch(String desc, String target, int occur)
	{
		String desc2lowercase = desc.toLowerCase();
		String target2lowercase = target.toLowerCase();
		int index = -1;
		for(int i = 0; i < occur; i++)
		{
			index++;
			index = desc2lowercase.indexOf(target2lowercase, index);
		}
		
		return index;
	}

	private static String getConditionDesc(PNode pNode, Keyword[] kwArray)
	{
		StringBuffer descBuf = new StringBuffer();
		int condNum = pNode.getConditionNum();
		
		for(int i = 0; i < condNum; i++)
		{
			String[] condInfo = pNode.getConditionInfo(i, kwArray, false);
			if(Integer.parseInt(condInfo[2])!=0)
			{
				condInfo[0] = condInfo[0]+"_"+condInfo[2];
			}
			descBuf.append(condInfo[0]).append(" = ").append(condInfo[1]).append(" and ");
		}
		
		int bufLen = descBuf.length();
		String condDesc = descBuf.delete(bufLen - 5, bufLen).toString();
		return condDesc;
	}
	
	private static String getFunctionDesc(PNode pNode, Keyword[] kwArray, Function[] fnArray)
	{
		StringBuffer descBuf = new StringBuffer();
		int fnNum = pNode.getFnNum();
		
		for(int i = 0; i < fnNum; i++)
		{
			int fnIndex = pNode.getFnIndex(i);
			Function fn = fnArray[fnIndex];
			if(fn.isSimpleFn())
			{
				String[] info = pNode.getSimpleFnInfo(i, kwArray, fnArray);
				if(info[0].equals("attr"))
				{
					descBuf.append(pNode.getCascadeFnName(i, kwArray, fnArray)).append(" (").append(info[1]).append(") and ");
				}
				else
				{
					descBuf.append(pNode.getCascadeFnName(i, kwArray, fnArray)).append(" (ID) and ");
				}
			}
		}
		
		int bufLen = descBuf.length();
		String fnDesc = descBuf.delete(bufLen - 5, bufLen).toString();
		return fnDesc;
	}
	
	private static String getGroupByDesc(PNode pNode, Keyword[] kwArray)
	{
		StringBuffer descBuf = new StringBuffer();
		if(pNode.getGroupbyID())
		{
			descBuf.append(Constant.groupby).append(" (ID) and ");
		}
		
		int gpNum = pNode.getGpNum();
		for(int i = 0; i < gpNum; i++)
		{
			String[] info = pNode.getGpInfo(i, kwArray);
			if(info[0].equals("attr"))
			{
				descBuf.append(Constant.groupby).append(" (").append(info[1]).append(") and ");
			}
			else
			{
				descBuf.append(Constant.groupby).append(" (ID) and ");
			}
		}
		
		int bufLen = descBuf.length();
		String gpDesc = descBuf.delete(bufLen - 5, bufLen).toString();
		return gpDesc;
	}
	
	private static Object getPredicateDesc(PNode pNode, Keyword[] kwArray, Tpredicate[] tpArray)
	{
		StringBuffer descBuf = new StringBuffer();
		String[] info = pNode.getTpInfo(kwArray, tpArray);
		if(info[2].length() > 8)//longer than a predicate name
		{
			descBuf.append(info[0]).append(" ").append(info[1]).append(" and ").append(info[2]);
		}
		else
		{
			descBuf.append(info[0]).append(" ").append(info[1]).append(" ").append(info[2]);
		}
		return descBuf;
	}
	
	private static TNode graph2Tree(PGraph pgraph)
	{
		int rootId = pgraph.getRootId();
		TNode root = new TNode(rootId);
		insertDescendant(null, root, pgraph);
		return root;
	}

	private static void insertDescendant(TNode parent, TNode tNode, PGraph pgraph)
	{
		int pNodeId = tNode.getPNodeId();
		
		int[] outEdges = pgraph.getOutEdges(pNodeId);
		int outEdgeNum = outEdges.length;
		
		for(int i = 0; i < outEdgeNum; i++)
		{
			int index = outEdges[i];
			if(parent != null && index == parent.getPNodeId())
			{
				continue;
			}
			TNode child = new TNode(index);
			tNode.createChild(child);
			insertDescendant(tNode, child, pgraph);
		}
		
		int[] inEdges = pgraph.getInEdges(pNodeId);
		int inEdgeNum = inEdges.length;
		
		for(int i = 0; i < inEdgeNum; i++)
		{
			int index = inEdges[i];
			if(parent != null && index == parent.getPNodeId())
			{
				continue;
			}
			TNode child = new TNode(index);
			tNode.createChild(child);
			insertDescendant(tNode, child, pgraph);
		}
	}
	
	private static int [] getTpinfo(PNode pNode, Keyword[] kwArray, Node ormNode ,DBinfo dbinfo, StringBuffer fromBuf, StringBuffer whereBuf, int i, boolean Sec)
	{
		int [] tpInfo = new int[2];//0-relId,1-label
		int RelId;
		int compRelIndex;
		int label;
		
		if(pNode.getTpInfer(Sec))//inferred node
		{
			RelId = ormNode.getCoreRelId(dbinfo);//get the relation id
			label = getNodeLabel(i);//get the label
		}
		else//directly annotated node
		{
			String[] condTp = pNode.getTpConditionInfo(kwArray,Sec);//0-RelName,1-AttrName,2-AttrValue
			
			int kwIndex;
			if(!Sec)
			{
				kwIndex= pNode.getTpKw4condition();
			}
			else
			{
				kwIndex= pNode.getTpSecKw4condition();
			}
			
			
			//for different cases
			if(condTp[1]==null||ormNode.isSingleValuedAttr(condTp[1]))//relation name
			{
				RelId = ormNode.getCoreRelId(dbinfo);//get the relation id
				label = getNodeLabel(i);//get the label
			}
			else if(condTp[2]==null)//attribute name
			{
				compRelIndex = ormNode.getCompRel4Attr(condTp[1]);//comp relation index of this node
				RelId = ormNode.getCompRelId(dbinfo,compRelIndex);//get the ormID
				//get the label
				label = pNode.getKwLabel(kwIndex);//this relation already included in the SQL
				if (label==0)
				{
					label = getNodeLabel(i,compRelIndex,1);//mutlti-valued attribute
					fromBuf.append("`").append(ormNode.getCompRelation(compRelIndex).getRelName()).append("` R").append(label).append(", ");
					whereBuf.append(getSQL4Edge(ormNode, compRelIndex, label, getNodeLabel(i), dbinfo)).append(" AND ");
				}
			}
			else//attribute value
			{
				compRelIndex = ormNode.getCompRel4Attr(condTp[1]);//"-1" means not a temporal attribute
				RelId = ormNode.getCompRelId(dbinfo,compRelIndex);//get the ormID
				//get the label
				label = pNode.getKwLabel(kwIndex);//index is the condition num
			}
		}
		
		
		tpInfo[0] = RelId;
		tpInfo[1] = label;
		
		return tpInfo;
	}
	
	
}
