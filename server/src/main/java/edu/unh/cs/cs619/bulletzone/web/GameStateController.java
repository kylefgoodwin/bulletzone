package edu.unh.cs.cs619.bulletzone.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

import edu.unh.cs.cs619.bulletzone.model.Game;
import edu.unh.cs.cs619.bulletzone.model.events.EventHistory;
import edu.unh.cs.cs619.bulletzone.model.events.GameEvent;
import edu.unh.cs.cs619.bulletzone.repository.GameRepository;
import edu.unh.cs.cs619.bulletzone.util.GameEventCollectionWrapper;
import edu.unh.cs.cs619.bulletzone.util.GridWrapper;

@RestController
@RequestMapping(value = "/games")
class GameStateController {
    private static final Logger log = LoggerFactory.getLogger(edu.unh.cs.cs619.bulletzone.web.GameStateController.class);

    private final Game game;
    private final EventHistory eventHistory = EventHistory.getInstance();

    @Autowired
    public GameStateController(GameRepository gameRepository) {
        this.game = gameRepository.getGame();
    }

    @RequestMapping(method = RequestMethod.GET, value = "/playergrid", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public
    @ResponseBody
    ResponseEntity<GridWrapper> playerGrid() {
        return new ResponseEntity<GridWrapper>(new GridWrapper(game.getGrid2D()), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/itemgrid", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public
    @ResponseBody
    ResponseEntity<GridWrapper> itemGrid() {
        return new ResponseEntity<GridWrapper>(new GridWrapper(game.getItemGrid2D()), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/terraingrid", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public
    @ResponseBody
    ResponseEntity<GridWrapper> terrainGrid() {
        return new ResponseEntity<GridWrapper>(new GridWrapper(game.getTerrainGrid2D()), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/events/{timeSince}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public ResponseEntity<GameEventCollectionWrapper> getHistory(@PathVariable long timeSince) {
        Collection<GameEvent> events = eventHistory.getHistory(timeSince);
        for (GameEvent event : events) {
            System.out.println("Sending " + event.toString());
        }
        return new ResponseEntity<>(new GameEventCollectionWrapper(events), HttpStatus.ACCEPTED);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String handleBadRequests(Exception e) {
        return e.getMessage();
    }
}