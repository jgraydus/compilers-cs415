/* Joshua Graydus | March 2016 */
package logging;

import java.io.InputStream;
import java.util.Properties;

public class Logger {

    private enum LogLevel {
        TRACE(0), DEBUG(1), INFO(2), WARN(3), ERROR(4), FATAL(5), SILENT(6);
        private final int val;
        LogLevel(final int val) { this.val = val; }
    }

    static LogLevel logLevel = LogLevel.INFO;

    static {
        try (final InputStream propFile = Logger.class.getClassLoader().getResourceAsStream("logger.properties")) {
            final Properties properties = new Properties();
            properties.load(propFile);
            final String level = properties.getProperty("log-level").trim();
            try {
                logLevel = LogLevel.valueOf(level);
            } catch (IllegalArgumentException e) {
                System.err.println("\"" + level + "\" is not a value logging level");
            }
        } catch (Exception e) {
            System.err.println("could not read logger.properties");
        }
    }

    private void log(final String message, final LogLevel requiredLevel) {
        if (logLevel.val <= requiredLevel.val) { System.out.println(message); }
    }

    public void trace(final String message) { log(message, LogLevel.TRACE); }
    public void debug(final String message) { log(message, LogLevel.DEBUG); }
    public void info(final String message) { log(message, LogLevel.INFO); }
    public void warn(final String message) { log(message, LogLevel.WARN); }
    public void error(final String message) { log(message, LogLevel.ERROR); }
    public void fatal(final String message) { log(message, LogLevel.FATAL); }
}