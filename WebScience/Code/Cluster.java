import java.util.ArrayList;
import java.lang.Math;

public class Cluster{
	
	private String cluster_name;
	private int size;
	private ArrayList<Tweet> tweet_list;
	private Long centroid;

	
	
	public Cluster(String cn){
		cluster_name = cn;
		size = 0;
		tweet_list = new ArrayList<Tweet>();
		centroid = 0L;
	}
	
	public String getName() {
		return cluster_name;
	}
	
	public void add_tweet(Tweet tweet){
		tweet_list.add(tweet);
		size = size + 1;
	}
	
	public int getSize(){
		return size;
	}
	
	public ArrayList<Tweet> getTweets(){
		return tweet_list;
	}
	
	public void setCentroid() {
		Long sumTime = 0L;
		for(int i = 0; i < tweet_list.size(); i++) {
			sumTime = sumTime + tweet_list.get(i).getTimestamp();
		}
		this.centroid = sumTime/tweet_list.size();
	}
	
	public Long getCentroid() {
		return centroid;
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