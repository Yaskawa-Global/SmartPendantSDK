using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using Google.Protobuf;
using Guis.V1;
using GVersion = Guis.V1.Version;
using Grpc.Core;

namespace Yaskawa.Ext
{
    public class Pendant
    {
        internal Pendant(Extension ext, Guis.V1.Pendant.PendantClient grpcClient, long id)
        {
            extension = ext;
            client = grpcClient;
            this.id = id;
            eventConsumers = new Dictionary<PendantEventType, List<Action<PendantEvent>>>();
            itemEventConsumers = new Dictionary<PendantEventType, Dictionary<string, List<Action<PendantEvent>>>>();
        }

        public GVersion pendantVersion()
        {
            lock (extension.SyncRoot)
                return client.PendantVersion(new CommonPendantRequest { Pid = id });
        }

        public void subscribeEventTypes(HashSet<PendantEventType> types)
        {
            var request = new SubscribeEventTypesRequest { Pid = id };
            request.Types_.Add(types ?? Enumerable.Empty<PendantEventType>());
            lock (extension.SyncRoot)
                client.SubscribeEventTypes(request);
        }

        public void unsubscribeEventTypes(HashSet<PendantEventType> types)
        {
            var request = new UnsubscribeEventTypesRequest { Pid = id };
            request.Types_.Add(types ?? Enumerable.Empty<PendantEventType>());
            lock (extension.SyncRoot)
                client.UnsubscribeEventTypes(request);
        }

        public void subscribeItemEventTypes(HashSet<string> itemIDs, HashSet<PendantEventType> types)
        {
            var request = new SubscribeItemEventTypesRequest { Pid = id };
            request.ItemIDs.Add(itemIDs ?? Enumerable.Empty<string>());
            request.Types_.Add(types ?? Enumerable.Empty<PendantEventType>());
            lock (extension.SyncRoot)
                client.SubscribeItemEventTypes(request);
        }

        public void unsubscribeItemEventTypes(HashSet<string> itemIDs, HashSet<PendantEventType> types)
        {
            foreach (var type in types ?? new HashSet<PendantEventType>())
            {
                if (itemEventConsumers.TryGetValue(type, out var byItem))
                {
                    foreach (var item in itemIDs ?? new HashSet<string>())
                        byItem.Remove(item);
                }
            }

            var request = new UnsubscribeItemEventTypesRequest { Pid = id };
            request.ItemIDs.Add(itemIDs ?? Enumerable.Empty<string>());
            request.Types_.Add(types ?? Enumerable.Empty<PendantEventType>());
            lock (extension.SyncRoot)
                client.UnsubscribeItemEventTypes(request);
        }

        public List<PendantEvent> events()
        {
            lock (extension.SyncRoot)
                return client.Events(new CommonPendantRequest { Pid = id }).Events.ToList();
        }

        public AsyncServerStreamingCall<PendantEvent> SubscribeEvents()
        {
           lock (extension.SyncRoot)
               return client.SubscribeEvents(new CommonPendantRequest { Pid = id });
        }

        public string currentLanguage()
        {
            lock (extension.SyncRoot)
                return client.CurrentLanguage(new CommonPendantRequest { Pid = id }).Value;
        }

        public string currentLocale()
        {
            lock (extension.SyncRoot)
                return client.CurrentLocale(new CommonPendantRequest { Pid = id }).Value;
        }

        public string currentScreenName()
        {
            lock (extension.SyncRoot)
                return client.CurrentScreenName(new CommonPendantRequest { Pid = id }).Value;
        }

        public AccessLevel currentAccessLevel()
        {
            lock (extension.SyncRoot)
            {
                return client.AccessLevel(new CommonPendantRequest { Pid = id }).Value switch
                {
                    "Operate" => AccessLevel.Operating,
                    "Edit" => AccessLevel.Editing,
                    "Management" => AccessLevel.Managing,
                    "Safety" => AccessLevel.ManagingSafety,
                    "Support" => AccessLevel.Support,
                    "Admin" => AccessLevel.Administering,
                    _ => AccessLevel.Operating,
                };
            }
        }

        public List<string> registerYML(string ymlSource)
        {
            lock (extension.SyncRoot)
                return client.RegisterYML(new RegisterYMLRequest { Pid = id, YmlSource = ymlSource ?? string.Empty }).ParseErrors.ToList();
        }

        public void registerYMLFile(string ymlFileName)
        {
            var yml = File.ReadAllText(Path.GetFullPath(ymlFileName));
            var errors = registerYML(yml);
            if (errors.Count > 0)
            {
                Console.WriteLine(ymlFileName + " YML Errors encountered:");
                foreach (var e in errors)
                    Console.WriteLine("  " + e);
                throw new Exception("YML Error in " + ymlFileName);
            }
        }

        public void registerImageFile(string imageFileName)
        {
            Console.WriteLine("[" + imageFileName + "] isn't accessible by the remote device. Opening the file locally and sending data.");
            var imageBytes = File.ReadAllBytes(Path.GetFullPath(imageFileName));
            registerImageData(imageBytes, imageFileName);
        }

        public void registerImageData(byte[] imageData, string imageName)
        {
            lock (extension.SyncRoot)
                client.RegisterImageData(new RegisterImageDataRequest { Pid = id, ImageData = ByteString.CopyFrom(imageData ?? Array.Empty<byte>()), ImageName = imageName ?? string.Empty });
        }

        public void registerHTMLFile(string htmlFileName)
        {
            Console.WriteLine("[" + htmlFileName + "] isn't accessible by the remote device. Opening the file locally and sending data.");
            var bytes = File.ReadAllBytes(Path.GetFullPath(htmlFileName));
            registerHTMLData(bytes, htmlFileName);
        }

        public void registerHTMLData(byte[] htmlData, string htmlName)
        {
            lock (extension.SyncRoot)
                client.RegisterHTMLData(new RegisterHTMLDataRequest { Pid = id, HtmlData = ByteString.CopyFrom(htmlData ?? Array.Empty<byte>()), HtmlName = htmlName ?? string.Empty });
        }

        public void registerTranslationFile(string locale, string translationFileName)
        {
            Console.WriteLine("[" + translationFileName + "] isn't accessible by the remote device. Opening the file locally and sending data.");
            var bytes = File.ReadAllBytes(Path.GetFullPath(translationFileName));
            registerTranslationData(locale, bytes, translationFileName);
        }

        public void registerTranslationData(string locale, byte[] translationData, string translationName)
        {
            lock (extension.SyncRoot)
                client.RegisterTranslationData(new RegisterTranslationDataRequest
                {
                    Pid = id,
                    Locale = locale ?? string.Empty,
                    TranslationData = ByteString.CopyFrom(translationData ?? Array.Empty<byte>()),
                    TranslationName = translationName ?? string.Empty
                });
        }

        public void registerUtilityMenu(string menuName, string menuText, string menuIcon)
        {
            lock (extension.SyncRoot)
                client.RegisterUtilityMenu(new RegisterUtilityMenuRequest { Pid = id, MenuName = menuName ?? string.Empty, MenuText = menuText ?? string.Empty, MenuIcon = menuIcon ?? string.Empty });
        }

        public void unregisterUtilityMenu(string menuName)
        {
            lock (extension.SyncRoot)
                client.UnregisterUtilityMenu(new UnregisterUtilityMenuRequest { Pid = id, MenuName = menuName ?? string.Empty });
        }

        public void registerUtilityWindow(string identifier, string itemtype, string menuitemname, string windowtitle)
        {
            lock (extension.SyncRoot)
                client.RegisterUtilityWindow(new RegisterUtilityWindowRequest { Pid = id, Identifier = identifier ?? string.Empty, ItemType = itemtype ?? string.Empty, MenuItemName = menuitemname ?? string.Empty, WindowTitle = windowtitle ?? string.Empty });
        }

        public void registerUtilityWindowWithMenu(string identifier, string itemtype, string menuitemname, string windowtitle, string menuName)
        {
            lock (extension.SyncRoot)
                client.RegisterUtilityWindowWithMenu(new RegisterUtilityWindowWithMenuRequest { Pid = id, Identifier = identifier ?? string.Empty, ItemType = itemtype ?? string.Empty, MenuItemName = menuitemname ?? string.Empty, WindowTitle = windowtitle ?? string.Empty, MenuName = menuName ?? string.Empty });
        }

        public void unregisterUtilityWindow(string identifier)
        {
            lock (extension.SyncRoot)
                client.UnregisterUtilityWindow(new UnregisterUtilityWindowRequest { Pid = id, Identifier = identifier ?? string.Empty });
        }

        public void openUtilityWindow(string identifier)
        {
            lock (extension.SyncRoot)
                client.OpenUtilityWindow(new OpenUtilityWindowRequest { Pid = id, Identifier = identifier ?? string.Empty });
        }

        public void closeUtilityWindow(string identifier)
        {
            lock (extension.SyncRoot)
                client.CloseUtilityWindow(new CloseUtilityWindowRequest { Pid = id, Identifier = identifier ?? string.Empty });
        }

        public void collapseUtilityWindow(string identifier)
        {
            lock (extension.SyncRoot)
                client.CollapseUtilityWindow(new CollapseUtilityWindowRequest { Pid = id, Identifier = identifier ?? string.Empty });
        }

        public void expandUtilityWindow(string identifier)
        {
            lock (extension.SyncRoot)
                client.ExpandUtilityWindow(new ExpandUtilityWindowRequest { Pid = id, Identifier = identifier ?? string.Empty });
        }

        public void refreshDynamicInstructions(DynamicInstructionType instructionType)
        {
            lock (extension.SyncRoot)
                client.RefreshDynamicInstructions(new RefreshDynamicInstructionsRequest { Pid = id, InstructionType = instructionType });
        }

        public void registerIntegration(string identifier, IntegrationPoint integrationPoint, string itemType, string buttonLabel, string buttonImage)
        {
            lock (extension.SyncRoot)
                client.RegisterIntegration(new RegisterIntegrationRequest { Pid = id, Identifier = identifier ?? string.Empty, IntegrationPoint = integrationPoint, ItemType = itemType ?? string.Empty, ButtonLabel = buttonLabel ?? string.Empty, ButtonImage = buttonImage ?? string.Empty });
        }

        public void unregisterIntegration(string identifier)
        {
            lock (extension.SyncRoot)
                client.UnregisterIntegration(new UnregisterIntegrationRequest { Pid = id, Identifier = identifier ?? string.Empty });
        }

        public void registerSwitch(string identifier, IntegrationPoint integrationPoint, string switchLabel, string offPositionLabel, string onPositionLabel, bool defaultState)
        {
            lock (extension.SyncRoot)
                client.RegisterSwitch(new RegisterSwitchRequest { Pid = id, Identifier = identifier ?? string.Empty, IntegrationPoint = integrationPoint, SwitchLabel = switchLabel ?? string.Empty, OffPositionLabel = offPositionLabel ?? string.Empty, OnPositionLabel = onPositionLabel ?? string.Empty, DefaultState = defaultState });
        }

        public void registerDirectOpenForInstr(string identifier, string instruction, List<string> instrTags)
        {
            var request = new RegisterDirectOpenForInstrRequest { Pid = id, Identifier = identifier ?? string.Empty, Instruction = instruction ?? string.Empty };
            request.InstrTags.Add(instrTags ?? Enumerable.Empty<string>());
            lock (extension.SyncRoot)
                client.RegisterDirectOpenForInstr(request);
        }

        public void unregisterDirectOpenForInstr(string identifier, string instruction)
        {
            lock (extension.SyncRoot)
                client.UnregisterDirectOpenForInstr(new UnregisterDirectOpenForInstrRequest { Pid = id, Identifier = identifier ?? string.Empty, Instruction = instruction ?? string.Empty });
        }

        public Any property(string itemID, string name)
        {
            lock (extension.SyncRoot)
                return client.Property(new PropertyRequest { Pid = id, ItemID = itemID ?? string.Empty, Name = name ?? string.Empty }).Value;
        }

        public List<Any> getProperties(List<PropQuery> queries)
        {
            lock (extension.SyncRoot)

          {
              var request = new GetPropertiesRequest
             {
                    Pid = id
             };

                request.Queries.AddRange(queries ?? Enumerable.Empty<PropQuery>());

                return client.GetProperties(request)
                            .Values
                            .ToList();
         }
        }


        public void setProperty(string itemID, string name, Any value)
        {
            lock (extension.SyncRoot)
                client.SetProperty(new SetPropertyRequest { Pid = id, ItemID = itemID ?? string.Empty, Name = name ?? string.Empty, Value = value });
        }

        public void setProperty(string itemID, string name, bool value) => setProperty(itemID, name, Extension.toAny(value));
        public void setProperty(string itemID, string name, int value) => setProperty(itemID, name, Extension.toAny((long)value));
        public void setProperty(string itemID, string name, long value) => setProperty(itemID, name, Extension.toAny(value));
        public void setProperty(string itemID, string name, double value) => setProperty(itemID, name, Extension.toAny(value));
        public void setProperty(string itemID, string name, string value) => setProperty(itemID, name, Extension.toAny(value));
        public void setProperty(string itemID, string name, List<object> value) => setProperty(itemID, name, Extension.toAny(value));
        public void setProperty(string itemID, string name, object[] value) => setProperty(itemID, name, Extension.toAny(value));
        public void setProperty(string itemID, string name, Dictionary<string, object> value) => setProperty(itemID, name, Extension.toAny(value));

        public void setProperties(List<PropValue> propValues)
        {
            var request = new SetPropertiesRequest { Pid = id };
            request.PropValuesList.Add(PropValues(propValues ?? new List<PropValue>()));
            lock (extension.SyncRoot)
                client.SetProperties(request);
        }

        public class PropValue
        {
            public PropValue(string itemId, string name, Any value)
            {
                ItemId = itemId;
                Name = name;
                Value = value;
            }

            public string ItemId;
            public string Name;
            public Any Value;
        }

        public List<Guis.V1.PropValues> PropValues(List<PropValue> propValues)
        {
            var grouped = new Dictionary<string, List<PropValue>>();
            foreach (var propValue in propValues)
            {
                if (!grouped.ContainsKey(propValue.ItemId))
                    grouped[propValue.ItemId] = new List<PropValue>();
                grouped[propValue.ItemId].Add(propValue);
            }

            var result = new List<Guis.V1.PropValues>();
            foreach (var entry in grouped)
            {
                var pvs = new Guis.V1.PropValues { ItemID = entry.Key };
                foreach (var p in entry.Value)
                    pvs.Props[p.Name] = p.Value;
                result.Add(pvs);
            }
            return result;
        }

        public static PropValue propValue(string itemID, string name, bool value) => new(itemID, name, Extension.toAny(value));
        public static PropValue propValue(string itemID, string name, int value) => new(itemID, name, Extension.toAny((long)value));
        public static PropValue propValue(string itemID, string name, long value) => new(itemID, name, Extension.toAny(value));
        public static PropValue propValue(string itemID, string name, double value) => new(itemID, name, Extension.toAny(value));
        public static PropValue propValue(string itemID, string name, string value) => new(itemID, name, Extension.toAny(value));
        public static PropValue propValue(string itemID, string name, List<object> value) => new(itemID, name, Extension.toAny(value));
        public static PropValue propValue(string itemID, string name, object[] value) => new(itemID, name, Extension.toAny(value));
        public static PropValue propValue(string itemID, string name, Dictionary<string, object> value) => new(itemID, name, Extension.toAny(value));

        public void setChartConfig(string chartID, Any config)
        {
            lock (extension.SyncRoot)
                client.SetChartConfig(new SetChartConfigRequest { Pid = id, ChartID = chartID ?? string.Empty, Config = config });
        }

        public void setChartConfig(string chartID, Dictionary<string, object> config)
        {
            setChartConfig(chartID, Extension.toAny(config));
        }

        public void setChartData(string chartID, Dictionary<string, Data> dataset)
        {
            setChartData(chartID, dataset, false);
        }

        public void setChartData(string chartID, Dictionary<string, Data> dataset, bool right)
        {
            var request = new SetChartDataRequest
            {
                Pid = id,
                ChartID = chartID ?? string.Empty,
                Right = right,
                Dataset = new DataSet()
            };
            if (dataset != null)
            {
                foreach (var kvp in dataset)
                    request.Dataset.Values[kvp.Key] = kvp.Value;
            }
            lock (extension.SyncRoot)
                client.SetChartData(request);
        }

        public Dictionary<string, Data> getChartData(string chartID)
        {
            return getChartData(chartID, false);
        }

        public Dictionary<string, Data> getChartData(string chartID, bool right)
        {
            lock (extension.SyncRoot)
                return client.GetChartData(new GetChartDataRequest { Pid = id, ChartID = chartID ?? string.Empty, Right = right }).Dataset.Values.ToDictionary(k => k.Key, v => v.Value);
        }

        public void addChartKey(string chartID, string key, Data data)
        {
            addChartKey(chartID, key, data, false);
        }

        public void addChartKey(string chartID, string key, Data data, bool right)
        {
            lock (extension.SyncRoot)
                client.AddChartKey(new AddChartKeyRequest { Pid = id, ChartID = chartID ?? string.Empty, Key = key ?? string.Empty, Data = data, Right = right });
        }

        public void removeChartKey(string chartID, string key)
        {
            removeChartKey(chartID, key, false);
        }

        public void removeChartKey(string chartID, string key, bool right)
        {
            lock (extension.SyncRoot)
                client.RemoveChartKey(new RemoveChartKeyRequest { Pid = id, ChartID = chartID ?? string.Empty, Key = key ?? string.Empty, Right = right });
        }

        public void hideChartKey(string chartID, string key)
        {
            hideChartKey(chartID, key, true, false);
        }

        public void hideChartKey(string chartID, string key, bool hidden)
        {
            hideChartKey(chartID, key, hidden, false);
        }

        public void hideChartKey(string chartID, string key, bool hidden, bool right)
        {
            lock (extension.SyncRoot)
                client.HideChartKey(new HideChartKeyRequest { Pid = id, ChartID = chartID ?? string.Empty, Key = key ?? string.Empty, Hidden = hidden, Right = right });
        }

        public void appendChartPoint(string chartID, string key, DataPoint pt)
        {
            appendChartPoint(chartID, key, pt, false);
        }

        public void appendChartPoint(string chartID, string key, DataPoint pt, bool right)
        {
            appendChartPoints(chartID, key, new List<DataPoint> { pt }, right);
        }

        public void appendChartPoints(string chartID, string key, List<DataPoint> pts)
        {
            appendChartPoints(chartID, key, pts, false);
        }

        public void appendChartPoints(string chartID, string key, List<DataPoint> pts, bool right)
        {
            var request = new AppendChartPointsRequest { Pid = id, ChartID = chartID ?? string.Empty, Key = key ?? string.Empty, Right = right };
            request.Points.Add(pts ?? Enumerable.Empty<DataPoint>());
            lock (extension.SyncRoot)
                client.AppendChartPoints(request);
        }

        public void incrementChartKey(string chartID, string key)
        {
            incrementChartKey(chartID, key, 1.0);
        }

        public void decrementChartKey(string chartID, string key)
        {
            incrementChartKey(chartID, key, -1.0);
        }

        public void incrementChartKey(string chartID, string key, double value)
        {
            lock (extension.SyncRoot)
                client.IncrementChartKey(new IncrementChartKeyRequest { Pid = id, ChartID = chartID ?? string.Empty, Key = key ?? string.Empty, Val = value });
        }

        public void decrementChartKey(string chartID, string key, double value)
        {
            incrementChartKey(chartID, key, -value);
        }

        public void notice(string title, string message, string log)
        {
            lock (extension.SyncRoot)
                client.Notice(new NoticeRequest { Pid = id, Title = title ?? string.Empty, Message = message ?? string.Empty, Log = log ?? string.Empty });
        }

        public void notice(string title, string message)
        {
            notice(title, message, "");
        }

        public void dispNotice(Disposition disposition, string title, string message, string log)
        {
            lock (extension.SyncRoot)
                client.DispNotice(new DispNoticeRequest { Pid = id, Disposition = disposition, Title = title ?? string.Empty, Message = message ?? string.Empty, Log = log ?? string.Empty });
        }

        public void dispNotice(Disposition disposition, string title, string message)
        {
            dispNotice(disposition, title, message, "");
        }

        public void error(string title, string message, string log)
        {
            lock (extension.SyncRoot)
                client.Error(new ErrorRequest { Pid = id, Title = title ?? string.Empty, Message = message ?? string.Empty, Log = log ?? string.Empty });
        }

        public void error(string title, string message)
        {
            error(title, message, "");
        }

        public void popupDialog(string identifier, string title, string message, string positiveOption, string negativeOption)
        {
            lock (extension.SyncRoot)
                client.PopupDialog(new PopupDialogRequest { Pid = id, Identifier = identifier ?? string.Empty, Title = title ?? string.Empty, Message = message ?? string.Empty, PositiveOption = positiveOption ?? string.Empty, NegativeOption = negativeOption ?? string.Empty });
        }

        public void cancelPopupDialog(string identifier)
        {
            lock (extension.SyncRoot)
                client.CancelPopupDialog(new CancelPopupDialogRequest { Pid = id, Identifier = identifier ?? string.Empty });
        }

        public string insertInstructionAtSelectedLine(string instruction)
        {
            lock (extension.SyncRoot)
                return client.InsertInstructionAtSelectedLine(new InsertInstructionRequest { Pid = id, Instruction = instruction ?? string.Empty }).Value;
        }

        public string replaceInstructionAtSelectedLine(string instruction)
        {
            lock (extension.SyncRoot)
                return client.ReplaceInstructionAtSelectedLine(new ReplaceInstructionRequest { Pid = id, Instruction = instruction ?? string.Empty }).Value;
        }

        public string deleteInstructionAtSelectedLine()
        {
            lock (extension.SyncRoot)
                return client.DeleteInstructionAtSelectedLine(new DeleteInstructionRequest { Pid = id }).Value;
        }

        public void addEventConsumer(PendantEventType eventType, Action<PendantEvent> c)
        {
            var set = new HashSet<PendantEventType> { eventType };
            if (!eventConsumers.ContainsKey(eventType))
                eventConsumers[eventType] = new List<Action<PendantEvent>>();
            eventConsumers[eventType].Add(c);
            subscribeEventTypes(set);
        }

        public void addItemEventConsumer(string itemName, PendantEventType eventType, Action<PendantEvent> c)
        {
            var itemSet = new HashSet<PendantEventType> { eventType };
            var nameSet = new HashSet<string> { itemName };

            if (!itemEventConsumers.ContainsKey(eventType))
                itemEventConsumers[eventType] = new Dictionary<string, List<Action<PendantEvent>>>();
            if (!itemEventConsumers[eventType].ContainsKey(itemName))
                itemEventConsumers[eventType][itemName] = new List<Action<PendantEvent>>();
            itemEventConsumers[eventType][itemName].Add(c);

            if (eventType != PendantEventType.VisibleChanged)
                subscribeEventTypes(itemSet);
            else
                subscribeItemEventTypes(nameSet, itemSet);
        }

        public void handleEvent(PendantEvent e)
        {
            if (eventConsumers.ContainsKey(e.EventType))
            {
                foreach (var consumer in eventConsumers[e.EventType])
                    consumer.Invoke(e);
            }

            var props = e.Props;
            if (props != null && (props.ContainsKey("item") || props.ContainsKey("identifier")))
            {
                if (itemEventConsumers.ContainsKey(e.EventType))
                {
                    var consumers = itemEventConsumers[e.EventType];
                    var itemName = props.ContainsKey("item") ? props["item"].SValue : props["identifier"].SValue;
                    if (consumers.ContainsKey(itemName))
                    {
                        foreach (var consumer in consumers[itemName])
                            consumer.Invoke(e);
                    }
                }
            }
        }

        public void displayScreen(string identifier)
        {
            lock (extension.SyncRoot)
                client.DisplayScreen(new DisplayScreenRequest { Pid = id, Identifier = identifier ?? string.Empty });
        }

        public void displayHelp(string title, string htmlContentFile)
        {
            lock (extension.SyncRoot)
                client.DisplayHelp(new DisplayHelpRequest { Pid = id, Title = title ?? string.Empty, HtmlContentFile = htmlContentFile ?? string.Empty });
        }

        public void appendYmlRowToContainer(string containerID, Dictionary<string, Any> ymlProperties)
        {
            var request = new AppendRowRequest { Pid = id, ContainerID = containerID ?? string.Empty };
            if (ymlProperties != null)
            {
                foreach (var kv in ymlProperties)
                    request.Dict[kv.Key] = kv.Value;
            }
            lock (extension.SyncRoot)
                client.AppendRow(request);
        }

        public void appendMultipleYmlRowsToContainer(string containerID, List<Any> ymlRowMaps)
        {
            var request = new AppendRowsRequest { Pid = id, ContainerID = containerID ?? string.Empty };
            request.Dicts.Add(ymlRowMaps ?? Enumerable.Empty<Any>());
            lock (extension.SyncRoot)
                client.AppendRows(request);
        }

        public void insertYmlRowInContainer(string containerID, int rowIndex, Dictionary<string, Any> ymlProperties)
        {
            var request = new InsertRowRequest { Pid = id, ContainerID = containerID ?? string.Empty, Index = rowIndex };
            if (ymlProperties != null)
            {
                foreach (var kv in ymlProperties)
                    request.Dict[kv.Key] = kv.Value;
            }
            lock (extension.SyncRoot)
                client.InsertRow(request);
        }

        public void deleteYmlRowFromContainer(string containerID, int rowIndex)
        {
            lock (extension.SyncRoot)
                client.DeleteRow(new DeleteRowRequest { Pid = id, ContainerID = containerID ?? string.Empty, Index = rowIndex });
        }

        public void clearAllYmlRowsFromContainer(string containerID)
        {
            lock (extension.SyncRoot)
                client.ClearRows(new ClearRowsRequest { Pid = id, ContainerID = containerID ?? string.Empty });
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
        }

        protected Extension extension;
        protected Guis.V1.Pendant.PendantClient client;
        protected long id;
        protected Dictionary<PendantEventType, List<Action<PendantEvent>>> eventConsumers;
        protected Dictionary<PendantEventType, Dictionary<string, List<Action<PendantEvent>>>> itemEventConsumers;
    }
}
