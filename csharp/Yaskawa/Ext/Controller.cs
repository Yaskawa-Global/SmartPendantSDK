
using System.Collections.Generic;

using Thrift.Protocol;
using Thrift.Collections;

using ControllerEventType = Yaskawa.Ext.API.ControllerEventType;


namespace Yaskawa.Ext
{

    public class Controller
    {
        internal Controller(Extension ext, TProtocol protocol, long id)
        {
            extension = ext;
            client = new API.Controller.Client(protocol);
            this.id = id;
        }

        public void connect(string hostName)
        {
            client.connect(id, hostName);
        }

        public void disconnect()
        {
            client.disconnect(id);
        }

        public void subscribeEventTypes(ISet<ControllerEventType> types)
        {
            var ts = new THashSet<ControllerEventType>();
            foreach(var t in types)
                ts.Add(t);

            client.subscribeEventTypes(id, ts);
        }

        public void unsubscribeEventTypes(ISet<ControllerEventType> types)
        {
            var ts = new THashSet<ControllerEventType>();
            foreach(var t in types)
                ts.Add(t);

            client.unsubscribeEventTypes(id, ts);
        }

        public List<API.ControllerEvent> events()
        {
            return client.events(id);
        }

        public bool connected()
        {
            return client.connected(id);
        }

        //string connectedHostName(long c);
        public string softwareVersion()
        {
            return client.softwareVersion(id);
        }

        public bool monitoring()
        {
            return client.monitoring(id);
        }

        public bool haveExclusiveControl()
        {
            return client.haveExclusiveControl(id);
        }

        //Dictionary<sbyte, int> robots(long c);
        public int currentRobot()
        {
            return client.currentRobot(id);
        }


        protected Extension extension;
        protected API.Controller.Client client;
        protected long id;
    }

}
