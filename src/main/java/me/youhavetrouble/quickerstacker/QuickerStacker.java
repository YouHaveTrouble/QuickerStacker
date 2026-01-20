package me.youhavetrouble.quickerstacker;


import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.InteractionManager;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.BootEvent;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.modules.interaction.InteractionSimulationHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import me.youhavetrouble.quickerstacker.interaction.QuickStackInteraction;
import me.youhavetrouble.quickerstacker.interaction.QuickStackToChestInteraction;
import me.youhavetrouble.quickerstacker.interaction.QuickStackToNearbyChestsInteraction;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class QuickerStacker extends JavaPlugin {

    public QuickerStacker(@NonNullDecl JavaPluginInit init) {
        super(init);
    }

    @Override
    public void setup() {
        this.getCodecRegistry(Interaction.CODEC)
                .register(
                        "Yht_QuickerStacker_QuickStack",
                        QuickStackInteraction.class,
                        QuickStackInteraction.CODEC
                );
        this.getCodecRegistry(Interaction.CODEC)
                .register(
                        "Yht_QuickerStacker_QuickStackToChest",
                        QuickStackToChestInteraction.class,
                        QuickStackToChestInteraction.CODEC
                );
        this.getCodecRegistry(Interaction.CODEC)
                .register(
                        "Yht_QuickerStacker_QuickStackToNearbyChests",
                        QuickStackToNearbyChestsInteraction.class,
                        QuickStackToNearbyChestsInteraction.CODEC
                );

        this.getEventRegistry().registerGlobal(BootEvent.class, (event) -> {
            for (BlockType block : BlockType.getAssetMap().getAssetMap().values()) {
                if (!"Open_Container".equals(block.getInteractions().get(InteractionType.Use))) continue;
                try {
                    Field interactionsField = block.getClass().getDeclaredField("interactions");
                    interactionsField.setAccessible(true);
                    Object interactionsObj = interactionsField.get(block);
                    if (!(interactionsObj instanceof Map<?,?> interactionsMap)) continue;
                    Map<Object, Object> modifiableMap = new HashMap<>(interactionsMap);
                    modifiableMap.put(InteractionType.Secondary, "Yht_QuickerStacker_QuickStack");
                    interactionsField.set(block, modifiableMap);
                } catch (NoSuchFieldException | IllegalAccessException _) {
                    System.out.println("Error modifying interactions for block type: " + block.getId());
                    continue;
                }
                System.out.println(block.getId());
                System.out.println(block.getInteractions().get(InteractionType.Secondary));
            }
        });

    }

    /**
     * Check if the player can interact with the block at the given position
     * @param ref Player ref
     * @param world World
     * @param x x
     * @param y y
     * @param z z
     * @return true if the player can interact with the block, false otherwise
     */
    public static boolean canInteractWithBlock(Ref<EntityStore> ref, World world, int x, int y, int z){
        Player player = ref.getStore().getComponent(ref, Player.getComponentType());
        if (player == null) return false;
        PlayerRef playerRef = ref.getStore().getComponent(ref, PlayerRef.getComponentType());
        InteractionManager interactionManager = new InteractionManager(player, playerRef, new InteractionSimulationHandler());
        BlockType blockType = world.getBlockType(x, y, z);
        if (blockType == null) return false;
        var event = new UseBlockEvent.Pre(InteractionType.Use, InteractionContext.forProxyEntity(interactionManager, player, ref), new Vector3i(x, y, z), blockType);
        ref.getStore().invoke(ref, event);
        return !event.isCancelled();
    }

}
