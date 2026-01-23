package me.youhavetrouble.quickerstacker.interaction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ListTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MoveTransaction;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.meta.BlockStateModule;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import me.youhavetrouble.quickerstacker.QuickerStacker;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.ArrayList;
import java.util.Collection;

public class QuickStackToNearbyChestsInteraction extends SimpleBlockInteraction {

    public static final BuilderCodec<QuickStackToNearbyChestsInteraction> CODEC = BuilderCodec
            .builder(QuickStackToNearbyChestsInteraction.class, QuickStackToNearbyChestsInteraction::new)
            .build();

    @Override
    protected void interactWithBlock(@NonNullDecl World world, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NullableDecl ItemStack itemStack, @NonNullDecl Vector3i vector3i, @NonNullDecl CooldownHandler cooldownHandler) {
        Ref<EntityStore> ref = interactionContext.getEntity();
        Player player = ref.getStore().getComponent(ref, Player.getComponentType());
        if (player == null) return;
        PlayerRef playerRef = ref.getStore().getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) return;
        BlockPosition targetBlockPosition = interactionContext.getTargetBlock();
        if (targetBlockPosition == null) return;
        Collection<ItemContainerState> nearbyContainers = getNearbyContainers(world, ref, ref.getStore(), 10);
        if (nearbyContainers.isEmpty()) return;
        Inventory playerInventory = player.getInventory();
        if (playerInventory == null) return;
        int itemsMoved = 0;
        for (ItemContainerState containerState : nearbyContainers) {
            ItemContainer itemContainer = containerState.getItemContainer();
            ListTransaction<MoveTransaction<ItemStackTransaction>> transaction = playerInventory.getCombinedHotbarFirst().quickStackTo(itemContainer);
            for (var tr : transaction.getList()) {
                ItemStack item = tr.getAddTransaction().getQuery();
                if (item == null) continue;
                itemsMoved += item.getQuantity();
            }
        }
        if (itemsMoved <= 0) return;
        NotificationUtil.sendNotification(playerRef.getPacketHandler(), "Quick stacked "+ itemsMoved +" items to nearby containers", NotificationStyle.Success);
    }

    @Override
    protected void simulateInteractWithBlock(@NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NullableDecl ItemStack itemStack, @NonNullDecl World world, @NonNullDecl Vector3i vector3i) {

    }

    /**
     * Search for nearby item containers within the given range
     * @param world World to search in
     * @param ref
     * @param store entity store
     * @param range Range to search in
     * @return Collection of found item containers
     */
    private Collection<ItemContainerState> getNearbyContainers(World world, Ref<EntityStore> ref, Store<EntityStore> store, int range) {
        ArrayList<ItemContainerState> foundContainers = new ArrayList<>();
        TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
        if (transformComponent == null) return foundContainers;
        Vector3d position = transformComponent.getPosition();
        ComponentType<ChunkStore, ItemContainerState> stashComponentType = BlockStateModule.get().getComponentType(ItemContainerState.class);
        if (stashComponentType == null) return foundContainers;
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y < range; y++) {
                for (int z = -range; z <= range; z++) {
                    if (!QuickerStacker.canInteractWithBlock(
                            ref,
                            world,
                            (int) (position.getX() + x),
                            (int) (position.getY() + y),
                            (int) (position.getZ() + z))
                    ) continue;
                    WorldChunk chunk = world.getChunk(ChunkUtil.indexChunkFromBlock(position.getX() + x, position.getZ() + z));
                    if (chunk == null) continue;
                    Ref<ChunkStore> blockRef = chunk.getBlockComponentEntity((int) position.getX() + x, (int) position.getY() + y, (int) position.getZ() + z);
                    if (blockRef == null) continue;
                    ItemContainerState containerState = blockRef.getStore().getComponent(blockRef, stashComponentType);
                    if (containerState == null) continue;
                    foundContainers.add(containerState);
                }
            }
        }
        return foundContainers;
    }
}
