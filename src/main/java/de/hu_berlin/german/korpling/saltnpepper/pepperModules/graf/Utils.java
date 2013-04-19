/** This file contains helper methods not directly related to 
 *  MASC, GrAF, SALT or ANNIS. */

package de.hu_berlin.german.korpling.saltnpepper.pepperModules.graf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//import org.yaml.snakeyaml.Yaml;

/* @author Arne Neumann */
public class Utils {

//	/** prints the current working directory */
//	public static void printWorkingDir() throws IOException {
//		  java.io.File f = new java.io.File(".");
//		  System.out.println(f.getCanonicalPath());
//	}
//	
//	/** converts Long numbers into Integers, if possible. 
//	 *  source: http://stackoverflow.com/a/1590842 */
//	public static int safeLongToInt(long l) {
//	    if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
//	        throw new IllegalArgumentException
//	            (l + " cannot be cast to int without changing its value.");
//	    }
//	    return (int) l;
//	}	
//	
//	public static List<String> recursiveListDir(String path){
//		  List<String> fnamesList = new ArrayList<String>();	   
//		  File[] faFiles = new File(path).listFiles();
//		  
//		  for(File file: faFiles){
//		    if(file.isDirectory()){
//		    	List<String> tmpFnames = recursiveListDir(file.getAbsolutePath());
//		    	for (String fname: tmpFnames) {
//		    		fnamesList.add(fname);
//		    	}
//		    }		
//		    else {
//		      String absPath = file.getAbsolutePath();
//		      String correctedPath = absPath.replace("\\", "/"); // Really necessary?
//		      fnamesList.add(correctedPath);
//		    }
//		  }
//		  return fnamesList;
//	}
//	
//	public static List<String> recursiveListDir(String path, String fileNameEndsWith){
//		List<String> fnamesList = recursiveListDir(path);
//		List<String> filteredFnamesList = new ArrayList<String>();
//		for (String fname : fnamesList) {
//			if (fname.endsWith(fileNameEndsWith)) {
//				filteredFnamesList.add(fname);
//			}
//		}
//		return filteredFnamesList;
//	}	
//
//	public static void writeStackTraceToFile(Exception e, String fileName) 
//    			  throws IOException {
//		String logfileDir = Utils.getVariableFromYamlFile("logfileDir");
//		File logfile = new File(logfileDir, fileName);
//		FileWriter fw = new FileWriter(logfile, true);
//		PrintWriter pw = new PrintWriter(fw);
//		e.printStackTrace(pw);
//		pw.close();
//		fw.close();		
//	}
//	
//	/** returns a variable from the specified YAML file */
//	public static String getVariableFromYamlFile(String pathToYamlFile, String variable) 
//				  throws FileNotFoundException {
//		InputStream yamlStream = new FileInputStream(new File(pathToYamlFile));
//		Yaml yaml = new Yaml();
//		Map<String, String> data = (Map<String, String>) yaml.load(yamlStream);
//		return data.get(variable);
//	}
//
//	/** returns a variable from the default YAML file, i.e. config.yaml */
//	public static String getVariableFromYamlFile(String variable) 
//	  throws FileNotFoundException {
//		return getVariableFromYamlFile("config.yaml", variable);
//	}	
//	
//
//	/** given an object, the method returns the name of the interface class 
//	 *  that is implemented by the object's class. */
//	public static String getInterfaceClassName(Object object) {
//		Class<?> interfaceClass = object.getClass().getInterfaces()[0];
//		return interfaceClass.getSimpleName();
//	}
}
