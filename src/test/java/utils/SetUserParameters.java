package utils;

import com.github.javafaker.Faker;
import entities.user.User;

public class SetUserParameters { //генератор параметров для создания пользователя

    public Faker faker = new Faker();

    public User setParameters() {
        User user = new User();
        user.setEmail(faker.internet().safeEmailAddress());
        user.setName(faker.name().username());
        user.setPassword(faker.internet().password(5, 25));
        return user;
    }
}
