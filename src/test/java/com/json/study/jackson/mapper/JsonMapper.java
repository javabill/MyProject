package com.json.study.jackson.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * @author miao.yang susing@gmail.com
 * @since 14-4-24.
 */
@SuppressWarnings("unchecked")
public class JsonMapper {

    private final ObjectMapper mapper;

    public JsonMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public void writeValue(Writer writer, Object obj) throws IOException {
        Preconditions.checkNotNull(writer);

        try {
            mapper.writeValue(writer, obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("jackson format error: " + obj.getClass(), e);
        }
    }

    public String writeValueAsString(Object obj) {

        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("jackson format error: " + obj.getClass(), e);
        }
    }

    public <T> T readValue(String json, Class<T> type) {

        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            throw new RuntimeException("jackson parse error :" + json.substring(0, Math.min(100, json.length())), e);
        }
    }

    public <T> T readValue(Reader reader, Class<T> type) throws IOException {

        Preconditions.checkNotNull(reader);

        try {
            return mapper.readValue(reader, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("jackson parse error.", e);
        }
    }

    public <T> T readValue(String json, TypeReference<T> type) {
        try {
            return (T) mapper.readValue(json, type);
        } catch (Exception e) {
            throw new RuntimeException("jackson parse error.", e);
        }
    }

    public <T> T readValue(Reader reader, TypeReference<T> type) throws IOException {

        Preconditions.checkNotNull(reader);

        try {
            return (T) mapper.readValue(reader, type);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("jackson parse error.", e);
        }
    }
}
