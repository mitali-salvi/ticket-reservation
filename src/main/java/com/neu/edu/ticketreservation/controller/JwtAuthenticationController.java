package com.neu.edu.ticketreservation.controller;

import com.neu.edu.ticketreservation.bean.JwtRequest;
import com.neu.edu.ticketreservation.bean.JwtResponse;
import com.neu.edu.ticketreservation.bean.UserDTO;
import com.neu.edu.ticketreservation.config.security.JwtTokenUtil;
import com.neu.edu.ticketreservation.service.JwtUserDetailsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.instrument.MeterRegistry;

@RestController
public class JwtAuthenticationController {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtTokenUtil jwtTokenUtil;

	@Autowired
	private JwtUserDetailsService userDetailsService;

	@Autowired
	MeterRegistry registry;

	// @RequestMapping(value = "/v1/authenticate", method = RequestMethod.POST)
	// public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {

	// 	registry.counter("custom.metrics.counter", "ApiCall", "AuthenticatePost").increment();
	// 	authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());

	// 	final UserDetails userDetails = userDetailsService
	// 			.loadUserByUsername(authenticationRequest.getUsername());

	// 	final String token = jwtTokenUtil.generateToken(userDetails);

	// 	return ResponseEntity.ok(new JwtResponse(token));
	// }
	
	// @RequestMapping(value = "/v1/register", method = RequestMethod.POST)
	// public ResponseEntity<?> saveUser(@RequestBody UserDTO user) throws Exception {
	// 	registry.counter("custom.metrics.counter", "ApiCall", "RegisterPost").increment();
	// 	if(userDetailsService.findUser(user.getUsername())!=null){
	// 		logger.info("User already exists");
    //         return new ResponseEntity<>("User already exists.", HttpStatus.BAD_REQUEST);
	// 	}

	// 	logger.info("User saved successfully");

	// 	return ResponseEntity.ok(userDetailsService.save(user).getUsername());
	// }

	// private void authenticate(String username, String password) throws Exception {
	// 	try {
	// 		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
	// 	} catch (DisabledException e) {
	// 		throw new Exception("USER_DISABLED", e);
	// 	} catch (BadCredentialsException e) {
	// 		throw new Exception("INVALID_CREDENTIALS", e);
	// 	}
	// }
}