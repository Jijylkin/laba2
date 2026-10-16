package com.example.tournament.repository;

import com.example.tournament.model.TeamApplication;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class TeamApplicationRepository {

    private List<TeamApplication> applications = new ArrayList<>();
    private long nextId = 1;

    public List<TeamApplication> findAll() {
        return applications;
    }

    public TeamApplication findById(Long id) {
        for (TeamApplication application : applications) {
            if (application.getId().equals(id)) {
                return application;
            }
        }
        return null;
    }

    public TeamApplication save(TeamApplication application) {
        application.setId(nextId++);
        applications.add(application);
        return application;
    }

    public void deleteById(Long id) {
        applications.removeIf(x -> x.getId().equals(id));
    }

    public List<TeamApplication> findByTeamId(Long teamId) {
        List<TeamApplication> result = new ArrayList<>();
        for (TeamApplication application : applications) {
            if (application.getTeamId().equals(teamId)) {
                result.add(application);
            }
        }
        return result;
    }

    public List<TeamApplication> findByTournamentId(Long tournamentId) {
        List<TeamApplication> result = new ArrayList<>();
        for (TeamApplication application : applications) {
            if (application.getTournamentId().equals(tournamentId)) {
                result.add(application);
            }
        }
        return result;
    }
}
