package city.norain.slimefun4.utils;

import java.lang.reflect.Field;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.slf4j.Log4jLogger;
import org.slf4j.LoggerFactory;

public class LogUtil {
    public static void setSlf4jLogLevel(final Class<?> clazz, final Level logLevel) {
        try {
            Log4jLogger log4Jlogger = (Log4jLogger) LoggerFactory.getLogger(clazz);

            Field field = clazz.getClassLoader()
                    .loadClass("org.apache.logging.slf4j.Log4jLogger")
                    .getDeclaredField("logger");
            field.setAccessible(true);

            Logger logger = (Logger) field.get(log4Jlogger);
            logger.setLevel(logLevel);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
