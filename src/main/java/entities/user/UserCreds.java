package entities.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class UserCreds { //лкасс для запроса (на авторизацию)

    private String email;

    private String password;

    public static UserCreds getUserCreds(User user) {
        return new UserCreds(user.getEmail(), user.getPassword());
    }
}
