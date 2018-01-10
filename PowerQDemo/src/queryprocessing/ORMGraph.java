package queryprocessing;



import java.util.ArrayList;
import java.util.HashMap;

import queryprocessing.Constant.RelType;



public class ORMGraph
{
	private ArrayList<Node> nodelist;
	private HashMap<String, Integer> nodemap;
	private int[] inedgelist;
	private int[] outedgelist;
	private HashMap<String, String> relmap;
	private String[][] label;
	
	public ORMGraph(DBinfo dbinfo)
	{
		this.nodelist = new ArrayList<Node>();
		this.nodemap = new HashMap<String, Integer>();
		this.relmap = new HashMap<String, String>();
		
		Node prev = null;
		int relNum = dbinfo.getRelNum();
		
		for(int i = 0; i < relNum; i++)
		{
			Relation rel = dbinfo.getRel(i);
			Node node = this.createNode(prev, rel);
			prev = node;
		}
		
		int nodeNum = this.getNodeNum();
		this.inedgelist = new int[nodeNum];
		this.outedgelist = new int[nodeNum];
		this.label = new String[nodeNum][nodeNum];
		
		for(int i = 0; i < relNum; i++)
		{
			for(int j = 0; j < relNum; j++)
			{
				Relation relFrom = dbinfo.getRel(i);
				Relation relTo = dbinfo.getRel(j);
				int fromNodeId = this.getNodeId(relFrom);
				int toNodeId = this.getNodeId(relTo);
				if(fromNodeId != -1 && toNodeId != -1)
				{
					this.label[fromNodeId][toNodeId] = dbinfo.getDesc(i, j);
				}
			}
		}
		int fkNum = dbinfo.getFKNum();
		for(int i = 0; i < fkNum; i++)
		{
			Relation relL = dbinfo.getRelLinFK(i);
			Relation relR = dbinfo.getRelRinFK(i);
			int nodeLid = this.getNodeId(relL);
			int nodeRid = this.getNodeId(relR);
			if(nodeLid != -1 && nodeRid != -1)
			{
				this.createEdge(nodeLid, nodeRid);
			}
		}
	}
	
	private Node createNode(Node prev, Relation rel)
	{
		RelType type = rel.getRelType();
		Constant.TemporalType tempType = rel.getRelTempType();
		int nodeid = this.nodelist.size();
		if(type == RelType.Component)// component relation must be next to its main relation
		{
			prev.addCompRel(rel);
			if (tempType != Constant.TemporalType.GeneralRelation)
			{
				prev.addTempCompRel(rel);
			}
			String name = prev.getNodeName();
			String relAlias = rel.getRelAlias();
			this.relmap.put(relAlias, name);
			return prev;
		}
		else
		{
			Node node = new Node(rel);
			String name = node.getNodeName();
			this.nodelist.add(node);
			this.nodemap.put(name, nodeid);
			return node;
		}
	}
	
	private void createEdge(int nodeLid, int nodeRid)
	{
		this.outedgelist[nodeLid] |= 1 << nodeRid;
		this.inedgelist[nodeRid] |= 1 << nodeLid;
	}

	public int getNodeNum()
	{
		return this.nodelist.size();
	}
	
	public int[] getOutEdges(int index)
	{
		int[] nodeId = new int[this.nodelist.size()];
		int len = 0;
		int outEdgeList = this.outedgelist[index];
		
		while(outEdgeList > 0)
		{
			int neighborNodeId = (int) (Math.log(outEdgeList) / Math.log(2));
			nodeId[len++] = neighborNodeId;
			outEdgeList &= ~(1 << neighborNodeId);
		}
		
		int[] outEdges = new int[len];
		System.arraycopy(nodeId, 0, outEdges, 0, len);
		return outEdges;
	}
	
	public int[] getInEdges(int index)
	{
		int[] nodeId = new int[this.nodelist.size()];
		int len = 0;
		int inEdgeList = this.inedgelist[index];
		
		while(inEdgeList > 0)
		{
			int neighborNodeId = (int) (Math.log(inEdgeList) / Math.log(2));
			nodeId[len++] = neighborNodeId;
			inEdgeList &= ~(1 << neighborNodeId);
		}
		
		int[] inEdges = new int[len];
		System.arraycopy(nodeId, 0, inEdges, 0, len);
		return inEdges;
	}
	
	public int getNodeId(Relation rel)
	{
		String relAlias = rel.getRelAlias();
		if(this.nodemap.containsKey(relAlias))
		{
			return this.nodemap.get(relAlias);
		}
		return -1;
	}
	
	public int getNodeId(String name)
	{
		if(this.relmap.containsKey(name))
		{
			name = this.relmap.get(name);
		}
		return this.nodemap.get(name);
	}

	
	public Node getNode(int index)
	{
		return this.nodelist.get(index);
	}
	
	public boolean isSameNode(String str1, String str2)
	{
		if(this.relmap.containsKey(str1))
		{
			str1 = this.relmap.get(str1);
		}
		if(this.relmap.containsKey(str2))
		{
			str2 = this.relmap.get(str2);
		}
		
		if(str1.equals(str2))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean isSameAttr(String str1, String str2)
	{
		if(str1.equals(str2))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public boolean isSingleValuedAttr(String str)
	{
		if(this.relmap.containsKey(str))
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	public String getLabel(int fromNodeId, int toNodeId)
	{
		return this.label[fromNodeId][toNodeId];
	}
	
	int[][] getTpAttr(int ormNodeId, String relName)//what's this??
	{
		Node ormNode =  this.getNode(ormNodeId);//get the ORM node
		int compRelIndex = ormNode.getCompRel4Name(relName);//get the component rel
		Relation[] tempRelList= new Relation[1];//????
		if(compRelIndex!=-1)//have component relation
		{
			tempRelList[0] = ormNode.getCompRel(compRelIndex);
		}
		else
		{
			tempRelList[0] = ormNode.getCoreRel();
		}
		int[][] pAttrList = tempRelList[0].getTempAttrList();
		
		return pAttrList;//(from,to,attr_name)
	}
	
}
