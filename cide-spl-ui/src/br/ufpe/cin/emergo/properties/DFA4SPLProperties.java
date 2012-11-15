package br.ufpe.cin.emergo.properties;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

import br.ufal.cideei.Activator;

public class DFA4SPLProperties {

	public static final QualifiedName ALLOY_FILE_PROPKEY = new QualifiedName(Activator.PLUGIN_ID, "alloyfile");
	private static final String DEFAULT_ALLOY_FILE = "";
	
	private static final QualifiedName MAIN_CLASS_PROPKEY = new QualifiedName(Activator.PLUGIN_ID, "mainclass");;
	private static final String DEFAULT_MAIN_CLASS = "";
	
	public static String getAlloyFilePath(IResource resource) {
		try {
			String value = resource.getPersistentProperty(ALLOY_FILE_PROPKEY);
			if (value == null){
				return DEFAULT_ALLOY_FILE;
			}
			return value;
		} catch (CoreException e) {
			e.printStackTrace();
			return DEFAULT_ALLOY_FILE;
		}
	}

	public static void setAlloyFilePath(IResource resource, String alloyFilePath) {
		try {
			resource.setPersistentProperty(DFA4SPLProperties.ALLOY_FILE_PROPKEY, "" + alloyFilePath);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public static String getMainClass(IResource resource) {
		try {
			String value = resource.getPersistentProperty(MAIN_CLASS_PROPKEY);
			if (value == null){
				return DEFAULT_MAIN_CLASS;
			}
			return value;
		} catch (CoreException e) {
			e.printStackTrace();
			return DEFAULT_MAIN_CLASS;
		}
	}

	public static void setMainClass(IResource resource, String mainClass) {
		try {
			resource.setPersistentProperty(DFA4SPLProperties.MAIN_CLASS_PROPKEY, "" + mainClass);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

}