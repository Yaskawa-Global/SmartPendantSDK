# Smart Pendant Extension SDK Java Quick-Start

This guide will walk you though creating a simple 'hello world' extension using Java on your desktop development environment.

## Setup

First, you will need to install a Java Development Kit (JDK) on your desktop.  The Smart Pendant Java execution environment uses [OpenJDK 11](https://openjdk.java.net/), but compatible JDK implementations for your platform will suffice - such as Oracle JDK 11 or later.

Under a Debian-based Linux desktop (such as Ubuntu) install OpenJDK via:

    sudo apt install openjdk-11-jdk

On Windows or Mac OS X, visit [jdk.java.net](https://jdk.java.net/) for downloads and installation instructions.

If you prefer, many Integrated Development Environments (IDEs) come packaged with a Java JDK, such as the Open Source [Eclipse](https://www.eclipse.org/) from IBM, Apache [NetBeans](https://netbeans.apache.org/) or [IntelliJ IDEA](https://www.jetbrains.com/idea/) by JetBrains.  This guide will utilize only the command-line, rather than any IDE, for simplicity. 

Next, you will need to obtain the Extension SDK library `yaskawa-ext-1.4.5.jar` file and a few jar files on which it depends: `libthrift-0.11.0.jar` [Apache Thrift](https://thrift.apache.org/) implementation, `slf4j-api.jar` ([Simple Logging Facade for Java](https://www.slf4j.org/)) and a concrete logger, such as `slf4j-simple.jar`.

While it is possible to build the Extension SDK, Thrift and SL4J libraries from their sources, it is simpler to download the necessary versions from here:

 * [libthrift-0.11.0.jar](https://s3.us-east-2.amazonaws.com/yaskawa-yii/SmartPendant/extension/libthrift-0.11.0.jar)
 * [slf4j-api.jar](https://s3.us-east-2.amazonaws.com/yaskawa-yii/SmartPendant/extension/slf4j-api.jar)
 * [slf4j-simple.jar](https://s3.us-east-2.amazonaws.com/yaskawa-yii/SmartPendant/extension/slf4j-simple.jar)
 * [yaskawa-ext-1.4.5.jar](https://s3.us-east-2.amazonaws.com/yaskawa-yii/SmartPendant/extension/yaskawa-ext-1.4.5.jar)

## Extension Main

Create a single `MyExtension.java` text file which will contain the Main entry point and the extension code.  Your files are independent of the pendant and so should be in an independent project folder unrelated to the Smart Pendant desktop installation.

If using an IDE, you may wish to use the IDE's built-in project management; otherwise just use the text editor of your choice.  The extension application does not require any Java-native GUI components, as the User Interface will be implemented using the Extension API and hosted within the Smart Pendant app.  Hence, if using an IDE, a 'command line' application project type will be sufficient.

Paste in the following source:  ***NOTE: If connecting to the Desktop Mock Controller App, replace -1 with 10080***

```java
import java.io.IOException;
import java.util.*;

import org.apache.thrift.TException;
import org.apache.thrift.transport.TTransportException;

import yaskawa.ext.*;


public class MyExtension {

    public MyExtension() throws TTransportException, Exception
    {
        var myExtVersion = new Version(1,0,0);
        var languages = Set.of("en");

        extension = new Extension("dev.my-extension", // canonical name
                                  myExtVersion,       // version of this extension
                                  "Acme Me",          // vendor
                                  languages,
                                  "localhost", -1); // default host/IP and port number
                                //"localhost", 10080); // default host/IP and port number
                                                       // when connecting to
                                                       //  Desktop Mock Pendant App

        // obtain references to the Pendant and Controller API functions
        pendant = extension.pendant();
        controller = extension.controller();
    }

    protected Extension extension;
    protected Pendant pendant;
    protected Controller controller;

    public void run() throws TException, IOException
    {
        // Query the version of the SP API we're communicating with (different from the Smart Pendant app version):
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
```

As required by Java, we're declaring a class `MyExtension` with the same name as our file.  It contains a standard static `main` entry point.

Once `main` instantiates the `MyExtension` class, the first thing the instance does is create an instance of `Extension`.  This triggers a connection to the Smart Pendant app API server.  The Extension API is contained in the `yaskawa.ext.api` namespace, which contains client classes automatically generated by Apache Thrift from the Thrift interface declarations (more on this later).  There are also additional convenience and wrapper classes in the `yaskawa.ext` namespace.
All of these classes are found in the `yaskawa-ext.jar` SDK library.

The arguments to the `Extension` constructor are documented in the class reference, but include a unique string used to globally identify your extension - its *canonical name* (following the Java-style reverse-DNS convension is advisable), your extension's version, vendor name and languages supported by your user-interface and IP address of the API server (Smart Pendant).  The underlying API also requires a unique launch key that the pendant provides to ensure your extension is really the extension it claims, but this is automatically inserted by the Java `Extension` constructor code, in this case.  Launch keys are only relevant for extensions installed on the pendant and are ignored during development.

As mentioned, the Extension API is divided into Pendant functions (UI integration) and Controller functions (robot & controller related).  References to these API services are obtained by the `extension.pendant()` and `extension.controller()` respectivelly.

Our simple extension calls `extension.apiVersion()` to query the Smart Pendant API server about which version of the API it is supporting.  Future versions of the API are planned to be backward-compatible (as supported by the Thrift protocol).  Finally, it calls one of the standard logging functons `extension.info`, which causes the passed string to be printed in the Smart Pendant global log.

*TODO: provide information on how to stream the log to a remote terminal in real-time*

### Building

If you are using an IDE, you'll want to add the three `.jar` files above to your project dependencies and compile the `MyExtension.java` file and place it in a jar named `MyExtension.jar`.

If using the command-line, you can issue: (or place this in a simple `build.sh` script, for example)

```bash
javac -cp libthrift-0.11.0.jar:slf4j-api.jar:yaskawa-ext-1.4.5.jar *.java
jar -cfe MyExtension.jar MyExtension MyExtension.class
```

The `-cp` option sets the Java class-path - the set of directories or jar archives in which it searches for classes.

(you'll need to adjust the paths to your jar files appropriately unless they're in the current directory)

*Windows Note:* Paths and the Java class-path use `;` as a seperator in Windows (rather than `:` as *nix, as shown above)

### Running

Once built, we can run our extension, but it will throw an exception since the Smart Pendant app API server isn't running.
You'll also need a concrete SL4J logging implementation - such as the `slf4j-simple.jar` file below which logs to standard output.

```bash
java -cp yaskawa-ext-1.4.5.jar:libthrift-0.11.0.jar:slf4j-api.jar:slf4j-simple.jar:MyExtension.jar:. MyExtension
```
(again, adjusting the paths to where your jar files are located)

You should recieve a

    Exception: org.apache.thrift.transport.TTransportException: java.net.ConnectException: Connection refused (Connection refused)

for your trouble :)

### Connecting to the Smart Pendant API


#### Desktop Smart Pendant App

For development only, it is possible to run a Desktop version of the Smart Pendant that does not support connection to the robot controller.  Instead, the desktop app has a built-in *mock controller*.  However, the mock controller is a simple proxy that is missing most of the functionality of a real controller (it is not a simulated controller, but a simple proxy to enable the app to minimally function without a real controller connected).  This may be sufficient to development some parts of your extension, particularly when developing the user interface.

Download and install the Desktop Smart Pendant Mock App.

*NOTE: Until general public availablilty, please request the Desktop Smart Pendant from Yaskawa directly via [email](../assets/images/email-contact.png).  Please specify Linux or Windows.*

Gain Development Access by navigating to the Settings -> General screen, while in the Management access level, checking the "Enable Development Access" checkbox.  The Desktop Mock App accepts factory default passcodes for security access, so the Management access passcode is "9999999999999999" (16 9s).

![Enable Development Access](assets/images/EnableDevAccess.png "Enable Development Access"){:height="480px"}

*NOTE: When using the Desktop App on a PC, not all keyboard keys will work as expected when editing text on the app - as some keys are mapped to simulated hardware pendant keys.  You may need to use the in-app virtual keyboard by clicking keys with your mouse.*


#### Physical Smart Pendant 

If you have a physical Smart Pendant available, you can direct your desktop extension to connect to the pendant's API via the network.  

In Smart Pendant 1.4.5, extension support is not available by default.  Before enabling it, you must install an 'Extension Support Update' on the pendant, then enable *Development Access*.  Unzip the following zip file onto a USB storage device (at the top level, not in a subfolder).

 * [SmartPendantExtSupport1.4.5-UpdateMedia.zip](https://s3.us-east-2.amazonaws.com/yaskawa-yii/SmartPendant/extension/SmartPendantExtSupport1.4.5-UpdateMedia.zip)

It contains two files: an enclosed zip file & a `yaskawa_update.sh` file.  Insert the USB into the Smart Pendant, either prior to startup, or while running.  If running, navigate to the System Settings -> General screen and press the *Update Pendant Software* button.  The installation will take a few minutes and then will restart the pendant.

The Smart Pendant does not normally allow connections to the Extension API externally.  To enable this, you will need to enable *Development Access*.  From the Settings -> General screen, while in the Management access level, check the "Enable Development Access" checkbox.  Note that this will permanently 'taint' the pendant for production use.  *You will need to relaunch the Smart Pendant app* for the API to accept connections from a remote host.

Finally, update your extension code by editing the connection hostname or IP in the call to `new Extension`, as shown below (from the default empty `""`).  Use the IP address of the YRC controller to which the Smart Pendant unit is connected. Obviously, the YRC controller will need to be network accessible from your desktop PC (e.g. by connecting the YRC controller LAN2 Ethernet connector to the network to which your PC is connected and ensuring a compatible LAN2 IP address has been set in the Network section of the Controller Settings screen).

*TODO: show picture.*



#### Faking Extension Installation

The Smart Pendant API service will generally only allow use by extensions that have been installed.  However, for development convenience, using the "Development Settings & Tools" it is possible to manually enter some hard-coded extension information so that the pendant will think the extension has been installed.

Once the Smart Pendant has development access enabled (see above), select the System Settings -> Development menu.  You'll see a section for hard-coded extension information:

![Hard-Coded Extension Information](assets/images/DevelopmentSettingsExtInfo.png "Hard-Coded Extension Information")

  * **Canonical Name** - is a machine-readable identifier that must be unique for all extensions from all vendors.  It should remain constant for all versions and language translations of your extension.  It is not visible to end-users.  For production, we recommend adopting a reverse-domain-name style identifier of the form "com.mycompany.my-extension".  Although it does not have to correspond to an actual domain, if your organization has a domain, you should use that for at least the first two components.  This *must match the canonical name parameter* your extension code passes to the API when registering.
  * **Display Name** - this is the name end-users will see displayed on the user-interface.  The packaging tool will allow specifying the display name in multiple languages, but this hard-coded information only allows one.  Hence, choose your main language and enter a display name in that language.  There is no need to repeat your company/organization name as part of the extension name - the Vendor will also be displayed to end-users.  For example "Gripper Model ZX77".
  * **Version** - this is your version for your extension.  It follows the [Semantic Versioning](http://semver.org) convention - MAJOR.MINOR.PATCH[-release][+build].  During development, the major version is typically 0 (so, you may opt to start with version 0.1.0, for example).  
  * **Vendor** - this is you or your organization as appropriate.  This is user visible and used to identify the extension to customers in conjunction with the Display Name.
  * **Runtime** - this corresponds to the runtime environment of an installed extension.  While not directly applicable if running the extension remotely, select the runtime corresponding to your extension (e.g. "Java 11" for an extension written using Java JDK 11 or higher).

Once you have filled-in the information, click the {Set} button and the Smart Pendant will now behave as if your extension was installed.  In particular, the API service will allow connections that register with the given Canonical Name.


#### Update API Service IP

Once you have either a Smart Pendant Desktop app or have enabled Develpment access on the physical pendant, you can edit your Java extension code to update the IP address to which it connects.  If both the Java extension and Desktop Smart Pendant are running on the same PC, the default (locahost) will suffice, otherwise enter the IPv4 address as a string (usual dotted notation).

```Java
       extension = new Extension("dev.my-extension", // canonical name
                                 myExtVersion,       // version of this extension
                                 "Acme Me",          // vendor
                                 languages,
                                 "192.168.1.55", -1);
```                                  

Ensure the canonicalName parameter matches what you entered on the Development Settings & Tools screen above.

Re-build and re-run it and you should see output similar to:
```bash
API version: 1.4.5
```

This indicates your extension sucessfully connected to the API, registered your extension and called the `apiVersion()` function to retrieve and print the version of the API the SP API server supports.

*Troubleshooting:* If your extension is failing to connect & register, check the pendant logs (e.g. pendant.log) for information.


## Adding a User Interface

### First Steps

Extensions may add their own user interface (UI) to the main pendant UI.  For consistiency across extensions and to enable tigher integration with the standard pendant interface, the UI elements are hosted within the Smart Pendant app itself.  The extension is responsible for describing the UI using the declarative [YML](yml-reference.html) markup language and providing the interaction logic to interact with it via events and API function calls.

There are a number of Smart Pendant UI integration points, where extensions may insert UI elements, such as menu items, jogging panel buttons, utility windows.  Here, we describe creating a half-screen utility window that can be opened from the main menu.

Use your IDE or text editor to create a new file named `MyUtility.yml` and paste the following content:

```qml
MyUtility : Utility 
{

    Column {
        spacing: 20

        Label { text: "My Smart Pendant Utility" }

        Text { 
            text: "Hello, World from a Java extension!"
        }

        Button {
            text: "Click Me"
        }

    }

}
```

Developers familiar with Qt's QML markup language will recognize YML's similarity.  Each UI Item is declared with a name and a pair of braces enclosing its content.  Items may be visual - such as rectangles, buttons and text or non-visual, such layout Items like `Column`.  In this case, we're declaring our own Item named `MyUtility` which inherits the properties of the built-in `Utility` `Item` type (which is required for declaring a utility window).

Each `Item` type has a set of predefined properties, whos values can be specified.  For example, the `Column` Item type has a `spacing` property which we're binding to the value `20` above.  Similarly, both the `Label` and `Text` Item types have a `text` property, which we're binding to a string (- this is actually because the `Label` type inherits from the `Text` type).

Spacing in YML works much like JSON and Javscript, where spaces and newlines are both white-space and the precise amount of white-space is not significant outside of string literals.  Properties in YML are types and can be declare to be of type `Bool`, `Int`, `Real` or `String`.  The values specified for properties can use expressions with a Javascript-like syntax (a subset of Javascript).

To register our Utility window with the Pendant API, add the following Java code to your `MyExtension.java` file and the end of the `run` method:

```java
        // read YML text from the file
        String yml = new String(Files.readAllBytes(Paths.get("MyUtility.yml")), StandardCharsets.UTF_8);
        //  and register it with the pendant
        var errors = pendant.registerYML(yml);

        // Register it as a Utility window
        pendant.registerUtilityWindow("myutil",true,"MyUtility",
                                      "My Util", "My Util",
                                      UtilityWindowWidth.FullWidth, UtilityWindowHeight.HalfHeight,
                                      UtilityWindowExpansion.expandableNone);

        // run 'forever' (or until API service shutsdown)                                      
        extension.run(() -> false);
```

You'll also need to add some imports for the symbols utilized at the top of your source file:

```java
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import yaskawa.ext.api.UtilityWindowWidth;
import yaskawa.ext.api.UtilityWindowHeight;
import yaskawa.ext.api.UtilityWindowExpansion;
```

Firstly, the text for the YML file is read into a `String` named `yml` from the `MyUtility.yml` file.  Next, this is registered with the pendant via the `pendant.registerYML` call.  We're omitting error checking here for brevity, but any statically detected syntax errors in the YML are returned by this function.  Assuming no errors, all the types declared in the YML passed will now be available to reference by the extension - in this case `MyUtility`.

Next, a call to `pendant.registerUtilityWindow` is made to request a Utility window integration in the pendant UI.  There are many parameters for the function, explained in the reference, but the key parameters here are a unique identifier (per extension) for the window `myutil`, the Item type to instantiate `MyUtility`, the text for the menu item and window title `My Util` and some parameters related to the window size.

The final `extension.run()` call runs the event loop until the passed function returns true (which it never does in this case), or until the API service sends a shutdown event (- more on events later).


*NOTE: With SP 1.4.5 or earlier, you may need to Restart or exit and re-launch the Smart Pendant app for each invocation*

![quick start utility window 0](assets/images/QuickStartUtility0.png "MyUtility Window"){:height="480px"}

If you re-build and re-run your extension, you will now notice a new menu item on the Smart Pendant under the Utility submenu, titled "My Util".  Select this item and it will open a utility window showing the content of your extension's UI.  

You can click the Button, but it will have no effect for now.

### Reacting to UI Events

In order to react to the user, your extension will need to listen for UI events generated by the YML controls, such as the `Button`, by adding event consumer callbacks.  Firstly, lets add a way to identify our `Button`.  All `Item`s in YML can include an `id` property, which provides a unique identifier by which to reference them.  Edit your `MyUtility.yml` to add an `id` property to the `Button`:

```qml
Button {
    id: mybutton
    text: "Click Me"
}
```

Now, add the following line just prior to the call to `extension.run()`, which registers a callback to the method `onClicked` (which we'll define) when a `Clicked` event is received from the button.

```java
pendant.addItemEventConsumer("mybutton", PendantEventType.Clicked, this::onClicked);
```

and the new method:

```java
void onClicked(PendantEvent e) {
    System.out.println("button clicked!");
}
```

You'll need some additional imports too:

```java
import yaskawa.ext.api.PendantEvent;
import yaskawa.ext.api.PendantEventType;
```

Now if you re-build & re-run, clicking on the button should result in the "button clicked!" being printed to standard output.

### Setting Properties

We've seen some properties that are supported by the Items we've used - such as `text` & `spacing`.  The [YML reference](yml-reference.html) provides a complete list of the properties supported by each Item type.  Property values can also be read and updated at runtime through the API.  For example, lets change the message text once the button has been clicked.  First, add an `id` to the `Text` Item so we can reference it:

```qml
Text { 
    id: message
    text: "Hello, World from a Java extension!"
}
```

Then, in our `onClicked` callback, set the `text` property:

```java
try {
    System.out.println("button clicked!");

    pendant.setProperty("message", "text", "Thanks for clicking!");
    
} catch (Exception ex) {
    System.out.println("Unable to set message text property: "+ex.getMessage());
}
```

The key is the `setProperty` call, which takes the id of the Item (the `Text`), the name of the property to set (`text`) and the new value.  Note that the `setProperty` method is overloaded in the SDK client for common Java types, but to avoid runtime errors, the type should match the property type appropriately (although sensible conversions will be performed automatically, for example you can set a `Real` property from an integer).

You'll also notice that Java requires us to catch Exceptions in our callback, since we can't declare `onClicked` to thow exceptions as the standard Consumer functional interface doesn't declare consumers as throwing them.
