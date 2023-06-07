
package yaskawa.ext;

import java.util.*;
import java.util.function.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.ByteBuffer;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;

import java.nio.charset.StandardCharsets;

import yaskawa.ext.api.*;

public class Pendant
{
    Pendant(Extension ext, TProtocol protocol, long id) //throws org.apache.thrift.TTransportException
    {
        extension = ext;
        client = new yaskawa.ext.api.Pendant.Client(protocol);
        this.id = id;
        eventConsumers = new HashMap<PendantEventType, ArrayList<Consumer<yaskawa.ext.api.PendantEvent>>>();
        itemEventConsumers = new HashMap<PendantEventType, HashMap<String, ArrayList<Consumer<yaskawa.ext.api.PendantEvent>>>>();
    }

    public Version pendantVersion() throws TException
    {
        synchronized(extension) {
            return new Version(client.pendantVersion(id));
        }
    }

    public void subscribeEventTypes(Set<PendantEventType> types) throws TException
    {
        synchronized(extension) {
            client.subscribeEventTypes(id, types);
        }
    }

    public void unsubscribeEventTypes(Set<PendantEventType> types) throws TException
    {
        synchronized(extension) {
            client.unsubscribeEventTypes(id, types);
        }
    }

    public void subscribeItemEventTypes(Set<String> itemIDs, Set<PendantEventType> types) throws TException
    {
        synchronized(extension) {
            client.subscribeItemEventTypes(id, itemIDs, types);
        }
    }

    public void unsubscribeItemEventTypes(Set<String> itemIDs, Set<PendantEventType> types) throws TException
    {
        synchronized(extension) {
            client.unsubscribeItemEventTypes(id, itemIDs, types);
        }
    }

    public List<yaskawa.ext.api.PendantEvent> events() throws TException
    {
        synchronized(extension) {
            return client.events(id);
        }
    }

    public String currentLanguage() throws TException
    {
        synchronized(extension) {
            return client.currentLanguage(id);
        }
    }

    public String currentLocale() throws TException
    {
        synchronized(extension) {
            return client.currentLocale(id);
        }
    }

    public String currentScreenName() throws TException
    {
        synchronized(extension) {
            return client.currentScreenName(id);
        }
    }

    public List<String> registerYML(String ymlSource) throws TException
    {
        synchronized(extension) {
            return client.registerYML(id, ymlSource);
        }
    }

    // convenience - on error, prints errors to output and throws
    public void registerYMLFile(String ymlFileName) throws TException, IOException, Exception
    {
        String yml = new String(Files.readAllBytes(Paths.get(ymlFileName)), StandardCharsets.UTF_8);
        var errors = registerYML(yml);
        if (errors.size() > 0) {
            System.out.println(ymlFileName+" YML Errors encountered:");
            for(var e : errors)
                System.out.println("  "+e);
            throw new Exception("YML Error in "+ymlFileName);
        }
    }

    public void registerImageFile(String imageFileName) throws IllegalArgument, TException, IOException
    {
        try {
            synchronized(extension) {
                client.registerImageFile(id, imageFileName);
            }
        } catch (Exception e) {
            // something went wrong - possible file isn't accessible from service end, so send data over API 
            var imageBytes = Files.readAllBytes(Paths.get(imageFileName));
            synchronized(extension) {
                client.registerImageData(id, ByteBuffer.wrap(imageBytes), imageFileName);
            }
        }
    }
    public void registerImageData(java.nio.ByteBuffer imageData, String imageName) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.registerImageData(id, imageData, imageName);
        }
    }

    public void registerHTMLFile(String htmlFileName) throws IllegalArgument, TException, IOException
    {
        try {
            synchronized(extension) {
                client.registerHTMLFile(id, htmlFileName);
            }
        } catch (Exception e) {
            // something went wrong - possible file isn't accessible from service end, so send data over API
            var dataBytes = Files.readAllBytes(Paths.get(htmlFileName));
            synchronized(extension) {
                client.registerHTMLData(id, ByteBuffer.wrap(dataBytes), htmlFileName);
            }
        }
    }
    public void registerHTMLData(java.nio.ByteBuffer htmlData, String htmlName) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.registerHTMLData(id, htmlData, htmlName);
        }
    }


    public void registerTranslationFile(String locale, String translationFileName) throws IllegalArgument, TException, IOException
    {
        try {
            synchronized(extension) {
                client.registerTranslationFile(id, locale, translationFileName);
            }
        } catch (Exception e) {
            // something went wrong - possible file isn't accessible from service end, so send data over API
            var dataBytes = Files.readAllBytes(Paths.get(translationFileName));
            synchronized(extension) {
                client.registerTranslationData(id, locale, ByteBuffer.wrap(dataBytes), translationFileName);
            }
        }
    }
    public void registerTranslationData(String locale, java.nio.ByteBuffer translationData, String translationName) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.registerTranslationData(id, locale, translationData, translationName);
        }
    }

    public void registerUtilityMenu(String menuName, String menuTitle, String menuIcon) throws TException
    {
        synchronized(extension) {
            client.registerUtilityMenu(id, menuName, menuTitle, menuIcon);
        }
    }

    public void unregisterUtilityMenu(String menuName) throws TException
    {
        synchronized(extension) {
            client.unregisterUtilityMenu(id, menuName);
        }
    }
    
    public void registerUtilityWindow(String identifier, String itemType, String menuItemName, String windowTitle, String menuName) throws TException
    {
        synchronized(extension) {
            client.registerUtilityWindow(id, identifier, itemType, menuItemName, windowTitle, menuName);
        }
    }

    public void unregisterUtilityWindow(String identifier) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.unregisterUtilityWindow(id, identifier);
        }
    }

    public void openUtilityWindow(String identifier) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.openUtilityWindow(id, identifier);
        }
    }

    public void closeUtilityWindow(String identifier) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.closeUtilityWindow(id, identifier);
        }
    }

    public void collapseUtilityWindow(String identifier) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.collapseUtilityWindow(id, identifier);
        }
    }

    public void expandUtilityWindow(String identifier) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.expandUtilityWindow(id, identifier);
        }
    }



    public void registerIntegration(String identifier, IntegrationPoint integrationPoint, String itemType, String buttonLabel, String buttonImage) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.registerIntegration(id, identifier, integrationPoint, itemType, buttonLabel, buttonImage);
        }
    }

    public void unregisterIntegration(String identifier) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.unregisterIntegration(id, identifier);
        }
    }


    public Any property(String itemID, String name) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.property(id, itemID, name);
        }
    }

    public void setProperty(String itemID, String name, Any value) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.setProperty(id, itemID, name, value);
        }
    }
    // convenience overloads
    public void setProperty(String itemID, String name, boolean value) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.setProperty(id, itemID, name, Any.bValue(value));
        }
    }
    public void setProperty(String itemID, String name, int value) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.setProperty(id, itemID, name, Any.iValue((long)value));
        }
    }
    public void setProperty(String itemID, String name, long value) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.setProperty(id, itemID, name, Any.iValue(value));
        }
    }
    public void setProperty(String itemID, String name, double value) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.setProperty(id, itemID, name, Any.rValue(value));
        }
    }
    public void setProperty(String itemID, String name, String value) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.setProperty(id, itemID, name, Any.sValue(value));
        }
    }

    public void setProperty(String itemID, String name, List<Object> array) throws IllegalArgument, TException
    {
        var a = new ArrayList<Any>(array.size());
        for(var e : array) {
            a.add(Extension.toAny(e));
        }

        synchronized(extension) {
            client.setProperty(id, itemID, name, Any.aValue(a));
        }
    }

    public void setProperty(String itemID, String name, Object[] array) throws IllegalArgument, TException
    {
        var a = new ArrayList<Any>(array.length);
        for(var e : array) {
            a.add(Extension.toAny(e));
        }

        synchronized(extension) {
            client.setProperty(id, itemID, name, Any.aValue(a));
        }
    }

    public void setProperty(String itemID, String name, Map<String, Object> map) throws IllegalArgument, TException
    {
        var m = new HashMap<String,Any>();
        for(var k : map.keySet()) {
            m.put(k, Extension.toAny(map.get(k)));
        }
        synchronized(extension) {
            client.setProperty(id, itemID, name, Any.mValue(m));
        }
    }


    public void setProperties(List<PropValue> propValues) throws org.apache.thrift.TException
    {
        synchronized(extension) {
            client.setProperties(id, this.propValues(propValues));
        }
    }

    // Convenience
    // The List<PropValues> taken by setProperties() are tedious to construct in Java,
    //  so provice convenience methods that take itemID, name, value and can be assembled into
    //  a list.

    public static class PropValue
    {
        public PropValue(String itemID, String name, Any value)
        {
            this.itemID = itemID;
            this.name = name;
            this.value = value;
        }

        public String itemID;
        public String name;
        public Any value;
    }

    // convert from List PropValue to List<PropValues> (collects props of same item together)
    public static List<PropValues> propValues(List<PropValue> propValues)
    {
        // collect by itemID
        var m = new LinkedHashMap<String, List<PropValue>>();
        for(var propValue : propValues) {
            if (!m.containsKey(propValue.itemID)) {
                m.put(propValue.itemID, new ArrayList<PropValue>());
            }
            m.get(propValue.itemID).add(propValue);
        }

        // now convert to api.PropValues
        var pvl = new ArrayList<PropValues>();
        for (Map.Entry<String, List<PropValue>> entry : m.entrySet()) {
            String itemID = entry.getKey();
            var pvs = new PropValues();
            pvs.setItemID(itemID);
            var pm = new LinkedHashMap<String, Any>();
            for(var p : entry.getValue())
                pm.put(p.name, p.value);
            pvs.setProps(pm);
            pvl.add(pvs);
        }
        return pvl;
    }



    // client calls these and construcs a List.of them for setProperties()
    public static PropValue propValue(String itemID, String name, boolean value)
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
        var a = new ArrayList<Any>(value.size());
        for(var e : value)
            a.add(Extension.toAny(e));
        return new PropValue(itemID, name, Any.aValue(a));
    }
    public static PropValue propValue(String itemID, String name, Object[] value)
    {
        var a = new ArrayList<Any>(value.length);
        for(var e : value)
            a.add(Extension.toAny(e));
        return new PropValue(itemID, name, Any.aValue(a));
    }
    public static PropValue propValue(String itemID, String name, Map<String, Object> value)
    {
        var m = new HashMap<String,Any>();
        for(var k : value.keySet())
            m.put(k, Extension.toAny(value.get(k)));
        return new PropValue(itemID, name, Any.mValue(m));
    }


    /*
        Charting API functions
    */
    
    /**
     * Sets the configuration object of a chart by ID. Refer to [TODO] for
     * documentation on chart configuration options
     * @param chartID String ID
     * @param config 
    */
    public void setChartConfig(String chartID, Any config)
            throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.setChartConfig(id, chartID, config);
        }
    }

    public void setChartConfig(String chartID, Map<String, Object> config)
            throws IllegalArgument, TException
    {
        var m = new HashMap<String,Any>();
        for(var k : config.keySet()) {
            m.put(k, Extension.toAny(config.get(k)));
        }
        synchronized(extension) {
            client.setChartConfig(id, chartID, Any.mValue(m));
        }
    }

    public void setChartData(String chartID, Map<String, Data> dataset)
            throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.setChartData(id, chartID, dataset, false);
        }
    }

    public void setChartData(String chartID, Map<String, Data> dataset, boolean right)
            throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.setChartData(id, chartID, dataset, right);
        }
    }

    public Map<String, Data> getChartData(String chartID)
        throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.getChartData(id, chartID, false);
        }
    }

    public Map<String, Data> getChartData(String chartID, boolean right)
        throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.getChartData(id, chartID, right);
        }
    }

    public void addChartKey(String chartID, String key, Data data)
            throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.addChartKey(id, chartID, key, data, false);
        }
    }

    public void addChartKey(String chartID, String key, Data data, boolean right)
            throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.addChartKey(id, chartID, key, data, right);
        }
    }
    
    public void removeChartKey(String chartID, String key)
            throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.removeChartKey(id, chartID, key, false);
        }
    }

    public void removeChartKey(String chartID, String key, boolean right)
            throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.removeChartKey(id, chartID, key, right);
        }
    }

    public void hideChartKey(String chartID, String key)
            throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.hideChartKey(id, chartID, key, true, false);
        }
    }

    public void hideChartKey(String chartID, String key, boolean hidden)
            throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.hideChartKey(id, chartID, key, hidden, false);
        }
    }

    public void hideChartKey(String chartID, String key, boolean hidden, boolean right)
            throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.hideChartKey(id, chartID, key, hidden, right);
        }
    }

    public void appendChartPoint(String chartID, String key, DataPoint pt)
            throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.appendChartPoints(id, chartID, key, Arrays.<DataPoint>asList(pt), false);
        }
    }

    public void appendChartPoint(String chartID, String key, DataPoint pt, boolean right)
            throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.appendChartPoints(id, chartID, key, Arrays.<DataPoint>asList(pt), right);
        }
    }

    public void appendChartPoints(String chartID, String key, List<DataPoint> pts)
            throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.appendChartPoints(id, chartID, key, pts, false);
        }
    }

    public void appendChartPoints(String chartID, String key, List<DataPoint> pts, boolean right)
            throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.appendChartPoints(id, chartID, key, pts, right);
        }
    }

    public void incrementChartKey(String chartID, String key)
            throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.incrementChartKey(id, chartID, key, 1.0);
        }
    }

    public void decrementChartKey(String chartID, String key)
            throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.incrementChartKey(id, chartID, key, -1.0);
        }
    }

    public void incrementChartKey(String chartID, String key, double value)
            throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.incrementChartKey(id, chartID, key, value);
        }
    }

    public void decrementChartKey(String chartID, String key, double value)
            throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.incrementChartKey(id, chartID, key, -value);
        }
    }

    /*
    // image export not implemented with C++ charting elements
    public String exportChartImage(String chartID, String imageFileName)
            throws IOException, IllegalArgument, TException
    {
        String fullImagePath = client.exportChartImage(id, chartID, imageFileName);
        Path imgPath = Paths.get(fullImagePath);
        if (!Files.exists(imgPath) || Files.isDirectory(imgPath)) {
            // read the image and write it locally
            ByteBuffer imgBuf = client.exportChartImageData(id, chartID, imageFileName);
            byte [] bytebuf = new byte[imgBuf.remaining()];
            imgBuf.get(bytebuf);
            Files.write(Paths.get(imageFileName), bytebuf);
            return imageFileName;
        } else {
            return fullImagePath;
        }
    }

    public ByteBuffer exportChartImageData(String chartID, String imageFileName)
            throws IllegalArgument, TException
    {
        return client.exportChartImageData(id, chartID, imageFileName);
    }
    */

    public void notice(String title, String message, String log) throws TException
    {
        synchronized(extension) {
            client.notice(id, title, message, log);
        }
    }
    public void notice(String title, String message) throws TException
    { notice(title, message, ""); }

    public void dispNotice(Disposition disposition, String title, String message, String log) throws TException
    {
        synchronized(extension) {
            client.dispNotice(id, disposition, title, message, log);
        }
    }
    public void dispNotice(Disposition disposition, String title, String message) throws TException
    { dispNotice(disposition, title, message, ""); }

    public void error(String title, String message, String log) throws TException
    {
        synchronized(extension) {
            client.error(id, title, message, log);
        }
    }
    public void error(String title, String message) throws TException
    { error(title, message, ""); }


    public void popupDialog(String identifier, String title, String message, String positiveOption, String negativeOption) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.popupDialog(id, identifier, title, message, positiveOption, negativeOption);
        }
    }

    public void cancelPopupDialog(String identifier) throws TException
    {
        synchronized(extension) {
            client.cancelPopupDialog(id, identifier);
        }
    }
    
    public String insertInstructionAtSelectedLine(String instruction) throws TException
    {
        synchronized(extension) {
            return client.insertInstructionAtSelectedLine(id, instruction);
        }
    }

    public void displayScreen(String identifier) throws TException
    {
        synchronized(extension) {
            client.displayScreen(id, identifier);
        }
    }

    public void displayHelp(String title, String htmlContentFile) throws TException
    {
        synchronized(extension) {
            client.displayHelp(id, title, htmlContentFile);
        }
    }
    
    // Event consumer functions

    public synchronized void addEventConsumer(PendantEventType eventType, Consumer<yaskawa.ext.api.PendantEvent> c) throws TException
    {
        if (!eventConsumers.containsKey(eventType))
            eventConsumers.put(eventType, new ArrayList<Consumer<yaskawa.ext.api.PendantEvent>>());
        eventConsumers.get(eventType).add(c);        

        subscribeEventTypes(Set.of( eventType ));
    }

    public synchronized void addItemEventConsumer(String itemName, PendantEventType eventType, Consumer<yaskawa.ext.api.PendantEvent> c) throws TException
    {
        if (!itemEventConsumers.containsKey(eventType))
            itemEventConsumers.put(eventType, new HashMap<String, ArrayList<Consumer<yaskawa.ext.api.PendantEvent>>>());
        var consumers = itemEventConsumers.get(eventType);
        if (!consumers.containsKey(itemName))
            consumers.put(itemName, new ArrayList<Consumer<yaskawa.ext.api.PendantEvent>>());
        consumers.get(itemName).add(c);

        if(eventType != PendantEventType.VisibleChanged)
            subscribeEventTypes(Set.of( eventType ));
        else
            subscribeItemEventTypes(Set.of( itemName ), Set.of( eventType ));
    }

    // invoke consumer callbacks relevant to event
    public synchronized void handleEvent(PendantEvent e) throws InvalidID, TException, IllegalArgument, RuntimeException
    {
        // an event we have a consumer for?
        if (eventConsumers.containsKey(e.getEventType())) {
            for(Consumer<yaskawa.ext.api.PendantEvent> consumer : eventConsumers.get(e.getEventType())) 
                consumer.accept(e);
        }

        // is this event from a YML item?   
        var props = e.getProps();     
        if (e.isSetProps() && (props.containsKey("item") || props.containsKey("identifier"))) {
            // do we have a consumer for this event type & item ?
            if (itemEventConsumers.containsKey(e.getEventType())) {
                var consumers = itemEventConsumers.get(e.getEventType());
                String itemName = props.containsKey("item") ?
                                       props.get("item").getSValue()
                                     : props.get("identifier").getSValue();
                if (consumers.containsKey(itemName)) {                    
                    for(Consumer<yaskawa.ext.api.PendantEvent> consumer : consumers.get(itemName)) 
                        consumer.accept(e);
                }
            }
        }
    }

    protected Extension extension;
    protected yaskawa.ext.api.Pendant.Client client;
    protected long id;

    protected HashMap<PendantEventType, ArrayList<Consumer<yaskawa.ext.api.PendantEvent>>> eventConsumers;
    protected HashMap<PendantEventType, HashMap<String, ArrayList<Consumer<yaskawa.ext.api.PendantEvent>>>> itemEventConsumers;
}

