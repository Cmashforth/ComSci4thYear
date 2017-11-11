import java.util.ArrayList;
import java.util.HashMap;

//Class for a Window that contains tweets
public class Window {

	//The two instances of a Window, a list of tweets within the window and a Map of the clusters in the window
	ArrayList<Tweet> tweetList;
	HashMap<Integer, Cluster> windowClusters;
	
	//Constructor for a window
	public Window() {
		this.tweetList = new ArrayList<Tweet>();
		this.windowClusters = new HashMap<Integer,Cluster>();
	}
	
	//Adds a Tweet to the window
	public void addTweet(Tweet tweet) {
		tweetList.add(tweet);
	}
	
	//Returns the size of the window (number of tweets)
	public int getSize() {
		return this.tweetList.size();
	}
	
	//Getters for the ClusterMap and the first Tweet in the window
	public HashMap<Integer,Cluster> getWindowClusters(){
		return windowClusters;
	}
	
	public Tweet getFirstTweet() {
		return tweetList.get(0);
	}
	
	//Organises the tweets in clusters and enters these clusters into the cluster map, similar to the cluster addition in the main file
	public void organiseClusters() {
		for(int i = 0; i < tweetList.size(); i++) {
			if(windowClusters.containsKey(tweetList.get(i).getClusterID()) == false){
				windowClusters.put(tweetList.get(i).getClusterID(), new Cluster(tweetList.get(i).getClusterName()));
			}
			Cluster currentCluster = windowClusters.get(tweetList.get(i).getClusterID());
			currentCluster.add_tweet(tweetList.get(i));
			windowClusters.put(tweetList.get(i).getClusterID(), currentCluster);
		}
	}
	
	
	
	
	
}
