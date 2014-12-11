package EC504.GUI;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



import org.apache.tomcat.util.codec.binary.StringUtils;

import LoadData.MapData;
import RTree.RTreeNode_GlobalScale;
import RTree.pqDistances;
import StateNeighbors.StateNeighbors;

/**
 * Servlet implementation class FindNearestServlet
 */
@WebServlet(name="FindNearestServlet", urlPatterns={"/Find"})
public class FindNearestServlet extends HttpServlet {
	private static final long serialVersionUID = 102831973239L;
      
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
			// Store form fields and convert to appropriate data types
			Double y; //Double.parseDouble(request.getParameter("latitude"));
			Double x; //= Double.parseDouble(request.getParameter("longitude"));
			int k;// = Integer.parseInt(request.getParameter("k"));
			
<<<<<<< HEAD
			// If the user does not input a latitude, longtitude, or a value for k, set default values
=======
			// If the user does not input a latitude, longtitude, or a value for k
>>>>>>> 7ef50aca702b0b3006cd48354dfb51b7eff2f105
			try {
				y = Double.parseDouble(request.getParameter("latitude"));
			} catch(NumberFormatException e) {
				y = 42.3581;
			}
			
			try {
				x = Double.parseDouble(request.getParameter("longitude"));
			} catch(NumberFormatException e) {
				x = -71.0636;
			}
			
			try{
				k = Integer.parseInt(request.getParameter("k"));
				if (k < 1 || k > 10) {
					k = 10;
				}
			} catch(NumberFormatException e) {
			   k = 10;
			}
				
			// Truncate original values to four decimal places (or will cause error in "pqDistances.java, line 38")
			x = (long) (x * 10000) / 10000.0;
			y = (long) (y * 10000) / 10000.0;
	 
	        // Load map data
			String myFile = getServletContext().getRealPath("NationalFile_StateProvinceDecimalLatLong.txt");
			HashMap mapData_States = LoadMapData(myFile);
			
			// Load state neighbors
			StateNeighbors stateNeighbors = LoadStateNeighborsList();

			// Create RTree(ish) data structure
			RTreeNode_GlobalScale Root = CreateRTree(mapData_States);
			ArrayList<RTreeNode_GlobalScale> nodesContainingPoint;
			nodesContainingPoint = Root.findNodesContainingPoint(x, y);
			String stateAbbrv = null;
			
			pqDistances pq = new pqDistances();
			pqDistances pqMajorityVote = new pqDistances();
			
			for (int ii = 0; ii < nodesContainingPoint.size(); ii++) {
				// Get current state node
				RTreeNode_GlobalScale currentStateNode = nodesContainingPoint.get(ii);
				stateAbbrv = currentStateNode.getName();											// Get name of state (abbreviation)
				
				if (stateAbbrv.equals("Root: United States")) continue;
				
				// Add distance calculations to PQ for this state
    			pq.addAdditionalDistances(mapData_States, 10000, stateAbbrv, x, y);
    			pqMajorityVote.addAdditionalDistances(mapData_States, 10000, stateAbbrv, x, y);
    			
    			//  -------------------- Find this State's State Neighbors and their distances --------------------- 
    			
    			ArrayList<String> neighbors = new ArrayList<String>();								// Array to keep track this state's neighbors
    			
    			// Iterate through the State Neighbors list to find this state's neighbors
    			Set<String> keys = StateNeighbors.stateNeighbors.keySet();
    			for (String key: keys) {
    				
    				// Skip if abbreviation we're looking for doesn't match with this entry
    				if (!key.equals(stateAbbrv)) continue;
    				
    				// Found the state in the State Neighbors list! Now place its neighbors in the neighbors array for later use
    				neighbors.addAll(StateNeighbors.stateNeighbors.get(key));
    				System.out.println("Querying state and its neighbors... " + key + ": " + StateNeighbors.stateNeighbors.get(key));
    				
    			}
    			
    			// Place this state's neighbors' counties and calculated distance into NearestCounties hashmap
    			for (int jj = 0; jj < neighbors.size(); jj++){
    				
    				String neighborAbbrv = neighbors.get(jj).toString();							// Get current state neighbor's name
    				pq.addAdditionalDistances(mapData_States, 10000, neighborAbbrv, x, y);
    				pqMajorityVote.addAdditionalDistances(mapData_States, 10000, neighborAbbrv, x, y);
    			}
			}
			
			// Display resulting nodes that contain the original coordinates or nearby
			/*for (int ii = 0; ii < nodesContainingPoint.size(); ii++) {
				System.out.println("From recursive function. Nodes containing test point: " + nodesContainingPoint.get(ii).getName());
			}*/

			// Print results
			//pq.printQueue(k);
			ArrayList topK = pqMajorityVote.getStateAndCountyName(5);
			System.out.println(topK);
			
			String[] StateCountyStringArray;
			int max = 0;
			String StateAndCountyWithMostPoints = "";
			for (int ii = 0; ii < topK.size(); ii ++){
				StateCountyStringArray = ((String) topK.get(ii)).split(" ");
				String state = StateCountyStringArray[0];
				String county = StateCountyStringArray[1];
				//System.out.println(MapData.getCount(state, county));//getCount
				if (MapData.getCount(state, county) > max){
					max = MapData.getCount(state, county);
					StateAndCountyWithMostPoints = state + ", " + county;
				}
			}
			
			System.out.println("You are in: " + StateAndCountyWithMostPoints + " with number of points: " + max);

	        // HTML Response
	        PrintWriter writer = response.getWriter();
	        //String htmlResponse = "<!DOCTYPEhtml><html><head><style type=\"text/css\">body{font-size:0.8em;font-family:sans-serif;margin-top:0.1em;margin-left:0;margin-right:0}</style></head><script>alert(\"Original coordinates(latitude,longitude):\n\" y, x);</script><body></body></html>";
	        String htmlResponse = "<html>";
	        htmlResponse += "<style type=\"text/css\"> body { font-size: 0.8em; font-family: sans-serif; margin-top: 0.1em; margin-left: 0; margin-right: 0;}</style>";
	        htmlResponse += "<body><b>Original coordinates (latitude, longitude):</b> " + "(" + y +", " + x + ")" + "<br/>";      
	        htmlResponse += "<p><b>The " + k + " nearest counties are:</b>" + "<br/>";
	        htmlResponse += pq.printQueue(k);
	        htmlResponse += "<p>Based on majority voting of the top five nearest counties, you are in: " + StateAndCountyWithMostPoints + " with number of points: " + max + "<br/>";
	        htmlResponse += "</body>";
	        htmlResponse += "</html>";

	        // Return response
	        writer.println(htmlResponse);
			//pq.printQueue(k);
	}

	public static RTreeNode_GlobalScale CreateRTree(HashMap<String, HashMap<String, ArrayList>> mapData) {
		// Root of treec
		RTreeNode_GlobalScale rootNode = new RTreeNode_GlobalScale();			
		
		// This prints out all the states (for debugging purposes)
		for (Object current_state : mapData.keySet()) { // Iterate through all states
	    		// IMPORTANT: Uncomment when selecting to print out a specific state (for testing purposes only)
	    		//if (!current_state.toString().equals("MH")) continue;
	    	
	    		RTreeNode_GlobalScale stateNode = new RTreeNode_GlobalScale(); // Initialize current state's nodes
	        	//System.out.println(current_state); // Prints out state
	        	stateNode.setName(current_state.toString()); // Add state's name to node
	        
	        	HashMap state_value = (HashMap) mapData.get(current_state); // Get internal state hashmap (counties and their dimensions)
	        
	        	// Iterate through all counties in state
	        	for (Object current_county : state_value.keySet()) { 
					//System.out.print("   - " + current_county); // Prints out county belonging to [this] state
					// Get county's list of rectangular dimensions
					ArrayList county_dimensions = (ArrayList) state_value.get(current_county); 
					//System.out.print(" " + county_dimensions + "\n"); // Print out county's dimensions (points form)
					
					// Store coordinates. Note ArrayList points order: [x1, y1, x2, y2]
					Double x1, x2, y1, y2;
				
					// If ArrayList does not have 4 points, skip county
					if (county_dimensions.size() != 4) continue; 
					
					y2 = (Double) county_dimensions.get(0); // Get y2 = max latitude
					x1 = (Double) county_dimensions.get(1); // Get x1 = min longitude 
					y1 = (Double) county_dimensions.get(2); // Get y1 = min latitude
					x2 = (Double) county_dimensions.get(3); // Get x2 = max longitude
				
					// Create county node: name, x1, x2, y1, y2
					RTreeNode_GlobalScale countyNode = new RTreeNode_GlobalScale(current_county.toString(), x1, x2, y1, y2);
					
					// Add county to state
					stateNode.addChild(countyNode);
		        	}
			        // Add state to root node
			        rootNode.addChild(stateNode);
		    	}
	    rootNode.setName("Root: United States");
	    return rootNode;
	}

	public static HashMap LoadMapData(String filename) throws IOException {
		System.out.println("Loading Map Data...");
		MapData mapData = new MapData(filename);
		System.out.println("Loading Map Data completed...");
		return mapData.getStates();
	}
	
	/**
	 * Loads states' neighbors
	 * @return
	 * @throws IOException
	 */
	public static StateNeighbors LoadStateNeighborsList() throws IOException{
		System.out.println("Loading State Neighbors data...");
		StateNeighbors stateNeighbors = new StateNeighbors();
		System.out.println("Loading State Neighbors completed...");
		return stateNeighbors;
	}
}
