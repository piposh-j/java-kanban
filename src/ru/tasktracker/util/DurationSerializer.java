
package ru.tasktracker.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.Duration;

public class DurationSerializer implements JsonSerializer<Duration> {

    @Override
    public JsonElement serialize(Duration duration, Type srcType, JsonSerializationContext context) {
        return new JsonPrimitive(duration.getSeconds() / 60);
    }
}