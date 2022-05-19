package catpoint.service;

import catpoint.data.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import service.ImageServiceInterface;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    @Mock
    private SecurityRepository repository;
    private SecurityService securityService =null;
    private boolean active = true;
    private Sensor sensor = new Sensor();

    private ImageServiceInterface imageServiceInterface;

    @BeforeEach
    void init()
    {
            securityService = new SecurityService(repository);
    }
    @Test
    void pendingStatus_alarmIsArmed_SensorIsActivated_SystemReturnsPendingStatus()
    {
        sensor.setActive(active);
        ArmingStatus armingStatusHome = ArmingStatus.ARMED_HOME;
        ArmingStatus armingStatusAway = ArmingStatus.ARMED_AWAY;
        when(repository.pendingAlarmStatus(sensor, armingStatusHome)).thenReturn(AlarmStatus.PENDING_ALARM);
        lenient().when(repository.pendingAlarmStatus(sensor, armingStatusAway)).thenReturn(AlarmStatus.PENDING_ALARM);
        Assertions.assertEquals(AlarmStatus.PENDING_ALARM, securityService.changeToPending(sensor, armingStatusHome));
        verify(repository).pendingAlarmStatus(sensor,armingStatusHome);
    }
}
