package sk.intersoft.vicinity.semptests;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
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

import static java.nio.file.StandardOpenOption.*;

public class AutoDiscoveryFunctionalTest {
    private static final Logger LOG = Logger.getLogger(AutoDiscoveryFunctionalTest.class);

    private static String[] agentIDs = new String[]{
            "25d95474-7ddf-40f8-8f52-f4ecd3751aa0",
            "c1aa668e-38ff-405d-a1dd-fbc09168ec74",
            "1d2b9e77-ff16-445e-a317-96fb20557d1f"
    };
    private static String[] agentConfigs = new String[]{
//            "td-sample.json"
            "td-sample-01.json" /*,
            "td-sample-02.json",
            "td-sample-03.json",
            "td-sample-04.json",
            "td-sample-05.json",
            "td-sample-06.json",
            "td-sample-07.json",
            "td-sample-08.json",
            "td-sample-09.json",
            "td-sample-10.json" */
    };
    private static int adapterPort = 8040;
    private static boolean updateTypeTest = false; //if true suppose previous run with value false - i.e. itemsFromNMwithOIDs.json exists
    private static boolean cleanAgentAfterTest = true; //if true, delete all items from agent


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
                Thread.sleep(6000);
            } catch (Exception ex) {
            }
            LOG.info(String.format("Adapter with config from %s started!", agentConfigs[i]));
        }

        //get data from the Adapters and save it in the file
        for (int i = 0; i < agentConfigs.length; i++) {
            try {
                AdapterClient adapterClient = new AdapterClient(String.valueOf(adapterPort + i));
                JSONObject itemsFromAdapter = adapterClient.getObjects();
                //LOG.info(itemsFromAdapter.toString());
                JSONArray thingDescriptions = itemsFromAdapter.getJSONArray("thing-descriptions");

                for (int j = 0; j < thingDescriptions.length(); j++) {
                    tdListA.add(thingDescriptions.getJSONObject(j));
                }
            } catch (Exception e) {
                LOG.info("Error by processing the response from Adapter from config " +
                        agentConfigs[i] + ": " + e.getMessage());
                System.exit(102);
            }
            LOG.info(String.format("Reponse from Adapter with config %s processed!", agentConfigs[i]));
        }

        Collections.sort(tdListA, new JsonCompare());
        BufferedWriter outA = null;
        try {
            outA = new BufferedWriter(
                    new OutputStreamWriter(
                            Files.newOutputStream(
                                    Paths.get(String.format("itemsFromAdapters.json")),
                                    CREATE, TRUNCATE_EXISTING, WRITE)));
            saveListOfTDs(tdListA, outA, false);
            outA.close();
        } catch (Exception e) {
            LOG.info("Error by processing the response from adapters: " + e.getMessage());
            e.printStackTrace();
            System.exit(103);
        }


        //start the agent with all adapters
        RunAgent agentAllAdapters = new RunAgent("agent-config.json");
        agentAllAdapters.start();
        LOG.info("Agent with all adapters started!");
        //just wait a little bit for agent to start
        try {
            Thread.sleep(24000 * agentConfigs.length);
        } catch (Exception ex) {
        }


        //get data from Network Manager and save it
        int numberOfTDs;
        if (!updateTypeTest)
        {
            numberOfTDs = getAgentItemsFromNM(agentIDs[0], "itemsFromNM.json", false);
        } else {
            numberOfTDs = getAgentItemsFromNM(agentIDs[0], "itemsFromNM2.json", true);
        }
        LOG.info(String.format("Agent %s has %d TDs registered at NM!", agentIDs[0], numberOfTDs));


        //compare the results between adapters and NM
        boolean result = false;
        if (!updateTypeTest) {
            try {
                result = FileUtils.contentEquals(
                        new File(String.format("itemsFromAdapters.json")),
                        new File(String.format("itemsFromNM.json")));
            } catch (Exception e) {
                LOG.info(String.format("Error by comparing the results of adapters and NM: %s", e.getMessage()));
                result = false;
            }
            LOG.info(String.format("Comparing the results of adapters and NM: %s.", result ? "MATCH" : "DIFFER"));
        } else {
            try {
                result = FileUtils.contentEquals(
                        new File(String.format("itemsFromNMwithOIDs.json")),
                        new File(String.format("itemsFromNM2withOIDs.json")));
            } catch (Exception e) {
                LOG.info(String.format("Error by comparing the results of NM with OIDs: %s", e.getMessage()));
                result = false;
            }
            LOG.info(String.format("Comparing the results from NM before and after update: %s.", result ? "MATCH" : "DIFFER"));
        }


        //stop the agent with all adapters
        agentAllAdapters.stop();
        LOG.info("Agent with all adapters stopped!");

        //stop the adapters
        for (Thread t : adapters) {
            try {
                t.stop();
            } catch (Exception e) {
                LOG.info(String.format("Error by stopping the adapter %d: %s!", t.getId(), e.getMessage()));
                System.exit(-1);
            }
            LOG.info(String.format("Adapter %d stopped!", t.getId()));
        }

        if (cleanAgentAfterTest) {
            //start the empty agent
            RunAgent agentEmptyAdapter = new RunAgent("agent-config-empty.json");
            agentEmptyAdapter.start();
            LOG.info("Agent with empty adapter started!");
            //just wait a little bit for agent to start
            try {
                Thread.sleep(25000);
            } catch (Exception ex) {
            }

            //get data from Network Manager and save it
            numberOfTDs = getAgentItemsFromNM(agentIDs[0], "itemsFromNM-empty.json", false);
            LOG.info(String.format("Agent %s has %d TDs registered at NM!", agentIDs[0], numberOfTDs));

            //stop agent with empty adapter
            agentEmptyAdapter.stop();
            LOG.info("Agent with empty adapter stopped!");
        }


        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;

        LOG.info(String.format("Test duration: %s", String.format("%02d:%02d:%02d.%d\n\n", timeElapsed / (3600 * 1000),
                timeElapsed / (60 * 1000) % 60, timeElapsed / 1000 % 60, timeElapsed % 1000)));

        System.exit(0);
    }

    private static int getAgentItemsFromNM(String agentID, String fileName, boolean withOID) {
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
            if (withOID) {
                String fileName2;
                int idx = fileName.lastIndexOf('.');
                fileName2 =  fileName.substring(0, idx) + "withOIDs.json";
                BufferedWriter out2 = new BufferedWriter(
                        new OutputStreamWriter(
                                Files.newOutputStream(
                                        Paths.get(fileName2),
                                        CREATE, TRUNCATE_EXISTING, WRITE)));
                saveListOfTDs(tdList, out2, true);
            }
            saveListOfTDs(tdList, out, false);
            out.close();
            return tdList.size();
        } catch (Exception e) {
            LOG.info(String.format("Error by processing the response from NM %d: %s", 1, e.getMessage()));
            System.exit(101);
        }
        return 0;
    }

    private static void saveListOfTDs(List<JSONObject> tdList, BufferedWriter out, boolean withOID) throws Exception {
        for (JSONObject item : tdList) {
            ThingDescription td = ThingDescription.create(item,
                    new ThingValidator(false));
            if (td != null) {
                out.write(td.toString(3, withOID));
                out.flush();
            }
        }
    }


}