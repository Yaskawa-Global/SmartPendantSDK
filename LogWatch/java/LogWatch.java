import java.io.IOException;
import java.util.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import yaskawa.ext.api.IllegalArgument;
import yaskawa.ext.api.LoggingEvent;
import yaskawa.ext.api.LoggingLevel;

import yaskawa.ext.*;

import java.util.function.*;



public class LogWatch {

    public LogWatch() throws TTransportException, Exception
    {
        var version = new Version(1,0,0);
        var languages = Set.of("en");

        extension = new Extension("",
                                  "yii.logwatch", 
                                  version, "Yaskawa", languages,
                                  "localhost", -1);

        extension.addLoggingConsumer(this::onLoggingEvent);
        extension.subscribeLoggingEvents();
    }

    protected Extension extension;


    public void run() throws TException, IOException
    {
        // Query the verson of the SP API we're communicating with:
        System.out.println("LogWatch - Connected API version: "+extension.apiVersion());
        
        // run 'forever' (or until API service shutsdown, or fatal exception)
        try {
            extension.run(() -> false);
        } catch (Exception e) {
            System.out.println("Exception: "+e.toString());
            extension.close();
            System.out.println("Disconnected.");
        }
    }

    void onLoggingEvent(LoggingEvent e)
    {
        String level = "LOG";
        if (e.level == LoggingLevel.Debug)
            level = "DEBUG";
        else if (e.level == LoggingLevel.Info)
            level = "INFO";
        else if (e.level == LoggingLevel.Warn)
            level = "WARN";
        else if (e.level == LoggingLevel.Critical)
            level = "CRITICAL";

        System.out.println(e.datetime+" "+level+": "+e.entry);
    }


    public static void main(String[] args) 
    {
        while (true) {
            try {

                var logWatch = new LogWatch();
                logWatch.run();

            } catch (Exception e) {
                boolean connectionFailure = (e != null) && (e.getCause() != null) && 
                                              (e.getCause().getClass().getSimpleName().equals("ConnectException"));
                if (!connectionFailure)
                    System.out.println("Exception: "+e.toString());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ie) {}
            }
        }

    }

}



