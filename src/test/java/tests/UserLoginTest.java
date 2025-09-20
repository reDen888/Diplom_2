package tests;

import base.BaseTest;
import data.TestData;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import models.AuthResponse;
import models.ErrorResponse;
import models.User;
import org.junit.Before;
import org.junit.Test;
import api.ApiClient;

import static java.net.HttpURLConnection.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

public class UserLoginTest extends BaseTest {
    private User existingUser;

    @Before
    public void prepareTestData() {
        apiClient = new ApiClient(requestSpec);
        String email = TestData.generateEmail();
        String password = TestData.generatePassword();
        String name = TestData.generateName();
        existingUser = new User(email, password, name);
        Response response = createUser(existingUser);
        AuthResponse authResponse = response.as(AuthResponse.class);
        accessToken = extractAccessToken(authResponse);
        refreshToken = extractRefreshToken(authResponse);
    }

    @Test
    @DisplayName("Вход под существующим пользователем")
    @Description("Проверяет успешную аутентификацию существующего пользователя.")
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
    @DisplayName("Вход с неверным логином")
    @Description("Проверяет, что попытка входа с несуществующим email приводит к ошибке.")
    public void testLoginWithInvalidEmail() {
        String invalidEmail = "nonexisting_" + System.currentTimeMillis() + "@ya.ru";
        String password = existingUser.getPassword();
        User invalidUser = new User(invalidEmail, password, "Some Name");
        Response response = loginUser(invalidUser);
        response.then().statusCode(HTTP_UNAUTHORIZED);
        ErrorResponse errorResponse = response.as(ErrorResponse.class);
        assertFalse("Ответ должен быть неуспешным", errorResponse.isSuccess());
        response.then().body("message", containsString("email or password are incorrect"));
    }

    @Test
    @DisplayName("Вход с неверным паролем")
    @Description("Проверяет, что попытка входа с неверным паролем приводит к ошибке.")
    public void testLoginWithInvalidPassword() {
        String email = existingUser.getEmail();
        String invalidPassword = "wrongpassword_" + System.currentTimeMillis();
        User invalidUser = new User(email, invalidPassword, "Some Name");
        Response response = loginUser(invalidUser);
        response.then().statusCode(HTTP_UNAUTHORIZED);
        ErrorResponse errorResponse = response.as(ErrorResponse.class);
        assertFalse("Ответ должен быть неуспешным", errorResponse.isSuccess());
        response.then().body("message", containsString("email or password are incorrect"));
    }
}