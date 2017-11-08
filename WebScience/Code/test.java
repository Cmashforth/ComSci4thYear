
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
			System.out.println(currentEntity.getName());
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
		
		
		
		
		
		
		
		/*
		System.out.println("Output Generation");
		for(int i = 1; i< clusterMap.size(); i++) {
			Cluster currentCluster = clusterMap.get(i);
			if(currentCluster.getSigma() != 0) {
				ArrayList<TimeUnit> clusterTimeUnits = currentCluster.getTimeUnits();
				for(int j = 0; j < clusterTimeUnits.size(); j++) {
					TimeUnit timeSpan = clusterTimeUnits.get(j);
					if(timeSpan.getSize() >= currentCluster.getSigma()) {
						ArrayList<Tweet> outputTweets = timeSpan.getTweetList();
						for(int k = 0; k < outputTweets.size(); k++) {
							output = output + outputTweets.get(k).toString();
						}
					}
				}	
			}
		}
		
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
		
	*/	
    }	
    
}