package sk.intersoft.vicinity.semptests;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import sk.intersoft.vicinity.agent.JsonCompare;
import sk.intersoft.vicinity.agent.thing.ThingDescription;
import sk.intersoft.vicinity.agent.thing.ThingValidator;
import sk.intersoft.vicinity.semptests.controllers.AdapterController;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStreamWriter;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.nio.file.StandardOpenOption.*;

public class AutoDiscoveryFunctionalTest {
    private static final Logger LOG = Logger.getLogger(AutoDiscoveryFunctionalTest.class);

    private static String[] agentIDs = new String[]{
            "f4734225-25af-4337-905d-179fbc41ea8e",
            "2d131e4f-616c-43a4-a433-389d5abd04c7",
            "ed1cdc0c-9fff-4de1-b493-460a6ee3a285",
            "f9e6065b-e241-4da2-8a57-32a329d67676",
            "b07d74e1-0ef1-4eaf-ac95-6e66b99ad682",
            "9ce835cc-bdc4-418f-8ba0-cc4d3ef97813",
            "526a6e8f-2dfc-4e18-a7d8-7914f3da2a66",
            "c0a065f8-1c18-4392-aae8-a32ad5bf6a9e",
            "c1aa668e-38ff-405d-a1dd-fbc09168ec74",
            "1d2b9e77-ff16-445e-a317-96fb20557d1f"
    };
    private static String[] agentConfigs = new String[]{
            "td-sample-01.json",
            "td-sample-02.json",
            "td-sample-03.json",
            "td-sample-04.json",
            "td-sample-05.json",
            "td-sample-06.json",
            "td-sample-07.json",
            "td-sample-08.json",
            "td-sample-09.json",
            "td-sample-10.json"
    };
    private static int adapterPort = 8040;
    private static int numberOfAdapters = 10;


/*
    public static void main(String [ ] args) {
        long start = System.currentTimeMillis();

//        ArrayList<Thread> adapters = new ArrayList<Thread>();
//
//        //TODO: generovat objects_TD podla slovnikov pre property a actions & events
//        List<JSONObject> tdListA = new ArrayList<JSONObject>();
//        for (int i = 1; i <= numberOfAdapters; i++) {
//            Application adapter = new Application();
//            adapter.objects_TD = String.format("td-sample-%02d.json", i);
//            adapter.port = String.valueOf(adapterPort + i - 1);
//            Thread t1 = new Thread(adapter);
//            adapters.add(t1);
//            t1.start();
//            //just wait for adapter to start
//            try {
//                Thread.sleep(1000);
//            } catch (Exception ex) {
//            }
//            System.out.println(String.format("Adapter %02d started!", i));
//        }

        //start the empty agent
        ProcessBuilder builder3 = new ProcessBuilder();
        startAgent(builder3, "agent-config-empty.json");
        System.out.println("Agent started!");
        //just wait a little bit for agent to start
        try {
            Thread.sleep(100000);
        } catch (Exception ex) {
        }

        getAgentItemsFromNM(agentIDs[6], "itemsFromNM-empty.json");
        System.out.println(String.format("Reponse from NM processed!"));

        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;

        LOG.info(String.format("Test duration: %s", String.format("%02d:%02d:%02d.%d\n\n", timeElapsed / (3600 * 1000),
                timeElapsed / (60 * 1000) % 60, timeElapsed / 1000 % 60, timeElapsed % 1000)));
    }
*/


    public static void main(String[] args) {
        long start = System.currentTimeMillis();

        ArrayList<Thread> adapters = new ArrayList<Thread>();

        //TODO: generovat objects_TD podla slovnikov pre property a actions & events
        List<JSONObject> tdListA = new ArrayList<JSONObject>();
        for (int i = 0; i < agentConfigs.length; i++) {
            Application adapter = new Application();
            adapter.objects_TD = agentConfigs[i];
            adapter.port = String.valueOf(adapterPort + i);
            Thread t1 = new Thread(adapter);
            adapters.add(t1);
            t1.start();
            //just wait for adapter to start
            try {
                Thread.sleep(4000);
            } catch (Exception ex) {
            }
            System.out.println(String.format("Adapter with config from %s started!", agentConfigs[i]));
        }

        //get data from the Adapters and save it in the file
        for (int i = 0; i < numberOfAdapters; i++) {
            try {
                AdapterClient adapterClient = new AdapterClient(String.valueOf(adapterPort + i));
                JSONObject itemsFromAdapter = adapterClient.getObjects();
                //System.out.println(itemsFromAdapter.toString());
                JSONArray thingDescriptions = itemsFromAdapter.getJSONArray("thing-descriptions");

                for (int j = 0; j < thingDescriptions.length(); j++) {
                    tdListA.add(thingDescriptions.getJSONObject(j));
                }
            } catch (Exception e) {
                System.out.println("Error by processing the response from Adapter from config " +
                        agentConfigs[i] + ": " +e.getMessage());
                System.exit(102);
            }
            System.out.println(String.format("Reponse from Adapter with config %s processed!", agentConfigs[i]));
        }

        Collections.sort(tdListA, new JsonCompare());
        BufferedWriter outA = null;
        try {
            outA = new BufferedWriter(
                    new OutputStreamWriter(
                            Files.newOutputStream(
                                    Paths.get(String.format("itemsFromAdapters.json")),
                                    CREATE, TRUNCATE_EXISTING, WRITE)));
            saveListOfTDs(tdListA, outA);
            outA.close();
        } catch (Exception e) {
            System.out.println("Error by processing the response from adapters: " + e.getMessage());
            e.printStackTrace();
            System.exit(103);
        }


        //start the agent with all adapters
        RunAgent agentAllAdapters = new RunAgent("agent-config-10a.json");
        agentAllAdapters.start();
        System.out.println("Agent with all adapters started!");
        //just wait a little bit for agent to start
        try {
            Thread.sleep(220000);
        } catch (Exception ex) {
        }


        //get data from Network Manager and save it int outFileNM
        getAgentItemsFromNM(agentIDs[6], "itemsFromNM.json");
        System.out.println(String.format("Reponse from NM processed!"));


        //compare the results between adapters and NM
        boolean result = false;
        try {
            result = FileUtils.contentEquals(
                    new File(String.format("itemsFromAdapters.json")),
                    new File(String.format("itemsFromNM.json")));
        } catch (Exception e) {
            System.out.println(String.format("Error by comparing the results of %02d. adapter and NM: %s", 1, e.getMessage()));
            result = false;
        }
        System.out.println(String.format("Comparing the results of %02d. adapter and NM: %s.", 1, result ? "MATCH" : "DIFFER"));


        //stop the agent with all adapters
        agentAllAdapters.stop();
        System.out.println("Agent with all adapters stopped!");

        //stop the adapters
        for (Thread t : adapters) {
            try {
                t.stop();
            } catch (Exception e) {
                System.out.println(String.format("Error by stopping the adapter %d: %s!", t.getId(), e.getMessage()));
                System.exit(-1);
            }
            System.out.println(String.format("Adapter %d stopped!", t.getId()));
        }

        //start the empty agent
        RunAgent agentEmptyAdapter = new RunAgent("agent-config-empty.json");
        agentEmptyAdapter.start();
        System.out.println("Agent with empty adapter started!");
        //just wait a little bit for agent to start
        try {
            Thread.sleep(100000);
        } catch (Exception ex) {
        }

        //get data from Network Manager and save it int outFileNM
        //for (int i = 0; i < 1; i++  ) {
        getAgentItemsFromNM(agentIDs[6], "itemsFromNM-empty.json");
        System.out.println(String.format("Reponse from NM processed!"));

        //stop agent with empty adapter
        agentEmptyAdapter.stop();
        System.out.println("Agent with empty adapter stopped!");

        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;

        LOG.info(String.format("Test duration: %s", String.format("%02d:%02d:%02d.%d\n\n", timeElapsed / (3600 * 1000),
                timeElapsed / (60 * 1000) % 60, timeElapsed / 1000 % 60, timeElapsed % 1000)));

        System.exit(0);
    }

    private static void getAgentItemsFromNM(String agentID, String fileName) {
        try {
            NMclient nmClient = new NMclient();
            JSONObject itemsFromNM = nmClient.getAgentItems(agentID);

            JSONArray message = itemsFromNM.getJSONArray("message");
            List<JSONObject> tdList = new ArrayList<JSONObject>();
            for (int j = 0; j < message.length(); j++) {
                tdList.add(message.getJSONObject(j).getJSONObject("id").getJSONObject("info"));
            }
            Collections.sort(tdList, new JsonCompare());
            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(
                            Files.newOutputStream(
                                    Paths.get(fileName),
                                    CREATE, TRUNCATE_EXISTING, WRITE)));
            saveListOfTDs(tdList, out);
            out.close();
        } catch (Exception e) {
            System.out.println(String.format("Error by processing the response from NM %d: %s", 1, e.getMessage()));
            System.exit(101);
        }
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