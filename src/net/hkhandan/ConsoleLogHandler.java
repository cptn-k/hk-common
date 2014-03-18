/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.hkhandan;

import java.io.OutputStream;
import java.util.Calendar;
import java.util.logging.*;

/**
 *
 * @author hamed
 */
public class ConsoleLogHandler extends StreamHandler {
    private static class LineFormatter extends java.util.logging.Formatter {
        private static Calendar c = Calendar.getInstance();
        public String format(LogRecord rec) {
            c.setTimeInMillis(rec.getMillis());
            String lead = String.format("%02d%02d%2d%03d%c",
                    c.get(Calendar.HOUR_OF_DAY),
                    c.get(Calendar.MINUTE),
                    c.get(Calendar.SECOND),
                    c.get(Calendar.MILLISECOND),
                    (rec.getLevel() == Level.SEVERE)?'!':' ');
            String leadspace = String.format("%" + lead.length() + "s", " ");
            StringBuffer buff = new StringBuffer();
            buff.append(lead);

            String[] msg = rec.getMessage().split("\n");
            for(int i = 0; i < msg.length; i++) {
                if(i > 0)
                    buff.append(leadspace);
                buff.append(msg[i]);
                buff.append('\n');
            }
            if(rec.getThrown() != null) {
                Throwable t = rec.getThrown();

                lead = String.format("%" + lead.length() + "s", " ");
                buff.append(leadspace);
                buff.append("<< ");
                buff.append(t.toString());
                //buff.append('>');
                buff.append(" >>\n");
                StackTraceElement[] elements = t.getStackTrace();
                for(int i = 0; i < elements.length; i++) {
                    buff.append(leadspace);
                    buff.append(elements[i].toString());
                    buff.append('\n');
                }
            }
            return buff.toString();
         }

        public String getHead() {
            return "-----------------------------------";
        }

        public String getTail() {
            return "-----------------------------------";
        }
    }

    public ConsoleLogHandler() {
        this(System.err);
    }

    public ConsoleLogHandler(OutputStream os) {
        setFormatter(new LineFormatter());
        setOutputStream(os);
    }

    @Override
    public void publish(LogRecord record) {
        super.publish(record);
        flush();
    }

    @Override
    public void close() {
        flush();
    }

}
