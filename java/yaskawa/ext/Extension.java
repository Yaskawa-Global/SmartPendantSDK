package yaskawa.ext;

import java.util.*;
import java.util.function.*;
import java.nio.file.*;
import java.io.*;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;

import yaskawa.ext.api.*;


public class Extension 
{
    /** 
     * Connect to the Extension SDK API server & register.
     * Extensions running on the pendant hardware, or on a desktop PC connecting to a mock pendant app on the
     *  *same* PC can just pass "", -1 for hostname & port.
     * For connecting to a remote pendant hardware or app over the network, use an approproate
     *  name or IP for hostname and 10080 or 20080 for the port.
     */
    public Extension(String canonicalName, Version version, String vendor, Set<String> supportedLanguages,
                     String hostname, int port) throws TTransportException, IllegalArgument, Exception
    {
        boolean runningInPendantContainer = false;

        // Look for launch key file in pendant container
        //  (also an indication we're running on the pendant)
        String launchKey = "";
        try {
            // If launchKey file exists, read it to get launchKey
            //and assume we're running in a pendant container
            String launchKeyFilePath = "/extensionService/launchKey";
            File launchKeyFile = new File(launchKeyFilePath);
            if (launchKeyFile.exists() && launchKeyFile.isFile()) {
                launchKey = new String(Files.readAllBytes(Paths.get(launchKeyFilePath)));
                runningInPendantContainer = true;
            }
        } catch (Exception e) {}

        if (runningInPendantContainer) {
            // if on the pendant, ignore passed host & port
            hostname = "10.0.3.1";
            port = 20080;
        }
        else {
            // not in pendant container, if host and/or port not
            //  supplied use default for connecting to mock pendant app
            //  on same host
            if (hostname == "")
                hostname = "localhost";
            if (port <= 0)
                port = 10080;
        }

        transport = new TSocket(hostname, port);
        transport.open();
        protocol = new TBinaryProtocol(transport);

        extensionProtocol = new TMultiplexedProtocol(protocol, "Extension");
        controllerProtocol = new TMultiplexedProtocol(protocol, "Controller");
        pendantProtocol = new TMultiplexedProtocol(protocol, "Pendant");
        robotProtocol = new TMultiplexedProtocol(protocol, "Robot");

        client = new yaskawa.ext.api.Extension.Client(extensionProtocol);

        id = 0;
        try {
            // API call
            id = client.registerExtension(launchKey, canonicalName, version, vendor, supportedLanguages);

        } catch(IllegalArgument a) {
            throw new Exception("Extension registration failed - registerExtension() responded with illegal argument exception (check launchKey & canonicalName; extention already registered/running?): "+((a.getMessage()==null)?"":a.getMessage()));
        } catch(Exception e) {
            throw new Exception("Extension registration failed: "+e.getMessage());
        }
        if (id == 0)
            throw new Exception("Extension registration failed.");

        controllerMap = new HashMap<Long, Controller>();
        pendantMap = new HashMap<Long, Pendant>();

        loggingConsumers = new ArrayList<Consumer<yaskawa.ext.api.LoggingEvent>>();
    }

    public Extension(String canonicalName, Version version, String vendor, Set<String> supportedLanguages) throws TTransportException, IllegalArgument, Exception
    {
        this(canonicalName, version, vendor, supportedLanguages, "", -1);
    }

    public void close() 
    {
        try {            
            if (id > 0) {
                synchronized(this) {
                    client.unregisterExtension(id);
                    transport.close();
                }
            }
        } catch (Exception e) {}
    }


    public Version apiVersion() throws TException
    {
        synchronized(this) {
            return new Version(client.apiVersion());
        }
    }

    public void ping() throws TException, InvalidID
    {
        synchronized(this) {
            client.ping(id);
        }
    }

    public Controller controller() throws TException
    {
        synchronized(this) {
            var cid = client.controller(id);
            if (!controllerMap.containsKey(cid))
                controllerMap.put(cid, new Controller(this, controllerProtocol, robotProtocol, cid));

            return controllerMap.get(cid);
        }
    }

    public Pendant pendant() throws TException, InvalidID
    {
        synchronized(this) {
            var pid = client.pendant(id);
            if (!pendantMap.containsKey(pid))
                pendantMap.put(pid, new Pendant(this, pendantProtocol, pid));

            return pendantMap.get(pid);
        }
    }


    public void log(LoggingLevel level, String message) throws TException
    {
        synchronized(this) {
            client.log(id, level, message);
            if (copyLoggingToStdOutput) 
                System.out.println(logLevelNames[level.getValue()]+": "+message);
        }
    }


    public void subscribeLoggingEvents() throws TException
    {
        synchronized(this) {
            client.subscribeLoggingEvents(id);
        }
    }

    public void unsubscribeLoggingEvents() throws TException
    {
        synchronized(this) {
            client.unsubscribeLoggingEvents(id);
        }
    }

    public List<LoggingEvent> logEvents() throws TException
    {
        synchronized(this) {
            return client.logEvents(id);
        }
    }

    public List<storageInfo> listAvailableStorage() throws TException
    {
        synchronized(this) {
            return client.listAvailableStorage(id);
        }
    }

    public List<String> listFiles(String path) throws TException
    {
        synchronized(this) {
            return client.listFiles(id, path);
        }
    }

    public long openFile(String path, String flag) throws TException
    {
        synchronized(this) {
            return client.openFile(id, path, flag);
        }
    }

    public void closeFile(long filehandle) throws TException
    {
        synchronized(this) {
            client.closeFile(id, filehandle);
        }
    }

    public boolean isOpen(long filehandle) throws TException
    {
        synchronized(this) {
            return client.isOpen(id, filehandle);
        }
    }

    public String read(long filehandle) throws TException
    {
         synchronized(this) {
	     return client.read(id, filehandle);
        }
    }

    public java.lang.String readChunk(long filehandle, long offset, long len) throws TException
    {
         synchronized(this) {
	     return client.readChunk(id, filehandle, offset, len);
        }
    }

    public void write(long filehandle, String data) throws TException
    {
         synchronized(this) {
	     client.write(id, filehandle, data);
        }
    }
  
    public void flush(long filehandle) throws TException
    {
         synchronized(this) {
	     client.flush(id, filehandle);
        }
    }
    

    Object lockObject() {
        return this;
    }

    // convenience
    public boolean copyLoggingToStdOutput = false;
    public boolean outputEvents = false;

    public void debug(String message) throws TException { log(LoggingLevel.Debug, message); }
    public void info(String message) throws TException { log(LoggingLevel.Info, message); }
    public void warn(String message) throws TException { log(LoggingLevel.Warn, message); }
    public void critical(String message) throws TException { log(LoggingLevel.Critical, message); }


    public void addLoggingConsumer(Consumer<yaskawa.ext.api.LoggingEvent> c) throws TException
    {
        loggingConsumers.add(c);
    }



    public void run(BooleanSupplier stopWhen) throws InvalidID, TException, IllegalArgument, RuntimeException
    {
        boolean stop = false;
        do {
            boolean recievedShutdownEvent = false;

            for (Long c : controllerMap.keySet()) {
                Controller controller = controllerMap.get(c);

                for (ControllerEvent e : controller.events()) {
                    if (outputEvents) {
                        System.out.print("ControllerEvent:"+e.eventType);
                        if (e.isSetProps()) {
                            var props = e.getProps();
                            for(var prop : props.entrySet()) 
                                System.out.print("   "+prop.getKey()+":"+prop.getValue().toString());
                        }
                        System.out.println();
                    }
                    controller.handleEvent(e);
                }
            }
    
            for (Long p : pendantMap.keySet()) {
                Pendant pendant = pendantMap.get(p);

                for (PendantEvent e : pendant.events()) {
                    if (outputEvents) {
                        System.out.print("PendantEvent:"+e.eventType);
                        if (e.isSetProps()) {
                            var props = e.getProps();
                            for(var prop : props.entrySet()) 
                                System.out.print("  "+prop.getKey()+": "+prop.getValue().toString());
                        }
                        System.out.println();
                    }
                    pendant.handleEvent(e);

                    recievedShutdownEvent = (e.getEventType() == PendantEventType.Shutdown);
                }    
            }

            if (loggingConsumers.size() > 0) {
                for(var event : logEvents()) {
                    for (var consumer : loggingConsumers)
                        consumer.accept(event);
                }
            }

            stop = stopWhen.getAsBoolean() || recievedShutdownEvent;
            try { 
                if (!stop)
                    Thread.sleep(200); 
            } catch (InterruptedException ex) { 
                stop = true; 
            }

        } while (!stop);
    }


    public static Any toAny(Object o)
    {
        if (o instanceof Boolean)
            return Any.bValue((Boolean)o);
        else if (o instanceof Integer)
            return Any.iValue((Integer)o);
        else if (o instanceof Long)
            return Any.iValue((Long)o);
        else if (o instanceof Double)
            return Any.rValue((Double)o);
        else if (o instanceof String)
            return Any.sValue((String)o);
        else if (o instanceof Position)
            return Any.pValue((Position)o);
        else if (o instanceof List) {
            var a = new ArrayList<Any>( ((List)o).size() );
            for(var e : (List)o)
                a.add(toAny(e));
            return Any.aValue(a);
        }
        else if (o instanceof Map) {
            Map map = (Map)o;
            var m = new HashMap<String,Any>();
            for(Object k : map.keySet()) {
                if (!(k instanceof String))
                    throw new RuntimeException("Maps with non-String keys unsupported");
                m.put(toAny(k).getSValue(), toAny(map.get(k)));
            }
            return Any.mValue(m);
        }
        throw new RuntimeException("Unsupported conversion to Any from "+o.getClass().getSimpleName());
    }




    private static final String[] logLevelNames = { "DEBUG", "INFO", "WARN", "CRITICAL" };

    protected long id;
    protected yaskawa.ext.api.Extension.Client client;
    protected TTransport transport;
    protected TProtocol protocol;
    protected TMultiplexedProtocol extensionProtocol;
    protected TMultiplexedProtocol controllerProtocol;
    protected TMultiplexedProtocol pendantProtocol;
    protected TMultiplexedProtocol robotProtocol;

    protected Map<Long, Controller> controllerMap;
    protected Map<Long, Pendant> pendantMap;

    protected ArrayList<Consumer<yaskawa.ext.api.LoggingEvent>> loggingConsumers;

}

