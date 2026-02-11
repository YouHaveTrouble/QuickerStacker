package me.youhavetrouble.quickerstacker.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

import java.util.*;

public class QuickerStackerConfig {

    private String[] quickStackContainers;
    private Set<String> quickStackContainerSet;

    public static final BuilderCodec<QuickerStackerConfig> CODEC = BuilderCodec.builder(QuickerStackerConfig.class, QuickerStackerConfig::new)
            .append(new KeyedCodec<>("quickStackContainers", Codec.STRING_ARRAY),
                    (config, value) -> {
                        config.quickStackContainers = value;
                        config.quickStackContainerSet = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(config.quickStackContainers)));
                    },
                    (config) -> config.quickStackContainers).add()
            .build();

    public QuickerStackerConfig() {}

    public Set<String> getQuickStackContainers() {
        return quickStackContainerSet;
    }

}
