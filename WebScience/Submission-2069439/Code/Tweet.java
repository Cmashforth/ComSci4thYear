//Class for a Tweet (which is a line in the csv file)
public class Tweet{
	
	//Instances for a Tweet, dividing each column (bar the tweet content) into a separate instance
	private int clusterID;
	private String clusterName;
	private Long tweetID;
	private Long timestamp;
	private int userID;
	private String tokens;
	private String line; //Contains the entire csv line, with commas intact

	//Constructor for a Tweet object
	public Tweet(int ci, String cn, Long ti, Long ts, int ui, String tok, String txt){
		clusterID = ci;
		clusterName = cn;
		tweetID = ti;
		timestamp = ts;
		userID = ui;
		tokens = tok;
		line = txt;
	}
	
	//Getters for sthe instances
	public int getClusterID(){
		return clusterID;
	}
	
	public String getClusterName(){
		return clusterName;
	}
	
	public Long getTweetID() {
		return tweetID;
	}
	
	public Long getTimestamp() {
		return timestamp;
	}
	
	public int getUserID() {
		return userID;
	}
	
	public String getTokens() {
		return tokens;
	}
	
	public String getText() {
		return line;
	}
	
}