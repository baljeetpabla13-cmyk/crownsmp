# CrownSMP

A Minecraft SMP plugin for Paper 1.21 built around legendary Crowns.

## Building

Push to the `main` branch — GitHub Actions will automatically build the JAR.  
Download it from the **Actions** tab → latest run → **Artifacts → CrownSMP**.

## File Structure

```
CrownSMP/
├── pom.xml
├── .github/
│   └── workflows/
│       └── build.yml
└── src/main/
    ├── resources/
    │   ├── plugin.yml
    │   └── config.yml
    └── java/com/crownSMP/
        ├── CrownSMPPlugin.java
        ├── commands/
        │   └── CrownCommand.java
        ├── listeners/
        │   ├── CrownEquipListener.java
        │   ├── CrownPassiveListener.java
        │   ├── CrownAbilityListener.java
        │   ├── MiningListener.java
        │   ├── LivesListener.java
        │   └── RevivalListener.java
        ├── managers/
        │   ├── CrownManager.java
        │   ├── LivesManager.java
        │   └── RevivalManager.java
        ├── models/
        │   └── CrownType.java
        └── utils/
            ├── CooldownManager.java
            └── CrownItemUtil.java
```

## Commands

| Command | Description |
|---|---|
| `/crown give <player> <crown>` | Give a Crown |
| `/crown remove <player>` | Remove Crown from player |
| `/crown lives <player> [set\|add\|remove] [n]` | Manage lives |
| `/crown revive <player>` | Join/start a revival ritual |
| `/crown scatter [crown]` | Scatter one or all Crowns |
| `/crown reload` | Reload config |

## Crown Abilities (Sneak + Right-click)

| Crown | Passive | Ability |
|---|---|---|
| Tides | Water Breathing | Water Knockback |
| Inferno | Fire Resistance | Auto-Smelt + Fire Strike |
| Storms | Speed | Lightning Strike + Electric Arrow |
| Titans | +4 Hearts, KB Resist | Vein Mine + Earth Wall |
| Shadows | Invisibility | Shadow Teleport + Stealth |
| Frost | — | Freeze + Slow |
| Blood | Lifesteal | Heal on Kill |
| Void | All minor passives | Void Teleport |
