package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import model.Restaurant;
import yelp.YelpAPI;

public class MySQLDBConnection implements DBConnection {

	private Connection conn = null;
	private static final int MAX_RECOMMENDED_RESTAURANTS = 10;

	public MySQLDBConnection() {
		this(DBUtil.URL);
	}

	public MySQLDBConnection(String url) {
		try {
			// Forcing the class representing the MySQL driver to load and
			// initialize.
			// The newInstance() call is a work around for some broken Java
			// implementations
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(url);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		if (conn != null) {
			try {
				conn.close();
			} catch (Exception e) {
				/* ignored */
			}
		}
	}

	@Override
	public boolean setVisitedRestaurants(String userId, List<String> businessIds) {
		// TODO Auto-generated method stub
		String query = "INSERT INTO history (user_id,business_id) VALUES (?,?)";
		try {
			PreparedStatement statement = conn.prepareStatement(query);
			for(String businessId : businessIds) {
				statement.setString(1, userId);
				statement.setString(2, businessId);
				statement.execute();
			}
			return true;
		} catch (SQLException e){
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean unsetVisitedRestaurants(String userId, List<String> businessIds) {
		// TODO Auto-generated method stub
		String query = "DELETE FROM WHERE user_id = ? AND business_id = ?";
		try {
			PreparedStatement statement = conn.prepareStatement(query);
			for(String businessId : businessIds) {
				statement.setString(1, userId);
				statement.setString(2, businessId);
				statement.execute();
			}
			return true;
		} catch (SQLException e){
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public Set<String> getVisitedRestaurants(String userId) {
		// TODO Auto-generated method stub
		Set<String> visitedRestaurants = new HashSet<String>();
		try {
			String sql = "SELECT business_id from history WHERE user_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);;
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				String visitedRestaurant = rs.getString("business_id");
				visitedRestaurants.add(visitedRestaurant);
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return visitedRestaurants;
	}

	@Override
	public JSONObject getRestaurantsById(String businessId, boolean isVisited) {
		// TODO Auto-generated method stub
	
		try {
			if(conn != null){
				String sql = "SELECT * FROM restaurants WHERE business_id = ?";
				PreparedStatement statement = conn.prepareStatement(sql);
				statement.setString(1, businessId);
				ResultSet rs = statement.executeQuery();
				if(rs.next()){
					String name = rs.getString("name");
					String categories = rs.getString("categories");
					String city = rs.getString("city");
					String state = rs.getString("state");
					String fullAddress = rs.getString("full_address");
					double stars = rs.getDouble("stars");
					double latitude = rs.getDouble("latitude");
					double longitude = rs.getDouble("longitude");
					String imageUrl = rs.getString("image_url");
					String url = rs.getString("url");
					JSONObject object = new Restaurant(businessId, name, categories, city, state, stars,fullAddress,
							 latitude, longitude, imageUrl, url).toJSONObject();
					object.put("is_visited", isVisited);
					return object;
				}
			}
		} catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public JSONArray recommendRestaurants(String userId) {
		// TODO Auto-generated method stub
		
		try {
			if(conn == null){
				return null;
			}
			
			Set<String> visitedRestaurants = getVisitedRestaurants(userId); //step1
			Set<String> allCategories = new HashSet<>();
			for(String restaurant : visitedRestaurants){
				allCategories.addAll(getCategories(restaurant));
			}
			
			Set<String> allRestaurants = new HashSet<>(); //step 3
			for(String category : allCategories){
				Set<String> set = getBusinessId(category);
				allRestaurants.addAll(set);
			}
			
			Set<JSONObject> diff = new HashSet<>(); //step 4
			int count = 0;
			for(String businessId : allRestaurants){
				//Perform filtering (allRestaurants - visitedRestaurants)
				if(!visitedRestaurants.contains(businessId)){
					diff.add(getRestaurantsById(businessId,false));
					count++;
					if(count >= MAX_RECOMMENDED_RESTAURANTS){
						break;
					}
				}
			}
			
			return new JSONArray(diff);
		} catch (Exception e){
			System.out.println(e.getMessage());
		}
		return null;
	}

	@Override
	public Set<String> getCategories(String businessId) {
		// TODO Auto-generated method stub
		Set<String> set = new HashSet<>();
		try {
		
			if(conn == null){
				return set;
			}
			
			String sql = "SELECT categories FROM restaurants WHERE business_id = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, businessId);
			ResultSet rs = statement.executeQuery();
			if(rs.next()){
				String [] categories = rs.getString("categories").split(",");
				for(String category : categories){
					set.add(category.trim());
				}
			}
			
		} catch (Exception e){
			System.out.println(e.getMessage());
		}
		return set;
	}

	@Override
	public Set<String> getBusinessId(String category) {
		// TODO Auto-generated method stub
		Set<String> set = new HashSet<>();
		try {
			if(conn == null){
				return set;
			}
			
			String sql = "SELECT business_id FROM restaurants WHERE categories LIKE ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, "%" + category + "%");
			ResultSet rs = statement.executeQuery();
			while(rs.next()){
				set.add(rs.getString("business_id"));
			}
			
		} catch (Exception e){
			System.out.println(e.getMessage());
		}
		return set;
	}

	@Override
	public JSONArray searchRestaurants(String userId, double lat, double lon, String term) {
		// TODO Auto-generated method stub
		try {
			// Connect to Yelp API
			YelpAPI api = new YelpAPI();
			JSONObject response = new JSONObject(api.searchForBusinessesByLocation(lat, lon));
			JSONArray array = (JSONArray) response.get("businesses");

			List<JSONObject> list = new ArrayList<>();
			Set<String> visited = getVisitedRestaurants(userId);

			for (int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);
				// Clean and purify
				Restaurant restaurant = new Restaurant(object);
				String businessId = restaurant.getBusinessId();
				String name = restaurant.getName();
				String categories = restaurant.getCategories();
				String city = restaurant.getCity();
				String state = restaurant.getState();
				String fullAddress = restaurant.getFullAddress();
				double stars = restaurant.getStars();
				double latitude = restaurant.getLatitude();
				double longitude = restaurant.getLongitude();
				String imageUrl = restaurant.getImageUrl();
				String url = restaurant.getUrl();
				// return clean restaurant objects
				JSONObject obj = restaurant.toJSONObject();
				if (visited.contains(businessId)) {
					obj.put("is_visited", true);
				} else {
					obj.put("is_visited", false);
				}

				String sql = "INSERT IGNORE INTO restaurants VALUES (?,?,?,?,?,?,?,?,?,?,?)";

				PreparedStatement statement = conn.prepareStatement(sql);
				statement.setString(1, businessId);
				statement.setString(2, name);
				statement.setString(3, categories);
				statement.setString(4, city);
				statement.setString(5, state);
				statement.setDouble(6, stars);
				statement.setString(7, fullAddress);
				statement.setDouble(8, latitude);
				statement.setDouble(9, longitude);
				statement.setString(10, imageUrl);
				statement.setString(11, url);
				statement.execute();

				// Perform filtering if term is specified.
				if (term == null || term.isEmpty()) {
					list.add(obj);
				} else {
					if (categories.contains(term) || fullAddress.contains(term) || name.contains(term)) {
						list.add(obj);
					}
				}
			}

			return new JSONArray(list);

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return null;
	}

	@Override
	public String getFirstLastName(String userId) {
		// TODO Auto-generated method stub
		String name = "";
		try {
			if(conn != null){
				String query = "SELECT first_name, last_name FROM users WHERE user_id = ?";
				PreparedStatement statement = conn.prepareStatement(query);
				statement.setString(1, userId);
				ResultSet rs = statement.executeQuery();
				if(rs.next()){
					name += rs.getString("first_name") + " "
							+ rs.getString("last_name");
				}
			}
		} catch (Exception e){
			System.out.println(e.getMessage());
		}
		
		return name;
	}

	@Override
	public Boolean verifyLogin(String userId, String password) {
		// TODO Auto-generated method stub
		try {
			if(conn == null) {
				return false;
			}
			
			String sql = "SELECT user_id FROM users WHERE user_id = ? and password = ?";
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, userId);
			ResultSet rs = statement.executeQuery();
			if(rs.next()){
				return true;
			}
		} catch (Exception e){
			System.out.println(e.getMessage());
		}
		
		return false;
	}

}
