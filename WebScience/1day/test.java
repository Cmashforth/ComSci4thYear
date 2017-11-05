
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.lang.StringBuilder;
import java.util.HashMap;

public class test {

    public static void main(String[] args) {
		
    	BufferedReader br = null;
		String line = " ";
		String csvSplitBy = ",";
		HashMap<Integer,Cluster> clusterMap = new HashMap<Integer,Cluster>();
		Long maxTime = 0L;
		Long minTime = 3000000000000000000L;
		

		try {
            br = new BufferedReader(new FileReader(args[0]));
            while ((line = br.readLine()) != null) {
				String[] row = line.split(csvSplitBy);
				Tweet tweetInsert = new Tweet(Integer.parseInt(row[0]),row[1],Long.parseLong(row[2]),Long.parseLong(row[3]),Integer.parseInt(row[4]),row[5],row[6]);
				
				if(tweetInsert.getTimestamp() > maxTime) {
					maxTime = tweetInsert.getTimestamp();
				}
				
				if(tweetInsert.getTimestamp() < minTime) {
					minTime = tweetInsert.getTimestamp();
				}
				
				if(clusterMap.containsKey(tweetInsert.getClusterID()) == false) {
					clusterMap.put(tweetInsert.getClusterID(),new Cluster(tweetInsert.getClusterName()));
				}
				Cluster currentCluster = clusterMap.get(tweetInsert.getClusterID());
				currentCluster.add_tweet(tweetInsert);
				clusterMap.put(tweetInsert.getClusterID(), currentCluster);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
		
		
		for(int i = 8680; i < 8681; i++) {
			Cluster currentCluster = clusterMap.get(i);
			ArrayList<Tweet> clusterTweetLst = currentCluster.getTweets();
			Long startTime = minTime;
			Long endTime = minTime + 3600000L;
			while(startTime < maxTime){
				TimeUnit currentUnit = new TimeUnit(startTime,endTime);
				for(int j = 0; j < clusterTweetLst.size(); j ++){
					if(clusterTweetLst.get(j).getTimestamp() > startTime) {
						if(clusterTweetLst.get(j).getTimestamp() < endTime) {
							currentUnit.addTweet(clusterTweetLst.get(j));
						}
					}
				}
				currentCluster.add_timeUnit(currentUnit);
				startTime = startTime + 3600000L;
				endTime = endTime + 3600000L;
			}
			ArrayList<TimeUnit> testList = currentCluster.getTimeUnits();
			for(int k = 0; k < testList.size(); k++) {
				TimeUnit unt = testList.get(k);
				Long time = unt.getMaxTime();
			}
			currentCluster.setSigma();
		}
		
    }

	
	private static void createOutput(HashMap<Integer, Cluster> clusterList){
		
		BufferedWriter bw = null;
		FileWriter fw = null;
		Cluster currentCluster = null;
		
		try {
			fw = new FileWriter("Output.csv");
			bw = new BufferedWriter(fw);
			
			for(int i = 1; i < clusterList.size(); i++) {
				currentCluster = clusterList.get(i);
				if(currentCluster.getSize() >= 5) {
					bw.write(currentCluster.tweetListToString());
				}
			}
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if(bw != null) {
					bw.close();
				}
				
				if(fw != null) {
					fw.close();
				}
			} catch(IOException ex) {
				ex.printStackTrace();
			}
		}
		
	}
	
    
		
}