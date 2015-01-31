
package org.usfirst.frc.team1736.robot;

import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the IterativeRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the manifest file in the resource
 * directory.
 */
public class Robot extends IterativeRobot {
	
	I2CGyro testgyro;
    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    public void robotInit() {
    	testgyro = new I2CGyro();
    	

    }

    /**
     * This function is called periodically during autonomous
     */
    public void autonomousPeriodic() {

    }

    /**
     * This function is called periodically during operator control
     */
    public void teleopPeriodic() {
        
    }
    
    @Override
    public void testInit(){ 
    }
    /**
     * This function is called periodically during test mode
     */
    public void testPeriodic() {
    	System.out.println("Gyro Val = " + testgyro.get_gyro_z() + " Gyro Angle = " + testgyro.get_gyro_angle());
    	SmartDashboard.putNumber("Gyro Value", testgyro.get_gyro_z());
    	SmartDashboard.putNumber("Gyro Angle", testgyro.get_gyro_angle());
    	SmartDashboard.putNumber("Gyro Value (RAW)", testgyro.get_gyro_z_raw());
    	
    }
    
}
