package org.eclipse.epsilon.smartsax;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.epsilon.smartsax.effectivemetamodel.impl.EffectiveFeature;
import org.eclipse.epsilon.smartsax.effectivemetamodel.impl.EffectiveMetamodel;
import org.eclipse.epsilon.smartsax.effectivemetamodel.impl.EffectiveType;

public class EffectiveMetamodelReconciler {

	//effective metamodels
	protected ArrayList<EffectiveMetamodel> effectiveMetamodels = new ArrayList<EffectiveMetamodel>();
	
	//epackages
	protected ArrayList<EPackage> packages = new ArrayList<EPackage>();
	
	protected HashMap<String, HashMap<String, ArrayList<String>>> traversalPlans = new HashMap<String, HashMap<String,ArrayList<String>>>();
	protected HashMap<String, HashMap<String, ArrayList<String>>> actualObjectsAndFeaturesToLoad = new HashMap<String, HashMap<String,ArrayList<String>>>();
	protected HashMap<String, HashMap<String, ArrayList<String>>> typesToLoad = new HashMap<String, HashMap<String,ArrayList<String>>>();
	 
	protected HashMap<String, ArrayList<String>> placeHolderObjects = new HashMap<String, ArrayList<String>>();
	
	//visited EClasses
	protected ArrayList<EClass> visitedClasses = new ArrayList<EClass>();

	
	public ArrayList<EffectiveMetamodel> getEffectiveMetamodels() {
		return effectiveMetamodels;
	}
	
	public void addEffectiveMetamodel(EffectiveMetamodel effectiveMetamodel)
	{
		effectiveMetamodels.add(effectiveMetamodel);
	}
	
	public void addEffectiveMetamodels(ArrayList<EffectiveMetamodel> effectiveMetamodels)
	{
		this.effectiveMetamodels.addAll(effectiveMetamodels);
	}
	
	public void addPackage(EPackage ePackage)
	{
		packages.add(ePackage);
	}
	
	public void addPackages(Collection<?> packages)
	{
		this.packages.addAll((Collection<? extends EPackage>) packages);
	}
	
	public HashMap<String, HashMap<String, ArrayList<String>>> getObjectsAndRefNamesToVisit() {
		return traversalPlans;
	}
	
	public HashMap<String, HashMap<String, ArrayList<String>>> getActualObjectsToLoad() {
		return actualObjectsAndFeaturesToLoad;
	}
	
	public HashMap<String, HashMap<String, ArrayList<String>>> getTypesToLoad() {
		return typesToLoad;
	}
	
	public void reconcile()
	{
		//for each epackage, add to 'actualObjectToLoad' considering 
		for(EPackage ePackage: packages)
		{
			//for each eclassifier
			for(EClassifier eClassifier: ePackage.getEClassifiers())
			{
				//if eclassifier is a eclass
				if (eClassifier instanceof EClass) {
					
					//if the class is an actual eclass to load, add to the map
					if (actualObjectToLoad(ePackage, (EClass) eClassifier)) {
						addActualObjectToLoad((EClass) eClassifier);
					}
					
					if (typesToLoad(ePackage, (EClass) eClassifier)) {
						addTypesToLoad((EClass) eClassifier);
					}
					
					//cast to eClass
					EClass eClass = (EClass) eClassifier;
					
					//clear visited class
					visitedClasses.clear();
					
					//visit EClass
					planTraversal(eClass);
				}
			}
		}
		
		for(EPackage ePackage: packages)
		{
			for(EClassifier eClassifier: ePackage.getEClassifiers())
			{
				if (eClassifier instanceof EClass) {
					EClass leClass = (EClass) eClassifier;
					if (actualObjectToLoad(ePackage, (EClass) eClassifier)) {
						
						for(EReference eReference: leClass.getEAllReferences())
						{
							if(actualObjectsAndFeaturesToLoad.get(ePackage.getName()).get(eClassifier.getName()).contains(eReference.getName()))
							{
								EClass eType = (EClass) eReference.getEType();
								addTypesToLoad(eType);
							}
						}
					}
					
					if (typesToLoad(ePackage, leClass)) {
						for(EReference eReference: leClass.getEAllReferences())
						{
							if(typesToLoad.get(ePackage.getName()).get(eClassifier.getName()).contains(eReference.getName()))
							{
								EClass eType = (EClass) eReference.getEType();
								addTypesToLoad(eType);
							}
						}

					}
				}
			}
		}
		
		for(EffectiveMetamodel em: effectiveMetamodels)
		{
			for(EffectiveType et: em.getAllOfKind())
			{
				ArrayList<String> features = actualObjectsAndFeaturesToLoad.get(em.getName()).get(et.getName());
				for(EffectiveFeature ef: et.getAttributes())
				{
					if (!features.contains(ef.getName())) {
						features.add(ef.getName());
					}
				}
				for(EffectiveFeature ef: et.getReferences())
				{
					if (!features.contains(ef.getName())) {
						features.add(ef.getName());
					}
				}
			}
			for(EffectiveType et: em.getAllOfType())
			{
				ArrayList<String> features = actualObjectsAndFeaturesToLoad.get(em.getName()).get(et.getName());
				for(EffectiveFeature ef: et.getAttributes())
				{
					if (!features.contains(ef.getName())) {
						features.add(ef.getName());
					}
				}
				for(EffectiveFeature ef: et.getReferences())
				{
					if (!features.contains(ef.getName())) {
						features.add(ef.getName());
					}
				}
			}
			for(EffectiveType et: em.getTypes())
			{
				ArrayList<String> features = typesToLoad.get(em.getName()).get(et.getName());
				for(EffectiveFeature ef: et.getAttributes())
				{
					if (!features.contains(ef.getName())) {
						features.add(ef.getName());
					}
				}
				for(EffectiveFeature ef: et.getReferences())
				{
					if (!features.contains(ef.getName())) {
						features.add(ef.getName());
					}
				}
			}
		}
	}

	
	//returns true if the eclass is an actual object to load
	public boolean actualObjectToLoad(EPackage ePackage, EClass eClass)
	{
		//for each effective metamodel in the container
		for(EffectiveMetamodel em: effectiveMetamodels)
		{
			//if em's name is equal to epack's name
			if (em.getName().equalsIgnoreCase(ePackage.getName())) {
				
				//for each type in all of kind
				for(EffectiveType et: em.getAllOfKind())
				{
					//get the element name
					String elementName = et.getName();
					//if the element's name is equal to the eclass's name, return true
					if (elementName.equals(eClass.getName())) {
						return true;
					}
					
					//get the eclass by name
					EClass kind = (EClass) ePackage.getEClassifier(elementName);
					
					//if the eclass's super types contains the type also return true
					if(eClass.getESuperTypes().contains(kind))
					{
						return true;
					}
				}
				
				//for each type in all of type
				for(EffectiveType et: em.getAllOfType())
				{
					//get name
					String elementName = et.getName();
					//if name equals, return true
					if (elementName.equals(eClass.getName())) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public boolean typesToLoad(EPackage ePackage, EClass eClass)
	{
		//for each effective metamodel in the container
		for(EffectiveMetamodel em: effectiveMetamodels)
		{
			//if em's name is equal to epack's name
			if (em.getName().equalsIgnoreCase(ePackage.getName())) {
				
				//for each type in all of type
				for(EffectiveType et: em.getTypes())
				{
					//get name
					String elementName = et.getName();
					//if name equals, return true
					if (elementName.equals(eClass.getName())) {
						return true;
					}
					
					//get the eclass by name
					EClass kind = (EClass) ePackage.getEClassifier(elementName);
					
					//if the eclass's super types contains the type also return true
					if(eClass.getESuperTypes().contains(kind))
					{
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public void addActualObjectToLoad(EClass eClass)
	{
		//get the epackage name
		String epackage = eClass.getEPackage().getName();
		
		//get the submap with the epackage name
		HashMap<String, ArrayList<String>> subMap = actualObjectsAndFeaturesToLoad.get(epackage);
		
		//if sub map is null
		if (subMap == null) {
			
			//create new sub map
			subMap = new HashMap<String, ArrayList<String>>();
			
			//create new refs for the map
			ArrayList<String> refs = getFeaturesForClassToLoad(eClass);
			
			//add the ref to the sub map
			subMap.put(eClass.getName(), refs);
			
			//add the sub map to objectsAndRefNamesToVisit
			actualObjectsAndFeaturesToLoad.put(epackage, subMap);
		}
		else
		{
			//if sub map is not null, get the refs by class name
			ArrayList<String> refs = subMap.get(eClass.getName());

			//if refs is null, create new refs and add the ref and then add to sub map
			if (refs == null) {
				refs = getFeaturesForClassToLoad(eClass);
				subMap.put(eClass.getName(), refs);
			}
		}
	}
	
	public void addTypesToLoad(EClass eClass)
	{
		//get the epackage name
		String epackage = eClass.getEPackage().getName();
		
		//get the submap with the epackage name
		HashMap<String, ArrayList<String>> subMap = typesToLoad.get(epackage);
		
		//if sub map is null
		if (subMap == null) {
			
			//create new sub map
			subMap = new HashMap<String, ArrayList<String>>();
			
			//create new refs for the map
			ArrayList<String> refs = getFeaturesForTypeToLoad(eClass);
			
			//add the ref to the sub map
			subMap.put(eClass.getName(), refs);
			
			//add the sub map to objectsAndRefNamesToVisit
			typesToLoad.put(epackage, subMap);
		}
		else
		{
			//if sub map is not null, get the refs by class name
			ArrayList<String> refs = subMap.get(eClass.getName());

			//if refs is null, create new refs and add the ref and then add to sub map
			if (refs == null) {
				refs = getFeaturesForTypeToLoad(eClass);
				subMap.put(eClass.getName(), refs);
			}
		}
	}
	
	public ArrayList<String> getFeaturesForTypeToLoad(EClass eClass)
	{
		//get the package
		EPackage ePackage = eClass.getEPackage();
		//prepare the result
		ArrayList<String> result = new ArrayList<String>();
		
		//for all model containers
		for(EffectiveMetamodel em: effectiveMetamodels)
		{
			//if the container is the container needed
			if (em.getName().equals(ePackage.getName())) {
				for(EffectiveType et: em.getTypes())
				{
					//if class name equals, add all references and attributes
					if (eClass.getName().equals(et.getName())) {
						for(EffectiveFeature ef: et.getAttributes())
						{
							result.add(ef.getName());
						}
						for(EffectiveFeature ef: et.getReferences())
						{
							result.add(ef.getName());
						}
						//break loop2;
					}
					
					//if eclass is a sub class of the kind, add all attributes and references
					EClass kind = (EClass) ePackage.getEClassifier(et.getName());
					if (eClass.getEAllSuperTypes().contains(kind)) {
						for(EffectiveFeature ef: et.getAttributes())
						{
							result.add(ef.getName());
						}
						for(EffectiveFeature ef: et.getReferences())
						{
							result.add(ef.getName());
						}
						//break loop1;
					}
				}
			}
		}
		return result;
	}
	
	public ArrayList<String> getFeaturesForClassToLoad(EClass eClass)
	{
		//get the package
		EPackage ePackage = eClass.getEPackage();
		//prepare the result
		ArrayList<String> result = new ArrayList<String>();
		
		//for all model containers
		for(EffectiveMetamodel em: effectiveMetamodels)
		{
			//if the container is the container needed
			if (em.getName().equals(ePackage.getName())) {
				//for elements all of kind
				//loop1:
				for(EffectiveType et: em.getAllOfKind())
				{
					//if class name equals, add all attributes and references
					if (eClass.getName().equals(et.getName())) {
						for(EffectiveFeature ef: et.getAttributes())
						{
							result.add(ef.getName());
						}
						for(EffectiveFeature ef: et.getReferences())
						{
							result.add(ef.getName());
						}
						//break loop1;
					}
					
					//if eclass is a sub class of the kind, add all attributes and references
					EClass kind = (EClass) ePackage.getEClassifier(et.getName());
					if (eClass.getEAllSuperTypes().contains(kind)) {
						for(EffectiveFeature ef: et.getAttributes())
						{
							result.add(ef.getName());
						}
						for(EffectiveFeature ef: et.getReferences())
						{
							result.add(ef.getName());
						}
						//break loop1;
					}
				}
				
				//for elements all of type
				//loop2:
				for(EffectiveType et: em.getAllOfType())
				{
					//if class name equals, add all references and attributes
					if (eClass.getName().equals(et.getName())) {
						for(EffectiveFeature ef: et.getAttributes())
						{
							result.add(ef.getName());
						}
						for(EffectiveFeature ef: et.getReferences())
						{
							result.add(ef.getName());
						}
						//break loop2;
					}
				}
			}
		}
		return result;
	}

	public void planTraversal(EClass eClass)
	{
		//add this class to the visited
		visitedClasses.add(EcoreUtil.copy(eClass));
		
		//if this one is a live class, should addRef()
		if (liveClass(eClass.getEPackage(), eClass.getName())) {
			//add class to objectsAndRefNamesToVisit
			addToTraversalPlan(eClass, null);
			//add to placeholder if necessary
			insertPlaceHolderOjbects(eClass.getEPackage(), eClass);
		}
		
		//for each reference
		for(EReference eReference: eClass.getEAllReferences())
		{
			//if the etype of the reference has not been visited
			if (!visitedEClass((EClass) eReference.getEType())) {
				//visit the etype
				planTraversal((EClass) eReference.getEType());
			}
			
			//if is live reference
			if (liveReference(eReference)) {
				//add class and reference to objectsAndRefNamesToVisit
				addToTraversalPlan(eClass, eReference);
				//insert placeholder if necessary
				insertPlaceHolderOjbects(eClass.getEPackage(), eClass);
			}
		}
		
		//for every eclassifier
		for(EClassifier every: eClass.getEPackage().getEClassifiers())
		{
			if (every instanceof EClass) {
				EClass theClass = (EClass) every;
				
				if (theClass.getEAllSuperTypes().contains(eClass)) {
					for(EReference eReference: theClass.getEAllReferences())
					{
						if (!visitedEClass((EClass) eReference.getEType())) {
							planTraversal((EClass) eReference.getEType());
						}
						
						if (liveReference(eReference)) {
							addToTraversalPlan(theClass, eReference);
							insertPlaceHolderOjbects(theClass.getEPackage(), theClass);
						}
					}
				}
			}
		}
	}

	
	//determines if a class is live class, this is used to generate the traversal path
	public boolean liveClass(EPackage ePackage, String className)
	{
		//for each effective metamodel
		for(EffectiveMetamodel em: effectiveMetamodels)
		{
			//get the package first
			if (em.getName().equalsIgnoreCase(ePackage.getName())) {
				
				//for all of kinds
				for(EffectiveType et: em.getAllOfKind())
				{
					//the element name
					String elementName = et.getName();
					//if name equals return true
					if (className.equals(elementName)) {
						return true;
					}
					
					//get the eclass for the mec
					EClass kind = (EClass) ePackage.getEClassifier(elementName);
					//get the eclass for the current class under question
					EClass actual = (EClass) ePackage.getEClassifier(className);
					//if the current class under question is a sub class of the mec, should return true
					if(actual.getEAllSuperTypes().contains(kind))
					{
						return true;
					}
					//if the current class under question is a super class of the mec, should also return true
					if (kind.getEAllSuperTypes().contains(actual)) 
					{
						return true;
					}
				}
				
				for(EffectiveType et: em.getAllOfType())
				{
					//the element n ame
					String elementName = et.getName();
					//if name equals return true
					if (className.equals(elementName)) {
						return true;
					}
					
					//get the eclass for the mec
					EClass type = (EClass) ePackage.getEClassifier(elementName);
					//get the eclass for the class under question
					EClass actual = (EClass) ePackage.getEClassifier(className);
					//if the class under question is a super class of the mec, should return true
					if (type.getEAllSuperTypes() != null && type.getEAllSuperTypes().contains(actual)) 
					{
						return true;
					}
				}
				
				for(EffectiveType et: em.getTypes())
				{
					//the element n ame
					String elementName = et.getName();
					//if name equals return true
					if (className.equals(elementName)) {
						return true;
					}
					
					//get the eclass for the mec
					EClass type = (EClass) ePackage.getEClassifier(elementName);
					//get the eclass for the class under question
					EClass actual = (EClass) ePackage.getEClassifier(className);
					//if the class under question is a super class of the mec, should return true
					if (type.getEAllSuperTypes() != null && type.getEAllSuperTypes().contains(actual)) 
					{
						return true;
					}
				}
				
				ArrayList<String> placeHolders = placeHolderObjects.get(em.getName());
				
				if (placeHolders != null) {
					for(String t : placeHolderObjects.get(em.getName()))
					{
						if (t.equals(className)) {
							return true;
						}
						
						//get the eclass for the mec
						EClass type = (EClass) ePackage.getEClassifier(t);
						//get the eclass for the class under question
						EClass actual = (EClass) ePackage.getEClassifier(className);
						//if the class under question is a super class of the mec, should return true
						if (type.getEAllSuperTypes().contains(actual)) 
						{
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	//add classes and references to visit and fill up the objectsAndRefNamesToVisit map
	public void addToTraversalPlan(EClass eClass, EReference eReference)
	{
		//get the epackage name
		String epackage = eClass.getEPackage().getName();
		//get the submap with the epackage name
		HashMap<String, ArrayList<String>> subMap = traversalPlans.get(epackage);
		//if sub map is null
		if (subMap == null) {
			//create new sub map
			subMap = new HashMap<String, ArrayList<String>>();
			//create new refs for the map
			ArrayList<String> refs = new ArrayList<String>();
			//if eReference is not null
			if (eReference != null) {
				//add the eReference to the ref
				refs.add(eReference.getName());
			}
			//add the ref to the sub map
			subMap.put(eClass.getName(), refs);
			//add the sub map to objectsAndRefNamesToVisit
			traversalPlans.put(epackage, subMap);
		}
		else {
			//if sub map is not null, get the refs by class name
			ArrayList<String> refs = subMap.get(eClass.getName());

			//if refs is null, create new refs and add the ref and then add to sub map
			if (refs == null) {
				refs = new ArrayList<String>();
				if(eReference != null)
				{
					refs.add(eReference.getName());
				}
				subMap.put(eClass.getName(), refs);
			}
			//if ref is not null, add the ref
			else {
				if (eReference != null) {
					if (!refs.contains(eReference.getName())) {
						refs.add(eReference.getName());	
					}
				}
			}
		}
	}

	
	
	public void insertPlaceHolderOjbects(EPackage ePackage, EClass eClass)
	{
		//inserted 
		boolean inserted = false;
		//for each effective metamodel
		for(EffectiveMetamodel em: effectiveMetamodels)
		{
			//get the matching package
			if (em.getName().equals(ePackage.getName())) {
				
				//for each type in all of kind
				for(EffectiveType et: em.getAllOfKind())
				{
					//if types match, return
					if (et.getName().equals(eClass.getName())) {
						inserted = true;
						return;
					}
					
					//if types match return
					EClass kind = (EClass) ePackage.getEClassifier(et.getName());
					for(EClass superClass: eClass.getEAllSuperTypes())
					{
						if (kind.getName().equals(superClass.getName())) {
							inserted = true;
							return;
						}
					}
				}
				
				//for each type in all of type
				for(EffectiveType et: em.getAllOfType())
				{
					//if types match, return
					if (et.getName().equals(eClass.getName())) {
						inserted = true;
						return;
					}
				}
				
				ArrayList<String> placeHolders = placeHolderObjects.get(em.getName());
				
				if (placeHolders != null) {
					for(String t : placeHolderObjects.get(em.getName()))
					{
						if (t.equals(eClass.getName())) {
							inserted = true;
							return;
						}
					}
				}
				
				
				
				//if not inserted, add to types
				if (!inserted) {
					inserted = true;
					addPlaceHolderObject(ePackage.getName(), eClass.getName());
					return;
				}
			}
		}
		//if not inserted, create effective metamodel and add to types
		if (!inserted) {
			addPlaceHolderObject(ePackage.getName(), eClass.getName());
			EffectiveMetamodel newEffectiveMetamodel = new EffectiveMetamodel(ePackage.getName());
			effectiveMetamodels.add(newEffectiveMetamodel);
		}
	}
	
	public void addPlaceHolderObject(String ePackage, String type)
	{
		if (placeHolderObjects.containsKey(ePackage)) {
			ArrayList<String> metamodel = placeHolderObjects.get(ePackage);
			if (metamodel.contains(type)) {
				return;
			}
			else {
				metamodel.add(type);
			}
		}
		else {
			ArrayList<String> metamodel = new ArrayList<String>();
			metamodel.add(type);
			placeHolderObjects.put(ePackage, metamodel);
		}
	}

	//test to see if the class has been visited
	public boolean visitedEClass(EClass eClass)
	{
		for(EClass clazz: visitedClasses)
		{
			if (clazz.getName().equals(eClass.getName())) {
				return true;
			}
		}
		return false;
	}


	//returns true if the reference is live
	public boolean liveReference(EReference eReference)
	{
		//if reference is containment, we are not looking into non-containment references
		if(eReference.isContainment())
		{
			//get the etype
			EClassifier eClassifier = eReference.getEType();
			EClass etype = (EClass) eClassifier;
			
			//if etype is a live class, return true
			if (liveClass(etype.getEPackage(), etype.getName())) {
				return true;
			}
			
			return false;
		}
		return false;
		
	}

}


