package org.pimatic.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by h3llfire on 21.06.14.
 */
public class Device {

    public class Attribute {
        private String name;
        private String value;
        private String type;

        public Attribute(String name, String value, String type) {
            this.name = name;
            this.value = value;
            this.type = type;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

    }


    private String name;
    private String id;
    private String template;
    private ArrayList<Attribute> attributes;

    public Device(JSONObject obj) throws JSONException {
        this(obj.getString("name"), obj.getString("id"), obj.getString("id"));
        this.attributes = new ArrayList<Attribute>();
        JSONArray attrs = obj.getJSONArray("attributes");
        for(int i = 0; i < attrs.length(); i++) {
            addOrModifyAttribute(attrs.getJSONObject(i));
        }
    }

    public Device(String name, String id, String template) {
        this.name = name;
        this.id = id;
        this.template = template;
    }

    private void addOrModifyAttribute(JSONObject attr) throws JSONException {
        addOrModifyAttribute(attr.getString("name"), attr.getString("value"), attr.getString("type"));
    }

    public void addOrModifyAttribute(String name, String value) {
        addOrModifyAttribute(name, value, "string");
    }

    public void addOrModifyAttribute(String name, String value, String type) {
        Attribute a = getAttribute(name);

        if (a == null) {
            a = new Attribute(name, value, type);
        } else {
            a.setValue(value);
        }

        this.attributes.add(a);
    }

    public String getId() {
        return id;
    }

    public String toString() {
        return "Device: " + id;
    }

    public String getName() {
        return name;
    }

    public static JSONObject getJsonAttribute(JSONObject deviceObj, String name) throws JSONException {
        JSONArray attrs = deviceObj.getJSONArray("attributes");
        for(int i = 0; i < attrs.length(); i++) {
            JSONObject attr = attrs.getJSONObject(i);
            if(attr.getString("name").equals(name)) {
                return attr;
            }
        }
        return null;
    }

    public Attribute getAttribute(String attrName) {
        for(Attribute attr : attributes) {
            if(attr.getName().equalsIgnoreCase(attrName)) {
                return attr;
            }
        }
        return null;
    }

    public ArrayList<Attribute> getAttributes() {
        return attributes;
    }


    public <T> T visit(DeviceVisitor<T> v) {
        return v.visitDevice(this);
    }

}
