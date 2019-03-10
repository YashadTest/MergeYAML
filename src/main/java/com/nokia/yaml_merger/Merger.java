package com.nokia.yaml_merger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.scanner.ScannerException;

public class Merger {

	private final Yaml yaml = new Yaml();
	/*
	 * list of all the files which we want to merge
	 * */
	
	/*
	 * 
	 * Final YAML file where all the merged yaml data is dumped
	 * 
	 * */
	public  Map<String, Object> finalYAML;

	public Merger() {
		finalYAML = new LinkedHashMap<String, Object>();
	}
	/*
	 * Calling mergeRecursively to merge the files from child directory to the parent directory 
	 * sequentially where we can find the same file. 
	 *
	 **/
	public void merge(List<String> listOfAllFiles) throws Exception {

		for (int i=0;i<listOfAllFiles.size();i++) {
			String file=listOfAllFiles.get(i);
			InputStream inputStream = new FileInputStream(new File(file));
			Map<String, Object> inputYAML = yaml.load(inputStream);
			mergeRecursively(this.finalYAML, inputYAML);  
		}
	}
	/*
	 * Recursion is used to merge nested YAML MAP/Dictionary data.
	 * Merges two files at a time. 
	 * Since YAML data is in the form of "key: value", whenever the value is Map/dictionary the same recursion 
	 * method is called to merge until we get value in simple form which can be merged directly like:( list/String/Float etc)
	 * 
	 * */

	public void mergeRecursively(Map<String, Object> parentDataMap, Map<String, Object> childDataMap) {

		if (childDataMap == null) 
			return;//Nothing to merge.
		Iterator it = childDataMap.entrySet().iterator();


		while (it.hasNext()) {
			Map.Entry<String, Object>  pair = (Map.Entry<String, Object>)it.next();
			String key= (String) pair.getKey();
			Object childValue=pair.getValue();
			Object parentValue = parentDataMap.get(key);
			if (parentValue != null) {
				if (childValue instanceof Map && parentValue instanceof Map) {
					/*The value is again a nested Map hence recursion */
					mergeRecursively((Map<String, Object>) parentValue, (Map<String, Object>)  childValue);

				}else if (childValue instanceof List && parentValue instanceof List) {
					/*Appending list values of child into parent */
					List<Object> parentList = (List<Object>) parentDataMap.get(key);
					parentList.addAll((List<Object>) childValue);
				}else if ( (childValue instanceof String && parentValue instanceof String)||
						(childValue instanceof Integer && parentValue instanceof Integer)||
						(childValue instanceof Float && parentValue instanceof Float)||
						(childValue instanceof Boolean && parentValue instanceof Boolean)||
						(childValue instanceof Double && parentValue instanceof Double)){
					/*Replacing the ParentValues of type String/Float/Integer by childValue*/
					parentDataMap.put(key, childValue);
				}else {
					/*If the key of parent child is same but the value is not then throwing exception*/
					throw new InputMismatchException("Key Value pair doesnt match.");
				}
			} else {
				/*Updating parent value if its null, by child data*/
				parentDataMap.put(key, childValue);
			}
		}
	}

	public ArrayList<String> extractAllFiles(String filePath) throws Exception, FileNotFoundException {
		ArrayList<String> listOfAllFiles= new ArrayList<String>();
		File file = new File(filePath);
		/*Basic Vaidations to check file existence and its type.*/
		if(!(file.exists()&&file.isFile())) {
			throw new FileNotFoundException ();

		}

		String extension = "";
		final String filename=file.getName();
		int i = filename.lastIndexOf('.');
		if (i > 0) {
			extension = filename.substring(i+1);
		}


		if(!(extension.equals("yml"))&&!(extension.equals("yaml"))) {
			throw new Exception();

		}   

		file= file.getParentFile();

		/*Loops iterate from the given path till the root*/
		while(file.toPath().getNameCount()!=0) {
			File[] matchingFiles = file.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.equals(filename);
				}
			});
			/*Loop breaks at the directory when it does not find the file*/
			if(matchingFiles.length==0) {
				break;
			}
			listOfAllFiles.add(0,matchingFiles[0].toString());
			file = file.getParentFile();
		}
		return listOfAllFiles; 
	}

	public int execute(String filePath) {
		try {
			  
			ArrayList<String> listOfAllFiles=this.extractAllFiles(filePath);
			if(listOfAllFiles==null||listOfAllFiles.size()<1) {
				return 500;
			}
			this.merge(listOfAllFiles);

			int saveResult=this.saveResult(System.getProperty("user.dir")+"/result/mergedFiles.yml",this.finalYAML);
			if(saveResult!=200) {
				return saveResult;
			}
		}catch (Exception e)
		{
			if(e instanceof  IOException)
			{
				System.err.println("ERROR: Unable to read file.I/O Eception."+e);
				return 501;
			}
			if(e instanceof  InputMismatchException)
			{
				System.err.println("ERROR: File can not be mergered."+e);
				return 502;
			}
			
			if(e instanceof  FileNotFoundException ) {
				System.err.println("Wrong Path.Or File does not exist.");
				return 404;
			}
			if(e instanceof ScannerException)
			{
				System.err.println("Invalid YAML file.");
				return 400;
			}

			System.out.println("ERROR:"+e);
			return 500;
		}

		return 200;

	}


	public int  saveResult(String resultPath,Map<String,Object> finalResult) {
		/*Writing output in another file(Not required but did it anyway to understand the result better) */
		try {
			Yaml yaml = new Yaml();
			String output = yaml.dump(finalResult);
			FileWriter writer = new FileWriter(resultPath);
			yaml.dump(output, writer);
			System.out.println(output);
		}catch (Exception e) {

			return 500;
		}
		return 200;

	}
}

