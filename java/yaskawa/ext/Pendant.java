
package yaskawa.ext;

import java.util.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.ByteBuffer;

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

    public void registerImageFile(String imageFileName) throws IllegalArgument, TException, IOException
    {
        try {
            client.registerImageFile(id, imageFileName);
        } catch (Exception e) {
            // something went wrong - possible file isn't accessible from service end, so send data over API 
            var imageBytes = Files.readAllBytes(Paths.get(imageFileName));
            client.registerImageData(id, ByteBuffer.wrap(imageBytes), imageFileName);
        }
    }
    public void registerImageData(java.nio.ByteBuffer imageData, String imageName) throws IllegalArgument, TException
    {
        client.registerImageData(id, imageData, imageName);
    }

    public void registerUtilityWindow(String identifier, boolean integrated, String itemType, String menuItemName, String windowTitle, UtilityWindowWidth widthFormat, UtilityWindowHeight heightFormat, UtilityWindowExpansion sizeExpandability) throws TException
    {
        client.registerUtilityWindow(id, identifier, integrated, itemType, menuItemName, windowTitle, widthFormat, heightFormat, sizeExpandability);
    }

    public void registerIntegration(String identifier, IntegrationPoint integrationPoint, String itemType, String buttonLabel, String buttonImage) throws IllegalArgument, TException
    {
        client.registerIntegration(id, identifier, integrationPoint, itemType, buttonLabel, buttonImage);
    }



    public Any property(String itemID, String name) throws IllegalArgument, TException
    {
        return client.property(id, itemID, name);
    }

    public void setProperty(String itemID, String name, Any value) throws IllegalArgument, TException
    {
        client.setProperty(id, itemID, name, value);
    }
    // convenience overloads
    public void setProperty(String itemID, String name, boolean value) throws IllegalArgument, TException
    {
        client.setProperty(id, itemID, name, Any.bValue(value));
    }
    public void setProperty(String itemID, String name, int value) throws IllegalArgument, TException
    {
        client.setProperty(id, itemID, name, Any.iValue((long)value));
    }
    public void setProperty(String itemID, String name, long value) throws IllegalArgument, TException
    {
        client.setProperty(id, itemID, name, Any.iValue(value));
    }
    public void setProperty(String itemID, String name, double value) throws IllegalArgument, TException
    {
        client.setProperty(id, itemID, name, Any.rValue(value));
    }
    public void setProperty(String itemID, String name, String value) throws IllegalArgument, TException
    {
        client.setProperty(id, itemID, name, Any.sValue(value));
    }


    

    public void notice(String title, String message, String log) throws TException
    {
        client.notice(id, title, message, log);
    }
    public void notice(String title, String message) throws TException
    { notice(title, message, ""); }

    public void error(String title, String message, String log) throws TException
    {
        client.error(id, title, message, log);
    }
    public void error(String title, String message) throws TException
    { error(title, message, ""); }


    public void popupDialog(String identifier, String title, String message, String positiveOption, String negativeOption) throws IllegalArgument, TException
    {
        client.popupDialog(id, identifier, title, message, positiveOption, negativeOption);
    }

    public void cancelPopupDialog(String identifier) throws TException
    {
        client.cancelPopupDialog(id, identifier);
    }



    protected Extension extension;
    protected yaskawa.ext.api.Pendant.Client client;
    protected long id;
}

