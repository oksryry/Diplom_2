package forUser.UserUpdatesTests;

import entities.user.User;
import entities.user.UserInfo;
import forUser.UserApi;
import io.qameta.allure.Description;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import utils.Rules;
import utils.SetUserParameters;
import com.github.javafaker.Faker;

import java.util.Arrays;
import java.util.Locale;

import static io.restassured.RestAssured.given;
import static org.apache.hc.core5.http.HttpStatus.SC_OK;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

@RunWith(Parameterized.class)
public class UserUpdateAuthorizedTests {

    private final SetUserParameters generator = new SetUserParameters();
    private final User user = generator.setParameters(); // стартовые валидные данные (email+password+name)
    private static final UserApi userApi = new UserApi();

    private String accessToken;                          // "Bearer ..."

    @Rule
    public final Rules rule = new Rules();

    @Parameterized.Parameter(0)
    public String field;     // "name" или "email"
    @Parameterized.Parameter(1)
    public String newValue;  // новое значение

    @Before
    public void createUserForAuthorizationTests() {
        Response response = userApi.createUser(user);
        accessToken = response.jsonPath().getString("accessToken"); // сохранили токен из успешного запроса на создание юзера
    }


    @Parameterized.Parameters(name = "{index}: изменение {0} -> на {1}")
    public static Iterable<Object[]> data() {
        Faker faker = new Faker(Locale.forLanguageTag("ru"));
        return Arrays.asList(new Object[][]{
                {"name", faker.name().username()},
                {"email", faker.internet().safeEmailAddress()}
        });
    }

    @Test
    @Description("PATCH /api/auth/user с авторизацией: обновление одного поля возвращает 200 и новые данные")
    public void updateUser_withAuth_parametrized() {
        // Готовим тело PATCH только с нужным полем
        UserInfo upd = new UserInfo();
        String expectedEmail = user.getEmail();
        String expectedName = user.getName();

        switch (field) {
            case "name":
                upd.setName(newValue);
                expectedName = newValue;
                break;
            case "email":
                upd.setEmail(newValue);        // уникальный email, чтобы не словить 403 "already exists"
                expectedEmail = newValue;
                break;
        }

        Response patch =
                given()
                        .header("Authorization", accessToken)
                        .contentType(ContentType.JSON)
                        .body(upd) // благодаря NON_NULL уйдёт только одно поле
                        .when()
                        .patch("/api/auth/user");


        patch.then()
                .statusCode(SC_OK)
                .body("success", is(true))
                .body("user.name",  equalTo(expectedName))
                .body("user.email", equalTo(expectedEmail));
    }

    @After
    public void tearDown() {

        if (accessToken == null) {
            return; // нечего удалять
        }
        try {
            given()
                    .header("Authorization", accessToken) // токен уже с "Bearer ..."
                    .when()
                    .delete("/api/auth/user");
        } finally {
            accessToken = null; // чтобы токен не попал в следующий тест
        }

    }
}