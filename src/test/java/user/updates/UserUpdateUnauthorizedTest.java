package user.updates;

import entities.user.UserInfo;
import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runners.Parameterized;
import utils.Rules;
import utils.UserRules;

import java.util.Arrays;

import static org.apache.hc.core5.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

public class UserUpdateUnauthorizedTest {


    // настраивает RestAssured.baseURI
    public final Rules rule = new Rules();

    //создаёт и удаляет пользователя
    public final UserRules userRules = new UserRules();

    @Rule
    public final RuleChain chain = RuleChain
            .outerRule(rule)       // сначала baseURI
            .around(userRules);    // затем создание пользователя


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


    @Test
    @Description("PATCH /api/auth/user without Authorization returns 401 for any field")
    public void updateUserWithoutAuthTest() {
        // Формируем тело PATCH только с одним полем (NON_NULL => уйдёт только оно)
        UserInfo upd = new UserInfo();
        if ("name".equals(field))  upd.setName(newValue);
        if ("email".equals(field)) upd.setEmail(newValue);

        Response resp = userRules.steps().updateUserUnauthorized(upd);


        resp.then()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", is(false))
                .body("message", equalTo("You should be authorised"));
    }


}
