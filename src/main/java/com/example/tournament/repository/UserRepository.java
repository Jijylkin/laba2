package com.example.tournament.repository;

import com.example.tournament.model.User;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class UserRepository {

    private List<User> users = new ArrayList<>();
    private long nextId = 1;

    public List<User> findAll() {
        return users;
    }

    public User findById(Long id) {
        for (User user : users) {
            if (user.getId().equals(id)) {
                return user;
            }
        }
        return null;
    }

    public User save(User user) {
        user.setId(nextId++);
        users.add(user);
        return user;
    }

    public void deleteById(Long id) {
        users.removeIf(x -> x.getId().equals(id));
    }
}
