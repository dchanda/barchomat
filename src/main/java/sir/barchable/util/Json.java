package sir.barchable.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.*;
import java.util.Map;

/**
 * Json serialization util.
 *
 * @author Sir Barchable
 *         Date: 11/05/15
 */
public final class Json {

    private static final ObjectMapper mapper = new ObjectMapper()
        .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);

    private static final ObjectWriter prettyWriter = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
        .writer();

    /**
     * Static API.
     */
    private Json() { }

    public static String toString(Object o) throws JsonProcessingException {
        return mapper.writeValueAsString(o);
    }

    public static String toPrettyString(Object o) throws JsonProcessingException {
        return prettyWriter.writeValueAsString(o);
    }

    public static void write(Object o, OutputStream out) throws IOException {
        mapper.writeValue(out, o);
    }

    public static void write(Object o, Writer out) throws IOException {
        mapper.writeValue(out, o);
    }

    public static void writePretty(Object o, OutputStream out) throws IOException {
        prettyWriter.writeValue(out, o);
    }

    public static void writePretty(Object o, Writer out) throws IOException {
        prettyWriter.writeValue(out, o);
    }

    public static <T> T valueOf(String json, Class<T> type) throws IOException {
        return mapper.readValue(json, type);
    }

    public static <T> T read(InputStream in, Class<T> type) throws IOException {
        return mapper.readValue(in, type);
    }

    public static <T> T read(File f, Class<T> type) throws IOException {
        return mapper.readValue(f, type);
    }
    
    public static <T> T convertValue(Object m, Class<T> type) throws IOException {
        return mapper.convertValue(m, type);
    }
}
