package me.youhavetrouble.quickerstacker.interaction;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.ListTransaction;
import com.hypixel.hytale.server.core.inventory.transaction.MoveTransaction;
import com.hypixel.hytale.server.core.modules.block.components.ItemContainerBlock;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.client.SimpleBlockInteraction;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import me.youhavetrouble.quickerstacker.QuickerStacker;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class QuickStackToChestInteraction extends SimpleBlockInteraction {

    public static final BuilderCodec<QuickStackToChestInteraction> CODEC = BuilderCodec
            .builder(QuickStackToChestInteraction.class, QuickStackToChestInteraction::new)
            .build();

    @Override
    protected void interactWithBlock(@NonNullDecl World world, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NullableDecl ItemStack itemStack, @NonNullDecl Vector3i vector3i, @NonNullDecl CooldownHandler cooldownHandler) {
        Ref<EntityStore> ref = interactionContext.getEntity();
        Player player = ref.getStore().getComponent(ref, Player.getComponentType());
        if (player == null) return;
        BlockPosition targetBlockPosition = interactionContext.getTargetBlock();
        if (targetBlockPosition == null) return;
        int x = targetBlockPosition.x;
        int y = targetBlockPosition.y;
        int z = targetBlockPosition.z;
        if (!QuickerStacker.canInteractWithBlock(ref, world, x, y, z)) return;
        WorldChunk chunk = world.getChunk(ChunkUtil.indexChunkFromBlock(x, z));
        if (chunk == null) return;
        Ref<ChunkStore> blockRef = chunk.getBlockComponentEntity(x, y, z);
        if (blockRef == null) return;
        ItemContainerBlock containerBlock = blockRef.getStore().getComponent(blockRef, ItemContainerBlock.getComponentType());
        if (containerBlock == null) return;
        SimpleItemContainer container = containerBlock.getItemContainer();
        CombinedItemContainer playerInventory = InventoryComponent.getCombined(commandBuffer, ref, InventoryComponent.HOTBAR_FIRST);
        ListTransaction<MoveTransaction<ItemStackTransaction>> transaction = playerInventory.quickStackTo(container);
        if (transaction.size() <= 0) return;
        PlayerRef playerRef = ref.getStore().getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null) return;
        int itemsMoved = 0;
        for (var tr : transaction.getList()) {
            ItemStack item = tr.getAddTransaction().getQuery();
            if (item == null) continue;
            itemsMoved += item.getQuantity();
        }
        if (itemsMoved <= 0) return;
        NotificationUtil.sendNotification(playerRef.getPacketHandler(), "Quick stacked "+ itemsMoved +" items", NotificationStyle.Success);
    }

    @Override
    protected void simulateInteractWithBlock(@NonNullDecl InteractionType interactionType, @NonNullDecl InteractionContext interactionContext, @NullableDecl ItemStack itemStack, @NonNullDecl World world, @NonNullDecl Vector3i vector3i) {

    }
}
