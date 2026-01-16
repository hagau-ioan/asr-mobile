// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "iosApp",
    platforms: [
        .iOS(.v14)
    ],
    products: [
        .library(
            name: "iosApp",
            targets: ["iosApp"]
        )
    ],
    dependencies: [
        // Add your Swift Package dependencies here
        // Example:
        // .package(url: "https://github.com/example/package.git", from: "1.0.0")
    ],
    targets: [
        .target(
            name: "iosApp",
            dependencies: [
                // Add package dependencies here
            ],
            path: "iosApp",
            sources: ["iOSApp.swift"],
            resources: [
                .process("Info.plist")
            ]
        )
    ]
)
