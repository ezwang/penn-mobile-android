package com.pennapps.labs.pennmobile.api;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.pennapps.labs.pennmobile.classes.Course;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by Adel on 12/15/14.
 * Wrapper class for Gson Serializers
 */
public class Serializer {
    public static class CourseSerializer implements JsonDeserializer<List<Course>> {
        @Override
        public List<Course> deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
                throws JsonParseException {
            JsonElement content = je.getAsJsonObject().get("courses");
            return new Gson().fromJson(content, new TypeToken<List<Course>>(){}.getType());
        }
    }

    public static class DataSerializer<T> implements JsonDeserializer<T> {
        @Override
        public T deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
                throws JsonParseException {
            JsonElement content = je.getAsJsonObject().get("result_data");
            return new Gson().fromJson(content, type);
        }
    }
}
