using System;
using System.Collections.Generic;
using Thrift.Protocol;
using Thrift.Collections;
using Yaskawa.Ext.API;


namespace Yaskawa.Ext
{
    public class Controller
    {
        internal Controller(Extension ext, TProtocol protocol, TMultiplexedProtocol _robotProtocol, long id)
        {
            extension = ext;
            robotProtocol = _robotProtocol;
            client = new API.Controller.Client(protocol);
            this.id = id;
            eventConsumers = new Dictionary<ControllerEventType, List<Action<ControllerEvent>>>();
        }

        public bool requestPermissions(THashSet<string> permissions)
        {
            return client.requestPermissions(id, permissions);
        }

        public bool havePermission(String permission)
        {
            return client.havePermission(id, permission);
        }

        public void relinquishPermissions(THashSet<string> permissions)
        {
            client.relinquishPermissions(id, permissions);
        }

        public void connect(string hostName)
        {
            client.connect(id, hostName);
        }

        public void disconnect()
        {
            client.disconnect(id);
        }

        public void subscribeEventTypes(THashSet<ControllerEventType> types)
        {
            client.subscribeEventTypes(id, types);
        }

        public void unsubscribeEventTypes(THashSet<ControllerEventType> types)
        {
            client.unsubscribeEventTypes(id, types);
        }

        public List<ControllerEvent> events()
        {
            return client.events(id);
        }

        public bool connected()
        {
            return client.connected(id);
        }

        public String connectedHostName()
        {
            return client.connectedHostName(id);
        }

        public string softwareVersion()
        {
            return client.softwareVersion(id);
        }

        public bool monitoring()
        {
            return client.monitoring(id);
        }

        public bool haveExclusiveControl()
        {
            return client.haveExclusiveControl(id);
        }

        public OperationMode operationMode()
        {
            return client.operationMode(id);
        }

        public ServoState servoState()
        {
            return client.servoState(id);
        }

        public PlaybackState playbackState()
        {
            return client.playbackState(id);
        }

        public void run()
        {
            client.run(id);
        }

        public void pause()
        {
            client.pause(id);
        }

        public void resume()
        {
            client.resume(id);
        }

        public void stop()
        {
            client.stop(id);
        }


        // Jobs

        public String currentJob()
        {
            return client.currentJob(id);
        }

        public void setCurrentJob(String name, int line)
        {
            client.setCurrentJob(id, name, line);
        }

        public String defaultJob()
        {
            return client.defaultJob(id);
        }

        public bool jobExists(String name)
        {
            return client.jobExists(id, name);
        }

        public RobotJobInfo jobDetails(String name)
        {
            return client.jobDetails(id, name);
        }

        public List<String> jobs()
        {
            return client.jobs(id);
        }

        public List<String> jobsMatching(String nameRegex, String tag)
        {
            return client.jobsMatching(id, nameRegex, tag);
        }

        public void duplicateJob(String existingName, String newName)
        {
            client.duplicateJob(id, existingName, newName);
        }

        public void deleteJob(String name)
        {
            client.deleteJob(id, name);
        }

        public String jobSource(String name)
        {
            return client.jobSource(id, name);
        }

        public void storeJobSource(String name, String programmingLanguage, String sourceCode)

        {
            client.storeJobSource(id, name, programmingLanguage, sourceCode);
        }


        // Tools

        public Dictionary<int, String> tools()
        {
            return client.tools(id);
        }

        public Tool tool(int index)
        {
            return client.tool(id, index);
        }

        // IO

        public int inputNumber(String name)
        {
            return client.inputNumber(id, name);
        }

        public int inputGroupNumber(String name)
        {
            return client.inputGroupNumber(id, name);
        }

        public int outputNumber(String name)
        {
            return client.outputNumber(id, name);
        }

        public int outputGroupNumber(String name)
        {
            return client.outputGroupNumber(id, name);
        }

        public String inputName(int num)
        {
            return client.inputName(id, num);
        }

        public String outputName(int num)
        {
            return client.outputName(id, num);
        }

        public void setInputName(int num, String name)
        {
            client.setInputName(id, num, name);
        }

        public void setOutputName(int num, String name)
        {
            client.setOutputName(id, num, name);
        }

        public void monitorInput(int num)
        {
            client.monitorInput(id, num);
        }

        public void monitorInputGroups(int groupNum, int count)
        {
            client.monitorInputGroups(id, groupNum, count);
        }

        public void monitorOutput(int num)
        {
            client.monitorOutput(id, num);
        }

        public void monitorOutputGroups(int groupNum, int count)
        {
            client.monitorOutputGroups(id, groupNum, count);
        }

        public void unmonitorInput(int num)
        {
            client.unmonitorInput(id, num);
        }

        public void unmonitorInputGroups(int groupNum, int count)
        {
            client.unmonitorInputGroups(id, groupNum, count);
        }

        public void unmonitorOutput(int num)
        {
            client.unmonitorOutput(id, num);
        }

        public void unmonitorOutputGroups(int groupNum, int count)
        {
            client.unmonitorOutputGroups(id, groupNum, count);
        }

        public bool inputValue(int num)
        {
            return client.inputValue(id, num);
        }

        public int inputGroupsValue(int groupNum, int count)
        {
            return client.inputGroupsValue(id, groupNum, count);
        }

        public bool outputValue(int num)
        {
            return client.outputValue(id, num);
        }

        public int outputGroupsValue(int groupNum, int count)
        {
            return client.outputGroupsValue(id, groupNum, count);
        }

        public void setOutput(int num, bool value)
        {
            client.setOutput(id, num, value);
        }

        public void setOutputGroups(int groupNum, int count, int value)
        {
            client.setOutputGroups(id, groupNum, count, value);
        }

        public int inputAddress(String name)
        {
            return client.inputAddress(id, name);
        }

        public int inputAddressByNumber(int num)
        {
            return client.inputAddressByNumber(id, num);
        }

        public int outputAddress(String name)
        {
            return client.outputAddress(id, name);
        }

        public int outputAddressByNumber(int num)
        {
            return client.outputAddressByNumber(id, num);
        }

        public void monitorIOAddress(int address)
        {
            client.monitorIOAddress(id, address);
        }

        public void unmonitorIOAddress(int address)
        {
            client.unmonitorIOAddress(id, address);
        }

        public bool inputAddressValue(int address)
        {
            return client.inputAddressValue(id, address);
        }

        public bool outputAddressValue(int address)
        {
            return client.outputAddressValue(id, address);
        }

        public void setOutputAddress(int address, bool value)
        {
            client.setOutputAddress(id, address, value);
        }
        public void setNetworkInputAddress(int address, bool value)
        {
            client.setNetworkInputAddress(id, address, value);
        }

        public int mRegisterValue(int index){
            return client.mRegisterValue(id,index);
        }

        public void setMRegisterIndex(int index, short value){
            client.setMRegisterIndex(id,index, value);
        }

        public int fieldBusStatusInputGroup(String busType)
        {
            return client.fieldBusStatusInputGroup(id, busType);
        }

        public List<ControlGroup> controlGroups()
        {
            return client.controlGroups(id);
        }

        public sbyte currentControlGroup()
        {
            return client.currentControlGroup(id);
        }

        public sbyte robotCount()
        {
            return client.robotCount(id);
        }

        public int currentRobotIndex()
        {
            return client.currentRobot(id);
        }

        //Dictionary<sbyte, int> robots(long c);
        public Robot currentRobot()
        {
            return new Robot(this, robotProtocol, currentRobotIndex());
            //return client.currentRobot(id);
        }
        public Any variable(String name)
        {
            return client.variable(id, name);
        }

        public Any variableByAddr(VariableAddress addr)
        {
            return client.variableByAddr(id, addr);
        }

        public void setVariable(String name, Any value) 
        {
            client.setVariable(id, name, value);
        }

        public void setVariable(String name, bool value)
        {
            Any a = new Any();
            a.BValue = value;
            client.setVariable(id, name, a);
        }

        public void setVariable(String name, long value)
        {
            Any a = new Any();
            a.IValue = value;
            client.setVariable(id, name, a);
        }

        public void setVariable(String name, double value)
        {
            Any a = new Any();
            a.RValue = value;
            client.setVariable(id, name, a);
        }

        public void setVariable(String name, String value)
        {
            Any a = new Any();
            a.SValue = value;
            client.setVariable(id, name, a);
        }
        public void setVariable(String name, Position value)
        {
            Any a = new Any();
            a.PValue = value;
            client.setVariable(id, name, a);
        }

        public void setVariableByAddr(VariableAddress addr, Any value)
        {
            client.setVariableByAddr(id, addr, value);
        }

        public void setVariableByAddr(VariableAddress addr, bool value)
        {
            Any a = new Any();
            a.BValue = value;
            client.setVariableByAddr(id, addr, a);
        }

        public void setVariableByAddr(VariableAddress addr, long value)
        {
            Any a = new Any();
            a.IValue = value;
            client.setVariableByAddr(id, addr, a);
        }

        public void setVariableByAddr(VariableAddress addr, double value)
        {
            Any a = new Any();
            a.RValue = value;
            client.setVariableByAddr(id, addr, a);
        }

        public void setVariableByAddr(VariableAddress addr, String value)
        {
            Any a = new Any();
            a.SValue = value;
            client.setVariableByAddr(id, addr, a);
        }

        public void setVariableByAddr(VariableAddress addr, Position value)
        {
            Any a = new Any();
            a.PValue = value;
            client.setVariableByAddr(id, addr, a);
        }

        public VariableAddress variableAddrByNameAndSpace(String name, AddressSpace space)
        {
            return client.variableAddrByNameAndSpace(id, name, space);
        }

        public VariableAddress variableAddrByName(String name)
        {
            return client.variableAddrByName(id, name);
        }

        public void setVariableName(VariableAddress addr, String name)
        {
            client.setVariableName(id, addr, name);
        }

        public Zone zone(int index)
        {
            return client.zone(id, index);
        }

        public int newZone()
        {
            return client.newZone(id);
        }

        public void modifyZone(int index, Zone z)
        {
            client.modifyZone(id, index, z);
        }

        public void deleteZone(int index)
        {
            client.deleteZone(id, index);
        }


        // User frames

        public Dictionary<int, String> userFrames()
        {
            return client.userFrames(id);
        }

        public CoordinateFrame userFrame(int index)
        {
            return client.userFrame(id, index);
        }

        public int newUserFrame()
        {
            return client.newUserFrame(id);
        }

        public void setUserFrame(int index, CoordinateFrame f)
        {
            client.setUserFrame(id, index, f);
        }

        public void deleteUserFrame(int index)
        {
            client.deleteUserFrame(id, index);
        }


        // Networking

        public String networkInterfaceAddress(String controllerInterface)
        {
            return client.networkInterfaceAddress(id, controllerInterface);
        }

        public int requestNetworkAccess(String controllerInterface, int port, String protocol)

        {
            return client.requestNetworkAccess(id, controllerInterface, port, protocol);
        }

        public void removeNetworkAccess(int accessHandle)
        {
            client.removeNetworkAccess(id, accessHandle);
        }

        public int requestNetworkService(String controllerInterface, int port, String protocol)

        {
            return client.requestNetworkService(id, controllerInterface, port, protocol);
        }

        public void removeNetworkService(int serviceHandle)
        {
            client.removeNetworkService(id, serviceHandle);
        }

        // Event consumer functions
        
        public void addEventConsumer(ControllerEventType eventType, Action<ControllerEvent> c)
        {
            THashSet<ControllerEventType> Set = new THashSet<ControllerEventType>();
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