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
            return client.model(index);
        }

        public int dof()
        {
            return client.dof(index);
        }

        public Position jointPosition(OrientationUnit unit)
        {
            return client.jointPosition(index, unit);
        }

        public Position toolTipPosition(CoordinateFrame frame, int tool)
        {
            return client.toolTipPosition(index, frame, tool);
        }


        public bool forceLimitingAvailable()
        {
            return client.forceLimitingAvailable(index);
        }

        public bool forceLimitingActive()
        {
            return client.forceLimitingActive(index);
        }

        public bool forceLimitingStopped()
        {
            return client.forceLimitingStopped(index);
        }

        public bool switchBoxAvailable()
        {
            return client.switchBoxAvailable(index);
        }

        public int activeTool()
        {
            return client.activeTool(index);
        }

        public void setActiveTool(int tool)
        {
            client.setActiveTool(index, tool);
        }


        protected Controller c;
        protected API.Robot.Client client;
        protected int index;
    }
}