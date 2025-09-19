package entities.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor //Успешный ответ login/register: success + токены + данные пользователя -- AuthResponse — обёртка всего ответа: success, accessToken, refreshToken и user: UserInfo.
public class UserCreationAndAuthResponse {

    private Boolean success;

    private String accessToken;   // приходит как "Bearer ..."

    private String refreshToken;

    private UserInfo user;

    public UserCreationAndAuthResponse() { }

    public String accessTokenValue() {
        if (accessToken == null) return null;
        return accessToken.startsWith("Bearer ") ? accessToken.substring("Bearer ".length()) : accessToken;
    }
}
