package sk.intersoft.vicinity.semptests;

import org.apache.http.Header;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AdapterServiceImplTest {

    @Autowired
    private MockMvc mockMvc;
    private static String uri = "https://vicinity.bavenir.eu:3000/api";
    private static HttpClient client;
    private static String token;

    @BeforeClass
    public static void setUp() throws Exception {

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

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void thingDescriptionSample() throws Exception {

        this.mockMvc.perform(get("/objects").param("name", "td-sample.json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.adapter-id").value("test01-adapter01"))
                .andExpect(jsonPath("$.thing-descriptions").isArray())
                .andExpect(jsonPath("$.thing-descriptions", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.thing-descriptions[0]").isNotEmpty())
                .andExpect(jsonPath("$.thing-descriptions[0].name").value("My Sample CO2 Sensor Thing"))
                .andExpect(jsonPath("$.thing-descriptions[0].properties[0].read_link.output.field", Matchers.hasSize(2)))
                .andExpect(jsonPath("$.thing-descriptions[0].properties[0].read_link.output.field[1].name").value("timestamp"))
        ;
    }

    @Test
    public void thingDescriptionSample1() throws Exception {

        this.mockMvc.perform(get("/objects").param("name", "td-sample-1.json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.adapter-id").value("test01-adapter01"))
                .andExpect(jsonPath("$.thing-descriptions").isArray())
                .andExpect(jsonPath("$.thing-descriptions", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.thing-descriptions[0]").isNotEmpty())
                .andExpect(jsonPath("$.thing-descriptions[0].name").value("ForaBloodPressureMonitor"))
                .andExpect(jsonPath("$.thing-descriptions[0].properties[0].read_link.output.field", Matchers.hasSize(2)))
                .andExpect(jsonPath("$.thing-descriptions[0].properties[0].read_link.output.field[1].name").value("timestamp"))
                .andExpect(jsonPath("$.thing-descriptions[0].properties[0].monitors").value("adapters:SystolicBloodPressure"))
                .andExpect(jsonPath("$.thing-descriptions[0].properties[1].pid").value("diastolic"))
                .andExpect(jsonPath("$.thing-descriptions[0].properties[2].pid").value("pulse"))
        ;
    }

    @Test
    public void thingDescriptionSample2a() throws Exception {

        HttpGet request = new HttpGet(uri+"/agents/a3119d26-7ea7-4cbe-8a0d-d5d4b6c350b0/items");
        request.addHeader("x-access-token", token);
        HttpResponse response = client.execute(request);
        JSONObject rjobj = new JSONObject(EntityUtils.toString(response.getEntity()));
        JSONObject info = rjobj.getJSONArray("message").getJSONObject(0).getJSONObject("id").getJSONObject("info");

        this.mockMvc.perform(get("/objects").param("name", "td-sample-2a.json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.adapter-id").value(info.getString("adapter-id")))
                .andExpect(jsonPath("$.thing-descriptions[0].name").value(info.getString("name")))
                .andExpect(jsonPath("$.thing-descriptions[0].properties[0].read_link.href").value(info.getJSONArray("properties").getJSONObject(0).getJSONObject("read_link").getString("href")))
                .andExpect(jsonPath("$.thing-descriptions[0].events[0].monitors").value(info.getJSONArray("events").getJSONObject(0).getString("monitors")))
                .andExpect(jsonPath("$.thing-descriptions[0].events[0].output.description").value(info.getJSONArray("events").getJSONObject(0).getJSONObject("output").getString("description")))
        ;
                /*
                .andExpect(jsonPath("$.thing-descriptions").isArray())
                .andExpect(jsonPath("$.thing-descriptions", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.thing-descriptions[0]").isNotEmpty())
                .andExpect(jsonPath("$.thing-descriptions[0].properties[0].read_link.output.field", Matchers.hasSize(2)))
                .andExpect(jsonPath("$.thing-descriptions[0].properties[0].read_link.output.field[1].name").value("timestamp"))
                .andExpect(jsonPath("$.thing-descriptions[0].events[0].output.field", Matchers.hasSize(2)))
*/


    }

    @Test
    public void thingDescriptionSample2b() throws Exception {

        this.mockMvc.perform(get("/objects").param("name", "td-sample-2b.json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.adapter-id").value("test01-adapter01"))
                .andExpect(jsonPath("$.thing-descriptions").isArray())
                .andExpect(jsonPath("$.thing-descriptions", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.thing-descriptions[0]").isNotEmpty())
                .andExpect(jsonPath("$.thing-descriptions[0].name").value("sensor.getName"))
                .andExpect(jsonPath("$.thing-descriptions[0].oid").value("b6463811-30b3-4906-9c0b-efe2928651f7"))
                .andExpect(jsonPath("$.thing-descriptions[0].properties[0].read_link.href").value("/device/b6463811-30b3-4906-9c0b-efe2928651f7/property/status"))
                .andExpect(jsonPath("$.thing-descriptions[0].properties[0].read_link.output.field", Matchers.hasSize(2)))
                .andExpect(jsonPath("$.thing-descriptions[0].properties[0].read_link.output.field[1].name").value("value"))
                .andExpect(jsonPath("$.thing-descriptions[0].properties[0].write_link.href").value("/device/b6463811-30b3-4906-9c0b-efe2928651f7/property/status"))
                .andExpect(jsonPath("$.thing-descriptions[0].properties[0].write_link.input.field", Matchers.hasSize(1)))
                .andExpect(jsonPath("$.thing-descriptions[0].properties[0].write_link.output.field[0].name").value("success"))
                .andExpect(jsonPath("$.thing-descriptions[0].actions", Matchers.hasSize(0)))
                .andExpect(jsonPath("$.thing-descriptions[0].events", Matchers.hasSize(0)))
        ;
    }
}