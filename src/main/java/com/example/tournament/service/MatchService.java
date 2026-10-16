package com.example.tournament.service;

import com.example.tournament.model.Match;
import com.example.tournament.model.MatchStatus;
import com.example.tournament.model.Tournament;
import com.example.tournament.model.TournamentStatus;
import com.example.tournament.repository.MatchRepository;
import com.example.tournament.repository.TournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MatchService {

    @Autowired
    private MatchRepository matchRepository;
    @Autowired
    private TournamentRepository tournamentRepository;
    @Autowired
    private TeamApplicationService applicationService;

    public List<Match> getAll() {
        return matchRepository.findAll();
    }

    public Match getById(Long id) {
        Match match = matchRepository.findById(id);
        if (match == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Матч не найден");
        return match;
    }

    public Match create(Match data) {
        if (data.getTournamentId() == null || data.getTeam1Id() == null || data.getTeam2Id() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нужно указать tournamentId, team1Id и team2Id");
        Tournament tournament = tournamentRepository.findById(data.getTournamentId());
        if (tournament == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Турнир не найден");
        if (tournament.getStatus() != TournamentStatus.IN_PROGRESS)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Матчи можно создавать только когда турнир идёт");
        if (data.getTeam1Id().equals(data.getTeam2Id()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Команда не может играть сама с собой");
        if (!applicationService.isApproved(data.getTeam1Id(), tournament.getId())
                || !applicationService.isApproved(data.getTeam2Id(), tournament.getId()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Обе команды должны быть допущены к турниру");

        Match match = new Match();
        match.setTournamentId(data.getTournamentId());
        match.setTeam1Id(data.getTeam1Id());
        match.setTeam2Id(data.getTeam2Id());
        match.setScore1(0);
        match.setScore2(0);
        match.setStatus(MatchStatus.SCHEDULED);
        return matchRepository.save(match);
    }

    public Match update(Long id, Match data) {
        Match match = getById(id);
        if (match.getStatus() == MatchStatus.FINISHED)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Матч уже завершён, менять нельзя");
        if ((data.getTournamentId() != null && !data.getTournamentId().equals(match.getTournamentId()))
                || (data.getTeam1Id() != null && !data.getTeam1Id().equals(match.getTeam1Id()))
                || (data.getTeam2Id() != null && !data.getTeam2Id().equals(match.getTeam2Id())))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Турнир и команды матча менять нельзя");
        if ((data.getScore1() != null && data.getScore1() < 0) || (data.getScore2() != null && data.getScore2() < 0))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Счёт не может быть отрицательным");

        if (data.getScore1() != null) match.setScore1(data.getScore1());
        if (data.getScore2() != null) match.setScore2(data.getScore2());
        if (data.getStatus() != null) match.setStatus(data.getStatus());
        return match;
    }

    public void delete(Long id) {
        getById(id);
        matchRepository.deleteById(id);
    }
}
