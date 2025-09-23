using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using Thrift.Protocol;
using Thrift.Collections;
using Yaskawa.Ext.API;
using PendantEventType = Yaskawa.Ext.API.PendantEventType;
using Thrift.Protocol.Entities;

namespace Yaskawa.Ext
{

    public class Pendant
    {
        internal Pendant(Extension ext, TProtocol protocol, long id)
        {
            extension = ext;

            lock (extension.SyncRoot)
                client = new API.Pendant.Client(protocol);

            this.id = id;
            eventConsumers = new Dictionary<PendantEventType, List<Action<PendantEvent>>>();
            itemEventConsumers = new Dictionary<PendantEventType, Dictionary<string, List<Action<PendantEvent>>>>();
        }

        public Version pendantVersion()
        {
            lock (extension.SyncRoot)
                return new Version(client.pendantVersion(id).Result);
        }

        public void subscribeEventTypes(HashSet<PendantEventType> types)
        {
            lock (extension.SyncRoot)
                client.subscribeEventTypes(id, types).Wait();
        }

        public void unsubscribeEventTypes(HashSet<PendantEventType> types)
        {
            lock (extension.SyncRoot)
                client.unsubscribeEventTypes(id, types).Wait();
        }

        public void subscribeItemEventTypes(HashSet<String> itemIDs, HashSet<PendantEventType> types)
        {
            lock (extension.SyncRoot)
                client.subscribeItemEventTypes(id, itemIDs, types).Wait();
        }

        public void unsubscribeItemEventTypes(HashSet<String> itemIDs, HashSet<PendantEventType> types)
        {
            foreach(PendantEventType type in types)
            {
                var a = new Dictionary<string, List<Action<PendantEvent>>>();
                var b = new List<Action<PendantEvent>>();
                if (itemEventConsumers.TryGetValue(type, out a))
                {
                    foreach(string item in itemIDs)
                    {
                        if (a.TryGetValue(item, out b))
                        {
                            itemEventConsumers[type].Remove(item);
                        }

                    }
                }
            }

            lock (extension.SyncRoot)
                client.unsubscribeItemEventTypes(id, itemIDs, types).Wait();
        }

		public List<PendantEvent> events()
        {
            lock (extension.SyncRoot)
                return client.events(id).Result;
        }

        public string currentLanguage()
        {
            lock (extension.SyncRoot)
                return client.currentLanguage(id).Result;
        }

        public string currentLocale()
        {
            lock (extension.SyncRoot)
                return client.currentLocale(id).Result;
        }

        public string currentScreenName()
        {
            lock (extension.SyncRoot)
                return client.currentScreenName(id).Result;
        }

        public AccessLevel currentAccessLevel()
        {
            lock (extension.SyncRoot)
            {
                switch (client.accessLevel(id).Result)
                {
                    case "Operate":
                        return AccessLevel.Operating;

                    case "Edit":
                        return AccessLevel.Editing;

                    case "Management":
                        return AccessLevel.Managing;

                    case "Safety":
                        return AccessLevel.ManagingSafety;

                    case "Support":
                        return AccessLevel.Support;

                    case "Admin":
                        return AccessLevel.Administering;

                    default:
                        return AccessLevel.Operating;
                }
            }
        }
 
		public List<string> registerYML(string ymlSource)
		{
            lock (extension.SyncRoot)
                return client.registerYML(id, ymlSource).Result;
		}

		public void registerYMLFile(String ymlFileName)
		{
			String yml = new String(File.ReadAllText(Path.GetFullPath(ymlFileName)));
			var errors = registerYML(yml);
			if (errors.Count > 0) 
			{
				Console.WriteLine(ymlFileName+" YML Errors encountered:");
				foreach(var e in errors)
					Console.WriteLine("  "+e);
				throw new Exception("YML Error in "+ymlFileName);
			}
		}

		public void registerImageFile(String imageFileName)
		{
			//try 
			//{
   //             lock (extension.SyncRoot)
   //                 client.registerImageFile(id, imageFileName).Wait();
			//} 
			//catch (Exception e1)
			//{
				// something went wrong - possible file isn't accessible from service end, so send data over API
				Console.WriteLine("[" + imageFileName + "] isn't accessible by the remote device. Opening the file locally and sending data.");

				//try
				//{
					var imageBytes = File.ReadAllBytes(Path.GetFullPath(imageFileName));
					MemoryStream stream = new MemoryStream();
					using (BinaryWriter writer = new BinaryWriter(stream))
					{
						writer.Write(imageBytes);
					}

					byte[] bytes = stream.ToArray();
                    lock (extension.SyncRoot)
                        client.registerImageData(id, bytes, imageFileName).Wait();
			//	}
			//	catch (Exception e2)
			//	{
			//		throw e2; //backup attempt didn't work. raise the original exception
			//	}
			//}
		}

        public void registerImageData(byte[] imageData, String imageName)
        {
            lock (extension.SyncRoot)
                client.registerImageData(id, imageData, imageName).Wait();
        }

        public void registerHTMLFile(String htmlFileName)
        {
            try 
			{
                lock (extension.SyncRoot)
                    client.registerHTMLFile(id, htmlFileName).Wait();
            } 
			catch (Exception e1) 
			{
				// something went wrong - possible file isn't accessible from service end, so send data over API
				Console.WriteLine("[" + htmlFileName + "] isn't accessible by the remote device. Opening the file locally and sending data.");

				try
				{
					var dataBytes = File.ReadAllBytes(Path.GetFullPath(htmlFileName));
					MemoryStream dataStream = new MemoryStream();
					using (BinaryWriter writer = new BinaryWriter(dataStream))
					{
						writer.Write(dataBytes);
					}

					byte[] bytesData = dataStream.ToArray();
                    lock (extension.SyncRoot)
                        client.registerHTMLData(id, bytesData, htmlFileName).Wait();
				}
				catch (Exception)
				{
					throw e1; //backup attempt didn't work. raise the original exception
				}
			}
        }

        public void registerHTMLData(byte[] htmlData, String htmlName)
        {
            lock (extension.SyncRoot)
                client.registerHTMLData(id, htmlData, htmlName).Wait();
        }

        public void registerTranslationFile(String locale, String translationFileName)
        {
            try 
			{
                lock (extension.SyncRoot)
                    client.registerTranslationFile(id, locale, translationFileName).Wait();
            } 
			catch (Exception e1) 
			{
				// something went wrong - possible file isn't accessible from service end, so send data over API
				Console.WriteLine("[" + translationFileName + "] isn't accessible by the remote device. Opening the file locally and sending data.");

				try
				{
					var dataBytes = File.ReadAllBytes(Path.GetFullPath(translationFileName));
					MemoryStream translationStream = new MemoryStream();
					using (BinaryWriter writer = new BinaryWriter(translationStream))
					{
						writer.Write(dataBytes);
					}

					byte[] bytesStream = translationStream.ToArray();
                    lock (extension.SyncRoot)
                        client.registerTranslationData(id, locale, bytesStream, translationFileName).Wait();
				}
				catch (Exception)
				{
					throw e1; //backup attempt didn't work. raise the original exception
				}
			}
        }

        public void registerTranslationData(String locale, byte[] translationData, String translationName)
        {
            lock (extension.SyncRoot)
                client.registerTranslationData(id, locale, translationData, translationName).Wait();
        }
        
        public void registerUtilityMenu(string menuName, string menuText, string menuIcon)
        {
            lock (extension.SyncRoot)
                client.registerUtilityMenu(id, menuName, menuText, menuIcon).Wait();
        }

        public void unregisterUtilityMenu(string menuName)
        {
            lock (extension.SyncRoot)
                client.unregisterUtilityMenu(id, menuName).Wait();
        }

        public void registerUtilityWindow(string identifier, string itemtype, string menuitemname, string windowtitle) 
		{
            lock (extension.SyncRoot)
                client.registerUtilityWindow(id, identifier, itemtype, menuitemname, windowtitle).Wait();
		}

        public void registerUtilityWindowWithMenu(string identifier, string itemtype, string menuitemname, string windowtitle, string menuName)
        {
            lock (extension.SyncRoot)
                client.registerUtilityWindowWithMenu(id, identifier, itemtype, menuitemname, windowtitle, menuName).Wait();
        }

        public void unregisterUtilityWindow(String identifier)
		{
            lock (extension.SyncRoot)
                client.unregisterUtilityWindow(id, identifier).Wait();
		}

		public void openUtilityWindow(String identifier)
		{
            lock (extension.SyncRoot)
                client.openUtilityWindow(id, identifier).Wait();
		}

		public void closeUtilityWindow(String identifier)
		{
            lock (extension.SyncRoot)
                client.closeUtilityWindow(id, identifier).Wait();
		}

		public void collapseUtilityWindow(String identifier)
		{
            lock (extension.SyncRoot)
                client.collapseUtilityWindow(id, identifier).Wait();
		}

		public void expandUtilityWindow(String identifier)
		{
            lock (extension.SyncRoot)
                client.expandUtilityWindow(id, identifier).Wait();
        }
        
        public void refreshDynamicInstructions(DynamicInstructionType instructionType)
        {
            lock (extension.SyncRoot)
                client.refreshDynamicInstructions(id, instructionType).Wait();
        }

        public void registerIntegration(String identifier, IntegrationPoint integrationPoint, String itemType, String buttonLabel, String buttonImage) 
		{
            lock (extension.SyncRoot)
                client.registerIntegration(id, identifier, integrationPoint, itemType, buttonLabel, buttonImage).Wait();
		}

		public void unregisterIntegration(String identifier)
		{
            lock (extension.SyncRoot)
                client.unregisterIntegration(id, identifier).Wait();
		}

        public void registerSwitch(String identifier, IntegrationPoint integrationPoint, String switchLabel, String offPositionLabel, String onPositionLabel, bool defaultState)
        {
            lock (extension.SyncRoot)
                client.registerSwitch(id, identifier, integrationPoint, switchLabel, offPositionLabel, onPositionLabel, defaultState).Wait();
        }

        public void registerDirectOpenForInstr(string identifier, string instruction, List<string> instrTags)
        {
            lock (extension.SyncRoot)
                client.registerDirectOpenForInstr(id, identifier, instruction, instrTags).Wait();
        }

        public void unregisterDirectOpenForInstr(string identifier, string instruction)
        {
            lock (extension.SyncRoot)
                client.unregisterDirectOpenForInstr(id, identifier, instruction).Wait();
        }

        public Any property(string itemID, string name)
        {
            //Console.WriteLine(client.property(id, itemID, name).SValue);
            lock (extension.SyncRoot)
                return client.property(id, itemID, name).Result;
        }

        public void setProperty(string itemID, string name, Any @value)
		{
            lock (extension.SyncRoot)
                client.setProperty(id, itemID, name, @value).Wait();
		}

        //convenience overloads
        public void setProperty(string itemID, string name, bool @value)
        {
            Any a = new Any();
            a.BValue = @value;
            lock (extension.SyncRoot)
                client.setProperty(id, itemID, name, a).Wait();
        }

        public void setProperty(string itemID, string name, int @value)
        {
            Any a = new Any();
            a.IValue = @value;
            lock (extension.SyncRoot)
                client.setProperty(id, itemID, name, a).Wait();
        }

        public void setProperty(string itemID, string name, long @value)
        {
            Any a = new Any();
            a.IValue = @value;
            lock (extension.SyncRoot)
                client.setProperty(id, itemID, name, a).Wait();
        }

        public void setProperty(string itemID, string name, double @value)
        {
            Any a = new Any();
            a.RValue = @value;
            lock (extension.SyncRoot)
                client.setProperty(id, itemID, name, a).Wait();
        }

        public void setProperty(string itemID, string name, string @value)
        {
            Any a = new Any();
            a.SValue = @value;
            lock (extension.SyncRoot)
                client.setProperty(id, itemID, name, a).Wait();
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
            lock (extension.SyncRoot)
                client.setProperty(id, itemID, name, aList).Wait();
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
            lock (extension.SyncRoot)
                client.setProperty(id, itemID, name, aList).Wait();
        }

        public void setProperty(string itemID, string name, Dictionary<string, object> @value)
        {
            var m = new Dictionary<string, Any>();
            foreach(var k in m.Keys) 
			{
                m[k] = Extension.toAny(m[k]);
            }

            Any aObj = new Any();
            aObj.MValue = m;
            lock (extension.SyncRoot)
                client.setProperty(id, itemID, name, aObj).Wait();
        }

        public void setProperties(List<PropValue> propValues)
        {
            lock (extension.SyncRoot)
                client.setProperties(id, this.PropValues(propValues)).Wait();
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
            foreach(KeyValuePair<string, List<PropValue>> entry in m) 
			{
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
            lock (extension.SyncRoot)
                client.setChartConfig(id, chartID, config).Wait();
        }

        public void setChartConfig(String chartID, Dictionary<String, Object> config)
        {
            Any a = new Any();
            var m = new Dictionary<String,Any>();
            foreach(var k in config.Keys) {
                m[k] = Extension.toAny(config[k]);
            }
            a.MValue = m;
            lock (extension.SyncRoot)
                client.setChartConfig(id, chartID, a).Wait();
        }

        public void setChartData(String chartID, Dictionary<String, Data> dataset)
        {
            lock (extension.SyncRoot)
                client.setChartData(id, chartID, dataset, false).Wait();
        }

        public void setChartData(String chartID, Dictionary<String, Data> dataset, bool right)
        {
            lock (extension.SyncRoot)
                client.setChartData(id, chartID, dataset, right).Wait();
        }

        public Dictionary<String, Data> getChartData(String chartID)
        {
            lock (extension.SyncRoot)
                return client.getChartData(id, chartID, false).Result;
        }

        public Dictionary<String, Data> getChartData(String chartID, bool right)
        {
            lock (extension.SyncRoot)
                return client.getChartData(id, chartID, right).Result;
        }

        public void addChartKey(String chartID, String key, Data data)
        {
            lock (extension.SyncRoot)
                client.addChartKey(id, chartID, key, data, false).Wait();
        }

        public void addChartKey(String chartID, String key, Data data, bool right)
        {
            lock (extension.SyncRoot)
                client.addChartKey(id, chartID, key, data, right).Wait();
        }
        
        public void removeChartKey(String chartID, String key)
        {
            lock (extension.SyncRoot)
                client.removeChartKey(id, chartID, key, false).Wait();
        }

        public void removeChartKey(String chartID, String key, bool right)
        {
            lock (extension.SyncRoot)
                client.removeChartKey(id, chartID, key, right).Wait();
        }

        public void hideChartKey(String chartID, String key)
        {
            lock (extension.SyncRoot)
                client.hideChartKey(id, chartID, key, true, false).Wait();
        }

        public void hideChartKey(String chartID, String key, bool hidden)
        {
            lock (extension.SyncRoot)
                client.hideChartKey(id, chartID, key, hidden, false).Wait();
        }

        public void hideChartKey(String chartID, String key, bool hidden, bool right)
        {
            lock (extension.SyncRoot)
                client.hideChartKey(id, chartID, key, hidden, right).Wait();
        }

        public void appendChartPoint(String chartID, String key, DataPoint pt)
        {
            List<DataPoint> ptList = new List<DataPoint>(new[] { pt });
            lock (extension.SyncRoot)
                client.appendChartPoints(id, chartID, key, ptList, false).Wait();
        }

        public void appendChartPoint(String chartID, String key, DataPoint pt, bool right)
        {
            List<DataPoint> ptList = new List<DataPoint>(new[] { pt });
            lock (extension.SyncRoot)
                client.appendChartPoints(id, chartID, key, ptList, right).Wait();
        }

        public void appendChartPoints(String chartID, String key, List<DataPoint> pts)
        {
            lock (extension.SyncRoot)
                client.appendChartPoints(id, chartID, key, pts, false).Wait();
        }

        public void appendChartPoints(String chartID, String key, List<DataPoint> pts, bool right)
        {
            lock (extension.SyncRoot)
                client.appendChartPoints(id, chartID, key, pts, right).Wait();
        }

        public void incrementChartKey(String chartID, String key)
        {
            lock (extension.SyncRoot)
                client.incrementChartKey(id, chartID, key, 1.0).Wait();
        }

        public void decrementChartKey(String chartID, String key)
        {
            lock (extension.SyncRoot)
                client.incrementChartKey(id, chartID, key, -1.0).Wait();
        }

        public void incrementChartKey(String chartID, String key, double value)
        {
            lock (extension.SyncRoot)
                client.incrementChartKey(id, chartID, key, value).Wait();
        }

        public void decrementChartKey(String chartID, String key, double value)
        {
            lock (extension.SyncRoot)
                client.incrementChartKey(id, chartID, key, -value).Wait();
        }
        public void notice(String title, String message, String log)
        {
            lock (extension.SyncRoot)
                client.notice(id, title, message, log).Wait();
        }
        public void notice(String title, String message)
        {
            notice(title, message, "");
        }

        public void dispNotice(Disposition disposition, String title, String message, String log)
        {
            lock (extension.SyncRoot)
                client.dispNotice(id, disposition, title, message, log).Wait();
        }
        public void dispNotice(Disposition disposition, String title, String message)
        { 
            dispNotice(disposition, title, message, ""); 
        }

        public void error(String title, String message, String log)
        {
            lock (extension.SyncRoot)
                client.error(id, title, message, log).Wait();
        }
        public void error(String title, String message)
        { 
            error(title, message, ""); 
        }
        public void popupDialog(String identifier, String title, String message, String positiveOption, String negativeOption)
        {
            lock (extension.SyncRoot)
                client.popupDialog(id, identifier, title, message, positiveOption, negativeOption).Wait();
        }

        public void cancelPopupDialog(String identifier)
        {
            lock (extension.SyncRoot)
                client.cancelPopupDialog(id, identifier).Wait();
        }
        
        public String insertInstructionAtSelectedLine(String instruction)
        {
            lock (extension.SyncRoot)
                return client.insertInstructionAtSelectedLine(id, instruction).Result;
        }

        public String replaceInstructionAtSelectedLine(String instruction)
        {
            lock (extension.SyncRoot)
                return client.replaceInstructionAtSelectedLine(id, instruction).Result;
        }

        public String deleteInstructionAtSelectedLine()
        {
            lock (extension.SyncRoot)
                return client.deleteInstructionAtSelectedLine(id).Result;
        }

        // event consumer functions
        public void addEventConsumer(PendantEventType eventType, Action<PendantEvent> c)
        {
            HashSet<PendantEventType> Set = new HashSet<PendantEventType>();
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
            HashSet<PendantEventType> itemSet = new HashSet<PendantEventType>();
            HashSet<String> nameSet = new HashSet<String>();

            itemSet.Add(eventType);
            nameSet.Add(itemName);

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

            if (eventType != PendantEventType.VisibleChanged)
                subscribeEventTypes(itemSet);
            else
                subscribeItemEventTypes(nameSet, itemSet);
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
            if (e.__isset.props && (props.ContainsKey("item") || props.ContainsKey("identifier"))) 
			{
                // do we have a consumer for this event type & item ?
                if (itemEventConsumers.ContainsKey(e.EventType)) 
				{
                    var consumers = itemEventConsumers[e.EventType];
                    String itemName = props.ContainsKey("item") ? props["item"].SValue : props["identifier"].SValue;
                    if (consumers.ContainsKey(itemName)) 
					{                    
                        foreach(Action<PendantEvent> consumer in consumers[itemName]) 
                            consumer.Invoke(e);
                    }
                }
            }
        }
        public void displayScreen(String identifier)
        {
            lock (extension.SyncRoot)
                client.displayScreen(id, identifier).Wait();
        }

        public void displayHelp(String title, String htmlContentFile)
        {
            lock (extension.SyncRoot)
                client.displayHelp(id, title, htmlContentFile).Wait();
        }

        public void appendYmlRowToContainer(string containerID, Dictionary<string, Any> ymlProperties)
        {
            lock (extension.SyncRoot)
                client.appendRow(id, containerID, ymlProperties).Wait();
        }

        public void appendMultipleYmlRowsToContainer(string containerID, List<Any> ymlRowMaps)
        {
            lock (extension.SyncRoot)
                client.appendRows(id, containerID, ymlRowMaps).Wait();
        }

        public void insertYmlRowInContainer(string containerID, int rowIndex, Dictionary<string, Any> ymlProperties)
        {
            lock (extension.SyncRoot)
                client.insertRow(id, containerID, rowIndex, ymlProperties).Wait();
        }

        public void deleteYmlRowFromContainer(string containerID, int rowIndex)
        {
            lock (extension.SyncRoot)
                client.deleteRow(id, containerID, rowIndex).Wait();
        }

        public void clearAllYmlRowsFromContainer(string containerID)
        {
            lock (extension.SyncRoot)
                client.clearRows(id, containerID).Wait();
        }

        public enum AccessLevel
        {
            Monitoring = 0,
            Operating,
            Editing,
            Managing,
            ManagingSafety,
            Support,
            Administering
        };

        protected Extension extension;
        protected API.Pendant.Client client;
        protected long id;
        protected Dictionary<PendantEventType, List<Action<PendantEvent>>> eventConsumers;

        protected Dictionary<PendantEventType, Dictionary<string, List<Action<PendantEvent>>>> itemEventConsumers;

    }

}
