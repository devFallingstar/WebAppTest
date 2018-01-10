package queryprocessing;

import java.util.HashSet;

public class AuxGraph
{
	//auxiliary graph for pattern/schema graph generation
	private int[] nodelist;
	private int[] inedgelist;
	private int[] outedgelist;
	
	private int nodeNum;
	private HashSet<Integer> nodeSet;
	
	public AuxGraph()
	{
		this.nodelist = new int[Constant.maxNodeNum];
		this.inedgelist = new int[Constant.maxNodeNum];
		this.outedgelist = new int[Constant.maxNodeNum];
		this.nodeSet = new HashSet<Integer>();
		this.nodeNum = 0;
	}
	
	public void createNode(int node)
	{
		this.nodelist[this.nodeNum] = node;
		this.nodeNum++;
		this.nodeSet.add(node);
	}
	
	public void createEdge(int fromNodeId, int toNodeId)
	{
		this.outedgelist[fromNodeId] |= 1 << toNodeId;
		this.inedgelist[toNodeId] |= 1 << fromNodeId;
	}
	
	public AuxGraph getCopy()
	{
		AuxGraph graph = new AuxGraph();
		System.arraycopy(this.nodelist, 0, graph.nodelist, 0, this.nodeNum);
		graph.nodeNum = this.nodeNum;
		graph.nodeSet.addAll(this.nodeSet);
		
		System.arraycopy(this.outedgelist, 0, graph.outedgelist, 0, this.nodeNum);
		System.arraycopy(this.inedgelist, 0, graph.inedgelist, 0, this.nodeNum);
		return graph;
	}
	
	public boolean containNode(int node)
	{
		if(this.nodeSet.contains(node))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public int getNodeNum()
	{
		return this.nodeNum;
	}
	
	public boolean isLeafNode(int index)
	{
		int outedges = this.outedgelist[index];
		int inedges = this.inedgelist[index];
		int out = outedges & (outedges - 1);
		int in = inedges & (inedges - 1);
		if((outedges == 0 && in == 0) || (inedges == 0 && out == 0) || (outedges == 0 && inedges == 0))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public int getNode(int index)
	{
		return this.nodelist[index];
	}
	
	public int[] getOutEdgeList()
	{
		return this.outedgelist;
	}
	
	public int[] getInEdgeList()
	{
		return this.inedgelist;
	}
	
	public int getOutEdge(int index)
	{
		return this.outedgelist[index];
	}
	
	public int getInEdge(int index)
	{
		return this.inedgelist[index];
	}
	
	public int[] getOutEdges(int index)
	{
		int[] nodeId = new int[this.nodeNum];
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
		int[] nodeId = new int[this.nodeNum];
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
}
