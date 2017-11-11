import java.util.ArrayList;
import java.util.HashMap;

//Class for an Entity
public class Entity {

	//Instances for the name of the entity, list of Tweets and Map of Three-Sigma Values
	private String name;
	private ArrayList<Tweet> tweet_list;
	private HashMap<Long,Double> sigmaMap;
	
	//Constructor for an Entity Object
	public Entity(String clusterName) {
		this.name = clusterName;
		this.tweet_list = new ArrayList<Tweet>();
		this.sigmaMap = new HashMap<Long,Double>();
	}
	
	//Getters for several instances
	public String getName() {
		return this.name;
	}
	
	public ArrayList<Tweet> getTweets(){
		return this.tweet_list;
	}
	
	public HashMap<Long,Double> getSigmaMap(){
		return this.sigmaMap;
	}
	
	//Adds a tweet to the tweetList instance
	public void addTweet(Tweet newTweet) {
		this.tweet_list.add(newTweet);
	}
	
	//Calculates Sigmas for a specified Window Size, starts by creating a endPoint for a window
	//and a list of counts for a set of windows
	public void calculateSigma(Long minTime, Long maxTime,Long timeStep) {
		Long stepEnd = minTime + timeStep;
		ArrayList<Integer> stepAmounts = new ArrayList<Integer>();
		
		//Cycles through the timespan of allTweets and counts the number of tweets in each individual window
		while(minTime < maxTime) {
			int count = 0;
			for(int i = 0; i < this.tweet_list.size(); i++) {
				if(this.tweet_list.get(i).getTimestamp() > minTime) {
					if(this.tweet_list.get(i).getTimestamp() < stepEnd) {
						count = count + 1;
					}
				}
			}
			
			//Adds the count to the List and updates the window limits  
			stepAmounts.add(count);
			minTime = stepEnd;
			stepEnd = stepEnd + timeStep;
		}
		
		//Calculates the mean number of tweets per window size
		double sum = 0.0;
		for(int j = 0; j < stepAmounts.size(); j++) {
			sum = sum + stepAmounts.get(j);
		}
		double mean = sum/stepAmounts.size();
		
		//Calculates the standard deviation of the counts 
		double sumSq = 0.0;
		for(int k = 0; k < stepAmounts.size(); k ++) {
			sumSq = Math.pow((stepAmounts.get(k) - mean),2);
		}
		double sd = Math.sqrt(sumSq/(stepAmounts.size() - 1));
		
		//Calculates mean + 3*sd and rounds it up to the nearest integer (can't have a decimal of a tweet) 
		//then adds this sigma to the sigmaMap
		double sigma = Math.ceil(mean + (3*sd));
		this.sigmaMap.put(timeStep, sigma);
	}
}
