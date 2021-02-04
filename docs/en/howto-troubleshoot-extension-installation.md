
## Troubleshooting Extension Installation via YIP Packages

During the later stages of developing an extension, you will want to test packaging your extension into a Yaskawa Install Package (YIP) file (.yip extension) for installation on the production Smart Pendant hardware.  This is achieved using the [Smart Packager](smart-packager.html) desktop application.

In order to debug the extension, ensure you have set an "Install Override Passcode" when packaing, as you will require it to connect below.  Note that it is not recommended to distribute production YIP packages with an override passcode set, since anyone who obtains the code will be able to access your running extension environment.

Once you have created an extension .yip package file, you can install it on the Smart Pendant.
  * Place it on a USB storage device (e.g. flash thumb drive) & connect it to the pendant USB port at bottom
  * Navigate to the Package Management Screen (System Settings -> Packages menu option)
  * Click {+INSTALL} and select your package from the list and select {INSTALL}

This will create a new extension container context on the pendant and launch your extension according to the specified executable and folder properties.  If everything works as intended, your extension will run and register with the API.  However, sometimes, if your extension does appear to have launched or pehaps unexpectedly exited, you may wish to troubleshoot on the pendant.

After your extension is installed, a container context is created. 

### Default Linux Container Platform (`*:linux`) remote access

If your extension uses the Linux container platform (the default if not specified explicitly), you can remotely login to it via SSH.  For Smart Pendant 2.x, this is `armhf:linux` - so it will appear as a minimal Debian 9 (stretch) ARMhf CPU architecture environment.

To login, ensure that:
  * The YRC1000micro Controller is connected to your local network via its LAN port (or LAN2 for YRC1000).
  * Your desktop PC has connectivity to the same metwork
  * You have a suitable SSH client
    * We'll assume you are running a Debian-based Linux Desktop, such as Ubuntu below
    * Windows users may use PuTTY or other SSH clients

First, enable Development Access on the pendant, if it is not alredy enabled: Navigate to the General Settings Screen, ensure you have at least Management Security Access and check the "Enable Development Access" checkbox.  If this is the first time Development Access has been enabled on the pendant, a confirmation prompt will be shown.

Next, navigate to the Package Management Screen (System Settings -> Packages menu option) and select the Extensions tab.  Select your extension from the list and ensure it is enabled.  Since Development Access is active, you will see an "SSH Service" option in the extension detail panel at bottom.  Switch the service to "On".  This will start an SSHd service within the extension Linux container and setup a proxy from the YRC Controller port 20022 through to it.

Now, use your SSH login client to connect to the IP address of the YRC Controller LAN/LAN2 but specify port 20022 (instead of the default 22).  The LAN/LAN2 IP assigned IP address is visible on the Controller Settings Screen Network section (System Settings -> Controller menu option).  From a Linux PC (substituting the appropriate IP address):

```shell
$ slogin -l root -p 20022 192.168.0.20
root@192.168.0.20 password:
Linux com-yaskawa-yii-demo-extension-ext 4.9.18 #4 SMP PREEMPT Fri May 17 15:41:07 CDT 2019 armv7l
root@com-yaskawa-yii-demo-extension-ext:~# 
```

If you are connecting to the Demo Extension container, the Install Override Passcode will likely be "123456"; otherwise use the code you entered when packaging your extension with the Smart Packager.

You are now in a standard Debian Bash shell running within the extension container.  The prompt will reflect the canonicalName of your extension.


### Extension Service and files (`openjdk11` runtime)

The files associated with your extension&mdash;those in the package archive&mdash;are extracted to the `/extension` directory.  For example, the Demo Extension:

```shell
root@com-yaskawa-yii-demo-extension-ext:~# ls /extension
AccessTab.yml  ControlsTab.yml	DemoExtension.jar  EventsTab.yml  help	images	jobs  
LayoutTab.yml  libthrift-0.11.0.jar  NavPanel.yml  NavTab.yml  NetworkTab.yml  
slf4j-api.jar  slf4j-simple.jar  UtilWindow.yml  yaskawa-ext-2.0.4.jar
```

The systemctl that launches your extension binary is named simply `extension`:

```shell
root@com-yaskawa-yii-demo-extension-ext:~# systemctl status extension
● extension.service - Pendant Extension Service
   Loaded: loaded (/etc/systemd/system/extension.service; enabled; vendor preset: enabled)
   Active: active (running) since Tue 2021-02-02 21:35:10 UTC; 57min ago
 Main PID: 121 (extension.sh)
   CGroup: /system.slice/extension.service
           ├─121 /bin/bash /extensionService/extension.sh
           └─123 /usr/bin/java -cp DemoExtension.jar:libthrift-0.11.0.jar:slf4j-api.jar:slf4j-simple.jar:yaskawa-ext-2.0.4.jar::DemoExtension.jar DemoExtension
```

This will attempt to launch your extension several times.  The pendant will start the service after creating the running container and stop it before stopping the container (e.g. when SP exits, when the user disables the extension or prior updating your extension).  Note that your extension will recieve a quit event from the API prior to being stopped, but will be forcefully stopped if your binary does not exit.

The service can be started and stopped in the usual way using `systemctl stop extension` and `systemctl start extension`.


### Extension output logs

You can view the logs of your extension using the command:

```shell
root@com-yaskawa-yii-demo-extension-ext:~# journalctl -u extension
-- Logs begin at Thu 2021-02-04 07:26:00 UTC, end at Thu 2021-02-04 07:32:53 UTC. --
Feb 04 07:26:08 com-yaskawa-yii-demo-extension-ext systemd[1]: Started Pendant Extension Service.
Feb 04 07:26:13 com-yaskawa-yii-demo-extension-ext extension.sh[121]: ControllerEvent:PermissionGranted   permission:<Any sValue:networking>
Feb 04 07:26:13 com-yaskawa-yii-demo-extension-ext extension.sh[121]: PendantEvent:Startup
```

or even 'follow' them so you can see new output as it occurs (assuming your extension binary is running):

```shell
root@com-yaskawa-yii-demo-extension-ext:~# journalctl -f -u extension
-- Logs begin at Thu 2021-02-04 07:26:00 UTC. --
Feb 04 07:26:08 com-yaskawa-yii-demo-extension-ext systemd[1]: Started Pendant Extension Service.
Feb 04 07:26:13 com-yaskawa-yii-demo-extension-ext extension.sh[121]: ControllerEvent:PermissionGranted   permission:<Any sValue:networking>
Feb 04 07:26:13 com-yaskawa-yii-demo-extension-ext extension.sh[121]: PendantEvent:Startup
Feb 04 07:35:30 com-yaskawa-yii-demo-extension-ext extension.sh[121]: PendantEvent:UtilityOpened  identifier: <Any sValue:demoWindow>  width: <Any iValue:796>  x: <Any iValue:2>  y: <Any iValue:48>  height: <Any iValue:558>
Feb 04 07:35:31 com-yaskawa-yii-demo-extension-ext extension.sh[121]: PendantEvent:Clicked  checked: <Any bValue:true>  item: <Any sValue:eventstab>
Feb 04 07:35:32 com-yaskawa-yii-demo-extension-ext extension.sh[121]: PendantEvent:Clicked  checked: <Any bValue:false>  item: <Any sValue:popupquestion>
Feb 04 07:35:33 com-yaskawa-yii-demo-extension-ext extension.sh[121]: PendantEvent:PopupOpened  identifier: <Any sValue:myeventpopup1>
Feb 04 07:35:36 com-yaskawa-yii-demo-extension-ext extension.sh[121]: PendantEvent:PopupClosed  identifier: <Any sValue:myeventpopup1>  response: <Any sValue:positive>
```


### Java Extensions

While the package properties specifying the extension binary and directories can be used to indicate arbitrary shell scripts or binaries to launch, a common convenience for Java extensions allows just specifying the main .jar file.  This is interpreted to mean that the jar should be launched with all the .jar files provided in the Classpath.  So see exactly how your extension is launched, check the extension.sh script:

```shell
root@com-yaskawa-yii-demo-extension-ext:~# cd /extensionService/
root@com-yaskawa-yii-demo-extension-ext:/extensionService# ls
extension.sh  launchKey
root@com-yaskawa-yii-demo-extension-ext:/extensionService# cat extension.sh 
#!/bin/bash
echo $((RANDOM % 999999)) > /extensionService/launchKey
cd /extension
PATH=$PATH:.
/usr/bin/java -cp $(printf '%s:' *.jar):DemoExtension.jar  DemoExtension
root@com-yaskawa-yii-demo-extension-ext:/extensionService# 
```

Once way to troubleshoot your extension, is to manually launch it from the shell (after stopping the service manually - which will 'kill' the Java process and not allow your extension to unregister - so it will leave any UI elements on the pendant unresponsive):

```shell
root@com-yaskawa-yii-demo-extension-ext:/extensionService# systemctl stop extension
root@com-yaskawa-yii-demo-extension-ext:/extensionService# cd /extension
root@com-yaskawa-yii-demo-extension-ext:/extension# PATH=$PATH:. /usr/bin/java -cp $(printf '%s:' *.jar):DemoExtension.jar  DemoExtension
PendantEvent:Startup
...
```

If your extension sucesfully registers with the API, the SP should clean-up any UI elements that were left from the previous run.  You will be able to see the standard output and standard error of your extension binary when run this way, which should help with troubleshooting.

Happy debugging!
