import java.util.ArrayList;

public class Cluster{
	
	private String cluster_name;
	private int size;
	private ArrayList<Tweet> tweet_list;
	
	
	public Cluster(String cn){
		cluster_name = cn;
		size = 0;
		tweet_list = new ArrayList<Tweet>();
	}
	
	public void add_tweet(Tweet tweet){
		tweet_list.add(tweet);
		size = size + 1;
	}
	
	public int getSize(){
		return size;
	}
	
	public String tweetListToString() {
		String output = "";
		Tweet currentTweet = null;
		
		for(int i = 0; i < tweet_list.size(); i++) {
			currentTweet = tweet_list.get(i);
			output = output + currentTweet.toString();
		}
		return output;
	}
	
}