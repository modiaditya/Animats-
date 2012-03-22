package PredatorPrey;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import planet_navi.*;


public class Lion extends SimpleAgent {

	/***************** THE STEP FUNCTION ****************/



	@Override
	public void step() {

		//set own location
		Context context = ContextUtils.getContext(this);
		ContinuousSpace space = (ContinuousSpace)context.getProjection("Continuous Space");

		NdPoint point = space.getLocation(this);

		double x=point.getX();
		double y=point.getY();

		this.setLocation(x, y);
		//Before the step starts, update internal levels for hunger, threat, thirst.
		updateInternalLevels();
		
		

		
		// get the next action to be taken
		int nextAction = getNextAction();
		
		this.setCurrentAction(nextAction);
		/*
		 * 
		 * 1= move lion to buffallo
		 * 2= drink
		 * 4= move lion to water
		 * 5= move lion to den
		 * 6= move lion to mate
		 * 
		 */
		switch(nextAction)
		{
		case 0: ;break;
		case 1: moveLionToBuffallo();break;
		case 2: drink();break;
		case 4: moveLionToWater();break;
		case 5: moveLionToDen(); break;
		case 6: moveLionToMate();break;
		default:			//do nothing
		{
			doNothing();		
		}
		}

		//	Reduce the Lion's energy by one unit

		//Reduce the Lion's water content by one unit

		// Kill the Lion if its energy or water content drops below zero
		//if (this.getEnergy() < 0 || this.getBodyWaterContent()<0)
		if (this.getEnergy() < 0)	
		{
			World.removeLion(this.getId());
			die();
		}

	}

	

	/***************** ATTRIBUTES ****************/

	private double visionSensorDistance = 10; // defines the vision capability
	private double bodyWaterContent;


	/***************** ATTRIBUTE LEVELS *************************/


	private double hungerLevel;
	private double thirstLevel;
	private double matingLevel;
	/***************** ATTRIBUTE WEIGHTS **********************/

	private double weightForHunger;
	private double weightForThirst;
	private double weightForMating;



	/***************** FUNCTIONS ****************/

	public void moveLionToBuffallo()
	{
		Buffallo nearBuffallo = World.getNearestBuffallo(this);

		if(nearBuffallo==null)
		{
			//move lion to random location with 0,0 and 50,50
			moveLionRandomly();
			return;
		}
		else
		{
			double buffX = nearBuffallo.getLocation().xCoordinate;
			double buffY = nearBuffallo.getLocation().yCoordinate;

			if(this.getLocation()!=null)
			{
				double tempX = this.getLocation().xCoordinate;
				double tempY = this.getLocation().yCoordinate;

				double distance = Math.sqrt((tempX-buffX)*(tempX-buffX)+(tempY-buffY)*(tempY-buffY));

				if(World.checkBuffalloAggressionLevel(this.getId())>1)
				{
					//System.out.println("Lion retreating");
					//retreat
					moveLionToDen();
				}

				if (distance<1.0)
				{	
					if(nearBuffallo.getHitPoints()<0)
					{
						//kill and eat buffallo
						this.setEnergy(this.getEnergy()+50);
						nearBuffallo.dieBuffallo();
					}
					else{
						nearBuffallo.decreaseHitPoints(20);
					}
				}

				// implement the sensor vision to only distance of 10
				else if(distance> getVisionSensorDistance())
				{
					moveLionRandomly();
					return;
				}
			}

			moveLion(buffX, buffY);
		}
	}

	public void moveLionToDen()
	{
		Location thisLionLocation = this.getAnimatLocation();
		// check if lion already in the Den
		if((thisLionLocation.xCoordinate <= 10) && (thisLionLocation.yCoordinate>=40))
		{	 // do nothing
			this.setEnergy(this.getEnergy()-1);
		}
		else
		{ 	double xDen, yDen;
		xDen= 0 + (int)(Math.random() * ((5 - 0) + 1)); // random number between 0 and 10
		yDen= 45 + (int)(Math.random() * ((50 - 45) + 1)); // random number between 40 and 50
		moveLion(xDen, yDen);
		}	


	}

	public void moveLionToMate()
	{
		Lion nearest_ready_lion = (Lion) World.getNearestReadyToMate(this);

		if(nearest_ready_lion==null)
		{
			moveLionRandomly();
			return;
			//return;
		}

		// code to add reproduction starts

		// find distance between the buffallo's
		double nearest_ready_lionX= nearest_ready_lion.getLocation().xCoordinate;
		double nearest_ready_lionY= nearest_ready_lion.getLocation().yCoordinate;
		double thisLionX= this.getLocation().xCoordinate;
		double thisLionY= this.getLocation().yCoordinate;

		double distance = Math.sqrt((nearest_ready_lionX-thisLionX)*(nearest_ready_lionX-thisLionX)+(nearest_ready_lionY-thisLionY)*(nearest_ready_lionY-thisLionY));

		if(distance<1.0)
		{
			//this.set
			this.setEnergy(this.getEnergy()/2);
			nearest_ready_lion.setEnergy(this.getEnergy()/2);
			this.setIsReadyToMate(false);
			nearest_ready_lion.setIsReadyToMate(false);
			this.reproduce();
			this.setMatingLevel(0);
		}

		// code to add reproduction ends
		else
		{
			moveLion(nearest_ready_lion.getLocation().xCoordinate,nearest_ready_lion.getLocation().yCoordinate);
		}
	}

	public void moveLion(double destX, double destY)
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

		//reduce energy
		this.setEnergy(this.getEnergy()-1);

		// Move the agent on the space by one unit according to its new heading

		space.moveByDisplacement(this, deltaX,deltaY);
		this.setLocation(x+deltaX, y+deltaY);
	}

	public void moveLionRandomly()
	{
		//move lion to random location with 0,0 and 50,50
		moveLion((int)(Math.random() * ((50 - 0) + 1)), (int)(Math.random() * ((50 - 0) + 1)));


	}


	public void moveLionToWater()
	{
		double waterx = 35+15*Math.random();
		double watery = 35+15*Math.random();
		moveLion(waterx,watery);
	}

	public void drink()
	{
		this.setBodyWaterContent(this.getBodyWaterContent()+50);
	}
	public void updateInternalLevels()
	{
		//Sense energy level and update hunger level
		hungerLevel = (500-this.getEnergy())/5;

		//Sense thirst and update thirst level
		thirstLevel = (500-this.getBodyWaterContent())/5;

		if(this.getAge()>30)
		{
			this.setIsReadyToMate(true);
			this.setMatingLevel(100);
		}
	}

	public void reproduce()
	{
		Parameters p = RunEnvironment.getInstance().getParameters();
		Context context = ContextUtils.getContext(this);
		// Get the reproduction rate from the user parameters
		double rate = (Double)p.getValue("lionreproduce");
		
		int lion_id = World.getNextLionId();
		Lion Lion = new Lion(lion_id);
		World.addLion(lion_id, Lion);
		context.add(Lion);
		

	}
	public void doNothing()
	{
		this.setEnergy(this.getEnergy()-2);	
		this.setBodyWaterContent(this.getBodyWaterContent()-5);
	}

	/***************** CONSTRUCTORS****************/
	// This constructor is used to create an offspring
	public Lion (double energy){
		this.setEnergy(energy);               // assign the offspring energy
		this.setHeading(Math.random()*360);   // randomize the heading from 0-360 degrees
	}

	//Constructor to set the ID for the Lion

	public Lion(int ID)
	{
		this();
		setId(ID);

	}

	// This constructor is used to create initial wolves from the context creator
	public Lion(){
		// Get the value of the lion gain from food from the environment parameters
		Parameters p = RunEnvironment.getInstance().getParameters();
		double gain = (Double)p.getValue("liongainfromfood");

		this.setEnergy(Math.random() * 2 * 50);    // set the initial energy
		this.setHeading(Math.random()*360);          //  and initial heading

		//overriding the energy
		this.setEnergy(this.getEnergy()+50);
		this.setBodyWaterContent(Math.random()* 100); // initial water content
		this.setHungerLevel(this.getEnergy()/5);
		this.setThirstLevel(this.getBodyWaterContent()/5);
		this.setMatingLevel(100*(1+Math.abs(this.getAge()-45)));//peaks at 45 years of age

		// setting random weights between 10 and 50 
		this.setWeightForHunger(10 + (int)(Math.random() * ((50 - 10) + 1)));
		this.setWeightForThirst(10 + (int)(Math.random() * ((50 - 10) + 1)));
		this.setWeightForMating(0);


	}


	/****************** BRAIN *****************************/
	public int getNextAction()
	{
		Context context = ContextUtils.getContext(this);
		ContinuousSpace space = (ContinuousSpace)context.getProjection("Continuous Space");

		NdPoint point = space.getLocation(this);

		double x=point.getX();
		double y=point.getY();

		double maxWeight_Level_Product = -1.0;

		double productForHunger = this.getHungerLevel()*this.getWeightForHunger();
		if(productForHunger>maxWeight_Level_Product) maxWeight_Level_Product = productForHunger;

		double productForThirst = this.getThirstLevel()*this.getWeightForThirst();
		if(productForThirst>maxWeight_Level_Product) maxWeight_Level_Product = productForThirst;

		double productForMating = this.getMatingLevel()*this.getMatingLevel()*this.getWeightForMating();
		if(productForMating> maxWeight_Level_Product)maxWeight_Level_Product= productForMating;

		//if the energy is maximum then move lion to den

		//
		double movetoden=999;
		if(this.getEnergy()>=50)
		{	
			//return 
			//moveLionToDen();
			maxWeight_Level_Product = movetoden;

		}


		//System.out.println(productForHunger+":"+productForThreat+":"+productForThirst);

		if(maxWeight_Level_Product == productForHunger)
		{
			return 1; // move to buffallo and try to kill it and then eat it 

		}

		else if(maxWeight_Level_Product == productForThirst)
		{
			if(x>35 && y>35 && x<50 && y<50)
			{
				return 2; // 2 is drink

			}
			else
			{
				return 4;//move to water
			}
		}

		else if(maxWeight_Level_Product == movetoden)
		{
			return 5;
		}
		
		else if(maxWeight_Level_Product == productForMating)
		{
			return 6;
		}

		return 0; //do nothing
	}


	/***************** GETTERS AND SETTERS ****************/


	// Public getter for the data gatherer for counting 
	@Override
	public int isLion() {
		return 1;
	}

	public void setVisionSensorDistance (double param)
	{
		this.visionSensorDistance=param;

	}

	public double getVisionSensorDistance ()
	{
		return this.visionSensorDistance;

	}

	public void setBodyWaterContent(double waterContent)
	{
		this.bodyWaterContent=waterContent;
	}
	public double getBodyWaterContent()
	{
		return this.bodyWaterContent;
	}


	public double getHungerLevel(){
		return this.hungerLevel;
	}
	public double getThirstLevel(){
		return this.thirstLevel;
	}

	public double getWeightForHunger(){
		return this.weightForHunger;
	}
	public double getWeightForThirst(){
		return this.weightForThirst;
	}


	public void setHungerLevel(double iHungerLevel){
		this.hungerLevel=iHungerLevel;
	}
	public void setThirstLevel(double iThirstLevel){
		this.thirstLevel=iThirstLevel;
	}

	public void setWeightForHunger(double iWeightForHunger){
		this.weightForHunger=iWeightForHunger;
	}
	public void setWeightForThirst(double iWeightForThirst){
		this.weightForThirst=iWeightForThirst;
	}

	public double getMatingLevel()
	{
		return this.matingLevel;
	}

	public void setMatingLevel(double iMatingLevel)
	{
		this.matingLevel=iMatingLevel;
	}

	public void setWeightForMating(double iWeightForMating)
	{
		this.weightForMating= iWeightForMating;
	}

	public double getWeightForMating()
	{
		return this.weightForMating;
	}

}