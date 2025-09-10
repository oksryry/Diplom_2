package forUser;

import entities.user.User;
import entities.user.UserCreds;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import utils.Rules;
import utils.SetUserParameters;
import io.qameta.allure.junit4.DisplayName;

import static entities.user.UserCreds.getUserCreds;
import static io.restassured.RestAssured.given;
import static org.apache.hc.core5.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.CoreMatchers.*;

public class UserAuthorizationTests {

    private UserApi userApi = new UserApi();
    private SetUserParameters setUserParameters = new SetUserParameters();
    private User user = setUserParameters.setParameters();
    private String accessToken;

    @Rule
    public final Rules rule = new Rules();


    @Before
    public void setUp() {
        user = setUserParameters.setParameters();
    }

    @Step("Create user for authorization tests")
    private void createUserForAuthorizationTests() {
    Response response = userApi.createUser(user);
    accessToken = response.jsonPath().getString("accessToken"); // сохранили токен из успешного запроса на создание юзера

}

    @Step("User authorize in the system and get response")
    public Response userAuthorization() {
        Response response = userApi.userAuthorization(getUserCreds(user));
        return response;
    }


    @Test //проверка, что юзер может авторизоваться
    @DisplayName("Successful authorization returns accessToken")
    public void userSuccessfulAuthorization() {
        createUserForAuthorizationTests();
        Response loginResponse = userAuthorization();
        loginResponse.then()
                .statusCode(SC_OK)
                .body("success", is(true))
                .body("accessToken", allOf(notNullValue(), startsWith("Bearer ")))
                .body("user.email", equalTo(user.getEmail()))
                .body("user.name",  equalTo(user.getName()));
    }



//    @Test //если авторизоваться под несуществующим пользователем, запрос возвращает ошибку
//    @DisplayName("Authorization with invalid creds returns 404")
//    public void courierIncorrectLoginResponse() {
//        generateFakeCourier();
//        authorizeWithFakeCredentials()
//                .then()
//                .assertThat()
//                .statusCode(404)
//                .body("message", equalTo("Учетная запись не найдена"));
//    }


    @Test //если авторизоваться под несуществующим пользователем, запрос возвращает ошибку
    @DisplayName("Authorization with invalid creds returns 401")
    public void userLoginWithWrongCreds() {
        createUserForAuthorizationTests();
        // формируем неверные креды (правильный email, неправильный пароль)
        UserCreds badCreds = new UserCreds(user.getEmail(), user.getPassword() + "_wrong");
        given()
                .contentType(ContentType.JSON)
                .body(badCreds)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(SC_UNAUTHORIZED) // 401
                .body("success", is(false))
                .body("message", equalTo("email or password are incorrect"));
    }

//    @Step("Generate fake courier - with fake credentials to log in")
//    private Courier generateFakeCourier() {
//        Faker faker = new Faker();
//        Faker fakerRU = new Faker(Locale.forLanguageTag("ru"));
//        String invLogin = faker.bothify("????####");
//        String invPassword = faker.bothify("????####");
//        String invFirstName = fakerRU.name().firstName();
//        Courier invalidCourier = new Courier(invLogin, invPassword, invFirstName);
//        return invalidCourier;
//    }

//    @Step("Log in with fake courier/fake credentials")
//    private Response authorizeWithFakeCredentials()
//    {
//        Response response = courierUser.courierAuthorization(
//                new CourierCreds(generateFakeCourier().getLogin(), generateFakeCourier().getPassword()));
//        return response;
//    }
//
//    @Step("Delete created courier after test")
//    private void deleteCourier() {
//        id = courierUser.courierAuthorization(CourierCreds.getCourierCreds(courier)).as(CourierIdInLoginResponse.class).getId();
//        courierUser.deleteCourier(id);
//    }
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
