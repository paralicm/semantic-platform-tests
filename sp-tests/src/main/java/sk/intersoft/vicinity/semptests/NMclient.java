package sk.intersoft.vicinity.semptests;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

public class NMclient {

    private static String uri = "https://development.bavenir.eu:3000/api";
    private static HttpClient client;
    private static String token;

    public NMclient() {
        try {
            client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(uri+"/authenticate");
            post.addHeader("Accept", "application/json");
            post.addHeader("Content-Type", "application/json");
            StringEntity data = new StringEntity("{\"username\": \"marek.paralic@intersoft.sk\",\"password\": \"semantic\"}");
            post.setEntity(data);

            HttpResponse post_response = client.execute(post);

            JSONObject obj = new JSONObject( EntityUtils.toString(post_response.getEntity()));

            token = obj.getJSONObject("message").getString("token");

            System.out.println(token);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JSONObject getAgentItems(String agid) {
        try {
            HttpGet request = new HttpGet(String.format("%s/agents/%s/items", uri, agid));
            request.addHeader("x-access-token", token);
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
