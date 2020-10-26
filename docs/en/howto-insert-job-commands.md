
## Programmatically Inserting Job Commands

It is often possible to achieve a smooth user experience with your extension, by pre-installing customized INFORM jobs on the controller which the user can CALL and perhaps setting up pre-defined tool properties (presets) and Block I/O commands, depending on the nature of your extension.

However, sometimes it may be beneficial to be able to programmatically insert commands (instructions) into the current INFORM robot job the user has open and is editing.  For example, if you are developing an extension for end-of-arm tooling, while you might provide an INFORM job that takes various parameters that the user can utilize, you might wish to improve the experience by providing a user-interface form or controls whereby parameters can be chosen, then insert the CALL command with the selected parameters.

The pendant API provides an `insertInstructionAtSelectedLine()` function for this purpose.

```java
    // read UI controls for job params user selected 

    // Convert TextField entry to int
    //  (illustrative - use better error checking)
    int width = Integer.parseInt(pendant.property("gripperwidth","text").getSValue());

    String cmd = "CALL JOB:OR_RG_MOVE (1, 0, "+width", \"WIDTH\")";
    // e.g. "CALL JOB:OR_RG_MOVE (1, 0, 40, \"WIDTH\")"

    pendant.insertInstructionAtSelectedLine(cmd);
```

Using this function requires the user to 1) have the current job programming screen open, 2) the controller in Manual(Teach) operation mode, and 3) the job is editable.

On success, the function returns the String `"Success"`.  Otherwise, the string will indicate the error - such as if the specified job does not exist, isn't editable etc. (see return values documented in the function reference).

Currently, the only commands supported for insertion are:

  * `CALL JOB:<name> (<param>,<param>,...)`
  * `GETS <result> <systemvar>` - e.g. `GETS B000 $B000`
