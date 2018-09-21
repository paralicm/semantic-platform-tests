/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.intersoft.vicinity.semptests;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import sk.intersoft.vicinity.semptests.controllers.AdapterController;
import sk.intersoft.vicinity.semptests.services.AdapterService;
import sk.intersoft.vicinity.semptests.services.impl.AdapterServiceImpl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

/**
 *
 * @author dzandes
 */

@SpringBootApplication
public class Application implements Runnable {

    public String objects_TD = "";
    public String port = "";
    /*
    public static void main(String[] args){

        SpringApplication.run(Application.class, args);

        RestTemplate restTemplate = new RestTemplate();


         //* Agent_URL:Agent_Port is where your multi tenant agent is running, e.g. 160.43.33.111:8888
         //*   , therefore, you have to adapt it for your case
         //*
         //* The POST during startup to the Agent below is needed when/if we do not have Auto-Discovery

        String agentUrl = "http://<Agent_URL>:<Agent_Port>/agent/objects";

        AdapterService service = new AdapterServiceImpl();

        Response resp_ = service.getThingsDescription();
        
        HttpEntity<String> bodyEntity = new HttpEntity<>(resp_.getMessage());
        
        URI uri = UriComponentsBuilder.fromHttpUrl(agentUrl)
                                .build()
                                .encode().toUri();
        
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, bodyEntity, String.class);

        System.out.println("Response status : " + response.getStatusCodeValue());

    }
    */

    public void run() {
        if (port.equals(""))
            port = "8030";
        SpringApplication app = new SpringApplication(Application.class);
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("server.port", port);
        props.put("td-file", this.objects_TD);
        app.setDefaultProperties(props);
        app.run(new String[]{});

        //SpringApplication.run(Application.class);
    }
}