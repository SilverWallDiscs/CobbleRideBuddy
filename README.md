# CobbleRideBuddy

**Give your Pokémon a place on your back — and let them carry you through the sky.**

CobbleRideBuddy is a Fabric addon for Cobblemon 1.21.1. Its first proof of concept lets a sent-out Crobat cling to the player's back and turns the player's fall into vanilla Elytra-style gliding.

## First milestone

- Adds an **Attach to Back** action to Cobblemon's own Pokémon interaction wheel (Shift + right-click a sent-out Pokémon).
- Crobat is the first supported species.
- Uses Minecraft's native fall-flying movement rather than a separate, incompatible mount controller.
- Automatically switches to a rear third-person camera while gliding and restores the previous perspective after landing.
- Press **H** to release the buddy.
- Server validates ownership, distance, battle state, species, and attachment state.
- No additional radial-menu mod is required.

## Compatibility target

- Minecraft 1.21.1
- Fabric Loader
- Cobblemon 1.7.3 (the Cobbleverse 1.7.x baseline)
- Fabric API

The project compiles against Cobblemon 1.7.3. Its interaction-wheel integration uses Cobblemon's POKEMON_INTERACTION_GUI_CREATION event, so the addon extends the existing Pokémon wheel instead of replacing it or adding a second radial UI.

## How to use

1. Install Cobblemon, Fabric API, Fabric Language Kotlin, and CobbleRideBuddy on the client and server.
2. Send out a Crobat that belongs to you.
3. Shift + right-click Crobat and choose **Attach to Back** in the Cobblemon interaction wheel.
4. Walk off a ledge or jump and begin falling. Crobat will deploy and you will enter Elytra-style flight.
5. Press **H** to release Crobat.

## Build

Use Java 21 and run:

```bash
./gradlew build
```

The distributable JAR is written to build/libs/.

GitHub Actions builds the mod on every push and pull request, runs Gradle verification, and uploads the JAR as a workflow artifact. Pushing a v* tag also creates a GitHub Release with the built JAR.

## Development status

This is an early, Crobat-only prototype. The first release focuses on the gameplay and integration boundary. More small flying Pokémon, per-species attachment offsets, stamina, advanced camera motion, and configuration UI can be added after in-game validation.

## License

MIT. Cobblemon and Minecraft remain separate projects and retain their own licenses.
