
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
        return client.requestPermissions(id, permissions);
    }

    public boolean havePermission(String permission) throws IllegalArgument, TException
    {
        return client.havePermission(id, permission);
    }

    public void relinquishPermissions(java.util.Set<String> permissions) throws TException
    {
        client.relinquishPermissions(id, permissions);
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


    public OperationMode operationMode() throws TException
    {
        return client.operationMode(id);
    }

    public ServoState servoState() throws TException
    {
        return client.servoState(id);
    }

    public PlaybackState playbackState() throws TException
    {
        return client.playbackState(id);
    }

    public void run() throws TException
    {
        client.run(id);
    }

    public void pause() throws TException
    {
        client.pause(id);
    }

    public void resume() throws TException
    {
        client.resume(id);
    }

    public void stop() throws TException
    {
        client.stop(id);
    }


    // Jobs

    public String currentJob() throws TException
    {
        return client.currentJob(id);
    }

    public void setCurrentJob(String name, int line) throws IllegalArgument, TException
    {
        client.setCurrentJob(id, name, line);
    }

    public String defaultJob() throws TException
    {
        return client.defaultJob(id);
    }

    public boolean jobExists(String name) throws TException
    {
        return client.jobExists(id, name);
    }

    public RobotJobInfo jobDetails(String name) throws IllegalArgument, TException
    {
        return client.jobDetails(id, name);
    }

    public java.util.List<String> jobs() throws TException
    {
        return client.jobs(id);
    }

    public void duplicateJob(String existingName, String newName) throws IllegalArgument, TException
    {
        client.duplicateJob(id, existingName, newName);
    }

    public void deleteJob(String name) throws IllegalArgument, TException
    {
        client.deleteJob(id, name);
    }

    public String jobSource(String name) throws IllegalArgument, TException
    {
        return client.jobSource(id, name);
    }

    public void storeJobSource(String name, String programmingLanguage, String sourceCode) throws IllegalArgument, TException
    {
        client.storeJobSource(id, name, programmingLanguage, sourceCode);
    }


    // Tools

    public Map<Integer,String> tools() throws IllegalArgument, TException
    {
        return client.tools(id);
    }

    public Tool tool(int index) throws IllegalArgument, TException
    {
        return client.tool(id, index);
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


    public java.util.List<ControlGroup> controlGroups() throws TException
    {
        return client.controlGroups(id);
    }
    public byte currentControlGroup() throws TException
    {
        return client.currentControlGroup(id);
    }

    public byte robotCount() throws TException
    {
        return client.robotCount(id);
    }

    public int currentRobotIndex() throws TException
    {
        return client.currentRobot(id);
    }

    public Robot currentRobot() throws TException
    {
        return new Robot(this, robotProtocol, currentRobotIndex());
    }


    public Any variable(String name) throws IllegalArgument, TException
    {
        return client.variable(id, name);
    }

    public Any variableByAddr(VariableAddress addr) throws IllegalArgument, TException
    {
        return client.variableByAddr(id, addr);
    }

    public void setVariable(String name, Any value) throws IllegalArgument, TException
    {
        client.setVariable(id, name, value);
    }

    public void setVariable(String name, boolean value) throws IllegalArgument, TException
    {
        client.setVariable(id, name, Any.bValue(value));
    }

    public void setVariable(String name, long value) throws IllegalArgument, TException
    {
        client.setVariable(id, name, Any.iValue(value));
    }

    public void setVariable(String name, double value) throws IllegalArgument, TException
    {
        client.setVariable(id, name, Any.rValue(value));
    }

    public void setVariable(String name, String value) throws IllegalArgument, TException
    {
        client.setVariable(id, name, Any.sValue(value));
    }

    public void setVariable(String name, Position value) throws IllegalArgument, TException
    {
        client.setVariable(id, name, Any.pValue(value));
    }

    public void setVariableByAddr(VariableAddress addr, Any value) throws IllegalArgument, TException
    {
        client.setVariableByAddr(id, addr, value);
    }

    public void setVariableByAddr(VariableAddress addr, boolean value) throws IllegalArgument, TException
    {
        client.setVariableByAddr(id, addr, Any.bValue(value));
    }

    public void setVariableByAddr(VariableAddress addr, long value) throws IllegalArgument, TException
    {
        client.setVariableByAddr(id, addr, Any.iValue(value));
    }

    public void setVariableByAddr(VariableAddress addr, double value) throws IllegalArgument, TException
    {
        client.setVariableByAddr(id, addr, Any.rValue(value));
    }

    public void setVariableByAddr(VariableAddress addr, String value) throws IllegalArgument, TException
    {
        client.setVariableByAddr(id, addr, Any.sValue(value));
    }

    public void setVariableByAddr(VariableAddress addr, Position value) throws IllegalArgument, TException
    {
        client.setVariableByAddr(id, addr, Any.pValue(value));
    }

    public VariableAddress variableAddrByNameAndSpace(String name, AddressSpace space) throws IllegalArgument, TException
    {
        return client.variableAddrByNameAndSpace(id, name, space);
    }

    public VariableAddress variableAddrByName(String name) throws IllegalArgument, TException
    {
        return client.variableAddrByName(id, name);
    }

    public void setVariableName(VariableAddress addr, String name) throws IllegalArgument, TException
    {
        client.setVariableName(id, addr, name);
    }



    public Zone zone(int index) throws IllegalArgument, TException
    {
        return client.zone(id, index);
    }

    public int newZone() throws IllegalArgument, TException
    {
        return client.newZone(id);
    }

    public void modifyZone(int index, Zone z) throws IllegalArgument, TException
    {
        client.modifyZone(id, index, z);
    }

    public void deleteZone(int index) throws IllegalArgument, TException
    {
        client.deleteZone(id, index);
    }


    // User frames

    public Map<Integer,String> userFrames() throws IllegalArgument, TException
    {
        return client.userFrames(id);
    }

    public CoordinateFrame userFrame(int index) throws IllegalArgument, TException
    {
        return client.userFrame(id, index);
    }

    public int newUserFrame() throws IllegalArgument, TException
    {
        return client.newUserFrame(id);
    }

    public void setUserFrame(int index, CoordinateFrame f) throws IllegalArgument, TException
    {
        client.setUserFrame(id, index, f);
    }

    public void deleteUserFrame(int index) throws IllegalArgument, TException
    {
        client.deleteUserFrame(id, index);
    }


    // Networking

    public String networkInterfaceAddress(String controllerInterface) throws IllegalArgument, TException
    {
        return client.networkInterfaceAddress(id, controllerInterface);
    }

    public int requestNetworkAccess(String controllerInterface, int port, String protocol) throws IllegalArgument, TException
    {
        return client.requestNetworkAccess(id, controllerInterface, port, protocol);
    }

    public void removeNetworkAccess(int accessHandle) throws IllegalArgument, TException
    {
        client.removeNetworkAccess(id, accessHandle);
    }


    // Event consumer functions

    public void addEventConsumer(ControllerEventType eventType, Consumer<yaskawa.ext.api.ControllerEvent> c) throws TException
    {
        if (!eventConsumers.containsKey(eventType))
            eventConsumers.put(eventType, new ArrayList<Consumer<yaskawa.ext.api.ControllerEvent>>());
        eventConsumers.get(eventType).add(c);        

        subscribeEventTypes(Set.of( eventType ));
    }
    

    public void handleEvent(ControllerEvent e) {

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

