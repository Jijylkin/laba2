package com.example.tournament.repository;

import com.example.tournament.model.Tournament;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class TournamentRepository {

    private List<Tournament> tournaments = new ArrayList<>();
    private long nextId = 1;

    public List<Tournament> findAll() {
        return tournaments;
    }

    public Tournament findById(Long id) {
        for (Tournament tournament : tournaments) {
            if (tournament.getId().equals(id)) {
                return tournament;
            }
        }
        return null;
    }

    public Tournament save(Tournament tournament) {
        tournament.setId(nextId++);
        tournaments.add(tournament);
        return tournament;
    }

    public void deleteById(Long id) {
        tournaments.removeIf(x -> x.getId().equals(id));
    }
}
