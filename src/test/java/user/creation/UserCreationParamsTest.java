package user.creation;

import com.github.javafaker.Faker;
import entities.user.User;
import io.restassured.response.Response;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import steps.UserSteps;
import utils.Rules;

import java.util.Locale;

import io.qameta.allure.junit4.DisplayName;
import org.junit.Test;

import static org.apache.hc.core5.http.HttpStatus.SC_FORBIDDEN;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

@RunWith(Parameterized.class) //параметризованный тест, проверяет, чтобы создать пользователя, нужно передать в ручку все обязательные поля
public class UserCreationParamsTest {
    private final UserSteps steps = new UserSteps();


    @Rule
    public final Rules rule = new Rules();

    @Parameterized.Parameter(0)
    public User request;


    @Parameterized.Parameters(name = "{index}: При наборе данных для регистрации пльзователя {0}")
    public static Object[][] userParams() {
        Faker faker = new Faker(Locale.forLanguageTag("ru"));

        String email1 = faker.internet().safeEmailAddress();
        String email2 = faker.internet().safeEmailAddress();
        String name1  = faker.name().firstName();
        String name2  = faker.name().firstName();
        String pass1  = faker.internet().password(6, 20);
        String pass2  = faker.internet().password(6, 20);


        return new Object[][]{
                // нет email
                { new User(null,    pass1, name1) },
                // нет password
                { new User(email1,  null,  name2) },
                // нет name
                { new User(email2,  pass2, null ) }
        };
    }


    @Test
    @DisplayName("Check if create user without one parameter returns 403 Forbidden")
    public void createUserWithoutOneParameterTest() {
        Response resp = steps.createUser(request);

        resp.then()
                .statusCode(SC_FORBIDDEN)
                .body("success", is(false))
                .body("message", equalTo("Email, password and name are required fields"));
    }

}
