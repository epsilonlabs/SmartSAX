package org.eclipse.epsilon.smartsax;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.xmi.XMLLoad;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLHelperImpl;
import org.eclipse.epsilon.smartsax.effectivemetamodel.impl.EffectiveMetamodel;

public class SmartSAXXMIResource extends XMIResourceImpl{
	
	public static final String OPTION_EFFECTIVE_METAMODELS = "effective-metamodels";
	public static final String OPTION_RECONCILE = "reconcile";
	public static final String OPTION_LOAD_ALL_ATTRIBUTES = "load-all-attributes";
	public static final String OPTION_EFFECTIVE_METAMODEL_RECONCILER = "effective-metamodel-reconciler";
	public static final String OPTION_RECONCILE_EFFECTIVE_METAMODELS = "reconcile-effective-metamodels";

	
	public boolean loadAllAttributes = true;

	
	protected HashMap<String, HashMap<String, ArrayList<String>>> objectsAndRefNamesToVisit = new HashMap<String, HashMap<String,ArrayList<String>>>();
	protected HashMap<String, HashMap<String, ArrayList<String>>> actualObjectsToLoad = new HashMap<String, HashMap<String,ArrayList<String>>>();
	protected HashMap<String, HashMap<String, ArrayList<String>>> typesToLoad = new HashMap<String, HashMap<String,ArrayList<String>>>();

	protected boolean handleFlatObjects = false;
	protected SmartSAXXMILoadImpl sxl;
	
	public void clearCollections()
	{
		objectsAndRefNamesToVisit.clear();
		objectsAndRefNamesToVisit = null;
		actualObjectsToLoad.clear();
		actualObjectsToLoad = null;
		sxl.clearCollections();
	}
	
	
	@Override
	public void load(Map<?, ?> options) throws IOException {
		
		//loadAllAttributes = (Boolean) options.get(OPTION_LOAD_ALL_ATTRIBUTES);
		
		ArrayList<EffectiveMetamodel> effectiveMetamodels = (ArrayList<EffectiveMetamodel>) options.get(OPTION_EFFECTIVE_METAMODELS);
		if (effectiveMetamodels != null) {
			EffectiveMetamodelReconciler effectiveMetamodelReconciler = new EffectiveMetamodelReconciler();
			effectiveMetamodelReconciler.addEffectiveMetamodels(effectiveMetamodels);
			effectiveMetamodelReconciler.addPackages(getResourceSet().getPackageRegistry().values());
			if ((Boolean) options.get(OPTION_RECONCILE_EFFECTIVE_METAMODELS)) {
				effectiveMetamodelReconciler.reconcile();
			}
			actualObjectsToLoad = effectiveMetamodelReconciler.getActualObjectsToLoad();
			objectsAndRefNamesToVisit = effectiveMetamodelReconciler.getObjectsAndRefNamesToVisit();
			typesToLoad = effectiveMetamodelReconciler.getTypesToLoad();
		}
		else {
			EffectiveMetamodelReconciler effectiveMetamodelReconciler = (EffectiveMetamodelReconciler) options.get(OPTION_EFFECTIVE_METAMODEL_RECONCILER);
			if (effectiveMetamodelReconciler != null) {
				if ((Boolean) options.get(OPTION_RECONCILE_EFFECTIVE_METAMODELS)) {
					effectiveMetamodelReconciler.reconcile();
				}
				actualObjectsToLoad = effectiveMetamodelReconciler.getActualObjectsToLoad();
				objectsAndRefNamesToVisit = effectiveMetamodelReconciler.getObjectsAndRefNamesToVisit();
				typesToLoad = effectiveMetamodelReconciler.getTypesToLoad();
			}
		}
		super.load(options);
	}
	
	
	public SmartSAXXMIResource(URI uri) {
		super(uri);
	}
	
	
	
	@Override
	protected XMLLoad createXMLLoad() {
		SmartSAXXMILoadImpl xmiLoadImpl = new SmartSAXXMILoadImpl(createXMLHelper());
		sxl = xmiLoadImpl;
		xmiLoadImpl.setLoadAllAttributes(loadAllAttributes);
		
		xmiLoadImpl.setObjectsAndRefNamesToVisit(objectsAndRefNamesToVisit);
		xmiLoadImpl.setActualObjectsToLoad(actualObjectsToLoad);
		xmiLoadImpl.setTypesToLoad(typesToLoad);
		return xmiLoadImpl; 
	}
	
	@Override
	protected XMLLoad createXMLLoad(Map<?, ?> options) {
		if (options != null && Boolean.TRUE.equals(options.get(OPTION_SUPPRESS_XMI)))
	    {
			SmartSAXXMILoadImpl xmiLoadImpl = new SmartSAXXMILoadImpl(new XMLHelperImpl(this));
			xmiLoadImpl.setLoadAllAttributes(loadAllAttributes);
			
			xmiLoadImpl.setObjectsAndRefNamesToVisit(objectsAndRefNamesToVisit);
			xmiLoadImpl.setActualObjectsToLoad(actualObjectsToLoad);
			xmiLoadImpl.setTypesToLoad(typesToLoad);
			return xmiLoadImpl;
	    }
	    else
	    {
	      return createXMLLoad();
	    }
	}
		
}
