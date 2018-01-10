package queryprocessing;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import queryprocessing.Constant.NodeType;

/**
 * Servlet implementation class DatabaseModel
 */
@WebServlet("/database")
public class DatabaseModel extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public DatabaseModel() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		processRequest(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		processRequest(request, response);
	}

	@SuppressWarnings("unchecked")
	private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
		
		String page = request.getParameter("page").toString().trim();
		
		try
		{
			if(page.equals(Constant.graphpage))
			{
				String dataset = request.getParameter("dataset").toString().trim();
				SearchEngine se = ConnectionPool.getConnection(dataset);
				if(request.getParameter("Save") != null)
				{
					String element = request.getParameter("element").toString().trim();
					String link = request.getParameter("link").toString().trim();
					JSONParser parser = new JSONParser();
					JSONArray er = new JSONArray();
					er.add((JSONArray) parser.parse(element));
					er.add((JSONArray) parser.parse(link));
					se.updateDBinfo(er);
					se.setJSONER(er);
				}
				else
				{
					if(request.getParameter("Restore") != null)
					{
						se.clearView();
					}
					if(!se.useView())
					{
						ORMGraph ormgraph = se.getORMGraph();
						JSONArray db = this.getDBinfo(ormgraph);
						JSONArray er = this.getERinfo(ormgraph);
						se.setJSONDB(db);
						se.setJSONER(er);
					}
				}
				outDatabasePage(out, dataset, se.getJSONDB(), se.getJSONER());
			}
		}
		catch(Exception e)
		{
			String dataset = request.getParameter("dataset").toString().trim();
			SearchEngine se = ConnectionPool.getConnection(dataset);
			se.clearView();
			e.printStackTrace();
			Constant.writeLog(e.getMessage());
			outputErrorPage(out);
		}
		finally
		{
			out.close();
		}
		
	}

	private void outDatabasePage(PrintWriter out, String dataset, JSONArray db, JSONArray er) throws IOException
	{
		out.println("<!DOCTYPE html>");
		out.println("<html>");
		out.println("<head>");
		out.println("  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
		out.println("  <title>PowerQ</title>");
		out.println("  <script src=\"library/jquery-1.9.1.js\"></script>");
		out.println("  <script src=\"library/bootstrap.js\"></script>");
		out.println("  <script src=\"library/joint.js\"></script>");
		out.println("  <script src=\"library/joint.shapes.erd.js\"></script>");
		out.println("  <script src=\"library/joint.layout.DirectedGraph.js\"></script>");
		out.println("  <link rel=\"stylesheet\" href=\"library/bootstrap.css\">");
		out.println("  <link rel=\"stylesheet\" href=\"library/joint.css\">");
		out.println("  <link rel=\"stylesheet\" type=\"text/css\" href=\"scripts/Main.css\">");
		out.println("  <style type=\"text/css\">");
		out.println("   .mgc-toolbar {");
		out.println("     margin:0 0 10px 0;");
		out.println("     height:45px;");
		out.println("     background-image: url(images/toolbarbg.png);");
		out.println("     background-repeat: repeat-x;");
		out.println("   }");
		out.println("   #divPaper {");
		out.println("    height: 74vh;");
		out.println("    width: 100%;");
		out.println("    overflow: auto;");
		out.println("    position: relative;");
		out.println("    background-color: #FFFFFF;");
		out.println("    border: 2px solid #B6C7D8;");
		out.println("   }");
		out.println("   #divTable {");
		out.println("    height: 74vh;");
		out.println("    width: 100%;");
		out.println("    position: relative;");
		out.println("   }");
		out.println("   #divElement {");
		out.println("    height: 48%;");
		out.println("    width: 100%;");
		out.println("    position: absolute;");
		out.println("    right: 0px;");
		out.println("    top: 0px;");
		out.println("    background-color: #FFFFFF;");
		out.println("    border: 2px solid #B6C7D8;");
		out.println("    overflow: auto;");
		out.println("   }");
		out.println("   #divLink {");
		out.println("    height: 48%;");
		out.println("    width: 100%;");
		out.println("    position: absolute;");
		out.println("    right: 0px;");
		out.println("    bottom: 0px;");
		out.println("    background-color: #FFFFFF;");
		out.println("    border: 2px solid #B6C7D8;");
		out.println("    overflow: auto;");
		out.println("   }");
		out.println("  </style>");
		out.println("</head>");
		out.println("<body>");
		out.println("<table width=\"100%\">");
		out.println("  <tr><td><a href=\"" + Constant.rootURL + "/homepage.html\"><img src=\"images/logo2.png\"  border=\"0\" /></a>&nbsp;&nbsp;&nbsp;&nbsp;</td></tr>");
		out.println("  <tr><td><div class=\"mgc-toolbar\"></div></td></tr>");
		out.println("</table>");
		out.println("<table style=\"table-layout: fixed; width:100%\">");
		out.println("  <tr>");
		out.println("    <th bgcolor=\"#D5DDF3\" width=\"50%\" >Database ER Diagram</th>");
		out.println("    <th bgcolor=\"#D5DDF3\" width=\"50%\" >Mappings between Diagram and Schema</th>");
		out.println("  </tr>");
		out.println("  <tr>");
		out.println("    <td>");
		out.println("      <div id=\"divPaper\"></div>");
		out.println("    </td>");
		out.println("    <td>");
		out.println("      <div id=\"divTable\">");
		out.println("        <div id=\"divElement\">");
		out.println("          <span class=\"table-add-el glyphicon glyphicon-plus\"></span>");
		out.println("          <table id=\"tblEl\" class=\"table\">");
		out.println("            <tr>");
		out.println("              <th>#</th>");
		out.println("              <th>Element</th>");
		out.println("              <th>Name</th>");
		out.println("              <th>Relation</th>");
		out.println("              <th>Attribute</th>");
		out.println("              <th></th>");
		out.println("              <th></th>");
		out.println("            </tr>");
		out.println("            <tr class=\"hide\">");
		out.println("              <td class=\"row-id\"></td>");
		out.println("              <td>");
		out.println("                <select>");
		out.println("                  <option value=\"Entity\">Entity</option>");
		out.println("                  <option value=\"Relationship\">Relationship</option>");
		out.println("                  <option value=\"Key\">Key</option>");
		out.println("                  <option value=\"Normal\">Attribute</option>");
		out.println("                  <option value=\"Multivalued\">Multivalued Attribute</option>");
		out.println("                </select>");
		out.println("              </td>");
		out.println("              <td contenteditable=\"true\">undefined</td>");
		out.println("              <td>");
		out.println("                <select class=\"select-select\">");
		
		String tblOpt = "                  <option value=\"N.A.\">N.A.</option>";
		HashMap<String, String> colOptMap = new HashMap<String, String>();
		for(int i = 0; i < db.size(); i++)
		{
			JSONObject tbl = (JSONObject) db.get(i);
			String tblName = (String) tbl.get("Table");
			tblOpt += "\n                  <option value=\"" + tblName + "\">" + tblName + "</option>";
			String colOpt = "                  <option value=\"N.A.\">N.A.</option>";
			JSONArray colArray = (JSONArray) tbl.get("Column");
			for(int j = 0; j < colArray.size(); j++)
			{
				String colName = (String) colArray.get(j);
				colOpt += "\n                  <option value=\"" + colName + "\">" + colName + "</option>";
			}
			colOptMap.put(tblName, colOpt);
		}
		colOptMap.put("N.A.", "                  <option value=\"N.A.\">N.A.</option>");
		
		out.println(tblOpt);
		out.println("                </select>");
		out.println("              </td>");
		out.println("              <td>");
		out.println("                <select class=\"select-update\">");
		out.println("                  <option value=\"N.A.\">N.A.</option>");
		out.println("                </select>");
		out.println("              </td>");
		out.println("              <td>");
		out.println("                <span class=\"table-remove-el glyphicon glyphicon-remove\"></span>");
		out.println("              </td>");
		out.println("              <td>");
		out.println("                <span class=\"table-up-el glyphicon glyphicon-arrow-up\"></span>");
		out.println("                <span class=\"table-down-el glyphicon glyphicon-arrow-down\"></span>");
		out.println("              </td>");
		out.println("            </tr>");
		
		JSONArray elList = (JSONArray) er.get(0);
		String elOpt = "                  <option value=\"Entity\">Entity</option>\n"
				+ "                  <option value=\"Relationship\">Relationship</option>\n"
				+ "                  <option value=\"Key\">Key</option>\n"
				+ "                  <option value=\"Normal\">Attribute</option>\n"
				+ "                  <option value=\"Multivalued\">Multivalued Attribute</option>";
		
		for(int i = 0; i < elList.size(); i++)
		{
			out.println("            <tr>");
			out.println("              <td class=\"row-id\">" + (i + 1) + "</td>");
			out.println("              <td>");
			out.println("                <select>");
			
			JSONObject el = (JSONObject) elList.get(i);
			String elChoice = "\"" + (String) el.get("Element") + "\"";
			
			out.println(elOpt.replace(elChoice, elChoice + " selected"));
			out.println("                </select>");
			out.println("              </td>");
			out.println("              <td contenteditable=\"true\">" + el.get("Name") + "</td>");
			out.println("              <td>");
			out.println("                <select class=\"select-select\">");
			
			String tblName = (String) el.get("Relation");
			String tblChoice = "\"" + tblName + "\"";

			out.println(tblOpt.replace(tblChoice, tblChoice + " selected"));
			out.println("                </select>");
			out.println("              </td>");
			out.println("              <td>");
			out.println("                <select class=\"select-update\">");
			
			String colChoice = "\"" + (String) el.get("Attribute") + "\"";
			String colOpt = colOptMap.get(tblName);
			
			out.println(colOpt.replace(colChoice, colChoice + " selected"));
			out.println("                </select>");
			out.println("              </td>");
			out.println("              <td>");
			out.println("                <span class=\"table-remove-el glyphicon glyphicon-remove\"></span>");
			out.println("              </td>");
			out.println("              <td>");
			out.println("                <span class=\"table-up-el glyphicon glyphicon-arrow-up\"></span>");
			out.println("                <span class=\"table-down-el glyphicon glyphicon-arrow-down\"></span>");
			out.println("              </td>");
			out.println("            </tr>");
		}
		
		out.println("          </table>");
		out.println("        </div>");
		out.println("        <div id=\"divLink\">");
		out.println("          <span class=\"table-add-lk glyphicon glyphicon-plus\"></span>");
		out.println("          <table id=\"tblLk\" class=\"table\">");
		out.println("            <tr>");
		out.println("              <th>#</th>");
		out.println("              <th>Element1</th>");
		out.println("              <th>Element2</th>");
		out.println("              <th>Cardinality</th>");
		out.println("              <th></th>");
		out.println("              <th></th>");
		out.println("            </tr>");
		out.println("            <tr class=\"hide\">");
		out.println("              <td class=\"row-id\"></td>");
		out.println("              <td>");
		out.println("                <select class=\"select-ref\"></select>");
		out.println("              </td>");
		out.println("              <td>");
		out.println("                <select class=\"select-ref\"></select>");
		out.println("              </td>");
		out.println("              <td>");
		out.println("                <select>");
		
		String cdOpt = "                  <option value=\"N.A.\">N.A.</option>\n"
				+ "                  <option value=\"1\">1</option>\n"
				+ "                  <option value=\"N\">N</option>";
		
		out.println(cdOpt);
		out.println("                </select>");
		out.println("              </td>");
		out.println("              <td>");
		out.println("                <span class=\"table-remove-lk glyphicon glyphicon-remove\"></span>");
		out.println("              </td>");
		out.println("              <td>");
		out.println("                <span class=\"table-up-lk glyphicon glyphicon-arrow-up\"></span>");
		out.println("                <span class=\"table-down-lk glyphicon glyphicon-arrow-down\"></span>");
		out.println("              </td>");
		out.println("            </tr>");
		
		String refOpt = "";
		for(int i = 0; i < elList.size(); i++)
		{
			refOpt += "                  <option value=\"" + i + "\">" + (i +1) + "</option>\n";
		}
		
		JSONArray lkList = (JSONArray) er.get(1);
		for(int i = 0; i < lkList.size(); i++)
		{
			out.println("            <tr>");
			out.println("              <td class=\"row-id\">" + (i + 1) + "</td>");
			out.println("              <td>");
			out.println("                <select class=\"select-ref\">");
			
			JSONObject lk = (JSONObject) lkList.get(i);
			int ref1 = Integer.parseInt((String) lk.get("Element1"));
			String refChoice1 = "\"" + ref1 + "\"";
			
			out.println(refOpt.replace(refChoice1, refChoice1 + " selected"));
			out.println("                </select>");
			out.println("              </td>");
			out.println("              <td>");
			out.println("                <select class=\"select-ref\">");
			
			int ref2 = Integer.parseInt((String) lk.get("Element2"));
			String refChoice2 = "\"" + ref2 + "\"";
			
			out.println(refOpt.replace(refChoice2, refChoice2 + " selected"));
			out.println("                </select>");
			out.println("              </td>");
			out.println("              <td>");
			out.println("                <select>");
			
			String cdChoice = "\"" + (String) lk.get("Cardinality") + "\"";
			
			out.println(cdOpt.replace(cdChoice, cdChoice + " selected"));
			out.println("                </select>");
			out.println("              </td>");
			out.println("              <td>");
			out.println("                <span class=\"table-remove-lk glyphicon glyphicon-remove\"></span>");
			out.println("              </td>");
			out.println("              <td>");
			out.println("                <span class=\"table-up-lk glyphicon glyphicon-arrow-up\"></span>");
			out.println("                <span class=\"table-down-lk glyphicon glyphicon-arrow-down\"></span>");
			out.println("              </td>");
			out.println("            </tr>");
		}
		out.println("          </table>");
		out.println("        </div>");
		out.println("      </div>");
		out.println("    </td>");
		out.println("  </tr>");
		out.println("  <tr>");
		out.println("    <td colspan=\"2\" align=\"right\"><br>");
		out.println("      <form action=\"database\" method=\"post\">");
		out.println("        <button id=\"btnDraw\" type=\"button\" class=\"btn btn-primary\">Draw Diagram</button>");
		out.println("        <input type=\"hidden\" name=\"dataset\" value=" + dataset + ">");
		out.println("        <input type=\"hidden\" name=\"page\" value=\"graph\">");
		out.println("        <input type=\"hidden\" name=\"element\" value=\"\">");
		out.println("        <input type=\"hidden\" name=\"link\" value=\"\">");
		out.println("        <input name=\"Save\" type=\"submit\" value=\"Save\" class=\"btn btn-primary\">");
		out.println("        <input name=\"Restore\" type=\"submit\" value=\"Restore\" class=\"btn btn-primary\">");
		out.println("      </form>");
		out.println("    </td>");
		out.println("  </tr>");
		out.println("</table>");
		out.println("<script>");
		StringWriter writer = new StringWriter();
		db.writeJSONString(writer);
		out.println("  var db = " + writer.toString());
		out.println("</script>");
		out.println("<script src=\"scripts/DatabaseModel.js\"></script>");
		out.println("</body>");
		out.println("</html>");
	}
	
	@SuppressWarnings("unchecked")
	private JSONArray getDBinfo(ORMGraph ormgraph)
	{
		JSONArray db = new JSONArray();
		JSONObject obj;
		JSONArray array;
		String tbl = "Table"; String col = "Column";
		for(int i = 0; i < ormgraph.getNodeNum(); i++)
		{
			Node node = ormgraph.getNode(i);
			Relation rel = node.getCoreRelation();
			obj = new JSONObject();
			obj.put(tbl, rel.getRelAlias());
			array = new JSONArray();
			for(int j = 0; j < rel.getAttrNum(); j++)
			{
				array.add(rel.getAttrName(j));
			}
			obj.put(col, array);
			db.add(obj);
			
			for(int j = 0; j < node.getCompRelNum(); j++)
			{
				Relation comprel = node.getCompRelation(j);
				obj = new JSONObject();
				obj.put(tbl, comprel.getRelAlias());
				array = new JSONArray();
				for(int k = 0; k < comprel.getAttrNum(); k++)
				{
					array.add(comprel.getAttrName(k));
				}
				obj.put(col, array);
				db.add(obj);
			}
		}
		return db;
	}
	
	@SuppressWarnings("unchecked")
	private JSONArray getERinfo(ORMGraph ormgraph)
	{
		JSONArray er = new JSONArray();
		JSONArray elList = new JSONArray();
		JSONArray lkList = new JSONArray();
		String entity = "Entity"; 
		String relationship = "Relationship"; 
		String key = "Key"; 
		String normal = "Normal"; 
		String multivalued = "Multivalued";
		String na = "N.A.";
		String one = "1";
		String many = "N";
//		int associateNum = 0;
		
		for(int i = 0; i < ormgraph.getNodeNum(); i++)
		{
			Node node = ormgraph.getNode(i);
			NodeType type = node.getNodeType();
			Relation coreRel = node.getCoreRelation();
			String name = node.getNodeName();
			String rel = coreRel.getRelAlias();
			
			JSONObject obj;
			if(type == NodeType.Relationship)
			{
				obj = createElement(relationship, name, rel, na);
				elList.add(obj);
				for(int j = 0; j < coreRel.getAttrNum(); j++)
				{
					String att = coreRel.getAttrName(j);
					if(!coreRel.isKeyAttr(att))
					{
						JSONObject eclipse = createElement(normal, att, rel, att);
						elList.add(eclipse);
						lkList.add(createLink(elList, obj, eclipse, na));
					}
				}
			}
			else
			{
				obj = createElement(entity, name, rel, na);
				elList.add(obj);
				for(int j = 0; j < coreRel.getAttrNum(); j++)
				{
					String el = normal;
					String att = coreRel.getAttrName(j);
					if(coreRel.isKeyAttr(att))
					{
						el = key;
					}
					if(!coreRel.isFKAttr(att))
					{
						JSONObject eclipse = createElement(el, att, rel, att);
						elList.add(eclipse);
						lkList.add(createLink(elList, obj, eclipse, na));
					}
				}
			}
			
			for(int j = 0; j < node.getCompRelNum(); j++)
			{
				Relation compRel = node.getCompRelation(j);
				String att = null;
				for(int k = 0; k < compRel.getAttrNum(); k++)
				{
					att = compRel.getAttrName(k);
					if(!compRel.isFKAttr(att))//only support non-composite multivalued attributes
					{
						break;
					}
				}
				JSONObject eclipse = createElement(multivalued, att, compRel.getRelAlias(), att);
				elList.add(eclipse);
				lkList.add(createLink(elList, obj, eclipse, na));
			}
		}
		
		for(int i = 0; i < ormgraph.getNodeNum(); i++)
		{
			Node fromNode = ormgraph.getNode(i);
			NodeType fromType = fromNode.getNodeType();
			int[] outEdges = ormgraph.getOutEdges(i);
			String fromName = fromNode.getNodeName();
			int fromIndex = getJSONObjIndex(elList, fromName);
			
			if(fromType == NodeType.Relationship)
			{
				for(int j = 0; j < outEdges.length; j++)
				{
					Node toNode = ormgraph.getNode(outEdges[j]);
					String toName = toNode.getNodeName();
					int toIndex = getJSONObjIndex(elList, toName);
					if(j == 0)
					{
						lkList.add(createLink(toIndex, fromIndex, many));
					}
					else
					{
						lkList.add(createLink(fromIndex, toIndex, many));
					}
					
				}
			}
			else if(fromType == NodeType.Mix)
			{
				for(int j = 0; j < outEdges.length; j++)
				{
					Node toNode = ormgraph.getNode(outEdges[j]);
					String toName = toNode.getNodeName();
					int toIndex = getJSONObjIndex(elList, toName);
//					JSONObject diamond = createElement(relationship, ("associate" + (++associateNum)), na, na);
					JSONObject diamond = createElement(relationship, "associate", na, na);
					elList.add(diamond);
					int index = elList.lastIndexOf(diamond);
					lkList.add(createLink(fromIndex, index, many));
					lkList.add(createLink(index, toIndex, one));
				}
			}
		}
		er.add(elList);
		er.add(lkList);
		return er;
	}
	
	private int getJSONObjIndex(JSONArray elList, String name)
	{
		for(int i = 0; i < elList.size(); i++)
		{
			JSONObject obj = (JSONObject) elList.get(i);
			if(obj.get("Name").equals(name))
			{
				return i;
			}
		}
		return -1;
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject createElement(String el, String name, String rel, String att)
	{
		JSONObject obj = new JSONObject();
		obj.put("Element", el);
		obj.put("Name", name);
		obj.put("Relation", rel);
		obj.put("Attribute", att);
		return obj;
	}
	
	private JSONObject createLink(JSONArray elList, JSONObject obj1, JSONObject obj2, String cd)
	{
		int el1 = elList.indexOf(obj1);
		int el2 = elList.indexOf(obj2);
		return createLink(el1, el2, cd);
	}
	
	@SuppressWarnings("unchecked")
	private JSONObject createLink(int el1, int el2, String cd)
	{
		JSONObject obj = new JSONObject();
		obj.put("Element1", Integer.toString(el1));
		obj.put("Element2", Integer.toString(el2));
		obj.put("Cardinality", cd);
		return obj;
	}
	
	private void outputErrorPage(PrintWriter out)
	{
		out.println("<!DOCTYPE html>");
		out.println("<html>");
		out.println("<head>");
		out.println("	<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
		out.println("	<title>PowerQ</title>");
		out.println("	<style type=\"text/css\">");
		out.println("	td,th {");
		out.println("	  font-family: Arial, Helvetica, sans-serif;");
		out.println("     font-size: 14px;");
		out.println("   }");
		out.println("  .mgc-toolbar {");
		out.println("  margin:0 0 10px 0;");
		out.println("  height:45px;");
		out.println("  background-image: url(images/toolbarbg.png);");
		out.println("  background-repeat: repeat-x;");
		out.println("  }");
		out.println("  </style>");
		out.println("  <link rel=\"stylesheet\" type=\"text/css\" href=\"scripts/Main.css\" />");
		out.println("</head>");
		out.println("<body>");
		out.println("<table width=\"100%\">");
		out.println("  <tr>");
		out.println("    <td><a href=\"" + Constant.rootURL + "/homepage.html\"><img src=\"images/logo2.png\"  border=\"0\" /></a>&nbsp;&nbsp;&nbsp;&nbsp;</td></tr>");
		out.println("  <tr><td><div class=\"mgc-toolbar\"></div></td></tr>");
		out.println("</table>");
		out.println("<img src=\"images/error.png\"  border=\"0\" />");
		out.println("</body>");
		out.println("</html>");		
	}
}
