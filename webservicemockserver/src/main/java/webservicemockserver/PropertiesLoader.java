package webservicemockserver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {
	
	/**
	 * Loads properties from the classpath, based the specified resourceFileName.
	 * 
	 * @param resourceFileName
	 * @return
	 * @throws IOException
	 */
	public static Properties loadProperties(String resourceFileName) {
        
		Properties configuration = new Properties();
		 InputStream inputStream = null;
		 
		try {
			
	        inputStream = PropertiesLoader.class
	          .getClassLoader()
	          .getResourceAsStream(resourceFileName);
        
	        configuration.load(inputStream);
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Cannot load resource file " + resourceFileName);
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return configuration;
    }
}
