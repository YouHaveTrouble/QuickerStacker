package me.youhavetrouble.quickerstacker;


import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import me.youhavetrouble.quickerstacker.interaction.QuickStackToChestInteraction;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class QuickerStacker extends JavaPlugin {

    public QuickerStacker(@NonNullDecl JavaPluginInit init) {
        super(init);
    }

    @Override
    public void setup() {
        this.getCodecRegistry(Interaction.CODEC).register("Yht_QuickerStacker_QuickStackToChest", QuickStackToChestInteraction.class, QuickStackToChestInteraction.CODEC);
    }

}
