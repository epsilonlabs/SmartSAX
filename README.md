# SmartSAX

SmartSAX aims at partial loading XMI-based EMF models to reduce the time and memory consumption for conventional EMF model loader


For Demonstration, please go through the code in the Demonstration class.
The static method generateEffectiveMetamodel() generates the effective metamodel needed to compute the GraBaTs 2009 query. 
After the effective metamodel is generated, put them in an list, like below:

```java

	ArrayList<EffectiveMetamodel> effectiveMetamodels = new ArrayList<EffectiveMetamodel>();
	EffectiveMetamodel effectiveMetamodel = Demonstration.generateEffectiveMetamodel();
	effectiveMetamodels.add(effectiveMetamodel);
```

Then follow the conventional EMF process for registering metamodels, like below
```java
	
	ResourceSet resourceSet = new ResourceSetImpl();
	
	ResourceSet ecoreResourceSet = new ResourceSetImpl();
	ecoreResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
		.put("*", new XMIResourceFactoryImpl());
	Resource ecoreResource = ecoreResourceSet.
		createResource(URI.createFileURI(new File("model/JDTAST.ecore").getAbsolutePath()));
	ecoreResource.load(null);
	for (EObject o : ecoreResource.getContents()) {
		EPackage ePackage = (EPackage) o;
		resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
	}
```

Then follow the conventional EMF process for loading models, only difference is, create a SmartSAXResrouceFactory, like below
```java
	resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().
		put("xmi", new SmartSAXModelResourceFactory());
	Resource resource = resourceSet.
		createResource(URI.createFileURI(new File("model/set0.xmi").getAbsolutePath()));
```

To partial load, construct the loadOptions, in which, put the effective metamodels, and the option for loading all attributes for types, like below
```java
	Map<String, Object> loadOptions = new HashMap<String, Object>();
	loadOptions.put(SmartSAXXMIResource.OPTION_EFFECTIVE_METAMODELS, effectiveMetamodels);
	loadOptions.put(SmartSAXXMIResource.OPTION_LOAD_ALL_ATTRIBUTES, false);
```

Then call load(), SmartSAX will partial load with respect to the effective metamodels
```java

	resource.load(loadOptions);
	for (EObject o : resource.getContents()) {
		System.out.println(o);
	}
```
