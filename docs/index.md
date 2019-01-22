# Smart Pendant SDK Development

These pages describe the SDK for developing extensions for use on the Yaskawa [Smart Pendant](https://www.motoman.com/products/controllers/smartpendant) - a programming pendant for [Yaskawa Robots](https://www.motoman.com/industrial-robots).

## Overview

The Smart Series robots by Yaskawa are shipped with a touch screen programming pendant - the [Smart Pendant]((https://www.motoman.com/products/controllers/smartpendant)).  The pendant hardware consists of an ARMhf embedded computer that runs the pendant UI independently from the real-time robot control system running on the controller (e.g. [YRC1000micro](https://www.motoman.com/products/controllers/yrc1000micro) ).  In addition to the Yaskawa developed programming interface, this SDK allows developers to create *extensions* for the Smart Pendant that run on the embedded computer in a Debian 9 isolated Linux container.  Extensions can be implemented as stand-alone Linux executables that interact with the standard pendant UI and the robot controller via specified APIs.

The standard extension execution enviroment also provides OpenJDK (and later .NET 4.6) so that extensions can be developed as cross-architeture (CPU) executables so that may be interchanges with future verisons of the Smart Pendant platform running on other archiectures.  The extension API is network aware, so during development it is possible to run extension executables on a desktop computer interacting with the pendant via the network, or with a simulated pendant desktop application.


[//]: # (Here is [another page]({{ site.baseurl }}{% link anotherpage.md %})  )
