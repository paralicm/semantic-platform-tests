package sk.intersoft.vicinity.agent;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;

public class JsonCompare implements Comparator<JSONObject> {
    private static final String KEY_NAME1 = "name";
    private static final String KEY_NAME2 = "type";

    @Override
    public int compare(JSONObject a, JSONObject b) {
        String valA = new String();
        String valB = new String();
        String valA2 = new String();
        String valB2 = new String();

        try {
            valA = (String) a.get(KEY_NAME1);
            valB = (String) b.get(KEY_NAME1);
            valA2 = (String) a.get(KEY_NAME2);
            valB2 = (String) b.get(KEY_NAME2);
            //System.out.println(valA + " : " + valB);
        } catch (JSONException e) {
            //just log and ignore
            System.out.println("Error by comparing two json objects: " + e.getMessage());
        }
        if (valA.equals(valB))
            return valA2.compareTo(valB2);
        return valA.compareTo(valB);
    }
}
