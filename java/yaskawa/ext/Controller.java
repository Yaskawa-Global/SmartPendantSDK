
package yaskawa.ext;

import java.util.*;
import java.util.function.*;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TMultiplexedProtocol;

import yaskawa.ext.api.*;


public class Controller
{
    Controller(Extension ext, TProtocol protocol, TMultiplexedProtocol robotProtocol, long id) throws TTransportException
    {
        extension = ext;
        client = new yaskawa.ext.api.Controller.Client(protocol);
        this.robotProtocol = robotProtocol;
        this.id = id;
        eventConsumers = new HashMap<ControllerEventType, ArrayList<Consumer<yaskawa.ext.api.ControllerEvent>>>();
    }


    public boolean requestPermissions(java.util.Set<String> permissions) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.requestPermissions(id, permissions);
        }
    }

    public boolean havePermission(String permission) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.havePermission(id, permission);
        }
    }

    public void relinquishPermissions(java.util.Set<String> permissions) throws TException
    {
        synchronized(extension) {
            client.relinquishPermissions(id, permissions);
        }
    }

    public void connect(String hostName) throws TException
    {
        synchronized(extension) {
            client.connect(id, hostName);
        }
    }

    public void disconnect() throws TException
    {
        synchronized(extension) {
            client.disconnect(id);
        }
    }

    public void subscribeEventTypes(Set<ControllerEventType> types) throws TException
    {
        synchronized(extension) {
            client.subscribeEventTypes(id, types);
        }
    }

    public void unsubscribeEventTypes(Set<ControllerEventType> types) throws TException
    {
        synchronized(extension) {
            client.unsubscribeEventTypes(id, types);
        }
    }

    public List<yaskawa.ext.api.ControllerEvent> events() throws TException
    {
        synchronized(extension) {
            return client.events(id);
        }
    }

    public boolean connected() throws TException
    {
        synchronized(extension) {
            return client.connected(id);
        }
    }

    public String connectedHostName() throws TException
    {
        synchronized(extension) {
            return client.connectedHostName(id);
        }
    }

    public String softwareVersion() throws TException
    {
        synchronized(extension) {
            return client.softwareVersion(id);
        }
    }

    public boolean monitoring() throws TException
    {
        synchronized(extension) {
            return client.monitoring(id);
        }
    }

    public boolean haveExclusiveControl() throws TException
    {
        synchronized(extension) {
            return client.haveExclusiveControl(id);
        }
    }


    public OperationMode operationMode() throws TException
    {
        synchronized(extension) {
            return client.operationMode(id);
        }
    }

    public ServoState servoState() throws TException
    {
        synchronized(extension) {
            return client.servoState(id);
        }
    }

    public PlaybackState playbackState() throws TException
    {
        synchronized(extension) {
            return client.playbackState(id);
        }
    }

    public PlaybackCycle playbackCycle() throws TException
    {
        synchronized(extension) {
            return client.playbackCycle(id);
        }
    }
    
    public void setPlaybackCycle(PlaybackCycle cycle) throws TException
    {
        synchronized(extension) {
            client.setPlaybackCycle(id, cycle);
        }
    }

    public void run() throws TException
    {
        synchronized(extension) {
            client.run(id);
        }
    }

    public void pause() throws TException
    {
        synchronized(extension) {
            client.pause(id);
        }
    }

    public void resume() throws TException
    {
        synchronized(extension) {
            client.resume(id);
        }
    }

    public void stop() throws TException
    {
        synchronized(extension) {
            client.stop(id);
        }
    }


    // Jobs

    public String currentJob() throws TException
    {
        synchronized(extension) {
            return client.currentJob(id);
        }
    }

    public void setCurrentJob(String name, int line) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.setCurrentJob(id, name, line);
        }
    }

    public String defaultJob() throws TException
    {
        synchronized(extension) {
            return client.defaultJob(id);
        }
    }

    public boolean jobExists(String name) throws TException
    {
        synchronized(extension) {
            return client.jobExists(id, name);
        }
    }

    public RobotJobInfo jobDetails(String name) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.jobDetails(id, name);
        }
    }

    public java.util.List<String> jobs() throws TException
    {
        synchronized(extension) {
            return client.jobs(id);
        }
    }

    public java.util.List<String> jobsMatching(String nameRegex, String tag) throws TException
    {
        synchronized(extension) {
            return client.jobsMatching(id, nameRegex, tag);
        }
    }

    public void duplicateJob(String existingName, String newName) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.duplicateJob(id, existingName, newName);
        }
    }

    public void deleteJob(String name) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.deleteJob(id, name);
        }
    }

    public String jobSource(String name) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.jobSource(id, name);
        }
    }

    public void storeJobSource(String name, String programmingLanguage, String sourceCode) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.storeJobSource(id, name, programmingLanguage, sourceCode);
        }
    }


    // Tools

    public Map<Integer,String> tools() throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.tools(id);
        }
    }

    public Tool tool(int index) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.tool(id, index);
        }
    }



    // IO

    public int inputNumber(String name) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.inputNumber(id, name);
        }
    }

    public int inputGroupNumber(String name) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.inputGroupNumber(id, name);
        }
    }

    public int outputNumber(String name) throws IllegalArgument, TException    
    {
        synchronized(extension) {
            return client.outputNumber(id, name);
        }
    }
    
    public int outputGroupNumber(String name) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.outputGroupNumber(id, name);
        }
    }
    
    public String inputName(int num) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.inputName(id, num);
        }
    }

    public String outputName(int num) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.outputName(id, num);
        }
    }

    public void setInputName(int num, String name) throws TException
    {
        synchronized(extension) {
            client.setInputName(id, num, name);
        }
    }

    public void setOutputName(int num, String name) throws TException
    {
        synchronized(extension) {
            client.setOutputName(id, num, name);
        }
    }

    public void monitorInput(int num) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.monitorInput(id, num);
        }
    }

    public void monitorInputGroups(int groupNum, int count) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.monitorInputGroups(id, groupNum, count);
        }
    }

    public void monitorOutput(int num) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.monitorOutput(id, num);
        }
    }

    public void monitorOutputGroups(int groupNum, int count) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.monitorOutputGroups(id, groupNum, count);
        }
    }

    public void unmonitorInput(int num) throws TException
    {
        synchronized(extension) {
            client.unmonitorInput(id, num);
        }
    }

    public void unmonitorInputGroups(int groupNum, int count) throws TException
    {
        synchronized(extension) {
            client.unmonitorInputGroups(id, groupNum, count);
        }
    }

    public void unmonitorOutput(int num) throws TException
    {
        synchronized(extension) {
            client.unmonitorOutput(id, num);
        }
    }

    public void unmonitorOutputGroups(int groupNum, int count) throws TException
    {
        synchronized(extension) {
            client.unmonitorOutputGroups(id, groupNum, count);
        }
    }

    public boolean inputValue(int num) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.inputValue(id, num);
        }
    }

    public int inputGroupsValue(int groupNum, int count) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.inputGroupsValue(id, groupNum, count);
        }
    }

    public boolean outputValue(int num) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.outputValue(id, num);
        }
    }

    public int outputGroupsValue(int groupNum, int count) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.outputGroupsValue(id, groupNum, count);
        }
    }

    public void setOutput(int num, boolean value) throws TException
    {
        synchronized(extension) {
            client.setOutput(id, num, value);
        }
    }

    public void setOutputGroups(int groupNum, int count, int value) throws TException
    {
        synchronized(extension) {
            client.setOutputGroups(id, groupNum, count, value);
        }
    }

    public int inputAddress(String name) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.inputAddress(id, name);
        }
    }

    public int inputAddressByNumber(int num) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.inputAddressByNumber(id, num);
        }
    }

    public int outputAddress(String name) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.outputAddress(id, name);
        }
    }

    public int outputAddressByNumber(int num) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.outputAddressByNumber(id, num);
        }
    }

    public void monitorIOAddress(int address) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.monitorIOAddress(id, address);
        }
    }

    public void unmonitorIOAddress(int address) throws TException
    {
        synchronized(extension) {
            client.unmonitorIOAddress(id, address);
        }
    }

    public boolean inputAddressValue(int address) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.inputAddressValue(id, address);
        }
    }

    public boolean outputAddressValue(int address) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.outputAddressValue(id, address);
        }
    }
    
    public boolean ioAddressValue(int address) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.ioAddressValue(id, address);
        }
    }
    
    public void setOutputAddress(int address, boolean value) throws TException
    {
        synchronized(extension) {
            client.setOutputAddress(id, address, value);
        }
    }

    public int mRegisterValue(int index) throws TException
    {
        synchronized(extension){
            return client.mRegisterValue(id, index);
        }
    }

    public void setMRegisterIndex(int index, short value) throws TException
    {
        synchronized(extension){
            client.setMRegisterIndex(id, index, value);
        }
    }
 
    public int fieldBusStatusInputGroup(String busType) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.fieldBusStatusInputGroup(id, busType);
        }
    }

    public java.util.List<ControlGroup> controlGroups() throws TException
    {
        synchronized(extension) {
            return client.controlGroups(id);
        }
    }
     
    public byte currentControlGroup() throws TException
    {
        synchronized(extension) {
            return client.currentControlGroup(id);
        }
    }

    public byte robotCount() throws TException
    {
        synchronized(extension) {
            return client.robotCount(id);
        }
    }

    public int currentRobotIndex() throws TException
    {
        synchronized(extension) {
            return client.currentRobot(id);
        }
    }

    public Robot currentRobot() throws TException
    {
        return new Robot(this, robotProtocol, currentRobotIndex());
    }


    public Any variable(String name) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.variable(id, name);
        }
    }

    public Any variableByAddr(VariableAddress addr) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.variableByAddr(id, addr);
        }
    }
    
    public void monitorVariable(VariableAddress addr) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.monitorVariable(id, addr);
        }
    }
    
    public void unmonitorVariable(VariableAddress addr) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.unmonitorVariable(id, addr);
        }
    }

    public void setVariable(String name, Any value) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.setVariable(id, name, value);
        }
    }

    public void setVariable(String name, boolean value) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.setVariable(id, name, Any.bValue(value));
        }
    }

    public void setVariable(String name, long value) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.setVariable(id, name, Any.iValue(value));
        }
    }

    public void setVariable(String name, double value) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.setVariable(id, name, Any.rValue(value));
        }
    }

    public void setVariable(String name, String value) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.setVariable(id, name, Any.sValue(value));
        }
    }

    public void setVariable(String name, Position value) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.setVariable(id, name, Any.pValue(value));
        }
    }

    public void setVariableByAddr(VariableAddress addr, Any value) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.setVariableByAddr(id, addr, value);
        }
    }

    public void setVariableByAddr(VariableAddress addr, boolean value) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.setVariableByAddr(id, addr, Any.bValue(value));
        }
    }

    public void setVariableByAddr(VariableAddress addr, long value) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.setVariableByAddr(id, addr, Any.iValue(value));
        }
    }

    public void setVariableByAddr(VariableAddress addr, double value) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.setVariableByAddr(id, addr, Any.rValue(value));
        }
    }

    public void setVariableByAddr(VariableAddress addr, String value) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.setVariableByAddr(id, addr, Any.sValue(value));
        }
    }

    public void setVariableByAddr(VariableAddress addr, Position value) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.setVariableByAddr(id, addr, Any.pValue(value));
        }
    }

    public VariableAddress variableAddrByNameAndSpace(String name, AddressSpace space) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.variableAddrByNameAndSpace(id, name, space);
        }
    }

    public VariableAddress variableAddrByName(String name) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.variableAddrByName(id, name);
        }
    }

    public void setVariableName(VariableAddress addr, String name) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.setVariableName(id, addr, name);
        }
    }



    public Zone zone(int index) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.zone(id, index);
        }
    }

    public int newZone() throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.newZone(id);
        }
    }

    public void modifyZone(int index, Zone z) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.modifyZone(id, index, z);
        }
    }

    public void deleteZone(int index) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.deleteZone(id, index);
        }
    }


    // User frames

    public Map<Integer,String> userFrames() throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.userFrames(id);
        }
    }

    public CoordinateFrame userFrame(int index) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.userFrame(id, index);
        }
    }

    public int newUserFrame() throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.newUserFrame(id);
        }
    }

    public void setUserFrame(int index, CoordinateFrame f) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.setUserFrame(id, index, f);
        }
    }

    public void deleteUserFrame(int index) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.deleteUserFrame(id, index);
        }
    }


    // Networking

    public String networkInterfaceAddress(String controllerInterface) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.networkInterfaceAddress(id, controllerInterface);
        }
    }

    public int requestNetworkAccess(String controllerInterface, int port, String protocol) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.requestNetworkAccess(id, controllerInterface, port, protocol);
        }
    }

    public void removeNetworkAccess(int accessHandle) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.removeNetworkAccess(id, accessHandle);
        }
    }

    public int requestNetworkService(String controllerInterface, int port, String protocol) throws IllegalArgument, TException
    {
        synchronized(extension) {
            return client.requestNetworkService(id, controllerInterface, port, protocol);
        }
    }

    public void removeNetworkService(int serviceHandle) throws IllegalArgument, TException
    {
        synchronized(extension) {
            client.removeNetworkService(id, serviceHandle);
        }
    }




    // Event consumer functions

    public synchronized void addEventConsumer(ControllerEventType eventType, Consumer<yaskawa.ext.api.ControllerEvent> c) throws TException
    {
        if (!eventConsumers.containsKey(eventType))
            eventConsumers.put(eventType, new ArrayList<Consumer<yaskawa.ext.api.ControllerEvent>>());
        eventConsumers.get(eventType).add(c);        

        subscribeEventTypes(Set.of( eventType ));
    }
    

    public synchronized void handleEvent(ControllerEvent e) {

        // an event we have a consumer for?
        if (eventConsumers.containsKey(e.getEventType())) {
            for(Consumer<yaskawa.ext.api.ControllerEvent> consumer : eventConsumers.get(e.getEventType())) 
                consumer.accept(e);
        }

    }


    protected Extension extension;
    protected yaskawa.ext.api.Controller.Client client;
    protected long id;
    protected TMultiplexedProtocol robotProtocol;

    protected HashMap<ControllerEventType, ArrayList<Consumer<yaskawa.ext.api.ControllerEvent>>> eventConsumers;

}

