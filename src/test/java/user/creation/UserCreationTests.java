package user.creation;

import entities.user.User;
import entities.user.UserCreationAndAuthResponse;
import org.junit.*;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import steps.UserSteps;
import utils.Rules;
import utils.SetUserParameters;
import static org.apache.hc.core5.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.*;

public class UserCreationTests {

    private String accessToken;
    private final UserSteps steps = new UserSteps();

    private SetUserParameters generator = new SetUserParameters(); //создаём объект генератора пользака
    private User user;

    @Rule
    public final Rules rule = new Rules();

    @Before
    public void setUp() {
        user = generator.setParameters(); // уникальные email/password/name
    }


    @Test//поверяем, что пользака можно создать и возвращается корректный код ответа
    @DisplayName("Check creation of User method - statusCode 201 must be returned")
    public void createUserTest() {
        Response resp = steps.createUser(user);
        resp.then()
                .statusCode(anyOf(is(SC_OK), is(SC_CREATED)))
                .body("success", is(true))
                .body("accessToken", allOf(notNullValue(), startsWith("Bearer ")))
                .body("user.email", equalTo(user.getEmail()))
                .body("user.name",  equalTo(user.getName()));

        // сохраним токен для очистки в @After
        accessToken = resp.as(UserCreationAndAuthResponse.class).getAccessToken();
   }

    @Test//проверяем, что нельзя создать двух ошиднаковых юзеров
    @DisplayName("Check that's impossible to create the same user")
    public void createTheSameUserTest() {
        // успешно создаём юзера (и запоминаем токен для удаления)
        accessToken = steps.createUserAndGetToken(user);

        // повторяем регистрацию — должен быть запрет
        steps.createUser(user)
                .then()
                .statusCode(SC_FORBIDDEN)
                .body("success", equalTo(false))
                .body("message", equalTo("User already exists"));
    }

    @After
    public void tearDown() {
        if (accessToken != null) {
            steps.deleteUser(accessToken);
        }
    }
}
