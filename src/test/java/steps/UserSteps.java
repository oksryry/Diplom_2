package steps;

import entities.user.User;
import entities.user.UserCreds;
import entities.user.*;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;

public class UserSteps {
    private final UserApi userApi = new UserApi();

    @Step("API: create user {user.email}")
    public Response createUser(User user) {
        return userApi.createUser(user);
    }

    @Step("API: авторизуемся пользователем {creds.email}")
    public Response authorize(UserCreds creds) {
        return userApi.userAuthorization(creds);
    }

    @Step("API: create user and get accessToken")
    public String createUserAndGetToken(User user) {
        UserCreationAndAuthResponse resp =
                userApi.createUser(user)
                        .then()
                        .statusCode(anyOf(is(200), is(201)))
                        .extract().as(UserCreationAndAuthResponse.class);
        return resp.getAccessToken(); // "Bearer ..."
    }

    @Step("API: delete user")
    public void deleteUser(String accessTokenWithBearer) {
        userApi.deleteUserByToken(accessTokenWithBearer)
                .then()
                .statusCode(anyOf(is(200), is(202), is(204)));
    }

    @Step("API: create user without one field in JSON")
    public Response createUserRaw(String rawJson) {
        return userApi.createUserParams(rawJson);
    }

    @Step("API: PATCH /api/auth/user — update user (authorized)")
    public Response updateUserAuthorized(String accessTokenWithBearer, Object body) {
        return given()
                .header("Authorization", accessTokenWithBearer) // "Bearer ..."
                .contentType(ContentType.JSON)
                .body(body)                                     // например, new UserInfo(name/email)
                .when()
                .patch("/api/auth/user");
    }

    @Step("API: PATCH /api/auth/user update user (unauthorized)")
    public Response updateUserUnauthorized(Object body) {
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .patch("/api/auth/user");
    }
}
