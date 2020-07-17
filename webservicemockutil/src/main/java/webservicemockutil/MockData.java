package webservicemockutil;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class MockData {
	
	private static final Logger logger = LoggerFactory.getLogger(MockData.class);
	 
	private static String mockDataHome;
	private String mockServerName;
	private Map<String, Long> mappingFileTimestampMap = new HashMap<String, Long>();
	//mapping folderPath, request URL, response filename
	private Map<String, Map<String, String>> jsonMappingMap = new HashMap<String, Map<String, String>>();
	//default mappings aggregates mappings from all mapping files and maps each request url to a response
	//file under one of the "default" folders
	private Map<String, String> defaultMappingMap = new HashMap<String, String>();
	private List<String> alternateResponseFiles;
	private String entryPageUrl;
	
	public static MockData getMockData(String mockServerName,  String mockDataHome) {

		MockData mockData = new MockData();
		MockData.mockDataHome = mockDataHome;
		mockData.mockServerName = mockServerName;
		mockData.loadAllMappingFiles();
		mockData.generateDefaultMapping();
		mockData.loadAlternateResponseFiles();
		
		logger.debug("call printJsonMappingMap");
		
		mockData.printJsonMappingMap();
		
		return mockData;
	}

	public String getMockServerName() {
		return mockServerName;
	}
	
	public String findFilePath(String pathInfo, String flow, String scenario) {
	
		logger.debug("pathInfo:" + pathInfo + " flow:" + flow + " scenario:" + scenario);
		String jsonOrXmlFile;
		String responseFile;
		boolean fileExists;
		
		//user does not select flow/scenario
		if (null == flow) {
			jsonOrXmlFile = findMatchingFile(defaultMappingMap, pathInfo);
			responseFile = mockDataHome + "/" + mockServerName + "/" + jsonOrXmlFile;
			fileExists = new File(responseFile).isFile();
			logger.debug("responseFile:" + responseFile + " exists? " + fileExists);
			
			return responseFile;
		}
		
		//user does select flow/scenario
		jsonOrXmlFile =  findMatchingFile(jsonMappingMap.get(flow), pathInfo);
		responseFile = mockDataHome + "/" + mockServerName + "/" + flow + "/" + scenario + "/" + jsonOrXmlFile;
		fileExists = new File(responseFile).isFile();
		
		if (!fileExists) {
			//try find response file from the default scenario of this flow
			responseFile = mockDataHome + "/" + mockServerName + "/" + flow + "/default/" + jsonOrXmlFile;
			fileExists = new File(responseFile).isFile();
		}
		
		if (!fileExists) {
			//try find response file from the default scenario
			jsonOrXmlFile = findMatchingFile(defaultMappingMap, pathInfo);
			responseFile = mockDataHome + "/" + mockServerName + "/" + jsonOrXmlFile;
			fileExists = new File(responseFile).isFile();
		}

		logger.debug("responseFile:" + responseFile + " exists? " + fileExists);
		
		return responseFile;
	}
	
	private String findMatchingFile(Map<String, String> map, String pathInfo) {
		String matchingFile = map.get(pathInfo);
		
		if (null == matchingFile) {
			//try pattern matching
			boolean matched = false;
			
			for (String pattern: map.keySet()) {
				matched = Pattern.matches(pattern, pathInfo);
				
				if(matched) {
					matchingFile = map.get(pattern);
					break;
				}
			}
		}
		
		return matchingFile;
	}
	
	public List<String> getAlternateResponseFiles() {
		return alternateResponseFiles;
	}
	
	private void loadAllMappingFiles() {
		
		//entry-mapping.json
		File entryMappingFile = new File(mockDataHome + "/" + mockServerName + "/entry-mapping.json");
		
		ObjectMapper objectMapper = 
				new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS).configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		List<String> mappingFileFolders;
		EntryMapping entryMapping = null;
		
		try {
			entryMapping = objectMapper.readValue(entryMappingFile, EntryMapping.class);
		} catch (IOException e1) {
			e1.printStackTrace();
			throw new MockDataException("Error loading entry-mapping.json");
		}
		
		entryPageUrl = entryMapping.getEntryPageUrl();
		mappingFileFolders = entryMapping.getFlowNames();
		
		for (String folderPath : mappingFileFolders) {
			
			try {
				initMappings(folderPath);
			} catch (IOException | URISyntaxException e) {
				e.printStackTrace();
				throw new MockDataException("Error loading mapping file from folder " + folderPath);
			}
		}
	}
	
	private void generateDefaultMapping() {
		//"default" folder in each flow contains all response files of happy scenario.
		//response files in a specific scenario folder only overrides some of those from the "default" folder
		
		String path;
		Map<String, String> map;
		
		for (String folderPath : jsonMappingMap.keySet()) {
			map =  jsonMappingMap.get(folderPath);
			for (String pathInfo : map.keySet()) {
				path = folderPath + "/default/" + map.get(pathInfo);
				if (defaultMappingMap.containsKey(pathInfo)) {
					logger.debug(pathInfo + " alrady exists and is mapped to " + defaultMappingMap.get(pathInfo));
				}
				defaultMappingMap.put(pathInfo, path);
			}
		}
	}
	
	private void loadAlternateResponseFiles() {
		//alternateResponseFiles.json contains the array of the request pathInfo. Their corresponding response files have a second version,
		//to be served alternately.
		File mappingFile = new File(mockDataHome + "/" + mockServerName + "/alternateResponseFiles.json");
		
		ObjectMapper objectMapper = 
				new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS).configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		
		try {
			alternateResponseFiles = objectMapper.readValue(mappingFile, ArrayList.class);
		} catch (IOException e1) {
			logger.error("alternateResponseFiles.json is missing");
			alternateResponseFiles = new ArrayList<String>();
		}
	}
	
	private String getMockServerRelativePath() {
		return mockServerName + "/";
	}
	
	private String getMockServerAbsolutePath() {
		return "/" + getMockServerRelativePath();
	}
	
	private void initMappings(String folderPath) throws JsonProcessingException, IOException, URISyntaxException {
		
		//mapping file is always foldername-mapping.json
		String foldername = folderPath.substring(folderPath.lastIndexOf("/") + 1);
		File mappingFile = new File(mockDataHome + "/" + mockServerName + "/" + folderPath + "/" + foldername + "-mapping.json");
		Long timestamp = mappingFileTimestampMap.get(folderPath);
		long lastModified = mappingFile.lastModified();
		
		if (null == timestamp || timestamp < lastModified) {
			mappingFileTimestampMap.put(folderPath, lastModified);
			ObjectMapper objectMapper = 
					new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS).configure(JsonParser.Feature.ALLOW_COMMENTS, true);
			Map<String, String> mapping = objectMapper.readValue(mappingFile, LinkedHashMap.class);
			jsonMappingMap.put(folderPath, mapping);
		}
		
	}
	
	public Map<String, List<String>> loadFlowScenarios() {
		
		File mappingFile = new File(mockDataHome + "/" + mockServerName + "/flow-scenarios.json");
		
		ObjectMapper objectMapper = new ObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
				.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		
		Map<String, List<String>> flowScenariosMap = null;
		
		try {
			flowScenariosMap = objectMapper.readValue(mappingFile, LinkedHashMap.class);
		} catch (IOException e) {
			e.printStackTrace();
			throw new MockDataException("Err loading flow scenarios.");
		}
		
		return flowScenariosMap;
	}

	public String getEntryPageUrl() {
		return entryPageUrl;
	}
	
	private void printJsonMappingMap() {
		
		logger.debug("jsonMappingMap:" + jsonMappingMap);
		
		ObjectMapper mapper = new ObjectMapper();
		try {
			String indented = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMappingMap);
			logger.debug("jsonMappingMap:");
			logger.debug(indented);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
