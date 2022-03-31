using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net.Mail;
using System.Threading;
using Thrift;
using Thrift.Protocol;
using Thrift.Server;
using Thrift.Transport;
using Thrift.Collections;

using Yaskawa.Ext.API;


namespace Yaskawa.Ext
{

    public class Extension
    {
        private static string[] logLevelNames = {"DEBUG", "INFO", "WARN", "CRITICAL"};
        protected long id;
        protected API.Extension.Client client;
        protected TTransport transport;
        protected TProtocol protocol;
        protected TMultiplexedProtocol extensionProtocol;
        protected TMultiplexedProtocol controllerProtocol;
        protected TMultiplexedProtocol pendantProtocol;

        protected Dictionary<long, Controller> controllerMap;
        protected Dictionary<long, Pendant> pendantMap;
        protected List<Action<LoggingEvent>> loggingConsumers;
        public Extension(string canonicalName, Version version, string vendor, ISet<string> supportedLanguages,
                         string hostname, int port)
        {
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
            catch (Exception _e)
            {
                Console.WriteLine(_e);
            }
            if (runningInPendantContainer)
            {
                // if on the pendant, ignore passed host & port
                hostname = "10.0.3.1";
                port = 20080;
            }
            else
            {
                // not in pendant container, if host and/or port not
                //  supplied use default for connecting to mock pendant app
                //  on same host
                if (hostname == "")
                    hostname = "localhost";
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
            transport = new TSocket(hostname, port);
            transport.Open();
            protocol = new TBinaryProtocol(transport);

            extensionProtocol = new TMultiplexedProtocol(protocol, "Extension");
            controllerProtocol = new TMultiplexedProtocol(protocol, "Controller");
            pendantProtocol = new TMultiplexedProtocol(protocol, "Pendant");

            client = new API.Extension.Client(extensionProtocol);

            var languages = new THashSet<string>();
            foreach(var language in supportedLanguages)
                languages.Add(language);
            id = client.registerExtension(launchKey, canonicalName, version, vendor, languages);
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
                        client.unregisterExtension(id);
                        transport.Close();
                    }
                }
                disposed = true;
            }
        }
        public Version apiVersion()
        {
            return new Version(client.apiVersion());
        }

        public void ping()
        {
            client.ping(id);
        }

        public Controller controller()
        {
            var cid = client.controller(id);
            if (!controllerMap.ContainsKey(cid))
                controllerMap[cid] = new Controller(this, controllerProtocol, cid);

            return controllerMap[cid];
        }

        public Pendant pendant()
        {
            var pid = client.pendant(id);
            if (!pendantMap.ContainsKey(pid))
                pendantMap[pid] = new Pendant(this, pendantProtocol, pid);

            return pendantMap[pid];
        }
        public void log(LoggingLevel level, String message)
        {
            client.log(id, level, message);
            if (copyLoggingToStdOutput) 
                Console.WriteLine(logLevelNames.GetValue((int)level)+": "+message);
        }
        public void subscribeLoggingEvents()
        {
            client.subscribeLoggingEvents(id);
        }
        public void unsubscribeLoggingEvents()
        {
            client.unsubscribeLoggingEvents(id);
        }

        public List<LoggingEvent> logEvents()
        {
            return client.logEvents(id);
        }

        Object lockObject() {
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
            Any a = new Any();
            if (o is bool b)
            {
                a.BValue = b;
                return a;
            }

            if (o is int i)
            {
                a.IValue = i;
                return a;
            }

            if (o is long l)
            {
                a.IValue = l;
                return a;
            }

            if (o is double d)
            {
                a.RValue = d;
                return a;
            }

            if (o is string s)
            {
                a.SValue = s;
                return a;
            }

            if (o is Position position)
            {
                a.PValue = position;
                return a;
            }

            if (o is List<object> objects)
            {
                var list = new ArrayList(objects.Count);
                foreach (var e in list)
                    list.Add(toAny(e));
                a.AValue = list.Cast<Any>().ToList();
                return a;
            }

            if (o is Dictionary<string, Any> map)
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

            throw new InvalidOperationException("Unsupported conversion to Any from " + o.GetType().Name);
        }
    }

}
