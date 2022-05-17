package catpoint.service;

import catpoint.data.AlarmStatus;
import catpoint.data.ArmingStatus;
import catpoint.data.Sensor;

import java.util.Set;

public interface SecurityServiceInterface {
    void pendingAlarmStatus();
    void changeAlarmStatus(Sensor sensor, Boolean active);
}
