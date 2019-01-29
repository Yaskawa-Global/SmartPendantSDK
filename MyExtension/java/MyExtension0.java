import java.io.IOException;
import java.util.*;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import yaskawa.ext.*;



public class MyExtension {

    public MyExtension() throws TTransportException, Exception
    {
        var version = new Version(1,0,0);
        var languages = Set.of("en");

        extension = new Extension("mylaunchkey",
                                  "dev.my-extension", 
                                  version, "Your Name", languages,
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
        // Query the verson of the SP API we're communicating with:
        System.out.println("API version: "+extension.apiVersion());

        // Send a message to the SP log
        extension.info("Hello Smart Pendant, I'm MyExtension");
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



