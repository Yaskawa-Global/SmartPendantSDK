package yaskawa.ext;

import java.util.*;
import java.util.function.*;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;

import yaskawa.ext.api.*;


public class Robot 
{
    Robot(Controller c, TProtocol protocol, int index) throws TTransportException
    {
        this.c = c;
        this.index = index;
        client = new yaskawa.ext.api.Robot.Client(protocol);
    }

    public String model() throws TException
    {
        return client.model(index);
    }

    public int dof() throws TException
    {
        return client.dof(index);
    }

    public boolean forceLimitingAvailable() throws TException
    {
        return client.forceLimitingAvailable(index);
    }

    public boolean forceLimitingActive() throws TException
    {
        return client.forceLimitingActive(index);
    }

    public boolean forceLimitingStopped() throws TException
    {
        return client.forceLimitingStopped(index);
    }

    public boolean switchBoxAvailable() throws TException
    {
        return client.switchBoxAvailable(index);
    }

    public int activeTool() throws TException
    {
        return client.activeTool(index);
    }

    public void setActiveTool(int tool) throws TException
    {
        client.setActiveTool(index, tool);
    }


    protected Controller c;
    protected yaskawa.ext.api.Robot.Client client;
    protected int index;
}

