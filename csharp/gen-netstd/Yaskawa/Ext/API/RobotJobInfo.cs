/**
 * <auto-generated>
 * Autogenerated by Thrift Compiler (0.17.0)
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 * </auto-generated>
 */
using System;
using System.Collections;
using System.Collections.Generic;
using System.Text;
using System.IO;
using System.Linq;
using System.Threading;
using System.Threading.Tasks;
using Microsoft.Extensions.Logging;
using Thrift;
using Thrift.Collections;
using Thrift.Protocol;
using Thrift.Protocol.Entities;
using Thrift.Protocol.Utilities;
using Thrift.Transport;
using Thrift.Transport.Client;
using Thrift.Transport.Server;
using Thrift.Processor;


#pragma warning disable IDE0079  // remove unnecessary pragmas
#pragma warning disable IDE0017  // object init can be simplified
#pragma warning disable IDE0028  // collection init can be simplified
#pragma warning disable IDE1006  // parts of the code use IDL spelling
#pragma warning disable CA1822   // empty DeepCopy() methods still non-static
#pragma warning disable IDE0083  // pattern matching "that is not SomeType" requires net5.0 but we still support earlier versions

namespace Yaskawa.Ext.API
{

  public partial class RobotJobInfo : TBase
  {
    private string _name;
    private string _programmingLanguage;
    private string _jobType;
    private bool _editable;
    private long _timestamp;
    private string _datetime;
    private string _comment;
    private global::Yaskawa.Ext.API.CoordinateFrame _frame;
    private global::Yaskawa.Ext.API.ControlGroup _controlling;

    public string Name
    {
      get
      {
        return _name;
      }
      set
      {
        __isset.name = true;
        this._name = value;
      }
    }

    public string ProgrammingLanguage
    {
      get
      {
        return _programmingLanguage;
      }
      set
      {
        __isset.programmingLanguage = true;
        this._programmingLanguage = value;
      }
    }

    public string JobType
    {
      get
      {
        return _jobType;
      }
      set
      {
        __isset.jobType = true;
        this._jobType = value;
      }
    }

    public bool Editable
    {
      get
      {
        return _editable;
      }
      set
      {
        __isset.editable = true;
        this._editable = value;
      }
    }

    public long Timestamp
    {
      get
      {
        return _timestamp;
      }
      set
      {
        __isset.timestamp = true;
        this._timestamp = value;
      }
    }

    public string Datetime
    {
      get
      {
        return _datetime;
      }
      set
      {
        __isset.datetime = true;
        this._datetime = value;
      }
    }

    public string Comment
    {
      get
      {
        return _comment;
      }
      set
      {
        __isset.comment = true;
        this._comment = value;
      }
    }

    public global::Yaskawa.Ext.API.CoordinateFrame Frame
    {
      get
      {
        return _frame;
      }
      set
      {
        __isset.frame = true;
        this._frame = value;
      }
    }

    public global::Yaskawa.Ext.API.ControlGroup Controlling
    {
      get
      {
        return _controlling;
      }
      set
      {
        __isset.controlling = true;
        this._controlling = value;
      }
    }


    public Isset __isset;
    public struct Isset
    {
      public bool name;
      public bool programmingLanguage;
      public bool jobType;
      public bool editable;
      public bool timestamp;
      public bool datetime;
      public bool comment;
      public bool frame;
      public bool controlling;
    }

    public RobotJobInfo()
    {
    }

    public RobotJobInfo DeepCopy()
    {
      var tmp193 = new RobotJobInfo();
      if((Name != null) && __isset.name)
      {
        tmp193.Name = this.Name;
      }
      tmp193.__isset.name = this.__isset.name;
      if((ProgrammingLanguage != null) && __isset.programmingLanguage)
      {
        tmp193.ProgrammingLanguage = this.ProgrammingLanguage;
      }
      tmp193.__isset.programmingLanguage = this.__isset.programmingLanguage;
      if((JobType != null) && __isset.jobType)
      {
        tmp193.JobType = this.JobType;
      }
      tmp193.__isset.jobType = this.__isset.jobType;
      if(__isset.editable)
      {
        tmp193.Editable = this.Editable;
      }
      tmp193.__isset.editable = this.__isset.editable;
      if(__isset.timestamp)
      {
        tmp193.Timestamp = this.Timestamp;
      }
      tmp193.__isset.timestamp = this.__isset.timestamp;
      if((Datetime != null) && __isset.datetime)
      {
        tmp193.Datetime = this.Datetime;
      }
      tmp193.__isset.datetime = this.__isset.datetime;
      if((Comment != null) && __isset.comment)
      {
        tmp193.Comment = this.Comment;
      }
      tmp193.__isset.comment = this.__isset.comment;
      if((Frame != null) && __isset.frame)
      {
        tmp193.Frame = (global::Yaskawa.Ext.API.CoordinateFrame)this.Frame.DeepCopy();
      }
      tmp193.__isset.frame = this.__isset.frame;
      if((Controlling != null) && __isset.controlling)
      {
        tmp193.Controlling = (global::Yaskawa.Ext.API.ControlGroup)this.Controlling.DeepCopy();
      }
      tmp193.__isset.controlling = this.__isset.controlling;
      return tmp193;
    }

    public async global::System.Threading.Tasks.Task ReadAsync(TProtocol iprot, CancellationToken cancellationToken)
    {
      iprot.IncrementRecursionDepth();
      try
      {
        TField field;
        await iprot.ReadStructBeginAsync(cancellationToken);
        while (true)
        {
          field = await iprot.ReadFieldBeginAsync(cancellationToken);
          if (field.Type == TType.Stop)
          {
            break;
          }

          switch (field.ID)
          {
            case 1:
              if (field.Type == TType.String)
              {
                Name = await iprot.ReadStringAsync(cancellationToken);
              }
              else
              {
                await TProtocolUtil.SkipAsync(iprot, field.Type, cancellationToken);
              }
              break;
            case 2:
              if (field.Type == TType.String)
              {
                ProgrammingLanguage = await iprot.ReadStringAsync(cancellationToken);
              }
              else
              {
                await TProtocolUtil.SkipAsync(iprot, field.Type, cancellationToken);
              }
              break;
            case 3:
              if (field.Type == TType.String)
              {
                JobType = await iprot.ReadStringAsync(cancellationToken);
              }
              else
              {
                await TProtocolUtil.SkipAsync(iprot, field.Type, cancellationToken);
              }
              break;
            case 4:
              if (field.Type == TType.Bool)
              {
                Editable = await iprot.ReadBoolAsync(cancellationToken);
              }
              else
              {
                await TProtocolUtil.SkipAsync(iprot, field.Type, cancellationToken);
              }
              break;
            case 5:
              if (field.Type == TType.I64)
              {
                Timestamp = await iprot.ReadI64Async(cancellationToken);
              }
              else
              {
                await TProtocolUtil.SkipAsync(iprot, field.Type, cancellationToken);
              }
              break;
            case 6:
              if (field.Type == TType.String)
              {
                Datetime = await iprot.ReadStringAsync(cancellationToken);
              }
              else
              {
                await TProtocolUtil.SkipAsync(iprot, field.Type, cancellationToken);
              }
              break;
            case 7:
              if (field.Type == TType.String)
              {
                Comment = await iprot.ReadStringAsync(cancellationToken);
              }
              else
              {
                await TProtocolUtil.SkipAsync(iprot, field.Type, cancellationToken);
              }
              break;
            case 8:
              if (field.Type == TType.Struct)
              {
                Frame = new global::Yaskawa.Ext.API.CoordinateFrame();
                await Frame.ReadAsync(iprot, cancellationToken);
              }
              else
              {
                await TProtocolUtil.SkipAsync(iprot, field.Type, cancellationToken);
              }
              break;
            case 9:
              if (field.Type == TType.Struct)
              {
                Controlling = new global::Yaskawa.Ext.API.ControlGroup();
                await Controlling.ReadAsync(iprot, cancellationToken);
              }
              else
              {
                await TProtocolUtil.SkipAsync(iprot, field.Type, cancellationToken);
              }
              break;
            default: 
              await TProtocolUtil.SkipAsync(iprot, field.Type, cancellationToken);
              break;
          }

          await iprot.ReadFieldEndAsync(cancellationToken);
        }

        await iprot.ReadStructEndAsync(cancellationToken);
      }
      finally
      {
        iprot.DecrementRecursionDepth();
      }
    }

    public async global::System.Threading.Tasks.Task WriteAsync(TProtocol oprot, CancellationToken cancellationToken)
    {
      oprot.IncrementRecursionDepth();
      try
      {
        var tmp194 = new TStruct("RobotJobInfo");
        await oprot.WriteStructBeginAsync(tmp194, cancellationToken);
        var tmp195 = new TField();
        if((Name != null) && __isset.name)
        {
          tmp195.Name = "name";
          tmp195.Type = TType.String;
          tmp195.ID = 1;
          await oprot.WriteFieldBeginAsync(tmp195, cancellationToken);
          await oprot.WriteStringAsync(Name, cancellationToken);
          await oprot.WriteFieldEndAsync(cancellationToken);
        }
        if((ProgrammingLanguage != null) && __isset.programmingLanguage)
        {
          tmp195.Name = "programmingLanguage";
          tmp195.Type = TType.String;
          tmp195.ID = 2;
          await oprot.WriteFieldBeginAsync(tmp195, cancellationToken);
          await oprot.WriteStringAsync(ProgrammingLanguage, cancellationToken);
          await oprot.WriteFieldEndAsync(cancellationToken);
        }
        if((JobType != null) && __isset.jobType)
        {
          tmp195.Name = "jobType";
          tmp195.Type = TType.String;
          tmp195.ID = 3;
          await oprot.WriteFieldBeginAsync(tmp195, cancellationToken);
          await oprot.WriteStringAsync(JobType, cancellationToken);
          await oprot.WriteFieldEndAsync(cancellationToken);
        }
        if(__isset.editable)
        {
          tmp195.Name = "editable";
          tmp195.Type = TType.Bool;
          tmp195.ID = 4;
          await oprot.WriteFieldBeginAsync(tmp195, cancellationToken);
          await oprot.WriteBoolAsync(Editable, cancellationToken);
          await oprot.WriteFieldEndAsync(cancellationToken);
        }
        if(__isset.timestamp)
        {
          tmp195.Name = "timestamp";
          tmp195.Type = TType.I64;
          tmp195.ID = 5;
          await oprot.WriteFieldBeginAsync(tmp195, cancellationToken);
          await oprot.WriteI64Async(Timestamp, cancellationToken);
          await oprot.WriteFieldEndAsync(cancellationToken);
        }
        if((Datetime != null) && __isset.datetime)
        {
          tmp195.Name = "datetime";
          tmp195.Type = TType.String;
          tmp195.ID = 6;
          await oprot.WriteFieldBeginAsync(tmp195, cancellationToken);
          await oprot.WriteStringAsync(Datetime, cancellationToken);
          await oprot.WriteFieldEndAsync(cancellationToken);
        }
        if((Comment != null) && __isset.comment)
        {
          tmp195.Name = "comment";
          tmp195.Type = TType.String;
          tmp195.ID = 7;
          await oprot.WriteFieldBeginAsync(tmp195, cancellationToken);
          await oprot.WriteStringAsync(Comment, cancellationToken);
          await oprot.WriteFieldEndAsync(cancellationToken);
        }
        if((Frame != null) && __isset.frame)
        {
          tmp195.Name = "frame";
          tmp195.Type = TType.Struct;
          tmp195.ID = 8;
          await oprot.WriteFieldBeginAsync(tmp195, cancellationToken);
          await Frame.WriteAsync(oprot, cancellationToken);
          await oprot.WriteFieldEndAsync(cancellationToken);
        }
        if((Controlling != null) && __isset.controlling)
        {
          tmp195.Name = "controlling";
          tmp195.Type = TType.Struct;
          tmp195.ID = 9;
          await oprot.WriteFieldBeginAsync(tmp195, cancellationToken);
          await Controlling.WriteAsync(oprot, cancellationToken);
          await oprot.WriteFieldEndAsync(cancellationToken);
        }
        await oprot.WriteFieldStopAsync(cancellationToken);
        await oprot.WriteStructEndAsync(cancellationToken);
      }
      finally
      {
        oprot.DecrementRecursionDepth();
      }
    }

    public override bool Equals(object that)
    {
      if (!(that is RobotJobInfo other)) return false;
      if (ReferenceEquals(this, other)) return true;
      return ((__isset.name == other.__isset.name) && ((!__isset.name) || (global::System.Object.Equals(Name, other.Name))))
        && ((__isset.programmingLanguage == other.__isset.programmingLanguage) && ((!__isset.programmingLanguage) || (global::System.Object.Equals(ProgrammingLanguage, other.ProgrammingLanguage))))
        && ((__isset.jobType == other.__isset.jobType) && ((!__isset.jobType) || (global::System.Object.Equals(JobType, other.JobType))))
        && ((__isset.editable == other.__isset.editable) && ((!__isset.editable) || (global::System.Object.Equals(Editable, other.Editable))))
        && ((__isset.timestamp == other.__isset.timestamp) && ((!__isset.timestamp) || (global::System.Object.Equals(Timestamp, other.Timestamp))))
        && ((__isset.datetime == other.__isset.datetime) && ((!__isset.datetime) || (global::System.Object.Equals(Datetime, other.Datetime))))
        && ((__isset.comment == other.__isset.comment) && ((!__isset.comment) || (global::System.Object.Equals(Comment, other.Comment))))
        && ((__isset.frame == other.__isset.frame) && ((!__isset.frame) || (global::System.Object.Equals(Frame, other.Frame))))
        && ((__isset.controlling == other.__isset.controlling) && ((!__isset.controlling) || (global::System.Object.Equals(Controlling, other.Controlling))));
    }

    public override int GetHashCode() {
      int hashcode = 157;
      unchecked {
        if((Name != null) && __isset.name)
        {
          hashcode = (hashcode * 397) + Name.GetHashCode();
        }
        if((ProgrammingLanguage != null) && __isset.programmingLanguage)
        {
          hashcode = (hashcode * 397) + ProgrammingLanguage.GetHashCode();
        }
        if((JobType != null) && __isset.jobType)
        {
          hashcode = (hashcode * 397) + JobType.GetHashCode();
        }
        if(__isset.editable)
        {
          hashcode = (hashcode * 397) + Editable.GetHashCode();
        }
        if(__isset.timestamp)
        {
          hashcode = (hashcode * 397) + Timestamp.GetHashCode();
        }
        if((Datetime != null) && __isset.datetime)
        {
          hashcode = (hashcode * 397) + Datetime.GetHashCode();
        }
        if((Comment != null) && __isset.comment)
        {
          hashcode = (hashcode * 397) + Comment.GetHashCode();
        }
        if((Frame != null) && __isset.frame)
        {
          hashcode = (hashcode * 397) + Frame.GetHashCode();
        }
        if((Controlling != null) && __isset.controlling)
        {
          hashcode = (hashcode * 397) + Controlling.GetHashCode();
        }
      }
      return hashcode;
    }

    public override string ToString()
    {
      var tmp196 = new StringBuilder("RobotJobInfo(");
      int tmp197 = 0;
      if((Name != null) && __isset.name)
      {
        if(0 < tmp197++) { tmp196.Append(", "); }
        tmp196.Append("Name: ");
        Name.ToString(tmp196);
      }
      if((ProgrammingLanguage != null) && __isset.programmingLanguage)
      {
        if(0 < tmp197++) { tmp196.Append(", "); }
        tmp196.Append("ProgrammingLanguage: ");
        ProgrammingLanguage.ToString(tmp196);
      }
      if((JobType != null) && __isset.jobType)
      {
        if(0 < tmp197++) { tmp196.Append(", "); }
        tmp196.Append("JobType: ");
        JobType.ToString(tmp196);
      }
      if(__isset.editable)
      {
        if(0 < tmp197++) { tmp196.Append(", "); }
        tmp196.Append("Editable: ");
        Editable.ToString(tmp196);
      }
      if(__isset.timestamp)
      {
        if(0 < tmp197++) { tmp196.Append(", "); }
        tmp196.Append("Timestamp: ");
        Timestamp.ToString(tmp196);
      }
      if((Datetime != null) && __isset.datetime)
      {
        if(0 < tmp197++) { tmp196.Append(", "); }
        tmp196.Append("Datetime: ");
        Datetime.ToString(tmp196);
      }
      if((Comment != null) && __isset.comment)
      {
        if(0 < tmp197++) { tmp196.Append(", "); }
        tmp196.Append("Comment: ");
        Comment.ToString(tmp196);
      }
      if((Frame != null) && __isset.frame)
      {
        if(0 < tmp197++) { tmp196.Append(", "); }
        tmp196.Append("Frame: ");
        Frame.ToString(tmp196);
      }
      if((Controlling != null) && __isset.controlling)
      {
        if(0 < tmp197++) { tmp196.Append(", "); }
        tmp196.Append("Controlling: ");
        Controlling.ToString(tmp196);
      }
      tmp196.Append(')');
      return tmp196.ToString();
    }
  }

}