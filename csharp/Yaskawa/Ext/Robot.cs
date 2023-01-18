using System;
using Thrift.Protocol;
using Yaskawa.Ext.API;

namespace Yaskawa.Ext
{
    public class Robot 
    {
        public Robot(Controller c, TProtocol protocol, int index)
        {
            this.c = c;
            this.index = index;
            client = new API.Robot.Client(protocol);
        }
        
        public String model()
        {
            return client.model(index).Result;
        }

        public int dof()
        {
            return client.dof(index).Result;
        }

        public Position jointPosition(OrientationUnit unit)
        {
            return client.jointPosition(index, unit).Result;
        }

        public Position toolTipPosition(CoordinateFrame frame, int tool)
        {
            return client.toolTipPosition(index, frame, tool).Result;
        }


        public bool forceLimitingAvailable()
        {
            return client.forceLimitingAvailable(index).Result;
        }

        public bool forceLimitingActive()
        {
            return client.forceLimitingActive(index).Result;
        }

        public bool forceLimitingStopped()
        {
            return client.forceLimitingStopped(index).Result;
        }

        public bool switchBoxAvailable()
        {
            return client.switchBoxAvailable(index).Result;
        }

        public int activeTool()
        {
            return client.activeTool(index).Result;
        }

        public void setActiveTool(int tool)
        {
            client.setActiveTool(index, tool).Wait();
        }

        public Position workHomePosition()
        {
            return client.workHomePosition(index).Result;
        }

        public void setWorkHomePosition(Position pos)
        {
            client.setWorkHomePosition(index, pos).Wait();
        }

        public Position secondHomePosition()
        {
            return client.secondHomePosition(index).Result;
        }

        public void setSecondHomePosition(Position pos)
        {
            client.setSecondHomePosition(index, pos).Wait();
        }

        public double maximumLinearSpeed()
        {
            return client.maximumLinearSpeed(index).Result;
        }


protected Controller c;
        protected API.Robot.Client client;
        protected int index;
    }
}