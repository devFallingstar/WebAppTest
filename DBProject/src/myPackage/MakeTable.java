package myPackage;

import java.util.ArrayList;

public class MakeTable {
	// This is class for making table about all situation(Current,Historical,Nested
	// Historical)
	
	private String makeGeneralSchema(String showTable, ArrayList<Relation> relations) {
		for (Relation relation : relations) {
	         if (relation != null && !relation.getName().equals("null") && !relation.getName().trim().equals("")) {
	            showTable += "<span style =" + "'font-size:15px'" + ">Relation  " + relation.getName() + " =( ";
	            if (relation.getKeyLists().size() != 0) {
	               showTable += "<span style =" + "'color:red; font-size:13px;'" + ">" + "<u>";
	               for (String key : relation.getKeyLists()) {
	                  if (key.equals("Start"))
	                     key = relation.getName() + "-" + key;
	                  else if (key.equals("End"))
	                     key = relation.getName() + "-" + key;
	                  showTable += key + ", ";
	               }
	               showTable = showTable.substring(0, showTable.length() - 2);
	               showTable += "</u></span>" + " , ";
	            }
	            showTable += "<span style =" + "'font-size:13px'" + ">";
	            for (String attr : relation.getAttLists()) {
	               if (attr.equals("Start"))
	                  attr = relation.getName() + "-" + attr;
	               else if (attr.equals("End"))
	                  attr = relation.getName() + "-" + attr;
	               showTable += attr + " , ";
	               
	            }
	            showTable = showTable.substring(0, showTable.length() - 2);
	            showTable += "</span>)</span><br><br>";
	         }
	      }
		return showTable;
	}
	
	private String makeHistoricalSchema(String showTable, ArrayList<Relation> relations) {
		for (Relation relation : relations) {
	         if (relation != null && !relation.getName().equals("null") && !relation.getName().trim().equals("")) {
	            showTable += "<span style =" + "'font-size:15px'" + ">Relation  " + relation.getName() + "_H" + " =( ";
	            if (relation.getKeyLists().size() != 0) {
	               showTable += "<span style =" + "'color:red; font-size:13px;'" + ">" + "<u>";
	               for (String key : relation.getKeyLists()) {
	                  if (key.equals("Start"))
	                     key = relation.getName() + "-" + key;
	                  else if (key.equals("End"))
	                     key = relation.getName() + "-" + key;
	                  showTable += key + ", ";
	               }
	               showTable = showTable.substring(0, showTable.length() - 2);
	               showTable += "</u></span>" + " , ";
	            }
	            showTable += "<span style =" + "'font-size:13px'" + ">";
	            for (String attr : relation.getAttLists()) {
	               if (attr.equals("Start"))
	                  attr = relation.getName() + "-" + attr;
	               else if (attr.equals("End"))
	                  attr = relation.getName() + "-" + attr;
	               showTable += attr + " , ";
	            }
	            showTable = showTable.substring(0, showTable.length() - 2);
	            showTable += "</span>)</span><br><br>";
	         }
	      }
		return showTable;
	}

	private String makeNestedSchema(String showTable, ArrayList<Relation> relations) {
		for (Relation relation : relations) {
			if (relation != null && !relation.getName().equals("null") && !relation.getName().trim().equals("")) {
				showTable += "<span style =" + "'font-size:15px'" + ">Relation  " + relation.getName() + "_N" + " =( ";
				if (relation.getKeyLists().size() != 0) {
					showTable += "<span style =" + "'color:red; font-size:13px;'" + ">" + "<u>";
					for (String key : relation.getKeyLists()) {
						if (key.equals("Start"))
							key = relation.getName() + "-" + key;
						else if (key.equals("End"))
							key = relation.getName() + "-" + key;
						showTable += key + ", ";
					}
					showTable = showTable.substring(0, showTable.length() - 2);
					showTable += "</u></span>" + " , ";
				}
				showTable += "<span style =" + "'font-size:13px'" + ">";
				for (String attr : relation.getAttLists()) {
					if (attr.equals("Start"))
						attr = relation.getName() + "-" + attr;
					else if (attr.equals("End"))
						attr = relation.getName() + "-" + attr;
					showTable += attr + " , ";
				}
				if (relation.getNestedGroup().size() == 0) {
					showTable = showTable.substring(0, showTable.length() - 2);
					showTable += ")<br><br> ";
				} else {
					for (Relation attr : relation.getNestedGroup()) {
						String attrName = attr.getName();
						if (attr.getName().equals("Start"))
							attrName = relation.getName() + "-" + attr.getName();
						else if (attr.equals("End"))
							attrName = relation.getName() + "-" + attr.getName();
						showTable += attrName + " ( ";

						for (Relation attR : relation.getNestedGroup()) {
							if (attr.equals(attR)) {
								if (attR.getNestedGroup().size() == 0) {
									if (attR.getKeyLists().size() != 0) {
										showTable += "<span style =" + "'color:red; font-size:13px;'" + ">" + "<u>";
										for (String att : attR.getKeyLists()) {
											if (att.equals("Start"))
												att = relation.getName() + "-" + att;
											else if (att.equals("End"))
												att = relation.getName() + "-" + att;
											showTable += att + ", ";
										}
										showTable = showTable.substring(0, showTable.length() - 2);
										showTable += "</u></span>" + " , ";
									}
									for (String att : attR.getAttLists()) {
										if (att.equals("Start"))
											att = relation.getName() + "-" + att;
										else if (att.equals("End"))
											att = relation.getName() + "-" + att;
										showTable += att + ", ";
									}
								}
							}
						}
						showTable = showTable.substring(0, showTable.length() - 2);
						showTable += "), ";
					}
					showTable = showTable.substring(0, showTable.length() - 2);
					showTable += ")</span><br><br>";
				}
			}
		}
		return showTable;
	}

	public String makeTable(String jsonTString) {

		String showTable = new String();
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

		// Make the tables for general(flat) schema
		showTable += "<h2 style=" + "'font-family:verdana'" + ">Generate Current Database Schema</h2>";
		showTable = makeGeneralSchema(showTable, relations_general);

		// Make the tables for Historical schema
		showTable += "<h2 style=" + "'font-family:verdana'" + ">Generate Historical Database Schema</h2>";
		showTable = makeHistoricalSchema(showTable, relations_historical);

		// Make the tables for Nested schema
		showTable += "<h2 style=" + "'font-family:verdana'"
				+ ">Generate Nested Relations for Historical Database Schema</h2>";
		showTable = makeNestedSchema(showTable, relations_nested);

		return showTable;
	}

}
