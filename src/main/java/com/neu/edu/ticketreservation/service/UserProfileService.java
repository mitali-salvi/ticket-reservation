package com.neu.edu.ticketreservation.service;

import com.neu.edu.ticketreservation.bean.UserBean;
import com.neu.edu.ticketreservation.bean.UserProfile;
import com.neu.edu.ticketreservation.dao.UserProfileDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {
	
	@Autowired
	private UserProfileDao userProfileDao;
	
	public UserProfile save(UserProfile userProfile) {
		return userProfileDao.save(userProfile);
	}

	public UserProfile getFromUserBean(UserBean user) {
		return userProfileDao.getUserProfileFromUserBean(user.getId());
	}
}