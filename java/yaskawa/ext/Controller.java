
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

    public String currentJob() throws TException
    {
        return client.currentJob(id);
    }

    public String defaultJob() throws TException
    {
        return client.defaultJob(id);
    }

    // IO

    public int inputNumber(String name) throws IllegalArgument, TException
    {
        return client.inputNumber(id, name);
    }

    public int inputGroupNumber(String name) throws IllegalArgument, TException
    {
        return client.inputGroupNumber(id, name);
    }

    public int outputNumber(String name) throws IllegalArgument, TException    
    {
        return client.outputNumber(id, name);
    }
    
    public int outputGroupNumber(String name) throws IllegalArgument, TException
    {
        return client.outputGroupNumber(id, name);
    }
    
    public String inputName(int num) throws IllegalArgument, TException
    {
        return client.inputName(id, num);
    }

    public String outputName(int num) throws IllegalArgument, TException
    {
        return client.outputName(id, num);
    }

    public void setInputName(int num, String name) throws TException
    {
        client.setInputName(id, num, name);
    }

    public void setOutputName(int num, String name) throws TException
    {
        client.setOutputName(id, num, name);
    }

    public void monitorInput(int num) throws IllegalArgument, TException
    {
        client.monitorInput(id, num);
    }

    public void monitorInputGroups(int groupNum, int count) throws IllegalArgument, TException
    {
        client.monitorInputGroups(id, groupNum, count);
    }

    public void monitorOutput(int num) throws IllegalArgument, TException
    {
        client.monitorOutput(id, num);
    }

    public void monitorOutputGroups(int groupNum, int count) throws IllegalArgument, TException
    {
        client.monitorOutputGroups(id, groupNum, count);
    }

    public void unmonitorInput(int num) throws TException
    {
        client.unmonitorInput(id, num);
    }

    public void unmonitorInputGroups(int groupNum, int count) throws TException
    {
        client.unmonitorInputGroups(id, groupNum, count);
    }

    public void unmonitorOutput(int num) throws TException
    {
        client.unmonitorOutput(id, num);
    }

    public void unmonitorOutputGroups(int groupNum, int count) throws TException
    {
        client.unmonitorOutputGroups(id, groupNum, count);
    }

    public boolean inputValue(int num) throws IllegalArgument, TException
    {
        return client.inputValue(id, num);
    }

    public int inputGroupsValue(int groupNum, int count) throws IllegalArgument, TException
    {
        return client.inputGroupsValue(id, groupNum, count);
    }

    public boolean outputValue(int num) throws IllegalArgument, TException
    {
        return client.outputValue(id, num);
    }

    public int outputGroupsValue(int groupNum, int count) throws IllegalArgument, TException
    {
        return client.outputGroupsValue(id, groupNum, count);
    }

    public void setOutput(int num, boolean value) throws TException
    {
        client.setOutput(id, num, value);
    }

    public void setOutputGroups(int groupNum, int count, int value) throws TException
    {
        client.setOutputGroups(id, groupNum, count, value);
    }

    public int inputAddress(String name) throws IllegalArgument, TException
    {
        return client.inputAddress(id, name);
    }

    public int inputAddressByNumber(int num) throws IllegalArgument, TException
    {
        return client.inputAddressByNumber(id, num);
    }

    public int outputAddress(String name) throws IllegalArgument, TException
    {
        return client.outputAddress(id, name);
    }

    public int outputAddressByNumber(int num) throws IllegalArgument, TException
    {
        return client.outputAddressByNumber(id, num);
    }

    public void monitorIOAddress(int address) throws IllegalArgument, TException
    {
        client.monitorIOAddress(id, address);
    }

    public void unmonitorIOAddress(int address) throws TException
    {
        client.unmonitorIOAddress(id, address);
    }

    public boolean inputAddressValue(int address) throws IllegalArgument, TException
    {
        return client.inputAddressValue(id, address);
    }

    public boolean outputAddressValue(int address) throws IllegalArgument, TException
    {
        return client.outputAddressValue(id, address);
    }

    public void setOutputAddress(int address, boolean value) throws TException
    {
        client.setOutputAddress(id, address, value);
    }


    protected Extension extension;
    protected yaskawa.ext.api.Controller.Client client;
    protected long id;
}

