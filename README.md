# ORTHOGONAL-ISOMETRIC CAMERA MOD
## For Minecraft Forge 1.7.10
Want to -
* Make a prettier picture of the base? 
* Get instant real-world nausea? 
* Pretend this is isometric RPGs? 
* Pretend your Minecraft is two-dimensional? 

Well aren't you lucky, huh.

This mod adds an isometric camera mode to fullfill all these needs and beyond. Billions will be satisfied, trillions will be fed subpar quality camera work.
Really, this mod's been greatly due for 1.7.10.

## How to use
Using numpad for now. See Controls within Minecraft to check which buttons to use. If you lack one, remap the keys to the toolbar numbers keys in Controls settings.
Modifier keys are used like CTRL. I think CTRL + Enable binds the camera angle to the player's view, which is nauseating, but powerful.

## Authors
See the list of contributors, `mcmod.info` and `CREDITS`. This is mostly MellowArpeggiation's work for now though.
This mod uses the template provided by GregTech developers, https://github.com/GTNewHorizons/ExampleMod1.7.10.

## Building
### Linux, perhaps FreeBSD with macOS, correct me on that
Execute `./gradlew build` in the mod's directory. The resulting `.jar`s' gotta be in `/build/libs/`.
The mod's template is reliant on RetroFuturaGradle, which has strict legacy purging policy of 2 years. If the mod refuses to build for that reason, amp the RFG dependency `com.gtnewhorizons.gtnhsettingsconvention` in `settings.gradle.kts` to the newest available one, and run `./gradlew updateBuildScript`. Try asking around on GregTech New Horizons what to do if more issues related to the template arise.
### Windows
Execute `gradlew.bat` instead of `./gradlew`. Same instructions apply as above.
