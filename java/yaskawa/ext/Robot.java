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
        synchronized(c.extension) {
            return client.model(index);
        }
    }

    public int dof() throws TException
    {
        synchronized(c.extension) {
            return client.dof(index);
        }
    }

    public Position jointPosition(OrientationUnit unit) throws TException
    {
        synchronized(c.extension) {
            return client.jointPosition(index, unit);
        }
    }

    public Position toolTipPosition(CoordinateFrame frame, int tool) throws TException
    {
        synchronized(c.extension) {
            return client.toolTipPosition(index, frame, tool);
        }
    }


    public boolean forceLimitingAvailable() throws TException
    {
        synchronized(c.extension) {
            return client.forceLimitingAvailable(index);
        }
    }

    public boolean forceLimitingActive() throws TException
    {
        synchronized(c.extension) {
            return client.forceLimitingActive(index);
        }
    }

    public boolean forceLimitingStopped() throws TException
    {
        synchronized(c.extension) {
            return client.forceLimitingStopped(index);
        }
    }

    public boolean switchBoxAvailable() throws TException
    {
        synchronized(c.extension) {
            return client.switchBoxAvailable(index);
        }
    }

    public int activeTool() throws TException
    {
        synchronized(c.extension) {
            return client.activeTool(index);
        }
    }

    public void setActiveTool(int tool) throws TException
    {
        synchronized(c.extension) {
            client.setActiveTool(index, tool);
        }
    }

    public Position workHomePosition() throws TException
    {
        synchronized(c.extension) {
            return client.workHomePosition(index);
        }
    }

    public void setWorkHomePosition(Position pos) throws TException
    {
        synchronized(c.extension) {
            client.setWorkHomePosition(index, pos);
        }
    }

    public Position secondHomePosition() throws TException
    {
        synchronized(c.extension) {
            return client.secondHomePosition(index);
        }
    }

    public void setSecondHomePosition(Position pos) throws TException
    {
        synchronized(c.extension) {
            client.setSecondHomePosition(index, pos);
        }
    }


    protected Controller c;
    protected yaskawa.ext.api.Robot.Client client;
    protected int index;
}

