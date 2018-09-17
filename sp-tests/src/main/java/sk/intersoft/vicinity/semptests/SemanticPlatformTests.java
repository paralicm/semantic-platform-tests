package sk.intersoft.vicinity.semptests;

import netscape.javascript.JSObject;
import org.json.JSONArray;
import org.json.JSONObject;
import sk.intersoft.vicinity.agent.thing.ThingDescription;
import sk.intersoft.vicinity.agent.thing.ThingValidator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.awt.SystemColor.info;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class SemanticPlatformTests {
    private static String agentID = "f4734225-25af-4337-905d-179fbc41ea8e";
    private static String outFileNM = "itemsFromNM.json";
    private static String outFileAdapter = "itemsFromAdapter.json";
    private static String adapterConfig = "td-sample-1.json";

    public static void main(String [ ] args) {

        //TODO: generovat objects_TD podla slovnikov pre property a actions & events
        /* ak viac adapterov - musia byt rozne porty */
        Application adapter1 = new Application();
        adapter1.objects_TD = adapterConfig;
        Thread t1 = new Thread(adapter1);
        t1.start();
        System.out.println("Adapter started!");
        //just wait a little bit for adapter to start
        try {
            Thread.sleep(10000);
        } catch (Exception ex) {}
        //get data from the Adapter and save it int outFileAdapter
        try {
            AdapterClient adapterClient = new AdapterClient();
            JSONObject itemsFromAdapter = adapterClient.getObjects();
            System.out.println(itemsFromAdapter.toString());

            JSONArray thingDescriptions = itemsFromAdapter.getJSONArray("thing-descriptions");
            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(
                            Files.newOutputStream(Paths.get(outFileAdapter), CREATE)));
            for (Object item : thingDescriptions) {
                JSONObject jsonItem = (JSONObject) item;
                ThingDescription td = ThingDescription.create(jsonItem,
                        new ThingValidator(false));
                if (td != null) {
                    out.write(td.toString(3));
                    out.flush();
                }
            }
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
        System.out.println("Agent started!");

        //just wait a little bit for agent to start
        try {
            Thread.sleep(10000);
        } catch (Exception ex) {}


        //get data from Network Manager and save it int outFileNM
        try {
            NMclient nmClient = new NMclient();
            JSONObject itemsFromNM = nmClient.getAgentItems(agentID);
            System.out.println(itemsFromNM.toString());

            JSONArray message = itemsFromNM.getJSONArray("message"); //.getJSONObject(0).getJSONObject("id").getJSONObject("info");
            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(
                            Files.newOutputStream(Paths.get(outFileNM), CREATE)));
            for (Object item : message) {
                JSONObject jsonItem = (JSONObject) item;
                ThingDescription td = ThingDescription.create(jsonItem.getJSONObject("id").getJSONObject("info"),
                        new ThingValidator(false));
                if (td != null) {
                    out.write(td.toString(3));
                    out.flush();
                }
            }
            out.close();
        } catch (Exception e) {
            System.out.println("Error by processing the response from NM: " + e.getMessage());
            System.exit(101);
        }
        System.out.println("Reponse from NM processed!");



        //TODO: porovnat vysledky

        //PrepareTDs prepare = new PrepareTDs();
        boolean result = false; //prepare.prepareN(1, "./adapter1-nm.txt");
        System.out.println(result?"success":"failed");
        //assertThat(result);

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


}
