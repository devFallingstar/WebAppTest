package queryprocessing;

import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import queryprocessing.Constant.RelType;

public class Viewinfo
{
	private JSONArray elArray;
	private JSONArray lkArray;
	private DBinfo dbinfo;
	
	private String[] view;
	private String[][] att;
	private int[][] key;
	private RelType[] type;
	private int[][] fk;
	private String[][] dbrel;
	private String[][] dbatt;
	
	private int[][] text;
	private int[][] output;
	private int[][] verify;
	private String[][] desc;
	
	private int [][] temporalDetail;
	private Constant.TemporalType[] tempType;
	
	public Viewinfo(JSONArray er, DBinfo dbinfo)
	{
		this.elArray = (JSONArray) er.get(0);
		this.lkArray = (JSONArray) er.get(1);
		this.dbinfo = dbinfo;
		this.parse();
	}
	
	public int[][] getTempDetail()
	{
		return this.temporalDetail;
	}
	
	public Constant.TemporalType[] getTempType()
	{
		return this.tempType;
	}
	
	public String[] getView()
	{
		return this.view;
	}
	
	public String[][] getAtt()
	{
		return this.att;
	}
	
	public int[][] getKey()
	{
		return this.key;
	}
	
	public RelType[] getType()
	{
		return this.type;
	}
	
	public int[][] getFK()
	{
		return this.fk;
	}
	
	public int[][] getTextAtt()
	{
		return this.text;
	}
	
	public int[][] getOutputAtt()
	{
		return this.output;
	}
	
	public int[][] getVerifyAtt()
	{
		return this.verify;
	}
	
	public String[][] getDesc()
	{
		return this.desc;
	}
	
	public String[][] getDBRel()
	{
		return this.dbrel;
	}
	
	public String[][] getDBAtt()
	{
		return this.dbatt;
	}
	
	private void parse()
	{
		ArrayList<String> viewArray = new ArrayList<String>();
		ArrayList<String[]> attArray = new ArrayList<String[]>();
		ArrayList<int[]> keyArray = new ArrayList<int[]>();
		ArrayList<RelType> typeArray = new ArrayList<RelType>();
		ArrayList<int[]> fkArray = new ArrayList<int[]>();
		ArrayList<String[]> dbrelArray = new ArrayList<String[]>();
		ArrayList<String[]> dbattArray = new ArrayList<String[]>();
		
		for(int i = 0; i < this.elArray.size(); i++)
		{
			JSONObject el = (JSONObject) this.elArray.get(i);
			String element = (String) el.get("Element");
			if(element.equals("Entity"))
			{
				String view = view4Entity(el);
				if(view != null)
				{
					viewArray.add(view);
					String[][] att = att4Entity(el);
					attArray.add(att[0]);
					dbrelArray.add(att[1]);
					dbattArray.add(att[2]);
					typeArray.add(type4Entity(el));
					keyArray.add(key4Entity(el));					
				}
			}
			if(element.equals("Relationship"))
			{
				String view = view4Relationship(el);
				if(view != null)
				{
					viewArray.add(view);
					String [][] att = att4Relationship(el);
					attArray.add(att[0]);
					dbrelArray.add(att[1]);
					dbattArray.add(att[2]);
					typeArray.add(type4Relationship(el));
					keyArray.add(key4Relationship(el));
				}
			}
			if(element.equals("Multivalued"))
			{
				String view = view4Multivalued(el);
				if(view != null)
				{
					viewArray.add(view);
					String[][] att = att4Multivalued(el);
					attArray.add(att[0]);
					dbrelArray.add(att[1]);
					dbattArray.add(att[2]);
					typeArray.add(type4Multivalued(el));
					keyArray.add(key4Multivalued(el));
				}
			}
		}
		
		for(int i = 0; i < this.elArray.size(); i++)
		{
			JSONObject el = (JSONObject) this.elArray.get(i);
			String element = (String) el.get("Element");
			if(element.equals("Entity"))
			{
				if(view4Entity(el) != null)
				{
					fkArray.addAll(fk4Entity(el, viewArray, attArray));
				}
			}
			if(element.equals("Relationship"))
			{
				if(view4Relationship(el) != null)
				{
					fkArray.addAll(fk4Relationship(el, viewArray, attArray));
				}
			}
			if(element.equals("Multivalued"))
			{
				if(view4Multivalued(el) != null)
				{
					fkArray.addAll(fk4Multivalued(el, viewArray, attArray));
				}
			}
		}
		
		this.view = viewArray.toArray(new String[viewArray.size()]);
		this.att = attArray.toArray(new String[attArray.size()][]);
		this.key = keyArray.toArray(new int[keyArray.size()][]);
		this.type = typeArray.toArray(new RelType[typeArray.size()]);
		this.fk = fkArray.toArray(new int[fkArray.size()][]);
		this.dbrel = dbrelArray.toArray(new String[dbrelArray.size()][]);
		this.dbatt = dbattArray.toArray(new String[dbattArray.size()][]);
		
		this.associateDBinfo();
		this.associateDesc();
	}
	
	private void associateDesc()
	{
		int viewNum = this.view.length;
		this.desc = new String[viewNum][viewNum];
		for(int i = 0; i < this.elArray.size(); i++)
		{
			JSONObject el = (JSONObject) this.elArray.get(i);
			String element = (String) el.get("Element");
			if(element.equals("Relationship"))
			{
				ArrayList<JSONObject> lkElArray = getLinkedEl(el);
				for(int j = 0; j < lkElArray.size(); j++)
				{
					JSONObject lk2El = lkElArray.get(j);
					String lk2Element = (String) lk2El.get("Element");
					if(lk2Element.equals("Entity"))
					{
						for(int k = j + 1; k < lkElArray.size(); k++)
						{
							JSONObject lk3El = lkElArray.get(k);
							String lk3Element = (String) lk3El.get("Element");
							if(lk3Element.equals("Entity"))
							{
								String viewName1 = view4Entity(lk2El);
								String viewName2 = view4Entity(lk3El);
								int id1 = getIndex(this.view, viewName1);
								int id2 = getIndex(this.view, viewName2);
								this.desc[id1][id2] = this.desc[id2][id1] = "associates";// use general word "associates"
							}
						}
					}
				}
			}
		}
	}
	
	private void associateDBinfo()
	{
		this.text = new int[this.view.length][];
		this.output = new int[this.view.length][];
		this.verify = new int[this.view.length][];
		for(int i = 0; i < this.view.length; i++)
		{
			ArrayList<Integer> textArray = new ArrayList<Integer>();
			ArrayList<Integer> outputArray = new ArrayList<Integer>();
			ArrayList<Integer> verifyArray = new ArrayList<Integer>();
			for(int j = 0; j < this.att[i].length; j++)
			{
				String relName = this.dbrel[i][j];
				String attName = this.dbatt[i][j];
				Relation rel = this.dbinfo.getRel(relName);
				if(rel.isTextAttr(attName))
				{
					textArray.add(j);
				}
				if(rel.isVerifyAttr(attName))
				{
					verifyArray.add(j);
				}
				if(rel.isOutputAttr(attName))
				{
					outputArray.add(j);
				}
			}
			this.text[i] = toArray(textArray);
			this.output[i] = toArray(outputArray);
			this.verify[i] = toArray(verifyArray);
		}
	}
	
	private int[] toArray(ArrayList<Integer> arrayList)
	{
		int len = arrayList.size();
		int[] array = new int[len];
		for(int i = 0; i < len; i++)
		{
			array[i] = arrayList.get(i);
		}
		return  array;
	}
	
	private ArrayList<int[]> fk4Entity(JSONObject el, ArrayList<String> viewArray, ArrayList<String[]> attArray)
	{
		ArrayList<int[]> fkArray = new ArrayList<int[]>();
		ArrayList<JSONObject> lkElArray = getLinkedEl(el);
		for(int i = 0; i < lkElArray.size(); i++)
		{
			JSONObject lkEl = lkElArray.get(i);
			String lkElement = (String) lkEl.get("Element");
			if(lkElement.equals("Relationship") && countCard(lkEl) == 1 && getCard(el, lkEl).equals("N"))
			{
				ArrayList<JSONObject> lk2ElArray = getLinkedEl(lkEl);
				for(int j = 0; j < lk2ElArray.size(); j++)
				{
					JSONObject lk2El = lk2ElArray.get(j);
					if(lk2El == el)
						continue;
					String lk2Element = (String) lk2El.get("Element");
					if(lk2Element.equals("Entity"))
					{
						ArrayList<JSONObject> lk3ElArray = getLinkedEl(lk2El);
						for(int k = 0; k < lk3ElArray.size(); k++)
						{
							JSONObject lk3El = lk3ElArray.get(k);
							String lk3Element = (String) lk3El.get("Element");
							if(lk3Element.equals("Key"))
							{
								String viewName = view4Entity(el);
								String refViewName = view4Entity(lk2El);
								String keyName = (String) lk3El.get("Name");
								int viewId = getIndex(viewArray, viewName);
								int fkId = getIndex(attArray.get(viewId), keyName);
								int refViewId = getIndex(viewArray, refViewName);
								int keyId = getIndex(attArray.get(refViewId), keyName);
								fkArray.add(new int[]{viewId, fkId});
								fkArray.add(new int[]{refViewId, keyId});
								break;
							}
						}
					}
				}
			}
		}
		return fkArray;
	}
	
	private ArrayList<int[]> fk4Relationship(JSONObject el, ArrayList<String> viewArray, ArrayList<String[]> attArray)
	{
		ArrayList<int[]> fkArray = new ArrayList<int[]>();
		ArrayList<JSONObject> lkElArray = getLinkedEl(el);
		for(int i = 0; i < lkElArray.size(); i++)
		{
			JSONObject lkEl = lkElArray.get(i);
			String lkElement = (String) lkEl.get("Element");
			if(lkElement.equals("Entity"))
			{
				ArrayList<JSONObject> lk2ElArray = getLinkedEl(lkEl);
				for(int j =  0; j < lk2ElArray.size(); j++)
				{
					JSONObject lk2El = lk2ElArray.get(j);
					String lk2Element = (String) lk2El.get("Element");
					if(lk2Element.equals("Key"))
					{
						String viewName = view4Relationship(el);
						String refViewName = view4Entity(lkEl);
						String keyName = (String) lk2El.get("Name");
						int viewId = getIndex(viewArray, viewName);
						int fkId = getIndex(attArray.get(viewId), keyName);
						int refViewId = getIndex(viewArray, refViewName);
						int keyId = getIndex(attArray.get(refViewId), keyName);
						fkArray.add(new int[]{viewId, fkId});
						fkArray.add(new int[]{refViewId, keyId});
						break;
					}
				}
			}
		}
		return fkArray;
	}
	
	private ArrayList<int[]> fk4Multivalued(JSONObject el, ArrayList<String> viewArray, ArrayList<String[]> attArray)
	{
		ArrayList<int[]> fkArray = new ArrayList<int[]>();
		ArrayList<JSONObject> lkElArray = getLinkedEl(el);
		JSONObject lkEl = lkElArray.get(0);
		ArrayList<JSONObject> lk2ElArray = getLinkedEl(lkEl);
		for(int j = 0; j < lk2ElArray.size(); j++)
		{
			JSONObject lk2El = lk2ElArray.get(j);
			String lk2Element = (String) lk2El.get("Element");
			if(lk2Element.equals("Key"))
			{
				String viewName = view4Multivalued(el);
				String refViewName = view4Entity(lkEl);
				String keyName = (String) lk2El.get("Name");
				int viewId = getIndex(viewArray, viewName);
				int fkId = getIndex(attArray.get(viewId), keyName);
				int refViewId = getIndex(viewArray, refViewName);
				int keyId = getIndex(attArray.get(viewId), keyName);
				fkArray.add(new int[]{viewId, fkId});
				fkArray.add(new int[]{refViewId, keyId});
				break;
			}
		}
		return fkArray;
	}
	
	private int getIndex(ArrayList<String> array, String str)
	{
		for(int i = 0; i < array.size(); i++)
		{
			if(array.get(i).equals(str))
			{
				return i;
			}
		}
		return -1;
	}
	
	private int getIndex(String[] array, String str)
	{
		for(int i = 0; i < array.length; i++)
		{
			if(array[i].equals(str))
			{
				return i;
			}
		}
		return -1;
	}
	
	private String view4Entity(JSONObject el)
	{
		return (String) el.get("Name");
	}
	
	private String view4Relationship(JSONObject el)
	{
		if(countCard(el) > 1)
		{
			return (String) el.get("Name");
		}
		return null;
	}
	
	private String view4Multivalued(JSONObject el)
	{
		ArrayList<JSONObject> lkElArray = getLinkedEl(el);
		if(lkElArray.size() > 0)
		{
			JSONObject lkEl = lkElArray.get(0);
			String lkElement = (String) lkEl.get("Element");
			if(lkElement.equals("Entity")) //only support multivalued attributes of entities
			{
				return (String) lkEl.get("Name") + "_" + (String) el.get("Name");
			}
		}
		return null;
	}
	
	private RelType type4Entity(JSONObject el)
	{
		ArrayList<JSONObject> lkElArray = getLinkedEl(el);
		for(int i = 0; i < lkElArray.size(); i++)
		{
			JSONObject lkEl = lkElArray.get(i);
			String lkElement = (String) lkEl.get("Element");
			if(lkElement.equals("Relationship") && countCard(lkEl) == 1 && getCard(el, lkEl).equals("N"))
			{
				return RelType.Mix;
			}
		}
		return RelType.Object;
	}
	
	private RelType type4Relationship(JSONObject el)
	{
		return RelType.Relationship;
	}
	
	private RelType type4Multivalued(JSONObject el)
	{
		return RelType.Component;
	}
	
	private int[] key4Entity(JSONObject el)
	{
		ArrayList<Integer> keyArray = new ArrayList<Integer>();
		ArrayList<JSONObject> lkElArray = getLinkedEl(el);
		for(int i = 0; i < lkElArray.size(); i++)
		{
			JSONObject lkEl = lkElArray.get(i);
			String lkElement = (String) lkEl.get("Element");
			if(lkElement.equals("Key"))
			{
				keyArray.add(i);
				break;
			}
		}
		
		int[] key = new int[keyArray.size()];
		for(int i = 0; i < keyArray.size(); i++)
		{
			key[i] = keyArray.get(i);
		}
		return key;
	}
	
	private int[] key4Relationship(JSONObject el)
	{
		ArrayList<Integer> keyArray = new ArrayList<Integer>();
		ArrayList<JSONObject> lkElArray = getLinkedEl(el);
		for(int i = 0; i < lkElArray.size(); i++)
		{
			JSONObject lkEl = lkElArray.get(i);
			String lkElement = (String) lkEl.get("Element");
			if(lkElement.equals("Entity") && getCard(el, lkEl).equals("N"))
			{
				ArrayList<JSONObject> lk2ElArray = getLinkedEl(lkEl);
				for(int j =  0; j < lk2ElArray.size(); j++)
				{
					JSONObject lk2El = lk2ElArray.get(j);
					String lk2Element = (String) lk2El.get("Element");
					if(lk2Element.equals("Key"))
					{
						keyArray.add(i);
						break;
					}
				}
			}
		}
		int[] key = new int[keyArray.size()];
		for(int i = 0; i < keyArray.size(); i++)
		{
			key[i] = keyArray.get(i);
		}
		return key;
	}
	
	private int[] key4Multivalued(JSONObject el)
	{
		return new int[] {0,1};
	}
	
	private String[][] att4Entity(JSONObject el)
	{
		ArrayList<String> attArray = new ArrayList<String>();
		ArrayList<String> dbrelArray = new ArrayList<String>();
		ArrayList<String> dbattArray = new ArrayList<String>();
		ArrayList<JSONObject> lkElArray = getLinkedEl(el);
		for(int i = 0; i < lkElArray.size(); i++)
		{
			JSONObject lkEl = lkElArray.get(i);
			String lkElement = (String) lkEl.get("Element");
			if(lkElement.equals("Key") || lkElement.equals("Normal"))
			{
				attArray.add((String) lkEl.get("Name"));
				dbrelArray.add((String) lkEl.get("Relation"));
				dbattArray.add((String) lkEl.get("Attribute"));
			}
			if(lkElement.equals("Relationship") && countCard(lkEl) == 1 && getCard(el, lkEl).equals("N"))
			{
				ArrayList<JSONObject> lk2ElArray = getLinkedEl(lkEl);
				for(int j = 0; j < lk2ElArray.size(); j++)
				{
					JSONObject lk2El = lk2ElArray.get(j);
					if(lk2El == el)
						continue;
					String lk2Element = (String) lk2El.get("Element");
					if(lk2Element.equals("Normal"))
					{
						attArray.add((String) lk2El.get("Name"));
						dbrelArray.add((String) lk2El.get("Relation"));
						dbattArray.add((String) lk2El.get("Attribute"));
					}
					if(lk2Element.equals("Entity"))
					{
						ArrayList<JSONObject> lk3ElArray = getLinkedEl(lk2El);
						for(int k = 0; k < lk3ElArray.size(); k++)
						{
							JSONObject lk3El = lk3ElArray.get(k);
							String lk3Element = (String) lk3El.get("Element");
							if(lk3Element.equals("Key"))
							{
								attArray.add((String) lk3El.get("Name"));
								dbrelArray.add((String) lk3El.get("Relation"));
								dbattArray.add((String) lk3El.get("Attribute"));
								break;
							}
						}
					}
				}
			}
		}
		return new String[][] {attArray.toArray(new String[attArray.size()]), dbrelArray.toArray(new String[dbrelArray.size()]), dbattArray.toArray(new String[dbattArray.size()])};
	}
	
	private String[][] att4Relationship(JSONObject el)
	{
		ArrayList<String> attArray = new ArrayList<String>();
		ArrayList<String> dbrelArray = new ArrayList<String>();
		ArrayList<String> dbattArray = new ArrayList<String>();
		ArrayList<JSONObject> lkElArray = getLinkedEl(el);
		for(int i = 0; i < lkElArray.size(); i++)
		{
			JSONObject lkEl = lkElArray.get(i);
			String lkElement = (String) lkEl.get("Element");
			if(lkElement.equals("Normal"))
			{
				attArray.add((String) lkEl.get("Name"));
				dbrelArray.add((String) lkEl.get("Relation"));
				dbattArray.add((String) lkEl.get("Attribute"));
			}
			if(lkElement.equals("Entity"))
			{
				ArrayList<JSONObject> lklkElArray = getLinkedEl(lkEl);
				for(int j = 0; j < lklkElArray.size(); j++)
				{
					JSONObject lklkEl = lklkElArray.get(j);
					String lklkElement = (String) lklkEl.get("Element");
					if(lklkElement.equals("Key"))
					{
						attArray.add((String) lklkEl.get("Name"));
						dbrelArray.add((String) lklkEl.get("Relation"));
						dbattArray.add((String) lklkEl.get("Attribute"));
						break;//only include one key
					}
				}
			}
		}
		return new String[][] {attArray.toArray(new String[attArray.size()]), dbrelArray.toArray(new String[dbrelArray.size()]), dbattArray.toArray(new String[dbattArray.size()])};
	}
	
	private String[][] att4Multivalued(JSONObject el)
	{
		ArrayList<String> attArray = new ArrayList<String>();
		ArrayList<String> dbrelArray = new ArrayList<String>();
		ArrayList<String> dbattArray = new ArrayList<String>();
		ArrayList<JSONObject> lkElArray = getLinkedEl(el);
		JSONObject lkEl = lkElArray.get(0);
		ArrayList<JSONObject> lklkElArray = getLinkedEl(lkEl);
		for(int i = 0; i < lklkElArray.size(); i++)
		{
			JSONObject lklkEl = lklkElArray.get(i);
			String lklkElement = (String) lklkEl.get("Element");
			if(lklkElement.equals("Key"))
			{
				attArray.add((String) lklkEl.get("Name"));
				dbrelArray.add((String) lklkEl.get("Relation"));
				dbattArray.add((String) lklkEl.get("Attribute"));
				break;//only include one key
			}
		}
		attArray.add((String) el.get("Name"));
		dbrelArray.add((String) el.get("Relation"));
		dbattArray.add((String) el.get("Attribute"));
		return new String[][] {attArray.toArray(new String[attArray.size()]), dbrelArray.toArray(new String[dbrelArray.size()]), dbattArray.toArray(new String[dbattArray.size()])};
	}
	
	private ArrayList<JSONObject> getLinkedEl(JSONObject el)
	{
		ArrayList<JSONObject> elList = new ArrayList<JSONObject>();
		for(int i = 0; i < this.lkArray.size(); i++)
		{
			JSONObject lk = (JSONObject) this.lkArray.get(i);
			int id1 = Integer.parseInt((String) lk.get("Element1"));
			int id2 = Integer.parseInt((String) lk.get("Element2"));
			JSONObject el1 = (JSONObject) this.elArray.get(id1);
			JSONObject el2 = (JSONObject) this.elArray.get(id2);
			if(el1 == el && el2 != el)
			{
				elList.add((JSONObject) this.elArray.get(id2));
			}
			if(el1 != el && el2 == el)
			{
				elList.add((JSONObject) this.elArray.get(id1));
			}
		}
		return elList;
	}
	
	private String getCard(JSONObject el1, JSONObject el2)
	{
		for(int i = 0; i < this.lkArray.size(); i++)
		{
			JSONObject lk = (JSONObject) this.lkArray.get(i);
			int id1 = Integer.parseInt((String) lk.get("Element1"));
			int id2 = Integer.parseInt((String) lk.get("Element2"));
			JSONObject lkEl1 = (JSONObject) this.elArray.get(id1);
			JSONObject lkEl2 = (JSONObject) this.elArray.get(id2);
			if((lkEl1 == el1 && lkEl2 == el2) || (lkEl1 == el2 && lkEl2 == el1))
			{
				return (String) lk.get("Cardinality");
			}
		}
		return "N.A.";
	}
	
	private int countCard(JSONObject el)
	{
		int cdNum = 0;
		for(int i = 0; i < this.lkArray.size(); i++)
		{
			JSONObject lk = (JSONObject) this.lkArray.get(i);
			int id1 = Integer.parseInt((String) lk.get("Element1"));
			int id2 = Integer.parseInt((String) lk.get("Element2"));
			JSONObject el1 = (JSONObject) this.elArray.get(id1);
			JSONObject el2 = (JSONObject) this.elArray.get(id2);
			String cd = (String) lk.get("Cardinality");
			if(el1 == el && el2 != el)
			{
				String element = (String) el2.get("Element");
				if(cd.equals("N") && element.equals("Entity"))
				{
					cdNum++;
				}
			}
			if(el1 != el && el2 == el)
			{
				String element = (String) el1.get("Element");
				if(cd.equals("N") && element.equals("Entity"))
				{
					cdNum++;
				}
			}
		}
		return cdNum;
	}
}
