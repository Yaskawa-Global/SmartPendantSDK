// Template for DemoExtension YIP file meta-data
{
    version: 2, // file format version
    // globally unique id for package
    canonicalName: "yeu.test-extension",
    vendor: "YEU",
    packageVersion: { v: "1.0.0" },
    type: "extension",
    supportedLanguages: ["en", "ja"],
    displayNames: {
        "en": "Test Extension",
        "ja": "デモ 拡張"
    },
    descriptions: {
        "en": "Test Csharp Extension"
    },
    authorName: "Yaskawa Europa",
    contactEmail: "frank.christis@yaskawa.eu.com",
    includesArchive: true,
    installationOverridePasscode: "123456",
    metadata: {
       // when developing & testing, it is useful to be able to reinstall
       //  the same version repeatedly over itself.
       //  Leave false for production release
        allow_reinstall: 'true'
    },
    requireControllerConnected: false,
    suggestControllerBackup: false,
    components: [
        {
            version: 1,
            // globally unique id for extension - must match csharp Extension() constructor
            canonicalName: "yeu.test-extension.ext",
            type: "extension",
            // componentVersion: - omit to inherit package version
            description: "Test Extension",
            modifiesController: false,
            skipWithoutController: false,
            extension: {
                version: 1,
                requireAPIVersion: { v:"1.0.0" },
                supportedLanguages: ["en", "ja"],
                displayNames: {
                    "en": "Test Extension",
                    "ja": "デモ 拡張"
                },
                //requiredPlatform: "armhf:linux",
                requiredPlatform: "any",
                requiredRuntime: "netcore22",
                requireNetworking: true,
                keepFilesOnUpdate: true,
                extFolder: '.',
                executableFile: "/dotnet/dotnet YaskawaTestExtension.dll",
                configuration: {}
            }
        }
    ]
}
