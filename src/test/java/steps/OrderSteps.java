package steps;

import entities.order.OrderRequest;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.Collections;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.apache.hc.core5.http.HttpStatus.SC_INTERNAL_SERVER_ERROR;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.anyOf;

public class OrderSteps {

    // ---------- запросы ----------

    @Step("API: Получаем валидные id ингредиентов (кол-во: {count})")
    public List<String> getIngredientIds(int count) {
        Response response =
                given()
                        .when()
                        .get("/api/ingredients")
                        .then()
                        .statusCode(SC_OK)
                        .extract().response();

        List<String> ids = response.jsonPath().getList("data._id");
        return ids.subList(0, Math.min(count, ids.size())); // первые count id
    }

    @Step("API: создаём заказ, пользователь авторизован, ингредиенты: {ingredientIds}")
    public Response createOrderAuthorized(String accessTokenWithBearer, List<String> ingredientIds) {
        return given()
                .header("Authorization", accessTokenWithBearer)   // "Bearer ..."
                .contentType(ContentType.JSON)
                .body(new OrderRequest(ingredientIds))            // {"ingredients":[...]}
                .when()
                .post("/api/orders");
    }

    @Step("API: создаём заказ, пользователь авторизован, без ингредиентов")
    public Response createOrderAuthorizedNoIngredients(String accessTokenWithBearer) {
        return createOrderAuthorized(accessTokenWithBearer, Collections.emptyList());
    }

    @Step("API: создаём заказ БЕЗ авторизации, ингредиенты: {ingredientIds}")
    public Response createOrderUnauthorized(List<String> ingredientIds) {
        return given()
                .contentType(ContentType.JSON)
                .body(new OrderRequest(ingredientIds))
                .when()
                .post("/api/orders");
    }

    @Step("API: создаём заказ БЕЗ авторизации и БЕЗ ингредиентов")
    public Response createOrderUnauthorizedNoIngredients() {
        return createOrderUnauthorized(Collections.emptyList());
    }

// ---------- проверки (ассерты) ----------

    @Step("Проверяем: заказ создан успешно (200, success=true, есть номер и имя)")
    public void assertOrderCreated(Response resp) {
        resp.then()
                .statusCode(SC_OK)
                .body("success", is(true))
                .body("order.number", notNullValue())
                .body("name", not(isEmptyOrNullString()));
    }

    @Step("Проверяем: 400 Bad Request — ингредиенты обязательны")
    public void assertNoIngredients400(Response resp) {
        resp.then()
                .statusCode(SC_BAD_REQUEST)
                .body("success", is(false))
                .body("message", equalTo("Ingredient ids must be provided"));
    }

    @Step("Проверяем: 401 Unauthorized — требуется авторизация")
    public void assertUnauthorized401(Response resp) {
        resp.then()
                .statusCode(SC_UNAUTHORIZED)
                .body("success", is(false))
                .body("message", anyOf(equalTo("You should be authorised"), notNullValue()));
    }

    @Step("Проверяем: 500 Internal Server Error — неверный хеш ингредиента")
    public void assertInvalidIngredient500(Response resp) {
        resp.then().statusCode(SC_INTERNAL_SERVER_ERROR);
    }

    // шаги для получения заказов пользователя
    @Step("API: запрашиваем заказы авторизованного пользователя")
    public Response getUserOrdersAuthorized(String accessTokenWithBearer) {
        return given()
                .header("Authorization", accessTokenWithBearer)
                .when().get("/api/orders");
    }

    @Step("API: запрашиваем заказы пользователя БЕЗ авторизации")
    public Response getUserOrdersUnauthorized() {
        return given().when().get("/api/orders");
    }

    @Step("Проверяем успешный ответ со списком заказов пользователя")
    public void assertUserOrdersSuccess(Response resp) {
        resp.then()
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

}
