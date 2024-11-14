package edu.unh.cs.cs619.bulletzone.web;

import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;

import edu.unh.cs.cs619.bulletzone.model.Builder;
import edu.unh.cs.cs619.bulletzone.model.FieldEntity;
import edu.unh.cs.cs619.bulletzone.model.FieldHolder;
import edu.unh.cs.cs619.bulletzone.model.Playable;
import edu.unh.cs.cs619.bulletzone.util.PlayableWrapper;
import edu.unh.cs.cs619.bulletzone.util.IntWrapper;
import jakarta.servlet.http.HttpServletRequest;
import com.google.common.base.Preconditions;

import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.IllegalTransitionException;
import edu.unh.cs.cs619.bulletzone.model.LimitExceededException;
import edu.unh.cs.cs619.bulletzone.model.Tank;
import edu.unh.cs.cs619.bulletzone.model.TankDoesNotExistException;
import edu.unh.cs.cs619.bulletzone.repository.GameRepository;
import edu.unh.cs.cs619.bulletzone.util.BooleanWrapper;
import edu.unh.cs.cs619.bulletzone.util.GridWrapper;
import edu.unh.cs.cs619.bulletzone.util.LongWrapper;

@RestController
@RequestMapping(value = "/games")
class GamesController {

    private static final Logger log = LoggerFactory.getLogger(GamesController.class);

    private final GameRepository gameRepository;

    @Autowired
    public GamesController(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @RequestMapping(method = RequestMethod.POST, value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    ResponseEntity<LongWrapper> join(HttpServletRequest request) {
        Tank tank;
        try {
            tank = gameRepository.join(request.getRemoteAddr());
            log.info("Player joined: tankId={} IP={}", tank.getId(), request.getRemoteAddr());

            return new ResponseEntity<LongWrapper>(
                    new LongWrapper(tank.getId()),
                    HttpStatus.CREATED
            );
        } catch (RestClientException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(method = RequestMethod.PUT, value = "{playableId}/{playableType}/turn/{direction}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<BooleanWrapper> turn(@PathVariable long playableId, @PathVariable int playableType, @PathVariable byte direction)
            throws TankDoesNotExistException, LimitExceededException, IllegalTransitionException {
        return new ResponseEntity<BooleanWrapper>(
                new BooleanWrapper(gameRepository.turn(playableId, playableType, Direction.fromByte(direction))),
                HttpStatus.OK
        );
    }

    @RequestMapping(method = RequestMethod.PUT, value = "{playableId}/{playableType}/move/{direction}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<BooleanWrapper> move(@PathVariable long playableId, @PathVariable int playableType, @PathVariable byte direction)
            throws TankDoesNotExistException, LimitExceededException, IllegalTransitionException {
        return new ResponseEntity<BooleanWrapper>(
                new BooleanWrapper(gameRepository.move(playableId, playableType, Direction.fromByte(direction))),
                HttpStatus.OK
        );
    }

    @RequestMapping(method = RequestMethod.PUT, value = "{playableId}/{playableType}/build/{entity}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<BooleanWrapper> build(@PathVariable long playableId, @PathVariable int playableType, @PathVariable String entity)
            throws TankDoesNotExistException, LimitExceededException {
        return new ResponseEntity<BooleanWrapper>(
                new BooleanWrapper(gameRepository.build(playableId, playableType, entity)),
                HttpStatus.OK
        );
    }

    @RequestMapping(method = RequestMethod.PUT, value = "{playableId}/{playableType}/fire", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<BooleanWrapper> fire(@PathVariable long playableId, @PathVariable int playableType)
            throws TankDoesNotExistException, LimitExceededException {
        return new ResponseEntity<BooleanWrapper>(
                new BooleanWrapper(gameRepository.fire(playableId, playableType, 1)),
                HttpStatus.OK
        );
    }

    @RequestMapping(method = RequestMethod.PUT, value = "{playableId}/{playableType}/fire/{bulletType}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<BooleanWrapper> fire(@PathVariable long playableId, @PathVariable int playableType, @PathVariable int bulletType)
            throws TankDoesNotExistException, LimitExceededException {
        return new ResponseEntity<BooleanWrapper>(
                new BooleanWrapper(gameRepository.fire(playableId, playableType, bulletType)),
                HttpStatus.OK
        );
    }

    @RequestMapping(method = RequestMethod.GET, value = "{playableId}/{playableType}/life", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<IntWrapper> getLife(@PathVariable int playableId, @PathVariable int playableType)
            throws TankDoesNotExistException {
        return new ResponseEntity<IntWrapper>(
                new IntWrapper(gameRepository.getLife(playableId, playableType)),
                HttpStatus.OK
        );
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "{playableId}/leave", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    HttpStatus leave(@PathVariable long playableId)
            throws TankDoesNotExistException {
        //System.out.println("Games Controller leave() called, tank ID: "+tankId);
        gameRepository.leave(playableId);
        return HttpStatus.OK;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String handleBadRequests(Exception e) {
        return e.getMessage();
    }

    @RequestMapping(method = RequestMethod.PUT, value = "{playableId}/eject/0", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<BooleanWrapper> ejectSoldier(@PathVariable long playableId)
            throws TankDoesNotExistException, LimitExceededException {
        return new ResponseEntity<BooleanWrapper>(
                new BooleanWrapper(gameRepository.ejectSoldier(playableId)),
                HttpStatus.OK
        );
    }

    @RequestMapping(method = RequestMethod.PUT, value = "{playableId}/ejectPowerUp", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    ResponseEntity<BooleanWrapper> ejectPowerUp(@PathVariable long playableId)
            throws TankDoesNotExistException {
        return new ResponseEntity<BooleanWrapper>(
                new BooleanWrapper(gameRepository.ejectPowerUp(playableId)),
                HttpStatus.OK
        );
    }
}