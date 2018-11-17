package sk.intersoft.vicinity.semptests;

import org.json.JSONArray;
import org.json.JSONObject;
import sk.intersoft.vicinity.agent.service.config.processor.ThingDescriptions;
import sk.intersoft.vicinity.agent.service.config.processor.ThingProcessor;
import sk.intersoft.vicinity.agent.service.config.processor.ThingsDiff;
import sk.intersoft.vicinity.agent.thing.InteractionPattern;
import sk.intersoft.vicinity.agent.thing.InteractionPatternEndpoint;
import sk.intersoft.vicinity.agent.thing.ThingDescription;
import sk.intersoft.vicinity.agent.thing.ThingValidator;

import java.util.*;

public class DIFFTest {
    // THING:
    private static final String adapterId = "adapter-id";
    private static final String thingName = "t-name";
    private static final String thingType = "t-type";

    // PROP
    private static final String pid = "property-id";
    private static final String patternReference = "reffers-to";


    // DATE TIME UTILS: START
    public static long millis(){
        return System.currentTimeMillis();
    }


    public static long duration(long ms) {
        return (millis() - ms);
    }

    public static String hours(long ms) {
        return ((ms / (1000 * 60 * 60)) % 24)+"";
    }

    public static String minutes(long ms) {
        return ((ms / (1000 * 60)) % 60)+"";
    }

    public static String seconds(long ms) {
        return ((ms / 1000) % 60)+"";
    }

    public static String format(long time) {
        return time + "ms :: "+hours(time)+":"+minutes(time)+":"+seconds(time);
    }
    // DATE TIME UTILS: END


    private static Map<String, String> oid2iid =  new HashMap<String, String>();

    private ThingDescriptions init(int num) throws Exception {
        ThingDescriptions things = new ThingDescriptions();
        for(int i = 1; i <= num; i++){
            String oid = "oid-"+i;
            String iid = "iid-"+i;
            oid2iid.put(oid, iid);
            things.add(configThing(oid, iid));
        }
        return things;
    }

    private String oid2iid(String oid) throws Exception{
        String iid = oid2iid.get(oid);
        if(iid == null) throw new Exception("OID ["+oid+"] does not exist");

        return iid;
    }

    private InteractionPattern property(String pid){
        InteractionPattern p = new InteractionPattern();
        JSONObject readLink = new JSONObject(
                "{\"href\": \"/device/{oid}/property/"+pid+"\",\"output\": {\"type\": \"object\","
                + "\"field\": [{\"name\": \"property\",\"schema\": {\"type\": \"string\" } },"
                + "{\"name\": \"value\", \"schema\": {\"type\": \"integer\" } } ] } }");
        p.id = pid;
        p.refersTo = patternReference;
        try {
            p.readEndpoint = InteractionPatternEndpoint.create(
                    readLink, InteractionPatternEndpoint.READ,
                    new ThingValidator(true));
        } catch (Exception e) {
            //no readEndPoint set
            System.out.println("!!! "+e.getMessage());
        }

        return p;
    }

    private void addProperty(ThingDescription thing, InteractionPattern property){
        thing.properties.put(property.id, property);
    }

    private ThingDescription thing(){
        ThingDescription t = new ThingDescription();
        t.adapterId = adapterId;
        t.name = thingName;
        t.type = thingType;

        addProperty(t, property(pid));

        return t;
    }

    private ThingDescription configThing(String oid, String iid) throws Exception {
        ThingDescription t = thing();
        t.oid = oid;
        t.adapterInfrastructureID = ThingDescription.identifier(adapterId, iid);
        t.password = "...";

        return t;
    }

    private ThingDescription adapterThing(String iid) throws Exception {
        ThingDescription t = thing();
        t.infrastructureId = iid;
        t.adapterInfrastructureID = ThingDescription.identifier(adapterId, iid);

        return t;
    }
    private ThingDescription update(ThingDescription t) throws Exception {
        addProperty(t, property("new pid"));
        return t;
    }

    private DIFFExpectation stub(ThingDescriptions config,
                                 int create,
                                 int update,
                                 int delete) throws Exception {
        DIFFExpectation expectation = new DIFFExpectation();

        int size = config.byAdapterInfrastructureID.keySet().size();
        List<ThingDescription> things = ThingDescriptions.toList(config.byAdapterInfrastructureID);
        if (size == 0) {
            size = config.byAdapterOID.keySet().size();
            things = ThingDescriptions.toList(config.byAdapterOID);
        }

        System.out.println("GENERATE ADAPTER STUB");
        System.out.println("config: "+size);
        System.out.println("EXPECTATION: ");
        System.out.println("create: "+create);
        System.out.println("update: "+update);
        System.out.println("delete: "+delete);
        if((update + delete) > size) throw new Exception("update + delete > things in config");

        int unchange = size - (update + delete);
        System.out.println("unchanged: "+unchange);

        int finalSize = create + update + unchange;
        System.out.println("final things in adapter: "+finalSize);


        Collections.shuffle(things);
        things.sort(Comparator.comparing(ThingDescription::getOID));
        /*
        System.out.println("things as sorted list: "+things.size());
        for(ThingDescription t : things) {
            System.out.println("  "+t.toSimpleString());
        }
        */


        System.out.println("LETS START!");


        int dstart = 0;
        int dend = delete;
        System.out.println("DELETE : "+delete+ " :: interval: " +dstart+ " -> "+dend);
        for(int i = dstart; i < dend; i++){
            ThingDescription t = things.get(i);
            //System.out.println("  deleting (not adding to adapter things): "+t.toSimpleString());

            expectation.delete.add(t.oid);
        }

        System.out.println("CREATE: "+create);
        for(int i = 0; i < create; i++){
            String iid = UUID.randomUUID().toString();
            ThingDescription c = adapterThing(iid);
            //System.out.println("  creating: "+iid + " :: "+c.toSimpleString());

            expectation.create.add(iid);
            expectation.things.add(c);
        }

        int upstart = dend;
        int upend = dend + update;
        System.out.println("UPDATE: "+update+ " :: interval: 0 -> "+update);
        for(int i = upstart; i < upend; i++){
            ThingDescription t = things.get(i);

            String iid = oid2iid(t.oid);
            ThingDescription u = update(adapterThing(iid));
            //System.out.println("  updating: "+iid + " :: "+u.toSimpleString());

            expectation.update.add(t.oid);
            expectation.things.add(u);
        }


        int unstart = upend;
        int unend = upend + unchange;
        System.out.println("UNCHANGED : "+unchange+ " :: interval: " +unstart+ " -> "+unend);
        for(int i = unstart; i < unend; i++){
            ThingDescription t = things.get(i);
            String iid = oid2iid(t.oid);
            ThingDescription u = adapterThing(iid);
            //System.out.println("  unchanging: "+iid + " :: "+u.toSimpleString());

            expectation.unchange.add(t.oid);
            expectation.things.add(u);
        }


        return expectation;
    }

    private Set<String> oids(ThingDescriptions desc) {
        List<ThingDescription> things =  ThingDescriptions.toList(desc.byAdapterInfrastructureID);
        Set<String> oids = new HashSet<String>();
        for(ThingDescription t : things) {
            oids.add(t.oid);
        }
        return oids;
    }
    private Set<String> iids(ThingDescriptions desc) {
        List<ThingDescription> things =  ThingDescriptions.toList(desc.byAdapterInfrastructureID);
        Set<String> iids = new HashSet<String>();
        for(ThingDescription t : things) {
            iids.add(t.infrastructureId);
        }
        return iids;
    }

    private static String config2json(ThingDescriptions config) throws Exception {
        //ThingDescriptions config = init(3);

        //System.out.println("CONFIG: \n"+config.toFullString(0));
        JSONArray array1 = new JSONArray();
        JSONArray array2 = new JSONArray();
        for (Map.Entry<String, ThingDescription> entry : config.byAdapterInfrastructureID.entrySet()) {
            JSONObject jobj = ThingDescription.toJSON(entry.getValue());
            //jobj.put("adapterinfrastructure-id", entry.getValue().adapterInfrastructureID);
            array1.put(jobj);
        }
        for (int i=0; i < array1.length(); i++) {
            JSONObject x = new JSONObject();
            x.put("id", (new JSONObject()).put("info", array1.getJSONObject(i)));
            array2.put(x);
        }
        JSONObject x = new JSONObject();
        //x.put("adapter-id", "T6.3.2AdapterForTesting");
        //x.put("thing-descriptions", array1);
        x.put("message", array2);

        //System.out.println("CONFIG JSON: \n"+x.toString(2));
        return x.toString(2);
    }


    public static void main(String[] args) throws Exception {

        int all = 10000;
        int noCreate = all/3;
        int noUpdate = all/3;
        int noDelete = all/3;

        if (args.length > 1) {
            all = Integer.parseInt(args[0]);
            noCreate = Integer.parseInt(args[1]);
            noUpdate = Integer.parseInt(args[2]);
            noDelete = Integer.parseInt(args[3]);
        }
        System.out.println("all/create/update/delete = " + all + "/" + noCreate + "/" + noUpdate + "/" + noDelete );
        DIFFTest g = new DIFFTest();

        ThingDescriptions config = g.init(all);
        DIFFExpectation expectation = g.stub(config, noCreate, noUpdate, noDelete);

        String configJson = config2json(config);
        //System.out.println(configJson);

        long start = millis();
        //Deserialization of the response from the NM
        ThingDescriptions parsedConfig = new ThingDescriptions();
        List<JSONObject> objects = ThingProcessor.processConfiguration(configJson);
        System.out.println("Number of parsed objects: " + objects.size());

        List<JSONObject> unprocessed = new ArrayList<JSONObject>();
        System.out.println("parsing things ... ");
        for (JSONObject object : objects) {
            ThingValidator validator = new ThingValidator(true);
            ThingDescription thing = validator.create(object);
            if (thing != null) {
                try {
                    parsedConfig.add(thing);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    System.out.println("unprocessed thing [" + thing.oid + "]! remove!");
                    unprocessed.add(object);
                }
            } else {
                System.out.println("unprocessed thing! validator errors: \n" + validator.failureMessage().toString(2));
                unprocessed.add(object);
            }
        }

        if (!unprocessed.isEmpty())
            System.exit(1);
        else
            System.out.println("Processing json string OK");

//        System.out.println("CONFIG: \n"+config.toFullString(0));
//        System.out.println("ADAPTER: \n"+expectation.things.toFullString(0));
        ThingsDiff diff = ThingsDiff.fire(config, expectation.things);

        //Serialization of the result to be sent to NM
        configJson = config2json(config);

        long end = duration(start);


        System.out.println("DIFF TOOK: "+format(end));

        System.out.println("DIFF: ");
        System.out.println(diff.toString(0));

        System.out.println("COMPARING EXPECTATIONS: ");
        System.out.println("DELETE: ");
        Set<String> delete = g.oids(diff.delete);
        //System.out.println("  expected: "+expectation.delete);
        //System.out.println("  real: "+delete);
        System.out.println("  match: "+delete.equals(expectation.delete));

        System.out.println("CREATE: ");
        Set<String> create = g.iids(diff.create);
        //System.out.println("  expected: "+expectation.create);
        //System.out.println("  real: "+create);
        System.out.println("  match: "+create.equals(expectation.create));

        System.out.println("UPDATE: ");
        Set<String> update = g.oids(diff.update);
        //System.out.println("  expected: "+expectation.update);
        //System.out.println("  real: "+update);
        System.out.println("  match: "+update.equals(expectation.update));

        System.out.println("UNCHANGE: ");
        Set<String> unchange = g.oids(diff.unchanged);
        //System.out.println("  expected: "+expectation.unchange);
        //System.out.println("  real: "+unchange);
        System.out.println("  match: "+unchange.equals(expectation.unchange));

        System.out.println("DIFF TOOK: "+format(end));

    }
}
