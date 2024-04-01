package dev.betterstudios.it.database;

import dev.betterstudios.it.team.Team;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Database {


    void save(List<Team> teams);
    void load();



}
