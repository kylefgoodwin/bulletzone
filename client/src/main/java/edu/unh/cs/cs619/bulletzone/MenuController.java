package edu.unh.cs.cs619.bulletzone;

import org.androidannotations.annotations.EBean;
import org.androidannotations.rest.spring.annotations.RestService;

import edu.unh.cs.cs619.bulletzone.rest.BulletZoneRestClient;

/**
 * Made by Alec Rydeen
 * Simple Class to take some of the Rest Client Calls out of MenuActivity
 */

@EBean
public class MenuController {

    private static final String TAG = "MenuController";

    @RestService
    BulletZoneRestClient restClient;

    public MenuController() {}

    public long joinAsync() {
        return restClient.join().getResult();
    }
}
