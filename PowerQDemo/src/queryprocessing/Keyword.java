package queryprocessing;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Keyword
{
	private String content;
	private boolean isphrase;
	private class Tag
	{
		private String name;
		private String attr;
		private String val;
		private Tag(String name, String attr, String val)
		{
			this.name = name;
			this.attr = attr;
			this.val = val;
		}
	}
	
	private Tag[] taglist;
	
	
	public Keyword(String content, boolean isphrase)
	{
		this.content = content;
		this.isphrase = isphrase;
		this.taglist = null;
	}
	
	public boolean isPhrase()
	{
		return this.isphrase;
	}
	
	public String getContent()
	{
		return this.content;
	}
	
	public int getTaglistLength()
	{
		if (this.taglist == null)
		{
			return 0;
		}
		return this.taglist.length;
	}
	
	public String getTagName(int index)
	{
		Tag tag = this.taglist[index];
		return tag.name;
	}
	
	public String getTagAttr(int index)
	{
		Tag tag = this.taglist[index];
		return tag.attr;
	}
	
	public String getTagVal(int index)
	{
		Tag tag = this.taglist[index];
		return tag.val;
	}
	
	public void createTag(DBinfo dbinfo, SQLBean sqlbean) throws SQLException
	{
		ArrayList<Tag> tags = new ArrayList<Tag>();
		int relNum = dbinfo.getRelNum();
		
		for(int i = 0; i < relNum; i++)
		{
			Relation rel = dbinfo.getRel(i);
			String relAlias = rel.getRelAlias();
			String relName = rel.getRelName();
//			String remark = rel.getRemark();
			
			this.createNameTag(tags, relAlias);
			
			int attrNum = rel.getAttrNum();
			for(int j = 0; j < attrNum; j++)
			{
				String attrName = rel.getAttrName(j);
				this.createAttrTag(tags, relAlias, attrName);
			}
			
			int textAttrNum = rel.getTextAttrNum();
			for(int j = 0; j < textAttrNum; j++)
			{
				String textAttrName = rel.getTextAttrName(j);
				this.createValTag(tags, relName, relAlias, textAttrName, sqlbean);
//				this.createValTag(tags, relName, relAlias, remark, textAttrName, sqlbean);
			}
		}
		
		this.taglist = tags.toArray(new Tag[tags.size()]);
	}
	
	private void createNameTag(ArrayList<Tag> taglist, String relAlias)
	{
		if(this.content.equalsIgnoreCase(relAlias))
		{
			Tag newtag = new Tag(relAlias, null, null);
			taglist.add(newtag);
		}
	}
	
	private void createAttrTag(ArrayList<Tag> taglist, String relAlias, String attrName)
	{
		if(this.content.equalsIgnoreCase(attrName))
		{
			Tag newtag = new Tag(relAlias, attrName, null);
			taglist.add(newtag);
		}
	}
	
	private void createValTag(ArrayList<Tag> taglist, String relName, String relAlias, String attrName, SQLBean sqlbean) throws SQLException
	{
		String val = null;
		if(this.isphrase)
		{
			val = "\"" + this.content + "\"";
		}
		else
		{
			val = this.content;
		}
		
		if(isMatch(relName, attrName, val, sqlbean))
		{
			Tag newtag = new Tag(relAlias, attrName, this.content);
			taglist.add(newtag);
		}
	}
	
	private void createValTag(ArrayList<Tag> taglist, String relName, String relAlias, String remark, String attrName, SQLBean sqlbean) throws SQLException
	{
		String val = null;
		if(this.isphrase)
		{
			val = "\"" + this.content + "\"";
		}
		else
		{
			val = this.content;
		}
		
		if(isMatch(relName, attrName, val, remark, sqlbean))
		{
			Tag newtag = new Tag(relAlias, attrName, this.content);
			taglist.add(newtag);
		}
	}
	
	public boolean isMatch(String rel, String attr, String val, SQLBean sqlbean) throws SQLException
	{
		StringBuffer sqlbuf = new StringBuffer();
		sqlbuf.append("SELECT * FROM `").append(rel).append("` WHERE MATCH(`").append(attr).append("`) AGAINST('").append(val).append("' IN BOOLEAN MODE) LIMIT 1");
		ResultSet rs = sqlbean.executeQueryScroll(sqlbuf.toString());
		int rowNum = getNumOfRows(rs);
		if(rowNum > 0)
		{
			return true;
		}
		else
		{
			return false;
		}
		
	}
	
	public boolean isMatch(String rel, String attr, String val, String remark, SQLBean sqlbean) throws SQLException
	{
		StringBuffer sqlbuf = new StringBuffer();
		if(remark == null)
		{
			sqlbuf.append("SELECT * FROM `").append(rel).append("` WHERE MATCH(`").append(attr).append("`) AGAINST('").append(val).append("' IN BOOLEAN MODE) LIMIT 1");
		}
		else
		{
			sqlbuf.append(remark);
			String attStr = parseRemark(remark, attr);
			if(remark.indexOf("WHERE") != -1)
			{
				sqlbuf.append(" AND MATCH(");
			}
			else
			{
				sqlbuf.append(" WHERE MATCH(");
			}
			sqlbuf.append(attStr).append(") AGAINST('").append(val).append("' IN BOOLEAN MODE) LIMIT 1");
		}
		ResultSet rs = sqlbean.executeQueryScroll(sqlbuf.toString());
		int rowNum = getNumOfRows(rs);
		if(rowNum > 0)
		{
			return true;
		}
		else
		{
			return false;
		}
		
	}
	
	private String parseRemark(String remark, String attr)
	{
		int stdIndex = "SELECT DISTINCT".length();
		int endIndex = remark.indexOf("FROM");
		String subRemark = remark.substring(stdIndex, endIndex).trim();
		String[] col = subRemark.split(", ");
		for(int i = 0; i < col.length; i++)
		{
			String[] component = col[i].split(" AS ");
			if(component[1].indexOf(attr) != -1)
			{
				return component[0];
			}
		}
		return null;
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
	
	public void updateKwTag(String[] choice)
	{
		int choiceNum = choice.length;
		Tag[] updateTagList = new Tag[choiceNum];
		
		for(int i = 0; i < choiceNum; i++)
		{
			String indexstr = choice[i];
			int index = Integer.parseInt(indexstr);
			updateTagList[i] = this.taglist[index];
		}
		this.taglist = updateTagList;
	}
	
	public void deleteTag(int index)
	{
		int tagNum = getTaglistLength()-1;
		int count = 0;
		if (tagNum>0)
		{
			Tag[] updateTagList = new Tag[tagNum];
			for(int i = 0; i < tagNum; i++)
			{
				if (count == index)
				{
					count++;
				}
				if (count <= tagNum+1)
				{
					updateTagList[i] = this.taglist[count];
					count++;
				}
				else
				{
					break;
				}
			}
			this.taglist = updateTagList;
		}
		else
		{
			this.taglist = null;
		}
	}
	
	public void updateKwTag4OnlyAttr()
	{
		ArrayList<String> choiceArray = new ArrayList<String>();
		for(int i = 0; i < this.getTaglistLength(); i++)
		{
			String attrTag = this.getTagAttr(i);
			String valTag = this.getTagVal(i);
			if(attrTag != null && valTag == null)
			{
				choiceArray.add(new Integer(i).toString());
			}
		}
		String[] choice = choiceArray.toArray(new String[choiceArray.size()]);
		this.updateKwTag(choice);
	}
	
	public void updateKwTag4NameAttr()
	{
		ArrayList<String> choiceArray = new ArrayList<String>();
		for(int i = 0; i < this.getTaglistLength(); i++)
		{
			String valTag = this.getTagVal(i);
			if(valTag == null)
			{
				choiceArray.add(new Integer(i).toString());
			}
		}
		String[] choice = choiceArray.toArray(new String[choiceArray.size()]);
		this.updateKwTag(choice);
	}
	
	public boolean hasAttrTag()
	{
		for(int i = 0; i < this.getTaglistLength(); i++)
		{
			String attrTag = this.getTagAttr(i);
			String valTag = this.getTagVal(i);
			if(attrTag != null && valTag == null)
			{
				return true;
			}
		}
		return false;
	}
	
	public boolean hasNameTag()
	{
		for(int i = 0; i < this.getTaglistLength(); i++)
		{
			String attrTag = this.getTagAttr(i);
			String valTag = this.getTagVal(i);
			if(attrTag == null && valTag == null)
			{
				return true;
			}
		}
		return false;
	}
}
