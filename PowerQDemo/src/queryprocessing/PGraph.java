package queryprocessing;

import queryprocessing.Constant.NodeType;

public class PGraph implements Comparable<PGraph>
{
	private PNode[] nodelist;
	private int[] inedgelist;
	private int[] outedgelist;
	
	private int nodeNum;
	private double score;
	private int centricId;
	
	public PGraph()
	{
		this.nodelist = new PNode[Constant.maxNodeNum];
		this.inedgelist = new int[Constant.maxNodeNum];
		this.outedgelist = new int[Constant.maxNodeNum];
		this.nodeNum = 0;
		this.score = 0.;
		this.centricId = 0;
	}
	
	public boolean createNode(PNode node)
	{
		if(this.nodeNum < Constant.maxNodeNum)
		{
			int nodeId = this.nodeNum;
			this.nodelist[nodeId] = node;
			this.nodeNum++;
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public void createEdge(AuxGraph graph)
	{
		int nodeNum = graph.getNodeNum();
		System.arraycopy(graph.getOutEdgeList(), 0, this.outedgelist, 0, nodeNum);
		System.arraycopy(graph.getInEdgeList(), 0, this.inedgelist, 0, nodeNum);
	}
	
	public void createEdge(AuxGraph path, int offset)
	{
		this.outedgelist[0] |= path.getOutEdge(0) << offset;
		this.inedgelist[0] |= path.getInEdge(0) << offset;
		
		int nodeNum = path.getNodeNum();
		for(int i = 1; i < nodeNum; i++)
		{
			this.outedgelist[i + offset] = ((path.getOutEdge(i)&(~1)) << offset) | (path.getOutEdge(i) & 1);
			this.inedgelist[i+offset] = ((path.getInEdge(i)&(~1)) << offset) | (path.getInEdge(i) & 1);
		}
	}
	
	@Override
	public int compareTo(PGraph arg0)
	{
		double val = (arg0.score - this.score);
		int res = 0;
		if(val > 0)
			res = 1;
		if(val < 0)
			res = -1;
		if(val == 0)
			res = 0;
		return res;
	}
	
	private int[][] computeDistance(ORMGraph ormgraph)
	{
		int[][] dist = new int[this.nodeNum][this.nodeNum];
		PNode firstNode = this.nodelist[0];
		if(firstNode.getType(ormgraph) != NodeType.Relationship)
		{
			dist[0][0] = 1;
		}
		if(this.nodeNum > 1)
		{
			for(int i = 1; i < this.nodeNum; i++)
			{
				PNode pNode = this.nodelist[i];
				
				int neighborId = this.getPrevNeighborId(i);
				PNode neighborNode = this.nodelist[neighborId];
				
				int overlap = 0;
				int newDist = 0;
				
				if(pNode.getType(ormgraph) != NodeType.Relationship)
				{
					newDist++;
					dist[i][i] = 1;
				}
				if(neighborNode.getType(ormgraph) != NodeType.Relationship)
				{
					newDist++;
					overlap = 1;
				}
				
				dist[i][neighborId] = dist[neighborId][i] = newDist;
				
				for(int j = 0; j < neighborId; j++)
				{
					dist[j][i] = dist[i][j] = dist[j][neighborId] + newDist - overlap;
				}
				
				for(int j = neighborId + 1; j < i; j++)
				{
					dist[j][i] = dist[i][j] = dist[j][neighborId] + newDist - overlap;
				}
				
			}
		}
		return dist;
	}
	
	private int getPrevNeighborId(int index)
	{
		int outedges = this.outedgelist[index];
		int inedges = this.inedgelist[index];
		outedges &= (1<<index) - 1;
		inedges &= (1<<index) - 1;
		int neighborId;
		
		if(outedges > 0)
		{
			neighborId = (int) (Math.log(outedges)/Math.log(2));
		}
		else
		{
			neighborId = (int) (Math.log(inedges)/Math.log(2));
		}
		
		return neighborId;
	}
	
	public int[] getTargetNode()
	{
		int[] nodeId = new int[this.nodeNum];
		int len = 0;
		for(int i = 0; i < this.nodeNum; i++)
		{
			PNode pNode = this.nodelist[i];
			if(!pNode.isTrival() && pNode.isTargetNode())
			{
				nodeId[len++] = i;
			}
		}

		int[] targetId = new int[len];
		System.arraycopy(nodeId, 0, targetId, 0, len);
		return targetId;
	}
	
	public int[] getConditionNode()
	{
		int[] nodeId = new int[this.nodeNum];
		int len = 0;
		for(int i = 0; i < this.nodeNum; i++)
		{
			PNode pNode = this.nodelist[i];
			if(!pNode.isTrival() && pNode.isConditionNode())
			{
				nodeId[len++] = i;
			}
		}

		int[] conditionId = new int[len];
		System.arraycopy(nodeId, 0, conditionId, 0, len);
		return conditionId;
	}
	
	public void computeScore(ORMGraph ormgraph)
	{
		this.score = 0.;
		int[][] dist = this.computeDistance(ormgraph);
		int[] targetId = this.getTargetNode();
		int[] conditionId = this.getConditionNode();
		
		double avgDist = 0.;
		int targetNum = targetId.length;
		int conditionNum = conditionId.length;
		
		if(targetNum > 0 && conditionNum > 0)
		{
			for(int i = 0; i < targetNum; i++)
			{
				for(int j = 0; j < conditionNum; j++)
				{
					avgDist += dist[targetId[i]][conditionId[j]];
				}
			}
			avgDist /= targetNum * conditionNum;
		}
		else
		{
			int[] maxDist = this.computeMaxDist(dist);
			this.setCentriID(maxDist, ormgraph);
			avgDist = maxDist[this.centricId];
		}
		this.score = 1 / (this.computeObjectNum(ormgraph) * avgDist);
	}
	
	
	private int computeObjectNum(ORMGraph ormgraph)
	{
		int objNum = 0;
		for(int i = 0; i < this.nodeNum; i++)
		{
			PNode pNode = this.nodelist[i];
			if(pNode.getType(ormgraph) != NodeType.Relationship)
			{
				objNum++;
			}
		}
		return objNum;
	}
	
	private void setCentriID(int[] maxDist, ORMGraph ormgraph)
	{
		int minDist = Integer.MAX_VALUE;
		for(int i = 0; i < maxDist.length; i++)
		{
			PNode pNode = this.nodelist[i];
			if(pNode.getType(ormgraph) != NodeType.Relationship && minDist > maxDist[i]) 
			{
				minDist = maxDist[i];
				this.centricId = i;
			}
		}
	}
	private int[] computeMaxDist(int[][] dist)
	{
		int[] maxDist = new int[this.nodeNum];
		for(int i = 0; i < this.nodeNum; i++)
		{
			for(int j = 0; j < this.nodeNum; j++)
			{
				if(maxDist[i] < dist[i][j])
				{
					maxDist[i] = dist[i][j];
				}
					
			}
		}
		return maxDist;
	}
	
	public int getNodeNum()
	{
		return this.nodeNum;
	}
	
	public int getRootId()
	{
		int[] targetId = this.getTargetNode();
		int len = targetId.length;
		if(len > 0)
		{
			return targetId[0];
		}
		else
		{
			return this.centricId;
		}
	}
	
	public PNode getNode(int index)
	{
		return this.nodelist[index];
	}
	
	public int getCentriId()
	{
		return this.centricId;
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
	
	public PGraph getDeepCopy()
	{
		PGraph pgraph = new PGraph();
		
//		System.arraycopy(this.nodelist, 0, pgraph.nodelist, 0, this.nodeNum);
		pgraph.nodeNum = this.nodeNum;
		pgraph.score = this.score;
		
		//copy pattern for each node
		for (int i=0; i<this.nodeNum; i++)
		{
			pgraph.setNode(this.nodelist[i].getDeepCopy(),i);
		}
		System.arraycopy(this.outedgelist, 0, pgraph.outedgelist, 0, this.nodeNum);
		System.arraycopy(this.inedgelist, 0, pgraph.inedgelist, 0, this.nodeNum);
		return pgraph;
	}
	
	public void setNode(PNode node,int index)
	{
		this.nodelist[index] = node;
	}
	
	//find the second node in this pGraph
		public PNode findSecNode(PNode pnodeFirst)
		{
			int idFirst = pnodeFirst.getPattern().getIdFirst();
			for(int i = 0; i < this.nodeNum; i++)
			{
				PNode pNode = this.nodelist[i];
				if(!pNode.isTrival() && pNode.isTpSequenceSecNode())
				{
					int idSec = pNode.getPattern().getIdSec();
					if (idFirst == idSec) return pNode;
				}
			}
			return null;
		}
		
		public int findSecNodeId(PNode pnode)
		{
			for(int i = 0; i < this.nodeNum; i++)
			{
				PNode pNode = this.nodelist[i];
				if(pNode == pnode)
				{
					return i;
				}
			}
			return -1;
		}
	
}
