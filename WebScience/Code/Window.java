import java.util.ArrayList;
import java.util.HashMap;

public class Window {

	int size;
	ArrayList<Tweet> tweetList;
	HashMap<Integer, Cluster> windowClusters;
	
	public Window() {
		this.size = 0;
		this.tweetList = new ArrayList<Tweet>();
		this.windowClusters = new HashMap<Integer,Cluster>();
	}
	
	public void addTweet(Tweet tweet) {
		tweetList.add(tweet);
		size = size + 1;
	}
	
	public int getSize() {
		return size;
	}
	
	public HashMap<Integer,Cluster> getWindowClusters(){
		return windowClusters;
	}
	
	public Tweet getFirstTweet() {
		return tweetList.get(0);
	}
	
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
