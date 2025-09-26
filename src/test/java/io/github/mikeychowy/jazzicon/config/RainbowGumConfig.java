package io.github.mikeychowy.jazzicon.config;

import io.avaje.spi.ServiceProvider;
import io.jstach.rainbowgum.LogConfig;
import io.jstach.rainbowgum.RainbowGum;
import io.jstach.rainbowgum.pattern.format.PatternEncoderBuilder;
import io.jstach.rainbowgum.spi.RainbowGumServiceProvider.RainbowGumProvider;
import java.util.Optional;
import org.jspecify.annotations.NonNull;

@ServiceProvider(value = RainbowGumProvider.class)
public class RainbowGumConfig implements RainbowGumProvider {
    @NonNull
    @Override
    public Optional<RainbowGum> provide(@NonNull LogConfig config) {
        return RainbowGum.builder(config) //
                .route(r -> {
                    r.level(System.Logger.Level.DEBUG);
                    r.appender(
                            "console", a -> {
                                a.encoder(new PatternEncoderBuilder("console")
                                        // We use the pattern encoder which follows logback pattern syntax.
                                        .pattern(
                                                "%cyan([%d{yyyy-MM-dd HH:mm:ss.SSS}]) %highlight([%thread]) %magenta(%-5level) %green(%logger{36}:%L) - %msg")
                                        // We use properties to override the above pattern if set.
                                        .fromProperties(config.properties())
                                        .build());
                            });
                })
                .optional();
    }
}
