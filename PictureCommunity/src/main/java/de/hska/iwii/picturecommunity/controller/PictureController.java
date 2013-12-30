package de.hska.iwii.picturecommunity.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.hska.iwii.picturecommunity.backend.dao.PictureDAO;
import de.hska.iwii.picturecommunity.backend.entities.Picture;
import de.hska.iwii.picturecommunity.backend.entities.User;
import de.hska.iwii.picturecommunity.backend.utils.ImageUtils;

@Component
@Scope("session")
public class PictureController implements Serializable{
	
	@Resource(name = "pictureDAO")
	private PictureDAO pictureDAO;
	
	@Resource(name = "loginController")
	private LoginController loginController;
	
	private List<Picture> images; 
	
    private UploadedFile file;  
    
    private boolean publicVisable;
    
    
    public UploadedFile getFile() {  
        return file;  
    }  
  
    public void setFile(UploadedFile file) {  
        this.file = file;  
    }  
  
    
    
    
    
    public void upload() { 
		if (file != null) {
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
			picture.setDescription("Bildgröße: " + file.getSize());
			picture.setMimeType(file.getContentType());
			picture.setName(file.getFileName());
			picture.setPublicVisible(isPublicVisable());
			
			pictureDAO.createPicture(user, picture);
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
    
    
    public String fetchData() throws IOException{
    	List<Picture> pictures = pictureDAO.getPictures(loginController.getCurrentUser(), 0, 10, false);
    	for (Picture picture : pictures) {
    		picture.setData(ImageUtils.scale(picture.getData(), 500, 313)); 		
		}
    	this.images = pictures;
    	
    	return null;	
    }
    
    
	public List<Picture> getImages() {
		return images;
	}
    
	public boolean isPublicVisable() {
		return publicVisable;
	}

	public void setPublicVisable(boolean publicVisable) {
		this.publicVisable = publicVisable;
	}



}
