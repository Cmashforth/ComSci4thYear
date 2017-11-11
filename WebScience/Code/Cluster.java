import java.util.ArrayList;


//Class for a Cluster of Tweets
public class Cluster{
	
	//Instances for the name of a cluster, a list of tweets and the centroid time of all tweets
	private String cluster_name;
	private ArrayList<Tweet> tweet_list;
	private Long centroid;

	//Constructor for a Cluster
	public Cluster(String cn){
		this.cluster_name = cn;
		this.tweet_list = new ArrayList<Tweet>();
		this.centroid = 0L;
	}
	
	//Getters for several instances
	public String getName() {
		return this.cluster_name;
	}
	
	public int getSize(){
		return this.tweet_list.size();
	}
	
	public ArrayList<Tweet> getTweets(){
		return this.tweet_list;
	}
	
	public Long getCentroid() {
		return centroid;
	}
	
	//Adds a tweet to the tweetList
	public void add_tweet(Tweet tweet){
		this.tweet_list.add(tweet);
	}
	
	//Calculates the mean Timestamp of all the tweets in the cluster
	public void setCentroid() {
		Long sumTime = 0L;
		for(int i = 0; i < this.tweet_list.size(); i++) {
			sumTime = sumTime + this.tweet_list.get(i).getTimestamp();
		}
		this.centroid = sumTime/this.tweet_list.size();
	}
}