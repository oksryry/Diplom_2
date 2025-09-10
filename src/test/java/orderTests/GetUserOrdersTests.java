package orderTests;

import entities.order.OrderRequest;
import entities.user.User;
import forUser.UserApi;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import utils.Rules;
import utils.SetUserParameters;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.apache.hc.core5.http.HttpStatus.SC_OK;
import static org.apache.hc.core5.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.greaterThan;


public class GetUserOrdersTests {

    private final SetUserParameters generator = new SetUserParameters();
    private final User user = generator.setParameters();
    private final UserApi userApi = new UserApi();

    private String accessToken; // "Bearer ..."

    @Rule
    public final Rules rule = new Rules(); // выставляет RestAssured.baseURI


    @Step("Создаём пользователя для теста")
    public void createUserForGetOrdersTests() {
        Response response = userApi.createUser(user);
        accessToken = response.jsonPath().getString("accessToken"); // сохранили токен из успешного запроса на создание юзера
    }



    @Step("Получаем {count} валидных id ингредиентов")
    private List<String> getListOfIngredientIds(int count) {
        Response resp = given()
                .when().get("/api/ingredients")
                .then().statusCode(SC_OK)
                .extract().response();

        List<String> ids = resp.jsonPath().getList("data._id");
        return new ArrayList<>(ids.subList(0, count)); // независимая копия
    }

    @Step("Создаём заказ (авторизован), ингредиентов: {count}")
    private Response createOrder(String token, List<String> ingredientIds, int count) {
        return given()
                .header("Authorization", token)
                .contentType(ContentType.JSON)
                .body(new OrderRequest(ingredientIds))          // {"ingredients":[...]}
                .when()
                .post("/api/orders");
    }

    @Step("Запрашиваем заказы пользователя (авторизован)")
    private Response getUserOrdersAuthorized(String token) {
        return given()
                .header("Authorization", token)
                .when()
                .get("/api/orders");
    }

    @Step("Проверяем успешный ответ со списком заказов пользователя")
    private void assertUserOrdersSuccess(Response response) {
        response.then()
                .statusCode(SC_OK)
                .body("success", anyOf(is(true), nullValue()))
                .body("orders", notNullValue())
                .body("orders.size()", greaterThan(0))
                .body("orders[0].number", notNullValue())
                .body("orders[0].ingredients", notNullValue())
                .body("orders[0].status", not(isEmptyOrNullString()))
                .body("total", greaterThanOrEqualTo(0))
                .body("totalToday", greaterThanOrEqualTo(0));
    }

    @Step("Запрашиваем заказы пользователя БЕЗ авторизации")
    private Response getUserOrdersUnauthorized() {
        return given()
                .when()
                .get("/api/orders");
    }

    @Step("Проверяем 401 Unauthorized и сообщение 'You should be authorised'")
    private void assertUnauthorized(Response response) {
        response.then()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", is(false))
                .body("message", equalTo("You should be authorised"));
    }

    // ================ ТЕСТЫ =================

    @Test
    @Description("Авторизованный пользователь получает свои заказы (возвращается непустой список, числа total/totalToday)")
    public void getUserOrders_withAuth_success() {
        createUserForGetOrdersTests();
        // Чтобы список точно был не пустой — создадим заказ
        List<String> ing = getListOfIngredientIds(2);
        createOrder(accessToken, ing, ing.size()).then().statusCode(SC_OK);

        Response orders = getUserOrdersAuthorized(accessToken);
        assertUserOrdersSuccess(orders);
    }

    @Test
    @Description("Неавторизованный пользователь при запросе заказов получает 401")
    public void getUserOrders_withoutAuth_401() {
        Response resp = getUserOrdersUnauthorized();
        assertUnauthorized(resp);
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

