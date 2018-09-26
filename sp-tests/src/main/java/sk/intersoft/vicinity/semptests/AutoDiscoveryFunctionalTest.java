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
    private static int adapterPort = 8040;
    private static int numberOfAdapters = 10;



    public static void main(String [ ] args) {
        long start = System.currentTimeMillis();

        ArrayList<Thread> adapters = new ArrayList<Thread>();

        //TODO: generovat objects_TD podla slovnikov pre property a actions & events
        List<JSONObject> tdListA = new ArrayList<JSONObject>();
        for (int i = 1; i <= numberOfAdapters; i++  ) {
            Application adapter = new Application();
            adapter.objects_TD = String.format("td-sample-%02d.json", i);
            adapter.port = String.valueOf(adapterPort + i - 1);
            Thread t1 = new Thread(adapter);
            adapters.add(t1);
            t1.start();
            //just wait for adapter to start
            try {
                Thread.sleep(7000);
            } catch (Exception ex) {
            }
            System.out.println(String.format("Adapter %02d started!", i));
        }


        //get data from the Adapters and save it in the file
        for (int i = 1; i <= numberOfAdapters; i++  ) {
            try {
                AdapterClient adapterClient = new AdapterClient(String.valueOf(adapterPort+i-1));
                JSONObject itemsFromAdapter = adapterClient.getObjects();
                //System.out.println(itemsFromAdapter.toString());
                JSONArray thingDescriptions = itemsFromAdapter.getJSONArray("thing-descriptions");

                for (int j = 0; j < thingDescriptions.length(); j++) {
                    tdListA.add(thingDescriptions.getJSONObject(j));
                }
            } catch (Exception e) {
                System.out.println("Error by processing the response from Adapter " + String.valueOf(i) + e.getMessage());
                System.exit(102);
            }
            System.out.println(String.format("Reponse from Adapter %02d processed!", i));
        }

        Collections.sort( tdListA, new JsonCompare());
        BufferedWriter outA = null;
        try {
            outA= new BufferedWriter(
                    new OutputStreamWriter(
                            Files.newOutputStream(
                                    Paths.get(String.format("itemsFromAdapter-%02d.json", 1)),
                                    CREATE, TRUNCATE_EXISTING, WRITE)));
            saveListOfTDs(tdListA, outA);
            outA.close();
        } catch (Exception e) {
            System.out.println("Error by processing the response from adapters: " + e.getMessage());
            e.printStackTrace();
            System.exit(103);
        }



        //start the agent
        ProcessBuilder builder = new ProcessBuilder();
        startAgent( builder, "agent-config-10a.json");
        System.out.println("Agent started!");
        //just wait a little bit for agent to start
        try {
            Thread.sleep(200000);
        } catch (Exception ex) {}



        //get data from Network Manager and save it int outFileNM
        //for (int i = 0; i < 1; i++  ) {
        try {
            NMclient nmClient = new NMclient();
            JSONObject itemsFromNM = nmClient.getAgentItems(agentIDs[3]);
            System.out.println(itemsFromNM.toString());

            JSONArray message = itemsFromNM.getJSONArray("message");
            List<JSONObject> tdList = new ArrayList<JSONObject>();
            for (int j = 0; j < message.length(); j++) {
                tdList.add(message.getJSONObject(j).getJSONObject("id").getJSONObject("info"));
            }
            Collections.sort(tdList, new JsonCompare());
            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(
                            Files.newOutputStream(
                                    Paths.get(String.format("itemsFromNM-%02d.json", 1)),
                                    CREATE, TRUNCATE_EXISTING, WRITE)));
            saveListOfTDs(tdList, out);
            out.close();
        } catch (Exception e) {
            System.out.println(String.format("Error by processing the response from NM %d: %s", 1, e.getMessage()));
            System.exit(101);
        }
        System.out.println(String.format("Reponse from NM %d processed!", 1));
        //}


        //porovnat vysledky
        //for (int i = 0; i < numberOfAdapters; i++  ) {
        boolean result = false;
        try {
            result = FileUtils.contentEquals(
                    new File(String.format("itemsFromAdapter-%02d.json", 1)),
                    new File(String.format("itemsFromNM-%02d.json", 1)));
        } catch (Exception e) {
            System.out.println(String.format("Error by comparing the results of %02d. adapter and NM: %s", 1, e.getMessage()));
            result = false;
        }
        System.out.println(String.format("Comparing the results of %02d. adapter and NM: %s.", 1, result ? "MATCH" : "DIFFER"));
        //}

        //PrepareTDs prepare = new PrepareTDs();
        //prepare.prepareN(1, "./adapter1-nm.txt");

        long finish = System.currentTimeMillis();
        long timeElapsed = finish - start;

        LOG.info(String.format("Test duration: %s", String.format("%02d:%02d:%02d.%d", timeElapsed/(3600*1000),
                timeElapsed/(60*1000) % 60, timeElapsed/1000 % 60, timeElapsed%1000)));

        //stop the agent
        try {
            builder.command("sh", "-c", System.getProperty("user.home")+"/vicinity/agent/agent.sh stop");
            Process process = builder.start();
        } catch (Exception e) {
            System.out.println("Error by stopping the agent: " + e.getMessage());
            System.exit(-1);
        }
        System.out.println("Agent stopped!");

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
        startAgent( builder, "agent-config-empty.json");
        System.out.println("Agent started!");
        //just wait a little bit for agent to start
        try {
            Thread.sleep(200000);
        } catch (Exception ex) {}

        System.exit(0);
    }

    private static void startAgent(ProcessBuilder builder, String agentConfigFile) {
        builder.command("sh", "-c", System.getProperty("user.home")+"/vicinity/agent/agent.sh");
        builder.directory(new File(System.getProperty("user.home")+"/vicinity/agent/"));
        //builder.redirectError(new File(System.getProperty("user.home")+"/vicinity/err.txt"));
        ClassLoader classLoader = AutoDiscoveryFunctionalTest.class.getClassLoader();
        try {
            Files.copy(
                    new File( classLoader.getResource(agentConfigFile).getFile()).toPath(),
                    new File(System.getProperty("user.home")+"/vicinity/agent/config/agents/agent-01.json").toPath(),
                    REPLACE_EXISTING);
            Process process = builder.start();
            process.waitFor();
        } catch (Exception e) {
            System.out.println("Error by starting the agent: " + e.getMessage());
            System.exit(100);
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