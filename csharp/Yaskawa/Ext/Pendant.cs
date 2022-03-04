using System;
using System.Collections.Generic;

using Thrift.Protocol;
using Thrift.Collections;
using Yaskawa.Ext.API;
using PendantEventType = Yaskawa.Ext.API.PendantEventType;

namespace Yaskawa.Ext
{

    public class Pendant
    {
        internal Pendant(Extension ext, TProtocol protocol, long id)
        {
            extension = ext;
            client = new API.Pendant.Client(protocol);
            this.id = id;
        }

        public void subscribeEventTypes(ISet<PendantEventType> types)
        {
            var ts = new THashSet<PendantEventType>();
            foreach(var t in types)
                ts.Add(t);

            client.subscribeEventTypes(id, ts);
        }

        public void unsubscribeEventTypes(ISet<PendantEventType> types)
        {
            var ts = new THashSet<PendantEventType>();
            foreach(var t in types)
                ts.Add(t);

            client.unsubscribeEventTypes(id, ts);
        }

        public List<API.PendantEvent> events()
        {
            return client.events(id);
        }

        public string currentLanguage()
        {
            return client.currentLanguage(id);
        }

        public string currentLocale()
        {
            return client.currentLocale(id);
        }

        public string currentScreenName()
        {
            return client.currentScreenName(id);
        }
 
       public List<string> registerYML(string ymlSource)
       {
           return client.registerYML(id, ymlSource);
       }

       public void registerutilitywindow(string identifier, string itemtype, string menuitemname, string windowtitle)
       {
           client.registerUtilityWindow(id, identifier, itemtype, menuitemname, windowtitle);
       }

        public string property(string itemID, string name)
        {
            //Console.WriteLine(client.property(id, itemID, name).SValue);
            return client.property(id, itemID, name).SValue;
        }

        public void setProperty(string itemID, string name, Any @value)
       {

           client.setProperty(id, itemID, name, @value);
       }

        protected Extension extension;
        protected API.Pendant.Client client;
        protected long id;
    }

}
