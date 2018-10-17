package sk.intersoft.vicinity.agentTest.thing;

import org.json.JSONObject;
import sk.intersoft.vicinity.agentTest.Dump;
import sk.intersoft.vicinity.agentTest.JSONUtil;

public class DataSchemaField {
    public String name;
    public String predicate;
    public DataSchema schema;

    // JSON KEYS
    public static final String NAME_KEY = "name";
    public static final String PREDICATE_KEY = "predicate";
    public static final String SCHEMA_KEY = "schema";

    public static DataSchemaField create(JSONObject fieldJSON,
                                         ThingValidator validator) throws Exception {
        DataSchemaField field = new DataSchemaField();
        try{
            field.name = JSONUtil.getString(NAME_KEY, fieldJSON);
            if (field.name == null) {
                validator.error("Missing [" + NAME_KEY + "] in data-schema-field: " + fieldJSON.toString());
            }

            JSONObject schema = JSONUtil.getObject(SCHEMA_KEY, fieldJSON);
            if(schema == null) {
                validator.error("Missing ["+SCHEMA_KEY+"] in  in data-schema-field: "+fieldJSON.toString());
            }
            else {
                field.schema = DataSchema.create(schema, validator);
            }

            field.predicate = JSONUtil.getString(PREDICATE_KEY, fieldJSON);
            if (field.predicate != null) {
                if(!field.schema.isSimpleType()){
                    validator.error("Ontology annotation for field predicate must [" + PREDICATE_KEY + "] must point to simple type: " + fieldJSON.toString());
                }
            }


        }
        catch(Exception e){
            validator.error("unable to process data-schema-field: "+fieldJSON.toString());
            return null;
        }

        return field;
    }

    public static JSONObject toJSON(DataSchemaField field) {
        JSONObject object = new JSONObject();

        object.put(NAME_KEY, field.name);
        if(field.predicate != null){
            object.put(PREDICATE_KEY, field.predicate);
        }
        object.put(SCHEMA_KEY, DataSchema.toJSON(field.schema));
        return object;
    }

    public String toString(int indent) {
        Dump dump = new Dump();

        dump.add("field:", indent);
        dump.add("name: "+name, (indent + 2));
        if(predicate != null) {
            dump.add("predicate: " + predicate, (indent + 2));
        }
        dump.add("schema: ", (indent + 2));
        dump.add(schema.toString(indent + 3));
        return dump.toString();
    }

}