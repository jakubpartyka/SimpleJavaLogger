package sjl;

import javax.naming.ConfigurationException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@SuppressWarnings("unused")
public class Logger {
    // don't let anyone instantiate this class
    private Logger(){}

    // ### PROPERTIES
    // general
    /**
     * Global log level. Message which log level value is higher that this field's value will be skipped
     */
    private static LogLevel LOG_LEVEL = LogLevel.INFO;
    /**
     * If counters should be incremented. If set to false all counters will be left at 0
     */
    private static boolean COUNT_MESSAGES = true;
    /**
     * If Logger should print log entries to standard output stream
     */
    private static boolean PRINT_TO_STD_OUT = true;
    /**
     * Timestamp format to be used when creating log entry. Should be set accordingly to Java's SimpleDateFormat structure
     */
    private static String TS_FORMAT = "MM/dd/yyyy HH:mm:ss.SSS";

    // logging to file
    /**
     * If Logger should save log entries to file
     */
    private static boolean PRINT_TO_FILE = false;
    /**
     * Path to file in which log entries will be saved (if PRINT_TO_FILE is set to true). Please provide absolute path
     */
    private static String LOG_FILE_PATH = null;

    // counters
    /**
     * Count of messages with respective log level that were received by the Logger. Represents how many times a message with respective log level was received by the Logger.
     * <br>
     * Unless log level is set to OFF or COUNT_MESSAGES is set to false - all messages will be taken into account.
     * <br>
     * Counters will be incremented even if PRINT_TO_FILE and PRINT_TO_STDOUT are set to false.
     */
    public static int counter_fatal, counter_error, counter_warn, counter_info, counter_debug, counter_trace;

    /**
     * Main method that logs provided message.
     * @param message message to be logged
     * @param level log level of provided message
     * @throws ConfigurationException when PRINT_TO_FILE is set to true but LOG_FILE_PATH was not set/
     * @throws IOException if failed to access log file, e.g. due to insufficient permissions
     */
    public static void log (String message, LogLevel level) throws ConfigurationException, IOException {
        if (Logger.LOG_LEVEL == LogLevel.OFF)       // don't do anything if log level is set to OFF
            return;

        if (COUNT_MESSAGES)                         // increment counter
            increment_log_level(level);

        if (!PRINT_TO_STD_OUT && !PRINT_TO_FILE)    // return if printing log entries is disabled
            return;

        if (level.value > Logger.LOG_LEVEL.value)  // skip this message if log level value is higher that set
            return;

        String entry = create_log_entry(message,level);

        if (PRINT_TO_STD_OUT)             // print to stdout if enabled
            System.out.println(entry);

        if (PRINT_TO_FILE)              // print to file if enabled
            print_to_file(entry);
    }

    /**
     * Saves log entry to log file
     * @param entry log entry to be saved in log file
     * @throws ConfigurationException if path to log file was not specified.
     * @throws IOException if failed to access log file
     */
    private static void print_to_file(String entry) throws ConfigurationException, IOException {
        if (LOG_FILE_PATH == null)
            throw new ConfigurationException("property LOG_FILE_PATH not set");

        FileWriter writer = new FileWriter(LOG_FILE_PATH,true);
        writer.write(entry + '\n');
        writer.flush();
        writer.close();
    }

    /**
     * Generates a log entry. Entry will include a timestamp, message's log level and message's content
     * @param msg message's content
     * @param lvl message's log level
     * @return a String object - a log entry
     */
    private static String create_log_entry(String msg,LogLevel lvl){
        String timestamp = new SimpleDateFormat(TS_FORMAT).format(new Timestamp(System.currentTimeMillis()));
        return ("[" + timestamp + "]" + "[" + lvl + "]:" + msg);
    }

    /**
     * Increment value of respective counter for provided log level value
     * @param lvl LogLevel for respective counter to be incremented
     */
    private static void increment_log_level(LogLevel lvl) {
        switch (lvl){
            case FATAL -> counter_fatal ++;
            case ERROR -> counter_error ++;
            case WARN -> counter_warn ++;
            case INFO -> counter_info ++;
            case DEBUG -> counter_debug ++;
            case TRACE -> counter_trace ++;
        }
    }

    /**
     * Sets Logger class fields' values basing on environmental variables. ENVs names must match Logger class field names
     * @throws IllegalAccessException if failed to set value of any field of class Logger
     */
    public static void readEnvs() throws IllegalAccessException {
        Field[] fields = Logger.class.getDeclaredFields();
        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers())) {
                String env_name = f.getName();
                String env_value = System.getenv(env_name);
                if (env_value != null) {                      // skip if env not set
                    String field_class = f.getType().getName();
                    f.setAccessible(true);
                    switch (field_class) {
                        case "int" -> f.setInt(null, Integer.parseInt(env_value));
                        case "long" -> f.setLong(null, Long.parseLong(env_value));
                        case "boolean" -> f.setBoolean(null, Boolean.parseBoolean(env_value));
                        case "logger.LogLevel", "sjl.LogLevel", "LogLevel" -> f.set(null,LogLevel.valueOf(env_value));
                        default -> f.set(null, env_value);
                    }
                    f.setAccessible(false);
                }
            }
        }
    }

    /**
     * Provides current values of Logger class fields
     * @return a String object containing names and current values of Logger class fields
     * @throws IllegalAccessException if failed to access field value
     */
    public static String get_config() throws IllegalAccessException {
        StringBuilder results = new StringBuilder();
        Field[] fields = Logger.class.getDeclaredFields();
        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers())) {
                results
                        .append(f.getName())
                        .append(":")
                        .append(f.get(null))
                        .append(" ; ");
            }
        }
        return results.toString();
    }

    // getters and setters
    public static LogLevel getLogLevel() {
        return LOG_LEVEL;
    }

    public static void setLogLevel(LogLevel logLevel) {
        LOG_LEVEL = logLevel;
    }

    public static boolean isCountMessages() {
        return COUNT_MESSAGES;
    }

    public static void setCountMessages(boolean countMessages) {
        COUNT_MESSAGES = countMessages;
    }

    public static String getTsFormat() {
        return TS_FORMAT;
    }

    public static void setTsFormat(String tsFormat) {
        TS_FORMAT = tsFormat;
    }

    public static boolean isPrintToStdOut() {
        return PRINT_TO_STD_OUT;
    }

    public static void setPrintToStdOut(boolean printToStdOut) {
        PRINT_TO_STD_OUT = printToStdOut;
    }

    public static boolean isPrintToFile() {
        return PRINT_TO_FILE;
    }

    public static void setPrintToFile(boolean printToFile) {
        PRINT_TO_FILE = printToFile;
    }

    public static String getLogFilePath() {
        return LOG_FILE_PATH;
    }

    public static void setLogFilePath(String logFilePath) {
        LOG_FILE_PATH = logFilePath;
    }
}
