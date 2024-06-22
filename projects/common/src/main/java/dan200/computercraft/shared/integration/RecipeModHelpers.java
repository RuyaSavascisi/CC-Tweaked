// SPDX-FileCopyrightText: 2022 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package dan200.computercraft.shared.integration;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.pocket.IPocketUpgrade;
import dan200.computercraft.api.turtle.ITurtleUpgrade;
import dan200.computercraft.api.upgrades.UpgradeData;
import dan200.computercraft.shared.ModRegistry;
import dan200.computercraft.shared.pocket.items.PocketComputerItem;
import dan200.computercraft.shared.turtle.items.TurtleItem;
import dan200.computercraft.shared.util.DataComponentUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Utilities for recipe mod plugins (such as JEI).
 */
public final class RecipeModHelpers {
    static final List<Supplier<TurtleItem>> TURTLES = List.of(ModRegistry.Items.TURTLE_NORMAL, ModRegistry.Items.TURTLE_ADVANCED);
    static final List<Supplier<PocketComputerItem>> POCKET_COMPUTERS = List.of(ModRegistry.Items.POCKET_COMPUTER_NORMAL, ModRegistry.Items.POCKET_COMPUTER_ADVANCED);

    private RecipeModHelpers() {
    }

    /**
     * Determine if a recipe should be hidden. This should be used in conjunction with {@link UpgradeRecipeGenerator}
     * to hide our upgrade crafting recipes.
     *
     * @param id The recipe ID.
     * @return Whether it should be removed.
     */
    public static boolean shouldRemoveRecipe(ResourceLocation id) {
        if (!id.getNamespace().equals(ComputerCraftAPI.MOD_ID)) return false;

        var path = id.getPath();
        return path.startsWith("turtle_normal/") || path.startsWith("turtle_advanced/")
            || path.startsWith("pocket_normal/") || path.startsWith("pocket_advanced/");
    }

    /**
     * Get additional ComputerCraft-related items which may not be visible in a creative tab. This includes upgraded
     * turtle and pocket computers for each upgrade.
     *
     * @param registries The currently available registries.
     * @return The additional stacks to show.
     */
    public static List<ItemStack> getExtraStacks(HolderLookup.Provider registries) {
        List<ItemStack> upgradeItems = new ArrayList<>();
        for (var turtleSupplier : TURTLES) {
            var turtle = turtleSupplier.get();
            forEachRegistry(registries, ITurtleUpgrade.REGISTRY, upgrade ->
                upgradeItems.add(DataComponentUtil.createStack(turtle, ModRegistry.DataComponents.RIGHT_TURTLE_UPGRADE.get(), UpgradeData.ofDefault(upgrade)))
            );
        }

        for (var pocketSupplier : POCKET_COMPUTERS) {
            var pocket = pocketSupplier.get();
            forEachRegistry(registries, IPocketUpgrade.REGISTRY, upgrade ->
                upgradeItems.add(DataComponentUtil.createStack(pocket, ModRegistry.DataComponents.POCKET_UPGRADE.get(), UpgradeData.ofDefault(upgrade)))
            );
        }

        return upgradeItems;
    }

    static <T> void forEachRegistry(HolderLookup.Provider registries, ResourceKey<Registry<T>> registry, Consumer<Holder.Reference<T>> consumer) {
        registries.lookup(registry).map(HolderLookup::listElements).orElse(Stream.empty()).forEach(consumer);
    }
}
