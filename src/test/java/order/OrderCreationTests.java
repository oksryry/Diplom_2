package order;

import org.junit.rules.RuleChain;
import steps.OrderSteps;
import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.Rule;
import org.junit.Test;
import utils.Rules;
import utils.UserRules;

import java.util.Arrays;
import java.util.List;

import static utils.TestConstants.DEFAULT_INGREDIENT_COUNT;

public class OrderCreationTests {

    private final OrderSteps orderSteps = new OrderSteps();

    private String accessToken() {
        return userRules.getAccessToken();
    } // "Bearer ..."


    public final Rules rule = new Rules(); // выставляем RestAssured.baseURI
    public final UserRules userRules = new UserRules(); // создаём и удаляем пользователя

    @Rule
    public final RuleChain chain = RuleChain
            .outerRule(rule)       // сначала baseURI
            .around(userRules);    // затем создание пользователя





    // ---------- ТЕСТ ----------
    @Test
    @Description("Создание заказа: авторизованный пользователь с валидными ингредиентами → success=true, есть номер")
    public void createOrderWithAuthWithIngredientsSuccessTest() {
        List<String> ids = orderSteps.getIngredientIds(DEFAULT_INGREDIENT_COUNT);
        Response resp = orderSteps.createOrderAuthorized(accessToken(), ids);
        orderSteps.assertOrderCreated(resp);                               // шаг 3
    }




// ---------- ТЕСТ ----------

    @Test
    @Description("Создание заказа: авторизованный пользователь БЕЗ ингредиентов → 400 + сообщение")
    public void createOrderWithAuthNoIngredients400Test() {
        Response resp = orderSteps.createOrderAuthorizedNoIngredients(accessToken());
        orderSteps.assertNoIngredients400(resp);
    }




// --------- ТЕСТ ---------

    @Test
    @Description("Создание заказа: авторизованный пользователь с НЕВЕРНЫМ хешем ингредиента → 500")
    public void createOrderWithAuthInvalidIngredient500Test() {
        List<String> badIds = Arrays.asList("invalid-hash-value");
        Response resp = orderSteps.createOrderAuthorized(accessToken(), badIds);
        orderSteps.assertInvalidIngredient500(resp);
    }




 //тест находит ошибку
// ---------- ТЕСТ ----------
    @Test
    @Description("Создание заказа: БЕЗ авторизации с валидными ингредиентами → 401 (требуется авторизация)")
    public void createOrderWithoutAuthWithIngredients401Test() {
        List<String> ids = orderSteps.getIngredientIds(DEFAULT_INGREDIENT_COUNT);
        Response resp = orderSteps.createOrderUnauthorized(ids);
        orderSteps.assertUnauthorized401(resp);                                       // шаг 3
    }



// ---------- ТЕСТ ----------

    @Test
    @Description("Создание заказа: БЕЗ авторизации и БЕЗ ингредиентов → 400 (пустой список)")
    public void createOrderWithoutAuthNoIngredients400Test() {
        Response resp = orderSteps.createOrderUnauthorizedNoIngredients();
        orderSteps.assertNoIngredients400(resp);
    }


}
