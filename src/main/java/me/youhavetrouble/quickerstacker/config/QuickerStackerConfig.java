package me.youhavetrouble.quickerstacker.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import java.util.*;

public class QuickerStackerConfig {

    private String[] quickStackInteractions = {
            "Open_Container",
            "Open_SimpleStorage",
    };
    private Set<String> quickStackInteractionSet = Set.of(quickStackInteractions);

    public static final BuilderCodec<QuickerStackerConfig> CODEC = BuilderCodec.builder(QuickerStackerConfig.class, QuickerStackerConfig::new)
            .append(new KeyedCodec<>("QuickStackInteractions", Codec.STRING_ARRAY),
                    (config, value) -> {
                        config.quickStackInteractions = value;
                        config.quickStackInteractionSet = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(config.quickStackInteractions)));
                    },
                    (config) -> config.quickStackInteractions)
            .documentation("List of interaction IDs that when detected on a block will cause QuickerStacker to attempt to add its own right click quick stack interaction. This only takes `Use` interaction type.")
            .add()
            .build();

    public QuickerStackerConfig() {}

    public Set<String> getQuickStackContainers() {
        return quickStackInteractionSet;
    }

}
