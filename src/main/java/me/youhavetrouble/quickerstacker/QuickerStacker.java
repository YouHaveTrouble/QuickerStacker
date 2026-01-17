package me.youhavetrouble.quickerstacker;


import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import me.youhavetrouble.quickerstacker.system.QuickStackToChestSystem;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class QuickerStacker extends JavaPlugin {

    public QuickerStacker(@NonNullDecl JavaPluginInit init) {
        super(init);
    }

    @Override
    public void start() {
        this.getEntityStoreRegistry().registerSystem(new QuickStackToChestSystem());
    }

}
