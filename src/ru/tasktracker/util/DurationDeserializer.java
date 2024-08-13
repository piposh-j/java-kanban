
package ru.tasktracker.util;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.Duration;

public class DurationDeserializer implements JsonDeserializer<Duration> {
    @Override
    public Duration deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        return Duration.ofMinutes(json.getAsInt());
    }
}