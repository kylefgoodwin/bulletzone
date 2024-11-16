package edu.unh.cs.cs619.bulletzone.controllers;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.view.View;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import edu.unh.cs.cs619.bulletzone.ClientActivity;
import edu.unh.cs.cs619.bulletzone.R;
import edu.unh.cs.cs619.bulletzone.TankEventController;

@RunWith(MockitoJUnitRunner.class)
public class TankEventControllerTest {

    private ClientActivity clientActivity;
    @Mock
    private TankEventController tankEventControllerMock;
    @Mock
    private View buttonUpMock;
    @Mock
    private View buttonRightMock;
    @Mock
    private View buttonDownMock;
    @Mock
    private View buttonLeftMock;

    @Before
    public void setUp() {
        clientActivity = new ClientActivity();
        clientActivity.setTankEventController(tankEventControllerMock);

        when(buttonUpMock.getId()).thenReturn(R.id.buttonUp);
        when(buttonRightMock.getId()).thenReturn(R.id.buttonRight);
        when(buttonDownMock.getId()).thenReturn(R.id.buttonDown);
        when(buttonLeftMock.getId()).thenReturn(R.id.buttonLeft);
    }

    @Test
    public void testMoveAsyncUp() {
        clientActivity.moveTest(buttonUpMock);
        verify(tankEventControllerMock).turnOrMove(eq(R.id.buttonUp), anyLong(), anyInt(), eq((byte) 0));
    }

    @Test
    public void testMoveAsyncRight() {
        clientActivity.moveTest(buttonRightMock);
        verify(tankEventControllerMock).turnOrMove(eq(R.id.buttonRight), anyLong(), anyInt(), eq((byte) 2));
    }

    @Test
    public void testMoveAsyncDown() {
        clientActivity.moveTest(buttonDownMock);
        verify(tankEventControllerMock).turnOrMove(eq(R.id.buttonDown), anyLong(), anyInt(), eq((byte) 4));
    }

    @Test
    public void testMoveAsyncLeft() {
        clientActivity.moveTest(buttonLeftMock);
        verify(tankEventControllerMock).turnOrMove(eq(R.id.buttonLeft), anyLong(), anyInt(), eq((byte) 6));
    }

    @Test
    public void testFireAsync() {
        clientActivity.fireTest();
        verify(tankEventControllerMock).fire(anyLong(), anyInt());
    }

    @Test
    public void testTurnAsync() {
        clientActivity.moveTest(buttonUpMock);
        verify(tankEventControllerMock).turnOrMove(eq(R.id.buttonUp), anyLong(), anyInt(), eq((byte) 0));

        clientActivity.moveTest(buttonRightMock);
        verify(tankEventControllerMock).turnOrMove(eq(R.id.buttonRight), anyLong(), anyInt(), eq((byte) 2));
    }
}