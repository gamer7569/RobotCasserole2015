
package org.usfirst.frc.team1736.robot;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeMap;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.CANJaguar;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.Gyro;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.PIDSource.PIDSourceParameter;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class CIMShady extends IterativeRobot {
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
	//*Constants*
	
	///////////////////////////////////////////////////////////////////////////////
	//Log objects
	///////////////////////////////////////////////////////////////////////////////
	
	BufferedWriter log;
	String filename = "/usr/local/logs/gyro_log";
	boolean enable_log = true;
	boolean log_open = false;
	
	
	//-Component IDS
	final static int JOY1_INT = 0;
	final static int JOY2_INT = 1;
	final static boolean SINGLE_JOYSTICK_IS_BEST_JOYSTICK = false;
	
	final static int LEFTROBOT_FRONTMOTOR_ID = 0;
	final static int LEFTROBOT_BACKMOTOR_ID = 1;
	final static int RIGHTROBOT_FRONTMOTOR_ID = 3;
	final static int RIGHTROBOT_BACKMOTOR_ID = 2;
	final static int SLIDE_MOTOR_ID = 4;
	
	//-Controller Buttons
	final static int XBOX_A_BUTTON = 1;
	final static int XBOX_B_BUTTON = 2;
	final static int XBOX_X_BUTTON = 3;
	final static int XBOX_Y_BUTTON = 4;
	final static int XBOX_LEFT_BUTTON = 5;
	final static int XBOX_RIGHT_BUTTON = 6;
	final static int XBOX_SELECT_BUTTON = 7;
	final static int XBOX_START_BUTTON = 8;
	final static int XBOX_LSTICK_BUTTON = 9;
	final static int XBOX_RSTICK_BUTTON = 10;
	
	//-Controller Axes
	final static int XBOX_LSTICK_XAXIS = 0;
	final static int XBOX_LSTICK_YAXIS = 1;
	final static int XBOX_LTRIGGER_AXIS = 2;
	final static int XBOX_RTRIGGER_AXIS = 3;
	final static int XBOX_RSTICK_XAXIS = 4;
	final static int XBOX_RSTICK_YAXIS = 5;
	
	//-Controller D-Pad POV Hat
	final static int XBOX_DPAD_POV = 0;
	
	//-Slide Correction Tuning Value
	final static double slideTune = .5;
	
	//PID Values
	final static double P = 1.3;
	final static double I = 0;
	final static double D = 2.0;
	final static double AUTO_P = 0.35;
	final static double AUTO_I = 0;
	final static double AUTO_D = 0.5;

	//-Elevator
	final static int ELEVATOR_MOTOR_ID = 1;
	final static double ELEVATOR_P = 1;
	final static double ELEVATOR_I = 0;
	final static double ELEVATOR_D = 0;
	final static int ENCODER_A = 0;
	final static int ENCODER_B = 1;
	final static int BOTTOM_SENSOR_ID = 14;
	final static int TOP_SENSOR_ID = 12;
	final static int MIDDLE_SENSOR_ID = 13;
	final static double MIN_RETRACTED_LEVEL = 1;
	final static int LEFT_DISTANCE_ID = 6;
	final static int RIGHT_DISTANCE_ID = 7;
	final static int MIN_RETRACTED_HEIGHT = 10;
	
	boolean Elev_PID = true;
	boolean goDown = false;

//	//-Gyro Values
//    final static int GYRO_ID = 0;
//    final static double GYRO_SENSITIVITY = 0.007;
    double gyroValue;
    
    //-Compressor IDs
    final static int PRESSURE_SENSOR_ID = 2;
	
    //-Closed/Open Loop
    final static boolean openLoop = true;
	
    //-Autonomous mode
    int autonomousMode = 0;
    
    //LRButton Debounce
    boolean lastL_ButtonValue = false;
    boolean lastR_ButtonValue = false;
    
    //AXButton Debounce
    boolean lastA_ButtonValue = false;
    boolean lastX_ButtonValue = false;
    
	//*Declaring robot parts*
	//-Joystick
	Joystick joy1;
	Joystick joy2;
	
	//-DriveTrain Motors
	VictorSP frontLeftMotor;
	VictorSP backLeftMotor;
	VictorSP frontRightMotor;
	VictorSP backRightMotor;
	VictorSP slideMotor;
	
	//-Compressor
	Compressor compressor;
	
	//-Solenoids
	Solenoid solenoidIn;
	Solenoid solenoidOut;
	Solenoid solenoidOpenClose;
	
	boolean current_SolOpenClose_value = false;
	
	//-Elevator
	Elevator elevator;
	
	//-DriveTrain
	SlideTrain slideTrain;	
	
//	//-Gyro
//	Gyro gyro;
	I2CGyro gyro;
	
	//-Lidar
	LIDAR lidar;
	
	//-Treemap for Elevator Levels
//	TreeMap<Integer, Double> levels;
	//0 is starting level
	int currentLevel = 0;
	
	AnalogInput pressureSensor;
	
	double[] levels = {0, 10, 18, 32, 42.5};
	//Low sensor is about 6 inches, high is about 43 inches
	
	//Autonomous Testing Variables
	double straightDistance = 25;
	double turnAngle = 90;
	double currentStep = 0;
	double stepTimer = 0;
	
    public void robotInit() {

    	//Joystick
    	joy1 = new Joystick(JOY1_INT);
    	if(SINGLE_JOYSTICK_IS_BEST_JOYSTICK)
    		joy2 = joy1;
    	else
    		joy2 =  new Joystick(JOY2_INT);
    	
    	//Motors
    	frontLeftMotor = new VictorSP(LEFTROBOT_FRONTMOTOR_ID);
    	backLeftMotor = new VictorSP(LEFTROBOT_BACKMOTOR_ID);
    	frontRightMotor = new VictorSP(RIGHTROBOT_FRONTMOTOR_ID);
    	backRightMotor = new VictorSP(RIGHTROBOT_BACKMOTOR_ID);
    	slideMotor = new VictorSP(SLIDE_MOTOR_ID);
    	
//    	//Gyro
//    	gyro = new Gyro(GYRO_ID);
//		gyro.initGyro();
//		gyro.setPIDSourceParameter(PIDSourceParameter.kAngle);
//		gyro.setSensitivity(GYRO_SENSITIVITY);
		//Gyro
		gyro = new I2CGyro();
		
    	//Drive Train
    	slideTrain = new SlideTrain(frontLeftMotor, backLeftMotor, frontRightMotor, backRightMotor, slideMotor, gyro, P, I, D);
    	slideTrain.enable();
    	
    	//Compressor
		compressor = new Compressor(2);
		//compressor.clearAllPCMStickyFaults();
		compressor.setClosedLoopControl(true);
		compressor.start();
		pressureSensor = new AnalogInput(PRESSURE_SENSOR_ID);
		
		//Elevator
		solenoidIn = new Solenoid(2, 2);
		solenoidOut = new Solenoid(2, 1);
    	solenoidOpenClose = new Solenoid(2, 0);
    	elevator = new Elevator(ELEVATOR_MOTOR_ID, ELEVATOR_P, ELEVATOR_I, ELEVATOR_D, ENCODER_A, ENCODER_B, BOTTOM_SENSOR_ID, TOP_SENSOR_ID, MIDDLE_SENSOR_ID, RIGHT_DISTANCE_ID, LEFT_DISTANCE_ID);
		elevator.enable();
		elevator.setSetpoint(levels[currentLevel]);
		elevator.setIsRetracted(true);
    	
    	lidar = new LIDAR();
    	lidar.start();
    	
    	SmartDashboard.putNumber("Autonomous Mode:", autonomousMode);
    }

    public void autonomousInit() {
    	
    	SmartDashboard.getNumber("Autonomous Mode:");
    	slideTrain.lastTime = Timer.getFPGATimestamp();
    	slideTrain.getPIDController().setPID(AUTO_P, AUTO_I, AUTO_D);
    	slideTrain.zeroAngle();
    }
    
    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {
    	
    	if(currentStep == 0) //Initialization/Zeroing
    	{
    		solenoidIn.set(false);
    		solenoidOut.set(true);
    		elevator.setIsRetracted(false);
    		elevator.disable();
    		elevator.setMotorSpeed(-0.75);
    		if(elevator.bottomSensor.get())
    		{
    			elevator.getPIDController().reset();
    			elevator.raw_encoder_bottom_offset = -elevator.get_raw_encoder();
    			elevator.setSetpoint(0);
        		currentLevel = 0;
        		elevator.getPIDController().enable();
        		currentStep = 1;
        		stepTimer = Timer.getFPGATimestamp();
    		}
    	}

    	switch(autonomousMode)
    	{
    	case 0: //barrel pickup only, currently assuming back enough to zero
    		if(currentStep == 1)
    		{
    			elevator.setSetpoint(6);
    			if(elevator.onTarget())
    			{
    				currentStep = 2;
    				stepTimer = Timer.getFPGATimestamp();
    			}
    		}
    		if(currentStep == 2 && Timer.getFPGATimestamp() - stepTimer > 0.75)
    		{
    			solenoidIn.set(true);
    			solenoidOut.set(false);
    			elevator.setIsRetracted(true);
    			if(slideTrain.driveStraight(straightDistance))
    				currentStep = 3;
    		}
    		if(currentStep == 3)
    		{
    			solenoidOpenClose.set(true);
    			current_SolOpenClose_value = true;
	    		currentStep = 4;
	    		stepTimer = Timer.getFPGATimestamp();
    		}
    		else if(currentStep == 4 && Timer.getFPGATimestamp() - stepTimer > 0.75)
    		{
    			elevator.setSetpoint(11);
	    		if(elevator.onTarget())
	    		{
	    			elevator.setIsAbove(true);
	    			currentStep = 5;
	    			stepTimer = Timer.getFPGATimestamp();
	    		}
    		}
    		else if(currentStep == 5 && Timer.getFPGATimestamp() - stepTimer > 0.75)
    		{
    			slideTrain.turnLeft(90);
    			if(slideTrain.onTarget())
    			{
    				currentStep = 6;
    				//Turning makes the encoder get off, so this "resets" it
    				straightDistance = slideTrain.rightEncoder.getDistance() + 36;
    			}
    		}
    		else if(currentStep == 6)
    		{
    			slideTrain.driveStraight(straightDistance);
    			currentLevel = 1;
    		}
    		break;
    	case 1:
    		
    		break;
    	case 2:
    		
    		break;
    		
    	case 3:
    		
    		break;
    	}

		frontRightMotor.set(-slideTrain.frontRightMotorValue);
		frontLeftMotor.set(slideTrain.frontLeftMotorValue);
		backRightMotor.set(-slideTrain.backRightMotorValue);
		backLeftMotor.set(slideTrain.backLeftMotorValue);
		slideMotor.set(slideTrain.slideMotorValue);
    	
    }

    public void teleopInit() {
    	
    	slideTrain.lastTime = Timer.getFPGATimestamp();
    	solenoidIn.set(false);
    	solenoidOut.set(false);
    	slideTrain.getPIDController().setPID(P, I, D);
    	slideTrain.zeroAngle();
    	
		if(enable_log){
			String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
			try {
				log = new BufferedWriter(new FileWriter(filename+timeStamp+".csv"));
				log.write("Initalized log at " + timeStamp);
				log.newLine();
				log_open = true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.out.println("ERROR COULD NOT OPEN GYRO LOG FILE");
				log_open = false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log_open = false;
			}
		}
    }
    
    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
		if(log_open){
			try {
				log.write(""+Timer.getFPGATimestamp()+",");
				log.write(""+gyro.get_gyro_z()+",");
				log.write(""+gyro.get_gyro_status()+",\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    	
    	
    	if(joy2.getRawButton(XBOX_LSTICK_BUTTON) && joy2.getRawButton(XBOX_RSTICK_BUTTON) && !elevator.isRetracted)
    	{
    		elevator.disable();
    		Elev_PID = false;
    		goDown = true;
    		elevator.setMotorSpeed(-0.5);
    	}
    	
    	SmartDashboard.putNumber("Lidar", lidar.getDistanceIn());
    	
    	//acquire gyroscope value, convert to radians, wrap to proper range
    	gyroValue = (gyro.get_gyro_angle()*(Math.PI/180)) % (2*Math.PI);
    	if(gyro.get_gyro_angle() < 0)
    		gyroValue += 2*Math.PI;
    	
    	if(joy1.getPOV(XBOX_DPAD_POV) != -1)
    	{
    		int dpadVal = joy1.getPOV(XBOX_DPAD_POV);
    		if(dpadVal == 0)
    		{
    			slideTrain.arcadeDrive(0.5, 0, 0, slideTune, true);
    		}
    		else if(dpadVal == 90)
    		{
    			slideTrain.arcadeDrive(0, 0, 0.8, slideTune, true);
    		}
    		else if(dpadVal == 180)
    		{
    			slideTrain.arcadeDrive(-0.5, 0, 0, slideTune, true);
    		}
    		else if(dpadVal == 270)
    		{
    			slideTrain.arcadeDrive(0, 0, -0.8, slideTune, true);
    		}
    	}
    	//Run either open- or closed-loop control, depending on what is requested
    	else if(!openLoop)
    		slideTrain.PIDarcadeDrive(joy1.getDirectionRadians(), joy1.getMagnitude(), joy1.getRawAxis(XBOX_RSTICK_XAXIS), true);
    	else
    		slideTrain.arcadeDrive((-1 *joy1.getRawAxis(XBOX_LSTICK_YAXIS)), joy1.getRawAxis(XBOX_RSTICK_XAXIS), joy1.getRawAxis(XBOX_LSTICK_XAXIS), slideTune, true);
    	if(!slideTrain.getPIDController().isEnable())
    		gyro.reset_gyro_angle();
    		
    	if(joy2.getRawButton(XBOX_A_BUTTON) && !lastA_ButtonValue && !elevator.isRetracted && elevator.isAbove)
    	{
    		solenoidIn.set(true);
    		solenoidOut.set(false);
    		elevator.setIsRetracted(true);
    	}
    	else if(joy2.getRawButton(XBOX_A_BUTTON) && elevator.getIsAbove() && elevator.isRetracted && !lastA_ButtonValue)
    	{
    		solenoidIn.set(false);
    		solenoidOut.set(true);
    		elevator.setIsRetracted(false);
    		
    	}
    	if(joy2.getRawButton(XBOX_X_BUTTON) && !lastX_ButtonValue)
    	{
    		solenoidOpenClose.set(!current_SolOpenClose_value);
    		current_SolOpenClose_value = !current_SolOpenClose_value;
    	}
//    	else if(joy1.getRawButton(XBOX_X_BUTTON) && !lastX_ButtonValue && solenoidOpenClose.get())
//    	{
//    		solenoidOpenClose.set(false);
//    	}
    	
    	if(elevator.bottomSensor.get() && !elevator.lastState && elevator.elevatorOutput < 0)
    	{
    		elevator.getPIDController().reset();
    		elevator.raw_encoder_bottom_offset = -elevator.get_raw_encoder();
    		elevator.setSetpoint(0);
    		//elevator.encoder.reset();
    		goDown = false;
    		currentLevel = 0;
    		Elev_PID = true;
    		elevator.getPIDController().enable();
    	}
    	if(joy2.getRawButton(XBOX_RIGHT_BUTTON) && !lastR_ButtonValue && currentLevel < (levels.length - 1) && Elev_PID)
    	{
    		elevator.setSetpoint(levels[currentLevel + 1]);
			currentLevel = currentLevel + 1;
			goDown = false;
    	}
    	else if(joy2.getRawButton(XBOX_LEFT_BUTTON) && !lastL_ButtonValue && Elev_PID && 
    			((currentLevel > 0 && !elevator.isRetracted) || (currentLevel > MIN_RETRACTED_LEVEL)))
    	{
    		elevator.setSetpoint(levels[currentLevel - 1]);
			currentLevel = currentLevel - 1;
			goDown = false;
    	}
    	else if(joy2.getRawAxis(XBOX_LTRIGGER_AXIS) > 0.75 && Elev_PID && ((elevator.getElevatorHeightIN() > 0 && !elevator.isRetracted) || (elevator.getElevatorHeightIN() > MIN_RETRACTED_HEIGHT)))
    	{
    		elevator.setSetpoint(elevator.getElevatorHeightIN() - 0.55);
    		if(currentLevel > 0)
    			if(elevator.getElevatorHeightIN() <= levels[currentLevel - 1])
    				currentLevel--;
    	}
    	else if(joy2.getRawAxis(XBOX_RTRIGGER_AXIS) > 0.75 && Elev_PID && elevator.getElevatorHeightIN() < elevator.MAX_HEIGHT)
    	{
    		elevator.setSetpoint(elevator.getElevatorHeightIN() + 0.7);
    		if(currentLevel < levels.length-1)
    			if(elevator.getElevatorHeightIN() >= levels[currentLevel + 1])
    				currentLevel++;
    	}
    	
    	if(joy2.getRawButton(XBOX_SELECT_BUTTON) && joy2.getRawButton(XBOX_START_BUTTON))
    	{
    		elevator.disable();
    		Elev_PID = false;
    		goDown = false;
    	}
    	else if(joy2.getRawButton(XBOX_RIGHT_BUTTON) && !Elev_PID)
    	{
    		elevator.setMotorSpeed(0.75);
    		SmartDashboard.putNumber("Elevator Distance", elevator.encoder.getDistance());
    		goDown = false;
    	}
    	else if(joy2.getRawButton(XBOX_LEFT_BUTTON) && !Elev_PID)
    	{
    		elevator.setMotorSpeed(-0.75);
    		SmartDashboard.putNumber("Elevator Distance", elevator.getElevatorHeightIN());
    		SmartDashboard.putNumber("Elevator Encoder Raw", elevator.encoder.getRaw());
    		goDown = false;
    	}
    	else if(!Elev_PID && !goDown)
    	{
    		elevator.setMotorSpeed(0);
    		SmartDashboard.putNumber("Elevator Distance", elevator.getElevatorHeightIN());
    		SmartDashboard.putNumber("Elevator Encoder Raw", elevator.encoder.getRaw());	
    	}
    		
        
    	//Set motor values. Note right motor values are inverted due to the physical
    	//configuration of the robot drivetrain
		frontRightMotor.set(-slideTrain.frontRightMotorValue);
		frontLeftMotor.set(slideTrain.frontLeftMotorValue);
		backRightMotor.set(-slideTrain.backRightMotorValue);
		backLeftMotor.set(slideTrain.backLeftMotorValue);
		slideMotor.set(slideTrain.slideMotorValue);
		
		lastA_ButtonValue = joy2.getRawButton(XBOX_A_BUTTON);
		lastX_ButtonValue = joy2.getRawButton(XBOX_X_BUTTON);
		
		lastR_ButtonValue = joy2.getRawButton(XBOX_RIGHT_BUTTON);
		lastL_ButtonValue = joy2.getRawButton(XBOX_LEFT_BUTTON);
		
		//Spit some debug info out to the Riolog
//		System.out.print("Front Right Motor: " + String.format( "%.2f", slideTrain.frontRightMotorValue) + " ");
//		System.out.print("Front Left Motor: " + String.format( "%.2f", slideTrain.frontLeftMotorValue) + " ");
//		System.out.print("Back Right Motor: " + String.format( "%.2f", slideTrain.backRightMotorValue) + " ");
//		System.out.print("Back Left Motor: " + String.format( "%.2f", slideTrain.backLeftMotorValue) + " ");
//		System.out.print("Slide Motor: " + String.format( "%.2f", slideTrain.slideMotorValue) + " ");
//		System.out.println("Gyro: " + String.format( "%.2f", gyroValue) + " PID Value:" + String.format( "%.2f", slideTrain.PIDOutput));
		SmartDashboard.putNumber("Pressure", getPressurePSI(pressureSensor.getAverageVoltage()));
    }
    
    public void disabledPeriodic(){
    	if(log_open){
    		log_open = false;
    		try {
				log.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}

    	
    }
    
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
    
    }
    
    public double getPressurePSI(double voltage) {
		return (voltage - 0.5) * (75 / 2);
	}
    
	public int getDirectionToStrafe()
	{
		double elevatorDifference = elevator.getLeftDistance() - elevator.getRightDistance();
		if(elevatorDifference < 1)
			return 0;
		else
		{
			if(elevatorDifference > 0)
			{
				return -1;
			}
			else
			{
				return 1;
			}
		}
	}
}
