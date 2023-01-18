/**
 * <auto-generated>
 * Autogenerated by Thrift Compiler (0.17.0)
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 * </auto-generated>
 */

#pragma warning disable IDE0079  // remove unnecessary pragmas
#pragma warning disable IDE0017  // object init can be simplified
#pragma warning disable IDE0028  // collection init can be simplified
#pragma warning disable IDE1006  // parts of the code use IDL spelling
#pragma warning disable CA1822   // empty DeepCopy() methods still non-static
#pragma warning disable IDE0083  // pattern matching "that is not SomeType" requires net5.0 but we still support earlier versions

namespace Yaskawa.Ext.API
{
  /// <summary>
  /// Type of predefined coordinate frames (representaton is implicit)
  /// * Joint - the joint space of the robot (dimension equals the number of axes / dof)
  /// * World - Cartesian frame of environment (typically coincident with the robot base)
  /// * Base  - Cartesian frame of the base mount of the robot
  ///           (for robots not mounted on a moveable base, fixed relative to the robot frame)
  /// * Robot - Cartesian frame of the robot (e.g. from first axis)
  /// * ToolPlate - Cartesian frame of the tool mounting plate
  /// * ToolTip - Cartesian frame of the tip of the tool (i.e. End-Effector)
  ///             (this depends on the specific tool)
  /// * User - Cartesian frame configured by user stored in the controller
  ///          (multiple user frames can be defined and referenced by index)
  /// </summary>
  public enum PredefinedCoordFrameType
  {
    Joint = 1,
    World = 2,
    Base = 7,
    Robot = 3,
    ToolPlate = 4,
    ToolTip = 5,
    User = 6,
    None = 0,
  }
}