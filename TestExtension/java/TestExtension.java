import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.ByteBuffer;

import yaskawa.ext.api.IllegalArgument;
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
import yaskawa.ext.api.Any;


import yaskawa.ext.*;

import java.util.*;


public class TestExtension {

    public TestExtension() throws TTransportException, IllegalArgument, Exception
    {
        var version = new Version(1,0,0);
        var languages = Set.of("en", "ja");

        extension = new Extension("yii.test-extension",
                                version, "YII", languages,
                                "localhost", 10080);
                                //"10.7.3.130", 10080);

        extension.copyLoggingToStdOutput = true;
        extension.info("Starting");

        System.out.println("API version: "+extension.apiVersion());
        pendant = extension.pendant();
        controller = extension.controller();
        System.out.println("Controller software version:"+controller.softwareVersion());

        extension.subscribeLoggingEvents();
    }

    protected Extension extension;
    protected Pendant pendant;
    protected Controller controller;

    public void run() throws TException, IOException
    {
        pendant.notice("Running", "TestExtension is now running.");

        System.out.println("Current locale:"+pendant.currentLocale());

        var tools = controller.tools();
        for (Map.Entry<Integer,String> tool : tools.entrySet()) {
            System.out.println(tool.getKey() + ":"+tool.getValue());
        }

        var tool = controller.tool(3);
        System.out.println("index:"+tool.index);
        System.out.println("weight:"+tool.weight);
        System.out.println("offset:"+tool.offset);
        System.out.println("orient:"+tool.orient);
        System.out.println("centerOfMass:"+tool.centerOfMass);
        System.out.println("momentOfInertia:"+tool.momentOfInertia);
        if (tool.isSetBlockIOName())
            System.out.println("blockIOName:"+tool.blockIOName);

        System.out.println("user frames");
        var userframes = controller.userFrames();
        for (Map.Entry<Integer,String> userFrame : userframes.entrySet()) {
            System.out.println(userFrame.getKey() + ":"+userFrame.getValue());
        }


        System.out.println("Screen Name:"+pendant.currentScreenName());
        System.out.println("Current Job:"+controller.currentJob());
        System.out.println("Default Job:"+controller.defaultJob());
        VariableAddress addr = new VariableAddress(Scope.Global, AddressSpace.Position, 1);
        controller.setVariableName(addr, "My Test Name");
        System.out.println("Variable My Test Name:"+controller.variable("My Test Name"));
        controller.setVariableName(new VariableAddress(Scope.Global, AddressSpace.Byte, 28), "BVar 28");
        System.out.println("BVar 28 address:"+controller.variableAddrByNameAndSpace("BVar 28", AddressSpace.Byte));

        addr = new VariableAddress(Scope.Global, AddressSpace.Real, 5);
        controller.setVariableName(addr, "RVar 5");
        controller.setVariable("RVar 5", 123.456);

        controller.setOutput(4, true);
        Long value = 1L+(128L<<8)+(1L<<16)+(128L<<24);
        System.out.println("Setting output group 2 to "+value.intValue());
        controller.setOutputGroups(2,4,value.intValue());

        //System.out.println("Zone index 0:"+controller.zone(0));
        System.out.println("UserFrame index 0:"+controller.userFrame(0));
        System.out.println("UserFrame index 1:"+controller.userFrame(1));
        //System.out.println("UserFrame index 4:"+controller.userFrame(4));

        var robot = controller.currentRobot();
        System.out.println("Robot:"+robot.model());
        System.out.println("  Joint Joints: "+robot.jointPosition(OrientationUnit.Degree));
        CoordinateFrame worldFrame = new CoordinateFrame(CoordFrameRepresentation.Implicit, PredefinedCoordFrameType.World);
        System.out.println("  Joint TCP:    "+robot.toolTipPosition(worldFrame, 0));

        System.out.println("Exists job IFTHEN?"+controller.jobExists("IFTHEN"));
        //System.out.println("IFTHEN source:"+controller.jobSource("IFTHEN"));
        //System.out.println("NEWJOB1 source:"+controller.jobSource("NEWJOB1"));
        String myjob = "/JOB\r\n"+
            "//NAME NEWJOB1\r\n"+
            "//POS\r\n"+
            "///NPOS 0,0,0,0,0,0\r\n"+
            "//INST\r\n"+
            "///DATE 2020/03/26 23:59\r\n"+
            "///ATTR SC,RW\r\n"+
            "///GROUP1 RB1\r\n"+
            "///LVARS 1,1,1,0,0,0,0,0\r\n"+
            "NOP\r\n"+
            "SET LB000 0\r\n"+
            "SET LI000 0\r\n"+
            "SET LD000 0\r\n"+
            "*LABEL\r\n"+
            "CALL JOB:JOB1\r\n"+
            "TIMER T=5.00\r\n"+
            "INC LB000\r\n"+
            "TIMER T=2.00\r\n"+
            "INC LI000\r\n"+
            "TIMER T=2.00\r\n"+
            "INC LD000\r\n"+
            "TIMER T=5.00\r\n"+
            "JUMP *LABEL\r\n"+
            "END\r\n";
        //controller.storeJobSource("NEWJOB1", "INFORM", myjob);

        System.out.println("Job details for IFTHEN:"+ controller.jobDetails("IFTHEN").toString());

        //controller.duplicateJob("IFTHEN", "IFTHEN2");
        //controller.deleteJob("IFTHEN");

        //var imageBytes = Files.readAllBytes(Paths.get("../ExtensionNavPanelIcon.png"));
        //pendant.registerImageData(ByteBuffer.wrap(imageBytes), "ExtensionNavPanelIcon.png");
        pendant.registerImageFile("ExtensionNavPanelIcon.png");
        pendant.registerImageFile("YaskawaLogo.png");
        pendant.registerImageFile("GripperOpenClose.png");

        pendant.registerHTMLFile("html/en/help-phlebotinum.html");
        pendant.registerImageFile("html/en/phlebotinum.png");
        //pendant.registerHTMLFile("html/en/myhtml1.html");
        //pendant.registerHTMLFile("myhtml0.html");

        String yml = new String(Files.readAllBytes(Paths.get("test.yml")), StandardCharsets.UTF_8);
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

        pendant.registerIntegration("gripper", IntegrationPoint.SmartFrameJogPanelTopRight, "", "", "GripperOpenClose.png");
        pendant.registerIntegration("settings", IntegrationPoint.SmartFrameJogPanelBottomCenter, "", "Settings", "");


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

        //!!!
//        controller.setOutput(25,true);
//        int outputNum = controller.outputNumber("CameraEnable");
//        controller.setOutput(outputNum,false);
//        System.out.println("Input 26 is " + controller.inputValue(26));
//        controller.setOutputGroups(4, 2, 43690);
//        controller.setOutputGroups(4, 1, 255);
        System.out.println("group 7 value =" + controller.outputGroupsValue(7,1)); // 0-255
        //!!!

        controller.addEventConsumer(ControllerEventType.IOValueChanged, this::onIoValueChanged);
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
        pendant.addItemEventConsumer("mycheckbox", PendantEventType.CheckedChanged, this::onMyCheckBoxCheckedChanged);
        pendant.addItemEventConsumer("myselector", PendantEventType.Activated, this::onMyComboBoxActivated);

        pendant.addItemEventConsumer("atextfield", PendantEventType.TextEdited, this::onTextEdited);
        pendant.addItemEventConsumer("setimage", PendantEventType.Clicked, this::onSetImageClicked);

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


    void onIoValueChanged(ControllerEvent e)
    {
        System.out.println(e.getProps());
    }

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
            if (this.clickCount > 5)
                pendant.closeUtilityWindow("ymlutil");

            Any a = pendant.property("mytext", "text");
            System.out.println(a.isSetBValue());
            System.out.println(a.isSetSValue()); // true - text is a String
            System.out.println(a.isSetRValue()); // false - not a double

            a.setIValue(10); // change type to int and value to 10
            System.out.println(a.isSetIValue()); // true
            System.out.println(a.getIValue()); // 10

            int i = 40;
            String cmd = "sdfdss"+i+"sdfds";

        } catch (Exception ex) {
            System.out.println("Unable to set message text property: "+ex.getMessage());
        }

        try {
            System.out.print("myarr:");
            System.out.println( pendant.property("row2", "myarr") );//!!!
            System.out.println("mymap:");
            System.out.println( pendant.property("row2", "mymap") );//!!!
            System.out.println("setting myarr");
            pendant.setProperty("row2", "myarr", new Integer[]{9,8,7,6});
            System.out.println("setting mymap");
            pendant.setProperty("row2", "mymap", Map.of("a",3,"b",4,"c",5));
        } catch (Exception ex2) {
            System.out.println("Unable to get row2 property: "+ex2);
        }
    }

    void onMyCheckBoxCheckedChanged(PendantEvent e)
    {
        try {
            boolean checked = e.getProps().get("checked").getBValue();
            System.out.println("CheckBox state:" + checked);
            if (checked)
                pendant.setProperty("myselector", "options", new String[] {"AAA", "BBB", "CCC"} );
            else
                pendant.setProperty("myselector", "options", new String[] {"zzz", "yyy", "xxx"} );
        } catch (Exception ex) {
            System.out.println("Unable to set options property: "+ex.getMessage());
        }

        try {
            // read back
            System.out.println("myselector options:"+pendant.property("myselector","options"));
        } catch (Exception ex) {
            System.out.println("Unable to get options property: "+ex.getMessage());
        }
    }

    void onMyComboBoxActivated(PendantEvent e)
    {
        System.out.println("ComboxBox selection:" + e.getProps().get("index").getIValue());
    }

    void onNoticeButtonClicked(PendantEvent e) 
    {
        try {
            pendant.notice("Button Clicked","The Button was clicked.");
            pendant.setProperty("row2", "opt1", "Index 0");

            for(var event : extension.logEvents()) {
                System.out.println("LOG "+event.datetime+": "+event.entry);
            }

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

    void onTextEdited(PendantEvent e) 
    {
        try {
            var value = e.getProps().get("text").getSValue();
            System.out.println("TextField edited:"+value);
            pendant.setProperty("posttext", "text", "You entered:"+value);
        } catch (Exception ex) {
            System.out.println("Exception: "+ex.toString());
        }
    }

    void onSetImageClicked(PendantEvent e)
    {
        try {
            System.out.println("Orig prop value="+pendant.property("dynamicimage","source"));

            System.out.println("Updating image source property");
            //pendant.setProperty("dynamicimage", "source", "ExtensionNavPanelIcon.png");
            pendant.setProperty("dynamicimage", "source","data:image/png;base64,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");
            // read back
            System.out.println("New prop value="+pendant.property("dynamicimage","source"));
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



