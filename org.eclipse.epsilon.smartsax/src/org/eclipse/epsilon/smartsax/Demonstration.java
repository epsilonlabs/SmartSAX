package org.eclipse.epsilon.smartsax;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.epsilon.smartsax.effectivemetamodel.impl.EffectiveMetamodel;

public class Demonstration {

	public static EffectiveMetamodel generateEffectiveMetamodel() {
		EffectiveMetamodel effectiveMetamodel = new EffectiveMetamodel("DOM");

		/*
		 * EffectiveType typeDeclaration =
		 * effectiveMetamodel.addToAllOfKind("TypeDeclaration");
		 */

		effectiveMetamodel.addToAllOfKind("TypeDeclaration");
		effectiveMetamodel.addReferenceToAllOfKind("TypeDeclaration", "bodyDeclarations");
		effectiveMetamodel.addReferenceToAllOfKind("TypeDeclaration", "name");

		effectiveMetamodel.addToAllOfKind("BodyDeclaration");
		effectiveMetamodel.addToAllOfKind("AbstractTypeDeclaration");
		effectiveMetamodel.addToAllOfKind("SimpleName");
		effectiveMetamodel.addAttributeToAllOfKind("SimpleName", "fullyQualifiedName");

		effectiveMetamodel.addToAllOfKind("MethodDeclaration");
		effectiveMetamodel.addReferenceToAllOfKind("MethodDeclaration", "modifiers");
		effectiveMetamodel.addReferenceToAllOfKind("MethodDeclaration", "returnType");

		effectiveMetamodel.addToAllOfKind("Type");
		effectiveMetamodel.addToAllOfKind("SimpleType");

		effectiveMetamodel.addReferenceToAllOfKind("SimpleType", "name");

		effectiveMetamodel.addToAllOfKind("Name");
		effectiveMetamodel.addAttributeToAllOfKind("Name", "fullyQualifiedName");

		effectiveMetamodel.addToAllOfKind("Modifier");
		effectiveMetamodel.addAttributeToAllOfKind("Modifier", "static");
		effectiveMetamodel.addAttributeToAllOfKind("Modifier", "public");

		return effectiveMetamodel;
	}

	public static void demo_1() throws IOException {
		ArrayList<EffectiveMetamodel> effectiveMetamodels = new ArrayList<EffectiveMetamodel>();
		EffectiveMetamodel effectiveMetamodel = Demonstration.generateEffectiveMetamodel();
		effectiveMetamodels.add(effectiveMetamodel);

		ResourceSet resourceSet = new ResourceSetImpl();

		ResourceSet ecoreResourceSet = new ResourceSetImpl();
		ecoreResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		Resource ecoreResource = ecoreResourceSet
				.createResource(URI.createFileURI(new File("model/JDTAST.ecore").getAbsolutePath()));
		ecoreResource.load(null);
		for (EObject o : ecoreResource.getContents()) {
			EPackage ePackage = (EPackage) o;
			resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
		}

		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new SmartSAXResourceFactory());
		Resource resource = resourceSet.createResource(URI.createFileURI(new File("model/set0.xmi").getAbsolutePath()));

		EffectiveMetamodelReconciler effectiveMetamodelReconciler = new EffectiveMetamodelReconciler();
		effectiveMetamodelReconciler.addPackages(resourceSet.getPackageRegistry().values());
		effectiveMetamodelReconciler.addEffectiveMetamodels(effectiveMetamodels);
		effectiveMetamodelReconciler.reconcile();

		Map<String, Object> loadOptions = new HashMap<String, Object>();
		loadOptions.put(SmartSAXXMIResource.OPTION_EFFECTIVE_METAMODEL_RECONCILER, effectiveMetamodelReconciler);
		loadOptions.put(SmartSAXXMIResource.OPTION_LOAD_ALL_ATTRIBUTES, false);
		loadOptions.put(SmartSAXXMIResource.OPTION_RECONCILE_EFFECTIVE_METAMODELS, true);
		resource.load(loadOptions);
		for (EObject o : resource.getContents()) {
			System.out.println(o);
		}

		System.out.println(resource.getContents().size());
	}

	public static void main(String[] args) throws Exception {

		Demonstration.demo_1();
	}
}
