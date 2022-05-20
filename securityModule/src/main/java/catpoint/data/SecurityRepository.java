package catpoint.data;

import java.util.List;
import java.util.Set;

/**
 * Interface showing the methods our security repository will need to support
 */
public interface SecurityRepository {
    void addSensor(Sensor sensor);
    void removeSensor(Sensor sensor);
    void updateSensor(Sensor sensor);
    void setAlarmStatus(AlarmStatus alarmStatus);
    void setArmingStatus(ArmingStatus armingStatus);
    Set<Sensor> getSensors();
    AlarmStatus getAlarmStatus();
    ArmingStatus getArmingStatus();
   // AlarmStatus justToSee(Sensor sensor);
    AlarmStatus pendingAlarmStatus(Sensor sensor, ArmingStatus armingStatus);
    AlarmStatus alarmStatus(ArmingStatus armingStatus, Sensor sensor, AlarmStatus alarmStatus);
    AlarmStatus noAlarmStatus(AlarmStatus alarmStatus, Set<Sensor> sensors);


}
