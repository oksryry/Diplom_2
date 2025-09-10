package forUser;

import entities.user.User;
import entities.user.UserCreds;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class UserApi {

    private static final String USER_REGISTER_URL = "/api/auth/register";

    private static final String USER_AUTHORIZATION_URL = "/api/auth/login";

    private static final String USER_DELETE_URL = "/api/auth/user";

    @Step("Create unique user")
    public Response createUser(User user) {
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(user)
                .when()
                .post(USER_REGISTER_URL);
    }

    @Step("Login with created user")
    public Response userAuthorization(UserCreds userCreds) {
        return given()
                .header("Content-type", "application/json")
                .and()
                .body(userCreds)
                .when()
                .post(USER_AUTHORIZATION_URL);
    }

    @Step("Create User without one parameter - email/password/name")
    public Response createUserParams(String requestBody) {
        return given()
                .header("Content-type", "application/json")
                .body(requestBody)
                .when()
                .post(USER_REGISTER_URL);
    }

    @Step("Delete user")
    public Response deleteUser(int email) {
        return given()
                .header("Content-type", "application/json")
                .when()
                .delete(USER_DELETE_URL + ":" + email);
    }
}
