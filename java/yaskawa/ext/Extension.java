package yaskawa.ext;

import java.util.*;
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
    public Extension(String launchKey, String canonicalName, Version version, String vendor, Set<String> supportedLanguages,
                     String hostname, int port) throws TTransportException, Exception
    {
        transport = new TSocket(hostname, port);
        transport.open();
        protocol = new TBinaryProtocol(transport);

        extensionProtocol = new TMultiplexedProtocol(protocol, "Extension");
        controllerProtocol = new TMultiplexedProtocol(protocol, "Controller");
        pendantProtocol = new TMultiplexedProtocol(protocol, "Pendant");

        client = new yaskawa.ext.api.Extension.Client(extensionProtocol);

        // var languages = new THashSet<String>();
        // for(String language : supportedLanguages)
        //     languages.Add(language);

        id = 0;
        try {
            id = client.registerExtension(launchKey, canonicalName, version, vendor, supportedLanguages);
        } catch(Exception e) {}
        if (id == 0)
            throw new Exception("Extension registration failed.");

        controllerMap = new HashMap<Long, Controller>();
        pendantMap = new HashMap<Long, Pendant>();
    }

    public Extension(String launchKey, String canonicalName, Version version, String vendor, Set<String> supportedLanguages) throws TTransportException, Exception
    {
        this(launchKey, canonicalName, version, vendor, supportedLanguages, "localhost", 36888);
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
            controllerMap.put(cid, new Controller(this, controllerProtocol, cid));

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

    public void debug(String message) throws TException { log(LoggingLevel.Debug, message); }
    public void info(String message) throws TException { log(LoggingLevel.Info, message); }
    public void warn(String message) throws TException { log(LoggingLevel.Warn, message); }
    public void critical(String message) throws TException { log(LoggingLevel.Critical, message); }


    private static final String[] logLevelNames = { "DEBUG", "INFO", "WARN", "CRITICAL" };

    protected long id;
    protected yaskawa.ext.api.Extension.Client client;
    protected TTransport transport;
    protected TProtocol protocol;
    protected TMultiplexedProtocol extensionProtocol;
    protected TMultiplexedProtocol controllerProtocol;
    protected TMultiplexedProtocol pendantProtocol;

    protected Map<Long, Controller> controllerMap;
    protected Map<Long, Pendant> pendantMap;
}

