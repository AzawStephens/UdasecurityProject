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
    private SecurityService securityService;
    private Sensor sensor;
    private Boolean active = true;
    private ImageServiceInterface imageServiceInterface;

    @BeforeEach
    void init()
    {
            securityService = new SecurityService(repository);
    }

    @Test
    void pendingStatus_alarmIsArmed_SensorIsActivated_SystemReturnsPendingStatus()
    {
        when(securityService.justToSee()).thenReturn(AlarmStatus.PENDING_ALARM);
        Assertions.assertEquals(AlarmStatus.PENDING_ALARM, securityService.justToSee());

    }

}
