import java.util.ArrayList;

public class TimeUnit {

	private int size;
	private ArrayList<Tweet> tweet_list;
	private Long minTime;
	private Long maxTime;
	
	public TimeUnit(Long min, Long max){
		this.size = 0;
		this.tweet_list = new ArrayList<Tweet>();
		minTime = min;
		maxTime = max;
	}
	
	public void addTweet(Tweet tweet) {
		tweet_list.add(tweet);
		size = size + 1;
	}
	
	public int getSize() {
		return size;
	}
	
	public Long getMaxTime() {
		return maxTime;
	}
	
	public ArrayList<Tweet> getTweetList(){
		return tweet_list;
	}
	
	
}
