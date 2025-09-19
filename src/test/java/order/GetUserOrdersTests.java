package order;

import org.junit.rules.RuleChain;
import steps.OrderSteps;
import io.qameta.allure.Description;
import io.restassured.response.Response;
import org.junit.Rule;
import org.junit.Test;
import utils.Rules;
import utils.UserRules;

import java.util.List;
import static org.apache.hc.core5.http.HttpStatus.SC_OK;
import static utils.TestConstants.DEFAULT_INGREDIENT_COUNT;


public class GetUserOrdersTests {

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




    // ================ ТЕСТЫ =================

    @Test
    @Description("Авторизованный пользователь получает свои заказы (возвращается непустой список, числа total/totalToday)")
    public void getUserOrdersWithAuthSuccessTest() {
        // Чтобы список точно был не пустой — создадим заказ
        List<String> ids = orderSteps.getIngredientIds(DEFAULT_INGREDIENT_COUNT);
        Response create = orderSteps.createOrderAuthorized(accessToken(), ids);
        create.then().statusCode(SC_OK);
// Запрашиваем заказы и проверяем
        Response orders = orderSteps.getUserOrdersAuthorized(accessToken());
        orderSteps.assertUserOrdersSuccess(orders);
    }

    @Test
    @Description("Неавторизованный пользователь при запросе заказов получает 401")
    public void getUserOrdersWithoutAuth401Test() {
        Response resp = orderSteps.getUserOrdersUnauthorized();
        orderSteps.assertUnauthorized401(resp);
    }


}

