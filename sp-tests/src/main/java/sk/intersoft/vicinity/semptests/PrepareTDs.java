package sk.intersoft.vicinity.semptests;

import com.sun.media.jfxmedia.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import sk.intersoft.vicinity.agent.thing.ThingDescription;
import sk.intersoft.vicinity.agent.thing.ThingValidator;

import java.io.IOException;

public class PrepareTDs {
    String fileWithTD = "td-sample-2a.json";

    public boolean prepareN(int n, String outFile) {
        try {
            ClassLoader cl = getClass().getClassLoader();
            String jsonString = IOUtils.toString(cl.getResourceAsStream("td-sample-2a.json"));
            JSONObject obj = new JSONObject(jsonString);
            ThingDescription td1 = ThingDescription.create(obj, new ThingValidator(false));
            if (td1 != null) {
                File fout = File.cre

            }
        } catch (Exception e) {
            Logger.logMsg(Logger.DEBUG, e.getMessage());
            return  false;
        }

        return true;
    }
 }
