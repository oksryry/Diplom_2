package entities.user;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter @Setter
@AllArgsConstructor
public class UserInfo { //для ответа: содержит то, что реально присылает сервер в "user" → email и name - вложенный объект пользователя в ответах login/register

    private String email;

    private String name;

    public UserInfo() { }

}
