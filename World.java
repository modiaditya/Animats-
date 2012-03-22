package planet_navi;


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import PredatorPrey.Buffallo;
import PredatorPrey.Lion;
import PredatorPrey.SimpleAgent;

public class World {

	public static Hashtable<Integer, Buffallo> buffalloAnimatsInTheWorld = new Hashtable<Integer, Buffallo>();
	public static int nextBuffalloId = -1;
	public static Hashtable<Integer, Lion> lionAnimatsInTheWorld = new Hashtable<Integer,Lion>();
	public static int nextLionId = -1;

	public static int[][] help_signal_array = new int[50][50];
	public static int[][] buffallo_agression_signal_array = new int[50][50];
	public static int[][] lion_roar_signal_array = new int[50][50];

	public static void initializeWorld()
	{
		for(int i=0;i<50;i++)
		{
			for (int j=0;j<50;j++)
			{
				help_signal_array[i][j] = -1;
				buffallo_agression_signal_array[i][j] = -1;
				lion_roar_signal_array[i][j] = -1;
			}
		}
	}

	public static void addBuffallo(Integer buffalloId, Buffallo currBuffallo)
	{
		buffalloAnimatsInTheWorld.put(buffalloId, currBuffallo);
	}

	public static void addLion(Integer lionId, Lion currLion)
	{
		lionAnimatsInTheWorld.put(lionId, currLion);
	}

	public static void removeBuffalo(Integer buffaloId)
	{
		buffalloAnimatsInTheWorld.remove(buffaloId);
	}

	public static void removeLion(Integer lionId)
	{
		lionAnimatsInTheWorld.remove(lionId);
	}
	/******************************************************************************/
	/*********************All the get methods below********************************/
	public static Buffallo getLeaderBuffallo(SimpleAgent currAnimat)
	{
		Buffallo leaderBuffallo = null;

		Set<Entry<Integer, Buffallo>> s= buffalloAnimatsInTheWorld.entrySet();
		Iterator<Entry<Integer,Buffallo>> i = s.iterator(); 

		int minimumKeyValue = -1;
		while(i.hasNext())
		{
			Entry<Integer,Buffallo> tableEntry = i.next();
			int index = tableEntry.getKey();
			if(index>minimumKeyValue)
			{
				minimumKeyValue = index;
			}
		}

		if(minimumKeyValue!=-1)
		{
			System.out.println("Leader Buffallo is id "+minimumKeyValue);
			leaderBuffallo = buffalloAnimatsInTheWorld.get(minimumKeyValue);
			leaderBuffallo.setLeader(true);
		}

		return leaderBuffallo;// returns null when no leader buffallo is found

	}
	public static Buffallo getNearestBuffallo(SimpleAgent currAnimat)
	{
		Buffallo nearestBuffallo =null;
		//get current agents x and y coordinate

		Location agentLocation = currAnimat.getAnimatLocation();

		double agentX=agentLocation.xCoordinate;
		double agentY=agentLocation.yCoordinate;

		double nearestDistance = 100000000;//really large value

		Set<Entry<Integer, Buffallo>> s= buffalloAnimatsInTheWorld.entrySet();

		Iterator<Entry<Integer,Buffallo>> i = s.iterator(); 

		while(i.hasNext())
		{
			Entry<Integer,Buffallo> tableEntry = i.next();
			Buffallo tempBuffallo = tableEntry.getValue();

			if(tempBuffallo.getId()==currAnimat.getId()) continue;
			Location buffalloLocation = tempBuffallo.getLocation();
			if(buffalloLocation!=null)
			{
				double x = buffalloLocation.xCoordinate; //get the temp Buffaloes location
				double y= buffalloLocation.yCoordinate; 

				double distance = Math.sqrt((x-agentX)*(x-agentX)+(y-agentY)*(y-agentY));

				if(distance<nearestDistance)
				{
					nearestDistance = distance; 
					nearestBuffallo = tempBuffallo;
				}
			}
		}

		return nearestBuffallo;
	}

	public static Lion getNearestLion(SimpleAgent currAnimat)
	{
		Lion nearestLion = null;
		Location agentLocation = currAnimat.getAnimatLocation();
		double agentX=agentLocation.xCoordinate;
		double agentY=agentLocation.yCoordinate;

		double nearestDistance = 100000000;//really large value

		Set<Entry<Integer, Lion>> s= lionAnimatsInTheWorld.entrySet();

		Iterator<Entry<Integer,Lion>> i = s.iterator(); 
		while(i.hasNext())
		{
			Entry<Integer,Lion> tableEntry = i.next();
			Lion tempLion = tableEntry.getValue();

			if(currAnimat.getClass()== tempLion.getClass())
			{
				if(tempLion.getId()==currAnimat.getId()) continue;
			}
			Location LionLocation = tempLion.getLocation();
			if(LionLocation!=null)
			{
				double x = LionLocation.xCoordinate; //get the temp Lion locaton
				double y= LionLocation.yCoordinate; 
				double distance = Math.sqrt((x-agentX)*(x-agentX)+(y-agentY)*(y-agentY));

				if(distance<nearestDistance)
				{	
					nearestDistance = distance; 
					nearestLion = tempLion;
				}
			}
		}
		return nearestLion;
	}

	//Method will return a list of confident buffalloes in the world, who consider themselves leaders
	public static ArrayList<Buffallo> getListOfConfidentBuffallo()
	{
		ArrayList<Buffallo> confident_buffallo_list = new ArrayList<Buffallo>();

		Set<Entry<Integer, Buffallo>> s= buffalloAnimatsInTheWorld.entrySet();

		Iterator<Entry<Integer,Buffallo>> i = s.iterator(); 

		while(i.hasNext())
		{
			Entry<Integer,Buffallo> tableEntry = i.next();
			Buffallo tempBuffallo = tableEntry.getValue();
			//			System.out.println("Finding leader");
			if(tempBuffallo.isLeader()){
				//				System.out.println("Found leader");
				confident_buffallo_list.add(tempBuffallo);
			}
		}

		return confident_buffallo_list;
	}


	//get the nearest leader amongst all possible leaders
	public static Buffallo getNearestLeaderBuffallo(SimpleAgent currAgent)
	{
		Buffallo nearest_leader_buffallo = null;

		Location agentLocation = currAgent.getAnimatLocation();
		double agentX=agentLocation.xCoordinate;
		double agentY=agentLocation.yCoordinate;

		double nearestDistance = 100000000;//really large value

		ArrayList<Buffallo> list_of_leaders = getListOfConfidentBuffallo();
		for(Buffallo temp_buffallo : list_of_leaders)
		{
			double temp_x_coord = temp_buffallo.getLocation().xCoordinate;
			double temp_y_coord = temp_buffallo.getLocation().yCoordinate;

			double distance = getDistance(agentX,agentY,temp_x_coord,temp_y_coord);
			if(distance<nearestDistance)
			{
				nearestDistance = distance;
				nearest_leader_buffallo = temp_buffallo;
			}
		}
		if(nearest_leader_buffallo==null) System.out.println("Debug: Null returned while fetching leader buffallo");
		return nearest_leader_buffallo;
	}

	// -- what is this for ??
	public static boolean isMovementPossibleToPostion(double x, double y)
	{
		Set<Entry<Integer, Lion>> s= lionAnimatsInTheWorld.entrySet();

		Iterator<Entry<Integer,Lion>> i = s.iterator(); 
		while(i.hasNext())
		{
			Entry<Integer,Lion> tableEntry = i.next();
			Lion tempLion = tableEntry.getValue();

			double distance = 10.0;
			if(tempLion != null) 
			{
				Location lion_location = tempLion.getLocation();
				if(lion_location!=null)
					distance= getDistance(x,y,lion_location.xCoordinate,lion_location.yCoordinate);
			}

			if(distance<1) return false;

		}

		Set<Entry<Integer, Buffallo>> p= buffalloAnimatsInTheWorld.entrySet();

		Iterator<Entry<Integer,Buffallo>> j = p.iterator(); 
		while(j.hasNext())
		{
			Entry<Integer,Buffallo> tableEntry = j.next();
			Buffallo tempBuffallo = tableEntry.getValue();

			if(!(tempBuffallo==null)) 
			{
				if(tempBuffallo.getLocation()!=null)
				{
					Location buf_location = tempBuffallo.getLocation();
					{
						double distance = getDistance(x,y,buf_location.xCoordinate,buf_location.yCoordinate);
						if(distance<0.5) return false;
					}
				}
			}


		}
		return true;
	}

	public static int checkBuffalloAggressionLevel(int id)
	{
		Lion temp_lion = lionAnimatsInTheWorld.get(id);
		Location lion_location = temp_lion.getLocation();

		Double x = lion_location.xCoordinate;
		Double y = lion_location.yCoordinate;

		int angry_buffalloes = 0;

		Set<Entry<Integer, Buffallo>> p= buffalloAnimatsInTheWorld.entrySet();

		Iterator<Entry<Integer,Buffallo>> j = p.iterator(); 
		while(j.hasNext())
		{
			Entry<Integer,Buffallo> tableEntry = j.next();
			Buffallo tempBuffallo = tableEntry.getValue();

			if(!(tempBuffallo==null)) 
			{
				if(tempBuffallo.getIsAggressive() == true)
				{
					double distance = getDistance(x,y,tempBuffallo.getLocation().xCoordinate,tempBuffallo.getLocation().yCoordinate);
					if(distance < 5)
					{
						angry_buffalloes++;
					}
				}
			}

		}
		System.out.println("Number of angry buffalloes:"+angry_buffalloes);
		return angry_buffalloes;
	}

	public static Location getNearestShoutingBuffalloLocation(int id)
	{
		Location shouter_location = null;

		Buffallo currBuff = buffalloAnimatsInTheWorld.get(id);

		ArrayList<Buffallo> all_shouting = new ArrayList<Buffallo>();
		Set<Entry<Integer, Buffallo>> s= World.buffalloAnimatsInTheWorld.entrySet();

		Iterator<Entry<Integer,Buffallo>> i = s.iterator(); 

		while(i.hasNext())
		{
			Entry<Integer,Buffallo> tableEntry = i.next();
			Buffallo tempBuffallo = tableEntry.getValue();

			if(tempBuffallo.getIsShouting())
			{
				all_shouting.add(tempBuffallo);
				System.out.println("Shouting");
			}

		}

		double min_distance = 100000;
		for(Buffallo tempBuffallo : all_shouting)
		{
			if(tempBuffallo.getId() == currBuff.getId()) continue;

			double distance = getDistance(currBuff.getLocation().xCoordinate,currBuff.getLocation().yCoordinate,
					tempBuffallo.getLocation().xCoordinate, tempBuffallo.getLocation().yCoordinate);
			if(distance < min_distance)
			{
				min_distance = distance;
				shouter_location = tempBuffallo.getLocation();
			}
		}

		return shouter_location;
	}

	public static double getDistanceFromLeader(SimpleAgent currAgent)
	{
		double distance = -1;

		Location agentLocation = currAgent.getAnimatLocation();
		double agentX=agentLocation.xCoordinate;
		double agentY=agentLocation.yCoordinate;

		Buffallo leaderBuffallo = getNearestLeaderBuffallo(currAgent);

		if(leaderBuffallo!=null)
		{
			Location leader_location = leaderBuffallo.getLocation();
			distance = getDistance(agentX,agentY,leader_location.xCoordinate,
					leader_location.yCoordinate);


		}
		return distance;//check for -1 in calling function
	}

	public static SimpleAgent getNearestReadyToMate(SimpleAgent currAnimat)
	{
		if(currAnimat instanceof Buffallo)
		{
			Buffallo nearestBuffalloReadyToMate =null;
			//get current agents x and y coordinate
			Location agentLocation = currAnimat.getAnimatLocation();

			double agentX=agentLocation.xCoordinate;
			double agentY=agentLocation.yCoordinate;

			double nearestDistance = 100000000;//really large value

			Set<Entry<Integer, Buffallo>> s= World.buffalloAnimatsInTheWorld.entrySet();

			Iterator<Entry<Integer,Buffallo>> i = s.iterator(); 

			while(i.hasNext())
			{
				Entry<Integer,Buffallo> tableEntry = i.next();
				Buffallo tempBuffallo = tableEntry.getValue();


				if(tempBuffallo.getId()==currAnimat.getId() || !tempBuffallo.getIsReadyToMate())
					continue;

				Location buffalloLocation = tempBuffallo.getLocation();
				if(buffalloLocation!=null)
				{
					double x = buffalloLocation.xCoordinate; //get the temp Buffaloes location
					double y= buffalloLocation.yCoordinate; 

					double distance = Math.sqrt((x-agentX)*(x-agentX)+(y-agentY)*(y-agentY));

					if(distance<nearestDistance)
					{
						nearestDistance = distance; 
						nearestBuffalloReadyToMate = tempBuffallo;
					}
				}
			}

			return nearestBuffalloReadyToMate;
		}
		else
		{
			Lion nearestLionReadyToMate =null;
			//get current agents x and y coordinate
			Location agentLocation = currAnimat.getAnimatLocation();

			double agentX=agentLocation.xCoordinate;
			double agentY=agentLocation.yCoordinate;

			double nearestDistance = 100000000;//really large value

			Set<Entry<Integer, Lion>> s= lionAnimatsInTheWorld.entrySet();

			Iterator<Entry<Integer,Lion>> i = s.iterator(); 

			while(i.hasNext())
			{
				Entry<Integer,Lion> tableEntry = i.next();
				Lion tempLion = tableEntry.getValue();


				if(tempLion.getId()==currAnimat.getId() || !tempLion.getIsReadyToMate())
					continue;

				Location lionLocation = tempLion.getLocation();
				if(lionLocation!=null)
				{
					double x = lionLocation.xCoordinate; //get the temp Buffaloes location
					double y= lionLocation.yCoordinate; 

					double distance = Math.sqrt((x-agentX)*(x-agentX)+(y-agentY)*(y-agentY));

					if(distance<nearestDistance)
					{
						nearestDistance = distance; 
						nearestLionReadyToMate = tempLion;
					}
				}
			}
		
			return nearestLionReadyToMate;

		}
	}
	public static double getDistance(double x1, double y1, double x2, double y2)
	{
		return Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
	}

	public static int getNextBuffalloId()
	{
		nextBuffalloId++;
		return nextBuffalloId;
	}
	public static int getNextLionId()
	{
		nextLionId++;
		return nextLionId;
	}

}
