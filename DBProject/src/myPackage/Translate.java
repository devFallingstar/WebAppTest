package myPackage;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Servlet implementation class Translate
 */
@WebServlet("/Translate")
public class Translate extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Translate() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		PrintWriter out = response.getWriter();
		RequestDispatcher dispatcher = null;
		String jsonTString = request.getParameter("ERJson4");
		JSONParser parser = new JSONParser();
		JSONObject stringToJson;
		HashMap<String, String> typeMap = new HashMap<>();
		String query = new String();
		String table = new String();
		
		try {
			stringToJson = (JSONObject)parser.parse(jsonTString);
			JSONArray nodeArr = (JSONArray) stringToJson.get("nodeDataArray");
			
			for (int i = 0; i < nodeArr.size(); i++) {
				JSONObject eachNodeObject = (JSONObject)parser.parse(nodeArr.get(i).toString());
				System.out.println(eachNodeObject.toJSONString());
				System.out.println("text : " + eachNodeObject.get("text"));
				try {
					String name = eachNodeObject.get("text").toString();
					String dataType = eachNodeObject.get("dataType").toString();
					typeMap.put(name, dataType);
//					System.out.println(name + " | " + dataType);
				}catch(Exception e) {
					// Do Nothing
				}
			}
			
//			System.out.println("jsonTString test : " + nodeArr.toJSONString());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		MakeTable mt = new MakeTable();
		MakeQuery mk = new MakeQuery();

		table = mt.makeTable(jsonTString).replace(" &#7488;", "");
		query = mk.makeQuery(jsonTString).replace(" &#7488;", "");

		request.setAttribute("table", table);
		request.setAttribute("query", query);

		dispatcher = request.getRequestDispatcher("translateResult.jsp");
		dispatcher.forward(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
