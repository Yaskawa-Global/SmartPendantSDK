package yaskawa.ext;

import java.util.*;
import java.util.function.*;

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
     * Local Extension clients can pass "" for the hostname and -1 for the port for appropriate defaults, 
     * Remote clients can pass the IP address for hostname and -1 for the port for the appropriate default port.
     */
    public Extension(String launchKey, String canonicalName, Version version, String vendor, Set<String> supportedLanguages,
                     String hostname, int port) throws TTransportException, Exception
    {
        // If client is instantiated from within the Smart Pendant hardware environment, it should connect
        //  to localhost:10080.  However, if the client is on the external network and the pendant is in Development
        //  mode, the client should connect to <ip>:20080 on the LAN2 port, which is proxied to port 10080 at the pendant.
        // Assume that if the hostname is "", we're running on the hardware.
        
        boolean localClient = (hostname == "") || (hostname == "localhost") || (hostname == "127.0.0.1") || (hostname == "::1");

        transport = new TSocket(localClient ? "localhost" : hostname, 
                                port > 0 ? port : (localClient ? 10080 : 10080));
        transport.open();
        protocol = new TBinaryProtocol(transport);

        extensionProtocol = new TMultiplexedProtocol(protocol, "Extension");
        controllerProtocol = new TMultiplexedProtocol(protocol, "Controller");
        pendantProtocol = new TMultiplexedProtocol(protocol, "Pendant");
        robotProtocol = new TMultiplexedProtocol(protocol, "Robot");

        client = new yaskawa.ext.api.Extension.Client(extensionProtocol);

        id = 0;
        try {
            id = client.registerExtension(launchKey, canonicalName, version, vendor, supportedLanguages);
        } catch(Exception e) {
            throw new Exception("Extension registration failed: "+e.getMessage());
        }
        if (id == 0)
            throw new Exception("Extension registration failed.");

        controllerMap = new HashMap<Long, Controller>();
        pendantMap = new HashMap<Long, Pendant>();
    }

    public Extension(String launchKey, String canonicalName, Version version, String vendor, Set<String> supportedLanguages) throws TTransportException, Exception
    {
        this(launchKey, canonicalName, version, vendor, supportedLanguages, "", -1);
    }

    protected void close() 
    {
        try {            
            if (id > 0) {
                client.unregisterExtension(id);
                transport.close();
            }
        } catch (Exception e) {}
    }


    public Version apiVersion() throws TException
    {
        return new Version(client.apiVersion());
    }

    public void ping() throws TException, InvalidID
    {
        client.ping(id);
    }

    public Controller controller() throws TException
    {
        var cid = client.controller(id);
        if (!controllerMap.containsKey(cid))
            controllerMap.put(cid, new Controller(this, controllerProtocol, robotProtocol, cid));

        return controllerMap.get(cid);
    }

    public Pendant pendant() throws TException, InvalidID
    {
        var pid = client.pendant(id);
        if (!pendantMap.containsKey(pid))
            pendantMap.put(pid, new Pendant(this, pendantProtocol, pid));

        return pendantMap.get(pid);
    }


    public void log(LoggingLevel level, String message) throws TException
    {
        client.log(id, level, message);
        if (copyLoggingToStdOutput) 
            System.out.println(logLevelNames[level.getValue()]+": "+message);        
    }


    // convenience
    public boolean copyLoggingToStdOutput = false;
    public boolean outputEvents = false;

    public void debug(String message) throws TException { log(LoggingLevel.Debug, message); }
    public void info(String message) throws TException { log(LoggingLevel.Info, message); }
    public void warn(String message) throws TException { log(LoggingLevel.Warn, message); }
    public void critical(String message) throws TException { log(LoggingLevel.Critical, message); }


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

            stop = stopWhen.getAsBoolean() || recievedShutdownEvent;
            try { 
                if (!stop)
                    Thread.sleep(200); 
            } catch (InterruptedException ex) { 
                stop = true; 
            }

        } while (!stop);
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
}

