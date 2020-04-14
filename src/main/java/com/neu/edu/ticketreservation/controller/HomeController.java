package com.neu.edu.ticketreservation.controller;

import com.neu.edu.ticketreservation.bean.UserBean;
import com.neu.edu.ticketreservation.dao.UserDao;
import com.neu.edu.ticketreservation.util.SecurityUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserDao userDao;
    
    @Autowired
	private SecurityUtil securityUtil;

    @GetMapping(path = "/theatres")
	public ResponseEntity<Object> createUserProfile(Authentication authentication) {
        logger.info("Get theatres");
		UserBean userBean = securityUtil.getPrincipal(userDao);
		
		return new ResponseEntity<>(HttpStatus.OK);
	}


    
}