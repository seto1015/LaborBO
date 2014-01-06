package de.hska.iwii.picturecommunity.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.servlet.http.HttpServletRequest;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.primefaces.push.PushContext;
import org.primefaces.push.PushContextFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.hska.iwii.picturecommunity.backend.dao.PictureDAO;
import de.hska.iwii.picturecommunity.backend.dao.UserDAO;
import de.hska.iwii.picturecommunity.backend.entities.Picture;
import de.hska.iwii.picturecommunity.backend.entities.User;
import de.hska.iwii.picturecommunity.backend.utils.ImageUtils;

@Component
@Scope("session")
public class PictureController implements Serializable{
	
	private final PushContext pushContext = PushContextFactory.getDefault().getPushContext();  
	
	private final static String CHANNEL = "/chat"; 
	
	@Resource(name = "pictureDAO")
	private PictureDAO pictureDAO;
	
	@Resource(name = "loginController")
	private LoginController loginController;
	
	@Resource(name = "userDAO")
	private UserDAO userDAO;
	
	private List<Picture> images; 
	
    private UploadedFile file; 
    
    private String description;
    
    private boolean publicVisable;
    
 	private User selectedUser;
 	
 	private List<User> users;
    
    
	public String fetchData() throws IOException {
		FacesContext fc = FacesContext.getCurrentInstance();
		ExternalContext ec = fc.getExternalContext();
		HttpServletRequest rq = (HttpServletRequest) ec.getRequest();

		if (rq.getMethod().equals("GET")) {
			selectedUser = loginController.getCurrentUser();
			this.images = fetchPictures(selectedUser, 0, 10, false, 500, 313);
			users = userDAO.findUsersByName("*", null);
		}
		return null;
	}
    
    public void upload() throws IOException { 
		if (file.getSize() > 0) {
			User user = loginController.getCurrentUser();
			Picture picture = new Picture();

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] outputBuffer = new byte[1024];
			try {
				InputStream fis = file.getInputstream();
				
				for (int readNum; (readNum = fis.read(outputBuffer)) != -1;) {
					bos.write(outputBuffer, 0, readNum);
					// System.out.println("read " + readNum + " bytes,");
				}
				fis.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			byte[] bytes = bos.toByteArray();
			try {
				bos.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			picture.setData(bytes);
			picture.setCreator(user); 	
			picture.setDescription(description);
			picture.setMimeType(file.getContentType());
			picture.setName(file.getFileName());
			picture.setPublicVisible(isPublicVisable());
		
			pictureDAO.createPicture(user, picture);
			description = "";
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Erfolgreich", "Bild " + file.getFileName() + " wurde hochgeladen!"));
			updateGalleria();
		}
    }  
	
	public StreamedContent getImage() {
		FacesContext context = FacesContext.getCurrentInstance();
		if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
			// 1. Phase: Rendern der HTML-Seite
			return new DefaultStreamedContent();
		} else {
			// Jetzt wird erst im zweiten Aufruf das Bild angefordert.
			String id = context.getExternalContext().getRequestParameterMap().get("id");
			Picture pic = pictureDAO.getPicture(Integer.parseInt(id));
			return new DefaultStreamedContent(new ByteArrayInputStream(pic.getData()));
		}
	}
    
    
	private List<Picture> fetchPictures(User user, int firstResult, int maxResults, boolean onlyPublicVisable, int newImageWidth, int newImageHeight) throws IOException{
		List<Picture> pictures = pictureDAO.getPictures(user, firstResult, maxResults, onlyPublicVisable);
		for (Picture picture : pictures) {
			picture.setData(ImageUtils.scale(picture.getData(), newImageWidth, newImageHeight));
		}
		return pictures;
	}
	
   public String updateGalleria() throws IOException{
	   	User loggedInUser = loginController.getCurrentUser();
	   	Set<User> friends = loggedInUser.getFriendsOf();
	 	boolean onlyPublicVisable = true;
	   	
	 	if(selectedUser != null){
		 	if(selectedUser.equals(loggedInUser) || friends.contains(selectedUser)){
		   		onlyPublicVisable = false;
		   	}
	 	}
	 	this.images = fetchPictures(selectedUser, 0, 10, onlyPublicVisable, 500, 313);
	   
	   return null;   
   }
    
   
   public String addFriend(){
	
//	 User loggedInUser = loginController.getCurrentUser();
//	  
//	 Set<User> friends = loggedInUser.getFriendsOf();
//	
//	 if(friends.contains(selectedUser)){
//		 FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Info", selectedUser.getName() + " ist bereits mit dir befreundet!"));
//	 }else{
//		 loggedInUser.getFriendsOf().add(selectedUser);
//		 userDAO.updateUser(loggedInUser);
//		 loginController.updateCurrentUser();
//		 FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Erfolgreich", selectedUser.getName() + " kann nun auch deine privaten Bilder sehen!"));
//	 }
	 FacesContext fc = FacesContext.getCurrentInstance();
	 User loggedInUser = loginController.getCurrentUser(); 
	 
	 if(loggedInUser.equals(selectedUser)){
		fc.addMessage(null, new FacesMessage("Info", "Du kannst nicht mit dir selbst befreundet sein!"));
		
		return null;
	 }
		  
	 Set<User> friends = selectedUser.getFriendsOf();
	
	 if(friends.contains(loggedInUser)){
		 fc.addMessage(null, new FacesMessage("Info", selectedUser.getName() + " ist bereits mit dir befreundet!"));
	 }else{
		 selectedUser.getFriendsOf().add(loggedInUser);
		 userDAO.updateUser(selectedUser);
		 fc.addMessage(null, new FacesMessage("Erfolgreich", selectedUser.getName() + " kann nun auch deine privaten Bilder sehen!"));
		 pushContext.push(CHANNEL, selectedUser.getName() + " is now friends with " + loggedInUser.getName());
	 }
	   
	   return null;   
   }
   
	public String homeGalleria() throws IOException {
		selectedUser = loginController.getCurrentUser();
		updateGalleria();

		return null;
	}
    
	public List<Picture> getImages() {
		return images;
	}
    
    public UploadedFile getFile() {  
        return file;  
    }  
  
    public void setFile(UploadedFile file) {  
        this.file = file;  
    } 
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isPublicVisable() {
		return publicVisable;
	}

	public void setPublicVisable(boolean publicVisable) {
		this.publicVisable = publicVisable;
	}

	public User getSelectedUser() {
		return selectedUser;
	}


	public void setSelectedUser(User selectedUser) {
		this.selectedUser = selectedUser;
	}


	public List<User> getUsers() {		
		return users;
	}


}
