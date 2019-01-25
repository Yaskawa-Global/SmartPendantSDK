
These pages describe the SDK for developing extensions for use on the Yaskawa [Smart Pendant](https://www.motoman.com/products/controllers/smartpendant) - a programming pendant for [Yaskawa Robots](https://www.motoman.com/industrial-robots).

## Overview

The Smart Series robots by [Yaskawa](https://www.yaskawa-global.com/) are shipped with a touch screen programming pendant - the [Smart Pendant](https://www.motoman.com/products/controllers/smartpendant) (pictured above).  The pendant hardware consists of an ARMhf embedded computer that runs the pendant UI independently from the real-time robot control system running on the controller (e.g. [YRC1000micro](https://www.motoman.com/products/controllers/yrc1000micro) ).  In addition to the Yaskawa developed programming interface, this SDK allows developers to create *extensions* for the Smart Pendant that run on the embedded computer in a Debian 9 isolated Linux container.  Extensions can be implemented as stand-alone Linux executables that interact with the standard pendant UI and the robot controller via specified APIs.

The standard extension execution enviroment also provides OpenJDK (and later .NET 4.6) so that extensions can be developed as cross-architeture (CPU) executables so they interoperate with future verisons of the Smart Pendant platform running on other archiectures.  The extension API is network aware, so during development it is possible to run extension executables on a desktop computer interacting with the pendant via the network, or with a simulated pendant desktop application.  Hence, developers may use the IDE with which they're most comfortable.


### API (Application Programming Interface)

The API is divided into two major parts - the Pendant API and the Controller API.  The Pendant API contains functions related to the UI (User Interface) integration and the Controller API contains functions for interacting with the robot controller (I/O, Jobs, Variables, Motion etc.).

The API functions are specified using a language-neutral IDL (Interface Definition Language) - the [Apache Thrift](https://thrift.apache.org/) IDL.  Thrift supports the automatic generation of client code in many programming languages, including Java, C#, C++ and Python.  The SDK provides a more conveinent wrapper around the generated Thrift client code for select languages (currently only Java).


### Execution Environment

When a packaged extension is deployed to the pendant via the user installation UI, it is deployed within a Linux container (LXC) containing a base Debian 9 ARMhf Linux evironment.  If specified, the container will also have OpenJDK10 installed.  Additional deb packages may be installed within the container during installation, if supplied and specified in the extension package.

During development, packaging of an extension is not necessary.  The extension executable may be run on any desktop with network connectivity to the pendant (or simulated pendant app) in order to connect to the API server.


### Packaging

For distribution, the packaging tool supplied with the SDK can be used to combine the extension executable and supporting files (data files, images, OS install packages, INFORM robot jobs, controller motoPlus apps etc.) into a single Yaskawa Install Package (.yip).  This can be distributed to end-users, who can place it on a USB drive and insert into the pendant for installation.  Internet connected Smart Pendant apps will be able to install packages from the web in future.

## Quick Start

[Developing a simple Java extension](java-quickstart.html)

## Reference

[Extension functions]()

[Pendant functions]()

[Controller functions]()

[YML UI Markup]()

[Thrift IDL API Definition Source](https://github.com/Yaskawa-Global/SmartPendantSDK/blob/master/extension.thrift)
(including doc comments)