package catpoint.service;

import catpoint.application.StatusListener;
import catpoint.data.AlarmStatus;
import catpoint.data.ArmingStatus;
import catpoint.data.SecurityRepository;
import catpoint.data.Sensor;
import service.ImageServiceInterface;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

/**
 * Service that receives information about changes to the security system. Responsible for
 * forwarding updates to the repository and making any decisions about changing the system state.
 *
 * This is the class that should contain most of the business logic for our system, and it is the
 * class you will be writing unit tests for.
 */
public class SecurityService {

    private ImageServiceInterface imageService;
    private SecurityRepository securityRepository;
    private Set<StatusListener> statusListeners = new HashSet<>();
    private SecurityServiceInterface securityServiceInterface;
    public SecurityService(SecurityRepository securityRepository, ImageServiceInterface imageService) {
        this.securityRepository = securityRepository;
        this.imageService = imageService;
    }

    public SecurityService(){}
    public SecurityService(SecurityRepository securityRepository) {
        this.securityRepository = securityRepository;
    }

    /**
     * Sets the current arming status for the system. Changing the arming status
     * may update both the alarm status.
     * @param armingStatus
     */

    public void setArmingStatus(ArmingStatus armingStatus) {
        if(armingStatus == ArmingStatus.DISARMED) {
            setAlarmStatus(AlarmStatus.NO_ALARM);
        }
        securityRepository.setArmingStatus(armingStatus);
    }


    /**
     * Internal method that handles alarm status changes based on whether
     * the camera currently shows a cat.
     * @param cat True if a cat is detected, otherwise false.
     */
    private void catDetected(Boolean cat) {
        if(cat && getArmingStatus() == ArmingStatus.ARMED_HOME) {
            setAlarmStatus(AlarmStatus.ALARM);
        } else {
            setAlarmStatus(AlarmStatus.NO_ALARM);
        }

        statusListeners.forEach(sl -> sl.catDetected(cat));
    }

    /**
     * Register the StatusListener for alarm system updates from within the SecurityService.
     * @param statusListener
     */
    public void addStatusListener(StatusListener statusListener) {
        statusListeners.add(statusListener);
    }

    public void removeStatusListener(StatusListener statusListener) {
        statusListeners.remove(statusListener);
    }

    /**
     * Change the alarm status of the system and notify all listeners.
     * @param status
     */
    public void setAlarmStatus(AlarmStatus status) {
        securityRepository.setAlarmStatus(status);
        statusListeners.forEach(sl -> sl.notify(status));
    }

    /**
     * Internal method for updating the alarm status when a sensor has been activated.
     */
    private void handleSensorActivated(Sensor sensor, ArmingStatus armingStatus) {
        if(securityRepository.getArmingStatus() == ArmingStatus.DISARMED) {
            return; //no problem if the system is disarmed
        }
        switch(securityRepository.getAlarmStatus()) {
            case NO_ALARM -> setAlarmStatus(AlarmStatus.PENDING_ALARM);
            case PENDING_ALARM -> setAlarmStatus(AlarmStatus.ALARM);
        }
    }

    /**
     * Internal method for updating the alarm status when a sensor has been deactivated
     */
    private void handleSensorDeactivated() {
        switch(securityRepository.getAlarmStatus()) {
            case PENDING_ALARM -> setAlarmStatus(AlarmStatus.NO_ALARM);
            case ALARM -> setAlarmStatus(AlarmStatus.PENDING_ALARM);
        }
    }



    public AlarmStatus changeToPending(Sensor sensorStatus, ArmingStatus armingStatus) //Works with test 1
    {
        if(sensorStatus.getActive() && armingStatus.equals(ArmingStatus.ARMED_HOME) ) {
           securityRepository.pendingAlarmStatus(sensorStatus, armingStatus);
            return AlarmStatus.PENDING_ALARM;
        } else if (sensorStatus.getActive() && armingStatus.equals(ArmingStatus.ARMED_AWAY)) {
            securityRepository.pendingAlarmStatus(sensorStatus, armingStatus);
            return AlarmStatus.PENDING_ALARM;
        }
        return AlarmStatus.NO_ALARM;
    }
    public AlarmStatus changeToAlarm(ArmingStatus armingStatus, Sensor sensor, AlarmStatus alarmStatus) //Works with test 2
    {

        switch (armingStatus)
        {
            case ARMED_AWAY, ARMED_HOME -> {
                if(sensor.getActive() && alarmStatus.equals(AlarmStatus.PENDING_ALARM))
                {
                    securityRepository.alarmStatus(armingStatus,sensor,alarmStatus);
                    return AlarmStatus.ALARM;
                }break;
            }

        }

        return AlarmStatus.NO_ALARM;
    }
    public AlarmStatus noAlarmSet(AlarmStatus alarmStatus, Set<Sensor> sensors) //Works with test 3
    {
        for(Sensor sensor: sensors)
        {
            if(sensor.getActive()) //if a sensor is active
            {
                return  AlarmStatus.PENDING_ALARM;
            }
        }
        if(alarmStatus.equals(AlarmStatus.PENDING_ALARM))
        {
            securityRepository.noAlarmStatus(alarmStatus,sensors);
            return AlarmStatus.NO_ALARM;
        }
        securityRepository.noAlarmStatus(alarmStatus,sensors);
        return AlarmStatus.PENDING_ALARM;
    }


    public AlarmStatus returnSameAlarm(AlarmStatus alarmStatus, Sensor sensor, boolean activationStatus) //Works with test 4
    {
        switch (alarmStatus)
        {
            case ALARM, PENDING_ALARM -> {
                securityRepository.noChangeToAlarm(alarmStatus, sensor,activationStatus);
                return alarmStatus;}
            default -> {
                securityRepository.noChangeToAlarm(alarmStatus, sensor,activationStatus);
                return AlarmStatus.NO_ALARM;
            }
        }
}

    /**
     * Change the activation status for the specified sensor and update alarm status if necessary.
     * @param sensor
     * @param active
     */
    public void changeSensorActivationStatus(Sensor sensor, Boolean active) { //Works with test 4 GUI PORTION
        if (getAlarmStatus() == AlarmStatus.PENDING_ALARM && !sensor.getActive()) {
            handleSensorDeactivated();
        } else if (getAlarmStatus() == AlarmStatus.ALARM && getArmingStatus() == ArmingStatus.DISARMED) {

            handleSensorDeactivated();
        }

        sensor.setActive(active);
        securityRepository.updateSensor(sensor);
    }
   public AlarmStatus sensorAlreadyActivated(Sensor sensor, boolean wishToActivate, AlarmStatus alarmStatus) //Works with test 5
   {
       boolean alreadyActive = sensor.getActive();
       if(alreadyActive && wishToActivate && alarmStatus.equals(AlarmStatus.PENDING_ALARM))
       {
           securityRepository.sensorAlreadyActivated(sensor,wishToActivate,alarmStatus);
            return AlarmStatus.ALARM;
       }else if(!alreadyActive && !wishToActivate)
       {
           securityRepository.sensorAlreadyActivated(sensor,wishToActivate,alarmStatus);
           return alarmStatus;
       }

       return AlarmStatus.NO_ALARM;
   }
//   public AlarmStatus alarmStatusCatFound(ImageServiceInterface imageService, ArmingStatus armingStatus)
//   {
//       if(imageService.imageContainsCat( ,1.0))
//   }



    /**
     * Send an image to the SecurityService for processing. The securityService will use its provided
     * ImageService to analyze the image for cats and update the alarm status accordingly.
     * @param currentCameraImage
     */
    public void processImage(BufferedImage currentCameraImage) {
        catDetected(imageService.imageContainsCat(currentCameraImage, 50.0f));
    }

    public AlarmStatus getAlarmStatus() {
        return securityRepository.getAlarmStatus();
    }

    public Set<Sensor> getSensors() {
        return securityRepository.getSensors();
    }

    public void addSensor(Sensor sensor) {
        securityRepository.addSensor(sensor);
    }

    public void removeSensor(Sensor sensor) {
        securityRepository.removeSensor(sensor);
    }

    public ArmingStatus getArmingStatus() {
        return securityRepository.getArmingStatus();
    }
}
