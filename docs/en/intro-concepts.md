# Concepts

A brief conceptual introduction to the main features of the YRC controller and API, relevant to extension development, is given below.

## Tools

...
Note: The YRC Controller associates a Tool with each Position (e.g. the Tool that was active when it was taught).  However, this is not currently provided by the API when accessing Position variables; though if a variable's Position is defined in a Tool CoordinateFrame will have the tool that defines the frame associated with it.


## Variables

The controller provides storage for variables that can be used by controller jobs (e.g. INFORM jobs).

 * **Types**: YRC Controllers support variables with types *Byte*, *Integer* (16bits), *Double* Integer (32bits), *Real* (floating point), *String* (text character sequences) and *Position*.

 * **Scope**: *Global* scope variables persist on the controller independently of any running programs/jobs and can be accessed by any job, whereas *Local* scope variables are job-specific and only exist while a job is running.  Extension can access global variables as a means to communicate with INFORM jobs or MotoPlus apps running on the controller.

Variables can be accessed by name or address.

**Addresses**: Most developers are accustomed to their programming language of choice managing the 
relationship between named variables and their storage location (numeric memory address), though in some languages the address of a variable is accessible (e.g. C).  In such languages, all variables, regardless of type, occupy a single address-space.

However, in the YRC controller series, variables are segregated according to type into different addresses spaces.  These different address-spaces and numeric addresses are presented to the user by prefixing variables with their type.  For example:

 * `B000` - the Byte variable at address (index) 0
 * `I050` - the Integer variable at address 50
 * `P200` - the Position variable at address 200
 * `D200` - the Double Integer at address 200

`P200` and `D200` are different variables since they live in difference address-spaces - the Position address-space and the Double Integer address-space respectively.

Variables may or may not have an associated name.  The names of Global variable can be set via the API.  It is good practice to use globally unique names for variables.


## I/O

Controllers may support mutiple types of Input/Output, including physical digital wires, network 
I/O with various protocols, virtual controller states etc.  

I/O can be referenced via two different address spaces: 
 - User-facing input, output and group numbers, referenced in the pendant interface 
   or user jobs/programs.  
 - Logical I/O numeric addresses, which cover inputs and outputs of all types.

The mapping from user input & output numbers to the underlying Logical I/O address is configurable (though often setup during manufacturing infrequently changed).

Inputs & Outputs represent single bits.  An I/O group is a set of 8 inputs/outputs (i.e. a byte).
1-4 groups can be read & written together, represend as 1-4 bytes (8,16,24 or 32 bits).

Fetching multiple I/O bits synchronously frequently via the Controller I/O functions is inefficient 
and should be avoided.  Prefer adding relevant I/O numbers to the monitored set and reacting
to IOValueChanged events instead.


## Zones

...


## User Frames

...


## Control Groups

A ControlGroup represents a set of axes that can be controlled - such as a robot, an external base (e.g. a rail) or a station (e.g. a part fixture able to rotate and tilt).

In many cases, the only control group defined is `R1` - a single robot connected to the controller.
However, the YRC1000 Controller is capable of supporting up-to 8 robots (or 72 axes).  The YRC1000micro and Smart Pendant only support a single robot.

Custom control groups can be configured on the controller, by combining simple groups - for example, by combining a robot and a base and station, or two robots etc.  

In addition, the controller may support coordinated motion between a master & slave control group, 
such that one will move in response to commanded motions of the other.  For example, a control 
group including a robot and a station where the station is the master, allows motions commanding the station (for example holding a part) to cause the robot to move in order to maintain the same relationship between the robot tool and the part on the station.

For example, `R1+R2+B2+S1:S1` designates a combined control group, consisting of the simple control groups corresponding to Robot 1, Robot 2 incuding a Base, and Station 1.  Additionally, Station 1 is the master control group.
