using System;
using System.Collections;
using System.Collections.Generic;
using System.Linq;
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
        public Extension(string launchKey, string canonicalName, Version version, string vendor, ISet<string> supportedLanguages,
                         string hostname="localhost", int port = 10080)
        {
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
        }

        private bool disposed = false;

        public void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        protected virtual void Dispose(bool disposing)
        {
            if (!this.disposed) {

                if (disposing) {
                    if (id > 0) {
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
                foreach(var e in list) 
                    list.Add(toAny(e));
                a.AValue = list.Cast<Any>().ToList();
                return a;
            }
            if (o is Dictionary<string, Any> map) {
                var m = new Dictionary<string,Any>();
                foreach(var k in map.Keys) {
                    if (!(k is string str))
                    {
                        throw new InvalidOperationException("Maps with non-String keys unsupported");
                    }
                    m[toAny(k).SValue] = toAny(map[k]);
                }

                a.MValue = m;
                return a;
            }
            throw new InvalidOperationException("Unsupported conversion to Any from "+o.GetType().Name);
        }
        protected long id;
        protected API.Extension.Client client;
        protected TTransport transport;
        protected TProtocol protocol;
        protected TMultiplexedProtocol extensionProtocol;
        protected TMultiplexedProtocol controllerProtocol;
        protected TMultiplexedProtocol pendantProtocol;

        protected Dictionary<long, Controller> controllerMap;
        protected Dictionary<long, Pendant> pendantMap;
    }

}
