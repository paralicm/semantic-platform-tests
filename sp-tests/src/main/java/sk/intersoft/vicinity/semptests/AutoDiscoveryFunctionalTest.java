package sk.intersoft.vicinity.semptests;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import sk.intersoft.vicinity.agentTest.JsonCompare;
import sk.intersoft.vicinity.agentTest.thing.ThingDescription;
import sk.intersoft.vicinity.agentTest.thing.ThingValidator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.nio.file.StandardOpenOption.*;

public class AutoDiscoveryFunctionalTest {
    private static final Logger LOG = Logger.getLogger(AutoDiscoveryFunctionalTest.class);

    private static String[] agentIDs = new String[]{
            "c1aa668e-38ff-405d-a1dd-fbc09168ec74",
            "1d2b9e77-ff16-445e-a317-96fb20557d1f",
            "d50e838a-0619-4338-b185-1ba58dee17c9",
            "46cbcd7f-d4ad-432f-bbb1-90f95b19b47a",
            "1ebff626-210d-437d-8412-0bfaa02d6274"
    };
    private static String[] agentConfigs = new String[]{
//            "td-sample.json"
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
    private static boolean updateTypeTest = false; //if true suppose previous run with value false - i.e. itemsFromNMwithOIDs.json exists
    private static boolean cleanAgentAfterTest = true; //if true, delete all items from agent
    public static boolean WINDOWS = false;
    public static boolean registrationLimitsTest = true;


    public static void main(String[] args) {
        AutoDiscoveryFunctionalTest test01 = new AutoDiscoveryFunctionalTest();
        long start = System.currentTimeMillis();

        if (registrationLimitsTest)
        {
            //test01.testLimitOfTDsInAdapter(10, 5, "adapter-objects.json");
            //test01.testLimitOfTDsInAdapter(1, 80, "adapter-objects-CERTH.json");
            //test01.testLimitOfTDsInAdapter(1, 55, "adapter-objects-AAU.json");
            //test01.testLimitOfTDsInAdapter(1, 45, "adapter-objects.json");
            test01.testLimitOfTDsInAdapter(5, 5, "adapter-objects-TM.json");
            System.exit(0);
        }

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
                Thread.sleep(8000);
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
        RunAgent agentAllAdapters = new RunAgent("agent-config.json", true);
        agentAllAdapters.start();
        LOG.info("Agent with all adapters started!");
        //just wait a little bit for agent to start
        try {
            Thread.sleep(50000 * agentConfigs.length);
        } catch (Exception ex) {
        }


        //get data from Network Manager and save it
        int numberOfTDs;
        if (!updateTypeTest)
        {
            numberOfTDs = getAgentItemsFromNM(agentIDs[0], "itemsFromNM.json", true);
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
        if (!WINDOWS) {
            agentAllAdapters.stop();
            //just wait a little bit
            try {
                Thread.sleep(8000 * agentConfigs.length);
            } catch (Exception ex) {
            }
            LOG.info("Agent with all adapters stopped!");
        }


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

        //just wait a little bit
        try {
            Thread.sleep(5000);
        } catch (Exception ex) {
        }

        if (cleanAgentAfterTest && !WINDOWS) {
            //start the empty agent
            RunAgent agentEmptyAdapter = new RunAgent("agent-config-empty.json", true);
            agentEmptyAdapter.start();
            LOG.info("Agent with empty adapter started!");
            //just wait a little bit for agent to run
            try {
                Thread.sleep(17000 * agentConfigs.length);
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

    private void testLimitOfTDsInAdapter(int numberOfAdapters, int numberOfTDsPerAdapter, String skeletonTDs) {

        //start the agent with active adapter
        String config = prepareAgentConfig("agent-config-active.json", numberOfAdapters);
        RunAgent agentActiveAdapter = new RunAgent(config, false);
        agentActiveAdapter.start();
        LOG.info("Agent with active adapter started!");
        //just wait a little bit for agent to start
        try {
            Thread.sleep(20000);
        } catch (Exception ex) {
            LOG.info("End of Agent sleep!");
        }

        long start = System.currentTimeMillis();

        ActiveAdapter[] activeAdapters = new ActiveAdapter[100];
        for (int i = 1; i <= numberOfAdapters; i++) {
            activeAdapters[i] = new ActiveAdapter(skeletonTDs, i, numberOfTDsPerAdapter);
            activeAdapters[i].start();
        }
        try {
            for (int i = 1; i <= numberOfAdapters; i++)
                activeAdapters[i].threadForAdapter.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long finish1 = System.currentTimeMillis();
        long timeElapsed1 = finish1 - start;

        LOG.info(String.format("Test duration for all %d adapters with %d TDs each: %s", numberOfAdapters, numberOfTDsPerAdapter, String.format("%02d:%02d:%02d.%d\n\n", timeElapsed1 / (3600 * 1000),
                timeElapsed1 / (60 * 1000) % 60, timeElapsed1 / 1000 % 60, timeElapsed1 % 1000)));

        //stop agent with active adapter
        agentActiveAdapter.stop();
        LOG.info("Agent with active adapter stopped!");

        //just wait a little bit for agent to stop
        try {
            Thread.sleep(10000);
        } catch (Exception ex) {
        }

        //start the empty agent
        RunAgent agentEmptyAdapter = new RunAgent("agent-config-empty.json", true);
        agentEmptyAdapter.start();
        LOG.info("Agent with empty adapter started!");
        //just wait a little bit for agent to run
        try {
            Thread.sleep(10000);
        } catch (Exception ex) {
        }

        //stop agent with empty adapter
        agentEmptyAdapter.stop();
        LOG.info("Agent with empty adapter stopped!");

    }

    private String prepareAgentConfig(String configFile, int numberOfAdapters) {
        try {
            ClassLoader cl = getClass().getClassLoader();
            String config = IOUtils
                    .toString(cl.getResourceAsStream(configFile));
            JSONObject newAgentConfig = new JSONObject(config);
            JSONArray jarr = newAgentConfig.getJSONArray("adapters");
            JSONObject adapter = jarr.getJSONObject(0);
            String adapterId = adapter.getString("adapter-id");
            JSONArray newjarr = new JSONArray();
            for (int i = 1; i <= numberOfAdapters; i++) {
                JSONObject newAdapter = new JSONObject(adapter.toString());
                newAdapter.put("adapter-id", adapterId + i);
                newjarr.put(i-1, newAdapter);
            }
            newAgentConfig.put("adapters", newjarr);

            File tmpFile = File.createTempFile("agentConfig", ".json");
            FileWriter writer = new FileWriter(tmpFile);
            writer.write(newAgentConfig.toString());
            writer.close();
            return tmpFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}