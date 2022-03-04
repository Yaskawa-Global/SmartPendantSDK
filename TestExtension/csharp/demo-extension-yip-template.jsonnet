// Template for DemoExtension YIP file meta-data
{
    version: 2, // file format version
    // globally unique id for package
    canonicalName: "com.yeu.test-extension",
    vendor: "Yaskawa",
    packageVersion: { v: "1.0.0" },
    type: "extension",
    supportedLanguages: ["en"],
    displayNames: {
        "en": "Test Extension"
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
                supportedLanguages: ["en"],
                displayNames: {
                    "en": "Test Extension"
                },
                //requiredPlatform: "armhf:linux",
                requiredPlatform: "any",
                requiredRuntime: "netcoreapp.2",
                requireNetworking: true,
                keepFilesOnUpdate: true,
                extFolder: '.',
                executableFile: "YaskawaTestExtension.dll",
                configuration: {}
            }
        }
    ]
}
