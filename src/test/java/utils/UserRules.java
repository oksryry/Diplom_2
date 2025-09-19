package utils;

import entities.user.User;
import org.junit.rules.ExternalResource;
import steps.UserSteps;

public class UserRules extends ExternalResource {

    private final UserSteps steps = new UserSteps();
    private final SetUserParameters generator = new SetUserParameters();

    private User user;
    private String accessToken; // "Bearer ..."

    //  геттеры для тестов
    public User getUser() { return user; }
    public String getAccessToken() { return accessToken; }
    public UserSteps steps() { return steps; }

    @Override
    protected void before() {
        // Подготовим валидного пользователя (для чистого teardown)
        user = generator.setParameters();
        accessToken = steps.createUserAndGetToken(user); // вернёт "Bearer ..."
    }

    @Override
    protected void after() {
        if (accessToken != null) {
            steps.deleteUser(accessToken);
            accessToken = null;
        }
    }
}
