# ModPack Downloader [![Build Status](http://play.nincraft.com:8080/buildStatus/icon?job=Mod Pack Downloader)](http://play.nincraft.com:8080/job/Mod Pack Downloader)
A simple command line downloader for Minecraft Forge Modpacks. Also works with Curse manifest JSONs.
# Usage
Execute via command line with two arguments, the manifest json and the folder where you want your mods downloaded.

Example: java -jar modpackdownloader.jar -manifest mods.json -folder mods

This will read the mods.json and download all mods to the mods folder.

It can also be ran without any arguments and default to manifest.json for the manifest and mods for the download folder.

For additional examples check out some of our modpacks that implement this:
- [NEB3 Modpack Repository](https://github.com/Nincraft/NincraftElectricBoogaloo3TheLightAmongTheLongForgottenDarkness/tree/develop)
- [TWBB Modpack Repository](https://github.com/UndeadZeratul/ThereWillBeBlood/tree/develop)
- [TWBB2 Modpack Repository](https://github.com/UndeadZeratul/ThereWillBeBlood2/tree/develop)
