import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.ByteBuffer;

import yaskawa.ext.api.ControllerEvent;
import yaskawa.ext.api.ControllerEventType;
import yaskawa.ext.api.CoordFrameRepresentation;
import yaskawa.ext.api.CoordinateFrame;
import yaskawa.ext.api.IntegrationPoint;
import yaskawa.ext.api.PendantEvent;
import yaskawa.ext.api.PendantEventType;
import yaskawa.ext.api.PredefinedCoordFrameType;
import yaskawa.ext.api.UtilityWindowWidth;
import yaskawa.ext.api.UtilityWindowHeight;
import yaskawa.ext.api.UtilityWindowExpansion;
import yaskawa.ext.api.OrientationUnit;
import yaskawa.ext.api.VariableAddress;
import yaskawa.ext.api.Scope;
import yaskawa.ext.api.AddressSpace;


import yaskawa.ext.*;

import java.util.*;


public class TestExtension {

    public TestExtension() throws TTransportException, Exception
    {
        var version = new Version(1,0,0);
        var languages = Set.of("en", "ja");

        extension = new Extension("mylaunchkey",
                                "yii.test-extension", 
                                version, "YII", languages,
                                "localhost", -1);

        extension.copyLoggingToStdOutput = true;
        extension.info("Starting");

        System.out.println("API version: "+extension.apiVersion());
        pendant = extension.pendant();
        controller = extension.controller();
        System.out.println("Controller software version:"+controller.softwareVersion());
    }

    protected Extension extension;
    protected Pendant pendant;
    protected Controller controller;

    public void run() throws TException, IOException
    {
        pendant.notice("Running", "TestExtension is now running.");

        System.out.println("Current locale:"+pendant.currentLocale());

        System.out.println("Screen Name:"+pendant.currentScreenName());
        System.out.println("Current Job:"+controller.currentJob());
        System.out.println("Default Job:"+controller.defaultJob());
        VariableAddress addr = new VariableAddress(Scope.Global, AddressSpace.Position, 1);
        controller.setVariableName(addr, "My Test Name");
        System.out.println("Variable My Test Name:"+controller.variable("My Test Name"));
        System.out.println("BVar 28 address:"+controller.variableAddrByNameAndSpace("BVar 28", AddressSpace.Byte));

        addr = new VariableAddress(Scope.Global, AddressSpace.Real, 5);
        controller.setVariableName(addr, "RVar 5");
        controller.setVariable("RVar 5", 123.456);

        //System.out.println("Zone index 0:"+controller.zone(0));
        System.out.println("UserFrame index 0:"+controller.userFrame(0));
        System.out.println("UserFrame index 1:"+controller.userFrame(1));
        System.out.println("UserFrame index 4:"+controller.userFrame(4));

        var robot = controller.currentRobot();
        System.out.println("Robot:"+robot.model());
        System.out.println("  Joint Joints: "+robot.jointPosition(OrientationUnit.Degree));
        CoordinateFrame worldFrame = new CoordinateFrame(CoordFrameRepresentation.Implicit, PredefinedCoordFrameType.World);
        System.out.println("  Joint TCP:    "+robot.toolTipPosition(worldFrame, 0));

        //var imageBytes = Files.readAllBytes(Paths.get("../ExtensionNavPanelIcon.png"));
        //pendant.registerImageData(ByteBuffer.wrap(imageBytes), "ExtensionNavPanelIcon.png");
        pendant.registerImageFile("../ExtensionNavPanelIcon.png");
        pendant.registerImageFile("../YaskawaLogo.png");

        String yml = new String(Files.readAllBytes(Paths.get("../test.yml")), StandardCharsets.UTF_8);
        var errors = pendant.registerYML(yml);
        if (errors.size() > 0) {
            System.out.println("YML Errors encountered:");
            for(var e : errors)
                System.out.println("  "+e);
        }


        pendant.registerUtilityWindow("ymlutil",true,"MyUtility",
                                      "YML Extension", "YML Extension",
                                      UtilityWindowWidth.FullWidth, UtilityWindowHeight.HalfHeight,
                                      UtilityWindowExpansion.expandableNone);

        pendant.registerIntegration("navpanel", IntegrationPoint.NavigationPanel, "ProgrammingPanel", "Extension", "ExtensionNavPanelIcon.png");


        controller.subscribeEventTypes(Set.of( 
            ControllerEventType.OperationMode, 
            ControllerEventType.ServoState,
            ControllerEventType.ActiveTool,
            ControllerEventType.PlaybackState,
            ControllerEventType.RemoteMode,
            ControllerEventType.IOValueChanged
            ));

        pendant.subscribeEventTypes(Set.of( 
            //PendantEventType.Startup,
            //PendantEventType.Shutdown,
            PendantEventType.SwitchedScreen,
            PendantEventType.UtilityOpened,
            PendantEventType.UtilityClosed,
            PendantEventType.UtilityMoved,
            PendantEventType.Clicked
            ));

        extension.ping();

        controller.setOutputName(8, "out8");
        try { Thread.sleep(300); } catch(Exception ie) {}

        controller.monitorOutput(8);
        controller.monitorIOAddress(10017);
        controller.setOutputGroups(5, 1, 1);
        controller.setOutputGroups(6, 1, 128);

        controller.addEventConsumer(ControllerEventType.OperationMode, this::onOperationModeChanged);
        controller.addEventConsumer(ControllerEventType.ServoState, this::onServoStateChanged);
        controller.addEventConsumer(ControllerEventType.PlaybackState, this::onPlaybackStateChanged);
        controller.addEventConsumer(ControllerEventType.ActiveTool, this::onActiveToolChanged);

        //var outputNumber = controller.outputNumber("out8");
        //System.out.println("output out8 # "+outputNumber);

        pendant.addEventConsumer(PendantEventType.UtilityOpened, this::onUtilityOpened);
        pendant.addEventConsumer(PendantEventType.UtilityClosed, this::onUtilityClosed);

        pendant.addItemEventConsumer("mybutton", PendantEventType.Clicked, this::onMyButtonClicked);
        pendant.addItemEventConsumer("noticebutton", PendantEventType.Clicked, this::onNoticeButtonClicked);
        pendant.addItemEventConsumer("toggleiogrp", PendantEventType.Clicked, this::onToggleIOGrpClicked);

        extension.outputEvents = true;

        try {
            // run 'forever' (or until API service shutsdown)                                      
            extension.run(() -> false);
        } catch (Exception e) {
            System.out.println("Exception occured:"+e.toString());
        }
    }

    protected boolean quit = false;


    private int clickCount = 0;
    boolean ioGroupFlip = false;


    void onOperationModeChanged(ControllerEvent e)
    {
        var opMode = e.getProps().get("name").getSValue();
        System.out.println("OperationMode: "+opMode);
    }

    void onServoStateChanged(ControllerEvent e)
    {
        var servo = e.getProps().get("name").getSValue();
        System.out.println("ServoState: "+servo);
    }

    void onPlaybackStateChanged(ControllerEvent e)
    {
        var playback = e.getProps().get("name").getSValue();
        System.out.println("PlaybackState: "+playback);
    }

    void onActiveToolChanged(ControllerEvent e)
    {
        try {
            var toolIndex = e.getProps().get("activeTool").getIValue();
            System.out.println("Ative tool: "+toolIndex);
        } catch (Exception ex) {
            System.out.println("Unable query active tool: "+ex.getMessage());
        }
    }

    void onMyButtonClicked(PendantEvent e) 
    {
        try {
            pendant.setProperty("mytext", "text", "Button clicked "+ Integer.toString(++this.clickCount)+" times.");
            pendant.setProperty("myrow", "gap", this.clickCount*5);

        } catch (Exception ex) {
            System.out.println("Unable to set message text property: "+ex.getMessage());
        }
    }

    void onNoticeButtonClicked(PendantEvent e) 
    {
        try {
            pendant.notice("Button Clicked","The Button was clicked.");
        } catch (Exception ex) {
            System.out.println("Exception: "+ex.toString());
        }
    }

    void onToggleIOGrpClicked(PendantEvent e) 
    {
        try {
            ioGroupFlip = !ioGroupFlip;
            int oldValue = controller.outputGroupsValue(3, 2);
            int newValue = ((ioGroupFlip ? 170 : 85) << 8) | (!ioGroupFlip ? 170: 85);
            controller.setOutputGroups(3, 2, newValue);
            System.out.println("Output group 3 old:"+oldValue+" new:"+newValue);
            controller.setOutput(8, ioGroupFlip);
            System.out.println("Output 8 set to :"+ioGroupFlip);

        } catch (Exception ex) {
            System.out.println("Exception: "+ex.toString());
        }
    }


    void onUtilityOpened(PendantEvent e) 
    {
        var props = e.getProps();
        if (props.get("identifier").toString() == "ymlutil") 
            System.out.println("Utility opened");
    }

    void onUtilityClosed(PendantEvent e) 
    {
        var props = e.getProps();
        if (props.get("identifier").toString() == "ymlutil") 
            System.out.println("Utility closed");
    }



    public void close()
    {
        System.out.println("Closing");
    }

    public static void main(String[] args) {
        TestExtension testExtension = null; 
        try {

            // launch
            try {
                testExtension = new TestExtension();
            } catch (Exception e) {
                System.out.println("TestExtension failed to start, aborting: "+e.toString());
            }

            // run
            testExtension.run();

        } catch (Exception e) {

            System.out.println("Exception: "+e.toString());        

        } finally {
            if (testExtension != null)
                testExtension.close();
        }
    }

}



