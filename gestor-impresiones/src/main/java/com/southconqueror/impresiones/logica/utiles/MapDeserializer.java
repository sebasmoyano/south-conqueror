package com.southconqueror.impresiones.logica.utiles;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import jersey.repackaged.com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by smoyano on 15/04/17.
 */
public class MapDeserializer  extends JsonDeserializer<Map<String, String>>
{
    @SuppressWarnings("PublicField")
    private static class Entry
    {
        @JsonProperty
        public String key;
        @JsonProperty
        public String value;
    }

    @Override
    public Map<String, String> deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException
    {
        List<Entry> list = jp.readValueAs(new TypeReference<List<Entry>>() {});
        ImmutableMap.Builder<String, String> map = ImmutableMap.builder();
        for (Entry entry : list) {
            map.put(entry.key, entry.value);
        }
        return map.build();
    }
}