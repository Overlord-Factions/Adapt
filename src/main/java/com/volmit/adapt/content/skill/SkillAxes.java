package com.volmit.adapt.content.skill;

import com.volmit.adapt.api.advancement.AdaptAdvancement;
import com.volmit.adapt.api.skill.SimpleSkill;
import com.volmit.adapt.api.world.AdaptPlayer;
import com.volmit.adapt.content.adaptation.AxesChop;
import com.volmit.adapt.content.adaptation.AxesGroundSmash;
import com.volmit.adapt.util.C;
import com.volmit.adapt.util.J;
import com.volmit.adapt.util.KList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class SkillAxes extends SimpleSkill<SkillAxes.Config> {
    public SkillAxes() {
        super("axes", "\u2725");
        setColor(C.YELLOW);
        setInterval(5251);
        setIcon(Material.GOLDEN_AXE);
        registerConfiguration(Config.class);
        registerAdaptation(new AxesGroundSmash());
        registerAdaptation(new AxesChop());
    }

    @EventHandler
    public void on(EntityDamageByEntityEvent e) {
        if(e.getDamager() instanceof Player p) {
            AdaptPlayer a = getPlayer((Player) e.getDamager());
            ItemStack hand = a.getPlayer().getInventory().getItemInMainHand();

            if(isAxe(hand)) {
                getPlayer(p).getData().addStat("axes.swings", 1);
                getPlayer(p).getData().addStat("axes.damage", e.getDamage());
                xp(a.getPlayer(), e.getEntity().getLocation(),13.26 * e.getDamage());
            }
        }
    }

    @EventHandler
    public void on(BlockBreakEvent e) {
        if(isAxe(e.getPlayer().getInventory().getItemInMainHand())) {
            double v = getValue(e.getBlock().getType());
            getPlayer(e.getPlayer()).getData().addStat("axes.blocks.broken", 1);
            getPlayer(e.getPlayer()).getData().addStat("axes.blocks.value", getValue(e.getBlock().getBlockData()));
            J.a(() -> xp(e.getPlayer(),e.getBlock().getLocation().clone().add(0.5, 0.5, 0.5), blockXP(e.getBlock(), v)));
        }
    }

    public double getValue(Material type) {
        double value = super.getValue(type) * 0.125;
        value += Math.min(9, type.getHardness());
        value += Math.min(10, type.getBlastResistance());

        if(type.name().endsWith("_LOG") || type.name().endsWith("_WOOD")) {
            value += 9.67;
        }
        if(type.name().endsWith("_PLANKS")) {
            value += 4.67;
        }
        if(type.name().endsWith("_LEAVES")) {
            value += 3.11;
        }

        return value;
    }


    @Override
    public void onTick() {

    }

    protected static class Config{}
}
