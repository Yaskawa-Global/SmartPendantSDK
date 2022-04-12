# SmartPendantSDK
Software Development Kit (SDK) for Smart Pendant Extensions

Please visit the [Yaskawa Motoman Developer Portal](https://developer.motoman.com) for further information.

## Branches

* **master** - this always corresponds to the API version supported in the latest Smart Pendant release.  
Note that later client libraries may be used with older Smart Pendants, just that if the extension code uses newer API functions not supported on the pendant in which it is executing, an exception will be thown.  If you create extensions that may run on against different API versions and wish to use newer functions, the code should include a conditional check against the reported API version supported during runtime.
* **VersionA_B_x** - this corresponds to the API in previous Smart Pendant releases.
* **future** - this may contain client functions that will only be present in future Smart Pendant releases or pre-releases.

