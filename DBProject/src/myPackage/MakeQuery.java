package myPackage;

import java.util.ArrayList;

//This is class for making query about all situation(Current,Historical,Nested Historical)
public class MakeQuery {

	private String makeQueryForRelation(String showQuery, ArrayList<Relation> relations, String suffix) {
		for (Relation relation : relations) {
			if (suffix.trim().equals("F")) {
				showQuery += "create table " + relation.getName() + "(<br>";
			} else {
				showQuery += "create table " + relation.getName() + "_" + suffix + "(<br>";
			}

			for (String key : relation.getKeyLists()) {
				if (key.trim().equals("Start") || key.trim().equals("End")) {
					key = relation.getName() + "_" + key;
				}
				showQuery += key + " varchar(255),<br>";
			}
			for (String attr : relation.getAttLists()) {
				if (attr.trim().equals("Start") || attr.trim().equals("End")) {
					attr = relation.getName() + "_" + attr;
				}
				showQuery += attr + " varchar(255),<br>";
			}
			showQuery += "primary key(";
			for (String key : relation.getKeyLists()) {
				showQuery += key + ",";
			}
			showQuery = showQuery.substring(0, showQuery.length() - 1);
			showQuery += "));<br><br>";
		}
		return showQuery;
	}

	private String makeQueryForNestedRelation(String showQuery, ArrayList<Relation> relations) {
		for (Relation relation : relations) {
			for (Relation attr : relation.getNestedGroup()) {// first layer group
				showQuery += "create type " + attr.getName() + " as object(<br>";
				for (String key : attr.getKeyLists()) {
					if (key.trim().equals("Start") || key.trim().equals("End")) {
						key = relation.getName() + "_" + key;
					}
					showQuery += key + " varchar(255),<br>";
				}
				for (String att : attr.getAttLists()) {
					if (att.trim().equals("Start") || att.trim().equals("End")) {
						att = relation.getName() + "_" + att;
					}
					showQuery += att + " varchar(255),<br>";
				}
				showQuery += "primary key(";
				for (String key : attr.getKeyLists()) {
					if (key.trim().equals("Start") || key.trim().equals("End")) {
						key = relation.getName() + "_" + key;
					}
					showQuery += key + ",";
				}
				showQuery = showQuery.substring(0, showQuery.length() - 1);
				showQuery += "));<br><br>";
				showQuery += "create type " + attr.getName() + " is table of " + attr.getName() + ";<br><br>";
				if (attr.getNestedGroup().size() != 0) {
					for (Relation tmp : attr.getNestedGroup()) {
						showQuery += "create type " + tmp.getName() + " as object(<br>";
						for (String key : tmp.getKeyLists()) {
							if (key.trim().equals("Start") || key.trim().equals("End")) {
								key = relation.getName() + "_" + key;
							}
							showQuery += key + " varchar(255),<br>";
						}
						for (String att : tmp.getAttLists()) {
							if (att.trim().equals("Start") || att.trim().equals("End")) {
								att = relation.getName() + "_" + att;
							}
							showQuery += att + " varchar(255),<br>";
						}
						showQuery += "primary key(";
						for (String key : tmp.getKeyLists()) {
							showQuery += key + ",";
						}
						showQuery = showQuery.substring(0, showQuery.length() - 1);
						showQuery += "));<br><br>";
						showQuery += "create type " + tmp.getName() + " is table of " + tmp.getName() + ";<br><br>";
					}
				}
			}
			showQuery += "create table " + relation.getName() + " (<br>";
			for (String key : relation.getKeyLists()) {
				if (key.trim().equals("Start") || key.trim().equals("End")) {
					key = relation.getName() + "_" + key;
				}
				showQuery += key + " varchar(255),<br>";
			}
			for (String att : relation.getAttLists()) {
				if (att.trim().equals("Start") || att.trim().equals("End")) {
					att = relation.getName() + "_" + att;
				}
				showQuery += att + " varchar(255),<br>";
			}
			for (Relation attr : relation.getNestedGroup()) {// first group
				showQuery += attr.getName() + " " + attr.getName() + "<br>";
				if (attr.getNestedGroup().size() != 0) {
					for (Relation tmp : attr.getNestedGroup()) {
						showQuery += tmp.getName() + " " + tmp.getName() + "<br>";
					}
				}
			}
			showQuery += "primary key(";
			for (String key : relation.getKeyLists()) {
				if (key.trim().equals("Start") || key.trim().equals("End")) {
					key = relation.getName() + "_" + key;
				}
				showQuery += key + ",";
			}
			showQuery = showQuery.substring(0, showQuery.length() - 1);
			showQuery += "))";

			if (relation.getNestedGroup().size() != 0) {
				showQuery += " nested table ";
				for (Relation attr : relation.getNestedGroup()) {// first layer group
					showQuery += attr.getName() + ", ";
					if (attr.getNestedGroup().size() != 0) {
						for (Relation tmp : attr.getNestedGroup()) {
							showQuery += tmp.getName() + ", ";
						}
					}
				}
				showQuery = showQuery.substring(0, showQuery.length() - 1);
				showQuery += relation.getName() + " N";
			}
			showQuery += ";<br><br>";
		}
		return showQuery;
	}

	// This is a method for making query
	public String makeQuery(String jsonTString) {
		String showQuery = new String();

		ArrayList<Relation> relations_nested = new ArrayList<Relation>();
		ArrayList<Relation> relations_general = new ArrayList<Relation>();
		ArrayList<Relation> relations_historical = new ArrayList<Relation>();

		ArrayList<Node> ERclass = new ER_Maker().getER(jsonTString);
		// Call each algorithm(Current Nested Historical)
		Algorithm_Nested a = new Algorithm_Nested();
		Algorithm_General b = new Algorithm_General();
		Algorithm_Historical c = new Algorithm_Historical();

		relations_nested = a.makingNested(ERclass);
		relations_general = b.makingGeneral(ERclass);
		relations_historical = c.makingHistorical(ERclass);

		// Generate Current Query
		showQuery += "<h2 style=" + "'font-family:verdana'" + ">Generate Current Query<h2>";
		showQuery = makeQueryForRelation(showQuery, relations_general, "F");

		// Generate Historical Query
		showQuery += "<h2 style=" + "'font-family:verdana'" + ">Generate Historical Query<h2>";
		showQuery = makeQueryForRelation(showQuery, relations_historical, "H");

		// Generate Historical Nested Query
		showQuery += "<h2 style=" + "'font-family:verdana'" + ">Generate Nested Relation Query<h2>";
		showQuery = makeQueryForNestedRelation(showQuery, relations_nested);

		return showQuery;
	}
}
