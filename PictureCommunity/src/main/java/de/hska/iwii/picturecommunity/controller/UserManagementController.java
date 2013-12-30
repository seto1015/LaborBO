package de.hska.iwii.picturecommunity.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.faces.model.ListDataModel;

import org.primefaces.model.SelectableDataModel;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.hska.iwii.picturecommunity.backend.dao.UserDAO;
import de.hska.iwii.picturecommunity.backend.entities.User;

@Component
@Scope("session")
public class UserManagementController  implements Serializable{

	
	@Resource(name = "userDAO")
	private UserDAO userDAO;
	
	
	private List<User> users;
	
	private User selectedUser;
	
	public String fetchData(){
		users = userDAO.findUsersByName("*", null);
		
		return null;
		
	}

	public List<User> getUsers() {
		return users;
	}


	
	public String deleteUser() {

		userDAO.deleteUser(selectedUser);

		return null;
	}

	public User getSelectedUser() {
		return selectedUser;
	}

	public void setSelectedUser(User selectedUser) {
		this.selectedUser = selectedUser;
	}

	
	
}
