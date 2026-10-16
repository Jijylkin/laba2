package com.example.tournament.repository;

import com.example.tournament.model.Team;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class TeamRepository {

    private List<Team> teams = new ArrayList<>();
    private long nextId = 1;

    public List<Team> findAll() {
        return teams;
    }

    public Team findById(Long id) {
        for (Team team : teams) {
            if (team.getId().equals(id)) {
                return team;
            }
        }
        return null;
    }

    public Team save(Team team) {
        team.setId(nextId++);
        teams.add(team);
        return team;
    }

    public void deleteById(Long id) {
        teams.removeIf(x -> x.getId().equals(id));
    }

    public List<Team> findByCaptainId(Long captainId) {
        List<Team> result = new ArrayList<>();
        for (Team team : teams) {
            if (team.getCaptainId().equals(captainId)) {
                result.add(team);
            }
        }
        return result;
    }
}
