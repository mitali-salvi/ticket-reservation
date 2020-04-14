package com.neu.edu.ticketreservation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @RequestMapping({ "/hello" })
	public String firstPage() {
		return "Hello World";
	}


    
}