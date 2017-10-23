package com.atomicobject.rts;

import org.json.simple.JSONObject;

public class resource {
    long id;
    String type;
    long total;

    public resource(JSONObject json) {
        long id = (long) json.get("id");
        String type = (String) json.get("type");
        long total = (long) json.get("total");
    }
}