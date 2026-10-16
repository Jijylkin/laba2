package com.example.tournament.service;

import com.example.tournament.model.Match;
import com.example.tournament.model.Team;
import com.example.tournament.model.TeamApplication;
import com.example.tournament.repository.MatchRepository;
import com.example.tournament.repository.TeamApplicationRepository;
import com.example.tournament.repository.TeamRepository;
import com.example.tournament.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TeamApplicationRepository applicationRepository;
    @Autowired
    private MatchRepository matchRepository;

    public List<Team> getAll() {
        return teamRepository.findAll();
    }

    public Team getById(Long id) {
        Team team = teamRepository.findById(id);
        if (team == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Команда не найдена");
        return team;
    }

    public Team create(Team data) {
        if (data.getName() == null || data.getName().isBlank() || data.getCaptainId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нужно указать name и captainId");
        List<Long> members = makeMembers(data.getCaptainId(), data.getMemberIds());

        Team team = new Team();
        team.setName(data.getName());
        team.setCaptainId(data.getCaptainId());
        team.setMemberIds(members);
        return teamRepository.save(team);
    }

    public Team update(Long id, Team data) {
        Team team = getById(id);
        if (data.getName() != null && data.getName().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Название не может быть пустым");
        Long captainId = data.getCaptainId() != null ? data.getCaptainId() : team.getCaptainId();
        List<Long> memberIds = data.getMemberIds() != null ? data.getMemberIds() : team.getMemberIds();
        List<Long> members = makeMembers(captainId, memberIds);

        if (data.getName() != null) team.setName(data.getName());
        team.setCaptainId(captainId);
        team.setMemberIds(members);
        return team;
    }

    public void delete(Long id) {
        getById(id);
        // удаляем заявки команды и матчи, где она играла
        for (TeamApplication a : applicationRepository.findByTeamId(id)) {
            applicationRepository.deleteById(a.getId());
        }
        for (Match m : new ArrayList<>(matchRepository.findAll())) {
            if (m.getTeam1Id().equals(id) || m.getTeam2Id().equals(id)) {
                matchRepository.deleteById(m.getId());
            }
        }
        teamRepository.deleteById(id);
    }

    // проверяем всех игроков, капитан всегда входит в состав
    private List<Long> makeMembers(Long captainId, List<Long> memberIds) {
        if (userRepository.findById(captainId) == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Капитан не найден");
        List<Long> members = new ArrayList<>();
        members.add(captainId);
        if (memberIds != null) {
            for (Long userId : memberIds) {
                if (userId == null || userRepository.findById(userId) == null)
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Игрок " + userId + " не найден");
                if (!members.contains(userId)) members.add(userId);
            }
        }
        return members;
    }
}
