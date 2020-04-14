package com.neu.edu.ticketreservation.controller;

import javax.validation.Valid;

import com.neu.edu.ticketreservation.bean.CreditCardDetails;
import com.neu.edu.ticketreservation.bean.UserBean;
import com.neu.edu.ticketreservation.bean.UserProfile;
import com.neu.edu.ticketreservation.bean.wrapper.UserProfileWrapper;
import com.neu.edu.ticketreservation.dao.UserDao;
import com.neu.edu.ticketreservation.service.StripeService;
import com.neu.edu.ticketreservation.service.UserProfileService;
import com.neu.edu.ticketreservation.util.SecurityUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

	@Autowired
	private StripeService stripeService;

	@Autowired
	private SecurityUtil securityUtil;

	@PostMapping(path = "/profile")
	public ResponseEntity<Object> createUserProfile(Authentication authentication,
			@Valid @RequestBody UserProfile userProfile) {
		logger.info("Post mapping");
		UserBean userBean = securityUtil.getPrincipal(userDao);
		userProfile.setUser(userBean);
		userProfileService.save(userProfile);
		logger.info("User Profile:" + userProfile.getFirstName()+"  "+userProfile.getLastName());
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping(path = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> getUserProfile(Authentication authentication ) {
		logger.info("Get mapping");
		UserBean userBean = securityUtil.getPrincipal(userDao);
		if(userBean ==null){
			logger.error("No user found");
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		UserProfile userProfile = userProfileService.getFromUserBean(userBean);
		UserProfileWrapper recipeWrapper = new UserProfileWrapper();
		return new ResponseEntity<>(recipeWrapper.copyFromUser(userProfile), HttpStatus.CREATED);
	}

	@PostMapping(path = "/addPayment")
	public ResponseEntity<Object> addPaymentMethod(Authentication authentication,
			@Valid @RequestBody CreditCardDetails creditCardDetails) {
		logger.info("Post mapping");
		UserBean userBean = securityUtil.getPrincipal(userDao);
		UserProfile userProfile = userProfileService.getFromUserBean(userBean);
		if(userProfile.isPaymentMethodAdded()){
			return new ResponseEntity<>("Payment method already exists" ,HttpStatus.BAD_REQUEST);
		}

		String customerId = stripeService.addCardToCustomer(userBean, userProfile, creditCardDetails);
		if(customerId ==null){
			return new ResponseEntity<>("Error attaching card to customer" ,HttpStatus.BAD_REQUEST);
		}

		userProfile.setStripeCustomerId(customerId);
		userProfile.setPaymentMethodAdded(true);
		userProfileService.save(userProfile);

		return new ResponseEntity<>("Successfully attached card to customer" ,HttpStatus.OK);
	}

}