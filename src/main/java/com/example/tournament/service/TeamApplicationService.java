package com.example.tournament.service;

import com.example.tournament.model.ApplicationStatus;
import com.example.tournament.model.Match;
import com.example.tournament.model.TeamApplication;
import com.example.tournament.model.Tournament;
import com.example.tournament.model.TournamentStatus;
import com.example.tournament.repository.MatchRepository;
import com.example.tournament.repository.TeamApplicationRepository;
import com.example.tournament.repository.TeamRepository;
import com.example.tournament.repository.TournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TeamApplicationService {

    @Autowired
    private TeamApplicationRepository applicationRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TournamentRepository tournamentRepository;
    @Autowired
    private MatchRepository matchRepository;

    public List<TeamApplication> getAll() {
        return applicationRepository.findAll();
    }

    public TeamApplication getById(Long id) {
        TeamApplication application = applicationRepository.findById(id);
        if (application == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Заявка не найдена");
        return application;
    }

    public TeamApplication create(TeamApplication data) {
        if (data.getTeamId() == null || data.getTournamentId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нужно указать teamId и tournamentId");
        if (teamRepository.findById(data.getTeamId()) == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Команда не найдена");
        Tournament tournament = tournamentRepository.findById(data.getTournamentId());
        if (tournament == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Турнир не найден");
        if (tournament.getStatus() != TournamentStatus.REGISTRATION)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Регистрация на турнир закрыта");
        for (TeamApplication a : applicationRepository.findByTeamId(data.getTeamId())) {
            if (a.getTournamentId().equals(data.getTournamentId()))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Команда уже подала заявку на этот турнир");
        }

        TeamApplication application = new TeamApplication();
        application.setTeamId(data.getTeamId());
        application.setTournamentId(data.getTournamentId());
        application.setStatus(ApplicationStatus.PENDING); // решение принимается отдельно через PATCH
        application.setAppliedAt(LocalDateTime.now()); // время ставит сервер
        return applicationRepository.save(application);
    }

    public TeamApplication update(Long id, TeamApplication data) {
        TeamApplication application = getById(id);
        if ((data.getTeamId() != null && !data.getTeamId().equals(application.getTeamId()))
                || (data.getTournamentId() != null && !data.getTournamentId().equals(application.getTournamentId())))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Команду и турнир в заявке менять нельзя");

        ApplicationStatus newStatus = data.getStatus();
        if (newStatus != null && newStatus != application.getStatus()) {
            Tournament tournament = tournamentRepository.findById(application.getTournamentId());
            if (newStatus == ApplicationStatus.APPROVED) {
                if (tournament.getStatus() != TournamentStatus.REGISTRATION)
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Регистрация на турнир закрыта");
                if (countApproved(tournament.getId()) >= tournament.getTeamLimit())
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Лимит команд в турнире уже набран");
            }
            if (application.getStatus() == ApplicationStatus.APPROVED && hasMatches(application))
                throw new ResponseStatusException(HttpStatus.CONFLICT, "У команды уже есть матчи в этом турнире");
            application.setStatus(newStatus);
        }
        return application;
    }

    public void delete(Long id) {
        TeamApplication application = getById(id);
        // нельзя убрать допуск команды, которая уже играет матчи
        if (application.getStatus() == ApplicationStatus.APPROVED && hasMatches(application))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "У команды уже есть матчи в этом турнире");
        applicationRepository.deleteById(id);
    }

    public int countApproved(Long tournamentId) {
        int count = 0;
        for (TeamApplication a : applicationRepository.findByTournamentId(tournamentId)) {
            if (a.getStatus() == ApplicationStatus.APPROVED) count++;
        }
        return count;
    }

    public boolean isApproved(Long teamId, Long tournamentId) {
        for (TeamApplication a : applicationRepository.findByTeamId(teamId)) {
            if (a.getTournamentId().equals(tournamentId) && a.getStatus() == ApplicationStatus.APPROVED)
                return true;
        }
        return false;
    }

    private boolean hasMatches(TeamApplication application) {
        for (Match m : matchRepository.findByTournamentId(application.getTournamentId())) {
            if (m.getTeam1Id().equals(application.getTeamId()) || m.getTeam2Id().equals(application.getTeamId()))
                return true;
        }
        return false;
    }
}
