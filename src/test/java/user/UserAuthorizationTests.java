package user;

import entities.user.User;
import entities.user.UserCreds;
import io.restassured.response.Response;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import steps.UserSteps;
import utils.Rules;

import io.qameta.allure.junit4.DisplayName;
import utils.UserRules;

import static org.apache.hc.core5.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.CoreMatchers.*;

public class UserAuthorizationTests {

    private final UserSteps steps = new UserSteps();

    private User user;

    public final Rules rule = new Rules();

    //создаёт и удаляет пользователя
    public final UserRules userRules = new UserRules();

    @Rule
    public final RuleChain chain = RuleChain
            .outerRule(rule)       // сначала baseURI
            .around(userRules);    // затем создание пользователя

    @Before
    public void initFromRule() {
        user = userRules.getUser();              // <<< важная строка
    }



    @Test //проверка, что юзер может авторизоваться
    @DisplayName("Successful authorization returns accessToken")
    public void userSuccessfulAuthorizationTest() {
        Response loginResponse = steps.authorize(UserCreds.getUserCreds(user));
        loginResponse.then()
                .statusCode(SC_OK)
                .body("success", is(true))
                .body("accessToken", allOf(notNullValue(), startsWith("Bearer ")))
                .body("user.email", equalTo(user.getEmail()))
                .body("user.name",  equalTo(user.getName()));
    }




    @Test //если авторизоваться под несуществующим пользователем, запрос возвращает ошибку
    @DisplayName("Authorization with invalid creds returns 401")
    public void userLoginWithWrongCredsTest() {
//      // юзер уже есть, делаем плохие креды (правильный email, неправильный пароль)
        UserCreds badCreds = new UserCreds(user.getEmail(), user.getPassword() + "_wrong");

        steps.authorize(badCreds)
                .then()
                .statusCode(SC_UNAUTHORIZED) // 401
                .body("success", is(false))
                .body("message", equalTo("email or password are incorrect"));
    }


}
