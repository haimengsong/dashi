package db;

import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class MongoDBConnection implements DBConnection{

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean setVisitedRestaurants(String userId, List<String> businessIds) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean unsetVisitedRestaurants(String userId, List<String> businessIds) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<String> getVisitedRestaurants(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject getRestaurantsById(String businessId, boolean isVisited) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONArray recommendRestaurants(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getCategories(String businessId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getBusinessId(String category) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONArray searchRestaurants(String userId, double lat, double lon, String term) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFirstLastName(String userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean verifyLogin(String userId, String password) {
		// TODO Auto-generated method stub
		return null;
	}

}
