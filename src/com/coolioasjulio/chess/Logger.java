package com.coolioasjulio.chess;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class Logger {

    private static final String defaultLogFile = "debug.log";
    private static Logger instance;

    public static Logger getGlobalLogger() {
        if (instance == null) {
            instance = new Logger(defaultLogFile);
        }
        return instance;
    }

    public static void setGlobalLogger(Logger logger) {
        if (instance != null) {
            instance.closeLogFile();
        }
        instance = logger;
    }

    private boolean loggingEnabled;
    private PrintStream out;

    public Logger() {

    }

    public Logger(String path) {
        if (!openLogFile(path)) {
            throw new IllegalArgumentException("Invalid path!");
        }
    }

    public Logger(PrintStream out) {
        this.out = out;
    }

    public PrintStream getOutputStream() {
        return out;
    }

    public void logErr(String err) {
        logErr(err, false);
    }

    public void logErr(String err, boolean flush) {
        log("***ERROR*** " + err, flush);
    }
    
    public void logf(String format, Object... args) {
        log(String.format(format, args));
    }

    public void log() {
        log("", false);
    }

    public void log(String log) {
        log(log, false);
    }

    public void log(String log, boolean flush) {
        if (loggingEnabled) {
            out.println(log);
            if (flush) {
                out.flush();
            }
        }
    }

    public void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    public boolean isLogFileOpened() {
        return out == null;
    }

    public boolean openLogFile(String path) {
        try {
            out = new PrintStream(new FileOutputStream(path));
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void closeLogFile() {
        out.close();
        out = null;
    }
}
