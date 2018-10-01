package sk.intersoft.vicinity.semptests;

import com.sun.media.jfxmedia.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import sk.intersoft.vicinity.agent.thing.ThingDescription;
import sk.intersoft.vicinity.agent.thing.ThingValidator;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.*;

public class PrepareTDs {
    String fileWithTD = "agent-config-10.json";

    public boolean prepareN(int n, String outFile) {
        try {
            ClassLoader cl = getClass().getClassLoader();
            String jsonString = IOUtils.toString(cl.getResourceAsStream("agent-config-empty.json"));
            JSONObject obj = new JSONObject(jsonString);
            ThingDescription td1 = ThingDescription.create(obj, new ThingValidator(false));
            if (td1 != null) {
                BufferedWriter out = new BufferedWriter(
                        new OutputStreamWriter(
                                Files.newOutputStream(Paths.get(outFile), CREATE, APPEND)));
                out.write(td1.toString(3, false));
                out.flush();
                out.close();
            } else
                return false;
        } catch (Exception e) {
            Logger.logMsg(Logger.DEBUG, e.getMessage());
            return  false;
        }

        return true;
    }
 }
