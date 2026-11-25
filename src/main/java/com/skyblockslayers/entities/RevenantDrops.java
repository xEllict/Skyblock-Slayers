package com.skyblockslayers.entities;

import com.skyblockslayers.slayer.DropEntry;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.enchantment.Enchantments;

public class RevenantDrops {
    // Each line: item id, count (or min and max), chance in percent
    private static final Map<Integer, List<DropEntry>> TABLE = Map.of(
            1, List.of(
                    DropEntry.item("revenant_flesh", 1, 2, 100)
            ),
            2, List.of(
                    DropEntry.item("revenant_flesh", 2, 5, 100),
                    DropEntry.item("foul_flesh", 1, 35),
                    DropEntry.item("undead_catalyst", 1, 10).withMinLevel(3),
                    DropEntry.item("revenant_catalyst", 1, 5).withMinLevel(5)
            ),
            3, List.of(
                    DropEntry.item("revenant_flesh", 6, 10, 100),
                    DropEntry.item("foul_flesh", 1, 50),
                    DropEntry.item("undead_catalyst", 1, 20).withMinLevel(3),
                    DropEntry.item("revenant_catalyst", 1, 10).withMinLevel(5),
                    DropEntry.enchantedBook(Enchantments.SMITE, 6, 4),
                    DropEntry.item("beheaded_horror", 1, 1.5).withMinLevel(6)
            ),
            4, List.of(
                    DropEntry.item("revenant_flesh", 12, 20, 100),
                    DropEntry.item("foul_flesh", 2, 35),
                    DropEntry.item("undead_catalyst", 1, 100.0 / 3).withMinLevel(3),
                    DropEntry.item("revenant_catalyst", 1, 100.0 / 6).withMinLevel(5),
                    DropEntry.item("revenant_viscera", 1, 15).withMinLevel(2),
                    DropEntry.enchantedBook(Enchantments.SMITE, 6, 6.25),
                    DropEntry.item("beheaded_horror", 1, 2.5).withMinLevel(6)
            )
    );

    public static List<DropEntry> getDrops(RevenantTier tier) {
        return TABLE.getOrDefault(tier.getTier(), List.of());
    }
}