
using System.Collections.Generic;

using Thrift.Protocol;
using Thrift.Collections;

using PendantEventType = Yaskawa.Ext.API.PendantEventType;
using UtilityWindowWidth = Yaskawa.Ext.API.UtilityWindowWidth;
using UtilityWindowHeight = Yaskawa.Ext.API.UtilityWindowHeight;
using UtilityWindowExpansion = Yaskawa.Ext.API.UtilityWindowExpansion;

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

        public void registerUtilityWindow(string identifier, bool integrated, string itemType, string menuItemName, string windowTitle, UtilityWindowWidth widthFormat, UtilityWindowHeight heightFormat, UtilityWindowExpansion sizeExpandability)
        {
            client.registerUtilityWindow(id, identifier, integrated, itemType, menuItemName, windowTitle, widthFormat, heightFormat, sizeExpandability);
        }

        public string property(string itemID, string name)
        {
            return client.property(id, itemID, name);
        }

        public void setProperty(string itemID, string name, string @value)
        {
            client.setProperty(id, itemID, name, @value);
        }

        protected Extension extension;
        protected API.Pendant.Client client;
        protected long id;
    }

}
