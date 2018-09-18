package sk.intersoft.vicinity.semptests;

import netscape.javascript.JSObject;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sk.intersoft.vicinity.agent.JsonCompare;
import sk.intersoft.vicinity.agent.thing.ThingDescription;
import sk.intersoft.vicinity.agent.thing.ThingValidator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static java.awt.SystemColor.info;
import static java.nio.file.StandardOpenOption.*;

public class SemanticPlatformTests {
    private static String agentID = "f4734225-25af-4337-905d-179fbc41ea8e";
    private static String adapterConfig = "td-sample.json";
    private static String adapterPort = "8040";
    private static String outFileAdapter = "itemsFromAdapter.json";
    private static String outFileNM = "itemsFromNM.json";


    public static void main(String [ ] args) {
        if (args.length == 5) {
            agentID = args[0];
            adapterConfig = args[1];
            adapterPort = args[2];
            outFileAdapter = args[3];
            outFileNM = args[4];
        }

        //TODO: generovat objects_TD podla slovnikov pre property a actions & events
        /* ak viac adapterov - musia byt rozne porty */
        Application adapter = new Application();
        adapter.objects_TD = adapterConfig;
        adapter.port = adapterPort;
        Thread t1 = new Thread(adapter);
        t1.start();
        System.out.println("Adapter started!");
        //just wait a little bit for adapter to start
        try {
            Thread.sleep(10000);
        } catch (Exception ex) {}
        //get data from the Adapter and save it int outFileAdapter
        try {
            AdapterClient adapterClient = new AdapterClient(adapterPort);
            JSONObject itemsFromAdapter = adapterClient.getObjects();
            System.out.println(itemsFromAdapter.toString());

            JSONArray thingDescriptions = itemsFromAdapter.getJSONArray("thing-descriptions");
            List<JSONObject> tdList = new ArrayList<JSONObject>();
            for (int i = 0; i < thingDescriptions.length(); i++) {
                tdList.add(thingDescriptions.getJSONObject(i));
            }
            Collections.sort( tdList, new JsonCompare());
            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(
                            Files.newOutputStream(Paths.get(outFileAdapter), CREATE, TRUNCATE_EXISTING, WRITE)));
            saveListOfTDs(tdList, out);
            out.close();
        } catch (Exception e) {
            System.out.println("Error by processing the response from Adapter: " + e.getMessage());
            System.exit(102);
        }
        System.out.println("Reponse from Adapter processed!");




        //start the agent
        ProcessBuilder builder = new ProcessBuilder();
        builder.command("sh", "-c", System.getProperty("user.home")+"/vicinity/agent/agent.sh");
        builder.directory(new File(System.getProperty("user.home")+"/vicinity/agent/"));
        //builder.redirectOutput(new File(System.getProperty("user.home")+"/vicinity/out.txt"));
        //builder.redirectError(new File(System.getProperty("user.home")+"/vicinity/err.txt"));
        try {
            Process process = builder.start();
            process.waitFor();
        } catch (Exception e) {
            System.out.println("Error by starting the agent: " + e.getMessage());
            System.exit(100);
        }
        //just wait a little bit for agent to start
        try {
            Thread.sleep(50000);
        } catch (Exception ex) {}
        System.out.println("Agent started!");


        //get data from Network Manager and save it int outFileNM
        try {
            NMclient nmClient = new NMclient();
            JSONObject itemsFromNM = nmClient.getAgentItems(agentID);
            System.out.println(itemsFromNM.toString());

            JSONArray message = itemsFromNM.getJSONArray("message");
            List<JSONObject> tdList = new ArrayList<JSONObject>();
            for (int i = 0; i < message.length(); i++) {
                tdList.add(message.getJSONObject(i).getJSONObject("id").getJSONObject("info"));
            }
            Collections.sort( tdList, new JsonCompare());
            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(
                            Files.newOutputStream(Paths.get(outFileNM), CREATE, TRUNCATE_EXISTING, WRITE)));
            saveListOfTDs(tdList, out);
            out.close();
        } catch (Exception e) {
            System.out.println("Error by processing the response from NM: " + e.getMessage());
            System.exit(101);
        }
        System.out.println("Reponse from NM processed!");



        //TODO: porovnat vysledky
        boolean result = false;
        try {
            result = FileUtils.contentEquals(new File(outFileAdapter), new File(outFileNM));
        } catch (Exception e) {
            System.out.println("Error by comparing the results of adapter and NM: " + e.getMessage());
            result = false;
        }
        System.out.println(result?"success":"failed");
        //PrepareTDs prepare = new PrepareTDs();
        //prepare.prepareN(1, "./adapter1-nm.txt");


        //stop the agent
        try {
            builder.command("sh", "-c", System.getProperty("user.home")+"/vicinity/agent/agent.sh stop");
            Process process = builder.start();
        } catch (Exception e) {
            System.out.println("Error by stopping the agent: " + e.getMessage());
            System.exit(-1);
        }
        System.out.println("Agent stopped!");

        //stop the adapter
        try {
            t1.stop();
        } catch (Exception e) {
            System.out.println("Error by stopping the adapter: " + e.getMessage());
            System.exit(-1);
        }
        System.out.println("Adapter stopped!");

        //just wait a little bit before end the test
        try {
            Thread.sleep(10000);
        } catch (Exception ex) {}

        System.exit(0);
    }

    private static void saveListOfTDs(List<JSONObject> tdList, BufferedWriter out) throws Exception {
        for (JSONObject item : tdList) {
            ThingDescription td = ThingDescription.create(item,
                    new ThingValidator(false));
            if (td != null) {
                out.write(td.toString(3));
                out.flush();
            }
        }
    }


}
