package sk.intersoft.vicinity.semptests;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class AdapterClient {

    private static String uri = "http://localhost:8030";
    private static HttpClient client;

    public AdapterClient() {
        try {
            client = HttpClientBuilder.create().build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JSONObject getObjects() {
        try {
            HttpGet request = new HttpGet(String.format("%s/objects", uri));
            HttpResponse response = client.execute(request);
            JSONObject rjobj = new JSONObject(EntityUtils.toString(response.getEntity()));
            //JSONObject info = rjobj.getJSONArray("message").getJSONObject(0).getJSONObject("id").getJSONObject("info");
            return rjobj;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
