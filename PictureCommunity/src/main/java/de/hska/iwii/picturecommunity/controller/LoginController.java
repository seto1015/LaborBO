package de.hska.iwii.picturecommunity.controller;

import java.io.IOException;
import java.io.Serializable;

import javax.annotation.Resource;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
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
	
	private boolean admin;
	
	private boolean loggedIn = false;
	
	private User currentUser;
	
	
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
				
				this.loggedIn = true;
				this.currentUser = user;
				this.admin = false;
				return "/pages/private/pictures.xhtml";
				}
				return null;
				
	}

	public String login() throws ServletException, IOException {
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		HttpServletRequest request = ((HttpServletRequest) context.getRequest());

		ServletResponse resposnse = ((ServletResponse) context.getResponse());
		RequestDispatcher dispatcher = request.getRequestDispatcher("/PictureCommunity/j_spring_security_check");
		dispatcher.forward(request, resposnse);
		SecurityContext sc = SecurityContextHolder.getContext();
		if (sc.getAuthentication() != null
				&& sc.getAuthentication().getPrincipal() instanceof User
				&& sc.getAuthentication().isAuthenticated()) {
			loggedIn = true;

			// User ist das Anwender-Objekt aus der Datenbank.
			this.currentUser = (User) sc.getAuthentication().getPrincipal();
			String userRole = currentUser.getRole();

			if (userRole.equals("user")) {
				this.admin = false;
			} else if (userRole.equals("admin")) {
				this.admin = true;
			}

		}
		FacesContext.getCurrentInstance().responseComplete();

		return null;
	}
	
	   public String loginOut(){
		   if(this.loggedIn){
			   return logout();
		   }
		   return "/pages/login.xhtml";
	   }
	   
	     public String logout() {
             SecurityContextHolder.getContext().setAuthentication(null);
             FacesContext.getCurrentInstance().getExternalContext().getSessionMap()
                             .clear();
             loggedIn = false;
             currentUser = null;
             return "/index.xhtml";
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

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}
	
	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public User getCurrentUser() {
		return currentUser;
	}
	
}
