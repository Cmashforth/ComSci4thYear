
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class test {

    public static void main(String[] args) {
    	
    	long startTime = System.currentTimeMillis();
		
    	//Creates several instances that will be used throughout the program
    	BufferedReader br = null;
		String line = " ";
		String csvSplitBy = ",";
		HashMap<Integer,Cluster> clusterMap = new HashMap<Integer,Cluster>();
		HashMap<String,Entity> entityMap = new HashMap<String,Entity>();
		ArrayList<Tweet> allTweets = new ArrayList<Tweet>();
		ArrayList<Tweet> outputTweets = new ArrayList<Tweet>();
		ArrayList<Window> outputWindows = new ArrayList<Window>();
		ArrayList<Tweet> checkOutput = new ArrayList<Tweet>();
		Long maxTime = 0L;
		Long minTime = 3000000000000000000L;
		String output = "";
		BufferedWriter bw = null;
		FileWriter fw = null;
		
		System.out.println("Creating Entity and Cluster Objects");
		
		//Tries to read in the file specified in the command line by line 
		try {
            br = new BufferedReader(new FileReader(args[0]));
            while ((line = br.readLine()) != null) {
				
            	//Splits the line by commas and creates a new Tweet object to
            	String[] row = line.split(csvSplitBy);
				Tweet tweetInsert = new Tweet(Integer.parseInt(row[0]),row[1],Long.parseLong(row[2]),Long.parseLong(row[3]),Integer.parseInt(row[4]),row[5],line);
				
				//Checks if the current Tweet is newer or older than the current max/min timestamp, then adds tweet to the ArrayList of all Tweets
				if(tweetInsert.getTimestamp() < minTime) {
					minTime = tweetInsert.getTimestamp();
				}
				
				if(tweetInsert.getTimestamp() > maxTime) {
					maxTime = tweetInsert.getTimestamp();
				}
				
				allTweets.add(tweetInsert);
				
				
				//Adds the tweet to a cluster and creates the cluster if it does not already exist 
				//The cluster is then added to the Map of Clusters when it is updated/created
				if(clusterMap.containsKey(tweetInsert.getClusterID()) == false) {
					clusterMap.put(tweetInsert.getClusterID(),new Cluster(tweetInsert.getClusterName()));
				}
				Cluster currentCluster = clusterMap.get(tweetInsert.getClusterID());
				currentCluster.add_tweet(tweetInsert);
				clusterMap.put(tweetInsert.getClusterID(), currentCluster);
				
				//Does similar to before but adds tweets to Entity Objects instead of Cluster Objects
				if(entityMap.containsKey(tweetInsert.getClusterName()) == false) {
					entityMap.put(tweetInsert.getClusterName(), new Entity(tweetInsert.getClusterName()));
					
				}
				Entity currentEntity = entityMap.get(tweetInsert.getClusterName());
				currentEntity.addTweet(tweetInsert);
				entityMap.put(tweetInsert.getClusterName(), currentEntity);
				
            }
        } catch (FileNotFoundException e) { //Exceptions for reading in the file
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
		
		//Runs through each cluster and calculates the centroid time for all the tweets in that cluster
		for(int i = 1; i < clusterMap.size(); i++) {
			Cluster currentCluster = clusterMap.get(i);
			currentCluster.setCentroid();
		}
		
		System.out.println("Centroids Calculated");
		System.out.println("Calculate Sigmas");
		
		//Cycles through the Entitys and calculates the 3-sigma values for each window size
		//5,10,20,40,80,160,360 minutes and re-enters the Entity into the Entity Map
		for(String key : entityMap.keySet()) {
			Entity currentEntity = entityMap.get(key);
			currentEntity.calculateSigma(minTime, maxTime, 300000L);
			currentEntity.calculateSigma(minTime, maxTime, 600000L);
			currentEntity.calculateSigma(minTime, maxTime, 1200000L);
			currentEntity.calculateSigma(minTime, maxTime, 2400000L);
			currentEntity.calculateSigma(minTime, maxTime, 4800000L);
			currentEntity.calculateSigma(minTime, maxTime, 9600000L);
			currentEntity.calculateSigma(minTime, maxTime, 21600000L);
			entityMap.put(key, currentEntity);
		}
		
		System.out.println("Sigmas Calculated");
		System.out.println("Start Burst Detection");
		
		//Cycles through all tweets and detects if a burst has occurred for a window size
		//that starts at the time of the tweet
		while(allTweets.size() != 0) {
			//Starting with the largest window size, the tweet is removed from the allTweets 
			//(size of allTweets is adjusted), the Sigmas for the entity are retrieved and
			//bursting is set to false
			long step = 21600000L; 
			Tweet currentTweet = allTweets.remove(0);
			allTweets.trimToSize();
			HashMap<Long, Double> entitySigmas = entityMap.get(currentTweet.getClusterName()).getSigmaMap();
			boolean burst = false;
			
			//Iterates through the windows sizes, reducing each time unless the window has been 
			//indicated as bursting
			while(step >= 300000L && burst == false) {
				//A List of possible tweets that may be outputted is created, first tweet is
				//automatically added
				ArrayList<Tweet> possTweets = new ArrayList<Tweet>();
				possTweets.add(currentTweet);
				Long endPoint = currentTweet.getTimestamp() + step;
				
				//If the Window End Point is greater than the maximum Tweet Time then the end point is 
				//set to the maximum Tweet Time
				if(endPoint > maxTime) {
					endPoint = maxTime;
				}
				
				//A new window is created and the current tweet is added to it
				int j = 0;
				Window newWindow = new Window();
				newWindow.addTweet(currentTweet);
				
				//
				while(j < allTweets.size() && allTweets.get(j).getTimestamp() <= endPoint) {
					if(allTweets.get(j).getClusterName().equals(currentTweet.getClusterName())) {
						newWindow.addTweet(allTweets.get(j));
					}
					j = j + 1;
				}
				if(newWindow.getSize() >= 10 && newWindow.getSize() >= entitySigmas.get(step)) {
					outputWindows.add(newWindow);
					burst = true;
				}
				if(step == 21600000L) {
					step = 9600000L;
				} else {
					step = step/2;
				}
			}
			
		}
		System.out.println("Finish Burst Detection");
		System.out.println("Start Cluster Selection");
		
		for(int i = 0; i < outputWindows.size(); i++) {
			outputWindows.get(i).organiseClusters();
			HashMap<Integer,Cluster> windowClusters = outputWindows.get(i).getWindowClusters();
			for(Integer clusterKey : windowClusters.keySet()) {
				Cluster currentCluster = windowClusters.get(clusterKey);
				if(currentCluster.getSize() > 10) {
					Cluster masterCluster = clusterMap.get(clusterKey);
					if(masterCluster.getCentroid() > outputWindows.get(i).getFirstTweet().getTimestamp()) {
						outputTweets.addAll(currentCluster.getTweets());
					}
				}
			}
		}
		
		System.out.println("Ended Cluster Selection");
		
		System.out.println("Create Output String");
		
		for(int i = 0; i < outputTweets.size(); i++) {
			if(checkOutput.contains(outputTweets.get(i)) == false) {
				output = output + outputTweets.get(i).getText() + '\n';
				checkOutput.add(outputTweets.get(i));
			}
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
		
		Long endTime = System.currentTimeMillis();
		System.out.println(endTime-startTime);
			
    }	
    
    
 
    
    
}