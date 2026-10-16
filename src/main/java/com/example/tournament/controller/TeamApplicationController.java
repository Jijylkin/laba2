package com.example.tournament.controller;

import com.example.tournament.model.TeamApplication;
import com.example.tournament.service.TeamApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/team-applications")
public class TeamApplicationController {

    @Autowired
    private TeamApplicationService teamApplicationService;

    @GetMapping
    public List<Map<String, Object>> getAll() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (TeamApplication application : teamApplicationService.getAll()) {
            result.add(toJson(application));
        }
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getOne(@PathVariable Long id) {
        return toJson(teamApplicationService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@RequestBody TeamApplication application) {
        return toJson(teamApplicationService.create(application));
    }

    @PatchMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody TeamApplication application) {
        return toJson(teamApplicationService.update(id, application));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        teamApplicationService.delete(id);
    }

    // переводим объект в json для ответа
    private Map<String, Object> toJson(TeamApplication application) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", application.getId());
        map.put("teamId", application.getTeamId());
        map.put("tournamentId", application.getTournamentId());
        map.put("status", application.getStatus());
        map.put("appliedAt", application.getAppliedAt());
        return map;
    }
}
