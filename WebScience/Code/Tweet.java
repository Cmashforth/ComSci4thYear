public class Tweet{
	
	private int clusterID;
	private String clusterName;
	private Long tweetID;
	private Long timestamp;
	private int userID;
	private String tokens;
	private String line;
	private Boolean output;
	
	public Tweet(int ci, String cn, Long ti, Long ts, int ui, String tok, String txt){
		clusterID = ci;
		clusterName = cn;
		tweetID = ti;
		timestamp = ts;
		userID = ui;
		tokens = tok;
		line = txt;
		output = false;
	}
	
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
	
	public void output() {
		this.output = true;
	}
	
	public boolean getOutput() {
		return this.output;
	}
	
	public String toString(){
		String output = Integer.toString(this.getClusterID()) + "," + this.getClusterName()
		+ "," + Long.toString(this.getTweetID()) + "," + Long.toString(this.getTimestamp())
		+ "," + Integer.toString(this.getUserID()) + "," + this.getTokens() + "," + this.getText() + "\n";
		return output;
	}
	
}