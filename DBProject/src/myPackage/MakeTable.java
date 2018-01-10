package myPackage;

import java.util.ArrayList;

public class MakeTable {
	// This is class for making table about all situation(Current,Historical,Nested
	// Historical)
	private String makeTableRow(String showTable, ArrayList<Relation> relations, String suffix) {
		// Generate Current Tables
		for (Relation relation : relations) {
			if (relation != null && !relation.getName().equals("null") && !relation.getName().trim().equals("")) {
				if (suffix.trim().equals("F")) {
					showTable += "<table><caption>" + "Relation : " + relation.getName() + "</caption><tr>";
				} else {
					showTable += "<table><caption>" + "Relation : " + relation.getName() + "_" + suffix
							+ "</caption><tr>";
				}

				if (relation.getKeyLists().size() != 0) {
					showTable += "<td><u>";
					for (String key : relation.getKeyLists()) {
						if (key.trim().equals("Start") || key.trim().equals("End")) {
							key = relation.getName() + "_" + key;
						}
						showTable += key + " |";
					}
					showTable = showTable.substring(0, showTable.length() - 1);
					showTable += "</u></td>";
				}
				for (String attr : relation.getAttLists()) {
					if (attr.trim().equals("Start") || attr.trim().equals("End")) {
						attr = relation.getName() + "_" + attr;
					}
					showTable += "<td>" + attr + "</td>";
				}
				showTable += "</tr></table><br><br>";
			}
		}
		return showTable;
	}

	private String makeNestedTableRow(String showTable, ArrayList<Relation> relations) {
		for (Relation relation : relations) {
			if (relation != null && !relation.getName().equals("null") && !relation.getName().trim().equals("")) {
				showTable += "<br><table><caption>" + "Relation : " + relation.getName() + "_N</caption><tr>";
				if (relation.getKeyLists().size() != 0) {
					showTable += "<td rowspan='3'><u>";
					for (String key : relation.getKeyLists()) {
						if (key.trim().equals("Start") || key.trim().equals("End")) {
							key = relation.getName() + "_" + key;
						}
						showTable += key + " |";
					}
					showTable = showTable.substring(0, showTable.length() - 1);
					showTable += "</u></td>";
				}
				for (String attr : relation.getAttLists()) {
					if (attr.trim().equals("Start") || attr.trim().equals("End")) {
						attr = relation.getName() + "_" + attr;
					}
					showTable += "<td  rowspan='3'>" + attr + "</td>";
				}

				for (Relation attr : relation.getNestedGroup()) {
					if (attr.getNestedGroup().size() != 0)
						showTable += "<td colspan='" + (1 + attr.getAttLists().size() + attr.getNestedGroup().size())
								+ "'>" + attr.getName() + "</td>";
					else
						showTable += "<td colspan='" + (1 + attr.getAttLists().size()) + "'>" + attr.getName() + ""
								+ "</td>";

					System.out.println("Number : "
							+ (attr.getKeyLists().size() + attr.getAttLists().size() + attr.getNestedGroup().size()));
				}
				showTable += "</tr><tr>";
				for (Relation attr : relation.getNestedGroup()) {
					if (attr.getNestedGroup().size() == 0) {
						if (attr.getKeyLists().size() != 0) {
							showTable += "<td rowspan='2'><u>";
							for (String att : attr.getKeyLists()) {
								if (att.trim().equals("Start") || att.trim().equals("End")) {
									att = relation.getName() + "_" + att;
								}
								showTable += att + " |";
							}
							showTable = showTable.substring(0, showTable.length() - 1);
							showTable += "</u></td>";
						}
						for (String att : attr.getAttLists()) {
							if (att.trim().equals("Start") || att.trim().equals("End")) {
								att = relation.getName() + "_" + att;
							}
							showTable += "<td rowspan='2'>" + att + "</td>";
						}
					}
				}
				for (Relation attr : relation.getNestedGroup()) {
					if (attr.getNestedGroup().size() != 0) {
						if (attr.getKeyLists().size() != 0) {
							showTable += "<td rowspan='2'><u>";
							for (String att : attr.getKeyLists()) {
								if (att.trim().equals("Start") || att.trim().equals("End")) {
									att = relation.getName() + "_" + att;
								}
								showTable += att + " |";
							}
							showTable = showTable.substring(0, showTable.length() - 1);
							showTable += "</u></td>";
						}
						for (String att : attr.getAttLists()) {
							if (att.trim().equals("Start") || att.trim().equals("End")) {
								att = relation.getName() + "_" + att;
							}
							showTable += "<td rowspan='2'>" + att + "</td>";
						}
						for (Relation tmp : attr.getNestedGroup()) {
							showTable += "<td>" + tmp.getName() + "</td>";
						}
						showTable += "</tr><tr>";
						for (Relation tmp : attr.getNestedGroup()) {
							if (tmp.getKeyLists().size() != 0) {
								showTable += "<td rowspan='2'><u>";
								for (String att : tmp.getKeyLists()) {
									if (att.trim().equals("Start") || att.trim().equals("End")) {
										att = relation.getName() + "_" + att;
									}
									showTable += att + " |";
								}
								showTable = showTable.substring(0, showTable.length() - 1);
								showTable += "</u></td>";
							}
						}
					}
				}
				showTable += "</tr></table>";
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
		showTable += "<h2 style=" + "'font-family:verdana'" + ">Generate Current Database Schema<h2>";
		showTable = makeTableRow(showTable, relations_general, "F");

		// Make the tables for Historical schema
		showTable += "<h2 style=" + "'font-family:verdana'" + ">Generate Historical Database Schema<h2>";
		showTable = makeTableRow(showTable, relations_historical, "H");

		// Make the tables for Nested schema
		showTable += "<h2 style=" + "'font-family:verdana'"
				+ ">Generate Nested Relations for Historical Database Schema<h2>";
		showTable = makeNestedTableRow(showTable, relations_nested);

		return showTable;
	}

}
