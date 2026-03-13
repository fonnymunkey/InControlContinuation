# Froggy's InControlContinuation

Froggy's InControlContinuation commissioned by Frog.

Current main changes:
- Multiple mobs are now allowed to be defined per spawn entry in potential spawns to reduce writing duplicate spawn entries.
- Potential spawns removal now allows for defining "*" to remove all entries matching the given filter.
- Added "biomereg" filter to all groups to allow for defining biomes by registry name rather than display name.
- Added "creaturetype" filter to potential spawns to allow limiting rule application to specific creature type passes.
- Creature type filter is not required however if used fixes issues with added spawn entries spawning too fast and bypassing spawn limits.
- Added "minplayerdist" and "maxplayerdist" filters to all groups to allow for defining how close or far a player has to be.
- Added general config options to allow for changing the monster and general mob minimum spawning distance from players.
- Added Nyx/Hyxcate event compat filters "harvestmoon", "starshower", "bloodmoon", "fullmoon", "redgiant", "grimeclipse", and "bluemoon".
- Fixed existing general config max player distance setting not being properly applied.
- Fixed summonaid not properly applying actions to the additional summoned mobs.
- Added missing spawn rule mob attribute multiply/add actions for "followrange_", "knockbackresistance_", "flyingspeed_", "armor_", and "armortoughness_".
- Added spawn rule actions to add damage source modifiers "sourcemodifiers" which allow for changing the damage received by a mob for specific damage sources.
- Each damage source modifier entry is defined as: "name", the name of the damage source to modify, "mult", value to multiply the damage by, "add", value to add to the damage after multiplying.
- Fix spawn actions applied through onJoin applying multiple times to the same entity.
- Reordered filter rules for better performance.