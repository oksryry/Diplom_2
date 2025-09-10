package utils;
import org.aeonbits.owner.Config;

@Config.Sources("classpath:config.properties")
public interface AppConfig extends Config {
    @Key("baseUrl")
    String baseUrl();
}
