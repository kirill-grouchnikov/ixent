package org.jvnet.ixent.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * This class is here to provide more succinct formatting of log records
 *
 * @author Kirill Grouchnikov
 */
public class IxentLogFormatter extends Formatter {
    private SimpleDateFormat dateFormatter;

    public IxentLogFormatter() {
        this.dateFormatter = new SimpleDateFormat("HH:mm:ss.SSS");
    }

    /**
     * Format log record
     *
     * @param record log record
     * @return formatted string
     */
    public String format(LogRecord record) {
        StringBuffer sb = new StringBuffer();

        String levelName = record.getLevel().getName();
//        if (levelName.length() > 4)
//            levelName = levelName.substring(0, 3);
        sb.append(levelName);

        // format time (without the date)
        Date time = new Date(record.getMillis());
        sb.append(" [" + this.dateFormatter.format(time) + "] ");

        // remove package names from class name
        String className = record.getSourceClassName();
        int lastPointIndex = className.lastIndexOf('.');
        if (lastPointIndex >= 0) {
            className = className.substring(lastPointIndex + 1);
        }
        sb.append(className + "." + record.getSourceMethodName() + " ");

        sb.append(record.getMessage());

        sb.append('\n');

        return sb.toString();
    }
}
