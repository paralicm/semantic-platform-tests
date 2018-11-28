package sk.intersoft.vicinity.semptests;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class ActiveAdapter implements Runnable {
    private static final Logger LOG = Logger.getLogger(ActiveAdapter.class);
    Thread threadForAdapter;
    private int adapterId;
    private int noTDs; //number of dynamically created TDs in the active adapter
    private String adapterConfigFile;

    public ActiveAdapter(String adapterConfigFile, int Id, int numberTDs) {
        this.adapterConfigFile = adapterConfigFile;
        this.adapterId = Id;
        this.noTDs = numberTDs;
    }

    public void start() {
        threadForAdapter = new Thread(this, "ActiveAdapter" + adapterId);
        threadForAdapter.start();
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();

        try {
            AgentClient agentClient = new AgentClient();
            JSONObject response = agentClient.postObjects(noTDs, adapterConfigFile, adapterId);

            String statusCodeReason = response.getString("statusCodeReason");
            LOG.info(statusCodeReason);
            LOG.info(response.getInt("statusCode"));
            if (statusCodeReason == "OK") {
                JSONObject msg = response.getJSONArray("message").getJSONObject(0);
                LOG.info(msg.getString("response"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        long finish1 = System.currentTimeMillis();
        long timeElapsed1 = finish1 - start;

        LOG.info(String.format("Test duration for adapter %d: %s", adapterId, String.format("%02d:%02d:%02d.%d\n\n", timeElapsed1 / (3600 * 1000),
                timeElapsed1 / (60 * 1000) % 60, timeElapsed1 / 1000 % 60, timeElapsed1 % 1000)));
    }
}