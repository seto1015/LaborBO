package de.hska.iwii.picturecommunity.converter;

import java.util.List;

import javax.annotation.Resource;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.springframework.stereotype.Component;

import de.hska.iwii.picturecommunity.backend.dao.UserDAO;
import de.hska.iwii.picturecommunity.backend.entities.User;


@Component("userConverter")
public class UserConverter implements Converter{


	@Resource(name = "userDAO")
	private UserDAO userDAO;
	
	private List<User> userDB;
	
		
	@Override
	 public Object getAsObject(FacesContext facesContext, UIComponent component, String submittedValue) {  
		if (submittedValue.trim().equals("")) {
			return null;
		} else {
			List<User> usersDB = getUserDB();
			for (User user : usersDB) {
				if (user.getName().equals(submittedValue)) {
					return user;
				}
			}
		}
		return null;
	}

	
	@Override
	public String getAsString(FacesContext facesContext, UIComponent component, Object value) {  
		if (value == null || value.equals("")) {
			return "";
		} else {
			return ((User) value).getName();
		}
	}


	public List<User> getUserDB() {	
		userDB = userDAO.findUsersByName("*", null);
		return userDB;
	}
	
}
