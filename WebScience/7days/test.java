
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.lang.StringBuilder;
import java.util.HashMap;

public class test {

    public static void main(String[] args) {
		
		HashMap<Integer, Cluster> clusterList = createList(args[0]);
		createOutput(clusterList);

    }

	private static HashMap<Integer, Cluster> createList(String csvFile){
		
		BufferedReader br = null;
		String line = " ";
		String csvSplitBy = ",";
		HashMap<Integer,Cluster> clusterMap = new HashMap<Integer,Cluster>();

		try {
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
				String[] row = line.split(csvSplitBy);
				Tweet tweetInsert = new Tweet(Integer.parseInt(row[0]),row[1],Long.parseLong(row[2]),Long.parseLong(row[3]),Integer.parseInt(row[4]),row[5],row[6]);
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
		return clusterMap;
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