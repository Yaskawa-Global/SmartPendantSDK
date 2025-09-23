using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading;
using Thrift;
using Thrift.Protocol;
using Thrift.Transport;
using Thrift.Transport.Client;
using Thrift.Collections;
using Thrift.Processor;

using Yaskawa.Ext.API;
using Thrift.Transport.Server;
using System.Net;

namespace Yaskawa.Ext
{

    public class Extension
    {
        private static string[] logLevelNames = {"DEBUG", "INFO", "WARN", "CRITICAL"};
        protected long id;
        protected API.Extension.Client client;
        public object SyncRoot
        {
            get;
            private set;
        }
        protected TConfiguration Configuration = null;  // new TConfiguration() if  needed
        protected TTransport transport;
        protected TProtocol protocol;
        protected TMultiplexedProtocol extensionProtocol;
        protected TMultiplexedProtocol controllerProtocol;
        protected TMultiplexedProtocol pendantProtocol;
        protected TMultiplexedProtocol robotProtocol;

        protected Dictionary<long, Controller> controllerMap;
        protected Dictionary<long, Pendant> pendantMap;
        protected List<Action<LoggingEvent>> loggingConsumers;
        public Extension(string canonicalName, Version version, string vendor, ISet<string> supportedLanguages,
                         string hostname, int port)
        {
            SyncRoot = new object();

            bool runningInPendantContainer = false;

            // Look for launch key file in pendant container
            //  (also an indication we're running on the pendant)
            string launchKey = "";
            try
            {
                // If launchKey file exists, read it to get launchKey
                //and assume we're running in a pendant container
                String launchKeyFilePath = "/extensionService/launchKey";
                if (File.Exists(launchKeyFilePath))
                {
                    launchKey = File.ReadAllText(launchKeyFilePath);
                    runningInPendantContainer = true;
                }
            }
            catch (Exception e)
            {
                Console.WriteLine(e);
            }
            if (runningInPendantContainer)
            {
                // if on the pendant, ignore passed host & port
                hostname = "10.0.0.4";
                port = 10080;
            }
            else
            {
                // not in pendant container, if host and/or port not
                //  supplied use default for connecting to mock pendant app
                //  on same host
                if (hostname == "" || hostname == "localhost")
                    hostname = "127.0.0.1";
                if (port <= 0)
                    port = 10080;
            }
            Console.WriteLine(hostname);
            Console.WriteLine(port);
            Console.WriteLine(launchKey);
            Console.WriteLine(canonicalName);
            Console.WriteLine(version);
            Console.WriteLine(vendor);
            Console.WriteLine(string.Join(",", supportedLanguages));
            
            Console.WriteLine("running in container? "+ runningInPendantContainer);

            Configuration = new TConfiguration();
            IPAddress ipaddr = IPAddress.Parse(hostname);
            //transport = new TSocketTransport(hostname, port, Configuration);
            transport = new TSocketTransport(ipaddr, port, Configuration);
            
            transport.OpenAsync().Wait();
            Console.WriteLine("Transport Socket opened: " + transport.IsOpen.ToString());
            protocol = new TBinaryProtocol(transport);

            extensionProtocol = new TMultiplexedProtocol(protocol, "Extension");
            controllerProtocol = new TMultiplexedProtocol(protocol, "Controller");
            pendantProtocol = new TMultiplexedProtocol(protocol, "Pendant");
            robotProtocol = new TMultiplexedProtocol(protocol, "Robot");

            lock (this.SyncRoot)
                client = new API.Extension.Client(extensionProtocol);

            var languages = new HashSet<string>();
            foreach(var language in supportedLanguages)
                languages.Add(language);
            lock (this.SyncRoot)
                id = client.registerExtension(launchKey, canonicalName, version, vendor, languages).Result;
            if (id == 0)
                throw new Exception("Extension registration failed.");

            controllerMap = new Dictionary<long, Controller>();
            pendantMap = new Dictionary<long, Pendant>();

            loggingConsumers = new List<Action<LoggingEvent>>();
        }

        private bool disposed = false;

        public void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        protected virtual void Dispose(bool disposing)
        {
            if (!this.disposed) 
            {
                if (disposing) 
                {
                    if (id > 0) 
                    {
                        lock (this.SyncRoot)
                            client.unregisterExtension(id).Wait();
                        transport.Close();
                    }
                }
                disposed = true;
            }
        }
        public Version apiVersion()
        {
            lock (this.SyncRoot)
                return new Version(client.apiVersion().Result);
        }

        public void ping()
        {
            lock (this.SyncRoot)
                client.ping(id).Wait();
        }

        public Controller controller()
        {
            lock (this.SyncRoot)
            {
                var cid = client.controller(id).Result;
                if (!controllerMap.ContainsKey(cid))
                    controllerMap[cid] = new Controller(this, controllerProtocol, robotProtocol, cid);

                return controllerMap[cid];
            }
        }

        public Pendant pendant()
        {
            lock (this.SyncRoot)
            {
                var pid = client.pendant(id).Result;
                if (!pendantMap.ContainsKey(pid))
                    pendantMap[pid] = new Pendant(this, pendantProtocol, pid);

                return pendantMap[pid];
            }
        }
        public void log(LoggingLevel level, String message)
        {
            lock (this.SyncRoot)
                client.log(id, level, message).Wait();
            if (copyLoggingToStdOutput) 
                Console.WriteLine(logLevelNames.GetValue((int)level)+": "+message);
        }
        public void subscribeLoggingEvents()
        {
            lock (this.SyncRoot)
                client.subscribeLoggingEvents(id).Wait();
        }
        public void unsubscribeLoggingEvents()
        {
            lock (this.SyncRoot)
                client.unsubscribeLoggingEvents(id).Wait();
        }

        public List<LoggingEvent> logEvents()
        {
            lock (this.SyncRoot)
                return client.logEvents(id).Result;
        }

        public List<storageInfo> listAvailableStorage()
        {
            lock (this.SyncRoot)
                return client.listAvailableStorage(id).Result;
        }

        public List<String> listFiles(String path)
        {
            lock (this.SyncRoot)
                return client.listFiles(id, path).Result;
        }

        public long openFile(String path, String flag)
        {
            lock (this.SyncRoot)
                return client.openFile(id, path, flag).Result;
        }

        public void closeFile(long filehandle)
        {
            lock (this.SyncRoot)
                client.closeFile(id, filehandle).Wait();
        }

        public bool isOpen(long filehandle)
        {
            lock (this.SyncRoot)
                return client.isOpen(id, filehandle).Result;
        }

        public String read(long filehandle)
        {
            lock (this.SyncRoot)
                return client.read(id, filehandle).Result;
        }

        public String readChunk(long filehandle, long offset, long len)
        {
            lock (this.SyncRoot)
                return client.readChunk(id, filehandle, offset, len).Result;
        }

        public void write(long filehandle, String data)
        {
            lock (this.SyncRoot)
                client.write(id, filehandle, data).Wait();
        }

        public void flush(long filehandle)
        {
            lock (this.SyncRoot)
                client.flush(id, filehandle).Wait();
        }

        object lockObject() {
            return this;
        }

        // convenience
        public bool copyLoggingToStdOutput = false;
        public bool outputEvents = false;

        public void debug(string message) { log(LoggingLevel.Debug, message); }
        public void info(string message) { log(LoggingLevel.Info, message); }
        public void warn(string message) { log(LoggingLevel.Warn, message); }
        public void critical(string message) { log(LoggingLevel.Critical, message); }
        public delegate bool BooleanSupplier();

        public void addLoggingConsumer(Action<LoggingEvent> c)
        {
            loggingConsumers.Add(c);
        }
        public void run(BooleanSupplier stopWhen)
        {
            bool stop = false;
            do {
                bool recievedShutdownEvent = false;

                foreach(long c in controllerMap.Keys) {
                    Controller controller = controllerMap[c];

                    foreach(ControllerEvent e in controller.events()) {
                        if (outputEvents) {
                            Console.Write("ControllerEvent:"+e.EventType);
                            if (e.__isset.props) {
                                var props = e.Props;
                                foreach(KeyValuePair<string,Any> prop in props) 
                                    Console.Write("   "+prop.Key+":"+prop.Value);
                            }
                            Console.WriteLine();
                        }
                        controller.handleEvent(e);
                    }
                }
        
                foreach(long p in pendantMap.Keys) {
                    Pendant pendant = pendantMap[p];

                    foreach(PendantEvent e in pendant.events()) {
                        if (outputEvents) {
                            Console.Write("PendantEvent:"+e.EventType); 
                            if (e.__isset.props) {
                                var props = e.Props;
                                foreach(var prop in props) 
                                    Console.Write("  "+prop.Key+": "+prop.Value);
                            }
                            Console.WriteLine();
                        }
                        pendant.handleEvent(e);

                        recievedShutdownEvent = (e.EventType == PendantEventType.Shutdown);
                    }    
                }
                if (!loggingConsumers.Any()) {
                    foreach(var _event in logEvents()) {
                        foreach(var consumer in loggingConsumers)
                            consumer.Invoke(_event);
                    }
                }

                stop = stopWhen() || recievedShutdownEvent;
                try { 
                    if (!stop)
                        Thread.Sleep(200); 
                } catch (ThreadInterruptedException ex) {
                    Console.WriteLine(ex);
                    stop = true; 
                }

            } while (!stop);
        }

        public static Any toAny(object o)
        {
            if (o is Any)
                return (Any)o;

            Any a = new Any();
            switch (o)
            {
                case bool b:
                    a.BValue = b;
                    return a;
                case int i:
                    a.IValue = i;
                    return a;
                case long l:
                    a.IValue = l;
                    return a;
                case double d:
                    a.RValue = d;
                    return a;
                case string s:
                    a.SValue = s;
                    return a;
                case Position position:
                    a.PValue = position;
                    return a;
                case List<object> objects:
                {
                    var list = new ArrayList(objects.Count);
                    foreach (var e in list)
                        list.Add(toAny(e));
                    a.AValue = list.Cast<Any>().ToList();
                    return a;
                }
                case Dictionary<string, Any> map:
                {
                    var m = new Dictionary<string, Any>();
                    foreach (var k in map.Keys)
                    {
                        if (!(k is string str))
                        {
                            throw new InvalidOperationException("Maps with non-String keys unsupported");
                        }

                        m[toAny(k).SValue] = toAny(map[k]);
                    }

                    a.MValue = m;
                    return a;
                }
                default:
                    throw new InvalidOperationException("Unsupported conversion to Any from " + o.GetType().Name);
            }
        }
    }

}
