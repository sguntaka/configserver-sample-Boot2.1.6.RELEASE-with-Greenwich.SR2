# This sample not working when adding a new repo to the config file and reload (actuator/refresh)

## This sample using - Spring boot 2.1.6.RELEASE and Greenwich.SR2

### Follow below steps to test the scenario

1. Add a default config uri @ "spring.cloud.config.server.git.uri" in config/configrepos.txt file
2. Add a git connection (refer config/configrepos.txt file)
3. Run the application
4. Make a get call and make sure getting response (Sample url: http://localhost:8080/config-test1/dev/master)
5. Now manually add a new repo connection in the config/configrepos.txt file.
          {
            "name": "config-test3",
            "connection": {
              "uri": "https://github.com/sguntaka/config-test3.git",
              "force-pull": "true",
              "searchPaths": "config*,config/*"
            }
          }

6. Make below call which loads the newly added repo into context and perform /refresh call 
	curl -X POST \
  http://localhost:8080/reloadconfig \
  -d '{}'  
7. Make get call to the new repo and check receiving the response or not (Sample url: http://localhost:8080/config-test3/dev/master)

## Change pom.xml update Spring Boot version to 1.5.22.RELEASE and Spring cloud to Edgware.RELEASE
1. Uncomment line 31 and comment line 32 in ConfigServerApplication.java class
2. Repeat the above steps. You will see a proper response for step 7.

 
