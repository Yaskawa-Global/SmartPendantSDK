using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Linq;
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
            eventConsumers = new Dictionary<PendantEventType, List<Action<PendantEvent>>>();
            itemEventConsumers = new Dictionary<PendantEventType, Dictionary<string, List<Action<PendantEvent>>>>();
        }

        public Version pendantVersion()
        {
            return new Version(client.pendantVersion(id));
        }
        public void subscribeEventTypes(THashSet<PendantEventType> types)
        {
            client.subscribeEventTypes(id, types);
        }

        public void unsubscribeEventTypes(THashSet<PendantEventType> types)
        {
            client.unsubscribeEventTypes(id, types);
        }

        public List<PendantEvent> events()
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
       public void registerYMLFile(String ymlFileName)
       {
           String yml = new String(File.ReadAllText(Path.GetFullPath(ymlFileName)));
           var errors = registerYML(yml);
           if (errors.Count > 0) {
               Console.WriteLine(ymlFileName+" YML Errors encountered:");
               foreach(var e in errors)
                    Console.WriteLine("  "+e);
               throw new Exception("YML Error in "+ymlFileName);
           }
       }
       public void registerImageFile(String imageFileName)
        {
            try {
                client.registerImageFile(id, imageFileName);
            } catch (Exception e) {
                // something went wrong - possible file isn't accessible from service end, so send data over API
                Console.WriteLine("file possibly isn't accessible from service end, so send data over API" + e);
                var imageBytes = File.ReadAllBytes(Path.GetFullPath(imageFileName));
                MemoryStream stream = new MemoryStream();
                using (BinaryWriter writer = new BinaryWriter(stream))
                {
                    writer.Write(imageBytes);
                }

                byte[] bytes = stream.ToArray();
                client.registerImageData(id, bytes, imageFileName);
            }
        }
        public void registerImageData(byte[] imageData, String imageName)
        {
            client.registerImageData(id, imageData, imageName);
        }

        public void registerHTMLFile(String htmlFileName)
        {
            try {
                client.registerHTMLFile(id, htmlFileName);
            } catch (Exception e) {
                // something went wrong - possible file isn't accessible from service end, so send data over API
                Console.WriteLine("file possibly isn't accessible from service end, so send data over API" + e);
                var dataBytes = File.ReadAllBytes(Path.GetFullPath(htmlFileName));
                MemoryStream dataStream = new MemoryStream();
                using (BinaryWriter writer = new BinaryWriter(dataStream))
                {
                    writer.Write(dataBytes);
                }

                byte[] bytesData = dataStream.ToArray();
                client.registerHTMLData(id, bytesData, htmlFileName);
            }
        }
        public void registerHTMLData(byte[] htmlData, String htmlName)
        {
                client.registerHTMLData(id, htmlData, htmlName);
        }
        public void registerTranslationFile(String locale, String translationFileName)
        {
            try {
                client.registerTranslationFile(id, locale, translationFileName);
            } catch (Exception e) {
                // something went wrong - possible file isn't accessible from service end, so send data over API
                Console.WriteLine("file possibly isn't accessible from service end, so send data over API" + e);
                var dataBytes = File.ReadAllBytes(Path.GetFullPath(translationFileName));
                MemoryStream translationStream = new MemoryStream();
                using (BinaryWriter writer = new BinaryWriter(translationStream))
                {
                    writer.Write(dataBytes);
                }

                byte[] bytesStream = translationStream.ToArray();
                client.registerTranslationData(id, locale, bytesStream, translationFileName);
            }
        }
        public void registerTranslationData(String locale, byte[] translationData, String translationName)
        {
            client.registerTranslationData(id, locale, translationData, translationName);
        }

        public void registerUtilityMenu(string menuName, string menuText, string menuIcon) 
        {
            client.registerUtilityMenu(id, menuName, menuText, menuIcon);
        }
        public void unregisterUtilityMenu(string menuName) 
        {
            client.unregisterUtilityMenu(id, menuName);
        }
       public void registerUtilityWindow(string identifier, string itemtype, string menuitemname, string windowtitle, string menuName) 
       {
           client.registerUtilityWindow(id, identifier, itemtype, menuitemname, windowtitle, menuName);
       }
       public void unregisterUtilityWindow(String identifier)
       {
           client.unregisterUtilityWindow(id, identifier);
       }
       public void openUtilityWindow(String identifier)
       {
            client.openUtilityWindow(id, identifier);
       }

       public void closeUtilityWindow(String identifier)
       {
           client.closeUtilityWindow(id, identifier);
       }

       public void collapseUtilityWindow(String identifier)
       {
           client.collapseUtilityWindow(id, identifier);
       }

       public void expandUtilityWindow(String identifier)
       {
           client.expandUtilityWindow(id, identifier);
       }
       public void registerIntegration(String identifier, IntegrationPoint integrationPoint, String itemType, String buttonLabel, String buttonImage) 
       {
           client.registerIntegration(id, identifier, integrationPoint, itemType, buttonLabel, buttonImage);
       }

       public void unregisterIntegration(String identifier)
       {
           client.unregisterIntegration(id, identifier);
       }
        public Any property(string itemID, string name)
        {
            //Console.WriteLine(client.property(id, itemID, name).SValue);
            return client.property(id, itemID, name);
        }

        public void setProperty(string itemID, string name, Any @value)
       {
           client.setProperty(id, itemID, name, @value);
       }
        //convenience overloads
        public void setProperty(string itemID, string name, bool @value)
        {
            Any a = new Any();
            a.BValue = @value;
            client.setProperty(id, itemID, name, a);
        }
        public void setProperty(string itemID, string name, int @value)
        {
            Any a = new Any();
            a.IValue = @value;
            client.setProperty(id, itemID, name, a);
        }
        public void setProperty(string itemID, string name, long @value)
        {
            Any a = new Any();
            a.IValue = @value;
            client.setProperty(id, itemID, name, a);
        }
        public void setProperty(string itemID, string name, double @value)
        {
            Any a = new Any();
            a.RValue = @value;
            client.setProperty(id, itemID, name, a);
        }
        public void setProperty(string itemID, string name, string @value)
        {
            Any a = new Any();
            a.SValue = @value;
            client.setProperty(id, itemID, name, a);
        }
        public void setProperty(string itemID, string name, List<object> @value)
        {
            var a = new ArrayList(@value.Count);
            foreach(var e in @value)
            {
                a.Add(Extension.toAny(e));
            }
            Any aList = new Any();
            aList.AValue = a.Cast<Any>().ToList();;
            client.setProperty(id, itemID, name, aList);
        }
        public void setProperty(string itemID, string name, object[] @value)
        {
            var a = new ArrayList(@value.Length);
            foreach(var e in @value)
            {
                a.Add(Extension.toAny(e));
            }
            Any aList = new Any();
            aList.AValue = a.Cast<Any>().ToList();;
            client.setProperty(id, itemID, name, aList);
        }
        public void setProperty(string itemID, string name, Dictionary<string, object> @value)
        {
            var m = new Dictionary<string, Any>();
            foreach(var k in m.Keys) {
                m[k] = Extension.toAny(m[k]);
            }

            Any aObj = new Any();
            aObj.MValue = m;
            client.setProperty(id, itemID, name, aObj);
        }
        public void setProperties(List<PropValue> propValues)
        {
            client.setProperties(id, this.PropValues(propValues));
        }
        
        // Convenience
        // The List<PropValues> taken by setProperties() are tedious to construct in Java,
        //  so provice convenience methods that take itemID, name, value and can be assembled into
        //  a list.
        
        public class PropValue
        {
            public PropValue(string itemId, string name, Any value)
            {
                this.ItemId = itemId;
                this.Name = name;
                this.Value = value;
            }
        
            public string ItemId;
            public string Name;
            public Any Value;
        }
        // convert from List PropValue to List<PropValues> (collects props of same item together)
        public List<PropValues> PropValues(List<PropValue> propValues)
        {
            // collect by itemID
            var m = new Dictionary<string, List<PropValue>>();
            foreach(var propValue in propValues)
            {
                var item = propValue.ItemId;
                if (!m.ContainsKey(item)) {
                    m[propValue.ItemId] = new List<PropValue>();
                }
                m[propValue.ItemId].Add(propValue);
            }
        
            // now convert to api.PropValues
            var pvl = new List<PropValues>();
            foreach(KeyValuePair<string, List<PropValue>> entry in m) {
                String itemID = entry.Key;
                var pvs = new PropValues();
                pvs.ItemID = itemID;
                var pm = new Dictionary<string, Any>();
                foreach (var p in entry.Value)
                    pm.Add(p.Name, p.Value);
                pvs.Props = pm;
                pvl.Add(pvs);
            }
            return pvl;
        }
        
        // client calls these and construcs a List.of them for setProperties()
        public static PropValue propValue(String itemID, String name, bool value)
        {
            return new PropValue(itemID, name, Extension.toAny(value));
        }
        public static PropValue propValue(String itemID, String name, int value)
        {
            return new PropValue(itemID, name, Extension.toAny((long)value));
        }
        public static PropValue propValue(String itemID, String name, long value)
        {
            return new PropValue(itemID, name, Extension.toAny(value));
        }
        public static PropValue propValue(String itemID, String name, double value)
        {
            return new PropValue(itemID, name, Extension.toAny(value));
        }
        public static PropValue propValue(String itemID, String name, String value)
        {
            return new PropValue(itemID, name, Extension.toAny(value));
        }
        public static PropValue propValue(String itemID, String name, List<Object> value)
        {
            var anylist = new Any();
            var a = new List<Any>(value.Count);
            foreach(var e in value)
                a.Add(Extension.toAny(e));
            anylist.AValue = a;
            return new PropValue(itemID, name, anylist);
        }
        public static PropValue propValue(String itemID, String name, Object[] value)
        {
            var anyObject = new Any();
            var a = new List<Any>(value.Length);
            foreach(var e in value)
                a.Add(Extension.toAny(e));
            anyObject.AValue = a;
            return new PropValue(itemID, name, anyObject);
        }
        public static PropValue propValue(String itemID, String name, Dictionary<String, Object> value)
        {
            var anyDict = new Any();
            var m = new Dictionary<String,Any>();
            foreach(var k in value.Keys)
                m[k] = Extension.toAny(value[k]);
            anyDict.MValue = m;
            return new PropValue(itemID, name, anyDict);
        }

        public void setChartConfig(String chartID, Any config)
        {
            client.setChartConfig(id, chartID, config);
        }
        public void setChartConfig(String chartID, Dictionary<String, Object> config)
        {
            Any a = new Any();
            var m = new Dictionary<String,Any>();
            foreach(var k in config.Keys) {
                m[k] = Extension.toAny(config[k]);
            }
            a.MValue = m;
            client.setChartConfig(id, chartID, a);
        }

        public void setChartData(String chartID, Dictionary<String, Data> dataset)
        {
            client.setChartData(id, chartID, dataset, false);
        }

        public void setChartData(String chartID, Dictionary<String, Data> dataset, bool right)
        {
            client.setChartData(id, chartID, dataset, right);
        }

        public Dictionary<String, Data> getChartData(String chartID)
        {
            return client.getChartData(id, chartID, false);

        }

        public Dictionary<String, Data> getChartData(String chartID, bool right)
        {
            return client.getChartData(id, chartID, right);
        }

        public void addChartKey(String chartID, String key, Data data)
        {
            client.addChartKey(id, chartID, key, data, false);
        }

        public void addChartKey(String chartID, String key, Data data, bool right)
        {
            client.addChartKey(id, chartID, key, data, right);
        }
        
        public void removeChartKey(String chartID, String key)
        {
            client.removeChartKey(id, chartID, key, false);
        }

        public void removeChartKey(String chartID, String key, bool right)
        {
            client.removeChartKey(id, chartID, key, right);
        }

        public void hideChartKey(String chartID, String key)
        {
            client.hideChartKey(id, chartID, key, true, false);
        }

        public void hideChartKey(String chartID, String key, bool hidden)
        {
            client.hideChartKey(id, chartID, key, hidden, false);
        }

        public void hideChartKey(String chartID, String key, bool hidden, bool right)
        {
            client.hideChartKey(id, chartID, key, hidden, right);
        }

        public void appendChartPoint(String chartID, String key, DataPoint pt)
        {
            List<DataPoint> ptList = new List<DataPoint>(new[] { pt });
            client.appendChartPoints(id, chartID, key, ptList, false);
        }

        public void appendChartPoint(String chartID, String key, DataPoint pt, bool right)
        {
            List<DataPoint> ptList = new List<DataPoint>(new[] { pt });
            client.appendChartPoints(id, chartID, key, ptList, right);
        }

        public void appendChartPoints(String chartID, String key, List<DataPoint> pts)
        {
            client.appendChartPoints(id, chartID, key, pts, false);
        }

        public void appendChartPoints(String chartID, String key, List<DataPoint> pts, bool right)
        {
            client.appendChartPoints(id, chartID, key, pts, right);
        }

        public void incrementChartKey(String chartID, String key)
        {
                client.incrementChartKey(id, chartID, key, 1.0);
        }

        public void decrementChartKey(String chartID, String key)
        {
                client.incrementChartKey(id, chartID, key, -1.0);
        }

        public void incrementChartKey(String chartID, String key, double value)
        {
            client.incrementChartKey(id, chartID, key, value);
        }

        public void decrementChartKey(String chartID, String key, double value)
        {
                client.incrementChartKey(id, chartID, key, -value);
        }
        public void notice(String title, String message, String log)
        {

            client.notice(id, title, message, log);
        }
        public void notice(String title, String message)
        {
            notice(title, message, "");
        }

        public void dispNotice(Disposition disposition, String title, String message, String log)
        {
            client.dispNotice(id, disposition, title, message, log);
        }
        public void dispNotice(Disposition disposition, String title, String message)
        { 
            dispNotice(disposition, title, message, ""); 
        }

        public void error(String title, String message, String log)
        {
            client.error(id, title, message, log);
        }
        public void error(String title, String message)
        { 
            error(title, message, ""); 
        }
        public void popupDialog(String identifier, String title, String message, String positiveOption, String negativeOption)
        {
            client.popupDialog(id, identifier, title, message, positiveOption, negativeOption);
        }

        public void cancelPopupDialog(String identifier)
        {
            client.cancelPopupDialog(id, identifier);
        }
        
        public String insertInstructionAtSelectedLine(String instruction)
        {
            return client.insertInstructionAtSelectedLine(id, instruction);
        }
        // event consumer functions
        public void addEventConsumer(PendantEventType eventType, Action<PendantEvent> c)
        {
            THashSet<PendantEventType> Set = new THashSet<PendantEventType>();
            Set.Add(eventType);
            var a = new List<Action<PendantEvent>>();
            if(!eventConsumers.TryGetValue(eventType, out a)){
                eventConsumers[eventType] = new List<Action<PendantEvent>>();
            }
            else
                eventConsumers[eventType].Add(c);    
            subscribeEventTypes(Set);
        }

        public void addItemEventConsumer(String itemName, PendantEventType eventType, Action<PendantEvent> c)
        {
            THashSet<PendantEventType> itemSet = new THashSet<PendantEventType>();
            itemSet.Add(eventType);
            var a = new Dictionary<string,List<Action<PendantEvent>>>();
            var b = new List<Action<PendantEvent>>();
            if (!itemEventConsumers.TryGetValue(eventType, out a))
            {
                itemEventConsumers[eventType] = new Dictionary<string, List<Action<PendantEvent>>>();
            }
            var consumers = itemEventConsumers[eventType];
            if (!consumers.TryGetValue(itemName, out b))
                consumers[itemName] = new List<Action<PendantEvent>>();
            consumers[itemName].Add(c);
            subscribeEventTypes(itemSet);
        }

        // invoke consumer callbacks relevant to event
        public void handleEvent(PendantEvent e)
        {
            // an event we have a consumer for?
            if (eventConsumers.ContainsKey(e.EventType)) 
            {
                foreach(Action<PendantEvent> consumer in eventConsumers[e.EventType]) 
                    consumer.Invoke(e);
            }

            // is this event from a YML item?   
            var props = e.Props;     
            if (e.__isset.props && (props.ContainsKey("item") || props.ContainsKey("identifier"))) {
                // do we have a consumer for this event type & item ?
                if (itemEventConsumers.ContainsKey(e.EventType)) {
                    var consumers = itemEventConsumers[e.EventType];
                    String itemName = props.ContainsKey("item") ? props["item"].SValue : props["identifier"].SValue;
                    if (consumers.ContainsKey(itemName)) {                    
                        foreach(Action<PendantEvent> consumer in consumers[itemName]) 
                            consumer.Invoke(e);
                    }
                }
            }
        }
        public void displayScreen(String identifier)
        {
            client.displayScreen(id, identifier);
        }
        protected Extension extension;
        protected API.Pendant.Client client;
        protected long id;
        protected Dictionary<PendantEventType, List<Action<PendantEvent>>> eventConsumers;

        protected Dictionary<PendantEventType, Dictionary<string, List<Action<PendantEvent>>>>
            itemEventConsumers;
    }

}
