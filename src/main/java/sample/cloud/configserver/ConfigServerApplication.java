package sample.cloud.configserver;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Properties;

@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication extends SpringBootServletInitializer {
    private static Class<ConfigServerApplication> applicationClass = ConfigServerApplication.class;
    
    @Autowired
    Environment env;
    
    //private static final String refreshurl = "http://localhost:8080/refresh";
	private static final String refreshurl = "http://localhost:8080/actuator/refresh";
    
    private static final String confileFilePath = "./config/configrepos.txt";

	public static void main(String[] args) {
        initConfiguration(new SpringApplicationBuilder()).build().run();
	}

	private static SpringApplicationBuilder initConfiguration(SpringApplicationBuilder applicationBuilder) {
		try {
			Properties prop = getConfigProperties();
			applicationBuilder.application().setDefaultProperties(prop);
		} catch (Exception ex) {
            ex.printStackTrace();
		}
		return applicationBuilder.sources(applicationClass);
	}

    //Read configuration from the file
	private static Properties getConfigProperties() {
        Properties prop = new Properties();
        try {
            JSONObject configObj = new JSONObject(new String ( Files.readAllBytes( Paths.get(confileFilePath) ) ));
            Iterator<?> keys = configObj.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                String value = configObj.getString(key);
                if (key.startsWith("common")) //Application related properties
                    System.setProperty(key, value);
                else {
                    if (key.equals("spring.cloud.config.server.git.repos")) {
                        String repos = configObj.get("spring.cloud.config.server.git.repos").toString();
                        JSONArray reposArray = new JSONArray(repos);
                        for (int i = 0; i < reposArray.length(); i++) {
                            JSONObject eachRepo = reposArray.getJSONObject(i);
                            JSONObject connectionObj = eachRepo.getJSONObject("connection");
                            Iterator<?> subKeys = connectionObj.keys();
                            while (subKeys.hasNext()) {
                                String subKey = (String) subKeys.next();
                                String subValue = connectionObj.getString(subKey);
                                prop.setProperty("spring.cloud.config.server.git.repos."
                                        + eachRepo.getString("name") + "." + subKey, subValue);
                            }
                        }
                    } else
                        prop.setProperty(key, value);
                }
            }
		} catch (Exception ex) {
            ex.printStackTrace();
		}
        return prop;
    } //getConfigProperties end

    /**
     * SpringApplicationBuilder configure(SpringApplicationBuilder application) will trigger to initialize the application settings
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder applicationBuilder) {
        return initConfiguration(applicationBuilder);
    }

    public String reloadConfigRepo() {
        String response = null;
        try {
                Properties prop = getConfigProperties();
                MutablePropertySources sources = ((ConfigurableEnvironment) env).getPropertySources();
                //applicationConfig,configServerClient,systemEnvironment,systemProperties,servletContextInitParams
                sources.replace("defaultProperties", new PropertiesPropertySource("defaultProperties", prop));
                response = "Reloaded config successfully";
                //can add refresh call from here to load new repo right away to spring context
                refreshAllRepos();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return response;
    } //reloadConfigRepo end

    //refresh all repos
    public ResponseEntity<String> refreshAllRepos() throws Exception {
        ResponseEntity<String> respEntity = null;
        try {
            HttpHeaders htppHeaders = new HttpHeaders();
            htppHeaders.setContentType(MediaType.APPLICATION_JSON);
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setReadTimeout((60 * 1000 * 3) + 5000);

            RestTemplate restTemplate = new RestTemplate(requestFactory);
            respEntity = restTemplate.exchange(refreshurl, HttpMethod.POST,new HttpEntity<String>(htppHeaders), String.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return respEntity;
    } 
}
