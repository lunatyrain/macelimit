package dev.macelimit;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class MaceLimitPlugin extends JavaPlugin implements Listener, TabExecutor {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        var cmd = getCommand("macelimit");
        if (cmd != null) {
            cmd.setExecutor(this);
            cmd.setTabCompleter(this);
        }
    }

    /** Caps the damage dealt with any mace (any enchantments, players or mobs). */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMaceHit(EntityDamageByEntityEvent e) {
        double limit = getConfig().getDouble("mace-damage-limit", 0);
        if (limit <= 0) return;                                   // 0 = no limit
        if (!(e.getDamager() instanceof LivingEntity attacker)) return;
        var eq = attacker.getEquipment();
        if (eq == null || eq.getItemInMainHand().getType() != Material.MACE) return;
        double finalDamage = e.getFinalDamage();
        if (finalDamage > limit) e.setDamage(e.getDamage() * (limit / finalDamage));
    }

    private static String fmt(double dmg) {
        String num = dmg == Math.floor(dmg) ? String.valueOf((long) dmg) : String.valueOf(dmg);
        return num + " damage (" + (dmg / 2) + " hearts)";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        double cur = getConfig().getDouble("mace-damage-limit", 0);
        if (args.length < 1) {
            sender.sendMessage(Component.text("Mace damage limit: " + (cur <= 0 ? "off" : fmt(cur))
                    + ". Use /" + label + " <damage|off> (2 damage = 1 heart).", NamedTextColor.YELLOW));
            return true;
        }
        double val;
        if (args[0].equalsIgnoreCase("off")) {
            val = 0;
        } else {
            try {
                val = Double.parseDouble(args[0]);
            } catch (NumberFormatException ex) {
                val = -1;
            }
            if (val <= 0 || Double.isNaN(val) || Double.isInfinite(val)) {
                sender.sendMessage(Component.text("Invalid number. Try 20, 12.5 or off.", NamedTextColor.RED));
                return true;
            }
        }
        getConfig().set("mace-damage-limit", val);
        saveConfig();
        sender.sendMessage(Component.text(val == 0 ? "Mace damage limit turned off."
                : "Mace damage limit set to " + fmt(val) + ".", NamedTextColor.GREEN));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        return args.length == 1 ? List.of("10", "20", "30", "40", "off") : List.of();
    }
}
