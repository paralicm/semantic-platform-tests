package sk.intersoft.vicinity.semptests;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class AgentClient {
    private static final Logger LOG = Logger.getLogger(AgentClient.class);
    private String uri = "http://localhost:9997/agent";
    private HttpClient client;

    public AgentClient() {
        try {
            client = HttpClientBuilder.create().build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JSONObject postObjects(int numberOfTDs, String filename, int id) {
        try {
            HttpPost request = new HttpPost(uri+"/objects");
            ClassLoader cl = getClass().getClassLoader();
            String payload = IOUtils
                        .toString(cl.getResourceAsStream(filename));
            JSONObject newPayload = new JSONObject(payload);
            String adapterId = newPayload.getString("adapter-id");
            JSONArray jarr = newPayload.getJSONArray("thing-descriptions");
            JSONObject td1 = jarr.getJSONObject(0);
            String td1Name = td1.getString("name");
            String td1Oid = td1.getString("oid");
            JSONObject td2 = jarr.getJSONObject(1);
            String td2Name = td2.getString("name");
            String td2Oid = td2.getString("oid");
            JSONArray newjarr = new JSONArray();
            int index = 0, i = 1;
            while (index < numberOfTDs) {
                JSONObject newTD1 = new JSONObject(td1.toString());
                newTD1.put("name", td1Name + i);
                newTD1.put("oid", td1Oid + i);
                newjarr.put(index++, newTD1);
                if (index == numberOfTDs)
                    break;
                JSONObject newTD2 = new JSONObject(td2.toString());
                newTD2.put("name", td2Name+i);
                newTD2.put("oid", td2Oid+i);
                newjarr.put(index++, newTD2);
                i++;
            }
            newPayload.put("adapter-id", adapterId+id);
            newPayload.put("thing-descriptions", newjarr);

            //LOG.info("NEWPAYLOAD --> " + newPayload.toString().substring(0,1000) + "...");
            StringEntity entity = new StringEntity(newPayload.toString(),
                    ContentType.APPLICATION_FORM_URLENCODED);
            request.setEntity(entity);

            HttpResponse response = client.execute(request);
            //LOG.info(response.getStatusLine().getStatusCode());
            JSONObject rjobj = new JSONObject(EntityUtils.toString(response.getEntity()));
            return rjobj;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
