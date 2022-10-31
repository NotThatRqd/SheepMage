# SheepMage

SheepMage is a Minecraft Spigot plugin that adds the "SheepWand". Use /sheepwand [player] (*/sw*) to get a SheepWand.
Use /reloadsheepmage to reload the config.yml

In order to use SheepMage you must have [Vault](https://www.spigotmc.org/resources/vault.34315/) and an economy plugin on your server.
The only tested version of Minecraft for SheepMage is 1.19.2

# Contributing

To contribute, clone the project using git and push your changes to a new branch and make a pull request.

When opening the project in IntelliJ Idea, click Run -> Edit Configurations and add a Maven configuration or edit the existing one.
Set the run to `clean package -Dtesterver=INSERT_HERE` and replace INSERT_HERE with the directory of the plugins folder in your server.

And of course set the working directory to your project directory.

# Building jar

To build SheepMage use the run button in Intellij Idea you setup when following the contributing section or run mvn clean package
