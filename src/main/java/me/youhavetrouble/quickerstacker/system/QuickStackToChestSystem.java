package me.youhavetrouble.quickerstacker.system;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.UseBlockEvent;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.meta.state.ItemContainerState;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

public class QuickStackToChestSystem extends EntityEventSystem<EntityStore, UseBlockEvent.Post> {

    public QuickStackToChestSystem() {
        super(UseBlockEvent.Post.class);
    }

    @Override
    public void handle(int index, @NonNullDecl ArchetypeChunk<EntityStore> archetypeChunk, @NonNullDecl Store<EntityStore> store, @NonNullDecl CommandBuffer<EntityStore> commandBuffer, @NonNullDecl UseBlockEvent.Post event) {
        if (event.getBlockType() != BlockType.EMPTY) return;
        if (event.getInteractionType() != InteractionType.Secondary) return; // right click
        Ref<EntityStore> ref = archetypeChunk.getReferenceTo(index);
        Player player = store.getComponent(ref, Player.getComponentType());
        if (player == null) return;
        World world = player.getWorld();
        if (world == null) return;
        WorldChunk chunk = world.getChunk(ChunkUtil.indexChunkFromBlock(event.getTargetBlock().x, event.getTargetBlock().z));
        if (chunk == null) return;
        var blockState = chunk.getState(event.getTargetBlock().x, event.getTargetBlock().y, event.getTargetBlock().z);
        if (!(blockState instanceof ItemContainerState containerState)) return;
        Inventory playerInventory = player.getInventory();
        if (playerInventory == null) return;
        playerInventory.getCombinedHotbarFirst().quickStackTo(containerState.getItemContainer());
        player.sendMessage(Message.raw("Quick stacked items to chest."));

        NotificationUtil.sendNotification(player.getPlayerRef().getPacketHandler(), "Quick stacked items to chest.");
    }

    @NullableDecl
    @Override
    public Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }
}
