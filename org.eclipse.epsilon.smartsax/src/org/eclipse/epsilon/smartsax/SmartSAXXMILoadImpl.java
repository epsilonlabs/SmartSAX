package org.eclipse.epsilon.smartsax;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.impl.XMILoadImpl;
import org.xml.sax.helpers.DefaultHandler;

public class SmartSAXXMILoadImpl extends XMILoadImpl{
	public boolean loadAllAttributes = true;
	
	protected HashMap<String, HashMap<String, ArrayList<String>>> objectsAndRefNamesToVisit = new HashMap<String, HashMap<String,ArrayList<String>>>();
	protected HashMap<String, HashMap<String, ArrayList<String>>> actualObjectsToLoad = new HashMap<String, HashMap<String,ArrayList<String>>>();
	protected HashMap<String, HashMap<String, ArrayList<String>>> typesToLoad = new HashMap<String, HashMap<String,ArrayList<String>>>();

	public void clearCollections()
	{
		objectsAndRefNamesToVisit.clear();
		objectsAndRefNamesToVisit = null;
		actualObjectsToLoad.clear();
		actualObjectsToLoad = null;
	}
	
	public void setObjectsAndRefNamesToVisit(
			HashMap<String, HashMap<String, ArrayList<String>>> objectsAndRefNamesToVisit) {
		this.objectsAndRefNamesToVisit = objectsAndRefNamesToVisit;
	}

	public void setActualObjectsToLoad(
			HashMap<String, HashMap<String, ArrayList<String>>> actualObjectsToLoad) {
		this.actualObjectsToLoad = actualObjectsToLoad;
	}
	
	public void setTypesToLoad(
			HashMap<String, HashMap<String, ArrayList<String>>> typesToLoad) {
		this.typesToLoad = typesToLoad;
	}
	
	public SmartSAXXMILoadImpl(XMLHelper helper) {
		super(helper);
	}
	
	public void setLoadAllAttributes(boolean loadAllAttributes) {
		this.loadAllAttributes = loadAllAttributes;
	}
	
	@Override
	protected DefaultHandler makeDefaultHandler() {
		SmartSAXXMIHandler handler = new SmartSAXXMIHandler(resource, helper, options); 
		handler.setLoadAllAttributes(loadAllAttributes);

		handler.setObjectsAndRefNamesToVisit(objectsAndRefNamesToVisit);
		handler.setActualObjectsToLoad(actualObjectsToLoad);
		handler.setTypesToLoad(typesToLoad);
		return handler; 
		
	}

}
