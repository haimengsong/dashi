package api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.MySQLDBConnection;

/**
 * Servlet implementation class VisitHistory
 */
@WebServlet("/history")
public class VisitHistory extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
    private static final DBConnection connection = new MySQLDBConnection();
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public VisitHistory() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		try {
			if(request.getParameterMap().containsKey("user_id")){
				String userId = request.getParameter("user_id");
				JSONArray array = new JSONArray();
				Set<String> visited_business_id= connection.getVisitedRestaurants(userId);
				for(String businessId : visited_business_id) {
					array.put(connection.getRestaurantsById(businessId, true));
				}
				//array.put(new JSONObject().put("status", "OK"));
				RpcParser.writeOutput(response, array);
			}else {
				RpcParser.writeOutput(response,
						new JSONObject().put("status", "InvalidParameter"));
			}
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		try{
			JSONObject input = RpcParser.parseInput(request);
			if(input.has("user_id") && input.has("visited")){
				String userId = (String) input.get("user_id");
				JSONArray array = (JSONArray) input.get("visited");
				List<String> visitedRestaurants = new ArrayList<>();
				for(int i = 0; i < array.length(); i++){
					String businessId = (String) array.get(i);
					visitedRestaurants.add(businessId);
				}
				if(connection.setVisitedRestaurants(userId, visitedRestaurants)){
					RpcParser.writeOutput(response, new JSONObject().put("status", "OK"));
				}else{
					RpcParser.writeOutput(response,
							new JSONObject().put("status", "InvalidParameter"));
				}
			}else {
				RpcParser.writeOutput(response,
						new JSONObject().put("status", "InvalidParameter"));
			}
		}catch (JSONException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * @see HttpServlet#doDelete(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		try {
			JSONObject input = RpcParser.parseInput(request);
			if(input.has("user_id") && input.has("visited")){
				String userId = (String) input.get("user_id");
				JSONArray array = (JSONArray) input.get("visited");
				List<String> visitedRestaurants = new ArrayList<>();
				for(int i = 0; i < array.length(); i++){
					String businessId = (String) array.get(i);
					visitedRestaurants.add(businessId);
				}
				if(connection.unsetVisitedRestaurants(userId, visitedRestaurants)){
					RpcParser.writeOutput(response, new JSONObject().put("status","OK"));
				}else{
					RpcParser.writeOutput(response, new JSONObject().put("status", "InvalidParameter"));
				}
				
			}else {
				RpcParser.writeOutput(response, new JSONObject().put("status", "InvalidParameter"));
			}
			
		} catch (Exception e){
			e.printStackTrace();
		}
	}

}
