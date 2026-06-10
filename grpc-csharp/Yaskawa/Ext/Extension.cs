using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using Google.Protobuf;
using Grpc.Net.Client;
using Guis.V1;
using GVersion = Guis.V1.Version;

namespace Yaskawa.Ext
{
    public class Extension : IDisposable
    {
        private static readonly string[] LogLevelNames = { "DEBUG", "INFO", "WARN", "CRITICAL" };

        private readonly bool _ownsChannel;
        private readonly GrpcChannel _channel;
        private bool _disposed;
        private readonly Dictionary<long, Pendant> _pendantMap = new();
        private readonly List<Action<LoggingEvent>> _loggingConsumers = new();

        protected long id;
        protected Guis.V1.Extension.ExtensionClient client;

        public object SyncRoot { get; } = new object();
        public bool copyLoggingToStdOutput { get; set; }
        public long Id => id;
        public GrpcChannel Channel => _channel;

        public Extension(string canonicalName, GVersion version, string vendor, ISet<string> supportedLanguages,
                         string hostname, int port)
            : this(CreateAddress(hostname, port, out var launchKey, out _),
                   canonicalName, launchKey, version, vendor, supportedLanguages)
        {
        }

        public Extension(string address, string canonicalName, string launchKey, GVersion version,
                         string vendor, IEnumerable<string> supportedLanguages)
        {
            _channel = GrpcChannel.ForAddress(address);
            _ownsChannel = true;
            client = new Guis.V1.Extension.ExtensionClient(_channel);
            Register(canonicalName, launchKey, version, vendor, supportedLanguages);
        }

        public Extension(GrpcChannel channel, string canonicalName, string launchKey, GVersion version,
                         string vendor, IEnumerable<string> supportedLanguages)
        {
            _channel = channel ?? throw new ArgumentNullException(nameof(channel));
            _ownsChannel = false;
            client = new Guis.V1.Extension.ExtensionClient(_channel);
            Register(canonicalName, launchKey, version, vendor, supportedLanguages);
        }

        private void Register(string canonicalName, string launchKey, GVersion version,
                              string vendor, IEnumerable<string> supportedLanguages)
        {
            var request = new RegisterExtensionRequest
            {
                CanonicalName = canonicalName ?? string.Empty,
                LaunchKey = launchKey ?? string.Empty,
                Version = version,
                Vendor = vendor ?? string.Empty,
            };
            if (supportedLanguages != null)
                request.SupportedLanguages.Add(supportedLanguages);

            lock (SyncRoot)
            {
                id = client.RegisterExtension(request).Eid;
            }
            if (id == 0)
                throw new Exception("Extension registration failed.");
        }

        private static string CreateAddress(string hostname, int port, out string launchKey, out bool runningInPendantContainer)
        {
            runningInPendantContainer = false;
            launchKey = string.Empty;
            try
            {
                const string launchKeyFilePath = "/extensionService/launchKey";
                if (File.Exists(launchKeyFilePath))
                {
                    launchKey = File.ReadAllText(launchKeyFilePath);
                    runningInPendantContainer = true;
                }
            }
            catch
            {
            }

            if (runningInPendantContainer)
            {
                hostname = "127.0.0.1";
                port = 50051;
            }
            else
            {
                if (string.IsNullOrWhiteSpace(hostname) || hostname == "localhost")
                    hostname = "127.0.0.1";
                if (port <= 0)
                    port = 50051;
            }

            return $"http://{hostname}:{port}";
        }

        public void Dispose()
        {
            Dispose(true);
            GC.SuppressFinalize(this);
        }

        protected virtual void Dispose(bool disposing)
        {
            if (_disposed) return;
            if (disposing)
            {
                try
                {
                    if (id > 0)
                    {
                        lock (SyncRoot)
                            client.UnregisterExtension(new CommonExtensionRequest { Eid = id });
                    }
                }
                catch
                {
                }

                if (_ownsChannel)
                    _channel.Dispose();
            }
            _disposed = true;
        }

        public GVersion apiVersion()
        {
            lock (SyncRoot)
                return client.ApiVersion(new Empty());
        }

        public void ping()
        {
            lock (SyncRoot)
                client.Ping(new CommonExtensionRequest { Eid = id });
        }

        public long controllerId()
        {
            lock (SyncRoot)
                return client.Controller(new CommonExtensionRequest { Eid = id }).Cid;
        }

        public Pendant pendant()
        {
            lock (SyncRoot)
            {
                var pid = client.Pendant(new CommonExtensionRequest { Eid = id }).Pid;
                if (!_pendantMap.TryGetValue(pid, out var pendant))
                {
                    pendant = new Pendant(this, new Guis.V1.Pendant.PendantClient(_channel), pid);
                    _pendantMap[pid] = pendant;
                }
                return pendant;
            }
        }

        public void log(LoggingLevel level, string message)
        {
            lock (SyncRoot)
                client.Log(new LogRequest { Eid = id, Level = level, Message = message ?? string.Empty });
            if (copyLoggingToStdOutput)
                Console.WriteLine($"{LogLevelNames[Math.Min((int)level, LogLevelNames.Length - 1)]}: {message}");
        }

        public void subscribeLoggingEvents()
        {
            lock (SyncRoot)
                client.SubscribeLoggingEvents(new CommonExtensionRequest { Eid = id });
        }

        public void unsubscribeLoggingEvents()
        {
            lock (SyncRoot)
                client.UnsubscribeLoggingEvents(new CommonExtensionRequest { Eid = id });
        }

        public List<LoggingEvent> logEvents()
        {
            lock (SyncRoot)
                return client.LogEvents(new CommonExtensionRequest { Eid = id }).Events.ToList();
        }

        public List<StorageInfo> listAvailableStorage()
        {
            lock (SyncRoot)
                return client.ListAvailableStorage(new CommonExtensionRequest { Eid = id }).Storages.ToList();
        }

        public List<string> listFiles(string path)
        {
            lock (SyncRoot)
                return client.ListFiles(new ListFilesRequest { Eid = id, Path = path ?? string.Empty }).Files.ToList();
        }

        public long openFile(string path, string flag)
        {
            lock (SyncRoot)
                return client.OpenFile(new OpenFileRequest { Eid = id, Path = path ?? string.Empty, Flags = flag ?? string.Empty }).Fid;
        }

        public void closeFile(long filehandle)
        {
            lock (SyncRoot)
                client.CloseFile(new CloseFileRequest { Eid = id, Fid = filehandle });
        }

        public bool isOpen(long filehandle)
        {
            lock (SyncRoot)
                return client.IsOpen(new CheckOpenRequest { Eid = id, Fid = filehandle }).Value;
        }

        public string read(long filehandle)
        {
            lock (SyncRoot)
                return client.Read(new ReadRequest { Eid = id, Fid = filehandle }).Value;
        }

        public string readChunk(long filehandle, long offset, long len)
        {
            lock (SyncRoot)
                return client.ReadChunk(new ReadChunkRequest { Eid = id, Fid = filehandle, Offset = offset, Len = len }).Value;
        }

        public void write(long filehandle, string data)
        {
            lock (SyncRoot)
                client.Write(new WriteRequest { Eid = id, Fid = filehandle, Data = data ?? string.Empty });
        }

        public void flush(long filehandle)
        {
            lock (SyncRoot)
                client.Flush(new FlushRequest { Eid = id, Fid = filehandle });
        }

        public string publicKey()
        {
            lock (SyncRoot)
                return client.PublicKey(new CommonExtensionRequest { Eid = id }).Value;
        }

        public string oneTimeAuthToken(string oneTimeSalt, byte[] publicKey)
        {
            lock (SyncRoot)
            {
                return client.OneTimeAuthToken(new OneTimeAuthRequest
                {
                    Eid = id,
                    OneTimeSalt = oneTimeSalt ?? string.Empty,
                    PublicKey = ByteString.CopyFrom(publicKey ?? Array.Empty<byte>())
                }).Value;
            }
        }

        public InstallPackageResponse installPackage(string authToken, byte[] packageData, string overridePasscodeEnc = "")
        {
            lock (SyncRoot)
            {
                return client.InstallPackage(new InstallPackageRequest
                {
                    Eid = id,
                    AuthToken = authToken ?? string.Empty,
                    PackageData = ByteString.CopyFrom(packageData ?? Array.Empty<byte>()),
                    OverridePasscodeEnc = overridePasscodeEnc ?? string.Empty,
                });
            }
        }

        public void debug(string message) => log(LoggingLevel.Debug, message);
        public void info(string message) => log(LoggingLevel.Info, message);
        public void warn(string message) => log(LoggingLevel.Warn, message);
        public void critical(string message) => log(LoggingLevel.Critical, message);

        public delegate bool BooleanSupplier();

        public void addLoggingConsumer(Action<LoggingEvent> c)
        {
            if (c == null) return;
            _loggingConsumers.Add(c);
        }

        public void run(BooleanSupplier stopWhen)
        {
            while (stopWhen == null || !stopWhen())
            {
                foreach (var e in logEvents())
                {
                    foreach (var consumer in _loggingConsumers)
                        consumer.Invoke(e);
                }
                var p = this.pendant();
                foreach (var e in p.events())
                    p.handleEvent(e);
                System.Threading.Thread.Sleep(50);
            }
        }

        public static Any toAny(object? o)
        {
            if (o == null)
                return new Any();

            switch (o)
            {
                case Any a:
                    return a;
                case bool b:
                    return new Any { BValue = b };
                case int i:
                    return new Any { IValue = i };
                case long l:
                    return new Any { IValue = l };
                case double d:
                    return new Any { RValue = d };
                case float f:
                    return new Any { RValue = f };
                case string s:
                    return new Any { SValue = s };
                case Vector v:
                    return new Any { VValue = v };
                case Position p:
                    return new Any { PValue = p };                
                case IDictionary dict:
                {
                    var map = new AnyMap();
                    foreach (DictionaryEntry entry in dict)
                        map.Items[Convert.ToString(entry.Key) ?? string.Empty] = toAny(entry.Value);
                    return new Any { MValue = map };
                }
                case IEnumerable enumerable when o is not string:
                {
                    var list = new AnyList();
                    foreach (var e in enumerable)
                        list.Items.Add(toAny(e));
                    return new Any { AValue = list };
                }
                default:
                    throw new ArgumentException($"Unsupported type for Any conversion: {o.GetType().FullName}");
            }
        }
    }
}
