package queryprocessing;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class ResultRetrieval
 */
@WebServlet("/result")
public class ResultRetrieval extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ResultRetrieval() {
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
	
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		
		//obtain session
		HttpSession session = request.getSession();
		
		String page = request.getParameter("page").toString().trim();
		
		try
		{
			if(page.equals(Constant.thirdpage))
			{
				Keyword[] kwArray = (Keyword[]) session.getAttribute("kwArray");
				Function[] fnArray = (Function[]) session.getAttribute("fnArray");
				Groupby[] gpArray = (Groupby[]) session.getAttribute("gpArray");
				Tpredicate[] tpArray = (Tpredicate[]) session.getAttribute("tpArray");
				String dataset = (String) session.getAttribute("dataset");
				String query = (String) session.getAttribute("query");
				PGraph[] patternArray = (PGraph[]) session.getAttribute("patternArray");
				
				SearchEngine se = ConnectionPool.getConnection(dataset);
				ORMGraph ormgraph = se.getORMGraph();
				DBinfo dbinfo = se.getDBinfo();
				boolean aggQuery;
				if(fnArray.length == 0 && gpArray.length == 0)
				{
					aggQuery = false;
				}
				else
				{
					aggQuery = true;
				}
				String pageString = request.getParameter("pageIndex");
				if(pageString == null)
				{
					String desc = request.getParameter("desc").toString().trim();
					int patternIndex = Integer.parseInt(request.getParameter("pattern").toString().trim());
					PGraph pgraph = patternArray[patternIndex];
					Constant.Database DBtype = se.getDBtype();
					SQLUnit sqlUnit;
					if (DBtype == Constant.Database.Employee)
					{
						sqlUnit = Translator.getSQLUnit(pgraph, ormgraph, kwArray, fnArray, gpArray, tpArray, dbinfo);
					}
					else
					{
						sqlUnit = Translator.getSQLUnitNoExist(pgraph, ormgraph, kwArray, fnArray, gpArray, tpArray, dbinfo);
//						sqlUnit = Translator.getSQLUnit(pgraph, ormgraph, kwArray, fnArray, gpArray, tpArray, dbinfo);
					}
					
					ResultSet rs = (ResultSet) session.getAttribute("resultset");
					if(rs != null)
					{
						rs.close();
					}
//					ResultSet rsNum = se.getSQLResult(sqlUnit.getCountSQL());
					rs = se.executeQuery(sqlUnit.getSQL());
					
					if(rs != null)
					{
//						rsNum.next();
						int rowNum = getNumOfRows(rs);
//						rsNum.close();
						
						if(rowNum > 0)
						{
							session.setAttribute("desc", desc);
							session.setAttribute("rowNum", rowNum);
							session.setAttribute("resultset", rs);
							session.setAttribute("sqlUnit", sqlUnit);
							session.setAttribute("pgraph", pgraph);
							outputResultPage(out, query, desc, rs, pgraph, ormgraph, sqlUnit, rowNum, 1, aggQuery);
						}
						else
						{
							output404Page(out);
						}
					}
					else
					{
						outputTimeExceedPage(out);
					}
				}
				else
				{
					String desc = (String) session.getAttribute("desc");
					int rowNum = (int) session.getAttribute("rowNum");
					ResultSet rs = (ResultSet) session.getAttribute("resultset");
					SQLUnit sqlUnit = (SQLUnit) session.getAttribute("sqlUnit");
					PGraph pgraph = (PGraph) session.getAttribute("pgraph");
					outputResultPage(out, query, desc, rs, pgraph, ormgraph, sqlUnit, rowNum, Integer.parseInt(pageString.toString().trim()), aggQuery);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			Constant.writeLog(e.getMessage());
			outputErrorPage(out);
		}
		finally
		{
			out.close();
		}
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
	
	private void outputResultPage(PrintWriter out, String query, String queryDesc, ResultSet rs, PGraph pgraph, ORMGraph ormgraph, SQLUnit sqlUnit, int rowNum, int page, boolean aggQuery) throws SQLException 
	{
		out.println("<!DOCTYPE html>");
		out.println("<html>");
		out.println("<head>");
		out.println("	<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
		out.println("	<title>PowerQ+</title>");
		out.println("	<style type=\"text/css\">");
		out.println("	td,th {");
		out.println("	  font-family: Arial, Helvetica, sans-serif;");
		out.println("     font-size: 14px;");
		out.println("   }");
		out.println("   img {");
		out.println("     border: 0;");
		out.println("   }");
		out.println("   #resultDiv {");
		out.println("    left: 2%;");
		out.println("    width: 96%;");
		out.println("    height: 80vh;");
		out.println("    overflow: auto;");
		out.println("    position: relative;");
		out.println("   }");
		out.println("   #resultTbl {");
		out.println("	 border-collapse:collapse;");
		out.println("    padding: 1px;");
		out.println("    left: 0px;");
		out.println("    top: 0px;");
		out.println("    width: 100%;");
		out.println("    position: absolute;");
		out.println("   }");
		out.println("  .mgc-toolbar {");
		out.println("    margin:0 0 10px 0;");
		out.println("    height:45px;");
		out.println("    background-image: url(images/toolbarbg.png);");
		out.println("    background-repeat: repeat-x;");
		out.println("  }");
		out.println("  .STYLE1 {");
		out.println("    color: #CC0033;");
		out.println("    font-weight: bold;");
		out.println("  }");
		out.println("  .STYLE2 {");
		out.println("    color: #0099CC;");
		out.println("    font-weight: bold;");
		out.println("  }");
		out.println("  .STYLE3 {");
		out.println("    color: #996600;");
		out.println("    font-weight: bold;");
		out.println("  }");
		out.println("  .STYLE6 {");
		out.println("    color: #FF6600;");
		out.println("    font-weight: bold;");
		out.println("  }");
		out.println("  .STYLE7 {font-family: \"Courier New\", Courier, monospace;}");
		out.println("  .STYLE4 {background-color: #FFFFFF;  border: 2px solid #B6C7D8;}");
		out.println("  .STYLE5 {background-color: #FFFFFF;  border: 1px solid #B6C7D8;}");
		out.println("  </style>");
		out.println("  <script src=\"library/jquery-1.9.1.js\"></script>");
		out.println("  <script src=\"library/jquery-ui.js\"></script>");
		out.println("  <link rel=\"stylesheet\" href=\"library/jquery-ui.css\" />");
		out.println("  <link rel=\"stylesheet\" type=\"text/css\" href=\"scripts/Main.css\" />");
		out.println("</head>");
		out.println("  <script type=\"text/javascript\">");
		out.println("    function navPage(p)");
		out.println("    {");
		out.println("      var addr = \"" + Constant.rootURL + "/result?page=result&pageIndex=\" + p;");
		out.println("      window.location = addr;");
		out.println("    }");
		out.println("    function whyDialog(n)");
		out.println("    {");
		out.println("      var div = \"#dialog\" + n;");
		out.println("      $(div).dialog({");
		out.println("        show: {");
		out.println("          effect: \"blind\",");
		out.println("          duration: 200");
		out.println("        },");
		out.println("        hide: {");
		out.println("          effect: \"blind\",");
		out.println("          duration: 200");
		out.println("        },");
		out.println("        resizable: true,");
		out.println("        height: 320,");
		out.println("        width: 600,");
		out.println("        modal: true,");
		out.println("        buttons: {");
		out.println("          'OK': function() {");
		out.println("            $(this).dialog('close');");
		out.println("          }");
		out.println("        }");
		out.println("      });");
		out.println("    }");
		out.println("  </script>");
		out.println("<body>");
		out.println("<table width=\"100%\">");
		out.println("  <tr>");
		out.println("    <td><a href=\"" + Constant.rootURL + "/homepage.html\"><img src=\"images/logo2.png\"  border=\"0\" /></a>&nbsp;&nbsp;&nbsp;&nbsp;</td></tr>");
		out.println("  <tr><td><div class=\"mgc-toolbar\"></div></td></tr>");
		out.println("</table>");
		out.println("<table  width=\"100%\">");
		out.println("  <tr>");
		out.println("    <td width=\"128\" valign=\"top\"><img src=\"images/paper.png\" width=\"128\" height=\"128\"></td>");
		out.println("    <td rowspan=\"2\">");
		out.println("      <table width=\"100%\" class=\"STYLE4\" cellspacing = \"0\" >");
		out.println("        <tr><td><p class=\"STYLE7\">Your keyword query is <strong>" + query + "</strong></p>");
		out.println("          <p class=\"STYLE7\">The interpretation is <strong>" + queryDesc + "</strong></p>");
		int stIndex = (page - 1) * Constant.rowPerPage;
		int range = page * Constant.rowPerPage  < rowNum ?  Constant.rowPerPage : (rowNum - (page - 1) * Constant.rowPerPage);
		out.println("          <p class=\"STYLE7\"> The result (<b>" + (stIndex + 1) + "-" + (stIndex + range) + " of " + rowNum + "</b>) is below :</p><br>");
		out.println("          <div id=\"resultDiv\" class=\"STYLE4\">");
		out.println("          	<table id=\"resultTbl\" cellpadding=\"10\">");
		ResultSetMetaData rsmd = rs.getMetaData();
		int colNum = rsmd.getColumnCount();
		int outputNum = sqlUnit.getOuputCol();
		out.println("              <tr>");
		out.println("                <th  align=\"left\" class=\"STYLE5\">#</th>");
		for(int i = 1; i <= outputNum; i++)
		{
			out.println("                <th  align=\"left\" class=\"STYLE5\">" + rsmd.getColumnName(i) + "</th>");
		}
		if(colNum > outputNum)
		{
			out.println("                <th align=\"left\" class=\"STYLE5\">&nbsp;&nbsp;&nbsp;</th>");
		}
		out.println("              </tr>");
		
		if(stIndex == 0)
		{
			rs.beforeFirst();
		}
		else
		{
			rs.absolute(stIndex);
		}
		for(int i = 0; i < range; i++)
		{
			rs.next();
			out.println("            	<tr>");
			out.println("                <td nowrap class=\"STYLE5\">" + (stIndex + 1 + i) + "</td>");
			for(int j = 1; j <= outputNum; j++)
			{
				if(rsmd.getColumnName(j).equals("url"))
				{
					out.println("                <td nowrap class=\"STYLE5\"><a href=\"" + rs.getString(j) + "\" target=\"_blank\">" + rs.getString(j) +  "</a></td>");
				}
				else
				{
					out.println("                <td nowrap class=\"STYLE5\">" + rs.getString(j) + "</td>");
				}
			}
			
			if(colNum > outputNum)
			{
				out.println("                <td nowrap class=\"STYLE5\"><a href=\"javascript:whyDialog(" + i + ");\">Verify</a>");
				out.println("                  <div id=\"dialog" + i + "\" title=\"Verify:\" style=\"display: none;\"><p>");
				int colIndex = outputNum + 1;
				int nodeNum = pgraph.getNodeNum();
				for(int j = 0; j < nodeNum; j++)
				{
					PNode node = pgraph.getNode(j);
					if(!node.isTrival())
					{
						boolean hasVerify;
						
						if(!aggQuery && node.getConditionNum() > 0)
						{
							hasVerify = true;
						}
						else if(node.getGroupbyID() || node.getGpNum() > 0)
						{
							hasVerify = true;
						}
						else
						{
							hasVerify = false;
						}
						if(hasVerify)
						{
							int ormNodeId = node.getORMNodeId();
							Node ormNode = ormgraph.getNode(ormNodeId);
							out.println("                    <span class=\"STYLE6\">" + ormNode.getNodeName() + "</span><br>");
							int verifyCol = sqlUnit.getVerifyCol(j);
							for(int k = colIndex; k < colIndex + verifyCol; k++)
							{
								String colName = rsmd.getColumnName(k); 
								if(colName.equals("url"))
								{
									out.println("                    <span class=\"STYLE2\">&nbsp;&nbsp;&nbsp;" + colName + "</span>: <a href=\"" + rs.getString(k) + "\" target=\"_blank\">" + rs.getString(k) + "</a><br>");
								}
								else
								{
									out.println("                    <span class=\"STYLE2\">&nbsp;&nbsp;&nbsp;" + colName + "</span>: " + rs.getString(k) + "<br>");
								}
							}
							out.println("                    <hr/>");
							colIndex += verifyCol;
						}
					}
				}
				out.println("                  </p></div>");
				out.println("                </td>");
			}
			out.println("              </tr>");
		}
		out.println("          	</table>");
		out.println("          </div>");
		out.println("            <br>");
		out.println("            <table border=\"0\" align=\"center\">");
		out.println("              <tr>");
		int stP = (page - 10) > 1 ? (page - 10) : 1;
		int pageNum = (int) Math.ceil(rowNum / (double) Constant.rowPerPage);
		int edP = (page + 10) < pageNum ? (page + 10) : pageNum;
		if(page > 1)
		{
			out.println("                <td><a href=\"javascript:navPage(" + (page - 1) +");\" style=\"text-decoration: none;\">Previous</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>");
		}
		for (int i = stP; i <= edP; i++) 
		{
			if(i == page)
			{
				out.println("                <td><strong>" + page + "</strong></td>");
			}
			else
			{
				out.println("                <td><a href=\"javascript:navPage(" + i + ");\" style=\"text-decoration: none;\">" + i + "</a></td>");
			}
		}
		if(page < pageNum)
		{
			out.println("                <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"javascript:navPage(" + (page + 1) + ");\" style=\"text-decoration: none;\">Next</a></td>");
		}
		out.println("              </tr>");
		out.println("            </table>");
		out.println("        </td></tr>");
		out.println("      </table>");
		out.println("    </td>");
		out.println("  </tr>");
		out.println("  <tr><td valign=\"bottom\"><img src=\"images/print.png\" width=\"128\" height=\"128\"></tr>");
		out.println("</table>");
		out.println("</body>");
		out.println("</html>");
	}
	
	private void outputTimeExceedPage(PrintWriter out)
	{
		out.println("<!DOCTYPE html>");
		out.println("<html>");
		out.println("<head>");
		out.println("	<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
		out.println("	<title>PowerQ+</title>");
		out.println("	<style type=\"text/css\">");
		out.println("	td,th {");
		out.println("	  font-family: Arial, Helvetica, sans-serif;");
		out.println("     font-size: 14px;");
		out.println("   }");
		out.println("  .mgc-toolbar {");
		out.println("    height:45px;");
		out.println("    background-image: url(images/toolbarbg.png);");
		out.println("    background-repeat: repeat-x;");
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
		out.println("<table>");
		out.println("  <tr>");
		out.println("    <td  bgcolor=\"#D5DDF3\">Sorry, the time exceeds limit.</td>");
		out.println("  </tr>");
		out.println("</table>");
		out.println("<hr/>");
		out.println("<img src=\"images/time.jpg\"  border=\"0\" />");
		out.println("</body>");
		out.println("</html>");
		out.println();
		out.println();
	}
	
//	private void outputOverflowPage(PrintWriter out)
//	{
//		out.println("<!DOCTYPE html>");
//		out.println("<html>");
//		out.println("<head>");
//		out.println("	<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
//		out.println("	<title>PowerQ</title>");
//		out.println("	<style type=\"text/css\">");
//		out.println("	td,th {");
//		out.println("	  font-family: Arial, Helvetica, sans-serif;");
//		out.println("     font-size: 14px;");
//		out.println("   }");
//		out.println("  .mgc-toolbar {");
//		out.println("    height:45px;");
//		out.println("    background-image: url(images/toolbarbg.png);");
//		out.println("    background-repeat: repeat-x;");
//		out.println("  }");
//		out.println("  </style>");
//		out.println("  <link rel=\"stylesheet\" type=\"text/css\" href=\"scripts/Main.css\" />");
//		out.println("</head>");
//		out.println("<body>");
//		out.println("<table width=\"100%\">");
//		out.println("  <tr>");
//		out.println("    <td><a href=\"" + Constant.rootURL + "/homepage.html\"><img src=\"images/logo2.png\"  border=\"0\" /></a>&nbsp;&nbsp;&nbsp;&nbsp;</td></tr>");
//		out.println("  <tr><td><div class=\"mgc-toolbar\"></div></td></tr>");
//		out.println("</table>");
//		out.println("<table>");
//		out.println("  <tr>");
//		out.println("    <td  bgcolor=\"#D5DDF3\">Sorry, the result size overflows.</td>");
//		out.println("  </tr>");
//		out.println("</table>");
//		out.println("<hr/>");
//		out.println("<img src=\"images/overflow.jpg\"  border=\"0\" />");
//		out.println("</body>");
//		out.println("</html>");
//		out.println();
//		out.println();
//	}
	
	private void output404Page(PrintWriter out)
	{
		out.println("<!DOCTYPE html>");
		out.println("<html>");
		out.println("<head>");
		out.println("	<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
		out.println("	<title>PowerQ+</title>");
		out.println("	<style type=\"text/css\">");
		out.println("	td,th {");
		out.println("	  font-family: Arial, Helvetica, sans-serif;");
		out.println("     font-size: 14px;");
		out.println("   }");
		out.println("  .mgc-toolbar {");
		out.println("    height:45px;");
		out.println("    background-image: url(images/toolbarbg.png);");
		out.println("    background-repeat: repeat-x;");
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
		out.println("<table>");
		out.println("  <tr>");
		out.println("    <td  bgcolor=\"#D5DDF3\">Sorry, what you search does not exist in the database. Please try some other query interpretations or queries.</td>");
		out.println("  </tr>");
		out.println("</table>");
		out.println("<hr/>");
		out.println("<img src=\"images/404.jpg\"  border=\"0\" />");
		out.println("</body>");
		out.println("</html>");
		out.println();
		out.println();
	}
	
	
	private void outputErrorPage(PrintWriter out)
	{
		out.println("<!DOCTYPE html>");
		out.println("<html>");
		out.println("<head>");
		out.println("	<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">");
		out.println("	<title>PowerQ+</title>");
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
