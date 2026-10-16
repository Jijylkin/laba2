package com.example.tournament.repository;

import com.example.tournament.model.Match;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class MatchRepository {

    private List<Match> matches = new ArrayList<>();
    private long nextId = 1;

    public List<Match> findAll() {
        return matches;
    }

    public Match findById(Long id) {
        for (Match match : matches) {
            if (match.getId().equals(id)) {
                return match;
            }
        }
        return null;
    }

    public Match save(Match match) {
        match.setId(nextId++);
        matches.add(match);
        return match;
    }

    public void deleteById(Long id) {
        matches.removeIf(x -> x.getId().equals(id));
    }

    public List<Match> findByTournamentId(Long tournamentId) {
        List<Match> result = new ArrayList<>();
        for (Match match : matches) {
            if (match.getTournamentId().equals(tournamentId)) {
                result.add(match);
            }
        }
        return result;
    }
}
