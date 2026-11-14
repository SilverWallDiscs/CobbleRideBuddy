# CobbleRideBuddy

**Your Pokémon does not become a mount. It becomes your flying buddy.**

CobbleRideBuddy is a Fabric addon for Cobblemon 1.21.1. It uses Cobblemon's native shoulder-companion state to let Crobat settle on the player's back, then adds an Elytra-like glide skill with gentle, chargeable propulsion.

## Current prototype

- **Ride Buddy Skill** is added to Cobblemon's built-in Pokémon interaction wheel for your sent-out Crobat.
- Crobat is stored with Cobblemon's native `ShoulderedState` and shoulder NBT, rather than being attached as a passenger entity.
- The existing Cobblemon shoulder renderer is reused; a client mixin moves Crobat from the shoulder position to the center of the player's back.
- Crobat is specifically made shoulder-mountable. Other species retain their normal Cobblemon settings.
- Tap **Space while airborne** to start gliding. No Elytra item is required.
- Hold **W while gliding** to gradually charge propulsion. Releasing W lets power drain gradually.
- Crobat's default maximum propulsion power is **10**. The acceleration is deliberately mild.
- A small HUD bar shows current Ride Buddy power.
- Press **K** to open **Companion Skills** and release Crobat. This replaces the old one-purpose H key and provides a home for future skills.
- Server-side checks validate the selected Crobat, owner, distance, battle state, shoulder state, and glide input.

## Controls

| Input | Action |
|---|---|
| Shift + right-click your Crobat | Open Cobblemon's Pokémon radial menu |
| Ride Buddy Skill | Attach Crobat to your back |
| Space (while airborne) | Start gliding |
| W (while gliding) | Gradually build propulsion power |
| K | Open Companion Skills |
| Release Crobat (in the skill screen) | Return Crobat to your party |

## Datapack-driven Pokémon tuning

The first profile lives at:

`data/cobbleridebuddy/ride_buddy_skills/crobat.json`

It controls the supported Pokémon, skill ID, maximum power, power gain and decay, thrust per power level, and maximum horizontal speed. Add another profile and register a skill implementation to extend the system to more Pokémon and abilities.

Example Crobat tuning:

```json
{
  "pokemon": "cobblemon:crobat",
  "skill": "cobbleridebuddy:ride_buddy",
  "maxPower": 10.0,
  "powerGainPerTick": 0.04,
  "powerDecayPerTick": 0.02,
  "thrustPerPower": 0.00012,
  "maxHorizontalSpeed": 2.2
}
```

## Compatibility

- Minecraft 1.21.1
- Fabric Loader
- Fabric API
- Fabric Language Kotlin
- Cobblemon 1.7.3
- Java 21

No separate radial-menu mod is required. The skill entry extends Cobblemon's own Pokémon interaction wheel, so it coexists with Cobblemon-based modpacks such as CobbleVerse.

## Build

Use Java 21 and run:

```bash
./gradlew build
```

The distributable JAR is written to `build/libs/`.

GitHub Actions builds the mod on every push and pull request, runs Gradle verification, and uploads the JAR as a workflow artifact. Pushing a `v*` tag also creates a GitHub Release with the built JAR.

## Testing note

The build verifies compilation and packaging. A real CobbleVerse session is still needed to validate exact Crobat positioning, animation, camera feel, and the final flight tuning.

## License

MIT. Cobblemon and Minecraft remain separate projects and retain their own licenses.
