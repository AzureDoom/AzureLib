<center>
<img src="https://wsrv.nl/?url=https%3A%2F%2Fwww.bisecthosting.com%2Fimages%2FCF%2FAzureLib%2FBH_AL_header.png&n=-1" alt="logo"/>

<p align="center">
	
AzureLib represents a branch derived from Geckolib 4.x, serving as an animation engine tailored for Minecraft Mods. It boasts various features, including support for intricate 3D keyframe-driven animations, over 30 different easing functions, concurrent animation capabilities, sound and particle keyframes, event-based keyframes, and numerous other functionalities. Currently, I'll focus on maintaining and supporting AzureLib; no help will be given to Geckolib.

<br>
Are you a developer and want to use this library in your mod? Add the following to your build.gradle
</p>
</center>

```
repositories {
        // The Maven with the mods source
        maven {url 'https://libs.azuredoom.com:4443/mods'}
}

dependencies {
        //Fabric or Quilt
       modImplementation "mod.azure.azurelib:azurelib-fabric-MCVERSION:MODVERSION"

        //Forge
       implementation fg.deobf("mod.azure.azurelib:azurelib-forge-MCVERSION:MODVERSION")
		
        //NeoForge
       implementation fg.deobf("mod.azure.azurelib:azurelib-neo-MCVERSION:MODVERSION")
}
```

<center>

<h1 style="font-size:10vw" align="center">Wiki</h1>
<p align="center">
You can find the AzureLib Wiki here: https://wiki.azuredoom.com/
</p>

<h1 style="font-size:10vw" align="center">License</h1>
<p align="center">
<img src="https://img.shields.io/github/license/AzureDoom/AzureLib?style=for-the-badge" alt="logo" height="70" /> 
</p>

</center>
