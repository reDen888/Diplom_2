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

public class UserCreationTest extends BaseTest {

    @Before
    public void setUp() {
        apiClient = new ApiClient(requestSpec);
    }

    @Test
    @DisplayName("Создание уникального пользователя")
    @Description("Проверяет возможность создания нового уникального пользователя.")
    public void testCreateUniqueUser() {
        String email = TestData.generateEmail();
        String password = TestData.generatePassword();
        String name = TestData.generateName();
        User user = new User(email, password, name);
        Response response = createUser(user);
        response.then().statusCode(HTTP_OK); // Проверка кода 200
        AuthResponse authResponse = response.as(AuthResponse.class);
        assertTrue("Ответ должен быть успешным", authResponse.isSuccess());
        assertEquals("Email должен совпадать", email, authResponse.getUser().getEmail());
        assertEquals("Имя должно совпадать", name, authResponse.getUser().getName());
        assertNotNull("accessToken не должен быть null", authResponse.getAccessToken());
        assertNotNull("refreshToken не должен быть null", authResponse.getRefreshToken());
    }

    @Test
    @DisplayName("Создание пользователя, который уже зарегистрирован")
    @Description("Проверяет, что попытка создания пользователя с уже существующим email приводит к ошибке.")
    public void testCreateExistingUser() {
        String email = TestData.generateEmail();
        String password = TestData.generatePassword();
        String name = TestData.generateName();
        User user = new User(email, password, name);
        // Первое создание
        createUser(user);
        // Попытка создать снова
        Response response = createUser(user);
        // Проверка кода 403 Forbidden
        response.then().statusCode(HTTP_FORBIDDEN);
        ErrorResponse errorResponse = response.as(ErrorResponse.class);
        assertFalse("Ответ должен быть неуспешным", errorResponse.isSuccess());
        response.then().body("message", containsString("User already exists"));
    }

    @Test
    @DisplayName("Создание пользователя без заполнения обязательного поля - Email")
    @Description("Проверяет, что попытка создания пользователя без email приводит к ошибке.")
    public void testCreateUserWithoutEmail() {
        // Используем TestData для генерации других полей
        String password = TestData.generatePassword();
        String name = TestData.generateName();
        // Передаем null для email
        User user = new User(null, password, name);
        Response response = createUser(user);
        // Проверка кода 403 Forbidden
        response.then().statusCode(HTTP_FORBIDDEN);
        // Проверка тела ответа на наличие сообщения об ошибке
        ErrorResponse errorResponse = response.as(ErrorResponse.class);
        assertFalse("Ответ должен быть неуспешным", errorResponse.isSuccess());
        // Использование containsString для большей гибкости
        response.then().body("message", containsString("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без заполнения обязательного поля - Name")
    @Description("Проверяет, что попытка создания пользователя без имени приводит к ошибке.")
    public void testCreateUserWithoutName() {
        // Используем TestData для генерации других полей
        String email = TestData.generateEmail();
        String password = TestData.generatePassword();
        // Передаем null для имени
        User user = new User(email, password, null);
        Response response = createUser(user);
        // Проверка кода 403 Forbidden
        response.then().statusCode(HTTP_FORBIDDEN);
        // Проверка тела ответа на наличие сообщения об ошибке
        ErrorResponse errorResponse = response.as(ErrorResponse.class);
        assertFalse("Ответ должен быть неуспешным", errorResponse.isSuccess());
        // Использование containsString для большей гибкости
        response.then().body("message", containsString("Email, password and name are required fields"));
    }

    @Test
    @DisplayName("Создание пользователя без заполнения обязательного поля - Password")
    @Description("Проверяет, что попытка создания пользователя без пароля приводит к ошибке.")
    public void testCreateUserWithoutPassword() {
        String email = TestData.generateEmail();
        String name = TestData.generateName();
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

}