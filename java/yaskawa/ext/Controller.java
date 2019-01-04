
package yaskawa.ext;

import java.util.*;
import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.protocol.TProtocol;

import yaskawa.ext.api.*;


public class Controller
{
    Controller(Extension ext, TProtocol protocol, long id) throws TTransportException
    {
        extension = ext;
        client = new yaskawa.ext.api.Controller.Client(protocol);
        this.id = id;
    }

    public void connect(String hostName) throws TException
    {
        client.connect(id, hostName);
    }

    public void disconnect() throws TException
    {
        client.disconnect(id);
    }

    public void subscribeEventTypes(Set<ControllerEventType> types) throws TException
    {
        client.subscribeEventTypes(id, types);
    }

    public void unsubscribeEventTypes(Set<ControllerEventType> types) throws TException
    {
        client.unsubscribeEventTypes(id, types);
    }

    public List<yaskawa.ext.api.ControllerEvent> events() throws TException
    {
        return client.events(id);
    }

    public boolean connected() throws TException
    {
        return client.connected(id);
    }

    //string connectedHostName(long c);
    public String softwareVersion() throws TException
    {
        return client.softwareVersion(id);
    }

    public boolean monitoring() throws TException
    {
        return client.monitoring(id);
    }

    public boolean haveExclusiveControl() throws TException
    {
        return client.haveExclusiveControl(id);
    }

    //Dictionary<sbyte, int> robots(long c);
    public int currentRobot() throws TException
    {
        return client.currentRobot(id);
    }


    protected Extension extension;
    protected yaskawa.ext.api.Controller.Client client;
    protected long id;
}

