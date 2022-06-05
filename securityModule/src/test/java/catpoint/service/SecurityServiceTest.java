package catpoint.service;

import catpoint.data.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import service.ImageServiceInterface;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import software.amazon.awssdk.services.rekognition.model.CreateStreamProcessorRequest;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    @Mock
    private SecurityRepository repository;
    private SecurityService securityService;

    private boolean active = true;
    private Sensor sensor = new Sensor();
    private AlarmStatus pendingAlarmStatus = AlarmStatus.PENDING_ALARM;

    private ImageServiceInterface imageServiceInterface;

    @BeforeEach
    void init()
    {
            securityService = new SecurityService(repository, imageServiceInterface);
    }


    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME","ARMED_AWAY"})
    void pendingStatus_alarmIsArmed_SensorIsActivated_SystemReturnsPendingStatus(ArmingStatus armingStatus) //TEST 1
    {
        sensor.setActive(active);
       // when(repository.pendingAlarmStatus(sensor, armingStatus)).thenReturn(AlarmStatus.PENDING_ALARM);
        Assertions.assertEquals(AlarmStatus.PENDING_ALARM, securityService.changeToPending(sensor, armingStatus));
        verify(repository,atLeastOnce()).pendingAlarmStatus(sensor,armingStatus);

    }

    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    void setStatusToAlarm_AlarmIsArmed_SensorIsActivated_SystemAlreadyPending_ReturnsAlamStatus(ArmingStatus armingStatus) //TEST 2
    {
        sensor.setActive(active);
       // when(repository.alarmStatus(armingStatus,sensor,pendingAlarmStatus)).thenReturn(AlarmStatus.ALARM);
        Assertions.assertEquals(AlarmStatus.ALARM, securityService.changeToAlarm(armingStatus,sensor,pendingAlarmStatus));
        verify(repository, atLeastOnce()).alarmStatus(armingStatus,sensor,pendingAlarmStatus);

    }
    @Test
    void setStatusToNoAlarm_AlarmInPendingMode_NoSensorsAreActive_ReturnNoAlarmStatus() //TEST 3
    {

        Sensor sensor1 = new Sensor("Front Door",SensorType.DOOR);
        Sensor sensor2 = new Sensor("Back Door", SensorType.DOOR);
        boolean notActive = false;
        sensor1.setActive(notActive);
        sensor2.setActive(notActive);
       Set<Sensor> theSensors = new HashSet<>();
       theSensors.add(sensor1);
       theSensors.add(sensor2);
     //  when(repository.noAlarmStatus(AlarmStatus.PENDING_ALARM,theSensors)).thenReturn(AlarmStatus.NO_ALARM);
       Assertions.assertEquals(AlarmStatus.NO_ALARM,securityService.noAlarmSet(AlarmStatus.PENDING_ALARM,theSensors));
      verify(repository, atLeastOnce()).noAlarmStatus(AlarmStatus.PENDING_ALARM,theSensors);
    }
//    public static Stream<Arguments> createSensors()
//    {
//        return Stream.of(
//                Arguments.of(new Sensor("Front Door", SensorType.DOOR)),
//                Arguments.of(new Sensor("Back Door", SensorType.DOOR))
//        );
//
//    }

   @ParameterizedTest
   @ValueSource(booleans = {true, false}) //TEST 4
    void alarmActive_ChangeInSensorMakesNoChanges_ReturnNoChangesToAlarmStatus(boolean status)
    {

            when(repository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
            securityService.changeSensorActivationStatus(sensor, status);
            verify(repository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

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
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void catDetected_SystemInAtHomeStatus_ReturnsALARMIfCatIsFound(boolean isCatDetected) //TEST 7
    {
        repository.setArmingStatus(ArmingStatus.ARMED_HOME);
        when(repository.catDetectedAlarmStatus(isCatDetected)).thenReturn(AlarmStatus.ALARM);
        securityService.catDetected(isCatDetected);
        verify(repository).catDetectedAlarmStatus(isCatDetected);
    }
    @Test
    void noCatNoAlarm_NoSensorsActivated_ReturnNoAlarm() //Test 8
    {
        Sensor sensor1 = new Sensor("Back door", SensorType.DOOR);
        Sensor sensor2 = new Sensor("Front window", SensorType.WINDOW);
        boolean isActive = false;
        boolean catDetected = false;
        sensor1.setActive(isActive);
        sensor2.setActive(isActive);
        Set<Sensor> theSensors = new HashSet<>();
        theSensors.add(sensor1);
        theSensors.add(sensor2);
        Assertions.assertEquals(AlarmStatus.NO_ALARM, securityService.noCatNoAlarmSet(catDetected,theSensors));

    }
    @Test
    void noAlarm_systemDisarmed_ReturnNoAlarm() // TEST 9
    {
        ArmingStatus armingStatus = ArmingStatus.DISARMED;
        Assertions.assertEquals(AlarmStatus.NO_ALARM,securityService.noAlarm(armingStatus));  //invokes the call to noAlarm
        verify(repository, atLeastOnce()).noAlarm(armingStatus);
    }
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    void sensorReset_systemIsArmed_ReturnInactiveSensors(ArmingStatus armingStatus) //TEST 10
    {
            Sensor sensor1 = new Sensor("Back door", SensorType.DOOR);
            Sensor sensor2 = new Sensor("Front sensor", SensorType.MOTION);
            sensor1.setActive(true);
            sensor2.setActive(true);
            Set<Sensor> theSensors = new HashSet<>();
            theSensors.add(sensor1);
            theSensors.add(sensor2);
            Assertions.assertEquals(theSensors,securityService.resetTheSensors(armingStatus, theSensors));
            for(Sensor aSensor: theSensors)
            {
                verify(repository,atLeastOnce()).updateSensor(aSensor);
            }
    }
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void catDetectedAgain_SystemInAtHomeStatus_ReturnsALARMIfCatIsFound(boolean isCatDetected) //TEST 11
    {
        repository.setArmingStatus(ArmingStatus.ARMED_HOME);
        when(repository.catDetectedAlarmStatus(isCatDetected)).thenReturn(AlarmStatus.ALARM);
        securityService.catDetected(isCatDetected);
        verify(repository).catDetectedAlarmStatus(isCatDetected);
    }





}
