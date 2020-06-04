package com.dal.catmeclone.authenticationandauthorization;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller	;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.dal.catmeclone.model.User;

@Controller
public class AuthenticationController {

	@RequestMapping("/login")
	public String login(Model model) {
		model.addAttribute("login", new User());
		return "login";
		
	}
	

	@RequestMapping("/") 
	public String Home()
	{
		
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String userole=authentication.getAuthorities().toArray()[0].toString();
		if(userole.equalsIgnoreCase("Admin"))
		{
			return "redirect:/adminDashboard";
		}
		else 
		{
			return "index";
		}
				

	}
	
	@GetMapping("/access-denied")
	public String showAccessDenied() {
		
		return "access-denied";
		
	}
	
}