package queryprocessing;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class SearchEnginecall
 */
@WebServlet("/powerq")
public class SearchEnginecall extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchEnginecall() {
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

	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		PrintWriter out = response.getWriter();
		
		//obtain session
		HttpSession session = request.getSession();
		
		
		
//		String xxx = request.getServletContext().getRealPath("/");
		
 		Map<String, String[]> paramsMap = request.getParameterMap();
		for (Map.Entry<String, String[]> entry : paramsMap.entrySet()) {
			System.out.println("++"+entry.getKey());
			for(String value: entry.getValue())
			{
				System.out.println(value);
			}
		}
		
		String page = request.getParameter("page");
		System.out.println(page);
		
//		String page = request.getParameter("page").toString().trim();
		
		try
		{
			if(page == null)
			{
				System.out.println("The parameter 'page' is NULL. Something goes wrongly.\n");
			}
			else if(page.equals(Constant.homepage))
			{
				//retrieve parameters
				String dataset = request.getParameter("dataset").toString().trim();//get dataset
				String query = request.getParameter("searchBox").toString().trim();//get query content
				Constant.writeLog("query: " + query + " dataset: " + dataset);
				session.setAttribute("query", query);
				session.setAttribute("dataset", dataset);
				
				SearchEngine se = ConnectionPool.getConnection(dataset);//connect to database
				ORMGraph ormgraph = se.getORMGraph();//create the ORM graph
				
				QueryUnit queryUnit = se.parseQuery(query);//parse query, get the query unit
				Keyword[] kwArray = queryUnit.getKwArray();//get the kw
				Function[] fnArray = queryUnit.getFnArray();//get the function
				Groupby[] gpArray = queryUnit.getGpArray();//get the group by
				Tpredicate[] tpArray = queryUnit.getTpArray();//get the tp
				InvalidQuery[] iqArray = queryUnit.getIqArray();
				String prePD = queryUnit.getPrePD();
				
				session.setAttribute("kwArray", kwArray);//set the values into the instance "session"
				session.setAttribute("fnArray", fnArray);
				session.setAttribute("gpArray", gpArray);
				session.setAttribute("tpArray", tpArray);
				
				//check the query
				String[] nonExistKw = nonExistCheck(kwArray);//existence of each kw //remain the same
				int nonExistKwNum = nonExistKw.length;
				String [] invalidFn = validFuncCheck(kwArray, fnArray); //remain the same
				int invalidFnNum = invalidFn.length;
				String[] invalidGp = validGroupCheck(kwArray, gpArray); //remain the same
				int invalidGpNum = invalidGp.length;
				//tp //change a little bit
//				String[] invalidTp = validTpCheck(kwArray,tpArray, se.getDBinfo());
//				int invalidTpNum = invalidTp.length;
				//format checking  - which keyword cannot be adjacent to another one
				int invalidQueryNum = iqArray.length;
				
				
				
				//if the query is not valid
				if(nonExistKwNum != 0 || invalidFnNum != 0 || invalidGpNum != 0 || prePD.length()!=0 || invalidQueryNum !=0)
				{
					this.output404Page(out, query, dataset, nonExistKw, invalidFn, invalidGp, iqArray,  prePD);//error page
				}
				else//turn to the first page
				{
					se.updateKwTag(kwArray, fnArray, gpArray);
					this.outputFirstPage(out, kwArray, fnArray, gpArray, tpArray, query, dataset, ormgraph);// turn to the first page,choose the search intention
				}
				
			}
			else if(page.equals(Constant.firstpage))//choose the user intention
			{
				Keyword[] kwArray = (Keyword[]) session.getAttribute("kwArray");//get the values from instance "session"
				Function[] fnArray = (Function[]) session.getAttribute("fnArray");
				Groupby[] gpArray = (Groupby[]) session.getAttribute("gpArray");
				Tpredicate[] tpArray = (Tpredicate[]) session.getAttribute("tpArray");
				String dataset = (String) session.getAttribute("dataset");
				String query = (String) session.getAttribute("query");
				
				SearchEngine se = ConnectionPool.getConnection(dataset);
				ORMGraph ormgraph = se.getORMGraph();
				
				int kwNum = kwArray.length;
				String[][] userChoice = new String[kwNum][];
				for(int i = 0; i < kwNum; i++)//user choice
				{
					userChoice[i] = request.getParameterValues("kw" + i);
				}
				
				PGraph[] patternArray = se.interpretQuery(kwArray, userChoice, fnArray, gpArray, tpArray);//get the pattern array 
				session.setAttribute("patternArray", patternArray);
				
				outputSecondPage(out, patternArray, kwArray, fnArray, gpArray, tpArray, query, dataset, ormgraph, 1);//turn to the second page
			}
			else if(page.equals(Constant.secondpage))//second page, show the ORM graph
			{
				Keyword[] kwArray = (Keyword[]) session.getAttribute("kwArray");//get the data already exists
				Function[] fnArray = (Function[]) session.getAttribute("fnArray");
				Groupby[] gpArray = (Groupby[]) session.getAttribute("gpArray");
				Tpredicate[] tpArray = (Tpredicate[]) session.getAttribute("tpArray");
				String dataset = (String) session.getAttribute("dataset");
				String query = (String) session.getAttribute("query");
				PGraph[] patternArray = (PGraph[]) session.getAttribute("patternArray");
				
				SearchEngine se = ConnectionPool.getConnection(dataset);//connect the database
				ORMGraph ormgraph = se.getORMGraph();
				int pageNum = Integer.parseInt(request.getParameter("pageIndex").toString().trim());// divide into serval pages
				outputSecondPage(out, patternArray, kwArray, fnArray, gpArray, tpArray, query, dataset, ormgraph, pageNum);
			}
		}
		catch(Exception e)
		{
			Constant.writeLog(e.getMessage());
			e.printStackTrace();
			outputErrorPage(out);
		}
		finally
		{
			out.close();
		}
	}


	private String[] validFuncCheck(Keyword[] kwArray, Function[] fnArray)
	{
		boolean hasNestedFn = false;
		ArrayList<String> invalidFnList = new ArrayList<String>();
		int fnNum = fnArray.length;
		for(int i = 0; i < fnNum; i++)//for each FN
		{
			Function fn = fnArray[i];
			String fnName = fn.getFnName();
			if(fn.getKwIndex() != -1)//have some following KW
			{
				int kwIndex = fn.getKwIndex();
				Keyword kw = kwArray[kwIndex];
				String kwStr = kw.isPhrase()? "\"" + kw.getContent() + "\"" : kw.getContent();
				if(!kw.hasAttrTag())
				{
					if(fnName.equals(Constant.function[Constant.countFnIndex]))
					{
						if(!kw.hasNameTag())
						{
							invalidFnList.add(kwStr);
							invalidFnList.add(fnName);
						}
					}
					else
					{
						invalidFnList.add(kwStr);
						invalidFnList.add(fnName);
					}
				}
			}
			else if(fn.getFnIndex() != -1)//?????
			{
				int subFnIndex = fn.getFnIndex();
				Function subFn = fnArray[subFnIndex];
				if(hasNestedFn)//nested aggregate
				{
					invalidFnList.add(subFn.getFnName());
					invalidFnList.add(fnName);
				}
				else//simple aggregate 
				{
					if(!subFn.isSimpleFn())
					{
						invalidFnList.add(subFn.getFnName());
						invalidFnList.add(fnName);
					}
					hasNestedFn = true;
				}
			}
//			else//the FN has no following keyword
//			{
//				if(fn.getInvalidWord().length()!=0)
//				{
//					invalidFnList.add(fn.getInvalidWord());
//				}
//				else
//				{
//					invalidFnList.add("null");
//				}
//				invalidFnList.add(fnName);
//			}
		}
		String[] invalidFnArray = invalidFnList.toArray(new String[invalidFnList.size()]);
		return invalidFnArray;
	}
	
	private String[] validGroupCheck(Keyword[] kwArray, Groupby[] gpArray)//??
	{
		ArrayList<String> invalidGpList = new ArrayList<String>();
		int gpNum = gpArray.length;
		for(int i = 0; i < gpNum; i++)//each "group-by"
		{
			int kwIndex = gpArray[i].getKwIndex();
			if (gpArray[i].getInvalidWord().length()!=0)
			{
				invalidGpList.add(gpArray[i].getInvalidWord());
				continue;
			}
//			if(kwIndex == -1)
//			{
//				invalidGpList.add("null");
//			}
			if(kwIndex != -1)
			{
				Keyword kw = kwArray[kwIndex];
				String kwStr = kw.isPhrase()? "\"" + kw.getContent() + "\"" : kw.getContent();
				if(!kw.hasAttrTag() && !kw.hasNameTag())
				{
					invalidGpList.add(kwStr);
				}
			}
		}
		String[] invalidGpArray = invalidGpList.toArray(new String[invalidGpList.size()]);
		return invalidGpArray;
	}
	
//	private String[] validTpCheck(Keyword[] kwArray, Tpredicate[] tpArray, DBinfo dbinfo)//??
//	{
//		ArrayList<String> invalidTpList = new ArrayList<String>();
//		int tpNum = tpArray.length;
//		//check whether the TP have two time interval as the operands.
//		for(int i = 0; i < tpNum; i++)
//		{
//			int kwIndex = tpArray[i].getKwIndex();
//			
//			
//			
////			if (kwIndex == -1)//this TP is the first word in the query, invalid
////			{
////				if(tpArray[i].getInvalidWord().length()!=0)
////				{
////					invalidTpList.add(tpArray[i].getInvalidWord());
////				}
////				else
////				{
////					invalidTpList.add("null");
////				}
////				invalidTpList.add(tpArray[i].getTpName());
////			}
////			else//Tp is not the first kw
////			{
////				int secKwIndex = tpArray[i].getKwIndexSec();
////				int fromLen = tpArray[i].getFrom().length();
////				if (secKwIndex == -1 && fromLen == 0)//invalid following kw, invalid k(i+1)
////				{
////					invalidTpList.add(tpArray[i].getInvalidWord()); 
////					invalidTpList.add(tpArray[i].getTpName());//needed change
////				}
//				
////				if (secKwIndex != -1)//have the secKw, but the second kw do not indicate a temporal relation, now valid, change the constraint
////				{
////					int tagNum = kwArray[secKwIndex].getTaglistLength();
////					for (int j = 0; j<tagNum; j++)
////					{
////						String relName = kwArray[secKwIndex].getTagName(j);
////						int relNum = dbinfo.getRelNum();
////						int relIndex = -1;
////						for (int k=0;k<relNum; k++)
////						{
////							if (relName.equalsIgnoreCase(dbinfo.getRel(k).getRelName()))
////							{
////								relIndex = k;
////								break;
////							}
////						}
////						if (dbinfo.getTemporalType(relIndex) == Constant.TemporalType.GeneralRelation)//this tag is not a temporal node, delete this tag.
////						{
////							kwArray[secKwIndex].deleteTag(j);
////						}
////					}
////					tagNum = kwArray[secKwIndex].getTaglistLength();
////					if (tagNum == 0)//there is no tag left, return error.
////					{
////						invalidTpList.add(kwArray[secKwIndex].getContent()); 
////						invalidTpList.add(tpArray[i].getTpName());//needed change
////					}
////				}
//			}
//		}
//		String[] invalidTpArray = invalidTpList.toArray(new String[invalidTpList.size()]);
//		return invalidTpArray;
//	}

	private String[] nonExistCheck(Keyword[] kwArray)
	{
		ArrayList<String> nonExistKwList = new ArrayList<String>();
		int kwNum = kwArray.length;
		for(int i = 0; i < kwNum; i++)
		{
			Keyword kw = kwArray[i];
			String kwStr = kw.isPhrase()? "\"" + kw.getContent() + "\"" : kw.getContent();
			int tagListLength = kw.getTaglistLength();
			if(tagListLength == 0)
			{
				nonExistKwList.add(kwStr);
			}
		}
		String[] nonExistKwArray = nonExistKwList.toArray(new String[nonExistKwList.size()]);
		return nonExistKwArray;
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
	/*
	private void output404Page(PrintWriter out, String query, String dataset, String[] nonExistKw, String[] invalidFn, String[] invalidGp)
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
		out.println("  </style>");
		out.println("  <link rel=\"stylesheet\" type=\"text/css\" href=\"scripts/Main.css\" />");
		out.println("</head>");
		out.println("<body>");
		out.println("<table>");
		out.println("  <tr>");
		out.println("    <td><a href=\"" + Constant.rootURL + "/homepage.html\"><img src=\"images/logo2.png\"  border=\"0\" /></a>&nbsp;&nbsp;&nbsp;&nbsp;</td>");
		out.println("    <td>");
		out.println("      <form action=\"powerq\" method=\"\">");
		out.println("       <input name=\"searchBox\" type=\"text\" size=\"50\" value=\"" + query.replace("\"", "&quot;") + "\">");
		out.println("       <input name=\"submit\" type=\"submit\" value=\"Search!\" style=\"height: 25px; width: 100px\">");
		out.println("       <label for=\"dataset\"> DataSet:</label>");
		out.println("       <select name=\"dataset\" >");
		switch(dataset)
		{
//			case "company":
//				out.println("        <option value=\"acmdl\">ACMDL</option>");
//				out.println("        <option value=\"imdb\">IMDB</option>");
//				out.println("        <option value=\"company\" selected>Company</option>");
//				break;
			case "imdb":
				out.println("        <option value=\"acmdl\">ACMDL</option>");
				out.println("        <option value=\"imdb\" selected>IMDB</option>");
//				out.println("        <option value=\"company\">Company</option>");
				break;
			case "acmdl": default:
				out.println("        <option value=\"acmdl\" selected>ACMDL</option>");
				out.println("        <option value=\"imdb\">IMDB</option>");
//				out.println("        <option value=\"company\">Company</option>");
				break;
		}
		out.println("      </select>");
		out.println("      <br>");
		out.println("      (<strong><a href=\"" + Constant.rootURL + "/introduction.html#samplequery\">Sample Queries</a></strong>)");
		out.println("      <input type=\"hidden\" name=\"page\" value=\"homepage\">");
		out.println("    </form>  ");
		out.println("  </td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("<br>");
		out.println("<table>");
		out.println("<tr>");
		
		StringBuffer explainBuf = new StringBuffer("Sorry,");
		int nonExistKwNum = nonExistKw.length;
		if(nonExistKwNum > 0)
		{
			explainBuf.append(" the following keywords");
			for(int i = 0; i < nonExistKwNum; i++)
			{
				explainBuf.append(" \"").append(nonExistKw[i]).append("\"");
			}
			explainBuf.append(" do not exist in the ").append(dataset).append(" database;");
		}
		int invalidFnNum = invalidFn.length;
		if(invalidFnNum > 0)
		{
			explainBuf.append(" the following keywords/functions");
			for(int i = 0; i < invalidFnNum; i += 2)
			{
				explainBuf.append(" \"").append(invalidFn[i]).append("\"");
			}
			explainBuf.append(" can not be applied by");
			for(int i = 0; i < invalidFnNum; i += 2)
			{
				explainBuf.append(" \"").append(invalidFn[i + 1]).append("\"");
			}
			explainBuf.append(";");
		}
		int invalidGpNum = invalidGp.length;
		if(invalidGpNum > 0)
		{
			explainBuf.append(" the following keywords");
			for(int i = 0; i < invalidGpNum; i++)
			{
				explainBuf.append(" \"").append(invalidGp[i]).append("\"");
			}
			explainBuf.append(" can not be applied by \"").append(Constant.groupby).append("\";");
		}
		int buflen = explainBuf.length();
		explainBuf.delete(buflen - 1, buflen);
		explainBuf.append(".");
		
		out.println("  <td  bgcolor=\"#D5DDF3\">" + explainBuf.toString() + " Please try some other queries or choose another dataset.</td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("<hr/>");
		out.println("<img src=\"images/404.jpg\"  border=\"0\" />");
		out.println("</body>");
		out.println("</html>");
	}
	*/
	
	private void output404Page(PrintWriter out, String query, String dataset, String[] nonExistKw, String[] invalidFn, String[] invalidGp, InvalidQuery[] iqArray,String prePD)
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
		out.println("  </style>");
		out.println("  <link rel=\"stylesheet\" type=\"text/css\" href=\"scripts/Main.css\" />");
		out.println("</head>");
		out.println("<body>");
		out.println("<table>");
		out.println("  <tr>");
		out.println("    <td><a href=\"" + Constant.rootURL + "/homepage.html\"><img src=\"images/logo2.png\"  border=\"0\" /></a>&nbsp;&nbsp;&nbsp;&nbsp;</td>");
		out.println("    <td>");
		out.println("      <form action=\"powerq\" method=\"\">");
		out.println("       <input name=\"searchBox\" type=\"text\" size=\"50\" value=\"" + query.replace("\"", "&quot;") + "\">");
		out.println("       <input name=\"submit\" type=\"submit\" value=\"Search!\" style=\"height: 25px; width: 100px\">");
		out.println("       <label for=\"dataset\"> DataSet:</label>");
		out.println("       <select name=\"dataset\" >");
		switch(dataset)
		{
			case "employee":
				out.println("        <option value=\"acmdl\">ACMDL</option>");
				out.println("        <option value=\"imdb\">IMDB</option>");
				out.println("        <option value=\"employee\" selected>Employee</option>");
				break;
			case "imdb":
				out.println("        <option value=\"acmdl\">ACMDL</option>");
				out.println("        <option value=\"imdb\" selected>IMDB</option>");
				out.println("        <option value=\"employee\">Employee</option>");
				break;
			case "acmdl": default:
				out.println("        <option value=\"acmdl\" selected>ACMDL</option>");
				out.println("        <option value=\"imdb\">IMDB</option>");
				out.println("        <option value=\"employee\">Employee</option>");
				break;
		}
		out.println("      </select>");
		out.println("      <br>");
		out.println("      (<strong><a href=\"" + Constant.rootURL + "/introduction.html#samplequery\">Sample Queries</a></strong>)");
		out.println("      <input type=\"hidden\" name=\"page\" value=\"homepage\">");
		out.println("    </form>  ");
		out.println("  </td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("<br>");
		out.println("<table>");
		out.println("<tr>");
		
		StringBuffer explainBuf = new StringBuffer("Sorry, this query is invalid and the details are shown below.<br>");
		int nonExistKwNum = nonExistKw.length;
		if(nonExistKwNum > 0)
		{
			explainBuf.append(" The following keywords");
			for(int i = 0; i < nonExistKwNum; i++)
			{
				explainBuf.append(" \"").append(nonExistKw[i]).append("\"");
			}
			explainBuf.append(" do not exist in the ").append(dataset).append(" database;<br>");
		}
		int invalidFnNum = invalidFn.length;
		if(invalidFnNum > 0)
		{
			explainBuf.append(" The following keywords/functions");
			for(int i = 0; i < invalidFnNum; i += 2)
			{
				explainBuf.append(" \"").append(invalidFn[i]).append("\"");
			}
			explainBuf.append(" can not be applied by");
			for(int i = 0; i < invalidFnNum; i += 2)
			{
				explainBuf.append(" \"").append(invalidFn[i + 1]).append("\"<br>");
			}
			explainBuf.append(";");
		}
		int invalidGpNum = invalidGp.length;
		if(invalidGpNum > 0)
		{
			explainBuf.append(" The following keywords");
			for(int i = 0; i < invalidGpNum; i++)
			{
				explainBuf.append(" \"").append(invalidGp[i]).append("\"");
			}
			explainBuf.append(" can not be applied by \"").append(Constant.groupby).append("\";<br>");
		}
//		int invalidPdNum = invalidPd.length;
//		if(invalidPdNum > 0)
//		{
//			explainBuf.append(" the following keywords");
//			for(int i = 0; i < invalidPdNum; i += 2)
//			{
//				explainBuf.append(" \"").append(invalidPd[i]).append("\"");
//			}
//			explainBuf.append(" can not be applied by");
//			for(int i = 0; i < invalidPdNum; i += 2)
//			{
//				explainBuf.append(" \"").append(invalidPd[i + 1]).append("\"");
//			}
//			explainBuf.append(";");
//		}
//		if(prePD.length()!=0)
//		{
//			explainBuf.append(" the following keywords");
//			explainBuf.append(" \"").append(prePD).append("\"");
//			explainBuf.append(" can not be followed by a time period;");
//		}
		if(iqArray.length!=0)
		{
			for(InvalidQuery iq:iqArray)
			{
				if(iq.getPreKw()!=null)
				{
					explainBuf.append(" The  keyword \"").append(iq.getPreKw()).append("\" in TYPE(").append(Constant.TYPE_to_String(iq.getPreType())).append(") ");
					explainBuf.append(" cannot be followed by keyword \"").append(iq.getKw()).append("\" in TYPE(").append(Constant.TYPE_to_String(iq.getKwType())).append(");<br>");
				}
				else
				{
					explainBuf.append("The keyword \""+ iq.getKw()+ "\" in TYPE("+ Constant.TYPE_to_String(iq.getKwType())+ ") has the folloing error:" + iq.getOtherInfo()+";<br>");
				}
			}
		}
		
		int buflen = explainBuf.length();
		explainBuf.delete(buflen - 1, buflen);
		explainBuf.append(".");
		
		out.println("  <td  bgcolor=\"#D5DDF3\">" + explainBuf.toString() + " Please try some other queries or choose another dataset.</td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("<hr/>");
		out.println("<img src=\"images/404.jpg\"  border=\"0\" />");
		out.println("</body>");
		out.println("</html>");
	}
	/*
	private void outputFirstPage(PrintWriter out, Keyword[] kwArray, Function[] fnArray, Groupby[] gpArray, String query, String dataset, ORMGraph ormgraph)
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
		out.println("   img {");
		out.println("     border: 0;");
		out.println("   }");
		out.println("   input[type=\"checkbox\"] {");
		out.println("    width: 15px;");
		out.println("    height: 15px;");
		out.println("    padding: 0;");
		out.println("    margin:2;");
		out.println("    position: relative;");
		out.println("    top: 2px;");
		out.println("  }");
		out.println("  .STYLE1 {");
		out.println("    color: #CC0033;");
		out.println("    font-weight: bold;");
		out.println("  }");
		out.println("  .STYLE2 {");
		out.println("    color: #0099CC;");
		out.println("    font-weight: bold;");
		out.println("  }");
		out.println("  </style>");
		out.println("  <script src=\"library/jquery-1.9.1.js\"></script>");
		out.println("  <script src=\"library/jquery-ui.js\"></script>");
		out.println("  <link rel=\"stylesheet\" href=\"library/jquery-ui.css\" />");
		out.println("  <link rel=\"stylesheet\" type=\"text/css\" href=\"scripts/Main.css\" />");
		out.println("</head>");
		out.println("  <script type=\"text/javascript\" language=\"JavaScript\">");
		out.println("    function clearCheckBoxes()");
		out.println("    {");
		out.println("      $('input:checkbox').prop('checked', false);");
		out.println("    }");
		out.println("    function selectCheckBoxes()");
		out.println("    {");
		out.println("      $('input:checkbox').prop('checked', true);");
		out.println("    }");
		out.println("    function checkCheckBoxes(n){");
		out.println("      var i;");
		out.println("      for(i = 0; i < n; i++)");
		out.println("      {");
		out.println("        var name = \"kw\" + i;");
		out.println("        if(!checkCheckBox(name))");
		out.println("        {");
		out.println("          $(\"#dialog\").html(\"<p>You didn't choose any of the checkboxes for keyword \" + (i + 1) + \" !</p>\");");
		out.println("          $(\"#dialog\").dialog({");
		out.println("          show: {");
		out.println("          effect: \"blind\",");
		out.println("          duration: 200");
		out.println("          },");
		out.println("          resizable: false,");
		out.println("          height: 200,");
		out.println("          modal: true,");
		out.println("          buttons: {");
		out.println("            'OK': function() {");
		out.println("              $(this).dialog('close');");
		out.println("            }");
		out.println("          }");
		out.println("        });");
		out.println("        return false;");
		out.println("        }");
		out.println("      }");
		out.println("      return true;");
		out.println("    }");
		out.println("    function checkCheckBox(name)");
		out.println("    {");
		out.println("      var x = document.getElementsByName(name);");
		out.println("      var i;");
		out.println("      for(i = 0; i < x.length; i++)");
		out.println("      {");
		out.println("        if(x[i].checked)");
		out.println("        {");
		out.println("          return true;");
		out.println("        }");
		out.println("      }");
		out.println("      return false;");
		out.println("    }");
		out.println("  </script>");
		out.println("<body>");
		out.println("<table>");
		out.println("  <tr>");
		out.println("    <td><a href=\"" + Constant.rootURL + "/homepage.html\"><img src=\"images/logo2.png\"  border=\"0\" /></a>&nbsp;&nbsp;&nbsp;&nbsp;</td>");
		out.println("    <td>");
		out.println("      <form action=\"powerq\" method=\"post\">");
		out.println("       <input name=\"searchBox\" type=\"text\" size=\"50\" value=\"" + query.replace("\"", "&quot;") + "\">");
		out.println("       <input name=\"submit\" type=\"submit\" value=\"Search!\" style=\"height: 25px; width: 100px\">");
		out.println("       <label for=\"dataset\"> DataSet:</label>");
		out.println("       <select name=\"dataset\" >");
		switch(dataset)
		{
//			case "company":
//				out.println("        <option value=\"acmdl\">ACMDL</option>");
//				out.println("        <option value=\"imdb\">IMDB</option>");
//				out.println("        <option value=\"company\" selected>Company</option>");
//				break;
			case "imdb":
				out.println("        <option value=\"acmdl\">ACMDL</option>");
				out.println("        <option value=\"imdb\" selected>IMDB</option>");
//				out.println("        <option value=\"company\">Company</option>");
				break;
			case "acmdl": default:
				out.println("        <option value=\"acmdl\" selected>ACMDL</option>");
				out.println("        <option value=\"imdb\">IMDB</option>");
//				out.println("        <option value=\"company\">Company</option>");
				break;
		}
		out.println("      </select>");
		out.println("      <br>");
		out.println("      (<strong><a href=\"" + Constant.rootURL + "/introduction.html#samplequery\">Sample Queries</a></strong>)");
		out.println("      <input type=\"hidden\" name=\"page\" value=\"homepage\">");
		out.println("    </form>  ");
		out.println("  </td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("<br>");
		out.println("<table>");
		out.println("<tr>");
		out.println("  <td  bgcolor=\"#D5DDF3\"><b> Step 1:</b> Choose matches of individual terms.</td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("<hr/>");
		out.println("<table width=\"700\">");
		out.println("<tr>");
		out.println(" <td>");
		
		int kwNum = kwArray.length;
		out.println("  <form action=\"powerq\" method=\"post\" onsubmit=\"return checkCheckBoxes(" + kwNum + ");\">");
		
		for(int i = 0; i < kwNum; i++)
		{
			Keyword kw = kwArray[i];
			out.println("    <fieldset><legend>" + Translator.getStatement(i + 1, kw, "STYLE1") + "</legend>");
			
			String[] kwDesc = Translator.getDesc(kw, ormgraph);
			
			out.println("      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
			out.println("      <input name=\"kw" + i + "\" type=\"checkbox\" value=\"" + 0 + "\">" + kwDesc[0]);
			
			for(int j = 1; j < kwDesc.length; j++)
			{
				out.println("<br>");
				out.println("      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
				out.println("      <input name=\"kw" + i + "\" type=\"checkbox\" value=\"" + j + "\">" + kwDesc[j]);
			}
			
			out.println("    </fieldset>");
			out.println("    <br><br>");
		}
		
		int fnNum = fnArray.length;
		for(int i = 0; i < fnNum; i++)
		{
			Function fn = fnArray[i];
			out.println("    <fieldset><legend>" + Translator.getStatement(i + 1, fn, "STYLE2") + "</legend>");
			String fnDesc = Translator.getDesc(fn, kwArray, fnArray);
			
			out.println("      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + fnDesc);
			out.println("    </fieldset>");
			out.println("    <br><br>");
		}
		
		int gpNum = gpArray.length;
		for(int i = 0; i < gpNum; i++)
		{
			int kwIndex = gpArray[i].getKwIndex();
			out.println("    <fieldset><legend>" + Translator.getStatement(i + 1, "STYLE2") + "</legend>");
			String fnDesc = Translator.getDesc(kwIndex, kwArray);
			
			out.println("      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + fnDesc);
			out.println("    </fieldset>");
			out.println("    <br><br>");
		}
		
		out.println("    <br>");
		out.println("    <p align=\"right\">");
		out.println("      <input type=\"hidden\" name=\"page\" value=\"keyword\">");
		out.println("      <button type=\"button\" onclick=\"selectCheckBoxes();\" style=\"width: 75px\">SelectAll</button>");
		out.println("      <button type=\"button\" onclick=\"clearCheckBoxes();\" style=\"width: 75px\">ClearAll</button>");
		out.println("      <input  name=\"continue\" type=\"submit\" value=\"Continue\" style=\"width: 75px\">");
		out.println("    </p>");
		out.println("  </form>");
		out.println("  <p align=\"right\">");
		out.println("      *Do not use the '<b>back</b>' button on your browser");
		out.println("  </p>");
		out.println("</td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("<div id=\"dialog\" title=\"Warning:\" style=\"display: none;\"></div>");
		out.println("</body>");
		out.println("</html>");
	}
	*/
	
	private void outputFirstPage(PrintWriter out, Keyword[] kwArray, Function[] fnArray, Groupby[] gpArray, Tpredicate[] pdArray, String query, String dataset, ORMGraph ormgraph)
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
		out.println("   input[type=\"checkbox\"] {");
		out.println("    width: 15px;");
		out.println("    height: 15px;");
		out.println("    padding: 0;");
		out.println("    margin:2;");
		out.println("    position: relative;");
		out.println("    top: 2px;");
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
		out.println("  </style>");
		out.println("  <script src=\"library/jquery-1.9.1.js\"></script>");
		out.println("  <script src=\"library/jquery-ui.js\"></script>");
		out.println("  <link rel=\"stylesheet\" href=\"library/jquery-ui.css\" />");
		out.println("  <link rel=\"stylesheet\" type=\"text/css\" href=\"scripts/Main.css\" />");
		out.println("</head>");
		out.println("  <script type=\"text/javascript\" language=\"JavaScript\">");
		out.println("    function clearCheckBoxes()");
		out.println("    {");
		out.println("      $('input:checkbox').prop('checked', false);");
		out.println("    }");
		out.println("    function selectCheckBoxes()");
		out.println("    {");
		out.println("      $('input:checkbox').prop('checked', true);");
		out.println("    }");
		out.println("    function checkCheckBoxes(n){");
		out.println("      var i;");
		out.println("      for(i = 0; i < n; i++)");
		out.println("      {");
		out.println("        var name = \"kw\" + i;");
		out.println("        if(!checkCheckBox(name))");
		out.println("        {");
		out.println("          $(\"#dialog\").html(\"<p>You didn't choose any of the checkboxes for keyword \" + (i + 1) + \" !</p>\");");
		out.println("          $(\"#dialog\").dialog({");
		out.println("          show: {");
		out.println("          effect: \"blind\",");
		out.println("          duration: 200");
		out.println("          },");
		out.println("          resizable: false,");
		out.println("          height: 200,");
		out.println("          modal: true,");
		out.println("          buttons: {");
		out.println("            'OK': function() {");
		out.println("              $(this).dialog('close');");
		out.println("            }");
		out.println("          }");
		out.println("        });");
		out.println("        return false;");
		out.println("        }");
		out.println("      }");
		out.println("      return true;");
		out.println("    }");
		out.println("    function checkCheckBox(name)");
		out.println("    {");
		out.println("      var x = document.getElementsByName(name);");
		out.println("      var i;");
		out.println("      for(i = 0; i < x.length; i++)");
		out.println("      {");
		out.println("        if(x[i].checked)");
		out.println("        {");
		out.println("          return true;");
		out.println("        }");
		out.println("      }");
		out.println("      return false;");
		out.println("    }");
		out.println("  </script>");
		out.println("<body>");
		out.println("<table>");
		out.println("  <tr>");
		out.println("    <td><a href=\"" + Constant.rootURL + "/homepage.html\"><img src=\"images/logo2.png\"  border=\"0\" /></a>&nbsp;&nbsp;&nbsp;&nbsp;</td>");
		out.println("    <td>");
		out.println("      <form action=\"powerq\" method=\"post\">");
		out.println("       <input name=\"searchBox\" type=\"text\" size=\"50\" value=\"" + query.replace("\"", "&quot;") + "\">");
		out.println("       <input name=\"submit\" type=\"submit\" value=\"Search!\" style=\"height: 25px; width: 100px\">");
		out.println("       <label for=\"dataset\"> DataSet:</label>");
		out.println("       <select name=\"dataset\" >");
		switch(dataset)
		{
			case "employee":
				out.println("        <option value=\"acmdl\">ACMDL</option>");
				out.println("        <option value=\"imdb\">IMDB</option>");
				out.println("        <option value=\"employee\" selected>Employee</option>");
				break;
			case "imdb":
				out.println("        <option value=\"acmdl\">ACMDL</option>");
				out.println("        <option value=\"imdb\" selected>IMDB</option>");
				out.println("        <option value=\"employee\">Employee</option>");
				break;
			case "acmdl": default:
				out.println("        <option value=\"acmdl\" selected>ACMDL</option>");
				out.println("        <option value=\"imdb\">IMDB</option>");
				out.println("        <option value=\"employee\">Employee</option>");
				break;
		}
		out.println("      </select>");
		out.println("      <br>");
		out.println("      (<strong><a href=\"" + Constant.rootURL + "/introduction.html#samplequery\">Sample Queries</a></strong>)");
		out.println("      <input type=\"hidden\" name=\"page\" value=\"homepage\">");
		out.println("    </form>  ");
		out.println("  </td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("<br>");
		out.println("<table>");
		out.println("<tr>");
		out.println("  <td  bgcolor=\"#D5DDF3\"><b> Step 1:</b> Choose matches of individual keywords.</td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("<hr/>");
		out.println("<table width=\"700\">");
		out.println("<tr>");
		out.println(" <td>");
		
		int kwNum = kwArray.length;
		out.println("  <form action=\"powerq\" method=\"post\" onsubmit=\"return checkCheckBoxes(" + kwNum + ");\">");
		
		for(int i = 0; i < kwNum; i++)//content in first page
		{
			Keyword kw = kwArray[i];//kw
			out.println("    <fieldset><legend>" + Translator.getStatement(i + 1, kw, "STYLE1") + "</legend>");
			
			String[] kwDesc = Translator.getDesc(kw, ormgraph);
			
			out.println("      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
			out.println("      <input name=\"kw" + i + "\" type=\"checkbox\" value=\"" + 0 + "\">" + kwDesc[0]);
			
			for(int j = 1; j < kwDesc.length; j++)
			{
				out.println("<br>");
				out.println("      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
				out.println("      <input name=\"kw" + i + "\" type=\"checkbox\" value=\"" + j + "\">" + kwDesc[j]);
			}
			
			out.println("    </fieldset>");
			out.println("    <br><br>");
		}
		
		int fnNum = fnArray.length;//FN
		for(int i = 0; i < fnNum; i++)
		{
			Function fn = fnArray[i];
			out.println("    <fieldset><legend>" + Translator.getStatement(i + 1, fn, "STYLE2") + "</legend>");
			String fnDesc = Translator.getDesc(fn, kwArray, fnArray);
			
			out.println("      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + fnDesc);
			out.println("    </fieldset>");
			out.println("    <br><br>");
		}
		
		int gpNum = gpArray.length;//GP
		for(int i = 0; i < gpNum; i++)
		{
			int kwIndex = gpArray[i].getKwIndex();
			out.println("    <fieldset><legend>" + Translator.getStatement(i + 1, "STYLE2") + "</legend>");
			String fnDesc = Translator.getDesc(kwIndex, kwArray);
			
			out.println("      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + fnDesc);
			out.println("    </fieldset>");
			out.println("    <br><br>");
		}
		
		int pdNum = pdArray.length;//PD
		for(int i = 0; i < pdNum; i++)
		{
			Tpredicate pd = pdArray[i];
			out.println("    <fieldset><legend>" + Translator.getStatement(i + 1, pd, "STYLE3") + "</legend>");
			String fnDesc = Translator.getDesc(pd, kwArray);
			
			out.println("      &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + fnDesc);
			out.println("    </fieldset>");
			out.println("    <br><br>");
		}
		
		out.println("    <br>");
		out.println("    <p align=\"right\">");
		out.println("      <input type=\"hidden\" name=\"page\" value=\"keyword\">");
		out.println("      <button type=\"button\" onclick=\"selectCheckBoxes();\" style=\"width: 75px\">SelectAll</button>");
		out.println("      <button type=\"button\" onclick=\"clearCheckBoxes();\" style=\"width: 75px\">ClearAll</button>");
		out.println("      <input  name=\"continue\" type=\"submit\" value=\"Continue\" style=\"width: 75px\">");
		out.println("    </p>");
		out.println("  </form>");
		out.println("  <p align=\"right\">");
		out.println("      *Do not use the '<b>back</b>' button on your browser");
		out.println("  </p>");
		out.println("</td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("<div id=\"dialog\" title=\"Warning:\" style=\"display: none;\"></div>");
		out.println("</body>");
		out.println("</html>");
	}
	
	private void outputSecondPage(PrintWriter out, PGraph[] patternArray, Keyword[] kwArray, Function[] fnArray, Groupby[] gpArray, Tpredicate[] tpArray, String query, String dataset, ORMGraph ormgraph, int page)
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
		out.println("  </style>");
		out.println("  </style>");
		out.println("  <script src=\"library/jquery-1.9.1.js\"></script>");
		out.println("  <script src=\"library/jquery-ui.js\"></script>");
		out.println("  <script src=\"library/raphael.js\"></script>");
		out.println("  <script src=\"library/xmlsax.js\"></script>");
		out.println("  <link rel=\"stylesheet\" href=\"library/jquery-ui.css\" />");
		out.println("  <script src=\"scripts/SaxEventHandler.js\"></script>");
		out.println("  <script src=\"scripts/XmlLayoutParser.js\"></script>");
		out.println("  <script src=\"scripts/Pattern.js\"></script>");
		out.println("  <script src=\"scripts/Main.js\"></script>");
		out.println("  <link rel=\"stylesheet\" type=\"text/css\" href=\"scripts/Main.css\" />");
		out.println("</head>");
		out.println("  <script type=\"text/javascript\">");
		out.println("    function navPage(p)");
		out.println("    {");
		out.println("      var addr = \"" + Constant.rootURL + "/powerq?page=query&pageIndex=\" + p;");
		out.println("      window.location = addr;");
		out.println("    }");
		out.println("  </script>");
		out.println("  <script>");
		out.println("    var patternXML = [];");
		int patternNum = patternArray.length;
		int stIndex = (page - 1) * Constant.resultPerPage;
		int range = page * Constant.resultPerPage  < patternNum ?  Constant.resultPerPage : (patternNum - (page - 1) * Constant.resultPerPage);
		
		String[] patternDesc = new String[range];
		for(int i = 0; i < range; i++)
		{
			PGraph pgraph = patternArray[i + stIndex];
			String[] desc = Translator.getQueryDesc(pgraph, ormgraph, kwArray, fnArray, gpArray, tpArray,  "STYLE1", "STYLE2", "STYLE3");
			patternDesc[i] = desc[1];
			out.println("    patternXML[" + i + "]=\"" + desc[0].replace("\"", "\\\"") + "\";");
		}
		
		out.println("  </script>");
		out.println("<body>");
		
		out.println("  <table>");
		out.println("    <tr>");
		out.println("      <td><a href=\"" + Constant.rootURL + "/homepage.html\"><img src=\"images/logo2.png\"  border=\"0\" /></a>&nbsp;&nbsp;&nbsp;&nbsp;</td>");
		out.println("      <td>");
		out.println("      <form action=\"powerq\" method=\"post\">");
		out.println("       <input name=\"searchBox\" type=\"text\" size=\"50\" value=\"" + query.replace("\"", "&quot;") + "\">");
		out.println("       <input name=\"submit\" type=\"submit\" value=\"Search!\" style=\"height: 25px; width: 100px\">");
		out.println("       <label for=\"dataset\"> DataSet:</label>");
		out.println("       <select name=\"dataset\" >");
		switch(dataset)
		{
			case "employee":
				out.println("        <option value=\"acmdl\">ACMDL</option>");
				out.println("        <option value=\"imdb\">IMDB</option>");
				out.println("        <option value=\"employee\" selected>Employee</option>");
				break;
			case "imdb":
				out.println("        <option value=\"acmdl\">ACMDL</option>");
				out.println("        <option value=\"imdb\" selected>IMDB</option>");
				out.println("        <option value=\"employee\">Employee</option>");
				break;
			case "acmdl": default:
				out.println("        <option value=\"acmdl\" selected>ACMDL</option>");
				out.println("        <option value=\"imdb\">IMDB</option>");
				out.println("        <option value=\"employee\">Employee</option>");
				break;
		}
		out.println("      </select>");
		out.println("      <br>");
		out.println("      (<strong><a href=\"" + Constant.rootURL + "/introduction.html#samplequery\">Sample Queries</a></strong>)");
		out.println("      <input type=\"hidden\" name=\"page\" value=\"homepage\">");
		out.println("    </form>  ");
		out.println("  </td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("<br>");
		out.println("<table>");
		out.println("<tr>");
		out.println("    <td  bgcolor=\"#D5DDF3\"><b> Step 2:</b> Choose interpretation(s) of the query.</td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("<hr/>");
		out.println("<table width=\"900\">");
		out.println("  <tr>");
		out.println("    <td><img src=\"images/database.png\" width=\"128\" height=\"128\"></td>");
		out.println("    <td>");
		out.println("      <table border=\"1\"   bordercolor=\"#B6C7D8\">");
		out.println("        <tr>");
		out.println("          <td>");
		out.println("            In a query pattern,");
		out.println("            <ul style=\"list-style-type:square\">");
		out.println("              <li> An <span class=\"STYLE1\">object node</span> (<img src=\"images/object.png\" width=\"32\" height=\"18\" align=\"top\">) denotes some object.</li><br>");
		out.println("              <li> A <span class=\"STYLE1\">relationship node</span> (<img src=\"images/relationship.png\" width=\"32\" height=\"18\" align=\"top\">) denotes a relationship.</li><br>");
		out.println("              <li> A <span class=\"STYLE1\">mixed node</span> (<img src=\"images/mix.png\" width=\"32\" height=\"18\" align=\"top\">) denotes some object and its associated many-to-one relationship.</li>");
		out.println("            </ul>");
		out.println("          </td>");
		out.println("        </tr>");
		out.println("      </table>");
		out.println("    </td>");
		out.println("  </tr>");
		out.println("</table>");
		out.println("<hr/>");
		out.println("<table cellpadding=\"5\" cellspacing=\"5\" style=\"table-layout: fixed;  width:100%\">");
		out.println("  <tr>");
		out.println("    <th bgcolor=\"#D5DDF3\" width=\"30px\">#</th>");
		out.println("    <th bgcolor=\"#D5DDF3\" width=\"60%\">Query Patterns (" + (stIndex + 1) + "-" + (stIndex + range) + " of " + patternNum + ")</th>");
		out.println("    <th bgcolor=\"#D5DDF3\" width=\"40%\">Query Interpretations</th>");
		out.println("    <th width=\"10px\"></th>");
		out.println("    <th width=\"70px\"></th>");
		out.println("  </tr>");
		
		for(int i = 0; i < range; i++)
		{
			out.println("  <tr><td align=\"center\"><strong>" + (stIndex + i + 1) + ".</strong></td>");
			out.println("    <td>");
			out.println("      <div id=\"divMask" + i + "\" class=\"divMaskStyle\">");
			out.println("        <div id=\"divPattern" + i + "\" class=\"divPatternStyle\"></div>");
			out.println("      </div>");
			out.println("    </td>");
			out.println("    <td>" + patternDesc[i].replace("\n", "<br>") + "</td><td></td>");
			out.println("    <td>");
			out.println("      <form action=\"result\" method=\"post\" target=\"_blank\">");
			out.println("        <input type=\"hidden\" name=\"page\" value=\"result\">");
			out.println("        <input type=\"hidden\" name=\"pattern\" value=\"" + (stIndex + i) + "\">");
			out.println("        <input type=\"hidden\" name=\"desc\" value=\"" + patternDesc[i].replace("\"", "&quot;").replace("\n", "<br>") + "\">");
			out.println("        <input name=\"retrieve\" type=\"submit\" value=\"Retrieve\" style=\"width: 70px\">");
			out.println("      </form>");
			out.println("    </td>");
			out.println("  </tr>");
		}
		out.println("  <tr>");
		out.println("    <td colspan=\"5\" align=\"right\">");
		out.println("      <p>");
		out.println("        *Do not use the '<b>back</b>' button on your browser");
		out.println("      </p>");
		out.println("    </td>");
		out.println("  </tr>");
		out.println("</table>");
		out.println("<br><br>");
		out.println("<table border=\"0\" align=\"center\">");
		out.println("  <tr>");
		int stP = (page - 10) > 1 ? (page - 10) : 1;
		int pageNum = (int) Math.ceil(patternNum / (double) Constant.resultPerPage);
		int edP = (page + 10) < pageNum ? (page + 10) : pageNum;
		if(page > 1)
		{
			out.println("<td><a href=\"javascript:navPage(" + (page - 1) +");\" style=\"text-decoration: none;\">Previous</a>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>");
		}
		for (int i = stP; i <= edP; i++) 
		{
			if(i == page)
			{
				out.println("    <td><strong>" + page + "</strong></td>");
			}
			else
			{
				out.println("    <td><a href=\"javascript:navPage(" + i + ");\" style=\"text-decoration: none;\">" + i + "</a></td>");
			}
		}
		if(page < pageNum)
		{
			out.println("    <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"javascript:navPage(" + (page + 1) + ");\" style=\"text-decoration: none;\">Next</a></td>");
		}
		out.println("  </tr>");
		out.println("</table>");
		out.println("  <div id=\"dialog\" title=\"Warning:\" style=\"display: none;\"><p>Retrieving result from this query pattern could freeze the browser. Continue?</p></div>");
		out.println("</body>");
		out.println("</html>");
	}
}
