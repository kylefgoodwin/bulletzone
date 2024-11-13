package edu.unh.cs.cs619.bulletzone.rest;

import org.androidannotations.rest.spring.annotations.Delete;
import org.androidannotations.rest.spring.annotations.Get;
import org.androidannotations.rest.spring.annotations.Path;
import org.androidannotations.rest.spring.annotations.Post;
import org.androidannotations.rest.spring.annotations.Put;
import org.androidannotations.rest.spring.annotations.Rest;
import org.androidannotations.rest.spring.annotations.RestService.*;
import org.androidannotations.rest.spring.api.RestClientErrorHandling;
import org.androidannotations.rest.spring.api.RestClientHeaders.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClientException;

import edu.unh.cs.cs619.bulletzone.util.BooleanWrapper;
import edu.unh.cs.cs619.bulletzone.util.GameEventCollectionWrapper;
import edu.unh.cs.cs619.bulletzone.util.GridWrapper;
import edu.unh.cs.cs619.bulletzone.util.IntWrapper;
import edu.unh.cs.cs619.bulletzone.util.LongWrapper;
import edu.unh.cs.cs619.bulletzone.util.ResultWrapper;

/** "http://stman1.cs.unh.edu:6191/games"
 * "http://10.0.0.145:6191/games"
 * http://10.0.2.2:8080/
 * Created by simon on 10/1/14.
 */

@Rest(rootUrl = "http://10.0.2.2:61922/games",
//@Rest(rootUrl = "http://stman1.cs.unh.edu:6192/games",
//@Rest(rootUrl = "http://stman1.cs.unh.edu:61912/games",
        converters = {StringHttpMessageConverter.class, MappingJackson2HttpMessageConverter.class}
        // TODO: disable intercepting and logging
        // , interceptors = { HttpLoggerInterceptor.class }
)
public interface BulletZoneRestClient extends RestClientErrorHandling {
    void setRootUrl(String rootUrl);

    @Post("")
    LongWrapper join() throws RestClientException;

    @Get("/playergrid")
    GridWrapper playerGrid();

    @Get("/itemgrid")
    GridWrapper itemGrid();

    @Get("/terraingrid")
    GridWrapper terrainGrid();

    @Get("/events/{sinceTime}")
    GameEventCollectionWrapper events(@Path("sinceTime") long sinceTime);

    @Put("/account/register/{username}/{password}")
    BooleanWrapper register(@Path String username, @Path String password);

    @Put("/account/login/{username}/{password}")
    LongWrapper login(@Path String username, @Path String password);

    @Put("/{playableId}/{playableType}/move/{direction}")
    BooleanWrapper move(@Path long playableId, @Path int playableType, @Path byte direction);

    @Put("/{playableId}/{playableType}/turn/{direction}")
    BooleanWrapper turn(@Path long playableId, @Path int playableType, @Path byte direction);

    @Put("/{playableId}/{playableType}/fire/1")
    BooleanWrapper fire(@Path long playableId, @Path int playableType);

    @Delete("/{playableId}/leave")
    BooleanWrapper leave(@Path long playableId);

    @Get("/account/balance/{userId}")
    Double getBalance(@Path("userId") long userId) throws RestClientException;

//    @Put("/account/balance/{userId}/deduct/{amount}")
//    BooleanWrapper deductBalance(@Path("userId") long userId, @Path("amount") double amount);

    @Put("/account/balance/{userId}/deposit/{amount}")
    BooleanWrapper depositBalance(@Path("userId") long userId, @Path("amount") double amount);

    @Put("/{playableId}/eject")
    BooleanWrapper ejectPowerUp(@Path long playableId);

    @Get("/{playableId}/{playableType}/life")
    IntWrapper getLife(@Path int playableId, @Path int playableType);
}
