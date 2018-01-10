package queryprocessing;

import java.util.ArrayList;

public class TNode
{
	private int pNodeId;
	private ArrayList<TNode> children;
	private String label;
	
	public TNode(int pNodeId)
	{
		this.pNodeId = pNodeId;
		this.children = new ArrayList<TNode>();
		this.label = null;
	}
	
	public void setNodeLabel(String label)
	{
		this.label = label;
	}
	
	public String getNodeLabel()
	{
		return this.label;
	}
	
	public boolean hasLabel()
	{
		if(this.label == null)
		{
			return false;
		}
		else
		{
			return true;
		}
	}
	
	public void createChild(TNode node)
	{
		this.children.add(node);
	}
	
	public int getPNodeId()
	{
		return this.pNodeId;
	}
	
	public TNode getChild(int index)
	{
		return this.children.get(index);
	}
	
	public int getChildNum()
	{
		return this.children.size();
	}
}
