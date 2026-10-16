package com.example.tournament.controller;

import com.example.tournament.model.Tournament;
import com.example.tournament.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tournaments")
public class TournamentController {

    @Autowired
    private TournamentService tournamentService;

    @GetMapping
    public List<Map<String, Object>> getAll() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Tournament tournament : tournamentService.getAll()) {
            result.add(toJson(tournament));
        }
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getOne(@PathVariable Long id) {
        return toJson(tournamentService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> create(@RequestBody Tournament tournament) {
        return toJson(tournamentService.create(tournament));
    }

    @PatchMapping("/{id}")
    public Map<String, Object> update(@PathVariable Long id, @RequestBody Tournament tournament) {
        return toJson(tournamentService.update(id, tournament));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        tournamentService.delete(id);
    }

    // переводим объект в json для ответа
    private Map<String, Object> toJson(Tournament tournament) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", tournament.getId());
        map.put("name", tournament.getName());
        map.put("teamLimit", tournament.getTeamLimit());
        map.put("status", tournament.getStatus());
        return map;
    }
}
