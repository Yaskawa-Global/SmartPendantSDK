import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.ByteBuffer;

import yaskawa.ext.api.ControllerEvent;
import yaskawa.ext.api.ControllerEventType;
import yaskawa.ext.api.IntegrationPoint;
import yaskawa.ext.api.PendantEvent;
import yaskawa.ext.api.PendantEventType;
import yaskawa.ext.api.UtilityWindowWidth;
import yaskawa.ext.api.UtilityWindowHeight;
import yaskawa.ext.api.UtilityWindowExpansion;

import yaskawa.ext.*;

import java.util.*;


public class TestExtension {

    public TestExtension() throws TTransportException, Exception
    {
        var version = new Version(1,0,0);
        var languages = Set.of("en", "ja");

        extension = new Extension("mylaunchkey",
                                "yii.test-extension", 
                                version, "YII", languages);

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

        //var outputNumber = controller.outputNumber("out8");
        //System.out.println("output out8 # "+outputNumber);

        // handle events until we get shutdown event
        while(!quit) {

            try {
                pollForEvents();

                Thread.sleep(300);

                //System.out.println("DOut #"+outputNumber+":"+controller.outputValue(outputNumber));//!!!

            } catch (TTransportException te) {

                System.out.println("Connect to API Service lost (exiting):"+te.toString());
                quit = true;
            } catch (Exception e) {
                System.out.println("Event handling exception occured:"+e.toString());
                try { Thread.sleep(300); } catch(InterruptedException ignore) {}
                //quit=true;
            }
        }

    }

    protected boolean quit = false;


    private int clickCount = 0;
    boolean ioGroupFlip = false;

    protected void pollForEvents() throws TException
    {

        for (ControllerEvent e : controller.events()) {
            System.out.print("ControllerEvent:"+e.eventType);
            for(var p : e.getProps().entrySet()) 
                System.out.print("   "+p.getKey()+":"+p.getValue());
            System.out.println();
        }

        for (PendantEvent e : pendant.events()) {
            System.out.print("PendantEvent:"+e.eventType);
            var props = e.getProps();
            for(var p : props.entrySet()) 
                System.out.print("  "+p.getKey()+": "+p.getValue());
            System.out.println();

            switch (e.eventType) {
                case Clicked: {
                    if (props.get("item").equals("mybutton")) {
                        pendant.setProperty("mytext", "text", "Button clicked "+ Integer.toString(++this.clickCount)+" times.");
                        pendant.setProperty("myrow", "gap", this.clickCount*5);
                    }
                    else if (props.get("item").equals("noticebutton"))
                        pendant.notice("Button Clicked","The Button was clicked.");
                    else if (props.get("item").equals("toggleiogrp")) {
                        ioGroupFlip = !ioGroupFlip;
                        int oldValue = controller.outputGroupsValue(3, 2);
                        int newValue = ((ioGroupFlip ? 170 : 85) << 8) | (!ioGroupFlip ? 170: 85);
                        controller.setOutputGroups(3, 2, newValue);
                        System.out.println("Output group 3 old:"+oldValue+" new:"+newValue);
                        controller.setOutput(8, ioGroupFlip);
                        System.out.println("Output 8 set to :"+ioGroupFlip);

                    }
                } break;
                case UtilityOpened: {
                    if (props.get("identifier") == "ymlutil") {
                        System.out.println("Utility opened");
                    }
                } break;
                case UtilityClosed: {
                    if (props.get("identifier") == "ymlutil") {
                        System.out.println("Utility closed");
                    }
                } break;
                case Shutdown: {
                    quit = true;
                } break;
            }



        }        
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



