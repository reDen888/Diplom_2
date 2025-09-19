package tests;

import base.BaseTest;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import models.AuthResponse;
import models.ErrorResponse;
import models.User;
import org.junit.After;
import org.junit.Test;

import static java.net.HttpURLConnection.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

public class UserCreationTest extends BaseTest {
    private String tempAccessTokenForUniqueUser; // Для временного хранения токена в testCreateUniqueUser

    @Test
    @DisplayName("Создание уникального пользователя")
    public void testCreateUniqueUser() {
        String email = "testuser_" + System.currentTimeMillis() + "@ya.ru";
        String password = "password123";
        String name = "Test User";
        User user = new User(email, password, name);
        Response response = createUser(user);

        response.then().statusCode(HTTP_OK); // Проверка кода 200

        AuthResponse authResponse = response.as(AuthResponse.class);
        assertTrue("Ответ должен быть успешным", authResponse.isSuccess());
        assertEquals("Email должен совпадать", email, authResponse.getUser().getEmail());
        assertEquals("Имя должно совпадать", name, authResponse.getUser().getName());
        assertNotNull("accessToken не должен быть null", authResponse.getAccessToken());
        assertNotNull("refreshToken не должен быть null", authResponse.getRefreshToken());

        // Сохраняем токен для очистки в tearDown этого теста
        tempAccessTokenForUniqueUser = extractAccessToken(authResponse);
    }

    @Test
    @DisplayName("Создание пользователя, который уже зарегистрирован")
    public void testCreateExistingUser() {
        String email = "existinguser_" + System.currentTimeMillis() + "@ya.ru";
        String password = "password123";
        String name = "Existing User";
        User user = new User(email, password, name);

        // Первое создание
        createUser(user);

        // Попытка создать снова
        Response response = createUser(user);

        // Проверка кода 403 Forbidden
        response.then().statusCode(HTTP_FORBIDDEN);
        ErrorResponse errorResponse = response.as(ErrorResponse.class);
        assertFalse("Ответ должен быть неуспешным", errorResponse.isSuccess());
        // Использование containsString для большей гибкости
        response.then().body("message", containsString("User already exists"));
    }

    @Test
    @DisplayName("Создание пользователя без заполнения обязательного поля")
    public void testCreateUserWithoutRequiredField() {
        String email = "nopassworduser_" + System.currentTimeMillis() + "@ya.ru";
        String name = "No Password User";
        // Передаем null для пароля
        User user = new User(email, null, name);
        Response response = createUser(user);

        // Проверка кода 403 Forbidden
        response.then().statusCode(HTTP_FORBIDDEN);

        // Проверка тела ответа на наличие сообщения об ошибке
        ErrorResponse errorResponse = response.as(ErrorResponse.class);
        assertFalse("Ответ должен быть неуспешным", errorResponse.isSuccess());
        // Использование containsString для большей гибкости
        response.then().body("message", containsString("Email, password and name are required fields"));
    }

    @After
    public void tearDown() {
        // Очистка для теста testCreateUniqueUser
        if (tempAccessTokenForUniqueUser != null) {
            try {
                deleteUser(tempAccessTokenForUniqueUser);
            } catch (Exception e) {
                System.err.println("Warning: Failed to delete temp user in test tearDown: " + e.getMessage());
            }
            tempAccessTokenForUniqueUser = null; // Сброс для следующего теста
        }

    }
}