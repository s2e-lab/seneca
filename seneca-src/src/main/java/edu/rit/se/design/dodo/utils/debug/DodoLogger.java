package edu.rit.se.design.dodo.utils.debug;

import java.io.IOException;
import java.util.logging.*;

/**
 * Utility class for logging & aid debugging.
 *
 * @author Joanna C. S. Santos (jds5109@rit.edu)
 */
public class DodoLogger {
    public static final boolean PRINT_ON_CONSOLE = true;

    public static Logger getLogger(Class klass) {
        return getLogger(klass, false);
    }

    public static Logger getLogger(Class klass, boolean prependInfo) {
        return getLogger(klass, false, null);
    }


    public static Logger getLogger(Class klass, boolean prependInfo, Handler handler) {
        try {
            if (prependInfo)
                System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tl:%1$tM:%1$tS %1$Tp] %4$s: %5$s%n");
            else
                System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s%n");
            Logger logger = Logger.getLogger(klass.getName());
            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);
            if (handler == null) {
                handler = PRINT_ON_CONSOLE ? new ConsoleHandler() : new FileHandler(logger.getName() + ".log");
            }
            handler.setLevel(Level.ALL);
            // pretty printing of messages
            SimpleFormatter simpleFormatter = new DodoLogFormatter(prependInfo);
            handler.setFormatter(simpleFormatter);
            logger.addHandler(handler);
            return logger;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static final class DodoLogFormatter extends SimpleFormatter {
        private final boolean prependInfo;

        public DodoLogFormatter(boolean prependInfo) {
            this.prependInfo = prependInfo;
        }

        @Override
        public synchronized String format(LogRecord record) {
            String className = record.getSourceClassName();
            String methodName = record.getSourceMethodName();
            int simpleNameIndex = className.lastIndexOf('.') + 1;
            String logSourceLocation = prependInfo ? String.format("%s.%s ", className.substring(simpleNameIndex), methodName) : "";
            String formattedMsg = logSourceLocation + super.format(record);

            if (record.getLevel().equals(Level.SEVERE))
                return String.format("\u001b[38;2;217;0;0mgit %s\u001b[0m", formattedMsg);
            if (record.getLevel().equals(Level.WARNING))
                return String.format("\u001b[38;2;255;150;0m%s\u001b[0m", formattedMsg);
            if (record.getLevel().equals(Level.INFO))
                return String.format("\u001b[38;2;0;32;255m%s\u001b[0m", formattedMsg);
            if (record.getLevel().equals(Level.CONFIG))
                return String.format("\u001b[38;2;0;136;255m%s\u001b[0m", formattedMsg);
            if (record.getLevel().equals(Level.FINE))
                return String.format("\u001b[38;2;0;127;35m%s\u001b[0m", formattedMsg);
            if (record.getLevel().equals(Level.FINER))
                return String.format("\u001b[38;2;77;180;100m%s\u001b[0m", formattedMsg);
            if (record.getLevel().equals(Level.FINEST))
                return String.format("\u001b[38;2;106;227;0m%s\u001b[0m", formattedMsg);
            // didn't match any of the above
            return className.substring(simpleNameIndex) + " " + formattedMsg;
        }
    }
}




