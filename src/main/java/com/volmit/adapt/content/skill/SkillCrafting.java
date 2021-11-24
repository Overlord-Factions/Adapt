package com.volmit.adapt.content.skill;

import com.volmit.adapt.api.advancement.AdaptAdvancement;
import com.volmit.adapt.api.skill.SimpleSkill;
import com.volmit.adapt.api.world.AdaptStatTracker;
import com.volmit.adapt.content.adaptation.CraftingDeconstruction;
import com.volmit.adapt.util.C;
import com.volmit.adapt.util.KList;
import eu.endercentral.crazy_advancements.AdvancementDisplay;
import eu.endercentral.crazy_advancements.AdvancementVisibility;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SkillCrafting extends SimpleSkill<SkillCrafting.Config> {
    public SkillCrafting() {
        super("crafting", "\u2756");
        setColor(C.YELLOW);
        setDescription("Crafting is its own reward");
        setInterval(3700);
        registerConfiguration(Config.class);
        setIcon(Material.STRING);
        registerAdaptation(new CraftingDeconstruction());
        registerAdvancement(AdaptAdvancement.builder()
            .icon(Material.BRICK)
            .key("challenge_craft_3k")
            .title("MacGyver Man")
            .description("Craft over 3,000 items")
            .frame(AdvancementDisplay.AdvancementFrame.CHALLENGE)
            .visibility(AdvancementVisibility.PARENT_GRANTED)
            .build());
        registerStatTracker(AdaptStatTracker.builder().advancement("challenge_craft_3k").goal(3000).stat("crafted.items").reward(4750).build());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void on(CraftItemEvent e) {
        ItemStack test = e.getRecipe().getResult().clone();
        int recipeAmount = e.getInventory().getResult().getAmount();
        switch(e.getClick()) {
            case NUMBER_KEY:
                if(e.getWhoClicked().getInventory().getItem(e.getHotbarButton()) != null) {
                    recipeAmount = 0;
                }
                break;

            case DROP:
            case CONTROL_DROP:
                ItemStack cursor = e.getCursor();
                if(!(cursor == null || cursor.getType().isAir())) {
                    recipeAmount = 0;
                }
                break;

            case SHIFT_RIGHT:
            case SHIFT_LEFT:
                if(recipeAmount == 0) {
                    break;
                }

                int maxCraftable = getMaxCraftAmount(e.getInventory());
                int capacity = fits(test, e.getView().getBottomInventory());
                if(capacity < maxCraftable) {
                    maxCraftable = ((capacity + recipeAmount - 1) / recipeAmount) * recipeAmount;
                }

                recipeAmount = maxCraftable;
                break;
            default:
        }

        if(test != null && recipeAmount > 0 && !e.isCancelled()) {

            double v = recipeAmount * getValue(test);
            getPlayer((Player) e.getWhoClicked()).getData().addStat("crafted.items", recipeAmount);
            getPlayer((Player) e.getWhoClicked()).getData().addStat("crafted.value", v);
            xp((Player) e.getWhoClicked(), v);
        }
    }

    private int fits(ItemStack stack, Inventory inv) {
        ItemStack[] contents = inv.getContents();
        int result = 0;

        for(ItemStack is : contents)
            if(is == null)
                result += stack.getMaxStackSize();
            else if(is.isSimilar(stack))
                result += Math.max(stack.getMaxStackSize() - is.getAmount(), 0);

        return result;
    }

    private int getMaxCraftAmount(CraftingInventory inv) {
        if(inv.getResult() == null) {
            return 0;
        }

        int resultCount = inv.getResult().getAmount();
        int materialCount = Integer.MAX_VALUE;

        for(ItemStack is : inv.getMatrix()) {
            if(is != null && is.getAmount() < materialCount) {
                materialCount = is.getAmount();
            }
        }

        return resultCount * materialCount;
    }

    @EventHandler
    public void on(FurnaceSmeltEvent e) {
        xp(e.getBlock().getLocation(), 24 + (getValue(e.getResult()) * 4), 16, 1000);
    }

    @Override
    public void onTick() {
        for(Player i : Bukkit.getOnlinePlayers())
        {
            checkStatTrackers(getPlayer(i));
        }
    }

    protected static class Config{}
}
