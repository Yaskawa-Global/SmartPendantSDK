using System;
using Thrift.Protocol;
using Yaskawa.Ext.API;

namespace Yaskawa.Ext
{
    public class Robot 
    {
        public Robot(Controller c, Extension ext, TProtocol protocol, int index)
        {
            this.c = c;
            this.index = index;
            extension = ext;


            lock (extension.SyncRoot)
                client = new API.Robot.Client(protocol);
        }
        
        public String model()
        {
            lock (extension.SyncRoot)
                return client.model(index).Result;
        }

        public int dof()
        {
            lock (extension.SyncRoot)
                return client.dof(index).Result;
        }

        public Position jointPosition(OrientationUnit unit)
        {
            lock (extension.SyncRoot)
                return client.jointPosition(index, unit).Result;
        }

        public Position toolTipPosition(CoordinateFrame frame, int tool)
        {
            lock (extension.SyncRoot)
                return client.toolTipPosition(index, frame, tool).Result;
        }


        public bool forceLimitingAvailable()
        {
            lock (extension.SyncRoot)
                return client.forceLimitingAvailable(index).Result;
        }

        public bool forceLimitingActive()
        {
            lock (extension.SyncRoot)
                return client.forceLimitingActive(index).Result;
        }

        public bool forceLimitingStopped()
        {
            lock (extension.SyncRoot)
                return client.forceLimitingStopped(index).Result;
        }

        public bool switchBoxAvailable()
        {
            lock (extension.SyncRoot)
                return client.switchBoxAvailable(index).Result;
        }

        public int activeTool()
        {
            lock (extension.SyncRoot)
                return client.activeTool(index).Result;
        }

        public void setActiveTool(int tool)
        {
            lock (extension.SyncRoot)
                client.setActiveTool(index, tool).Wait();
        }

        public Position workHomePosition()
        {
            lock (extension.SyncRoot)
                return client.workHomePosition(index).Result;
        }

        public void setWorkHomePosition(Position pos)
        {
            lock (extension.SyncRoot)
                client.setWorkHomePosition(index, pos).Wait();
        }

        public Position secondHomePosition()
        {
            lock (extension.SyncRoot)
                return client.secondHomePosition(index).Result;
        }

        public void setSecondHomePosition(Position pos)
        {
            lock (extension.SyncRoot)
                client.setSecondHomePosition(index, pos).Wait();
        }

        public double maximumLinearSpeed()
        {
            lock (extension.SyncRoot)
                return client.maximumLinearSpeed(index).Result;
        }


        protected Controller c;
        protected API.Robot.Client client;
        protected int index;

        protected object SyncRoot
        {
            get;
            private set;
        }
        Extension extension;
    }
}