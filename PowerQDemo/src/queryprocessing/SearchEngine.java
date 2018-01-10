package queryprocessing;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

import org.json.simple.JSONArray;

import queryprocessing.Constant.Database;
import queryprocessing.Constant.NodeType;
import queryprocessing.Constant.RelType;
import queryprocessing.Constant.TYPE;

public class SearchEngine
{
	private SQLBean sqlbean;
	private DBinfo dbinfo;
	private DBinfo viewinfo;
	private ORMGraph dbOrmgraph;
	private ORMGraph viewOrmgraph;
	private boolean useView;
	private Database DBtype;
	
	private JSONArray db;
	private JSONArray er;
	
	public SearchEngine(Database dbchoice)
	{
		this.parseDBinfo(dbchoice);
		this.dbOrmgraph = new ORMGraph(this.dbinfo);
		this.useView = false;
		this.DBtype = dbchoice;
	}
	public Database getDBtype()
	{
		return DBtype;
	}
	
	public void updateDBinfo(JSONArray er)
	{
		Viewinfo info = new Viewinfo(er, this.dbinfo);
		AuxGraph[] graphList = this.genSchemaGraph(info.getDBRel());
//		String[] sql = Translator.genViewSQL(info, graphList, this.dbinfo);
		String[] sql = Translator.genTableSQL(info, graphList, this.dbinfo);
		this.updateQueries(sql);
		this.viewinfo = new DBinfo(info.getView(), info.getAtt(), info.getOutputAtt(), info.getVerifyAtt(), info.getTextAtt(), info.getKey(), info.getType(), info.getFK(), info.getDesc(), true, info.getTempType(), info.getTempDetail());
		//		this.viewinfo.setRenark(sql);
		this.viewOrmgraph = new ORMGraph(this.viewinfo);
		this.useView = true;
	}
	
	private AuxGraph[] genSchemaGraph(String[][] rel)
	{
		AuxGraph[] graphList = new AuxGraph[rel.length];
		for(int i = 0; i < rel.length; i++)
		{
			HashSet<Integer> set = new HashSet<Integer>();
			for(int j = 0; j < rel[i].length; j++)
			{
				int node = this.dbinfo.getRelId(rel[i][j]);
				set.add(node);
			}
			LinkedList<Integer> nodeList = new LinkedList<Integer>(set);
			AuxGraph graph = this.findSubgraphInDB(nodeList);
			if(graph == null)
			{
				graph = new AuxGraph();
				for(int node : nodeList)
				{
					graph.createNode(node);
				}
				
			}
			graphList[i] = graph;
		}
		return graphList;
	}
	
	private AuxGraph findSubgraphInDB(LinkedList<Integer> leafNodeList)
	{
		LinkedList<AuxGraph> queue = new LinkedList<AuxGraph>();
		int firstLeaf = leafNodeList.getFirst();
		AuxGraph firstGraph = new AuxGraph();
		firstGraph.createNode(firstLeaf);
		queue.add(firstGraph);
		
		while(!queue.isEmpty())
		{
			AuxGraph graph = queue.pollFirst();
			if(isSubGraph(graph, leafNodeList))
			{
				return graph;
			}
			else
			{
				int nodeNum = graph.getNodeNum();
				if(nodeNum < Constant.maxNodeNum)
				{
					for(int i = 0; i < nodeNum; i++)
					{
						int relId = graph.getNode(i);
						Relation rel = this.dbinfo.getRel(relId);
						Relation[] refRel = rel.getRefRel();
						Relation[] refedRel = rel.getRefedRel();
						
						for(int j = 0; j < refRel.length; j++)
						{
							int neighborNode = this.dbinfo.getRelId(refRel[j]);
							if(!graph.containNode(neighborNode))
							{
								AuxGraph newgraph = graph.getCopy();
								newgraph.createNode(neighborNode);
								newgraph.createEdge(i, newgraph.getNodeNum() - 1);
								queue.add(newgraph);
							}
						}
						
						for(int j = 0; j < refedRel.length; j++)
						{
							int neighborNode = this.dbinfo.getRelId(refedRel[j]);
							if(!graph.containNode(neighborNode))
							{
								AuxGraph newgraph = graph.getCopy();
								newgraph.createNode(neighborNode);
								newgraph.createEdge(newgraph.getNodeNum() - 1, i);
								queue.add(newgraph);
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	public void setJSONDB(JSONArray db)
	{
		this.db = db;
	}
	
	public void setJSONER(JSONArray er)
	{
		this.er = er;
	}
	
	public JSONArray getJSONDB()
	{
		return this.db;
	}
	
	public JSONArray getJSONER()
	{
		return this.er;
	}

	public void setSQLBean(SQLBean sqlbean)
	{
		this.sqlbean = sqlbean;
	}

	private void parseDBinfo(Database dbchoice)
	{
		switch(dbchoice)
		{
			case Employee:
				this.dbinfo = new DBinfo(Constant.employee, Constant.employee_attr, Constant.employee_attr4output, Constant.employee_attr4verify, Constant.employee_text, Constant.employee_key, Constant.employee_type, Constant.employee_fk, Constant.employee_desc, false, Constant.employee_temporalType, Constant.employee_temporalDetail);
				break;
			case IMDB:
				this.dbinfo = new DBinfo(Constant.imdb, Constant.imdb_attr, Constant.imdb_attr4output, Constant.imdb_attr4verify, Constant.imdb_text, Constant.imdb_key, Constant.imdb_type, Constant.imdb_fk, Constant.imdb_desc, false, Constant.imdb_temporalType, Constant.imdb_temporalDetail);
				break;
			case ACMDL:
				this.dbinfo = new DBinfo(Constant.acmdl, Constant.acmdl_attr, Constant.acmdl_attr4output, Constant.acmdl_attr4verify, Constant.acmdl_text, Constant.acmdl_key, Constant.acmdl_type, Constant.acmdl_fk, Constant.acmdl_desc, false, Constant.acmdl_temporalType, Constant.acmdl_temporalDetail);
				break;
			default:
				break;
		}
	}

	
	public void updateQueries(String[] sql)
	{
		this.sqlbean.updateQueries(sql);
	}
	
	public ResultSet executeQuery(String sql)
	{
		return this.sqlbean.executeQueryScroll(sql);
	}
	
	private int getNumOfRows(ResultSet rs) throws SQLException
	{
		int currentRow = rs.getRow(); 
		int counter = rs.last() ? rs.getRow() : 0; 
		if (currentRow == 0) 
		{
			rs.beforeFirst();
		}
		else
		{
			rs.absolute(currentRow);
		}
		return counter; 
	}
	
	public boolean useView()
	{
		return this.useView;
	}
	
	public void clearView()
	{
		this.useView = false;
		this.viewinfo = null;
		this.viewOrmgraph = null;
	}
	
	public ORMGraph getORMGraph()
    {
		if(this.useView())
		{
			return this.viewOrmgraph;
		}
		else
		{
			return this.dbOrmgraph;
		}
    	
    }
    
    public DBinfo getDBinfo()
    {
    	if(this.useView())
    	{
    		return this.viewinfo;
    	}
    	else
    	{
    		return this.dbinfo;
    	}
    }
    
    public QueryUnit parseQuery(String query) throws SQLException
    {
    	query = query.replaceAll("\\s+"," ").trim() + " ";
    	int len = query.length();
    	boolean isphrase = false;
		int index = 0;
		int cursor = 0;
		String content = null;
		ArrayList<Keyword> kwList = new ArrayList<Keyword>();
		ArrayList<Function> fnList = new ArrayList<Function>();
		ArrayList<Groupby> gpList = new ArrayList<Groupby>();
		ArrayList<Tpredicate> tpList = new ArrayList<Tpredicate>();//TP
		ArrayList<InvalidQuery> iqList = new ArrayList<InvalidQuery>();//error information 
		
		String preWord = "";
		Constant.TYPE preType = Constant.TYPE.NULL;
		Constant.TYPE kwType = Constant.TYPE.NULL;
		String prePD = "";
		boolean validQuery;
		
		Function lastFn = null;
		Groupby lastGp = null;
		Tpredicate lastTp = null;
		
		String period = "";
		
		while(cursor < len)
		{
			if(query.charAt(cursor) == ' ' && !isphrase)
			{
				content = query.substring(index, cursor);//get one word
				int fnIndex = Constant.isFunction(content);
				int tpIndex = Constant.isTpredicate(content);
				if(fnIndex != -1)//aggregate function
				{
					kwType = Constant.TYPE.FN;
					Function fn = new Function(Constant.function[fnIndex]);
					fnList.add(fn);
					int fnNum = fnList.size();
					validQuery = queryValidation(preType, kwType);

					if(validQuery)
					{
						switch (preType)
						{
						case FN:
							lastFn.setFnIndex(fnNum - 1);//correct
							break;
						default:
							break;//kw,pd
						}
					}
					else//gather the error info
					{
						iqList.add(new InvalidQuery(preType,kwType,preWord,content,null));
					}
					lastFn = fn;
					preWord = content;
					preType = kwType;
				}
				else if (Constant.isGroupBy(content))//group by
				{
					kwType = Constant.TYPE.GP;
					Groupby gp = new Groupby();
					gpList.add(gp);
					validQuery = queryValidation(preType, kwType);
					
					if(!validQuery)
					{
						iqList.add(new InvalidQuery(preType,kwType,preWord,content,null));
					}
//					switch (preType)
//					{
//					case FN:
//						lastFn.setInvalidWord(content);//error
//						break;
//					case TP:
//						lastTp.setInvalidWord(content);//error
//						break;
//					case GP:
//						lastGp.setInvalidWord(content);//error
//						break;
//					default:
//						break;//kw,pd:correct
//					}
					lastGp = gp;
					preWord = content;
					preType = kwType;
				}
				else if(tpIndex !=-1)//is TP
				{
					kwType = Constant.TYPE.TP;
					Tpredicate tp = new Tpredicate(Constant.Tpredicate[tpIndex]);
					validQuery = queryValidation(preType, kwType);
					tp.setKwIndex(-1);
					int kwNum = kwList.size();
//					if (preWord.length()== 0)//no previous word
//					{
//						tp.setKwIndex(-1);
//						tp.setInvalidWord("Null");
//					}
//					else//have previous word
//					{
					if(validQuery)
					{
						switch (preType)
						{
						case KW:
							tp.setKwIndex(kwNum-1);
							break;
						case PD://should be valid
							tp.setPeriodIndex(0);//period is the first operand
							tp.extractPeriod(preWord);
							break;
						default:
							break;
						}
					}
					else
					{
						iqList.add(new InvalidQuery(preType,kwType,preWord,content,null));
					}
						
//						switch (preType)
//						{
//						case KW:
//							tp.setKwIndex(kwNum-1);
//							break;
//						case FN://error
//							lastFn.setInvalidWord(content);
//							tp.setInvalidWord(preWord);
//							break;
//						case GP://error
//							lastGp.setInvalidWord(content);
//							tp.setInvalidWord(preWord);
//							break;
//						case TP://error
//							lastTp.setInvalidWord(content);  
//							tp.setInvalidWord(preWord);
//							break;
//						case PD://should be valid
//							tp.setPeriodIndex(0);//period is the first operand
//							//??
//							break;
//						default:
//							break;
//						}
//					}
					tp.setTpIndex(Constant.isTpredicate(content));
					tpList.add(tp);
					lastTp = tp;
					preWord = content;
					preType = kwType;
				}
				else if(content.charAt(0) == '[')//PD
				{
					kwType = Constant.TYPE.PD;
					validQuery = queryValidation(preType, kwType);
					if(validQuery && preType == TYPE.TP)
					{
						if(lastTp.isPeriodExists())
						{
							iqList.add(new InvalidQuery(null,kwType,null,content,"The temporal predicate cannot have two time period operands."));
						}
						else
						{
							lastTp.setPeriodIndex(1);//period is the second operand
							boolean validPeriod = lastTp.extractPeriod(content);
							if(!validPeriod)
							{
								iqList.add(new InvalidQuery(null,kwType,null,content,"Invalid time period."));
							}
						}
					}
					else if(!validQuery)
					{
						iqList.add(new InvalidQuery(preType,kwType,preWord,content,null));
					}
					
//					if (preType == TYPE.TP)
//					{
//						lastTp.extractPeriod(content);
//					}
//					else if (preWord.length()==0)
//					{
//						prePD = "NULL";
//					}
//					else //invalid previous word
//					{
//						switch (preType){
//						case KW:
//							prePD = preWord;
//							break;
//						case FN:
//							lastFn.setInvalidWord(content);
//							prePD = preWord;
//							break;
//						case GP:
//							lastGp.setInvalidWord(content);
//							prePD = preWord;
//							break;
//						case PD:
//							prePD = "time period";
//							break;
//						default:
//							break;
//						}
//							
//					}
					preWord = content;
					preType = kwType;
				}
				else //kw
				{
					kwType = Constant.TYPE.KW;
					validQuery = queryValidation(preType, kwType);
					Keyword kw = new Keyword(content, false);
					kw.createTag(this.getDBinfo(), sqlbean);
					kwList.add(kw);
					int kwNum = kwList.size();
					if(validQuery)
					{
						switch (preType)
						{
						case FN:
							lastFn.setKwIndex(kwNum - 1);
							break;
						case GP:
							lastGp.setKwIndex(kwNum - 1);
							break;
						case TP:
							lastTp.setKwIndexSec(kwNum-1);
							break;
						default:
							break;
						}
					}
					else
					{
						iqList.add(new InvalidQuery(preType,kwType,preWord,content,null));
					}
//					switch (preType)
//					{
//					case FN:
//						lastFn.setKwIndex(kwNum - 1);
//						break;
//					case GP:
//						lastGp.setKwIndex(kwNum - 1);
//						break;
//					case TP:
//						lastTp.setKwIndexSec(kwNum-1);
//						break;
//					default:
//						break;
//					}
					preWord = content;
					preType = kwType;
				}
				index = cursor + 1;
			}
			else if(query.charAt(cursor) == '\"' && !isphrase)// the beginning of a phrase
			{
				isphrase = true;
				index = cursor + 1;
			}
			else if(query.charAt(cursor) == '\"' && isphrase) //the end of a phrase
			{
				kwType = Constant.TYPE.KW;
				validQuery = queryValidation(preType, kwType);
				content = query.substring(index, cursor);
				Keyword kw = new Keyword(content, true);//create a kw
				kw.createTag(this.getDBinfo(), sqlbean);//get the tag of this kw
				kwList.add(kw);
				int kwNum = kwList.size();
				if(validQuery)
				{
					switch (preType)
					{
					case FN:
						lastFn.setKwIndex(kwNum - 1);
						break;
					case GP:
						lastGp.setKwIndex(kwNum - 1);
						break;
					case TP:
						lastTp.setKwIndexSec(kwNum-1);
						break;
					default:
						break;
					}
				}
				else
				{
					iqList.add(new InvalidQuery(preType,kwType,preWord,content,null));
				}
				preWord = content;
				preType = kwType;
				
				cursor++;
				index = cursor + 1;
				isphrase = false;
			}
			cursor++;
		}
		
		validQuery = queryValidation(preType, Constant.TYPE.NULL);
		if(!validQuery)
		{
			iqList.add(new InvalidQuery(null,kwType,null,content,"This word cannot be the last word in query."));
		}
		
		
		
		Keyword[] kwArray = kwList.toArray(new Keyword[kwList.size()]);
		Function[] fnArray = fnList.toArray(new Function[fnList.size()]);
		Groupby[] gpArray = gpList.toArray(new Groupby[gpList.size()]);
		Tpredicate[] tpArray = tpList.toArray(new Tpredicate[tpList.size()]);
		InvalidQuery[] iqArray = iqList.toArray(new InvalidQuery[iqList.size()]);
		
		return new QueryUnit(kwArray, fnArray, gpArray,tpArray,iqArray,prePD);
    }
    
    public boolean queryValidation(Constant.TYPE preType, Constant.TYPE kwType)
    {
    	int preIndex = preType.ordinal();
    	int kwIndex = kwType.ordinal();
    	return Constant.KW_TYPE_validation[kwIndex][preIndex];
    }

    public void updateKwTag(Keyword[] kwArray, Function[] fnArray, Groupby[] gpArray)
    {
    	int fnNum = fnArray.length;
    	for(int i = 0; i < fnNum; i++)
    	{
    		Function fn = fnArray[i];
    		String fnName = fn.getFnName();
    		if(fn.isSimpleFn())
    		{
    			Keyword kw = kwArray[fn.getKwIndex()];
    			if(fnName.equals(Constant.function[Constant.countFnIndex]))
    			{
    				kw.updateKwTag4NameAttr();
    			}
    			else
    			{
    				kw.updateKwTag4OnlyAttr();
    			}
    		}
    	}
    	
    	int gpNum = gpArray.length;
    	for(int i = 0; i < gpNum; i++)
    	{
    		int kwIndex = gpArray[i].getKwIndex();
    		Keyword kw = kwArray[kwIndex];
    		kw.updateKwTag4NameAttr();
    	}
    }

	public PGraph[] interpretQuery(Keyword[] kwArray, String[][] userChoice, Function[] fnArray, Groupby[] gpArray,  Tpredicate[] tpArray) throws SQLException
	{
 		int kwNum = kwArray.length;
    	for(int i = 0; i < kwNum; i++)//for each kw, set the user choice
    	{
    		Keyword kw = kwArray[i];
    		String[] choice = userChoice[i];
    		kw.updateKwTag(choice);//now the tag list only contain the user choice
    	}
    	
    	int combNum = this.getCombinationNum(kwArray);//get the combination num
    	PriorityQueue<PGraph> pgraphQueue = new PriorityQueue<PGraph>();
    	
    	for(int i = 0; i < combNum; i++)
		{
			int[] combTag = getTagCombination(i, kwArray);
			int[][] tagGrouping = getTagGrouping(combTag, kwArray);
			PriorityQueue<PGraph> morePGraph = buildQueryPatternList(combTag, tagGrouping, kwArray, fnArray, gpArray, tpArray);
			pgraphQueue.addAll(morePGraph);
		}
    	
    	int queueLen = pgraphQueue.size();
    	PGraph[] queryPatternList = new PGraph[queueLen];
    	for(int i = 0; i < queueLen; i++)
    	{
    		PGraph queryPattern = pgraphQueue.poll();
    		queryPatternList[i] = queryPattern;
    	}
    	return queryPatternList;
	}
	
	private PriorityQueue<PGraph> buildQueryPatternList(int[] combTag, int[][] tagGrouping, Keyword[] kwArray, Function[] fnArray, Groupby[] gpArray, Tpredicate[] tpArray) throws SQLException
	{
		PriorityQueue<PGraph> queryPatternList = new PriorityQueue<PGraph>();
		ArrayList<Pattern> TempPatternList = new ArrayList<Pattern>();//temporal pattern list
		int groupNum = tagGrouping.length;
		PNode[] nonTrivialNode = new PNode[groupNum];
		Pattern[][] ptnList = new Pattern[groupNum][];
//		Pattern[][] tpPtnList = new Pattern[groupNum][];
		for(int i = 0; i < groupNum; i++)
		{
			int[] kwGroup = tagGrouping[i];
			ptnList[i] = genPatternList(combTag, kwGroup, kwArray, fnArray, gpArray, tpArray);
			int kwIndex = kwGroup[0];
			Keyword kwInGroup = kwArray[kwIndex];
			String tagName = kwInGroup.getTagName(combTag[kwIndex]);
			int ormNodeId = this.getORMGraph().getNodeId(tagName);
			nonTrivialNode[i] = new PNode(ormNodeId, null);
			
			if (tpArray.length>0)
			{
				int tpNum = tpArray.length;
				for (int j = 0; j < tpNum; j++)
				{
					//Pattern for temporal predicate
					Pattern TempPtn = new Pattern(combTag, kwGroup, kwArray, tpArray[j], j);
					if (TempPtn.isTpPattern())
					{
						TempPatternList.add(TempPtn);
					}
				}
			}
		}
		
		int combNum = this.getCombinationNum(ptnList);
		for(int i = 0; i < combNum; i++)
		{
			Pattern[] combPattern = this.getPatternCombination(i, ptnList);
			for(int j = 0; j < groupNum; j++)
			{
				nonTrivialNode[j].setPattern(combPattern[j]);
			}
			queryPatternList.addAll(this.genPatternGraph(nonTrivialNode, TempPatternList, kwArray, tpArray));
		}
		
//		return this.genPatternGraph(nonTrivialNode);
		return queryPatternList;
	}
	
	
	private Pattern[] genPatternList(int[] combTag, int[] kwGroup, Keyword[] kwArray, Function[] fnArray, Groupby[] gpArray ,Tpredicate[] tpArray) throws SQLException
	{
		int kwIndex = kwGroup[0];
		Keyword kwInGroup = kwArray[kwIndex];
		String tagName = kwInGroup.getTagName(combTag[kwIndex]);
		int ormNodeId = this.getORMGraph().getNodeId(tagName);
		
		ArrayList<Pattern> patternList = new ArrayList<Pattern>();
		Pattern ptn = new Pattern(combTag, kwGroup, kwArray, fnArray, gpArray, this.getORMGraph());
		patternList.add(ptn);
		if(fnArray.length > 0)
		{
			if(ptn.getKw4ConditionNum() > 0 && ptn.getGp4ConditionNum() == 0 && ptn.getFn4ConditionNum() == 0)
			{
				PGraph graph = new PGraph();
				graph.createNode(new PNode(ormNodeId, ptn));
				SQLUnit sqlunit = Translator.getSQLUnit(graph, this.getORMGraph(), kwArray, fnArray, gpArray, tpArray, dbinfo);
				ResultSet rs = this.executeQuery(sqlunit.getSQL());
				int rsNum = getNumOfRows(rs);
				if(rsNum > 1)
				{
					patternList.add(ptn.augmentPattern());
				}
			}
		}
		Pattern[] patternArray = patternList.toArray(new Pattern[patternList.size()]);
		return patternArray;
	}
	
	
	private Pattern[] getPatternCombination(int index, Pattern[][] ptnList)
	{
		int listNum = ptnList.length;
		
		Pattern[] combPtn = new Pattern[listNum];
		for(int i = 0; i < listNum; i++)
		{
			int ptnNum = ptnList[i].length;
			int j = index % ptnNum;
			combPtn[i] = ptnList[i][j];
			index = index / ptnNum;
		}
		return combPtn;
	}
	
	private int getCombinationNum(Pattern[][] ptnList)
	{
		int combNum = 1;
		int listNum = ptnList.length;
		for(int i = 0; i < listNum; i++)
		{
			combNum = combNum * ptnList[i].length;
		}
		
		return combNum;
	}
	

	private PriorityQueue<PGraph> genPatternGraph(PNode[] nonTrivialNode, ArrayList<Pattern> TempPatternList, Keyword[] kwArray, Tpredicate[] tpArray)
	{
		PriorityQueue<PGraph> queryPatternList = new PriorityQueue<PGraph>();
		ArrayList<PGraph> pGraphList = new ArrayList<PGraph>();//TP
		ORMGraph ormgraph = this.getORMGraph();
		
		int nonTrivialNodeNum = nonTrivialNode.length;
		if(nonTrivialNodeNum == 1)//original code do not have this part
		{
			PNode singleNode = nonTrivialNode[0];
			PGraph pgraph = new PGraph();
			pgraph.createNode(singleNode.getCopy());
			pGraphList =  annotateTemporalNodes(pgraph,TempPatternList, kwArray, ormgraph,dbinfo);
			for(PGraph pg:pGraphList)
			{
				pg.computeScore(ormgraph);
			}
			queryPatternList.addAll(pGraphList);// add this pgraph into the final patternList
//			pgraph.computeScore(this.getORMGraph());
//			queryPatternList.add(pgraph);
		}
		else
		{
			HashMap<Integer, ArrayList<PNode>> clusterList = clusterNonTrivialNode(nonTrivialNode);
			int clusterNum = clusterList.size();
			
			if(nonTrivialNodeNum == clusterNum)
			{
				LinkedList<Integer> nodeInORM = new LinkedList<Integer>(clusterList.keySet());
				AuxGraph subgraphInORM = findSubgraphInORM(nodeInORM);
				
				if(subgraphInORM != null)
				{
					PGraph pgraph = new PGraph();
					int nodeNum = subgraphInORM.getNodeNum();
					for(int i = 0; i < nodeNum; i++)
					{
						int ormNodeId = subgraphInORM.getNode(i);
						PNode pnode;
						if(clusterList.containsKey(ormNodeId))
						{
							pnode = clusterList.get(ormNodeId).get(0).getCopy();
						}
						else
						{
							pnode = new PNode(ormNodeId, null);
						}
						pgraph.createNode(pnode);
					}
					pgraph.createEdge(subgraphInORM);
					pGraphList =  annotateTemporalNodes(pgraph,TempPatternList, kwArray, ormgraph,dbinfo);
					for(PGraph pg:pGraphList)
					{
						pg.computeScore(ormgraph);
					}
					queryPatternList.addAll(pGraphList);// add this pgraph into the final patternList
//					pgraph.computeScore(this.getORMGraph());
//					queryPatternList.add(pgraph);
				}
			}
			else
			{
				int rootId = getRoot(clusterList);
				
				if(rootId != -1)
				{
					PGraph pgraph = buildPalindromePath(rootId, false, clusterList);
					if(pgraph != null)
					{
//						pgraph.computeScore(this.getORMGraph());
//						queryPatternList.add(pgraph);
						pGraphList =  annotateTemporalNodes(pgraph,TempPatternList, kwArray, ormgraph,dbinfo);
						for(PGraph pg:pGraphList)
						{
							pg.computeScore(ormgraph);
						}
						queryPatternList.addAll(pGraphList);
						
					}
				}
				else
				{
					int ormNodeNum = this.getORMGraph().getNodeNum();
					for(int i = 0; i < ormNodeNum; i++)
					{
						Node ormNode = this.getORMGraph().getNode(i);
						if(!(ormNode.getNodeType() == NodeType.Relationship || clusterList.containsKey(i)))
						{
							PGraph pgraph = buildPalindromePath(i, true, clusterList);
							if(pgraph != null)
							{
//								pgraph.computeScore(this.getORMGraph());
//								queryPatternList.add(pgraph);
								
								pGraphList =  annotateTemporalNodes(pgraph,TempPatternList, kwArray, ormgraph,dbinfo);
								for(PGraph pg:pGraphList)
								{
									pg.computeScore(ormgraph);
								}
								queryPatternList.addAll(pGraphList);
							}
						}
					}
				}
			}
		}
		return queryPatternList;
	}
	
//	private  ArrayList<PGraph> annotateTemporalNodes_old(PGraph pgraph, ArrayList<Pattern> TempPatternList, Keyword[] kwArray,  Tpredicate[] tpArray)//need to complete the functions
//	{
//		ORMGraph ormgraph =  this.getORMGraph();
////		PGraph newpgraph;
//		ArrayList<PGraph> pGraphList = new ArrayList<PGraph>();//the annotated graph.
//		pGraphList.add(pgraph);
//		if (TempPatternList.isEmpty())
//		{
//			return pGraphList;
//		}
//		
//		int tpLen = TempPatternList.size();
//		for (int i=0; i<tpLen; i++)
//		{
//			Pattern tpPattern = TempPatternList.get(i);
//			int tpIndex = tpPattern.getTpIndex();//tp index in the tpArray
//			int graphNum = pGraphList.size();
//			ArrayList<PGraph> newList = new ArrayList<PGraph>();//the annotated graph.
//			for (int k=0; k<graphNum; k++)
//			{
//				PGraph graph = pGraphList.get(k);
//				int pNodeNum = graph.getNodeNum();
//				if (tpArray[tpIndex].isTwoKw())//
//				{
//					for (int j=0; j<pNodeNum; j++)//for each node
//					{
//						PNode pnode = graph.getNode(j);
//						if (pnode.getKwgroup()==null)
//						{
//							continue;
//						}
//						if(Arrays.equals(pnode.getKwgroup(), tpPattern.getKwgroup()))//temporal predicate should be annotated to this node, pattern.kwgroup()
//						{
//							if(pnode.isTemporalNode(dbinfo,ormgraph))//this node is a temporal node
//							{
//								String relName = tpPattern.getDirectTpRelName(kwArray);
//								pnode.setPatternTp(tpPattern,relName);
//								
//								int ormNodeId = pnode.getORMNodeId();
//								Node ormNode =  ormgraph.getNode(ormNodeId);
//								int compRelIndex = ormNode.getCompRel4Name(relName);
//								Relation[] tempRelList= new Relation[1];
//								tempRelList[0] = ormNode.getCompRel(compRelIndex);
//								
//								int[][] pAttrList = tempRelList[0].getTempAttrList();
//								pnode.setTpAttr(pAttrList[0],dbinfo);
//								newList.add(graph);
//								break;
//							}
//							else
//							{
//								System.out.println("Syntax Error, the keyword before TP should indicate a temporal relation!\n");//there are no kw before the Tp.
//							}
//						}
//					}
//				}
//				else //the tp have one obj kw
//				{
//					for (int j=0; j<pNodeNum; j++)
//					{
//						PNode pnode = graph.getNode(j);
//						int ormNodeId = pnode.getORMNodeId();
//						Node ormNode = ormgraph.getNode(ormNodeId);
//						if (pnode.getKwgroup()==null)
//						{
//							continue;
//						}
//						
//						if(Arrays.equals(pnode.getKwgroup(), tpPattern.getKwgroup()))//temporal predicate should be annotated to this node, pattern.kwgroup()
//						{
//							if(pnode.isTemporalNode(dbinfo,ormgraph))//this node is a temporal node
//							{
//								annotateTemporalPattern(pnode,j,graph,TempPatternList.get(i),newList, kwArray);
//							}
//							else
//							{
//								findTemporalNode(graph,j ,tpPattern, newList,tpIndex);//find the nearest temporal node
//								break;
//							}
//							
//						}
//					}
//				}
//			}
//			
//			pGraphList = newList;
//		}
//		return pGraphList;//this list is empty
//	}
	
	private static ArrayList<PGraph> annotateTemporalNodes(PGraph pgraph, ArrayList<Pattern> TempPatternList, Keyword[] kwArray,ORMGraph ormgraph,DBinfo dbinfo)
	{
		int pNodeNum = pgraph.getNodeNum();
//		PGraph newpgraph;
		ArrayList<PGraph> pGraphList = new ArrayList<PGraph>();
		ArrayList<PGraph> pGraphListTemp = new ArrayList<PGraph>();//save the pGraph annotated by tp first kw.
		if (TempPatternList.isEmpty())
		{
			pGraphList.add(pgraph);
			return pGraphList;
		}
		
		int tpLen = TempPatternList.size();
		for (int i=0; i<tpLen; i++)
		{
			Pattern tpPattern = TempPatternList.get(i);
//			int tpIndex = tpPattern.getTpIndex();//tp index in the tpArray
			int id=0;
			id++;
			//the tp have two obj kw
			if (tpPattern.isTpSequence())//
			{
				for (int j=0; j<pNodeNum; j++)//for each node
				{
					PNode pnode = pgraph.getNode(j);
					if (pnode.getKwgroup()==null)
					{
						continue;
					}
					
					//annotate the first kw, the annotated graphs are added to "pGraphListTemp"
					if(containInt(pnode.getKwgroup(),tpPattern.getTpFirstKw()))
					{
						if(pnode.isTemporalNode(dbinfo,ormgraph))
						{
							//get the target relation name
							String relName = tpPattern.getDirectTpFirstRelName(kwArray);
							
							//get the tp attribute <start,end>
							int ormNodeId = pnode.getORMNodeId();//get the ORM node id
							int[][] pAttrList = ormgraph.getTpAttr(ormNodeId,relName);
							
							//annotate the relName and <s,e>
							pnode.setPatternTp(tpPattern,relName,pAttrList[0], id, false, false,dbinfo);//Sec-false, infer-false
							pGraphListTemp.add(pgraph);
							break;
//							annotate++;//annotate one kw
						}
						else//find the target node
						{
							findTpSequenceNode(pgraph,j ,tpPattern, pGraphListTemp,ormgraph,dbinfo,tpPattern.getTpIndex(),id,false);//find the nearest temporal node
							break;
						}
					}
					
				}
				//each graph in "pGraphListTemp", annotated the second kw
				int pGLen = pGraphListTemp.size();
				for(int k = 0; k<pGLen; k++)
				{
					PGraph pgraphSec = pGraphListTemp.get(k);
					for (int j=0; j<pNodeNum; j++)//for each node
					{
						PNode pnode = pgraphSec.getNode(j);
						if (pnode.getKwgroup()==null)
						{
							continue;
						}
						
						//annotate the second kw
						if(containInt(pnode.getKwgroup(),tpPattern.getTpSecKw()))
						{
							if(pnode.isTemporalNode(dbinfo,ormgraph))
							{
								//get the target relation name
								String relName = tpPattern.getDirectTpFirstRelName(kwArray);
								
								//get the tp attribute <start,end>
								int ormNodeId = pnode.getORMNodeId();//get the ORM node id
								int[][] pAttrList = ormgraph.getTpAttr(ormNodeId,relName);
								
								//annotate the relName and <s,e>
								pnode.setPatternTp(tpPattern,relName,pAttrList[0], id, true, false, dbinfo);//Sec-true, infer-false
								pGraphList.add(pgraphSec);
								break;
							}
							else//find the node
							{
								findTpSequenceNode(pgraphSec,j ,tpPattern, pGraphList,ormgraph,dbinfo,tpPattern.getTpIndex(),id,true);//Sec-true
								break;
							}
						}
					}
				}
			}
			else //the tp have one obj kw
			{
				for (int j=0; j<pNodeNum; j++)//find the target node
				{
					PNode pnode = pgraph.getNode(j);
					if (pnode.getKwgroup()==null)
					{
						continue;
					}
					
					int ormNodeId = pnode.getORMNodeId();
					Node ormNode = ormgraph.getNode(ormNodeId);
					
					
					if(containInt(pnode.getKwgroup(), tpPattern.getTpKwIndex()))//temporal predicate should be annotated to this node, pattern.kwgroup()
					{
						if(pnode.isTemporalNode(dbinfo,ormgraph))//this node is a temporal node
						{
							annotateTemporalPattern(pnode,j,pgraph,TempPatternList.get(i),pGraphList,kwArray,ormgraph,dbinfo);
						}
						else
						{
							findTemporalNode(pgraph,j ,tpPattern, pGraphList,tpPattern.getTpIndex(),ormgraph, dbinfo);//find the nearest temporal node
							break;
						}
						
					}
				}
			}
			
		}
		return pGraphList;//this list is empty
	}
	
	//find the nearest temporal node - for temporal sequence	
		private static void findTpSequenceNode(PGraph pgraph, int index, Pattern pattern, ArrayList<PGraph> pGraphList, ORMGraph ormgraph, DBinfo dbinfo , int tpIndex,int id, boolean Sec)
		{
			int pnodeNum = pgraph.getNodeNum();
			int[] visit = new int[pnodeNum];
			int[] nodeLevel = new int[pnodeNum];
			Queue<Integer> visitQueue = new LinkedList<Integer>();
			int tNodeLevel=32;//there are at most 31 levels
			
			visitQueue.offer(index);
			while(!visitQueue.isEmpty())
			{
				int currentNode = visitQueue.poll();
				if(nodeLevel[currentNode]>tNodeLevel)break;
				
				if(visit[currentNode]==0)//unvisited
				{
					visit[currentNode] = 1;
					
					if (pgraph.getNode(currentNode).isTemporalNode(dbinfo,ormgraph))//
					{
						tNodeLevel = nodeLevel[currentNode];
						findAnnotateTpSequencePattern(pgraph.getNode(currentNode),currentNode,pgraph,pattern,pGraphList, ormgraph, id,Sec, true, dbinfo);
					}
					//getAdjNode
					int[] adjInNode = pgraph.getInEdges(currentNode);
					int[] adjOutNode = pgraph.getOutEdges(currentNode);
					int inLen = adjInNode.length;
					int outLen = adjOutNode.length;
					int []adjNode = new int[inLen+outLen];
					System.arraycopy(adjInNode,0,adjNode,0,inLen);
					System.arraycopy(adjOutNode,0,adjNode,inLen,outLen);
					for(int nextNode:adjNode)
					{
						if(visit[nextNode]==0)//0 is not visited.
						{
							visitQueue.offer(nextNode);
							nodeLevel[nextNode] = nodeLevel[currentNode]+1;
						}
					}
				}
			}
		}
	
	
	private static void findAnnotateTpSequencePattern(PNode pnode,int nodeIndex, PGraph pgraph, Pattern pattern, ArrayList<PGraph> pGraphList, ORMGraph ormgraph,int id, boolean Sec,boolean infer, DBinfo dbinfo)
	{
		Relation[] tempRelList;

		tempRelList = pnode.getTempRelList(ormgraph);//attributes coulbe be annotated in this node
		
		int tempNodeNum = tempRelList.length;
		//for each temporal attribute, copy the graph, annotate, and add to the graph list
		for (int n=0; n<tempNodeNum; n++)//for each temporal attribute
		{
			int[][] pAttrList = tempRelList[n].getTempAttrList();
			int pAttrListLen = pAttrList.length;
			for (int k = 0; k<pAttrListLen; k++)
			{
				PGraph newpgraph2 = pgraph.getDeepCopy();
				newpgraph2.getNode(nodeIndex).setPatternTp(pattern,tempRelList[n].getRelName(),pAttrList[k],id,Sec,infer, dbinfo);
				pGraphList.add(newpgraph2);
			}
		}
		
	}	
		
	public static boolean containInt(int[] intArray, int element)
	{
		for(int i=0; i<intArray.length; i++)
		{
			if(intArray[i]==element) return true;
		}
		return false;
	}
	
	//find the nearest temporal node - bfs	
	private static void findTemporalNode(PGraph pgraph, int index, Pattern pattern, ArrayList<PGraph> pGraphList,int tpIndex,ORMGraph ormgraph, DBinfo dbinfo)
	{
		int pnodeNum = pgraph.getNodeNum();
		int[] visit = new int[pnodeNum];
		int[] nodeLevel = new int[pnodeNum];
		Queue<Integer> visitQueue = new LinkedList<Integer>();
		int tNodeLevel=32;//there are at most 31 levels
		
		visitQueue.offer(index);
		while(!visitQueue.isEmpty())
		{
			int currentNode = visitQueue.poll();
			if(nodeLevel[currentNode]>tNodeLevel)break;
			
			if(visit[currentNode]==0)//unvisited
			{
				visit[currentNode] = 1;
				
				if (pgraph.getNode(currentNode).isTemporalNode(dbinfo,ormgraph))//
				{
					tNodeLevel = nodeLevel[currentNode];
					findAnnotateTemporalPattern(pgraph.getNode(currentNode),currentNode,pgraph,pattern,pGraphList,ormgraph,dbinfo);
				}
				//getAdjNode
				int[] adjInNode = pgraph.getInEdges(currentNode);
				int[] adjOutNode = pgraph.getOutEdges(currentNode);
				int inLen = adjInNode.length;
				int outLen = adjOutNode.length;
				int []adjNode = new int[inLen+outLen];
				System.arraycopy(adjInNode,0,adjNode,0,inLen);
				System.arraycopy(adjOutNode,0,adjNode,inLen,outLen);
				for(int nextNode:adjNode)
				{
					if(visit[nextNode]==0)//0 is not visited.
					{
						visitQueue.offer(nextNode);
						nodeLevel[nextNode] = nodeLevel[currentNode]+1;
					}
				}
			}
		}
	}
	
//	private void findAnnotateTemporalPattern(PNode pnode,int nodeIndex, PGraph pgraph, Pattern pattern, ArrayList<PGraph> pGraphList)
//	{
//		Relation[] tempRelList;
//		ORMGraph ormgraph =  this.getORMGraph();
//		tempRelList = pnode.getTempRelList(ormgraph);
//		
//		int tempNodeNum = tempRelList.length;
//		for (int n=0; n<tempNodeNum; n++)
//		{
//			PGraph newpgraph = pgraph.getDeepCopy();
//			newpgraph.getNode(nodeIndex).setPatternTp(pattern,tempRelList[n].getRelName());//Problem
//			
//			int[][] pAttrList = tempRelList[n].getTempAttrList();
//			int pAttrListLen = pAttrList.length;
//			for (int k = 0; k<pAttrListLen; k++)
//			{
//				PGraph newpgraph2 = newpgraph.getDeepCopy();
//				newpgraph2.getNode(nodeIndex).setTpAttr(pAttrList[k], dbinfo);
//				pGraphList.add(newpgraph2);
//			}
//		}
//		
//	}
	
	private static void findAnnotateTemporalPattern(PNode pnode,int nodeIndex, PGraph pgraph, Pattern pattern, ArrayList<PGraph> pGraphList,ORMGraph ormgraph, DBinfo dbinfo)
	{
		Relation[] tempRelList;

		tempRelList = pnode.getTempRelList(ormgraph);
		
		int tempNodeNum = tempRelList.length;
		for (int n=0; n<tempNodeNum; n++)
		{
			PGraph newpgraph = pgraph.getDeepCopy();
			//set the rel name, and also set the attribute name
			newpgraph.getNode(nodeIndex).setPatternSimpleTp(pattern,tempRelList[n].getRelName(),true);//Problem
			
			int[][] pAttrList = tempRelList[n].getTempAttrList();
			int pAttrListLen = pAttrList.length;
			for (int k = 0; k<pAttrListLen; k++)
			{
				PGraph newpgraph2 = newpgraph.getDeepCopy();
				newpgraph2.getNode(nodeIndex).setTpAttr(pAttrList[k],dbinfo);
				pGraphList.add(newpgraph2);
			}
		}
		
	}
	
	private static void annotateTemporalPattern(PNode pnode,int nodeIndex, PGraph pgraph, Pattern pattern, ArrayList<PGraph> pGraphList, Keyword[] kwArray,ORMGraph ormgraph,DBinfo dbinfo)
	{
		
		int kwIndex = pattern.getTpKwIndex();
		int tagIndex = pattern.getTpTagIndex();
		String relName = kwArray[kwIndex].getTagName(tagIndex);
		
		int ormNodeId = pnode.getORMNodeId();
		Node ormNode =  ormgraph.getNode(ormNodeId);
		String ormName = ormNode.getNodeName();
		Relation[] tempRelList;
		
		if (relName == ormName)//obj node
		{
			tempRelList = pnode.getTempRelList(ormgraph);
		}
		else
		{
			int compRelIndex = ormNode.getCompRel4Name(relName);
			tempRelList = new Relation[1];
			tempRelList[0] = ormNode.getCompRel(compRelIndex);
		}
		
		int tempNodeNum = tempRelList.length;
		for (int n=0; n<tempNodeNum; n++)
		{
			PGraph newpgraph = pgraph.getDeepCopy();
			newpgraph.getNode(nodeIndex).setPatternSimpleTp(pattern,tempRelList[n].getRelName(),false);//Problem
			
			int[][] pAttrList = tempRelList[n].getTempAttrList();
			int pAttrListLen = pAttrList.length;
			for (int k = 0; k<pAttrListLen; k++)
			{
				PGraph newpgraph2 = newpgraph.getDeepCopy();
				newpgraph2.getNode(nodeIndex).setTpAttr(pAttrList[k],dbinfo);
				pGraphList.add(newpgraph2);
			}
		}
		
	}
	
	private PGraph buildPalindromePath(int rootId, boolean trivialRoot, HashMap<Integer, ArrayList<PNode>> clusterList)
	{
		PGraph pgraph = new PGraph();
		PNode root;
		if(trivialRoot)
		{
			root = new PNode(rootId, null);
		}
		else
		{
			ArrayList<PNode> cluster = clusterList.get(rootId);
			root = cluster.get(0).getCopy();
		}
		pgraph.createNode(root);
		LinkedList<Integer> nodeInORM = new LinkedList<Integer>();
		nodeInORM.add(rootId);
		
		for(int ormNodeId : clusterList.keySet())
		{
			if(ormNodeId != rootId)
			{
				nodeInORM.add(ormNodeId);
				AuxGraph subgraph = findSubgraphInORM(nodeInORM);
				ArrayList<PNode> cluster = clusterList.get(ormNodeId);
				int clusterSize = cluster.size();
				for(int i = 0; i < clusterSize; i++)
				{
					PNode leaf = cluster.get(i);
					if(!buildSimplePath(pgraph, root, leaf, subgraph))
					{
						return null;
					}
				}
				nodeInORM.removeLast();
			}
		}
		return pgraph;
	}

	private boolean buildSimplePath(PGraph pgraph, PNode root, PNode leaf, AuxGraph path)
	{
		if(path == null)
		{
			return false;
		}
		int graphNodeNum = pgraph.getNodeNum();
		int offset = graphNodeNum - 1;
		int pathNodeNum = path.getNodeNum();
		for(int i = 1; i < pathNodeNum - 1; i++)
		{
			int ormNodeId = path.getNode(i);
			PNode pnode = new PNode(ormNodeId, null);
			if(!pgraph.createNode(pnode))
			{
				return false;
			}
		}
		PNode leafcopy = leaf.getCopy();
		if(!pgraph.createNode(leafcopy))
		{
			return false;
		}
		pgraph.createEdge(path, offset);
		return true;
	}

	private int getRoot(HashMap<Integer, ArrayList<PNode>> clusterList)
	{
		for(int ormNodeId : clusterList.keySet())
		{
			ArrayList<PNode> cluster = clusterList.get(ormNodeId);
			if(cluster.size() == 1)
			{
				return ormNodeId;
			}
		}
		return -1;
	}

	private AuxGraph findSubgraphInORM(LinkedList<Integer> leafNodeList)
	{
		LinkedList<AuxGraph> queue = new LinkedList<AuxGraph>();
		
		int firstLeaf = leafNodeList.getFirst();
		AuxGraph firstGraph = new AuxGraph();
		firstGraph.createNode(firstLeaf);
		queue.add(firstGraph);

		while(!queue.isEmpty())
		{
			AuxGraph graph = queue.pollFirst();
			
			if(isSubGraph(graph, leafNodeList))
			{
				return graph;
			}
			else
			{
				int nodeNum = graph.getNodeNum();
				if(nodeNum < Constant.maxNodeNum)
				{
					for(int i = 0; i < nodeNum; i++)
					{
						int ormNodeId = graph.getNode(i);
						int[] outEdges = this.getORMGraph().getOutEdges(ormNodeId);
						int[] inEdges = this.getORMGraph().getInEdges(ormNodeId);
						
						for(int j = 0; j < outEdges.length; j++)
						{
							int neighborNodeId = outEdges[j];
							if(!graph.containNode(neighborNodeId))
							{
								AuxGraph newgraph = graph.getCopy();
								newgraph.createNode(neighborNodeId);
								newgraph.createEdge(i, newgraph.getNodeNum() - 1);
								queue.add(newgraph);
							}
						}
						
						for(int j = 0; j < inEdges.length; j++)
						{
							int neighborNodeId = inEdges[j];
							if(!graph.containNode(neighborNodeId))
							{
								AuxGraph newgraph = graph.getCopy();
								newgraph.createNode(neighborNodeId);
								newgraph.createEdge(newgraph.getNodeNum() - 1, i);
								queue.add(newgraph);
							}
						}
					}
				}
			}
		}
		return null;
	}

	private boolean isSubGraph(AuxGraph graph, LinkedList<Integer> leafNodeList)
	{
		for(Integer node : leafNodeList)
		{
			if(!graph.containNode(node))
			{
				return false;
			}
		}
		int nodeNum = graph.getNodeNum();
		HashSet<Integer> leafNodeSet = new HashSet<Integer>(leafNodeList);
		for(int i = 0; i < nodeNum; i++)
		{
			if(graph.isLeafNode(i))
			{
				int node = graph.getNode(i);
				if(!leafNodeSet.contains(node))
				{
					return false;
				}
			}
		}
		return true;
	}

	private static HashMap<Integer, ArrayList<PNode>> clusterNonTrivialNode(PNode[] nonTrivialNode)
	{
		HashMap<Integer, ArrayList<PNode>> clusterList = new HashMap<Integer, ArrayList<PNode>>();
		int nonTrivialNodeNum = nonTrivialNode.length;
		
		for(int i = 0; i < nonTrivialNodeNum; i++)
		{
			PNode pnode = nonTrivialNode[i];
			int ormNodeId = pnode.getORMNodeId();
			if(clusterList.containsKey(ormNodeId))
			{
				ArrayList<PNode> cluster = clusterList.get(ormNodeId);
				cluster.add(pnode);
			}
			else
			{
				ArrayList<PNode> cluster = new ArrayList<PNode>();
				cluster.add(pnode);
				clusterList.put(ormNodeId, cluster);
			}
		}
		return clusterList;
	}

	
	private int[][] getTagGrouping(int[] combTag, Keyword[] kwArray)
    {
    	int kwNum = kwArray.length;
    	
    	int[][] tmpGrouping = new int[kwNum][kwNum];
    	int groupNum = 0;
    	int[] groupLen = new int[kwNum];
    	tmpGrouping[0][0] = 0;
    	groupNum = 1;
    	groupLen[0] = 1;
    	
    	for(int i = 1; i < kwNum; i++)
    	{
    		int groupIndex = groupNum - 1;
    		if(this.isGroup(combTag, tmpGrouping[groupIndex], groupLen[groupIndex], i, kwArray))
    		{
    			tmpGrouping[groupIndex][groupLen[groupIndex]] = i;
    			groupLen[groupIndex]++;
    		}
    		else
    		{
    			tmpGrouping[groupIndex + 1][0] = i;
    			groupLen[groupIndex + 1] = 1;
    			groupNum++;
    		}
    	}
    	
    	int[][] kwGrouping = new int[groupNum][];
    	for(int i = 0; i < groupNum; i++)
    	{
    		kwGrouping[i] = new int[groupLen[i]];
    		System.arraycopy(tmpGrouping[i], 0, kwGrouping[i], 0, groupLen[i]);
    	}
    	
    	return kwGrouping;
    }
	
	private boolean isGroup(int[] combTag, int[] group, int len, int kwIndex, Keyword[] kwArray)
	{
		int kwIndexInGroup = group[0];
		Keyword kwInGroup = kwArray[kwIndexInGroup];
		Keyword kw = kwArray[kwIndex];
		
		String tagNameInGroup = kwInGroup.getTagName(combTag[kwIndexInGroup]);
		String tagName = kw.getTagName(combTag[kwIndex]);
		if(!this.getORMGraph().isSameNode(tagNameInGroup, tagName))
		{
			return false;
		}
		else
		{
			String tagAttr = kw.getTagAttr(combTag[kwIndex]);
			String tagVal = kw.getTagVal(combTag[kwIndex]);
			
			if(tagAttr == null && tagVal == null)
			{
				return false;
			}
			else
			{
				for(int i = 0; i < len; i++)
				{
					int groupKwIndex = group[i];
					Keyword groupKw = kwArray[groupKwIndex];
					String groupKwTagName = groupKw.getTagName(combTag[groupKwIndex]);
					String groupKwTagAttr = groupKw.getTagAttr(combTag[groupKwIndex]);
					String groupKwTagVal = groupKw.getTagVal(combTag[groupKwIndex]);
					
					if(groupKwTagVal != null && groupKwTagAttr.equals(tagAttr))
					{
						if(this.getORMGraph().isSingleValuedAttr(groupKwTagName))
						{
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	
	private int[] getTagCombination(int index, Keyword[] kwArray)
    {
    	int kwNum = kwArray.length;
    	
    	int[] combTag = new int[kwNum];
    	for(int i = 0; i < kwNum; i++)
    	{
    		Keyword kw = kwArray[i];
    		int tagNum = kw.getTaglistLength();
    		combTag[i] = index % tagNum;
    		index = index / tagNum;
    	}
    	return combTag;
    }
	  
	private int getCombinationNum(Keyword[] kwArray)
    {
    	int kwNum = kwArray.length;
    	int combNum = 1;
    	for(int i = 0; i < kwNum; i++)
    	{
    		Keyword kw = kwArray[i];
    		int tagNum = kw.getTaglistLength();
    		combNum = combNum * tagNum;
    	}
    	
    	return combNum;
    }
	
	
}
