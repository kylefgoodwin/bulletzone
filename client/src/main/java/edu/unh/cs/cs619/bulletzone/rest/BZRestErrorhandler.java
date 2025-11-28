package edu.unh.cs.cs619.bulletzone.rest;

import org.androidannotations.annotations.EBean;
import org.androidannotations.rest.spring.api.RestErrorHandler;
import org.springframework.core.NestedRuntimeException;

@EBean
public class BZRestErrorhandler implements RestErrorHandler {

    @Override
    public void onRestClientExceptionThrown(NestedRuntimeException e) {
        e.printStackTrace();
    }
}
