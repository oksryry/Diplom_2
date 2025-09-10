package forUser.UserUpdatesTests;

import entities.user.User;
import entities.user.UserUpdateRequest;
import forUser.UserApi;
import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.Parameterized;
import utils.Rules;
import utils.SetUserParameters;

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.apache.hc.core5.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

public class UserUpdateUnauthorizedTest {

    private final SetUserParameters generator = new SetUserParameters();
    private final User user = generator.setParameters();
    private static final UserApi userApi = new UserApi();

    private String accessToken;

    @Rule
    public final Rules rule = new Rules();

    @Parameterized.Parameter(0)
    public String field;
    @Parameterized.Parameter(1)
    public String newValue;

    @Parameterized.Parameters(name = "{index}: try update {0} without auth")
    public static Iterable<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"name",  "NoAuthUser"},
                {"email", "noauth@noauth.user"}
        });

    }

    @Before
    public void createUserForAuthorizationTests() {
        Response response = userApi.createUser(user);
        accessToken = response.jsonPath().getString("accessToken"); // сохранили токен из успешного запроса на создание юзера
    }

    @Test
    @Description("PATCH /api/auth/user without Authorization returns 401 for any field")
    public void updateUser_withoutAuth() {
        UserUpdateRequest upd = new UserUpdateRequest();
        if ("name".equals(field)) upd.setName(newValue);
        if ("email".equals(field)) upd.setEmail(newValue);

        given()
                .header("Content-Type", "application/json")
                .body(upd)
                .when()
                .patch("/api/auth/user")
                .then()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", is(false))
                .body("message", equalTo("You should be authorised"));
    }


}
