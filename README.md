<center>
<img src="https://wsrv.nl/?url=https%3A%2F%2Fwww.bisecthosting.com%2Fimages%2FCF%2FAzureLib%2FBH_AL_header.png&n=-1" alt="logo"/>

<h3 align="center">AzureLib is a fork of Geckolib 4.x, an animation engine for Minecraft Mods, with support for complex 3D keyframe-based animations, 30+ easings, concurrent animation support, sound and particle keyframes, event keyframes, and more. I will only be maintaining this library from now on, so no help will be given in relation to Geckolib.
<br>
<br>
Are you a developer and want to use this library in your mod? Add the following to your build.gradle
</h3>
</center>


```
repositories {
        // The Maven with the mods source
        maven {url 'https://dl.cloudsmith.io/public/azuredoom-mods/azurelib/maven/'}
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
<h3 align="center">
You can find the AzureLib Wiki here: https://wiki.azuredoom.com/
</h3>

<h1 style="font-size:10vw" align="center">License</h1>
<h3 align="center">
<img src="https://img.shields.io/github/license/AzureDoom/AzureLib?style=for-the-badge" alt="logo" height="70" /> 
</h3>

<h1 style="font-size:10vw" align="center">Acknowledgments</h1>
<h3 align="center">
  
[![Hosted By: Cloudsmith](https://img.shields.io/badge/OSS%20hosting%20by-cloudsmith-blue?logo=cloudsmith&style=for-the-badge)](https://cloudsmith.com)

<b>Package repository hosting is graciously provided by  [Cloudsmith](https://cloudsmith.com).
Cloudsmith is the only fully hosted, cloud-native, universal package management solution, that
enables your organization to create, store and share packages in any format, to any place, with total
confidence.</b><br>
</h3>
</center>
