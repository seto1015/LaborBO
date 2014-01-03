package de.hska.iwii.picturecommunity.controller;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class OnlineUsers {
	
	private List<String> users;
	   
    @PostConstruct
    public void init() {
        this.users = new ArrayList<String>();
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }
   
    public void addUser(String user) {
        this.users.add(user);
    }
   
    public void removeUser(String user) {
        this.users.remove(user);
    }
   
    public boolean contains(String user) {
        return this.users.contains(user);
    }


}
