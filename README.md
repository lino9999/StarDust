# StarDust 🌟

A magical Minecraft plugin that brings enchanting nighttime particle effects to every biome, creating an immersive atmospheric experience for your players.

## ✨ Features

- **20+ Unique Biome Effects**: Each biome features custom-tailored particle animations
- **8 Different Effect Types**: Falling, Spiral, Wave, Firefly, Snow, Meteor, Aurora, and Floating animations
- **Performance Optimized**: Smart queue system prevents server lag by limiting concurrent effects
- **Highly Customizable**: Configure every aspect of particles per biome
- **Player-Friendly**: Simple toggle commands let players control their experience
- **Automatic Night Detection**: Effects only appear during nighttime (13000-23000 ticks)
- **Multi-World Support**: Choose which worlds have particle effects

## 🎮 Effect Types

| Effect | Description | Best For |
|--------|-------------|----------|
| **FALLING** | Particles gently fall from the sky | Plains, general use |
| **SPIRAL** | Particles descend in beautiful spirals | Jungles, magical areas |
| **WAVE** | Particles move in wave patterns | Oceans, beaches, rivers |
| **FIREFLY** | Glowing particles with realistic firefly movement | Forests, swamps |
| **SNOW** | Gentle swaying snowfall | Snowy biomes |
| **METEOR** | Fast-moving meteor shower effects | Deserts, badlands |
| **AURORA** | Northern lights-style waves | Ice spikes, mountains |
| **FLOATING** | Particles float and drift mysteriously | Dark forests, mushroom fields |

## 🎯 Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/stardust status` | `stardust.use` | Check your particle status and queue position |
| `/stardust toggle` | `stardust.use` | Enable/disable particles for yourself |
| `/stardust reload` | `stardust.reload` | Reload the configuration file |

## ⚙️ Configuration

### Basic Settings
```yaml
spawn-interval: 5          # Ticks between particle spawns
particle-lifetime: 200     # How long particles last (in ticks)
max-players: 10           # Maximum players with active effects

enabled-worlds:
  - world
  - world_nether
  - world_the_end
```

### Biome Configuration Example
```yaml
biomes:
  FOREST:
    particle-type: VILLAGER_HAPPY  # Particle type
    fall-speed: 0.05               # Movement speed
    particle-count: 5              # Particles per spawn
    effect-type: FIREFLY           # Animation type
    spawn-radius: 30.0             # Horizontal spawn area
    spawn-height: 15.0             # Vertical spawn height
```

### Default Configuration
The plugin includes pre-configured settings for all vanilla biomes. You can customize any biome or add custom biome support.

## 🌍 Supported Particles

All Minecraft particle types are supported. Popular choices include:
- `END_ROD` - Bright white particles
- `SNOWFLAKE` - Perfect for winter biomes
- `VILLAGER_HAPPY` - Green particles for nature
- `FLAME` / `SOUL_FIRE_FLAME` - Fire effects
- `GLOW` - Soft glowing particles
- `CHERRY_LEAVES` - Pink falling leaves
- `NAUTILUS` - Underwater magic

## 🔧 Permissions

- `stardust.use` (default: true) - Access to basic commands
- `stardust.reload` (default: op) - Access to reload command

## 💡 Tips & Best Practices

1. **Performance**: Keep `max-players` reasonable (10-20) for best performance
2. **Spawn Interval**: Lower values = more particles but higher server load
3. **Particle Count**: Balance beauty with performance (3-8 recommended)
4. **Testing**: Use `/stardust toggle` to quickly test your configurations
5. **Biome Detection**: Players must be standing in the biome to see its effects

## 🐛 Troubleshooting

**Particles not appearing?**
- Check if it's nighttime (use `/time set night`)
- Verify you're in an enabled world
- Ensure you're not in the queue (`/stardust status`)
- Check if particles are toggled on

**Server lag?**
- Reduce `max-players` in config
- Increase `spawn-interval`
- Lower `particle-count` per biome

**Effects in wrong biome?**
- The plugin uses the biome at player's feet
- Some biomes may have similar names (check Minecraft Wiki)


*StarDust - Making Minecraft nights magical, one particle at a time* ✨
