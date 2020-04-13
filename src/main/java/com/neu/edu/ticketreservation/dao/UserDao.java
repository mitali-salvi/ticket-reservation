package com.neu.edu.ticketreservation.dao;

import com.neu.edu.ticketreservation.bean.DAOUser;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends CrudRepository<DAOUser, Integer> {
	
	DAOUser findByUsername(String username);
	
}