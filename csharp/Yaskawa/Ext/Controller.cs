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

            lock (extension.SyncRoot)
                client = new API.Controller.Client(protocol);
            this.id = id;
            eventConsumers = new Dictionary<ControllerEventType, List<Action<ControllerEvent>>>();
        }

        public bool requestPermissions(HashSet<string> permissions)
        {
            lock (extension.SyncRoot)
                return client.requestPermissions(id, permissions).Result;
        }

        public bool havePermission(String permission)
        {
            lock (extension.SyncRoot)
                return client.havePermission(id, permission).Result;
        }

        public void relinquishPermissions(HashSet<string> permissions)
        {
            lock (extension.SyncRoot)
                client.relinquishPermissions(id, permissions).Wait();
        }

        public void connect(string hostName)
        {
            lock (extension.SyncRoot)
                client.connect(id, hostName).Wait();
        }

        public void disconnect()
        {
            lock (extension.SyncRoot)
                client.disconnect(id).Wait();
        }

        public void subscribeEventTypes(HashSet<ControllerEventType> types)
        {
            lock (extension.SyncRoot)
                client.subscribeEventTypes(id, types).Wait();
        }

        public void unsubscribeEventTypes(HashSet<ControllerEventType> types)
        {
            lock (extension.SyncRoot)
                client.unsubscribeEventTypes(id, types).Wait();
        }

        public List<ControllerEvent> events()
        {
            lock (extension.SyncRoot)
                return client.events(id).Result;
        }

        public bool connected()
        {
            lock (extension.SyncRoot)
                return client.connected(id).Result;
        }

        public String connectedHostName()
        {
            lock (extension.SyncRoot)
                return client.connectedHostName(id).Result;
        }

        public string softwareVersion()
        {
            lock (extension.SyncRoot)
                return client.softwareVersion(id).Result;
        }

        public bool monitoring()
        {
            lock (extension.SyncRoot)
                return client.monitoring(id).Result;
        }

        public bool haveExclusiveControl()
        {
            lock (extension.SyncRoot)
                return client.haveExclusiveControl(id).Result;
        }

        public OperationMode operationMode()
        {
            lock (extension.SyncRoot)
                return client.operationMode(id).Result;
        }

        public ServoState servoState()
        {
            lock (extension.SyncRoot)
                return client.servoState(id).Result;
        }

        public PlaybackState playbackState()
        {
            lock (extension.SyncRoot)
                return client.playbackState(id).Result;
        }

        public PlaybackCycle playbackCycle()
        {
            lock (extension.SyncRoot)
                return client.playbackCycle(id).Result;
        }

        public void setPlaybackCycle(PlaybackCycle cycle)
        {
            lock (extension.SyncRoot)
                client.setPlaybackCycle(id, cycle).Wait();
        }

        public void run()
        {
            lock (extension.SyncRoot)
                client.run(id).Wait();
        }

        public void pause()
        {
            lock (extension.SyncRoot)
                client.pause(id).Wait();
        }

        public void resume()
        {
            lock (extension.SyncRoot)
                client.resume(id).Wait();
        }

        public void stop()
        {
            lock (extension.SyncRoot)
                client.stop(id).Wait();
        }


        // Jobs

        public String currentJob()
        {
            lock (extension.SyncRoot)
                return client.currentJob(id).Result;
        }

        public void setCurrentJob(String name, int line)
        {
            lock (extension.SyncRoot)
                client.setCurrentJob(id, name, line).Wait();
        }

        public String defaultJob()
        {
            lock (extension.SyncRoot)
                return client.defaultJob(id).Result;
        }

        public bool jobExists(String name)
        {
            lock (extension.SyncRoot)
                return client.jobExists(id, name).Result;
        }

        public RobotJobInfo jobDetails(String name)
        {
            lock (extension.SyncRoot)
                return client.jobDetails(id, name).Result;
        }

        public List<String> jobs()
        {
            lock (extension.SyncRoot)
                return client.jobs(id).Result;
        }

        public List<String> jobsMatching(String nameRegex, String tag)
        {
            lock (extension.SyncRoot)
                return client.jobsMatching(id, nameRegex, tag).Result;
        }

        public void duplicateJob(String existingName, String newName)
        {
            lock (extension.SyncRoot)
                client.duplicateJob(id, existingName, newName).Wait();
        }

        public void deleteJob(String name)
        {
            lock (extension.SyncRoot)
                client.deleteJob(id, name).Wait();
        }

        public String jobSource(String name)
        {
            lock (extension.SyncRoot)
                return client.jobSource(id, name).Result;
        }

        public void storeJobSource(String name, String programmingLanguage, String sourceCode)

        {
            lock (extension.SyncRoot)
                client.storeJobSource(id, name, programmingLanguage, sourceCode).Wait();
        }


        // Tools

        public Dictionary<int, String> tools()
        {
            lock (extension.SyncRoot)
                return client.tools(id).Result;
        }

        public Tool tool(int index)
        {
            lock (extension.SyncRoot)
                return client.tool(id, index).Result;
        }

        // IO

        public int inputNumber(String name)
        {
            lock (extension.SyncRoot)
                return client.inputNumber(id, name).Result;
        }

        public int inputGroupNumber(String name)
        {
            lock (extension.SyncRoot)
                return client.inputGroupNumber(id, name).Result;
        }

        public int outputNumber(String name)
        {
            lock (extension.SyncRoot)
                return client.outputNumber(id, name).Result;
        }

        public int outputGroupNumber(String name)
        {
            lock (extension.SyncRoot)
                return client.outputGroupNumber(id, name).Result;
        }

        public String inputName(int num)
        {
            lock (extension.SyncRoot)
                return client.inputName(id, num).Result;
        }

        public String outputName(int num)
        {
            lock (extension.SyncRoot)
                return client.outputName(id, num).Result;
        }

        public void setInputName(int num, String name)
        {
            lock (extension.SyncRoot)
                client.setInputName(id, num, name).Wait();
        }

        public void setOutputName(int num, String name)
        {
            lock (extension.SyncRoot)
                client.setOutputName(id, num, name).Wait();
        }

        public void monitorInput(int num)
        {
            lock (extension.SyncRoot)
                client.monitorInput(id, num).Wait();
        }

        public void monitorInputGroups(int groupNum, int count)
        {
            lock (extension.SyncRoot)
                client.monitorInputGroups(id, groupNum, count).Wait();
        }

        public void monitorOutput(int num)
        {
            lock (extension.SyncRoot)
                client.monitorOutput(id, num).Wait();
        }

        public void monitorOutputGroups(int groupNum, int count)
        {
            lock (extension.SyncRoot)
                client.monitorOutputGroups(id, groupNum, count).Wait();
        }

        public void unmonitorInput(int num)
        {
            lock (extension.SyncRoot)
                client.unmonitorInput(id, num).Wait();
        }

        public void unmonitorInputGroups(int groupNum, int count)
        {
            lock (extension.SyncRoot)
                client.unmonitorInputGroups(id, groupNum, count).Wait();
        }

        public void unmonitorOutput(int num)
        {
            lock (extension.SyncRoot)
                client.unmonitorOutput(id, num).Wait();
        }

        public void unmonitorOutputGroups(int groupNum, int count)
        {
            lock (extension.SyncRoot)
                client.unmonitorOutputGroups(id, groupNum, count).Wait();
        }

        public bool inputValue(int num)
        {
            lock (extension.SyncRoot)
                return client.inputValue(id, num).Result;
        }

        public int inputGroupsValue(int groupNum, int count)
        {
            lock (extension.SyncRoot)
                return client.inputGroupsValue(id, groupNum, count).Result;
        }

        public bool outputValue(int num)
        {
            lock (extension.SyncRoot)
                return client.outputValue(id, num).Result;
        }

        public int outputGroupsValue(int groupNum, int count)
        {
            lock (extension.SyncRoot)
                return client.outputGroupsValue(id, groupNum, count).Result;
        }

        public void setOutput(int num, bool value)
        {
            lock (extension.SyncRoot)
                client.setOutput(id, num, value).Wait();
        }

        public void setOutputGroups(int groupNum, int count, int value)
        {
            lock (extension.SyncRoot)
                client.setOutputGroups(id, groupNum, count, value).Wait();
        }

        public int inputAddress(String name)
        {
            lock (extension.SyncRoot)
                return client.inputAddress(id, name).Result;
        }

        public int inputAddressByNumber(int num)
        {
            lock (extension.SyncRoot)
                return client.inputAddressByNumber(id, num).Result;
        }

        public int outputAddress(String name)
        {
            lock (extension.SyncRoot)
                return client.outputAddress(id, name).Result;
        }

        public int outputAddressByNumber(int num)
        {
            lock (extension.SyncRoot)
                return client.outputAddressByNumber(id, num).Result;
        }

        public void monitorIOAddress(int address)
        {
            lock (extension.SyncRoot)
                client.monitorIOAddress(id, address).Wait();
        }

        public void unmonitorIOAddress(int address)
        {
            lock (extension.SyncRoot)
                client.unmonitorIOAddress(id, address).Wait();
        }

        public bool inputAddressValue(int address)
        {
            lock (extension.SyncRoot)
                return client.inputAddressValue(id, address).Result;
        }

        public bool outputAddressValue(int address)
        {
            lock (extension.SyncRoot)
                return client.outputAddressValue(id, address).Result;
        }

        public bool ioAddressValue(int address)
        {
            lock (extension.SyncRoot)
                return client.ioAddressValue(id, address).Result;
        }

        public void setOutputAddress(int address, bool value)
        {
            lock (extension.SyncRoot)
                client.setOutputAddress(id, address, value).Wait();
        }

        public int fieldBusStatusInputGroup(String busType)
        {
            lock (extension.SyncRoot)
                return client.fieldBusStatusInputGroup(id, busType).Result;
        }

        public List<ControlGroup> controlGroups()
        {
            lock (extension.SyncRoot)
                return client.controlGroups(id).Result;
        }

        public sbyte currentControlGroup()
        {
            lock (extension.SyncRoot)
                return client.currentControlGroup(id).Result;
        }

        public sbyte robotCount()
        {
            lock (extension.SyncRoot)
                return client.robotCount(id).Result;
        }

        public int currentRobotIndex()
        {
            lock (extension.SyncRoot)
                return client.currentRobot(id).Result;
        }

        //Dictionary<sbyte, int> robots(long c);
        public Robot currentRobot()
        {
            lock (extension.SyncRoot)
                return new Robot(this, extension, robotProtocol, currentRobotIndex());
            //return client.currentRobot(id);
        }
        public Any variable(String name)
        {
            lock (extension.SyncRoot)
                return client.variable(id, name).Result;
        }

        public Any variableByAddr(VariableAddress addr)
        {
            lock (extension.SyncRoot)
                return client.variableByAddr(id, addr).Result;
        }

        public void monitorVariable(VariableAddress addr)
        {
            lock (extension.SyncRoot)
                client.monitorVariable(id, addr).Wait();
        }

        public void unmonitorVariable(VariableAddress addr)
        {
            lock (extension.SyncRoot)
                client.unmonitorVariable(id, addr).Wait();
        }

public void setVariable(String name, Any value) 
        {
            lock (extension.SyncRoot)
                client.setVariable(id, name, value).Wait();
        }

        public void setVariable(String name, bool value)
        {
            Any a = new Any();
            a.BValue = value;
            lock (extension.SyncRoot)
                client.setVariable(id, name, a).Wait();
        }

        public void setVariable(String name, long value)
        {
            Any a = new Any();
            a.IValue = value;
            lock (extension.SyncRoot)
                client.setVariable(id, name, a).Wait();
        }

        public void setVariable(String name, double value)
        {
            Any a = new Any();
            a.RValue = value;
            lock (extension.SyncRoot)
                client.setVariable(id, name, a).Wait();
        }

        public void setVariable(String name, String value)
        {
            Any a = new Any();
            a.SValue = value;
            lock (extension.SyncRoot)
                client.setVariable(id, name, a).Wait();
        }
        public void setVariable(String name, Position value)
        {
            Any a = new Any();
            a.PValue = value;
            lock (extension.SyncRoot)
                client.setVariable(id, name, a).Wait();
        }

        public void setVariableByAddr(VariableAddress addr, Any value)
        {
            lock (extension.SyncRoot)
                client.setVariableByAddr(id, addr, value).Wait();
        }

        public void setVariableByAddr(VariableAddress addr, bool value)
        {
            Any a = new Any();
            a.BValue = value;
            lock (extension.SyncRoot)
                client.setVariableByAddr(id, addr, a).Wait();
        }

        public void setVariableByAddr(VariableAddress addr, long value)
        {
            Any a = new Any();
            a.IValue = value;
            lock (extension.SyncRoot)
                client.setVariableByAddr(id, addr, a).Wait();
        }

        public void setVariableByAddr(VariableAddress addr, double value)
        {
            Any a = new Any();
            a.RValue = value;
            lock (extension.SyncRoot)
                client.setVariableByAddr(id, addr, a).Wait();
        }

        public void setVariableByAddr(VariableAddress addr, String value)
        {
            Any a = new Any();
            a.SValue = value;
            lock (extension.SyncRoot)
                client.setVariableByAddr(id, addr, a).Wait();
        }

        public void setVariableByAddr(VariableAddress addr, Position value)
        {
            Any a = new Any();
            a.PValue = value;
            lock (extension.SyncRoot)
                client.setVariableByAddr(id, addr, a).Wait();
        }

        public VariableAddress variableAddrByNameAndSpace(String name, AddressSpace space)
        {
            lock (extension.SyncRoot)
                return client.variableAddrByNameAndSpace(id, name, space).Result;
        }

        public VariableAddress variableAddrByName(String name)
        {
            lock (extension.SyncRoot)
                return client.variableAddrByName(id, name).Result;
        }

        public void setVariableName(VariableAddress addr, String name)
        {
            lock (extension.SyncRoot)
                client.setVariableName(id, addr, name).Wait();
        }

        public Zone zone(int index)
        {
            lock (extension.SyncRoot)
                return client.zone(id, index).Result;
        }

        public int newZone()
        {
            lock (extension.SyncRoot)
                return client.newZone(id).Result;
        }

        public void modifyZone(int index, Zone z)
        {
            lock (extension.SyncRoot)
                client.modifyZone(id, index, z).Wait();
        }

        public void deleteZone(int index)
        {
            lock (extension.SyncRoot)
                client.deleteZone(id, index).Wait();
        }


        // User frames

        public Dictionary<int, String> userFrames()
        {
            lock (extension.SyncRoot)
                return client.userFrames(id).Result;
        }

        public CoordinateFrame userFrame(int index)
        {
            lock (extension.SyncRoot)
                return client.userFrame(id, index).Result;
        }

        public int newUserFrame()
        {
            lock (extension.SyncRoot)
                return client.newUserFrame(id).Result;
        }

        public void setUserFrame(int index, CoordinateFrame f)
        {
            lock (extension.SyncRoot)
                client.setUserFrame(id, index, f).Wait();
        }

        public void deleteUserFrame(int index)
        {
            lock (extension.SyncRoot)
                client.deleteUserFrame(id, index).Wait();
        }


        // Networking

        public String networkInterfaceAddress(String controllerInterface)
        {
            lock (extension.SyncRoot)
                return client.networkInterfaceAddress(id, controllerInterface).Result;
        }

        public int requestNetworkAccess(String controllerInterface, int port, String protocol)

        {
            lock (extension.SyncRoot)
                return client.requestNetworkAccess(id, controllerInterface, port, protocol).Result;
        }

        public void removeNetworkAccess(int accessHandle)
        {
            lock (extension.SyncRoot)
                client.removeNetworkAccess(id, accessHandle).Wait();
        }

        public int requestNetworkService(String controllerInterface, int port, String protocol)

        {
            lock (extension.SyncRoot)
                return client.requestNetworkService(id, controllerInterface, port, protocol).Result;
        }

        public void removeNetworkService(int serviceHandle)
        {
            lock (extension.SyncRoot)
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