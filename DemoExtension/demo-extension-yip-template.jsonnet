// Template for DemoExtension YIP file meta-data
{
    version: 2, // file format version
    // globally unique id for package
    canonicalName: "com.yaskawa.yii.demoextension",
    vendor: "Yaskawa",
    vendorIcon: "images/Yaskawa-Y-logo.png", // company logo
    packageVersion: { v: "2.0.6" },
    type: "extension",
    supportedLanguages: ["en"],
    displayNames: {
        "en": "Demo Extension",
        "ja": "デモ 拡張"
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
    requireControllerConnected: true,
    suggestControllerBackup: false,
    components: [
        {
            version: 2,
            // globally unique id for extension - must match Java Extension() constructor
            canonicalName: "com.yaskawa.yii.demoextension.ext",
            type: "extension",
            // componentVersion: - omit to inherit package version
            description: "Demonstration Extension",
            modifiesController: false,
            skipWithoutController: false,
            extension: {
                version: 2,
                requireAPIVersion: { v:"2.0.3" },
                supportedLanguages: ["en"],
                displayNames: {
                    "en": "Demo Extension",
                    "ja": "デモ 拡張"
                },
                iconName: "images/d-icon-256.png", // extension icon
                //requiredPlatform: "armhf:linux",
                requiredPlatform: "any",
                requiredRuntime: "openjdk11",
                requireNetworking: true,
                keepFilesOnUpdate: true,
                extFolder: '.',
                executableFile: "DemoExtension.jar",
                configuration: {}
            }
        },
        {
            version: 2,
            canonicalName: "com.yaskawa.yii.demoextension.job",
            type: "jobs",
            modifiesController: true,
            skipWithoutController: true,
            conflictOption: 'query',
            conflictDefaultAction: 'skip',
            prerequisites: {
                controllerModel: 'yrc1000micro'
            },
            jobs: {
                version: 1,
                jobs: [ // list of jobs (only one here)
                    {
                        jobControllerGroup: "R1",
                        jobFileName: "OR_RG_MOVE.JBI",
                        jobFolderPath: "jobs",
                        jobLanguage: "INFORM",
                        jobName: "OR_RG_MOVE",
                        jobType: "robot"
                    }
                ]
            }
        }
    ]
}