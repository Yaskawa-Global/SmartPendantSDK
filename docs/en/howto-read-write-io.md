
## Reading and Writing Controller I/O signals

The `controller` interface provides numerous functions for working with I/O.  I/O signals can be named (either manually from the pendant UI, or by supplying names in an install package).  The functions allow reading and writing inputs and outputs either by name or number.  An I/O signal is a single bit. However, I/O signals can be grouped into sets, of 1, 2 or 4 bytes (i.e. 8, 16 or 32 I/O signals).  Yaskawa documentation often uses the terms *group* and *byte* interchangeably to refer to a set of 8 I/O signals.  The functions allow referencing I/O either by individual I/O bit number or group(byte) number.

It is convenient to view the pendant I/O screen for reference, as it shows both I/O signal numbers and group numbers.  Also, if you hold-and-press a group, the right side of the screen will allow viewing and editing of 1,2,3 or 4 bytes as single decimal or hexadecimal values.

For example, to set the value of output 25 to 'ON' from your extension code, you can write:
```java
    controller.setOutput(25,true);
```
If the output has been named, you can also use the name.  That will make your code more robust to device I/O being reallocated at another address but retaining the same name:
```java
    int outputNum = controller.outputNumber("CameraEnable");
    controller.setOutput(outputNum,false); // turn off inspection camera
```

Similarly, you can read inputs:
```java
    System.out.println("Input 26 is " + controller.inputValue(26)); // true or false
```

To input and output groups of multiple I/O bits, use the corresponding 'Groups' functions:
```java
    // set group 4 (1 byte/group), to value 255 (i.e. set outputs 33-40 to 'on'/true)
    controller.setOutputGroups(4, 1, 255);
    
    // set groups 4 & 5 (2 bytes/groups), to value 43690 = 1010101010101010 in binary
    //  (i.e. outputs 33-48 to alternating on/off pattern)
    controller.setOutputGroups(4, 2, 43690);

    // read byte value of group 7 (1 group)
    System.out.println("group 7 value =" + controller.outputGroupsValue(7,1)); // 0-255
```

You can also programmatically modify the names of inputs and outputs:
```java
    controller.setOutputName(8, "myOutputName");
```

### Monitoring

If you need to periodically check the values of I/O in your code, rather than having to program a timer to repeatedly call `outputGroupsValue()` (for example), you can instead request I/O signals and groups to be monitored for you, and when their value changes, an event will be generated which you can capture with a callback function.

```java
    // in your init code, subscribe once to the IOvalueChanged event type
    controller.subscribeEventTypes(Set.of( 
        ControllerEventType.IOValueChanged
    ));

    controller.monitorOutput(8);

    controller.addEventConsumer(ControllerEventType.IOValueChanged, this::onIoValueChanged);
```
```java
    void onIoValueChanged(ControllerEvent e)
    {
        System.out.println(e.getProps());
        // possible output:
        // {address=<Any iValue:10017>, num=<Any iValue:8>, 
        //  groupNum=<Any iValue:1>, type=<Any sValue:output>, value=<Any bValue:true>}
        System.out.println(e.getProps().get("value").getBValue()); // true or false output value
    }

```

Now if you use the pendant I/O screen to manupulate output 8, you will see change events.

To stop receiving events for a particular I/O:
```java
    controller.unmonitorOutput(8);
```

Similar functions are available for monitoring groups: `monitorInputGroups`, `monitorOutputGroups`, `unmonitorInputGroups` and `unmonitorOutputGroups`.

Refer to the [Controller API Reference](gen-html/extension.html#Svc_Controller) for full details (- keeping in mind that where the programming language neutral reference documentation lists functions of the form `setOutputName(ControllerID c, i32 num, string name)` when mapped to the Java client library, it is used as `controller.setOutputName(int num, String name)`).

*Note: the current monitoring implementation has limited frequency (3hz), so if an I/O changes value and back within a small fraction of a second, the change may go unnoticed.*


### Logical Addresses (advanced)

The YRC1000(micro) controllers have advanced I/O mapping facilities.  The I/O and group numbers discussed above are actually mapped to logical I/O addresses dynamically.  While for most applications you will not need to alter the standard mapping from I/O numbers to logical addresses, the API has analogous functions for referencing I/O by its underlying logical address (with `Address` in the function name).

```java
    int outAddr = controller.outputAddress("GripperClose");
    controller.setOutputAddress(outAddr, true);
    int otherOutAddr = controller.outputAddressByNumber(25);
```