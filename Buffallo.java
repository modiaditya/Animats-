package PredatorPrey;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.util.ContextUtils;
import planet_navi.*;

public class Buffallo extends SimpleAgent {

	/***************** THE STEP FUNCTION ****************/
	@Override
	public void step() {

		int nextAction;
		//set own location
		Context context = ContextUtils.getContext(this);
		ContinuousSpace space = (ContinuousSpace)context.getProjection("Continuous Space");

		NdPoint point = space.getLocation(this);

		double x=point.getX();
		double y=point.getY();

		this.setLocation(x, y);

		//increment age at every step
		this.incrementAge();

		//Before the step starts, update internal levels for hunger, threat, thirst.
		updateInternalLevels();
		// after the above function call, we will have the value of state_vector which we can use

		if(!stateCapture.containsKey(state_vector))
		{
			setEqualWeights();
			stateCapture.put(state_vector, weightArray);
			
		}
		nextAction = getHighestWeightAction(); 
		for(int i=0;i<weightArray.length;i++)
		{
			System.out.print(this.getId()+","+weightArray[i]+" ");
		}
		System.out.println("::"+nextAction);
		
		
		
		
		
		//After updating internal levels, update buffallo state
		//updateState();

		
		//shout for protection if young start
		// -- check logic here if u want the threat level to be 90 and then shout for help - adi
		if(this.getAge()<15)
		{
			if(this.threatLevel>90)
			{
				//System.out.println("Threat!");
				this.isShouting = true;
				//World.shoutForHelp(this.getId());//randomly shout for help, later change to shout when threat is high
			}
			else{
				this.isShouting = false;
			}
		}
		else
		{
			this.isShouting = false;
		}
		//// shout for protection if young ends
		
		
		
		// store the prev energy and water level for the learn function
		double prevEnergy= this.getEnergy();
		double prevWater= this.getBodyWaterContent();
		double prevHitPoints = this.getHitPoints();
		
		
		// Get the Buffallo gain from food from the user parameters
		Parameters p = RunEnvironment.getInstance().getParameters();
		double gain = (Double)p.getValue("buffallogainfromfood");


		
		
		// set this action as current action
		this.setCurrentAction(nextAction);
		
		
		// debugging logic 
		//System.out.println("ID:"+this.getId()+",AXN:"+ nextAction+",ENERGY:"+this.getEnergy());
			
		try{
			File out=new File("output.txt");
			FileWriter f=new FileWriter(out,true);
			String ss=(getId()+","+",WH:"+getWeightForHunger()+",HL:"+getHungerLevel()+",WgT"+getWeightForThirst()+",TL"+getThirstLevel()+",WT"+getWeightForThreat()+",TtL"+getThreatLevel()+",WMg"+getWeightForMating()+",MgL"+getMatingLevel()+",WgPr"+getWeightForProtectTendency());
			f.write(ss+"\n"+"\n");
			f.close();
		}
		catch(Exception e)
		{

		}

		
		// take necessary action for the action decided.
		switch(nextAction)
		{
		
		case 0: moveBuffalloToFood();break;
		case 1: moveBuffalloToWater();break;
		case 2: moveAwayFromLion(); break;
		case 3: moveToShoutingBuffallo();
		case 4: moveBuffalloToNearestReadyToMate(); break;
		case 5: {
			if(moveBuffalloToLeaderBuffallo()==-1)
			{moveBuffalloToWater();}break;

		}
		default: moveBuffalloRandomly();  //move randomly
		}

		
		// after this state the action is performed
		
		// -- currently feedback is checked only in terms of the energy and water level
		String feedback = learn(nextAction,prevEnergy, prevWater, prevHitPoints );
		System.out.println(feedback);

		shortTermMemory s = new shortTermMemory();
		s.action= nextAction;
		s.reward= feedback;
		s.state= state_vector;
		
		memory.addToMemory(s);
		
		//update all the states in the memory
		updateWeights(feedback);
		
			// death of buffallo
		if (this.getEnergy() < 0 || this.getBodyWaterContent()<0)
		{
			World.removeBuffalo(this.getId());
			die();
		}

	}
	/*****************STATE_VECTOR***********************/
	private String state_vector = "";
	HashMap<String, double[]> stateCapture = new HashMap<String,double[]>();




	/*****************ATTRIBUTES*************************/
	private double bodyWaterContent;
	private double random;
	private boolean isLeader = false;
	static int countOfBuffallo=0;
	private double initialHeading;
	private int initialHeadingCount=0;
	private final int visionRange =50;
	private int confidence = -1;
	private boolean isAggressive = false;
	private boolean isShouting = false;
	private boolean hasMated = false;

	/***************** ATTRIBUTE LEVELS *************************/

	private double protectTendencyLevel;
	private double threatLevel;
	private double hungerLevel;
	private double thirstLevel;
	private final double maxThreatLevel=100;
	private double randomLevel;
	private double matingLevel;
	/***************** ATTRIBUTE WEIGHTS **********************/

	private double weightForProtection;
	private double weightForThreat;
	private double weightForHunger;
	private double weightForThirst;
	private double weightForConfidence;
	private double weightForRandom;
	private double weightForMating;
	
	double weightArray[]= new double[6];
	memoryQueue memory = new memoryQueue();
	/***************** FUNCTIONS ****************************/
	public void updateInternalWeights(int i)
	{
		int countOfWeight =0;
		if(getWeightForHunger()>1)
		{
			setWeightForHunger(getWeightForHunger()-1);
			countOfWeight++;
		}

		if(getWeightForThirst()>1)
		{
			setWeightForThirst(getWeightForThirst()-1);
			countOfWeight++;
		}
		if(getWeightForRandom()>1)
		{
			setWeightForRandom(getWeightForRandom()-1);
			countOfWeight++;
		}
		if(getWeightForThreat()>1)
		{
			setWeightForThreat(getWeightForThreat()-1);
			countOfWeight++;
		}
		if(getWeightForMating()>1)
		{
			setWeightForMating(getWeightForMating()-1);
			countOfWeight++;
		}
		switch(i)
		{
		case 1:
			setWeightForHunger(getWeightForHunger()+countOfWeight);
			break;
		case 2:
			setWeightForThirst(getWeightForThirst()+countOfWeight);
			break;
		case 3:
			setWeightForRandom(getWeightForRandom()+countOfWeight);
			break;
		case 4:
			setWeightForThreat(getWeightForThreat()+countOfWeight);
			break;
		case 5:
			setWeightForMating(getWeightForMating()+countOfWeight);
			break;

		}

	}

	public void updateState()
	{
		state_vector = "";
		if(this.hungerLevel>50)state_vector+="1"; else state_vector+="0";
		if(this.thirstLevel>50)state_vector+="1"; else state_vector+="0";
		if(this.matingLevel>50)state_vector+="1"; else state_vector+="0";
		if(this.threatLevel>50)state_vector+="1"; else state_vector+="0";
	}

	public void updateInternalLevels()
	{
		state_vector= "";

		//update hunger level
		if(this.getEnergy()<20) 
		{
			this.setHungerLevel(100);
			state_vector = state_vector +"3"; 
		}
		else if(this.getEnergy()>20 && this.getEnergy()<50)
		{
			this.setHungerLevel(60);
			state_vector = state_vector +"2";
		}	
		else 
		{
			this.setHungerLevel(0);
			state_vector = state_vector +"1";
		}
		
		//update thirst level
		if(this.getBodyWaterContent()<20) 
		{
			this.setThirstLevel(100);
			state_vector = state_vector +"3";
		}
		else if(this.getBodyWaterContent()>20 && this.getBodyWaterContent()<50)
		{
			this.setThirstLevel(60);
			state_vector = state_vector +"2";
		}
		else 
		{
			this.setThirstLevel(30);
			state_vector = state_vector +"1";
		}
		
		//update protection level
		if(this.getAge()>25)
		{
			Location shouter_location = World.getNearestShoutingBuffalloLocation(this.getId());
			if(shouter_location!=null)
			{
				double distance = World.getDistance(shouter_location.xCoordinate, shouter_location.yCoordinate, 
						this.getLocation().xCoordinate, this.getLocation().yCoordinate);

				if(distance<2.5)
				{
					this.setProtectTendencyLevel(120);
					state_vector = state_vector +"3";
				}
				else if(distance >2.5 && distance <5)
				{
					this.protectTendencyLevel=65;
					state_vector = state_vector +"2";
				}
				else
				{
					this.protectTendencyLevel = 0;
					state_vector = state_vector +"1";
				}
			}
			else
			{
				this.protectTendencyLevel = 0;
				state_vector = state_vector +"1";
				//System.out.println("p");
			}
				
		}
		else
		{
			this.protectTendencyLevel = 0;
			state_vector = state_vector +"1";
		}

		//update threat level
		Lion nearestLion =World.getNearestLion(this);
		if(nearestLion!=null)
		{
			Location nearestLionLocation= nearestLion.getLocation();
			double lionX= nearestLionLocation.xCoordinate;
			double lionY = nearestLionLocation.yCoordinate;


			Context context = ContextUtils.getContext(this);
			ContinuousSpace space = (ContinuousSpace)context.getProjection("Continuous Space");

			NdPoint point = space.getLocation(this);

			double x=point.getX();
			double y=point.getY();

			double distanceBetween=getDistance(x,y,lionX,lionY);
			
			if(distanceBetween<2.5)
			{
				this.setThreatLevel(120);
				state_vector = state_vector +"3";
			}
			else if(distanceBetween>2.5 && distanceBetween<5)
			{
				this.setThreatLevel(60);
				state_vector = state_vector +"2";
			}
			else if(distanceBetween>5&&distanceBetween<10)
			{
				this.setThreatLevel(0);
				state_vector = state_vector +"1";
			}
			else
			{
				this.setThreatLevel(0);
				state_vector = state_vector +"1";
			}
		}
		else
		{
			threatLevel=0;
			state_vector = state_vector +"1";
		}

		//update mating level
		if(!hasMated)
		{
			if(getAge()>30 && getAge()<40)
			{
				this.matingLevel = 100;
				setIsReadyToMate(true);
				state_vector = state_vector +"3";
			}
			else if(this.getAge()<30 && this.getAge()>15)
			{
				this.matingLevel = 60;
				setIsReadyToMate(true);
				state_vector = state_vector +"2";
			}
			else 
			{
				this.matingLevel = 0;
				setIsReadyToMate(false);
				state_vector = state_vector +"1";
			}
		}
		else
		{
			this.matingLevel = 0;
			state_vector = state_vector +"0";
		}

		//make leader
		if(this.getConfidence()>=4)
			this.setLeader(true);

		try{
			File out=new File("outputStateVectors.txt");
			FileWriter f=new FileWriter(out,true);
			
			f.write(state_vector+"\n");
			f.close();
		}
		catch(Exception e)
		{

		}
		System.out.println("-------------------"+state_vector+"---------------------");

	}
		public void reproduce()
	{
		Parameters p = RunEnvironment.getInstance().getParameters();
		Context context = ContextUtils.getContext(this);
		// Get the reproduction rate from the user parameters
		double rate = (Double)p.getValue("buffalloreproduce");
		int buf_id = World.getNextBuffalloId();
		Buffallo buffallo = new Buffallo(buf_id);
		World.addBuffallo(buf_id, buffallo);
		context.add(buffallo);
	
	}

	public void moveBuffalloToWater()
	{
		if(this.getLocation().xCoordinate>35 && this.getLocation().yCoordinate>35 && this.getLocation().xCoordinate<50 && this.getLocation().yCoordinate<50)
		{
			drink();
			return;
		}
		
		double waterx = 35+15*Math.random();
		double watery = 35+15*Math.random();
			moveBuffallo(waterx,watery);

	}
	public void moveBuffalloToFood()
	{
		
		if(this.getLocation().xCoordinate>0 && this.getLocation().yCoordinate>0 && this.getLocation().xCoordinate<15 && this.getLocation().yCoordinate<15)
		{
			eat();
			return;
		}
		double grassx = 15*Math.random();
		double grassy = 15*Math.random();
			moveBuffallo(grassx,grassy);
	}
	public int moveBuffalloToLeaderBuffallo()
	{
		Buffallo nearestLeaderBuffallo = World.getNearestLeaderBuffallo(this);
		if(nearestLeaderBuffallo==null)
		{
			Buffallo leaderBuffallo = World.getLeaderBuffallo(this);
			if(leaderBuffallo ==null)
			{
				//System.out.println("Cant find leader");
				return -1;
			}
		}
		else
		{
			//System.out.println("Follow my leader");
			moveBuffallo(nearestLeaderBuffallo.getLocation().xCoordinate,nearestLeaderBuffallo.getLocation().yCoordinate);
			return 0;
		}
		return -1;
	}
	public void moveBuffalloToNearestBuffallo()
	{

		Buffallo nearBuffallo = World.getNearestBuffallo(this);
		if(nearBuffallo==null)
		{
			return;
		}
		else
		{
			moveBuffallo(nearBuffallo.getLocation().xCoordinate,nearBuffallo.getLocation().yCoordinate);
		}

	}

	public void moveBuffalloToNearestReadyToMate()
	{
		Buffallo nearBuffallo = (Buffallo) World.getNearestReadyToMate(this);


		if(nearBuffallo==null)
		{
			moveBuffalloRandomly();
			return;
		}

		// code to add reproduction starts

		// find distance between the buffallo's
		double nearBuffalloX= nearBuffallo.getLocation().xCoordinate;
		double nearBuffalloY= nearBuffallo.getLocation().yCoordinate;
		double thisBuffalloX= this.getLocation().xCoordinate;
		double thisBuffalloY= this.getLocation().yCoordinate;

		double distance = Math.sqrt((nearBuffalloX-thisBuffalloX)*(nearBuffalloX-thisBuffalloX)+(nearBuffalloY-thisBuffalloY)*(nearBuffalloY-thisBuffalloY));

		if(distance<1.0)
		{
			//this.set
			this.setEnergy(this.getEnergy()/2);
			nearBuffallo.setEnergy(this.getEnergy()/2);
			this.setIsReadyToMate(false);
			this.hasMated = true;
			nearBuffallo.setIsReadyToMate(false);
			this.reproduce();
			this.setMatingLevel(0);
		}

		// code to add reproduction ends
		else
		{
			moveBuffallo(nearBuffallo.getLocation().xCoordinate,nearBuffallo.getLocation().yCoordinate);
		}

	}

	public void moveBuffallo(double destX, double destY)
	{
		Context context = ContextUtils.getContext(this);
		ContinuousSpace space = (ContinuousSpace)context.getProjection("Continuous Space");

		NdPoint point = space.getLocation(this);

		double x=point.getX();
		double y=point.getY();

		this.setLocation(x, y);

		double deltaX = 0.0;
		double deltaY = 0.0;

		double dy=destY-y;
		double dx=destX-x;


		double denominator = Math.sqrt((dx*dx) + (dy*dy));

		if(denominator!=0.0)
		{
			deltaX = dx/denominator;
			deltaY = dy/denominator;
		}

		// Move the agent on the space by one unit according to its new heading

		if(World.isMovementPossibleToPostion(x+deltaX,y+deltaY))
		{
			space.moveByDisplacement(this, deltaX, deltaY);
		}
		else
			moveBuffalloRandomly(); //try to move randomly
	}

	public void moveAwayFromLion()
	{
		Lion nearestLion =World.getNearestLion(this);
		Location nearestLionLocation= nearestLion.getLocation();
		double lionX= nearestLionLocation.xCoordinate;
		double lionY = nearestLionLocation.yCoordinate;


		Context context = ContextUtils.getContext(this);
		ContinuousSpace space = (ContinuousSpace)context.getProjection("Continuous Space");

		NdPoint point = space.getLocation(this);

		double x=point.getX();
		double y=point.getY();

		this.setLocation(x, y);

		double deltaX = 0.0;
		double deltaY = 0.0;

		double dy=lionY-y;
		double dx=lionX-x;


		double denominator = Math.sqrt((dx*dx) + (dy*dy));

		if(denominator!=0.0)
		{
			deltaX = dx/denominator;
			deltaY = dy/denominator;
		}

		// Move the agent on the space by one unit according to its new heading

		space.moveByDisplacement(this, -deltaX,-deltaY);


	}
	public void moveToShoutingBuffallo()
	{
		Location shouter_location = World.getNearestShoutingBuffalloLocation(this.getId());
		if(shouter_location !=null)
		{
			double distance = World.getDistance(shouter_location.xCoordinate, shouter_location.yCoordinate,
					this.getLocation().xCoordinate, this.getLocation().yCoordinate);
			//			World.gruntAggressive(this.getId());

			if(distance <5)
			{
				this.isAggressive = true;
				moveBuffallo(shouter_location.xCoordinate,shouter_location.yCoordinate);
			}
		}
		else
		{
			this.isAggressive = false;
		}
	}
	public void dieBuffallo()
	{
		World.removeBuffalo(this.getId());
		die();

	}

	// Public getter for the data gatherer for counting buffallo
	@Override
	public int isBuffallo() {
		return 1;
	}

	public void eat()
	{
		// check confidence and update if not confident
		if(this.getConfidence() ==-1)
		{
			this.setConfidence(this.getConfidence()+1);// setting intial confidence
			return;
		}

		if(this.getConfidence()<=3)
			this.setConfidence(getConfidence()+1);

		this.setEnergy(this.getEnergy()+10);
	}

	public void drink()
	{	int con = this.getConfidence();
	if(con ==-1)
	{
		this.setConfidence(this.getConfidence()+1);// setting intial confidence
		return;
	}
	if(con<=2 )
		this.setConfidence(getConfidence()+1);

	this.setBodyWaterContent(this.getBodyWaterContent()+10);
	}



	// function to get distance between two points
	public double getDistance(double x1, double y1, double x2, double y2)
	{
		return Math.sqrt((x2-x1)*(x2-x1)-(y2-y1)*(y2-y1));


	}

	public void incrementAge()
	{
		setAge(getAge()+1);
	}


	public static Buffallo getNearestBuffalloReadyToMate(SimpleAgent currAnimat)
	{
		Buffallo nearestBuffalloReadyToMate =null;
		//get current agents x and y coordinate
		//System.out.print("asdasda");
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
		//		System.out.println(nearestBuffalloReadyToMate.getId()+" is ready to mate "+nearestBuffalloReadyToMate.getIsReadyToMate());

		return nearestBuffalloReadyToMate;
	}


	public void moveBuffalloRandomly()
	{
		Context context = ContextUtils.getContext(this);
		ContinuousSpace space = (ContinuousSpace)context.getProjection("Continuous Space");


		int count = getInitialHeadingCount();

		if(count < 5)
		{
			count++;
			setInitialHeadingCount(count);
		}
		else
		{
			setInitialHeadingCount(0);
			// random by 360 degrees
			setInitialHeading(Math.random()*Math.PI*2);
		}
		space.moveByVector(this, 1, this.getInitialHeading(),0,0);
		
	}

	/***************** Sensor Range **************************/


	public boolean checkIfFoodInVision()
	{
		if((this.getLocation().xCoordinate-15) < visionRange && (this.getLocation().yCoordinate - 15 < visionRange))
			return true;
		else
			return false;

	}

	public boolean checkIfWaterInVision()
	{
		if((this.getLocation().xCoordinate-35 < visionRange) && (this.getLocation().yCoordinate - 35) < visionRange)
			return true;
		else
			return false;

	}



	/****************** BRAIN *****************************/
	public void setEqualWeights()
	{
		for(int i=0;i<weightArray.length;i++)
			weightArray[i]=10;	
	}
	
	public int getHighestWeightAction()
	{
		int maxIndex =0;
		double max= weightArray[0];
		
		for(int i=1;i<weightArray.length;i++)
		{
			if(max< weightArray[i])
			{
				max = weightArray[i];
				maxIndex = i;
			}
		}
		
		// code to return random in case the weights are freshly initialized
		// if max == 10, it means that the weights are initialized and not updated
		
		if(max == 10)
		{
			if(getAge()%15 ==0)
			{
				Random randomNumber = new Random();
				maxIndex= randomNumber.nextInt(6);
				
			}
			else
				maxIndex= this.getCurrentAction();
		}
		return maxIndex;
	}
	
	public void updateWeights(String feedback)
	{
		String iStateVectors="";
		
		if(feedback.equalsIgnoreCase("NO_CHANGE"))
			return;
		else if(feedback.equalsIgnoreCase("POSITIVE"))
		{
			for(int i=0;i<memory.size;i++)
			{
				if(memory.data[i]==null)
					break;
				
				increaseWeight(memory.data[i].state, memory.data[i].action);
				
			}
			
		}
		else if(feedback.equalsIgnoreCase("NEGATIVE"))
		{
			for(int i=0;i<memory.size;i++)
			{
				if(memory.data==null)
					break;
				
				decreaseWeight(memory.data[i].state, memory.data[i].action);
				
			}
			
		}
		
		
		
	}
	
	public void decreaseWeight(String iStateVector, int iNextAction)
	{
		double[] iWeightArray=stateCapture.get(iStateVector);
		for(int i=0;i<iWeightArray.length;i++)
		{
			if(i==iNextAction)
				iWeightArray[i]=iWeightArray[i]-0.167*25;
			else
			{
				iWeightArray[i]=iWeightArray[i]+0.167*5;
			}
			
		}
		stateCapture.remove(iStateVector);
		stateCapture.put(iStateVector, iWeightArray);
		
	}
	
	public void increaseWeight(String iStateVector, int iNextAction)
	{
		double[] iWeightArray=stateCapture.get(iStateVector);
		for(int i=0;i<iWeightArray.length;i++)
		{
			if(i==iNextAction)
				iWeightArray[i]=iWeightArray[i]+0.167*5;
			else
			{
				iWeightArray[i]=iWeightArray[i]-0.167;
			}
			
		}
		stateCapture.remove(iStateVector);
		stateCapture.put(iStateVector, iWeightArray);
		
	}

	public String learn(int nextAction , double prevEnergy, double prevWater, double prevHitPoints)
	{
		// update internal weights
		/*
		 * 	1= hunger
		 * 	2= thirst
		 * 	3= random
		 * 	4= threat
		 * 	5= mating
		 */
		
	
		double currentEnergy =  this.getEnergy();
		double currentWater = this.getBodyWaterContent();
		double currentHitPoints = this.getHitPoints();
		
	
		if(currentEnergy > prevEnergy)// indicates eating food increased energy level
		{
			return "POSITIVE";

		}	

		else if(currentWater > prevWater )
		{
			return "POSITIVE";

		}
		else if(currentHitPoints < prevHitPoints)
		{
			return "NEGATIVE";
		}
		
		return "NO_CHANGE";
	}

	/****************** CONSTRUCTORS ***********************/
	// This constructor is used to create an offspring
	public Buffallo(double energy){
		this();
		this.setEnergy(energy);               // assign the offspring energy
		this.setBodyWaterContent(energy);
		this.setHeading(Math.random()*360);   // randomize the heading from 0-360 degrees
	}


	//Constructor to set the ID for the Buffallo
	public Buffallo(int ID)
	{
		this();
		countOfBuffallo=ID;
		setId(ID);

	}


	// This constructor is used to create initial Buffallo from the context creator
	public Buffallo(){
		//	 Get the value of the sheeep gain from food from the environment parameters
		Parameters p = RunEnvironment.getInstance().getParameters();
		double gain = (Double)p.getValue("buffallogainfromfood");




		int randomAge= (int)(10*Math.random());
		this.setEnergy(Math.random() * 100);    // set the initial energy
		this.setHeading(Math.random()*360);          //  and initial heading
		this.setBodyWaterContent(Math.random()* 100); // initial water content
		this.setRandom(100); 						// set random
		this.setInitialHeading(Math.random()*Math.PI*2);// set random heading for 360 degrees
		this.setProtectTendencyLevel(10);
		this.setAge(randomAge);

		this.setHungerLevel(this.getEnergy()/5);
		this.setThirstLevel(this.getBodyWaterContent()/5);
		this.setThreatLevel(0);
		this.setRandomLevel(this.getRandom()/5); 
		this.setHitPoints(100);
		this.setWeightForHunger(20);
		this.setWeightForThirst(20);
		this.setWeightForRandom(0);
		this.setWeightForThreat(20);
		this.setWeightForMating(20);
		this.setWeightForProtectTendency(20);


		this.setLeader(false);

	
	}
	/****************** Getters and Setters *******************/
	public void setBodyWaterContent(double waterContent)
	{
		this.bodyWaterContent=waterContent;
	}
	public double getBodyWaterContent()
	{
		return this.bodyWaterContent;
	}

	public void setRandom(double iRandom)
	{
		this.random= iRandom;
	}

	public double getRandom()
	{
		return this.random;
	}
	public double getThreatLevel(){

		return this.threatLevel;
	}
	public double getHungerLevel(){
		return this.hungerLevel;
	}
	public double getThirstLevel(){
		return this.thirstLevel;
	}

	public double getMatingLevel()
	{
		return this.matingLevel;
	}

	public void setMatingLevel(double iMatingLevel)
	{
		this.matingLevel=iMatingLevel;
	}


	public double getWeightForThreat(){
		return this.weightForThreat;
	}
	public double getWeightForHunger(){
		return this.weightForHunger;
	}
	public double getWeightForThirst(){
		return this.weightForThirst;
	}

	public void setThreatLevel(double iThreatLevel){
		this.threatLevel=iThreatLevel;
	}
	public void setHungerLevel(double iHungerLevel){
		this.hungerLevel=iHungerLevel;
	}
	public void setThirstLevel(double iThirstLevel){
		this.thirstLevel=iThirstLevel;
	}

	public double getRandomLevel(){
		return this.randomLevel;
	}

	public void setRandomLevel(double iRandomLevel){
		this.randomLevel =  iRandomLevel;
	}
	public void setWeightForThreat(double iWeightForThreat){
		this.weightForThreat=iWeightForThreat;
	}
	public void setWeightForHunger(double iWeightForHunger){
		this.weightForHunger=iWeightForHunger;
	}
	public void setWeightForThirst(double iWeightForThirst){
		this.weightForThirst=iWeightForThirst;
	}
	public void setWeightForRandom(double iWeightForRandom){
		this.weightForRandom =  iWeightForRandom;

	}
	public double getWeightForRandom()
	{

		return this.weightForRandom;
	}

	public void setWeightForMating(double iWeightForMating)
	{
		this.weightForMating= iWeightForMating;
	}

	public double getWeightForMating()
	{
		return this.weightForMating;
	}
	public boolean isLeader()
	{
		return this.isLeader;
	}

	public void setLeader(boolean iIsLeader)
	{
		this.isLeader = iIsLeader;
	}
	public void setInitialHeading(double iInitialHeading)
	{
		this.initialHeading= iInitialHeading;
	}
	public double getInitialHeading()
	{
		return this.initialHeading;
	}

	public void setInitialHeadingCount(int iInitialHeadingCount)
	{
		this.initialHeadingCount =  iInitialHeadingCount;
	}

	public int getInitialHeadingCount()
	{
		return initialHeadingCount;
	}

	public int getConfidence()
	{
		return this.confidence;
	}

	public void setConfidence(int iConfidence)
	{
		this.confidence= iConfidence;
	}

	public double getProtectTendencyLevel()
	{
		return this.protectTendencyLevel;
	}

	public void setProtectTendencyLevel(double protectTendency)
	{
		this.protectTendencyLevel = protectTendency;
	}

	public double getWeightForProtectTendency()
	{
		return this.weightForProtection;
	}

	public void setWeightForProtectTendency(double pWeightForProtection)
	{
		this.weightForProtection = pWeightForProtection;
	}

	public boolean getIsAggressive()
	{
		return this.isAggressive;
	}

	public void setIsAggressive(boolean pIsAggressive)
	{
		this.isAggressive = pIsAggressive;
	}
	public boolean getIsShouting()
	{
		return this.isShouting;
	}

	public void setIsShouting(boolean pIsShouting)
	{
		this.isShouting = pIsShouting;
	}


	/****************************************************/

}			