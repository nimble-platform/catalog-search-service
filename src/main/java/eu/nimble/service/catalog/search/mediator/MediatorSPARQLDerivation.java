package eu.nimble.service.catalog.search.mediator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.biba.triple.store.access.PropertyValuesCrawler;
import de.biba.triple.store.access.Reader;
import de.biba.triple.store.access.enums.PropertyType;
import eu.nimble.service.catalog.search.impl.dao.Group;
import eu.nimble.service.catalog.search.impl.dao.LocalOntologyView;

public class MediatorSPARQLDerivation {

	private static final String FURNITURE2_OWL = "furniture2.owl";
	private Reader reader = null;
	private PropertyValuesCrawler propertyValuesCrawler = null;

	public MediatorSPARQLDerivation() {
		reader = new Reader();
		reader.setModeToLocal();
		reader.loadOntologyModel(FURNITURE2_OWL);
		
		propertyValuesCrawler = new PropertyValuesCrawler();
		propertyValuesCrawler.setModeToLocal();
		propertyValuesCrawler.loadOntologyModel(FURNITURE2_OWL);
	}

	public List<String> detectPossibleConcepts(String regex) {
		return reader.getAllConcepts(regex);
	}

	public List<String> detectPossibleProperties(String regex) {

		return reader.getAllDirectProperties(regex);
		// return reader.getAllProperties(regex);
	}

	public LocalOntologyView getViewForOneStepRange(String concept, LocalOntologyView instance) {
		LocalOntologyView localOntologyView = null;

		if (instance == null) {
			localOntologyView = new LocalOntologyView();
		}
		else{
			localOntologyView = instance;
		}

		String conceptAsUri = getURIOfConcept(concept);
		List<String> properties = reader.getAllDirectProperties(conceptAsUri);
		for (String proeprty : properties) {
			PropertyType pType = reader.getPropertyType(proeprty);
			if (pType == PropertyType.DATATYPEPROPERTY) {
				proeprty = reduceUROJustToName(proeprty);
				localOntologyView.addDataproperties(proeprty);
			} else {
				// It is a object property which means I must return the name of
				// the concept
				List<String> ranges = reader.getRangeOfProperty(proeprty);
				for (int i = 0; i < ranges.size(); i++) {
					String range = ranges.get(i);
					range = reduceUROJustToName(range);
					LocalOntologyView localOntologyView2 = new LocalOntologyView();
					localOntologyView2.setConcept(range);
					localOntologyView.getObjectproperties().put(range, localOntologyView2);
				}
			}
		}

		return localOntologyView;

	}

	private String reduceUROJustToName(String range) {
		range = range.substring(range.indexOf("#") + 1);
		return range;
	}

	private String getURIOfConcept(String concept) {
		List<String> allPossibleConcepts = reader.getAllConcepts(concept);
		for (String conceptURI : allPossibleConcepts) {
			String conceptURIShortened = conceptURI.substring(conceptURI.indexOf("#") + 1);
			if (conceptURIShortened.equals(concept)) {
				return conceptURI;
			}
		}
		Logger.getAnonymousLogger().log(Level.WARNING, "Couldn't find right concept in ontology: " + concept);
		return concept;
	}
	
	private String getURIOfProperty(String property) {
		List<String> allPossibleProperties = reader.getAllProperties(property);
		for (String propertyURI : allPossibleProperties) {
			String propertyURIShortened = propertyURI.substring(propertyURI.indexOf("#") + 1);
			if (propertyURIShortened.equals(property)) {
				return propertyURI;
			}
		}
		Logger.getAnonymousLogger().log(Level.WARNING, "Couldn't find right concept in ontology: " + property);
		return property;
	}
	

	/**
	 * Die Methode nimmt den REader/Search um das zu machen 
	 * @param amountOfGroups
	 * @param concept
	 * @param property
	 * @return
	 */
	public Map<String, List<Group>> generateGroup(int amountOfGroups, String concept, String property) {
		// TODO Auto-generated method stub
		concept = getURIOfConcept(concept);
		property = getURIOfProperty(property);
		List<String> values = propertyValuesCrawler.getAllDifferentValuesForAProperty(concept, property);
		for (int i =0; i < values.size();i++){
			String str = values.get(i);
			int index =  str.lastIndexOf("^");
			if (index > -1){
				str = str.substring(0, index-1);
			}
			str = str.replace(",", ".");
			values.set(i, str);
		}
		try{
			Map<String, List<Group>> result = new HashMap<String, List<Group>>();
			float min = getMinOfData(values);
			float max = getMaxOfData(values);
			float stepRate = (max -min) / (float)amountOfGroups;
			List<Group> discreditedGroups = new ArrayList<Group>();
			for (int i = 0; i < amountOfGroups; i++){
				Group group = new Group();
				float newMin = min + (stepRate*i);
				float newMax = min + (stepRate* (i+1));
				group.setDescription("From: " + newMin + " to "+ newMax);
				group.setMin(newMin);
				group.setMax(newMax);
				group.setProperty(property);
				discreditedGroups.add(group);
			}
			result.put(property, discreditedGroups);
			return result;
		}
		catch (Exception e){
			Logger.getAnonymousLogger().log(Level.WARNING, "Cannot transform data from " + property + " into floats");
		}
		return null;
	}

	private float getMinOfData(List<String> values) {
		float min = 999999;
		for (String value: values){
			float number = Float.valueOf(value);
			if (number < min){
				min = number;
			}
		}
			
		return min;
	}
	private float getMaxOfData(List<String> values) {
		float max = -999999;
		for (String value: values){
			float number = Float.valueOf(value);
			if (number > max){
				max = number;
			}
		}
			
		return max;
	}
}