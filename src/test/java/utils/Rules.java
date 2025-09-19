package utils;

import io.restassured.RestAssured;
import org.aeonbits.owner.ConfigFactory;
import org.junit.rules.ExternalResource;

public class Rules extends ExternalResource {

    private AppConfig appConfig;

    @Override
    protected void before() {
        appConfig = ConfigFactory.create(AppConfig.class);
        RestAssured.baseURI = appConfig.baseUrl();
    }


}
