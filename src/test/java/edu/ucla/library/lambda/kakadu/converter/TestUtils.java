
package edu.ucla.library.lambda.kakadu.converter;

import java.io.IOException;
import java.io.InputStream;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.joda.time.tz.FixedDateTimeZone;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Helper utilities for testing Lambda functions.
 */
public final class TestUtils {

    private static final String GMT = "GMT";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        MAPPER.registerModule(new TestJacksonMapperModule());
    }

    private static final DateTimeFormatter DT_FORMATTER = ISODateTimeFormat.dateTime().withZone(new FixedDateTimeZone(
            GMT, GMT, 0, 0));

    private TestUtils() {
    }

    /**
     * Helper method that parses a JSON object from a resource on the classpath as an instance of the provided type.
     *
     * @param aResource the path to the resource (relative to this class)
     * @param aClass the type to parse the JSON into
     * @throws IOException If there is trouble getting the resource as a stream
     */
    public static <T> T parse(final String aResource, final Class<T> aClass) throws IOException {
        final InputStream stream = TestUtils.class.getResourceAsStream(aResource);

        try {
            if (aClass == S3Event.class) {
                final String json = IOUtils.toString(stream);
                final S3EventNotification event = S3EventNotification.parseJson(json);

                return (T) new S3Event(event.getRecords());
            } else {
                return MAPPER.readValue(stream, aClass);
            }
        } finally {
            stream.close();
        }
    }

    private static class TestJacksonMapperModule extends SimpleModule {

        private static final long serialVersionUID = 1L;

        TestJacksonMapperModule() {
            super("TestJacksonMapperModule");

            super.addSerializer(DateTime.class, new DateTimeSerializer());
            super.addDeserializer(DateTime.class, new DateTimeDeserializer());
        }
    }

    private static class DateTimeSerializer extends JsonSerializer<DateTime> {

        @Override
        public void serialize(final DateTime aDateTime, final JsonGenerator aGenerator,
                final SerializerProvider aProvider) throws IOException {
            aGenerator.writeString(DT_FORMATTER.print(aDateTime));
        }
    }

    private static class DateTimeDeserializer extends JsonDeserializer<DateTime> {

        @Override
        public DateTime deserialize(final JsonParser aParser, final DeserializationContext aContext)
                throws IOException {
            return DT_FORMATTER.parseDateTime(aParser.getText());
        }
    }

}
