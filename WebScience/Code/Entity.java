import java.util.ArrayList;
import java.util.HashMap;

public class Entity {

	private String name;
	private int size;
	private ArrayList<Tweet> tweet_list;
	private HashMap<Long,Double> sigmaMap;
	
	public Entity(String clusterName) {
		this.name = clusterName;
		this.size = 0;
		this.tweet_list = new ArrayList<Tweet>();
		this.sigmaMap = new HashMap<Long,Double>();
	}
	
	public String getName() {
		return name;
	}
	
	public ArrayList<Tweet> getTweets(){
		return tweet_list;
	}
	
	public void setSigmaMap(){
		this.sigmaMap = null;
	}
	
	public void addTweet(Tweet newTweet) {
		tweet_list.add(newTweet);
		size = size + 1;
	}
	
	public void calculateSigma(Long minTime, Long maxTime,Long timeStep) {
		Long stepEnd = minTime + timeStep;
		ArrayList<Integer> stepAmounts = new ArrayList<Integer>();
		
		while(minTime < maxTime) {
			int count = 0;
			for(int i = 0; i < tweet_list.size(); i++) {
				if(tweet_list.get(i).getTimestamp() > minTime) {
					if(tweet_list.get(i).getTimestamp() < stepEnd) {
						count = count + 1;
					}
				}
			}
			stepAmounts.add(count);
			minTime = stepEnd;
			stepEnd = stepEnd + timeStep;
		}
		
		
		
		double sum = 0.0;
		for(int j = 0; j < stepAmounts.size(); j++) {
			sum = sum + stepAmounts.get(j);
			
		}
		
		double mean = sum/stepAmounts.size();
		
		double sumSq = 0.0;
		for(int k = 0; k < stepAmounts.size(); k ++) {
			sumSq = Math.pow((stepAmounts.get(k) - mean),2);
		}
		double sd = Math.sqrt(sumSq/(stepAmounts.size() - 1));
		double sigma = Math.ceil(mean + (3*sd));
		System.out.println(sigma);
		sigmaMap.put((timeStep/60000), sigma);
	}
}
