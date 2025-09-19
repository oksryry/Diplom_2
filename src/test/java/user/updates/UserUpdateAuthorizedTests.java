package user.updates;

import entities.user.UserInfo;
import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import utils.Rules;
import com.github.javafaker.Faker;
import utils.UserRules;

import java.util.Locale;

import static org.apache.hc.core5.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

public class UserUpdateAuthorizedTests {


    public final Rules rule = new Rules();

    //создаёт и удаляет пользователя
    public final UserRules userRules = new UserRules();

    @Rule
    public final RuleChain chain = RuleChain
            .outerRule(rule)       // сначала baseURI
            .around(userRules);    // затем создание пользователя

    private Faker fakerRU = new Faker(Locale.forLanguageTag("ru"));
    private Faker faker = new Faker();

    @Test
    @Description("PATCH /api/auth/user с авторизацией: обновление имени возвращает 200 и новые данные")
    public void updateUserWithAuthUpdateName() {
        // исходные ожидания
        String expectedEmail = userRules.getUser().getEmail();
        String expectedName  = fakerRU.name().username();

        // готовим тело PATCH только с name
        UserInfo upd = new UserInfo();
        upd.setName(expectedName);

        Response patch = userRules.steps()
                .updateUserAuthorized(userRules.getAccessToken(), upd);

        patch.then()
                .statusCode(SC_OK)
                .body("success", is(true))
                .body("user.name",  equalTo(expectedName))
                .body("user.email", equalTo(expectedEmail));
    }

    @Test
    @Description("PATCH /api/auth/user с авторизацией: обновление email возвращает 200 и новые данные")
    public void updateUserWithAuthUpdateEmail() {
        // исходные ожидания
        String expectedName  = userRules.getUser().getName();
        String expectedEmail = faker.internet().safeEmailAddress(); // уникальный email

        // готовим тело PATCH только с email
        UserInfo upd = new UserInfo();
        upd.setEmail(expectedEmail);

        Response patch = userRules.steps()
                .updateUserAuthorized(userRules.getAccessToken(), upd);

        patch.then()
                .statusCode(SC_OK)
                .body("success", is(true))
                .body("user.name",  equalTo(expectedName))
                .body("user.email", equalTo(expectedEmail));
    }
}



