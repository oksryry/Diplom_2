package forUser.UserCreationTests;

import entities.user.User;
import forUser.UserApi;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import utils.Rules;
import utils.SetUserParameters;

import static io.restassured.RestAssured.given;
import static org.apache.hc.core5.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.equalTo;

public class UserCreationTests {

    private String accessToken;

    private SetUserParameters generateUser = new SetUserParameters(); //создаём объект генератора пользака
    private User user = generateUser.setParameters(); //создаём объект пользака (генерируем его генератором)
    private UserApi userApi = new UserApi(); //осздаём объект класса, где есть действия с пользаками

    @Rule
    public final Rules rule = new Rules();

    @Step("Create user")
    private Response createUserForCreationTests() {
        Response response = userApi.createUser(user);
        accessToken = response.jsonPath().getString("accessToken"); // сохранили токен из успешного запроса на создание юзера
        return response;
    }

    @Test//поверяем, что пользака можно создать и возвращается корректный код ответа
    @DisplayName("Check creation of User method - statusCode 201 must be returned")
    public void createUser() {
//        createUserForCreationTests();
        Assert.assertEquals("Пользак успешно создан", SC_OK, createUserForCreationTests().statusCode());
    }

    @Test//проверяем, что нельзя создать двух ошиднаковых юзеров
    @DisplayName("Check that's impossible to create the same user")
    public void createTheSameUser() {
        createUserForCreationTests();
        createUserForCreationTests()
                .then()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
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
