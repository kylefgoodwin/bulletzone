package edu.unh.cs.cs619.bulletzone.controllers;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import edu.unh.cs.cs619.bulletzone.MenuActivity;
import edu.unh.cs.cs619.bulletzone.MenuController;

/**
 * Made by Alec Rydeen
 * Tests for correct rest-calling functions to be called based on user input in ClientActivity
 * Tests for the Login Button correctly calling join function
 * Will likely implement more test methods in the future, once more functionality is introduced.
 */

@RunWith(MockitoJUnitRunner.class)
public class MenuControllerTest {

    private MenuActivity menuActivity;
    @Mock
    private MenuController menuControllerMock;

    @Before
    public void setUp() {
        menuActivity = new MenuActivity();
        menuActivity.setMenuController(menuControllerMock);
    }

    @Test
    public void testButtonLogin() {
        menuActivity.joinTest();
        verify(menuControllerMock).joinAsync();
    }
}