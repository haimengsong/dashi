package offline;

import java.io.BufferedReader;
import java.io.FileReader;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;

import db.DBUtil;

public class Purify {
	public static void main(String[] args) {
		MongoClient mongoClient = new MongoClient();
		MongoDatabase db = mongoClient.getDatabase(DBUtil.DB_NAME);
		// The name of the file to open.
		String fileName = "/Users/song/Downloads/ratings_Musical_instruments.csv";

		String line = null;

		try {
			FileReader fileReader = new FileReader(fileName);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while ((line = bufferedReader.readLine()) != null) {
				String[] values = line.split(",");

				db.getCollection("ratings").insertOne(new Document().append("user", values[0]).append("item", values[1])
						.append("rating", Double.parseDouble(values[2])));

			}
			System.out.println("import Done!");
			bufferedReader.close();
			mongoClient.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
