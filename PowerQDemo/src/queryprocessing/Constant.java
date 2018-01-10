package queryprocessing;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Constant
{
	public static final String logfile = "./log";
	
	public static final String homepage = "homepage";
	public static final String graphpage = "graph";
	public static final String firstpage = "keyword";
	public static final String secondpage = "query";
	public static final String thirdpage = "result";
	
	public static final String rootSymbol = "_1_r";
	public static final String hightlightSymbol = "_1_h";
//	public static final String italicFontSymbol = "_i_";
//	public static final String boldFontSymbol = "_b_";
	public static final String kwFontSymbol = "_k_";
	public static final String fnFontSymbol = "_f_";
	public static final String tpFontSymbol = "_p_";
	
	public static final String objectNodeSymbol = "_0_o";
	public static final String relationshipNodeSymbol = "_0_r";
	public static final String mixedNodeSymbol = "_0_m";
	
	public static final String[] function = {"COUNT", "MAX", "MIN", "SUM", "AVG"};
	public static final int countFnIndex = 0;
	public static final String groupby = "GROUPBY";
	
	//Temporal predicate
	public static final String[] Tpredicate = {"Equal","During","Contains","Overlaps","Before","After"};
	public static enum TP {EQUAL, DURING, CONTAINS, OVERLAPS, BEFORE, AFTER};
//	public static final String[] Tpredicate = {"BEFORE","MEETS","DURING","OVERLAPS","STARTS","FINISHES","EQUAL","AFTER","MET_BY","CONTAINS","OVERLAPPED_BY","STARTED_BY","FINISHED_BY"};
//	public static enum TP {BEFORE,MEETS,DURING,OVERLAPS,STARTS,FINISHES,EQUAL,AFTER,MET_BY,CONTAINS,OVERLAPPED_BY,STARTED_BY,FINISHED_BY};
//	
	
//	public static enum TYPE {KW, FN, GP, TP, PD};//keyword, function, groupby, Tpredicate, period
	public static enum TYPE {NULL, KW, META, TP, PD, FN, GP};
	
	
//	public static final String search_id = "_search_id";
	public static final String view = "_view";
	public static final String dbIMDB = "imdb";
	public static final String dbACMDL = "acmdl";
	public static final String dbEmployee = "employees";
	
	public static final int maxNodeNum = 9;
	public static final int alertNodeNum = 3;
	public static final int resultPerPage = 5;
	public static final int rowPerPage = 100;
	
	public static enum Database {Company, ACMDL, IMDB, Employee};
	public static enum NodeType {Object, Relationship, Mix};
	public static enum RelType {Object, Relationship, Mix, Component};
	//Temporal type
	public static enum TemporalType {GeneralRelation,TemporalAttr,TemporalRelation,DateRelation,TemporalObj};
	
	public static final String rootURL = "http://powerq.comp.nus.edu.sg";
//	public static final String rootURL = "http://localhost:8080/PowerQDemo";
	
	
	//pre word type: NULL, KW, META, TP, PD, FN, GP
	public static final boolean[][] KW_TYPE_validation = new boolean[][]{
		{false,true,true,false,true,false,false},//NULL
		{true,true,true,true,true,true,true},//KW
		{true,true,true,true,true,true,true},//META
		{false,true,true,false,true,false,false},//TP
		{true,true,true,true,false,false,true},//PD
		{true,true,true,false,true,true,false},//FN
		{false,true,true,false,true,false,false}//GP
	};
	
	public static final String TYPE_to_String(TYPE type)
	{
		switch(type)
		{
		case NULL:return new String("NULL");
		case KW:return new String("Data-content keyword");
		case META:return new String("Metadata keyword");
		case TP:return new String("Temporal predicate");
		case PD:return new String("Time period");
		case FN:return new String("Aggregateds function");
		case GP:return new String("Group by");
			default:return new String("TYPE not found");
		}
	}
	
	public static final int isFunction(String name)
	{
		int funcNum = function.length;
		for(int i = 0; i < funcNum; i++)
		{
			if(function[i].equalsIgnoreCase(name))
			{
				return i;
			}
		}
		return -1;
	}
	
	public static final boolean isGroupBy(String name)
	{
		if(groupby.equalsIgnoreCase(name))
		{
			return true;
		}
		return false;
	}
	
	//Whether "name" is a temporal predicate. yes - return true
	public static final int isTpredicate(String name)
	{
		int funcNum = Tpredicate.length;
		for(int i = 0; i < funcNum; i++)
		{
			if(Tpredicate[i].equalsIgnoreCase(name))
			{
				return i;
			}
		}
		return -1;
	}
	
	public static final String getDBname(Database choice)
	{
		String dbname = null;
		switch(choice)
		{
			case Employee: 
				dbname = dbEmployee;
				break;
			case IMDB:
				dbname = dbIMDB;
				break;
			case ACMDL: default: 
				dbname = dbACMDL;
				break;
		}
		return dbname;
	}
	
	//get the interpretation of temporal predicate,[A1,A2] from the temporal table, [s,t] from the query. The structure of this array: row0:A1[s,t]; row1:A2[s,t] 
	public static String[][] getTpInterpretation(int choice)
	{
		String[][] tpInterpretation = new String[2][2];
		switch(choice)
		{
		case 0://EQUAL
			tpInterpretation[0][0] = "=";
			tpInterpretation[1][1] = "=";
			break;
		case 1://DURING
			tpInterpretation[0][0] = ">=";
			tpInterpretation[1][1] = "<=";
			break;
		case 2://CONTAINS
			tpInterpretation[0][0] = "<=";
			tpInterpretation[1][1] = ">=";
			break;
		case 3://OVERLAPS
			tpInterpretation[0][1] = "<=";
			tpInterpretation[1][0] = ">=";
			break;
		case 4://BEFORE
			tpInterpretation[1][0] = "<=";
			break;
		case 5://AFTER
			tpInterpretation[0][1] = ">=";
			break;
		default:
			System.out.println("input Tpredicate is invalid.");
			break;
		}
		return tpInterpretation;
	}
	
	//get the interpretation of temporal predicate,[A1,A2] from the temporal table, [s,t] from the query. The structure of this array: row0:A1[s,t]; row1:A2[s,t] 
//	public static String[][] getTpInterpretation(int choice)
//	{
//		String[][] tpInterpretation = new String[2][2];
//		switch(choice)
//		{
//		case 0://BEFORE
//			tpInterpretation[1][0] = "<";
//			break;
//		case 1://MEETS
//			tpInterpretation[1][0] = "=";
//			break;
//		case 2://DURING
//			tpInterpretation[0][0] = ">";
//			tpInterpretation[1][1] = "<";
//			break;
//		case 3://OVERLAPS
//			tpInterpretation[0][0] = "<";
//			tpInterpretation[1][0] = ">";
//			tpInterpretation[1][1] = "<";
//			break;
//		case 4://STARTS
//			tpInterpretation[0][0] = "=";
//			tpInterpretation[1][1] = "<";
//			break;
//		case 5://FINISHES
//			tpInterpretation[0][0] = ">";
//			tpInterpretation[1][1] = "=";
//			break;
//		case 6://EQUAL
//			tpInterpretation[0][0] = "=";
//			tpInterpretation[1][1] = "=";
//			break;
//		case 7://AFTER
//			tpInterpretation[0][1] = ">";
//			break;
//		case 8://MET_BY
//			tpInterpretation[0][1] = "=";
//			break;
//		case 9://CONSTAINS
//			tpInterpretation[0][0] = "<";
//			tpInterpretation[1][1] = ">";
//			break;
//		case 10://OVERLAPPED_BY
//			tpInterpretation[0][0] = ">";
//			tpInterpretation[0][1] = "<";
//			tpInterpretation[1][1] = ">";
//			break;
//		case 11://STARTED_BY
//			tpInterpretation[0][0] = "=";
//			tpInterpretation[1][1] = ">";
//			break;
//		case 12://FINISHED_BY
//			tpInterpretation[0][0] = "<";
//			tpInterpretation[1][1] = "=";
//			break;		
//		default:
//			System.out.println("input Tpredicate is invalid.");
//			break;
//		}
//		return tpInterpretation;
//	}
	
	public static final void writeLog(String message)
	{
		try
		{
			BufferedWriter bw = new BufferedWriter(new FileWriter(logfile, true));
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Calendar cal = Calendar.getInstance();
			bw.write(dateFormat.format(cal.getTime()) + " " + message + "\n");
			bw.close();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	//company database
	public static final String[] company = {
		"Employee", "Project", "Department", "Supplier", "Part", "EmpProj", "SPJ"
	};
	
	public static final String[][] company_attr = {
		{"Eid", "Name", "Salary", "Deptid"},
		{"Jid", "Name", "Budget"},
		{"Deptid", "Name", "Address"},
		{"Sid", "Name", "City"},
		{"Pid", "Name", "Color", "City", "Weight"},
		{"Eid", "Jid", "JoinDate"},
		{"Sid", "Jid", "Pid", "Quantity"},
	};
	
	public static final int[][] company_text = {
		{1}, {1}, {1,2}, {1,2}, {1,2,3}, {}, {}, 
	};
	
	
	public static final int[][] company_attr4output = {
		{0,1,2,3},
		{0,1,2},
		{0,1,2},
		{0,1,2},
		{0,1,2,3,4},
		{0,1,2},
		{0,1,2,3},
	};
	
	public static final int[][] company_attr4verify = {
		{1},
		{1},
		{1},
		{1,2},
		{1,2,3},
		{},
		{},
	};

	public static final int[][] company_key = {
		{0}, {0}, {0}, {0}, {0}, {0,1},{0,1,2},
	};
	
	
	public static final RelType[] company_type = {
		RelType.Mix, RelType.Object, RelType.Object, RelType.Object, RelType.Object, RelType.Relationship, RelType.Relationship
	};
	
	public static final int[][] company_fk = {
		{0,3}, {2,0},
		{5,0}, {0,0},
		{5,1}, {1,0},
		{6,0}, {3,0},
		{6,1}, {2,0},
		{6,2}, {4,0},
	};
	
	public static final String[][] company_desc = {
		{null, "is involved in", "works in", null, null, null, null},
		{"involves", null, null, "is supported by", "uses", null, null},
		{"has", null, null, null, null, null, null},
		{null, "supports", null, null, "supplies", null, null},
		{null, "is used by", null, "is supplied by", null, null, null},
		{null, null, null, null, null, null, null},
		{null, null, null, null, null, null, null},
	};
	
	//employee database
	public static final String[] employee = {
		"employee", "emptitle", "empsalary", "department",  "workfor",
	};
	
	public static final String[][] employee_attr = {
		{"empno", "birthdate", "name", "gender", "hiredate"},
//		{"empno", "title", "from", "to"},
		{"empno", "title", "start", "end"},
//		{"empno", "salary", "from", "to"},
		{"empno", "salary", "start", "end"},
		{"deptno", "name"},
//		{"empno", "deptno", "from", "to"},
		{"empno", "deptno", "start", "end"}, 
	};
	
	public static final int[][] employee_text = {
		{2,3}, {1}, {}, {1},  {},
	};
	
	public static final TemporalType[] employee_temporalType = {
		TemporalType.DateRelation, TemporalType.TemporalAttr, TemporalType.TemporalAttr,TemporalType.GeneralRelation, TemporalType.TemporalRelation, 
	};
	//{table_num,attr_num,from_num, to_num}, "-1"equals to "null"
	public static final int[][] employee_temporalDetail = {
		{0,4,4,4}, {0,1,1,1}, {1,1,2,3}, {2,1,2,3}, {4,-1,2,3},
	};	
	
	public static final int[][] employee_attr4output = {
		{1,2,3,4},
		{},
		{},
		{0,1},
		{},
	};
	
	public static final int[][] employee_attr4verify = {
		{2,4},
		{},
		{},
		{1},
		{},
	};
	
	public static final int[][] employee_key = {
		{0}, {0,1,2}, {0,2}, {0}, {0,1,2},
	};
	
	public static final RelType[] employee_type = {
		RelType.Object, RelType.Component, RelType.Component, RelType.Object, RelType.Relationship
	};
	
	public static final int[][] employee_fk = {
		{1,0}, {0,0},
		{2,0}, {0,0},
		{4,0}, {0,0},
		{4,1}, {3,0},
	};
	
	public static final String[][] employee_desc = {
		{null, null, null, "works for", null},
		{null, null, null, null, null},
		{null, null, null, null, null},
		{"has", null, null, null, null},
		{null, null, null, null, null},
	};
	
	
	//acm database
	public static final String[] acmdl = {
		"paper", "author", "author_aff_history", "editor", "proceeding", "publisher", "write", "edit",
	};
	
	public static final String[][] acmdl_attr = { 
		{"paperid", "procid", "date", "title", "keywords", "pagefrom", "pageto", "doinumber", "url"},
//		{"paperid", "procid", "date", "title", "keywords", "page_from", "page_to", "doinumber", "url"},
		{"authorid", "name"},
		{"authorid", "affiliation"},
		{"editorid", "name"},
		{"procid", "acronym", "title",  "date", "pages", "city", "country", "series",  "isbn13", "publisherid"},
		{"publisherid", "code", "name", "address", "city", "state", "country"},
		{"authorid", "paperid"},
		{"editorid", "procid"},
	};
	
	public static final int[][] acmdl_text = {
		{3}, {1}, {1}, {1}, {1,5,6}, {1,2}, {}, {},
	};
	
	//Temporal relation or not
	public static final TemporalType[] acmdl_temporalType = {
			TemporalType.DateRelation, TemporalType.GeneralRelation, TemporalType.GeneralRelation ,TemporalType.GeneralRelation, TemporalType.DateRelation, TemporalType.GeneralRelation, TemporalType.GeneralRelation, TemporalType.GeneralRelation,
	};
	
	//{table_num,attr_num,from_num, to_num}, "-1"equals to "null"
	public static final int[][] acmdl_temporalDetail = {
			{0,-1,2,2},{4,-1,3,3}
	};	
	
	public static final int[][] acmdl_attr4output = {
		{2,3,5,6,8},
		{1},
		{},
		{1},
		{1,2,3,4,5,6,7,8},
		{1,2,3,4,5,6},
		{},
		{},
	};
	
	public static final int[][] acmdl_attr4verify = {
		{3, 8},
		{1},
		{},
		{1},
		{1,2},
		{2},
		{},
		{},
	};
	
	public static final int[][] acmdl_key = {
		{0}, {0}, {0,1}, {0}, {0}, {0}, {0,1}, {0,1},
	};
	
	public static final RelType[] acmdl_type = {
		RelType.Mix, RelType.Object, RelType.Component, RelType.Object, RelType.Mix, RelType.Object, RelType.Relationship, RelType.Relationship
	};
	
	public static final int[][] acmdl_fk = {
		{0,1}, {4,0},
		{2,0}, {1,0},
		{4,9}, {5,0},
		{6,0}, {1,0},
		{6,1}, {0,0},
		{7,0}, {3,0},
		{7,1}, {4,0},
	};
	
	public static final String[][] acmdl_desc = { 
		{null, "is written by", null, null, "is included in", null, null, null},//0
		{"writes", null, null, null, null, null, null, null},//1
		{null, null, null, null, null, null, null, null},//2
		{null, null, null, null, "edits", null, null, null},//3
		{"includes", null, null, "is editted by", null, "is published by", null, null},//4
		{null, null, null, null, "publishes", null, null, null},//5
		{null, null, null, null, null, null, null, null},//6
		{null, null, null, null, null, null, null, null},//7	
	};
	
	//imdb database
	public static final String[] imdb = {
		"movie", "movie_genre", "movie_language", "cast", "director", "writer", "company", "play", "direct", "write", "produce",
	};
	
	public static final String[][] imdb_attr = { 
		{"movieid", "title", "year", "votes", "rating", "mpaa"},
		{"movieid", "genre"},
		{"movieid", "language"},
		{"castid", "name", "birthdate", "birthplace", "biography", "remark"},
		{"directorid", "name", "birthdate", "birthplace", "biography", "remark"},
		{"writerid", "name", "birthdate", "birthplace", "biography", "remark"},
		{"companyid", "name", "country"},
		{"movieid", "castid"},
		{"movieid", "directorid"},
		{"movieid", "writerid"},
		{"movieid", "companyid"},
	};
	
	public static final int[][] imdb_text = {
		{1}, {1}, {1}, {1}, {1}, {1}, {1}, {}, {}, {}, {},
	};
	
	public static final int[][] imdb_attr4output = {
		{1,2,3,4,5},
		{},
		{},
		{1,2,3,4,5},
		{1,2,3,4,5},
		{1,2,3,4,5},
		{1,2},
		{},
		{},
		{},
		{},
	};
	
	public static final int[][] imdb_attr4verify = {
		{1},
		{},
		{},
		{1},
		{1},
		{1},
		{1},
		{},
		{},
		{},
		{},
	};
	
	public static final int[][] imdb_key = {
		{0}, {0,1}, {0,1}, {0}, {0}, {0}, {0}, {0,1}, {0,1}, {0,1}, {0,1}, 
	};
	
	public static final RelType[] imdb_type = {
		RelType.Object, RelType.Component, RelType.Component, RelType.Object, RelType.Object, RelType.Object, RelType.Object, RelType.Relationship, RelType.Relationship, RelType.Relationship, RelType.Relationship
	};
	
	//Temporal relation or not
	public static final TemporalType[] imdb_temporalType = {
			TemporalType.GeneralRelation, TemporalType.GeneralRelation, TemporalType.GeneralRelation ,TemporalType.GeneralRelation, TemporalType.GeneralRelation, TemporalType.GeneralRelation, TemporalType.GeneralRelation, TemporalType.GeneralRelation, TemporalType.GeneralRelation, TemporalType.GeneralRelation, TemporalType.GeneralRelation
	};
	
	//{table_num,attr_num,from_num, to_num}, "-1"equals to "null"
	public static final int[][] imdb_temporalDetail = {
			
	};	
		
	public static final int[][] imdb_fk = {
		{1,0}, {0,0},
		{2,0}, {0,0},
		{7,0}, {0,0}, 
		{7,1}, {3,0}, 
		{8,0}, {0,0}, 
		{8,1}, {4,0}, 
		{9,0}, {0,0}, 
		{9,1}, {5,0}, 
		{10,0}, {0,0}, 
		{10,1}, {6,0}, 
	};
	public static final String[][] imdb_desc = { 
		{null, null, null, "is played by", "is directed by", "is written by", "is produced by", null, null, null, null},//0
		{null, null, null, null, null, null, null, null, null, null, null},//1
		{null, null, null, null, null, null, null, null, null, null, null},//2
		{"plays in", null, null, null, null, null, null, null, null, null, null},//3
		{"directs", null, null, null, null, null, null, null, null, null, null},//4
		{"writes", null, null, null, null, null, null, null, null, null, null},//5
		{"produces", null, null, null, null, null, null, null, null, null, null},//6
		{null, null, null, null, null, null, null, null, null, null, null},//7
		{null, null, null, null, null, null, null, null, null, null, null},//8
		{null, null, null, null, null, null, null, null, null, null, null},//9
		{null, null, null, null, null, null, null, null, null, null, null},//10
	};
}
