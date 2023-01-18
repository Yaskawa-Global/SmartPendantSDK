using System;
using System.Collections.Generic;
using Thrift.Protocol;
using Thrift.Collections;
using Yaskawa.Ext.API;
using System.Net;


namespace Yaskawa.Ext
{
    public class Controller
    {
        internal Controller(Extension ext, TProtocol protocol, long id)
        {
            extension = ext;
            client = new API.Controller.Client(protocol);
            this.id = id;
            eventConsumers = new Dictionary<ControllerEventType, List<Action<ControllerEvent>>>();
        }

        public bool requestPermissions(HashSet<string> permissions)
        {
            return client.requestPermissions(id, permissions).Result;
        }

        public bool havePermission(String permission)
        {
            return client.havePermission(id, permission).Result;
        }

        public void relinquishPermissions(HashSet<string> permissions)
        {
            client.relinquishPermissions(id, permissions).Wait();
        }

        public void connect(string hostName)
        {
            client.connect(id, hostName).Wait();
        }

        public void disconnect()
        {
            client.disconnect(id).Wait();
        }

        public void subscribeEventTypes(HashSet<ControllerEventType> types)
        {
            client.subscribeEventTypes(id, types).Wait();
        }

        public void unsubscribeEventTypes(HashSet<ControllerEventType> types)
        {
            client.unsubscribeEventTypes(id, types).Wait();
        }

        public List<ControllerEvent> events()
        {
            return client.events(id).Result;
        }

        public bool connected()
        {
            return client.connected(id).Result;
        }

        public String connectedHostName()
        {
            return client.connectedHostName(id).Result;
        }

        public string softwareVersion()
        {
            return client.softwareVersion(id).Result;
        }

        public bool monitoring()
        {
            return client.monitoring(id).Result;
        }

        public bool haveExclusiveControl()
        {
            return client.haveExclusiveControl(id).Result;
        }

        public OperationMode operationMode()
        {
            return client.operationMode(id).Result;
        }

        public ServoState servoState()
        {
            return client.servoState(id).Result;
        }

        public PlaybackState playbackState()
        {
            return client.playbackState(id).Result;
        }

        public PlaybackCycle playbackCycle()
        {
            return client.playbackCycle(id).Result;
        }

        public void setPlaybackCycle(PlaybackCycle cycle)
        {
            client.setPlaybackCycle(id, cycle).Wait();
        }

public void run()
        {
            client.run(id).Wait();
        }

        public void pause()
        {
            client.pause(id).Wait();
        }

        public void resume()
        {
            client.resume(id).Wait();
        }

        public void stop()
        {
            client.stop(id).Wait();
        }


        // Jobs

        public String currentJob()
        {
            return client.currentJob(id).Result;
        }

        public void setCurrentJob(String name, int line)
        {
            client.setCurrentJob(id, name, line).Wait();
        }

        public String defaultJob()
        {
            return client.defaultJob(id).Result;
        }

        public bool jobExists(String name)
        {
            return client.jobExists(id, name).Result;
        }

        public RobotJobInfo jobDetails(String name)
        {
            return client.jobDetails(id, name).Result;
        }

        public List<String> jobs()
        {
            return client.jobs(id).Result;
        }

        public List<String> jobsMatching(String nameRegex, String tag)
        {
            return client.jobsMatching(id, nameRegex, tag).Result;
        }

        public void duplicateJob(String existingName, String newName)
        {
            client.duplicateJob(id, existingName, newName).Wait();
        }

        public void deleteJob(String name)
        {
            client.deleteJob(id, name).Wait();
        }

        public String jobSource(String name)
        {
            return client.jobSource(id, name).Result;
        }

        public void storeJobSource(String name, String programmingLanguage, String sourceCode)

        {
            client.storeJobSource(id, name, programmingLanguage, sourceCode).Wait();
        }


        // Tools

        public Dictionary<int, String> tools()
        {
            return client.tools(id).Result;
        }

        public Tool tool(int index)
        {
            return client.tool(id, index).Result;
        }

        // IO

        public int inputNumber(String name)
        {
            return client.inputNumber(id, name).Result;
        }

        public int inputGroupNumber(String name)
        {
            return client.inputGroupNumber(id, name).Result;
        }

        public int outputNumber(String name)
        {
            return client.outputNumber(id, name).Result;
        }

        public int outputGroupNumber(String name)
        {
            return client.outputGroupNumber(id, name).Result;
        }

        public String inputName(int num)
        {
            return client.inputName(id, num).Result;
        }

        public String outputName(int num)
        {
            return client.outputName(id, num).Result;
        }

        public void setInputName(int num, String name)
        {
            client.setInputName(id, num, name).Wait();
        }

        public void setOutputName(int num, String name)
        {
            client.setOutputName(id, num, name).Wait();
        }

        public void monitorInput(int num)
        {
            client.monitorInput(id, num).Wait();
        }

        public void monitorInputGroups(int groupNum, int count)
        {
            client.monitorInputGroups(id, groupNum, count).Wait();
        }

        public void monitorOutput(int num)
        {
            client.monitorOutput(id, num).Wait();
        }

        public void monitorOutputGroups(int groupNum, int count)
        {
            client.monitorOutputGroups(id, groupNum, count).Wait();
        }

        public void unmonitorInput(int num)
        {
            client.unmonitorInput(id, num).Wait();
        }

        public void unmonitorInputGroups(int groupNum, int count)
        {
            client.unmonitorInputGroups(id, groupNum, count).Wait();
        }

        public void unmonitorOutput(int num)
        {
            client.unmonitorOutput(id, num).Wait();
        }

        public void unmonitorOutputGroups(int groupNum, int count)
        {
            client.unmonitorOutputGroups(id, groupNum, count).Wait();
        }

        public bool inputValue(int num)
        {
            return client.inputValue(id, num).Result;
        }

        public int inputGroupsValue(int groupNum, int count)
        {
            return client.inputGroupsValue(id, groupNum, count).Result;
        }

        public bool outputValue(int num)
        {
            return client.outputValue(id, num).Result;
        }

        public int outputGroupsValue(int groupNum, int count)
        {
            return client.outputGroupsValue(id, groupNum, count).Result;
        }

        public void setOutput(int num, bool value)
        {
            client.setOutput(id, num, value).Wait();
        }

        public void setOutputGroups(int groupNum, int count, int value)
        {
            client.setOutputGroups(id, groupNum, count, value).Wait();
        }

        public int inputAddress(String name)
        {
            return client.inputAddress(id, name).Result;
        }

        public int inputAddressByNumber(int num)
        {
            return client.inputAddressByNumber(id, num).Result;
        }

        public int outputAddress(String name)
        {
            return client.outputAddress(id, name).Result;
        }

        public int outputAddressByNumber(int num)
        {
            return client.outputAddressByNumber(id, num).Result;
        }

        public void monitorIOAddress(int address)
        {
            client.monitorIOAddress(id, address).Wait();
        }

        public void unmonitorIOAddress(int address)
        {
            client.unmonitorIOAddress(id, address).Wait();
        }

        public bool inputAddressValue(int address)
        {
            return client.inputAddressValue(id, address).Result;
        }

        public bool outputAddressValue(int address)
        {
            return client.outputAddressValue(id, address).Result;
        }

        public bool ioAddressValue(int address)
        {
            return client.ioAddressValue(id, address).Result;
        }

    public void setOutputAddress(int address, bool value)
        {
            client.setOutputAddress(id, address, value).Wait();
        }

        public int fieldBusStatusInputGroup(String busType)
        {
            return client.fieldBusStatusInputGroup(id, busType).Result;
        }

        public List<ControlGroup> controlGroups()
        {
            return client.controlGroups(id).Result;
        }

        public sbyte currentControlGroup()
        {
            return client.currentControlGroup(id).Result;
        }

        public sbyte robotCount()
        {
            return client.robotCount(id).Result;
        }

        public int currentRobotIndex()
        {
            return client.currentRobot(id).Result;
        }

        //Dictionary<sbyte, int> robots(long c);
        public Robot currentRobot()
        {
            return new Robot(this, robotProtocol, currentRobotIndex());
            //return client.currentRobot(id);
        }
        public Any variable(String name)
        {
            return client.variable(id, name).Result;
        }

        public Any variableByAddr(VariableAddress addr)
        {
            return client.variableByAddr(id, addr).Result;
        }

        public void monitorVariable(VariableAddress addr)
        {
            client.monitorVariable(id, addr).Wait();
        }

        public void unmonitorVariable(VariableAddress addr)
        {
            client.unmonitorVariable(id, addr).Wait();
        }

public void setVariable(String name, Any value) 
        {
            client.setVariable(id, name, value).Wait();
        }

        public void setVariable(String name, bool value)
        {
            Any a = new Any();
            a.BValue = value;
            client.setVariable(id, name, a).Wait();
        }

        public void setVariable(String name, long value)
        {
            Any a = new Any();
            a.IValue = value;
            client.setVariable(id, name, a).Wait();
        }

        public void setVariable(String name, double value)
        {
            Any a = new Any();
            a.RValue = value;
            client.setVariable(id, name, a).Wait();
        }

        public void setVariable(String name, String value)
        {
            Any a = new Any();
            a.SValue = value;
            client.setVariable(id, name, a).Wait();
        }
        public void setVariable(String name, Position value)
        {
            Any a = new Any();
            a.PValue = value;
            client.setVariable(id, name, a).Wait();
        }

        public void setVariableByAddr(VariableAddress addr, Any value)
        {
            client.setVariableByAddr(id, addr, value).Wait();
        }

        public void setVariableByAddr(VariableAddress addr, bool value)
        {
            Any a = new Any();
            a.BValue = value;
            client.setVariableByAddr(id, addr, a).Wait();
        }

        public void setVariableByAddr(VariableAddress addr, long value)
        {
            Any a = new Any();
            a.IValue = value;
            client.setVariableByAddr(id, addr, a).Wait();
        }

        public void setVariableByAddr(VariableAddress addr, double value)
        {
            Any a = new Any();
            a.RValue = value;
            client.setVariableByAddr(id, addr, a).Wait();
        }

        public void setVariableByAddr(VariableAddress addr, String value)
        {
            Any a = new Any();
            a.SValue = value;
            client.setVariableByAddr(id, addr, a).Wait();
        }

        public void setVariableByAddr(VariableAddress addr, Position value)
        {
            Any a = new Any();
            a.PValue = value;
            client.setVariableByAddr(id, addr, a).Wait();
        }

        public VariableAddress variableAddrByNameAndSpace(String name, AddressSpace space)
        {
            return client.variableAddrByNameAndSpace(id, name, space).Result;
        }

        public VariableAddress variableAddrByName(String name)
        {
            return client.variableAddrByName(id, name).Result;
        }

        public void setVariableName(VariableAddress addr, String name)
        {
            client.setVariableName(id, addr, name).Wait();
        }

        public Zone zone(int index)
        {
            return client.zone(id, index).Result;
        }

        public int newZone()
        {
            return client.newZone(id).Result;
        }

        public void modifyZone(int index, Zone z)
        {
            client.modifyZone(id, index, z).Wait();
        }

        public void deleteZone(int index)
        {
            client.deleteZone(id, index).Wait();
        }


        // User frames

        public Dictionary<int, String> userFrames()
        {
            return client.userFrames(id).Result;
        }

        public CoordinateFrame userFrame(int index)
        {
            return client.userFrame(id, index).Result;
        }

        public int newUserFrame()
        {
            return client.newUserFrame(id).Result;
        }

        public void setUserFrame(int index, CoordinateFrame f)
        {
            client.setUserFrame(id, index, f).Wait();
        }

        public void deleteUserFrame(int index)
        {
            client.deleteUserFrame(id, index).Wait();
        }


        // Networking

        public String networkInterfaceAddress(String controllerInterface)
        {
            return client.networkInterfaceAddress(id, controllerInterface).Result;
        }

        public int requestNetworkAccess(String controllerInterface, int port, String protocol)

        {
            return client.requestNetworkAccess(id, controllerInterface, port, protocol).Result;
        }

        public void removeNetworkAccess(int accessHandle)
        {
            client.removeNetworkAccess(id, accessHandle).Wait();
        }

        public int requestNetworkService(String controllerInterface, int port, String protocol)

        {
            return client.requestNetworkService(id, controllerInterface, port, protocol).Result;
        }

        public void removeNetworkService(int serviceHandle)
        {
            client.removeNetworkService(id, serviceHandle).Wait();
        }

        // Event consumer functions
        
        public void addEventConsumer(ControllerEventType eventType, Action<ControllerEvent> c)
        {
            HashSet<ControllerEventType> Set = new HashSet<ControllerEventType>();
            Set.Add(eventType);
            if (!eventConsumers.ContainsKey(eventType))
                eventConsumers[eventType] = new List<Action<ControllerEvent>>();
            eventConsumers[eventType].Add(c);        
        
            subscribeEventTypes(Set);
        }
        
        
        public void handleEvent(ControllerEvent e) {
        
            // an event we have a consumer for?
            if (eventConsumers.ContainsKey(e.EventType)) {
                foreach(Action<ControllerEvent> consumer in eventConsumers[e.EventType]) 
                    consumer.Invoke(e);
            }
        
        }
        protected Extension extension;
        protected API.Controller.Client client;
        protected long id;
        protected TMultiplexedProtocol robotProtocol;
        protected Dictionary<ControllerEventType, List<Action<ControllerEvent>>> eventConsumers;

    }
}