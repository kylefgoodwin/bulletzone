package edu.unh.cs.cs619.bulletzone.model;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public final class PlayableDoesNotExistException extends Exception {
    public PlayableDoesNotExistException(Long Id, int playableType) {
        super(String.format("Playable id:'%d' type:'%d' does not exist", Id, playableType));
    }
}