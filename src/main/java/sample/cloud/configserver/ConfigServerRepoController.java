package sample.cloud.configserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class ConfigServerRepoController {

	@Autowired
    ConfigServerApplication configServerApplication;

   @RequestMapping(method = RequestMethod.POST, value = "/reloadconfig", consumes = "text/plain")
    public @ResponseBody
    String reloadConfig() {
    		System.out.println("Inside controller -> reload config method");
        return configServerApplication.reloadConfigRepo();
    }
}
