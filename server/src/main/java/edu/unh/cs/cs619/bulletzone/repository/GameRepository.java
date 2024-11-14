package edu.unh.cs.cs619.bulletzone.repository;

import org.javatuples.Pair;

import edu.unh.cs.cs619.bulletzone.model.Builder;
import edu.unh.cs.cs619.bulletzone.model.Direction;
import edu.unh.cs.cs619.bulletzone.model.IllegalTransitionException;
import edu.unh.cs.cs619.bulletzone.model.LimitExceededException;
import edu.unh.cs.cs619.bulletzone.model.Playable;
import edu.unh.cs.cs619.bulletzone.model.Tank;
import edu.unh.cs.cs619.bulletzone.model.Game;
import edu.unh.cs.cs619.bulletzone.model.TankDoesNotExistException;
import edu.unh.cs.cs619.bulletzone.util.LongWrapper;

public interface GameRepository {

    Tank join(String ip);

    Game getGame();

    boolean turn(long playableId, int playableType, Direction direction)
            throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException;

    boolean move(long playableId, int playableType, Direction direction)
            throws TankDoesNotExistException, IllegalTransitionException, LimitExceededException;

    boolean fire(long playableId, int playableType, int strength)
            throws TankDoesNotExistException, LimitExceededException;

    boolean build(long playableId, int playableType, String entity)
            throws TankDoesNotExistException, LimitExceededException;

    boolean ejectPowerUp(long playableId)
            throws TankDoesNotExistException;

    boolean ejectSoldier(long playableId)
            throws TankDoesNotExistException;

    int getLife(long playableId, int playableType)
        throws TankDoesNotExistException;

    public void leave(long playableId)
            throws TankDoesNotExistException;
}