package RTree;

import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.ArrayList;
import java.io.*;

public class pqDistances
{
    private Comparator<Double> comparator; // used to "sort" prioriy queue
    private PriorityQueue<Double> queue; // stores distances only
    private HashMap<Double,String> distance_mapping; // maps distances to the distances in the priority queue "queue"
    private int size; // Max. size of priority queue
    private HashMap<String, HashMap<String, ArrayList>> States; // key ---> State, value ---> hashmap containing another hashmap of county (key) and the coordinates (value)

    /* Priority Queue structure
       @param size = size of the priority queue (i.e., max # of values the queue can hold)
       @param state = state found from Rtree
       @param latitude = origin point (x)
       @param longitude = origin point (y)
    */
    
    public pqDistances (HashMap states, int size, String state, double longitude, double latitude, int k) {
    	this.comparator = new pqComparator();
        this.queue = new PriorityQueue<Double>(size, this.comparator);
        this.distance_mapping = new HashMap<Double,String>();
        this.size = size;

        // Find state in hashmap
        HashMap<String, ArrayList> Counties = (HashMap)states.get(state);
        String county_name;
        ArrayList<Double> county_temp; //current county in the for loop
        double[] center_point;
        double distance;

        int count = 0;
        for (Object county : Counties.keySet()) {
            if (county == null) continue;
            county_name = (String)county;
            
            county_temp = (ArrayList)Counties.get(county_name); // the ArrayList for the current county (contains MBR data)
            
            // Find center point of the county
            center_point = getCenter(county_temp.get(0),county_temp.get(1),county_temp.get(2),county_temp.get(3));
           
            //System.out.println(county_name + ": " + center_point[0] + ", " + center_point[1]);
            
            // Calculate distance
            distance = calculateDistance(center_point, longitude, latitude);
            
            // Insert into the queue
            //addDistanceToQueue(distance);
            queue.add(distance);
            
            // Insert into hashmap to create a mapping between "queue" and "Counties"
            distance_mapping.put(distance,county_name);
            count++;
        }
    }

    // Loads map data from 'latest_map.dat'
    private void loadMap() throws IOException, ClassNotFoundException {
         File file = new File("latest_map.dat");
         FileInputStream f = new FileInputStream(file);
         ObjectInputStream s = new ObjectInputStream(f);
         HashMap<String, HashMap<String, ArrayList>> fileObj2 = (HashMap<String, HashMap<String, ArrayList>>) s.readObject();
         s.close();

         this.States = fileObj2;
    }

    // Get coordinates for the center of the county
    public double[] getCenter(double x1, double y1, double x2, double y2) {
        double mid_x = (x1 + x2) / 2;
        double mid_y = (y1 + y2) / 2;
        double[] center = { mid_x , mid_y };
        return center;
    }

    // Get distance
    public double calculateDistance(double[] center, double longitude, double latitude) {
        double x = (latitude - center[0]); //* Math.cos((longitude + center[1])/2);
        double y = (longitude - center[1]);
        return (Math.sqrt(x*x + y*y));
    }

    // Add distance to queue
    private void addDistanceToQueue(double distance) {
    	//queue = new PriorityQueue<Double>(size, comparator);
    	queue.add(distance);
    }

    //Print top 'k' counties from the queue
    public String printQueue(int k) {
         // PriorityQueue<Double> temp_queue = new PriorityQueue<Double>(size, comparator);
         double d;
         String county_name;
         int counter = 0;
         String county_results = "County: ";
         while ((queue.size() != 0) && (counter < k) ){
              d = queue.remove();
             
              // Now find the distance 'd' in the hashmap to figure out which county 'distance' corresponds to
              county_name = (String)distance_mapping.get(d);
              county_results += (county_name + " [distance: " + String.format("%.8g", d) + " km] </br>");
              System.out.println("County: " + county_name + " [distance: " + String.format("%.8g", d) + " km]");
              counter++;
         }
	return county_results;   
    }
}
