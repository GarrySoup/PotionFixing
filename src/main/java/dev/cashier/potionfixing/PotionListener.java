package dev.cashier.potionfixing;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import static dev.cashier.potionfixing.PotionFixing.choices;
import static dev.cashier.potionfixing.PotionFixing.presets;

public class PotionListener implements Listener {
    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        if (projectile instanceof ThrownPotion) {
            ThrownPotion potion = (ThrownPotion) projectile;
            ProjectileSource source = potion.getShooter();
            if (source instanceof Player) {
                Player player = (Player) source;
                if (choices.containsKey(player)) {
                    String preset = choices.get(player);
                    PotionPreset potionPreset = presets.get(preset);
                    Vector velocity = potion.getVelocity();
                    velocity.setX(velocity.getX() * potionPreset.throwMultiplier + potionPreset.offset);
                    velocity.setY(velocity.getY() * potionPreset.fallMultiplier);
                    velocity.setZ(velocity.getZ() * potionPreset.throwMultiplier + potionPreset.offset);
                    potion.setVelocity(velocity);
                }
            }
        }
    }
}
