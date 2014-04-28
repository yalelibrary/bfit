

package edu.yale.library.ladybird.persistence.dao;

import edu.yale.library.entity.model.User;

import java.util.List;

public interface UserDAO extends GenericDAO<User, Integer> {

    List<User> findByEmail(String field);

    List<User> findByUsername(String field);

}

