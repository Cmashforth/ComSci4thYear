
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
import java.util.Iterator;

public class test {

    public static void main(String[] args) {
		
    	BufferedReader br = null;
		String line = " ";
		String csvSplitBy = ",";
		HashMap<Integer,Cluster> clusterMap = new HashMap<Integer,Cluster>();
		HashMap<String,Entity> entityMap = new HashMap<String,Entity>();
		ArrayList<Tweet> allTweets = new ArrayList<Tweet>();
		ArrayList<Tweet> outputTweets = new ArrayList<Tweet>();
		Long maxTime = 0L;
		Long minTime = 3000000000000000000L;
		String output = "";
		BufferedWriter bw = null;
		FileWriter fw = null;
		
		System.out.println("Creating Entity and Cluster Objects");
		try {
            br = new BufferedReader(new FileReader(args[0]));
            while ((line = br.readLine()) != null) {
				String[] row = line.split(csvSplitBy);
				Tweet tweetInsert = new Tweet(Integer.parseInt(row[0]),row[1],Long.parseLong(row[2]),Long.parseLong(row[3]),Integer.parseInt(row[4]),row[5],row[6]);
				allTweets.add(tweetInsert);
				
				if(clusterMap.containsKey(tweetInsert.getClusterID()) == false) {
					clusterMap.put(tweetInsert.getClusterID(),new Cluster(tweetInsert.getClusterName()));
				}
				Cluster currentCluster = clusterMap.get(tweetInsert.getClusterID());
				currentCluster.add_tweet(tweetInsert);
				clusterMap.put(tweetInsert.getClusterID(), currentCluster);
				
				if(entityMap.containsKey(tweetInsert.getClusterName()) == false) {
					entityMap.put(tweetInsert.getClusterName(), new Entity(tweetInsert.getClusterName()));
					
				}
				Entity currentEntity = entityMap.get(tweetInsert.getClusterName());
				currentEntity.addTweet(tweetInsert);
				entityMap.put(tweetInsert.getClusterName(), currentEntity);
				
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
		
		minTime = allTweets.get(0).getTimestamp();
		maxTime = allTweets.get(allTweets.size() - 1).getTimestamp();
		System.out.println("Entity and Cluster Objects created");
		
		
		
		System.out.println("Cluster Centroid Calculation");
		for(int i = 1; i < clusterMap.size(); i++) {
			Cluster currentCluster = clusterMap.get(i);
			currentCluster.setCentroid();
		}
		
		System.out.println("Centroids Calculated");
		System.out.println("Calculate Sigmas");
		
		for(String key : entityMap.keySet()) {
			Entity currentEntity = entityMap.get(key);
			if(currentEntity.getTweets().size() > 10) {
				currentEntity.calculateSigma(minTime, maxTime, 300000L);
				currentEntity.calculateSigma(minTime, maxTime, 600000L);
				currentEntity.calculateSigma(minTime, maxTime, 1200000L);
				currentEntity.calculateSigma(minTime, maxTime, 2400000L);
				currentEntity.calculateSigma(minTime, maxTime, 4800000L);
				currentEntity.calculateSigma(minTime, maxTime, 9600000L);
				currentEntity.calculateSigma(minTime, maxTime, 21600000L);
			} else {
				currentEntity.setSigmaMap();	
			}
			entityMap.put(key, currentEntity);
		}
		System.out.println("Sigmas Calculated");
		
		System.out.println("Start Burst Detection");
		while(allTweets.size() != 0) {
			Long step = 300000L;
			Tweet currentTweet = allTweets.remove(0);
			HashMap<Long, Double> entitySigmas = entityMap.get(currentTweet.getClusterName()).getSigmaMap();
			if(entitySigmas != null) {
				while(step <= 21600000L) {
					if(entitySigmas.get(step) > 1.0) {
					ArrayList<Tweet> possTweets = countTweets(step,allTweets,currentTweet,clusterMap);
					if(possTweets.size() - 1 >= entitySigmas.get(step)) {
						for(int i = 0; i < possTweets.size(); i++) {
							Tweet possTweet = possTweets.get(i);
							if(outputTweets.contains(possTweet) == false) {
								outputTweets.add(possTweet);
							}
						}
					}
					}
					if(step != 9600000L) {
						step = step * 2;
					} else {
						step = 21600000L;
					}
				}
			}
			allTweets.trimToSize();
		}
		
		System.out.println("Finish Burst Detection");
		System.out.println("Create Output String");
		
		for(int i = 0; i< outputTweets.size(); i++) {
			output = output + outputTweets.get(i).toString();
		}
		
		System.out.println("Output Generated");
		
		try {
			fw = new FileWriter("Output.csv");
			bw = new BufferedWriter(fw);
			
			bw.write(output);
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
    
    
    private static ArrayList<Tweet> countTweets(Long step, ArrayList<Tweet> tweetList, Tweet currentTweet, HashMap<Integer, Cluster> clusterMap){
    	ArrayList<Tweet> possTweets = new ArrayList<Tweet>();
    	possTweets.add(currentTweet);
    	Long endTime = currentTweet.getTimestamp() + step;
    	String tweetEntity = currentTweet.getClusterName();
    	
    	for(int  i = 0; i < tweetList.size() && tweetList.get(i).getTimestamp() < endTime; i++) {
    		if(tweetList.get(i).getClusterName().equals(tweetEntity)) {
    			if(clusterMap.get(tweetList.get(i).getClusterID()).getCentroid() > endTime - step) {
    			 possTweets.add(tweetList.get(i));
    			}
    		}
    	}
    	return possTweets;
    	
    }
    
}