package com.example.tournament.service;

import com.example.tournament.model.Match;
import com.example.tournament.model.TeamApplication;
import com.example.tournament.model.Tournament;
import com.example.tournament.model.TournamentStatus;
import com.example.tournament.repository.MatchRepository;
import com.example.tournament.repository.TeamApplicationRepository;
import com.example.tournament.repository.TournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class TournamentService {

    @Autowired
    private TournamentRepository tournamentRepository;
    @Autowired
    private TeamApplicationRepository applicationRepository;
    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private TeamApplicationService applicationService;

    public List<Tournament> getAll() {
        return tournamentRepository.findAll();
    }

    public Tournament getById(Long id) {
        Tournament tournament = tournamentRepository.findById(id);
        if (tournament == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Турнир не найден");
        return tournament;
    }

    public Tournament create(Tournament data) {
        if (data.getName() == null || data.getName().isBlank() || data.getTeamLimit() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нужно указать name и teamLimit");
        if (data.getTeamLimit() < 2)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Лимит команд должен быть не меньше 2");

        Tournament tournament = new Tournament();
        tournament.setName(data.getName());
        tournament.setTeamLimit(data.getTeamLimit());
        tournament.setStatus(TournamentStatus.REGISTRATION); // новый турнир всегда на регистрации
        return tournamentRepository.save(tournament);
    }

    public Tournament update(Long id, Tournament data) {
        Tournament tournament = getById(id);
        int approved = applicationService.countApproved(id);

        if (data.getName() != null && data.getName().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Название не может быть пустым");
        if (data.getTeamLimit() != null) {
            if (data.getTeamLimit() < 2)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Лимит команд должен быть не меньше 2");
            if (data.getTeamLimit() < approved)
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Допущено больше команд, чем новый лимит");
        }
        if (data.getStatus() != null && data.getStatus() != tournament.getStatus()) {
            if (tournament.getStatus() == TournamentStatus.FINISHED)
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Турнир уже завершён");
            if (data.getStatus() == TournamentStatus.IN_PROGRESS && approved < 2)
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Для старта нужно хотя бы 2 допущенные команды");
        }

        if (data.getName() != null) tournament.setName(data.getName());
        if (data.getTeamLimit() != null) tournament.setTeamLimit(data.getTeamLimit());
        if (data.getStatus() != null) tournament.setStatus(data.getStatus());
        return tournament;
    }

    public void delete(Long id) {
        getById(id);
        // вместе с турниром удаляем его заявки и матчи
        for (TeamApplication a : applicationRepository.findByTournamentId(id)) {
            applicationRepository.deleteById(a.getId());
        }
        for (Match m : matchRepository.findByTournamentId(id)) {
            matchRepository.deleteById(m.getId());
        }
        tournamentRepository.deleteById(id);
    }
}
