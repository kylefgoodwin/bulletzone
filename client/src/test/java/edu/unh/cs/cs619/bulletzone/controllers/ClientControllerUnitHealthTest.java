package edu.unh.cs.cs619.bulletzone.controllers;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import android.widget.Toast;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.manipulation.Ordering;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import edu.unh.cs.cs619.bulletzone.ClientActivity;
import edu.unh.cs.cs619.bulletzone.ClientController;
import edu.unh.cs.cs619.bulletzone.PlayerData;
import edu.unh.cs.cs619.bulletzone.rest.BulletZoneRestClient;
import edu.unh.cs.cs619.bulletzone.util.IntWrapper;

@RunWith(MockitoJUnitRunner.class)
public class ClientControllerUnitHealthTest {

    @Mock
    private BulletZoneRestClient restClient;

    @Mock
    private ClientActivity clientActivity;


    private ClientController clientController;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        // Mock activity behavior if needed
        clientController = new ClientController();
        clientController.setRestClient(restClient);
    }

    @Test
    public void testPostLifeAsync_withValidTankId() throws Exception {
        // Mock PlayerData behavior
        mockStatic(PlayerData.class);
        PlayerData playerDataMock = mock(PlayerData.class);
        when(PlayerData.getPlayerData()).thenReturn(playerDataMock);

        when(playerDataMock.getTankId()).thenReturn(0L);
        when(playerDataMock.getTankLife()).thenReturn(100);

        // Call the method
        clientController.postLifeAsync(0, 1, clientActivity);

        // Verify that a toast is shown with the correct health
        verify(clientActivity).runOnUiThread(any(Runnable.class));  // If you are using an activity context
        Toast.makeText(eq(clientActivity), eq("Health: 100"), anyInt());
    }

    @Test
    public void testPostLifeAsync_withInvalidTankId() throws Exception {
        // Mock server response
        IntWrapper result = new IntWrapper();
        result.setResult(50);
        when(restClient.getLife(anyInt(), anyInt())).thenReturn(result);

        Toast mockedToast = mock(Toast.class);
        when(Toast.makeText(clientActivity, anyString(), eq(Toast.LENGTH_SHORT))).thenReturn(mockedToast);

        // Call the method
        clientController.postLifeAsync(1, 1, clientActivity);

        // Verify that the server's life value is displayed
        verify(clientActivity).runOnUiThread(any(Runnable.class));
        Toast.makeText(eq(clientActivity), eq("Health: 50"), anyInt());
    }

    @Test
    public void testPostLifeAsync_withServerError() throws Exception {
        // Mock server error
        when(restClient.getLife(anyInt(), anyInt())).thenThrow(new RuntimeException("Server Error"));

        // Call the method
        clientController.postLifeAsync(1, 1, clientActivity);

        // Verify that the error message toast is displayed
        verify(clientActivity).runOnUiThread(any(Runnable.class));
        Toast.makeText(eq(clientActivity), eq("Failed to fetch life."), anyInt());
    }
}
