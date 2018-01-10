package queryprocessing;

import queryprocessing.Constant.RelType;

public class DBinfo
{
	private Relation[] rels;
	private Dependency[] fks;
	private Dependency[][] rel2fk;
	private String[][] desc;
	
	private int [][] temporalDetail;
	Constant.TemporalType[] temporalType;
	private String[][] attr;
	
	public DBinfo(String[] db, String[][] attr, int[][] output, int[][] verify, int[][] text, int[][] key, RelType[] type, int[][] fk, String[][] desc, boolean useView, Constant.TemporalType[] temporalType, int [][] temporalDetail)
	{
		int relNum = db.length;
		this.rels = new Relation[relNum];
		this.temporalDetail = temporalDetail;
		this.temporalType = temporalType;
		this.attr = attr;
		for(int i = 0; i < relNum; i++)
		{
			Relation rel;
			if(useView)
			{
				rel = new Relation(db[i] + Constant.view, db[i], attr[i], output[i], verify[i], text[i], key[i], type[i],temporalType[i],temporalDetail,i);
			}
			else
			{
				rel = new Relation(db[i], db[i], attr[i], output[i], verify[i], text[i], key[i], type[i],temporalType[i],temporalDetail,i);
			}
			this.rels[i] = rel;
		}
		
		int depNum = fk.length / 2;
		this.fks = new Dependency[depNum];
		this.rel2fk = new Dependency[relNum][relNum];
		
		for(int i = 0; i < depNum; i++)
		{
			int relLid = fk[2*i][0];
			int relRid = fk[2*i+1][0];
			Relation relL = this.rels[relLid];
			Relation relR = this.rels[relRid];
			int attrL = fk[2*i][1];
			int attrR = fk[2*i+1][1];
			
			Dependency depend = new Dependency(relL, relR, attrL, attrR);
			relL.addOutFK(depend);
			relR.addInFK(depend);
			this.fks[i] = depend;
			this.rel2fk[relLid][relRid] = depend;
		}
		
		this.desc = desc;
	}
	
//	public void setRenark(String[] sql)
//	{
//		for(int i = 0; i < this.getRelNum(); i++)
//		{
//			Relation rel = this.getRel(i);
//			int stdIndex = sql[i].indexOf("SELECT DISTINCT");
//			rel.setRemark(sql[i].substring(stdIndex));
//		}
//	}
	
	public int getRelNum()
	{
		return this.rels.length;
	}
	
	public Relation getRel(int index)
	{
		return this.rels[index];
	}
	
	public Relation getRel(String alias)
	{
		int index = this.getRelId(alias);
		if(index != -1)
		{
			return this.rels[index];
		}
		else
		{
			return null;
		}
	}
	
	public int getRelId(String alias)
	{
		for(int i = 0; i < this.getRelNum(); i++)
		{
			Relation rel = getRel(i);
			if(rel.getRelAlias().equals(alias))
			{
				return i;
			}
		}
		return -1;
	}
	
	public int getRelId(Relation rel)
	{
		for(int i = 0; i < this.getRelNum(); i++)
		{
			if(this.getRel(i) == rel)
			{
				return i;
			}
		}
		return -1;
	}
	
	public int getFKNum()
	{
		return this.fks.length;
	}
	
	public Relation getRelLinFK(int index)
	{
		Dependency dep = this.fks[index];
		return dep.getRelL();
	}
	
	public Relation getRelRinFK(int index)
	{
		Dependency dep = this.fks[index];
		return dep.getRelR();
	}
	
	public String getDesc(int fromRelId, int toRelId)
	{
		return this.desc[fromRelId][toRelId];
	}
	
	public Dependency getDependency(int relLid, int relRid)
	{
		return this.rel2fk[relLid][relRid];
	}
	
	public Constant.TemporalType getTemporalType(int i)
	{
		return temporalType[i];
	}
	
	public int getTemporalDetialLen()
	{
		return temporalDetail.length;
	}
	
	public int getTemporalDetail(int i, int j)
	{
		return temporalDetail[i][j];
	}
	
	public String getAttr(int i, int j)
	{
		if (j==-1) return null;
		return attr[i][j];
	}
}
