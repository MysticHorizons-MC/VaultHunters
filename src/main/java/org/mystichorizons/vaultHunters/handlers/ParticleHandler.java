package org.mystichorizons.vaultHunters.handlers;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;

import java.util.Random;

public class ParticleHandler {
    private final ConfigHandler configHandler;
    private final Random random = new Random();

    public ParticleHandler(ConfigHandler configHandler) {
        this.configHandler = configHandler;
    }

    public void playParticles(Location location) {
        if (!configHandler.isParticlesEnabled()) {
            return;
        }

        String particleConfig = configHandler.getParticleList().isEmpty() ? null : configHandler.getParticleList().get(0);

        Particle particle;
        int amount;
        DustOptions dustOptions = null;

        if (particleConfig != null) {
            String[] parts = particleConfig.split(":");
            particle = Particle.valueOf(parts[0]);
            amount = Integer.parseInt(parts[1]);

            if (particle == Particle.DUST) {
                if (parts.length >= 3) {
                    String[] colorParts = parts[2].split(",");
                    Color color = Color.fromRGB(Integer.parseInt(colorParts[0]), Integer.parseInt(colorParts[1]), Integer.parseInt(colorParts[2]));
                    float size = parts.length >= 4 ? Float.parseFloat(parts[3]) : 1.0f;
                    dustOptions = new DustOptions(color, size);
                } else {
                    dustOptions = new DustOptions(randomColor(), 1.0f);
                }
            }
        } else {
            particle = Particle.DUST;
            amount = configHandler.getParticleLoop(); // Default to loop amount if not specified
            dustOptions = new DustOptions(randomColor(), 1.0f);
        }

        if (dustOptions != null) {
            location.getWorld().spawnParticle(particle, location, amount, dustOptions);
        } else {
            location.getWorld().spawnParticle(particle, location, amount);
        }
    }

    private Color randomColor() {
        return Color.fromRGB(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }
}
