package de.hska.iwii.picturecommunity.controller;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;

import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.hska.iwii.picturecommunity.backend.dao.PictureDAO;
import de.hska.iwii.picturecommunity.backend.dao.UserDAO;
import de.hska.iwii.picturecommunity.backend.entities.User;
import de.hska.iwii.picturecommunity.backend.utils.DataUtils;

@Component
@Scope("session")
public class StatisticsController implements Serializable {

	
	@Resource(name = "userDAO")
	private UserDAO userDAO;
		
	@Resource(name = "pictureDAO")
	private PictureDAO pictureDAO;
	
	private CartesianChartModel mostUploadedModel;  
	
	private int userAmount = 10;
	 
	public void fetchData() {
		Map<String, Long> values = new HashMap<String, Long>();
		mostUploadedModel = new CartesianChartModel();
		
		List<User> users = userDAO.findUsersByName("*", null);
		for (User user : users) {
			values.put(user.getName(), pictureDAO.getPictureCount(user));
		}
		Map<String, Long> sortedValues = DataUtils.sortByComparator(values);

		System.out.println("Unsort Map......");
		printMap(values);

		System.out.println("Sorted Map......");
		printMap(sortedValues);

		System.out.println("User Amount for diagramm: " + userAmount);
		
		ChartSeries cs = new ChartSeries();
		Iterator<Entry<String, Long>> iter = sortedValues.entrySet().iterator();
		for (int i = 0; iter.hasNext() && i < userAmount; i++) {
			Entry<String, Long> entry = iter.next();
			cs.setLabel("Bilder");
			cs.set(entry.getKey(), entry.getValue());

		}
		mostUploadedModel.addSeries(cs);
	}


	private void printMap(Map<String, Long> map) {
		for (Map.Entry entry : map.entrySet()) {
			System.out.println("Key : " + entry.getKey() + " Value : "
					+ entry.getValue());
		}
	}
	 
	public CartesianChartModel getMostUploadedModel() {
		return mostUploadedModel;
	}


	public int getUserAmount() {
		return userAmount;
	}


	public void setUserAmount(int userAmount) {
		this.userAmount = userAmount;
	}



	 
	 
}
