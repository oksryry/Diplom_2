package forUser.UserCreationTests;

import com.github.javafaker.Faker;
import forUser.UserApi;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import utils.Rules;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

import io.qameta.allure.junit4.DisplayName;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;

@RunWith(Parameterized.class) //параметризованный тест, проверяет, чтобы создать пользователя, нужно передать в ручку все обязательные поля
public class UserCreationParamsTest {



    private final String requestCreationWithoutOneField;

    private final UserApi userApi = new UserApi();

    public UserCreationParamsTest(String requestCreationWithoutOneField) {
        this.requestCreationWithoutOneField = requestCreationWithoutOneField;
    }

    @Rule
    public final Rules rule = new Rules();

        @Parameterized.Parameters(name = "{index}: При наборе данных для регистрации пльзователя {0}")
    public static Collection<Object[]> userParams() {
        Faker faker = new Faker(Locale.forLanguageTag("ru"));
        return Arrays.asList(new Object[][] {
                {String.format("{ \"password\": \"%s\", \"firstName\": \"%s\" }", faker.internet().password(5, 25), faker.name().firstName())},
                {String.format("{ \"email\": \"%s\", \"firstName\": \"%s\" }", faker.internet().safeEmailAddress(), faker.name().firstName())},
                {String.format("{ \"email\": \"%s\", \"password\": \"%s\" }", faker.internet().safeEmailAddress(), faker.internet().password(5, 25))},
        });
    }

    @Test
    @DisplayName("Check if create user without one parameter returns 403 Forbidden")
    public void createUserWithoutOneParameter() {
        userApi.createUserParams(requestCreationWithoutOneField)
                .then()
                .assertThat().body("message",equalTo("Email, password and name are required fields"))
                .and()
                .statusCode(403)
                .and()
                .assertThat().body("success", equalTo(false));


    }

}
