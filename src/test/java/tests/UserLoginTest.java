package tests;

import base.BaseTest;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import models.AuthResponse;
import models.ErrorResponse;
import models.User;
import org.junit.Before;
import org.junit.Test;

import static java.net.HttpURLConnection.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

public class UserLoginTest extends BaseTest {
    private User existingUser;

    @Before
    public void prepareTestData() {
        String email = "loginuser_" + System.currentTimeMillis() + "@ya.ru";
        String password = "password123";
        String name = "Login User";
        existingUser = new User(email, password, name);
        Response response = createUser(existingUser);
        AuthResponse authResponse = response.as(AuthResponse.class);
        accessToken = extractAccessToken(authResponse);
        refreshToken = extractRefreshToken(authResponse); // Сохраняем refreshToken
    }

    @Test
    @DisplayName("Вход под существующим пользователем")
    public void testLoginWithExistingUser() {
        Response response = loginUser(existingUser);
        response.then().statusCode(HTTP_OK); // Проверка кода 200

        AuthResponse authResponse = response.as(AuthResponse.class);
        assertTrue("Ответ должен быть успешным", authResponse.isSuccess());
        assertEquals("Email должен совпадать", existingUser.getEmail(), authResponse.getUser().getEmail());
        assertEquals("Имя должно совпадать", existingUser.getName(), authResponse.getUser().getName());
        assertNotNull("accessToken не должен быть null", authResponse.getAccessToken());
        assertNotNull("refreshToken не должен быть null", authResponse.getRefreshToken());
    }

    @Test
    @DisplayName("Вход с неверным логином и паролем")
    public void testLoginWithInvalidCredentials() {
        // Передаем неверные данные
        User invalidUser = new User("nonexisting@ya.ru", "wrongpassword", "Some Name");
        Response response = loginUser(invalidUser);

        // Проверка кода 401 Unauthorized
        response.then().statusCode(HTTP_UNAUTHORIZED);

        ErrorResponse errorResponse = response.as(ErrorResponse.class);
        assertFalse("Ответ должен быть неуспешным", errorResponse.isSuccess());
        // Использование containsString для большей гибкости
        response.then().body("message", containsString("email or password are incorrect"));
    }
}