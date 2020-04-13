package com.neu.edu.ticketreservation.controller;

import javax.validation.Valid;

import com.neu.edu.ticketreservation.bean.UserBean;
import com.neu.edu.ticketreservation.bean.UserProfile;
import com.neu.edu.ticketreservation.bean.wrapper.UserProfileWrapper;
import com.neu.edu.ticketreservation.dao.UserDao;
import com.neu.edu.ticketreservation.service.UserProfileService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProfileController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private UserDao userDao;

	@Autowired
	private UserProfileService userProfileService;

	@PostMapping(path = "/profile")
	public ResponseEntity<Object> createUserProfile(Authentication authentication,
			@Valid @RequestBody UserProfile userProfile) {
		logger.info("Post mapping");
		User user = (User) authentication.getPrincipal();

		UserBean userBean = userDao.findByUsername(user.getUsername());
		userProfile.setUser(userBean);
		userProfileService.save(userProfile);
		logger.info("User Profile:" + userProfile.getFirstName()+"  "+userProfile.getLastName());
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping(path = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getUserProfile(Authentication authentication ) {
		logger.info("Get mapping");
		User user = (User) authentication.getPrincipal();

		UserBean userBean = userDao.findByUsername(user.getUsername());
		if(userBean ==null){
			logger.error("No user found");
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		UserProfile userProfile = userProfileService.getFromUserBean(userBean);
		UserProfileWrapper recipeWrapper = new UserProfileWrapper();
		return new ResponseEntity<>(recipeWrapper.copyFromUser(userProfile), HttpStatus.CREATED);

	}

}