using System;
using System.Collections.Generic;

using Thrift;
using Thrift.Protocol;
using Thrift.Server;
using Thrift.Transport;
using Thrift.Collections;

using Yaskawa.Ext;


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
