# Mod Blacklister

A Fabric mod for Minecraft 1.21.1 that requires itself on clients and blacklists specified mods via a config file.

## Features
- Requires clients to have Mod Blacklister installed to join the server.
- Checks client mod lists and blocks players with blacklisted mods.
- Blacklisted mods are configured in `config/modblacklister.json`.

## Setup
1. Install on both server and clients in the `mods` folder.
2. Configure blacklisted mods in `config/modblacklister.json` (e.g., `["examplebadmod", "anotherbadmod"]`).
3. Requires Fabric API on both client and server.

## Building
Run `./gradlew build` to generate the mod JAR in `build/libs/modblacklister-1.0.0.jar`.