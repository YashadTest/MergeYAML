package com.nokia.yaml_merger;
import static org.junit.Assert.assertEquals;
import org.junit.*;
import java.util.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.FilenameUtils;

import org.yaml.snakeyaml.Yaml;


public class AppTest extends Merger  {


	private Merger mergerObj; 
	private Yaml yaml;
	@Before 
	public void initialize() {
		mergerObj= new Merger();
		yaml = new Yaml();

	}
	
	/*TEST METHOD: Merger.merge()*/
	/*
	 * TEST 1: If the list of files to merge is Null
	 * 
	 * */
	@Test(expected = Exception.class)
	public void test_mergeMethod_whenExceptionThrown() throws Exception {
		ArrayList<String> testList= null;
		this.mergerObj.merge(testList);
	}
	
	/*
	 * TEST 2 : If there occur any exception while reading the YAML file(error while loading the YAML), 
	 * to invoke this test this the wrong path is given )
	 * */
	@Test(expected = Exception.class)
	public void test_mergeMethod_whenExceptionThrown2() throws Exception {
		ArrayList<String> testList= new ArrayList<String>();
		testList.add("/some/wrong/path");
		this.mergerObj.merge(testList);
	}
	
	/*
	 * TEST 3 : When one YAML file is invalid
	 * It will throw an exception of type ScannerException but as of now all 
	 * types of exception are handled by Exception class
	 * */
	@Test(expected = Exception.class)
	public void test_mergeMethod_whenExceptionThrown3() throws Exception {
		ArrayList<String> testList= new ArrayList<String>();
		testList.add(FilenameUtils.separatorsToSystem(System.getProperty("user.dir")+"/testData/invalidYaml.yml"));
		testList.add(FilenameUtils.separatorsToSystem(System.getProperty("user.dir")+"/testData/testYaml1.yml"));
		this.mergerObj.merge(testList);
	}

	/*
	 * 
	 * TEST 4: When the YAML files have same key but different value types, 
	 * hence will throw InputMismatchException
	 * 
	 * */
	@Test (expected = InputMismatchException.class)
	public void test_mergeRecursively_whenExceptionThrown() throws InputMismatchException, FileNotFoundException  {
		InputStream inputStream = new FileInputStream(FilenameUtils.separatorsToSystem(System.getProperty("user.dir")+"/testData/testYaml2.yml"));
		Map<String, Object> test1 = yaml.load(inputStream);
		InputStream inputStream2 = new FileInputStream(FilenameUtils.separatorsToSystem(System.getProperty("user.dir")+"/testData/testYamlWithMisMatchInput.yml"));
		Map<String, Object> test2 = yaml.load(inputStream2);
		this.mergerObj.mergeRecursively(test1,test2);
	}
	
	/*
	 * TEST 5: This is postive test case when the merger successfully able to merge two YAML file.
	 * 
	 * */
	@Test 
	public void test_mergeRecursively_happyPath() throws IOException   {
		InputStream inputStream = new FileInputStream(FilenameUtils.separatorsToSystem(System.getProperty("user.dir")+"/testData/testYaml1.yml"));
		Map<String, Object> test1 = yaml.load(inputStream);
		inputStream.close();
		InputStream inputStream2 = new FileInputStream(FilenameUtils.separatorsToSystem(System.getProperty("user.dir")+"/testData/testYaml2.yml"));
		Map<String, Object> test2 = yaml.load(inputStream2);
		inputStream2.close();
		this.mergerObj.mergeRecursively(test1, test2);
		String output = yaml.dump(test1);
		//this.mergerObj.saveResult(System.getProperty("user.dir")+"/testData/expectedMergedYAML.yml", test1);
		InputStream inputStream3 = new FileInputStream(FilenameUtils.separatorsToSystem(System.getProperty("user.dir")+"/testData/expectedMergedYAML.yml"));
		Map<String, Object> expected = yaml.load(inputStream3);
		inputStream3.close();

		String expectedOutput=yaml.dump(expected);
		assertEquals(true,(output.equals(expectedOutput)));
	}

	/*TEST 6: When the wrong file path is given or file does not exist*/
	@Test (expected = FileNotFoundException.class)
	public void test_extractingFiles_fileNotFound() throws  Exception {
		this.mergerObj.extractAllFiles("/wrong/path/test.yml");
	}
	
	/*TEST 7: When the file type is not YAML/yml*/
	@Test(expected = Exception.class)
	public void test_extractingFiles_InvalidFileFormat() throws Exception {
		this.mergerObj.extractAllFiles(FilenameUtils.separatorsToSystem(System.getProperty("user.dir")+"/testData/dir4/textFile.txt"));
	} 

	/*TEST 8: Positive test case to extract all the files */
	@Test
	public void test_extractingFiles_HappyPath() throws Exception {

		String testPath=FilenameUtils.separatorsToSystem(System.getProperty("user.dir")+"/testData/dir4/dir3/dir2/dir1/findYAML.yml");
		ArrayList<String> outputlistOfAllFiles= this.mergerObj.extractAllFiles(testPath);
		ArrayList<String> expectedlistOfAllFiles= new ArrayList<String>();
		String path1=FilenameUtils.separatorsToSystem(System.getProperty("user.dir")+"/testData/dir4/dir3/dir2/dir1/findYAML.yml");
		String path2=FilenameUtils.separatorsToSystem(System.getProperty("user.dir")+"/testData/dir4/dir3/dir2/findYAML.yml");
		expectedlistOfAllFiles.add(path2);
		expectedlistOfAllFiles.add(path1);
		assertEquals(true, (expectedlistOfAllFiles.get(0).equals(outputlistOfAllFiles.get(0))&&
				expectedlistOfAllFiles.get(1).equals(outputlistOfAllFiles.get(1)))&&
				expectedlistOfAllFiles.size()==outputlistOfAllFiles.size());
	} 

}
