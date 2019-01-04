
package yaskawa.ext;

import java.util.*;
import org.apache.thrift.TException;
//import org.apache.thrift.TTransportException;
import org.apache.thrift.protocol.TProtocol;

import yaskawa.ext.api.*;

public class Pendant
{
    Pendant(Extension ext, TProtocol protocol, long id) //throws org.apache.thrift.TTransportException
    {
        extension = ext;
        client = new yaskawa.ext.api.Pendant.Client(protocol);
        this.id = id;
    }

    public void subscribeEventTypes(Set<PendantEventType> types) throws TException
    {
        client.subscribeEventTypes(id, types);
    }

    public void unsubscribeEventTypes(Set<PendantEventType> types) throws TException
    {
        client.unsubscribeEventTypes(id, types);
    }

    public List<yaskawa.ext.api.PendantEvent> events() throws TException
    {
        return client.events(id);
    }

    public String currentLanguage() throws TException
    {
        return client.currentLanguage(id);
    }

    public String currentLocale() throws TException
    {
        return client.currentLocale(id);
    }

    public String currentScreenName() throws TException
    {
        return client.currentScreenName(id);
    }

    public List<String> registerYML(String ymlSource) throws TException
    {
        return client.registerYML(id, ymlSource);
    }

    public void registerUtilityWindow(String identifier, boolean integrated, String itemType, String menuItemName, String windowTitle, UtilityWindowWidth widthFormat, UtilityWindowHeight heightFormat, UtilityWindowExpansion sizeExpandability) throws TException
    {
        client.registerUtilityWindow(id, identifier, integrated, itemType, menuItemName, windowTitle, widthFormat, heightFormat, sizeExpandability);
    }

    // get property overloads
    public boolean boolProperty(String itemID, String name) throws IllegalArgument, TException
    {
        return client.boolProperty(id, itemID, name);
    }

    public long intProperty(String itemID, String name) throws IllegalArgument, TException
    {
        return client.intProperty(id, itemID, name);
    }

    public double realProperty(String itemID, String name) throws IllegalArgument, TException
    {
        return client.realProperty(id, itemID, name);
    }

    public String stringProperty(String itemID, String name) throws IllegalArgument, TException
    {
        return client.stringProperty(id, itemID, name);
    }

    // TODO: can we do:
    // public <T> T getProperty(String itemID, String name) throws IllegalArgument, TException
    // {

    // }


    // set property overloads
    public void setProperty(String itemID, String name, boolean value) throws IllegalArgument, TException
    {
        client.setBoolProperty(id, itemID, name, value);
    }

    public void setProperty(String itemID, String name, long value) throws IllegalArgument, TException
    {
        client.setIntProperty(id, itemID, name, value);
    }

    public void setProperty(String itemID, String name, double value) throws IllegalArgument, TException
    {
        client.setRealProperty(id, itemID, name, value);
    }

    public void setProperty(String itemID, String name, String value) throws IllegalArgument, TException
    {
        client.setStringProperty(id, itemID, name, value);
    }


    protected Extension extension;
    protected yaskawa.ext.api.Pendant.Client client;
    protected long id;
}

