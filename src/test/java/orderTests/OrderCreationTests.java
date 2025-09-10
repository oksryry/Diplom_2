package orderTests;

import entities.order.OrderRequest;
import entities.user.User;
import forUser.UserApi;
import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.http.client.protocol.ResponseContentEncoding;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import utils.Rules;
import utils.SetUserParameters;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.apache.hc.core5.http.HttpStatus.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.isEmptyOrNullString;

public class OrderCreationTests {

    private final SetUserParameters generator = new SetUserParameters();
    private final User user = generator.setParameters();
    private final UserApi userApi = new UserApi();
    private static final int DEFAULT_INGREDIENT_COUNT = 2; //значение взято из документации, оно же выглядит весьма удобным и логичным

    private String accessToken; // "Bearer ..."

    @Rule
    public final Rules rule = new Rules(); // выставляем RestAssured.baseURI

    @Step("Создаём пользователя для теста")
    public void createUserForAuthorizationTests() {
        Response response = userApi.createUser(user);
        accessToken = response.jsonPath().getString("accessToken"); // сохранили токен из успешного запроса на создание юзера
    }



//Создаём заказ: пользователь авторизован, есть ингредиенты
    @Step("Получаем валидные id ингредиентов (кол-во: {count})")
    private List<String> getListOfIngredientIds(int count) {
        Response response = given()
                .when()
                .get("/api/ingredients")
                .then()
                .statusCode(SC_OK)
                .extract().response();

        List<String> ids = response.jsonPath().getList("data._id");
        return ids.subList(0, count); // первые count id
    }

    @Step("Создаём заказ: пользователь авторизован, есть ингредиенты; {ingredientIds}")
    private Response createOrderAuthorizedWithIngridients(String token, List<String> ingredientIds) {
        return given()
                .header("Authorization", token)         // "Bearer ..."
                .contentType(ContentType.JSON)
                .body(new OrderRequest(ingredientIds))  // {"ingredients":[...]}
                .when()
                .post("/api/orders");
    }

    @Step("Проверяем, что заказ создан успешно")
    private void assertOrderCreated_withAuth_withIngredients(Response response) {
        response.then()
                .statusCode(SC_OK)
                .body("success", is(true))
                .body("order.number", notNullValue())
                .body("name", not(isEmptyOrNullString()));
    }

    // ---------- ТЕСТ ----------
    @Test
    @Description("Создание заказа: авторизованный пользователь с валидными ингредиентами → success=true, есть номер")
    public void createOrder_withAuth_withIngredients_success() {
        createUserForAuthorizationTests();
        List<String> ingredients = getListOfIngredientIds(DEFAULT_INGREDIENT_COUNT);           // шаг 1
        Response response = createOrderAuthorizedWithIngridients(accessToken, ingredients); // шаг 2
        assertOrderCreated_withAuth_withIngredients(response);                                // шаг 3
    }




//Создаём заказ: авторизованный пользователь, без ингредиентов
    @Step("Создаём заказ: авторизованный пользователь, без ингредиентов")
    private Response createOrderAuthorizedWithoutIngredients(String token) {
        return given()
                .header("Authorization", token)            // "Bearer ..."
                .contentType(ContentType.JSON)
                .body(new OrderRequest(Collections.emptyList())) // {"ingredients":[]}
                .when()
                .post("/api/orders");
    }

    @Step("Проверяем ответ: 400 Bad Request и сообщение про обязательные ингредиенты")
    private void assertNoIngredientsBadRequest(Response response) {
        response.then()
                .statusCode(SC_BAD_REQUEST)
                .body("success", is(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

// ---------- ТЕСТ ----------

    @Test
    @Description("Создание заказа: авторизованный пользователь БЕЗ ингредиентов → 400 + сообщение")
    public void createOrder_withAuth_noIngredients_400() {
        createUserForAuthorizationTests();
        Response response = createOrderAuthorizedWithoutIngredients(accessToken);
        assertNoIngredientsBadRequest(response);
    }



    //Создание заказа: авторизованный пользователь с НЕВЕРНЫМ хешем ингредиента → 500
    @Step("Готовим список с невалидным ID ингредиента: {invalidId}")
    private List<String> prepareInvalidIngredientIds(String invalidId) {
        return Collections.singletonList(invalidId);
    }

    @Step("Создаём заказ: авторизованный пользователь, с невалидными ингредиентами (некорректный хеш)")
    private Response createOrderAuthorizedWithInvalidIngredients(String token, List<String> ingredientIds) {
        return given()
                .header("Authorization", token)           // "Bearer ..."
                .contentType(ContentType.JSON)
                .body(new OrderRequest(ingredientIds))    // {"ingredients":[...]}
                .when()
                .post("/api/orders");
    }

    @Step("Проверяем: сервер вернул 500 Internal Server Error")
    private void assertInvalidIngredient500(Response response) {
        response.then().statusCode(SC_INTERNAL_SERVER_ERROR);
    }

// --------- ТЕСТ ---------

    @Test
    @Description("Создание заказа: авторизованный пользователь с НЕВЕРНЫМ хешем ингредиента → 500")
    public void createOrder_withAuth_invalidIngredient_500() {
        createUserForAuthorizationTests();
        List<String> badIds = prepareInvalidIngredientIds("invalid-hash-value");
        Response resp = createOrderAuthorizedWithInvalidIngredients(accessToken, badIds);
        assertInvalidIngredient500(resp);
    }




    //Создание заказа: БЕЗ авторизации с валидными ингредиентами → 401 (требуется авторизация)
    //тест находит ошибку
    @Step("Создаём заказ: без авторизации, с валидными ингредиентами")
    private Response createOrderWithoutAuth(List<String> ingredientIds, int count) {
        return given()
                .contentType(ContentType.JSON)
                .body(new OrderRequest(ingredientIds))  // {"ingredients":[...]}
                .when()
                .post("/api/orders");
    }

    @Step("Проверяем: 401 Unauthorized и сообщение про необходимость авторизации")
    private void assertUnauthorized(Response response) {
        response.then()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", is(false))
                .body("message", anyOf(equalTo("You should be authorised"), notNullValue()));
    }

// ---------- ТЕСТ ----------

    @Test
    @Description("Создание заказа: БЕЗ авторизации с валидными ингредиентами → 401 (требуется авторизация)")
    public void createOrder_withoutAuth_withIngredients_401() {
        List<String> ingredients = getListOfIngredientIds(2);                 // шаг 1
        Response resp = createOrderWithoutAuth(ingredients, ingredients.size()); // шаг 2
        assertUnauthorized(resp);                                         // шаг 3
    }




    //Создание заказа: БЕЗ авторизации и БЕЗ ингредиентов → 400 (пустой список)
    @Step("Создаём заказ: без авторизации и без ингредиентов")
    private Response createOrderWithoutAuthAndWithoutIngredients() {
        return given()
                .contentType(ContentType.JSON)
                .body(new OrderRequest(Collections.emptyList())) // {"ingredients":[]}
                .when()
                .post("/api/orders");
    }

// ---------- ТЕСТ ----------

    @Test
    @Description("Создание заказа: БЕЗ авторизации и БЕЗ ингредиентов → 400 (пустой список)")
    public void createOrder_withoutAuth_noIngredients_400() {
        Response response = createOrderWithoutAuthAndWithoutIngredients(); // шаг 1
        assertNoIngredientsBadRequest(response);                           // шаг 2
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
