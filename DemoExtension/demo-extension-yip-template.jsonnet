// Template for DemoExtension YIP file meta-data
{
    version: 2, // file format version
    // globally unique id for package
    canonicalName: "com.yaskawa.yii.demo-extension",
    vendor: "Yaskawa",
    vendorIcon: "images/Yaskawa-Y-logo.png", // company logo
    packageVersion: { v: "2.0.3" },
    supportedLanguages: ["en"],
    displayNames: {
        "en": "Demo Extension"
    },
    descriptions: {
        "en": "Demonstration Java Extension"
    },
    authorName: "Yaskawa Innovation Inc.",
    contactEmail: "david.jung@yaskawainnovation.com",
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
            version: 2,
            // globally unique id for extension - must match Java Extension() constructor
            canonicalName: "com.yaskawa.yii.demo-extension.ext",
            type: "extension",
            description: "Demonstration Extension",
            iconName: "images/d-icon-256.png", // extension icon
            modifiesController: false,
            skipWithoutController: false,
            extension: {
                version: 2,
                requireAPIVersion: { v:"2.0.3" },
                supportedLanguages: ["en"],
                displayNames: {
                    "en": "Demo Extension"
                },
                requiredPlatform: "armhf:linux",
                //requiredPlatform: "any",
                requiredRuntime: "openjdk11",
                requireNetworking: true,
                keepFilesOnUpdate: true,
                extFolder: '.',
                executableFile: "DemoExtension.jar",
                configuration: {}
            }
        }
    ]
}
