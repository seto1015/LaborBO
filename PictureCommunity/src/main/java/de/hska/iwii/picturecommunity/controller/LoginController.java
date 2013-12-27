package de.hska.iwii.picturecommunity.controller;

import java.io.Serializable;

import javax.annotation.Resource;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

import de.hska.iwii.picturecommunity.backend.dao.UserDAO;
import de.hska.iwii.picturecommunity.backend.entities.User;

@Component
@Scope("session")
public class LoginController implements Serializable{

	@Resource(name = "authenticationManager")
	private AuthenticationManager authenticationManager;
	
	@Resource(name = "userDAO")
	private UserDAO userDAO;
	
	private String name;
	
	private String email;
	
	private String password;
	
	
	public String newUser(){
		
		
				User user = new User(email, password, name, User.ROLE_USER); // User-Objekt aus der Datenbank
				String passwd = user.getPassword(); // Passwort des Users
				
				
				userDAO.createUser(user);
				
				// Anmeldung durch Username und Password
				UsernamePasswordAuthenticationToken token
				= new UsernamePasswordAuthenticationToken(user, passwd);
				// Mit Spring-Security anmelden, dazu muss
				// der neue Anwender bereits in der Datenbank
				// vorhanden sein.
				
			
				Authentication authUser = authenticationManager.authenticate(token);
				// Sicherheitshalber prüfen, ob die Anmeldung geklappt hat
				// (sollte eigentlich immer der Falls ein).
				if (authUser.isAuthenticated()) {
				// Anmeldeinformation im Security-Kontext speichern
				SecurityContext sc = SecurityContextHolder.getContext();
				sc.setAuthentication(authUser);
				// Session anlegen und Security-Kontext darin speichern
				// (JSF-Spezifisch)
				FacesContext fc = FacesContext.getCurrentInstance();
				ExternalContext ec = fc.getExternalContext();
				((HttpSession) ec.getSession(true)).setAttribute(
				HttpSessionSecurityContextRepository.
				SPRING_SECURITY_CONTEXT_KEY,
				SecurityContextHolder.getContext());
				return "/pages/private/private.xhtml";
				}
				return null;
				
	}

	public boolean getLoggedIn() {
		SecurityContext sc = SecurityContextHolder.getContext();

		if (sc.getAuthentication().isAuthenticated()) {
			return true;
		}
		return false;
	}
	
	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public String getPassword() {
		return password;
	}


	public void setPassword(String password) {
		this.password = password;
	}
	
}
