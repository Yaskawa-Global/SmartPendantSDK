// Template for Calculator YIP file meta-data
{
    version: 2, // file format version
    // globally unique id for package
    canonicalName: "com.yaskawa.yii.calculator",
    vendor: "Yaskawa",
    vendorIcon: "images/Yaskawa-Y-logo.png", // company logo
    packageVersion: { v: "1.0.0" },
    type: "extension",
    supportedLanguages: ["en", "ja"],
    displayNames: {
        "en": "Calculator",
        "ja": "電卓"
    },
    descriptions: {
        "en": "Simple calculator utility"
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
            canonicalName: "com.yaskawa.yii.calculator.ext",
            type: "extension",
            description: "Calculator",
            modifiesController: false,
            skipWithoutController: false,
            extension: {
                version: 2,
                requireAPIVersion: { v:"2.0.4" },
                supportedLanguages: ["en"],
                displayNames: {
                    "en": "Calculator",
                    "ja": "電卓"
                },
                iconName: "images/calculator_icon-64.png", // extension icon
                requiredPlatform: "any",
                requiredRuntime: "openjdk11",
                requireNetworking: false,
                keepFilesOnUpdate: false,
                extFolder: '.',
                executableFile: "Calculator.jar",
                configuration: {}
            }
        }
    ]
}
