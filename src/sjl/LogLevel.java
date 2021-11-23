package sjl;

public enum LogLevel {
    OFF(0),
    FATAL(1),
    ERROR(2),
    WARN(3),
    INFO(4),
    DEBUG(5),
    TRACE(6);

    int value;
    LogLevel(int value) {
        this.value = value;
    }
}
