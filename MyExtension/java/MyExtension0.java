import java.io.IOException;
import java.util.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import yaskawa.ext.api.UtilityWindowWidth;
import yaskawa.ext.api.UtilityWindowHeight;
import yaskawa.ext.api.UtilityWindowExpansion;

import yaskawa.ext.*;



public class MyExtension {

    public MyExtension() throws TTransportException, Exception
    {
        var myExtVersion = new Version(1,0,0);
        var languages = Set.of("en");

        extension = new Extension("mylaunchkey",
                                  "dev.my-extension", 
                                  myExtVersion, "Your Name", languages,
                                  "localhost", -1);

        // obtain references to the Pendant and Controller API functions
        pendant = extension.pendant();
        controller = extension.controller();
    }

    protected Extension extension;
    protected Pendant pendant;
    protected Controller controller;

    public void run() throws TException, IOException
    {
        // Query the verson of the SP API we're communicating with (different from the Smart Pendant app version):
        System.out.println("API version: "+extension.apiVersion());

        // Send a message to the SP log
        extension.info("Hello Smart Pendant, I'm MyExtension");


        // read YML text from the file
        String yml = new String(Files.readAllBytes(Paths.get("MyUtility.yml")), StandardCharsets.UTF_8);
        //  and register it with the pendant
        var errors = pendant.registerYML(yml);

        // Register it as a Utility window
        pendant.registerUtilityWindow("myutil",true,"MyUtility",
                                      "YML Util", "YML Util",
                                      UtilityWindowWidth.FullWidth, UtilityWindowHeight.HalfHeight,
                                      UtilityWindowExpansion.expandableNone);

        // run 'forever' (or until API service shutsdown)                                      
        extension.run(() -> false);
    }


    public static void main(String[] args) 
    {
        try {

            MyExtension myExtension = new MyExtension();
            myExtension.run();

        } catch (Exception e) {
            System.out.println("Exception: "+e.toString());    
        }
        
    }

}



