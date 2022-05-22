package catpoint.service;

import catpoint.data.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import service.ImageServiceInterface;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    @Mock
    private SecurityRepository repository;
    private SecurityService securityService =null;
    @Mock
    private Sensor sensorMock;
    private boolean active = true;
    private Sensor sensor = new Sensor();
    private ArmingStatus armingStatusHome = ArmingStatus.ARMED_HOME;
    private ArmingStatus armingStatusAway = ArmingStatus.ARMED_AWAY;
    private AlarmStatus pendingAlarmStatus = AlarmStatus.PENDING_ALARM;

    private ImageServiceInterface imageServiceInterface;

    @BeforeEach
    void init()
    {
            securityService = new SecurityService(repository);
            sensorMock.setName("Back Door");
            sensorMock.setSensorType(SensorType.DOOR);
    }


    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME","ARMED_AWAY"})
    void pendingStatus_alarmIsArmed_SensorIsActivated_SystemReturnsPendingStatus(ArmingStatus armingStatus) //TEST 1
    {
        sensor.setActive(active);
        when(repository.pendingAlarmStatus(sensor, armingStatus)).thenReturn(AlarmStatus.PENDING_ALARM);
        Assertions.assertEquals(AlarmStatus.PENDING_ALARM, securityService.changeToPending(sensor, armingStatus));
        verify(repository).pendingAlarmStatus(sensor,armingStatus);
        System.out.println("Pending status was returned");
    }
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    void setStatusToAlarm_AlarmIsArmed_SensorIsActivated_SystemAlreadyPending_ReturnsAlamStatus(ArmingStatus armingStatus) //TEST 2
    {
        sensor.setActive(active);
        when(repository.alarmStatus(armingStatus,sensor,pendingAlarmStatus)).thenReturn(AlarmStatus.ALARM);
        Assertions.assertEquals(AlarmStatus.ALARM, securityService.changeToAlarm(armingStatus,sensor,pendingAlarmStatus));
        verify(repository).alarmStatus(armingStatus,sensor,pendingAlarmStatus);
        System.out.println("Alarm status was returned");
    }
    @Test
    void setStatusToNoAlarm_AlarmInPendingMode_NoSensorsAreActive_ReturnNoAlarmStatus() //TEST 3
    {

        Sensor sensor1 = new Sensor("Front Door",SensorType.DOOR);
        Sensor sensor2 = new Sensor("Back Door", SensorType.DOOR);
        boolean notActive = false;
        sensor1.setActive(notActive);
        sensor2.setActive(notActive);
        repository.addSensor(sensor1);
        repository.addSensor(sensor2);
       when(repository.noAlarmStatus(AlarmStatus.PENDING_ALARM,repository.getSensors())).thenReturn(AlarmStatus.NO_ALARM);
       Assertions.assertEquals(AlarmStatus.NO_ALARM,securityService.noAlarmSet(AlarmStatus.PENDING_ALARM,repository.getSensors()));
       verify(repository,times(2)).noAlarmStatus(AlarmStatus.PENDING_ALARM,repository.getSensors());
       System.out.println("Alarm set to no alarm");
    }
//    public static Stream<Arguments> createSensors()
//    {
//        return Stream.of(
//                Arguments.of(new Sensor("Front Door", SensorType.DOOR)),
//                Arguments.of(new Sensor("Back Door", SensorType.DOOR))
//        );
//
//    }
    @Test
    void sensorAlreadyActivated_SensorSetToActiveAndSystemPending_ReturnAlarmState() //TEST 5
    {
       Sensor sensor = new Sensor("Back Window", SensorType.WINDOW);
       sensor.setActive(true);
        boolean wishToActivate = true;
        when(repository.sensorAlreadyActivated(sensor,wishToActivate,pendingAlarmStatus)).thenReturn(AlarmStatus.ALARM);
        Assertions.assertEquals(AlarmStatus.ALARM,securityService.sensorAlreadyActivated(sensor,wishToActivate,pendingAlarmStatus));
        verify(repository).sensorAlreadyActivated(sensor, wishToActivate,pendingAlarmStatus);
    }
    @Test
    void noChangesToAlarm_SensorIsNotActiveAlready_ReturnNoChange() //TEST 6
    {
        Sensor sensor = new Sensor("Back Window", SensorType.WINDOW);
        sensor.setActive(false);
        boolean wishToActivate = false;
        when(repository.sensorAlreadyActivated(sensor,wishToActivate,pendingAlarmStatus)).thenReturn(pendingAlarmStatus);
        Assertions.assertEquals(pendingAlarmStatus,securityService.sensorAlreadyActivated(sensor,wishToActivate,pendingAlarmStatus));
        verify(repository).sensorAlreadyActivated(sensor, wishToActivate,pendingAlarmStatus);
    }
   // @Test

}
