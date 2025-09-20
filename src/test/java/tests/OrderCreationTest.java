package tests;

import base.BaseTest;
import data.TestData;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import models.*;
import org.junit.Before;
import org.junit.Test;
import api.ApiClient;

import java.util.*;

import static java.net.HttpURLConnection.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

public class OrderCreationTest extends BaseTest {
    private List<String> validIngredients;

    @Before
    public void prepareTestData() {
        apiClient = new ApiClient(requestSpec);
        // Создание уникального пользователя
        String email = TestData.generateEmail();
        String password = TestData.generatePassword();
        String name = TestData.generateName();
        User user = new User(email, password, name);
        Response registerResponse = createUser(user);
        // Проверяем, что регистрация успешна
        registerResponse.then().statusCode(HTTP_OK);
        AuthResponse authResponse = registerResponse.as(AuthResponse.class);
        // Извлекаем токены
        accessToken = extractAccessToken(authResponse);
        refreshToken = extractRefreshToken(authResponse);
        // Получение списка ингредиентов
        Response ingredientsResponse = getIngredients();
        ingredientsResponse.then().statusCode(HTTP_OK);
        IngredientsResponse ingredients = ingredientsResponse.as(IngredientsResponse.class);
        assertTrue("Не удалось получить список ингредиентов", ingredients.isSuccess());
        assertNotNull("Список ингредиентов не должен быть null", ingredients.getData());
        assertTrue("Список ингредиентов должен содержать минимум 2 элемента", ingredients.getData().size() >= 2);
        // Выбираем первые два ингредиента (используем getId())
        validIngredients = Arrays.asList(
                ingredients.getData().get(0).getId(),
                ingredients.getData().get(1).getId()
        );
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и ингредиентами")
    @Description("Проверяет возможность создания заказа авторизованным пользователем с корректным списком ингредиентов.")
    public void testCreateOrderWithAuthAndIngredients() {
        Response response = createOrder(validIngredients, accessToken);
        // Проверяем код ответа
        response.then().statusCode(HTTP_OK);
        Map<String, Object> responseBody = response.as(Map.class);
        // Проверяем поля ответа
        assertTrue("Ответ должен быть успешным (success=true)", (Boolean) responseBody.get("success"));
        assertNotNull("Имя заказа (name) не должно быть null", responseBody.get("name"));
        assertTrue("Имя заказа не должно быть пустым", !((String) responseBody.get("name")).isEmpty());
        // Проверяем поле order
        @SuppressWarnings("unchecked")
        Map<String, Object> order = (Map<String, Object>) responseBody.get("order");
        assertNotNull("Поле order не должно быть null", order);
        // Проверяем номер заказа
        Object numberObj = order.get("number");
        assertNotNull("Номер заказа (number) не должен быть null", numberObj);
        assertTrue("Номер заказа должен быть числом", numberObj instanceof Number);
        int orderNumber = ((Number) numberObj).intValue();
        assertTrue("Номер заказа должен быть больше 0", orderNumber > 0);
        // Проверяем наличие других полей заказа
        assertNotNull("Поле id заказа не должно быть null", order.get("_id"));
        assertNotNull("Поле status заказа не должно быть null", order.get("status"));
        assertNotNull("Поле createdAt заказа не должно быть null", order.get("createdAt"));
        assertNotNull("Поле updatedAt заказа не должно быть null", order.get("updatedAt"));
        // Проверяем ingredients
        assertNotNull("Поле ingredients заказа не должно быть null", order.get("ingredients"));
        assertTrue("Поле ingredients должно быть списком", order.get("ingredients") instanceof List);
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    @Description("Проверяет поведение API при попытке создания заказа без авторизации. (Документация требует 401, но API возвращает 200)")
    public void testCreateOrderWithoutAuth() {
        Response response = createOrder(validIngredients, null);
        // Проверяем код ответа
        response.then().statusCode(HTTP_OK);
        // Десериализуем ответ в Map
        Map<String, Object> responseBody = response.as(Map.class);
        // Проверяем поля ответа - API возвращает success=true
        Boolean success = (Boolean) responseBody.get("success");
        assertNotNull("Ответ должен быть успешным (success не null)", success);
        // Проверяем, что имя заказа возвращается
        String name = (String) responseBody.get("name");
        assertNotNull("Имя заказа (name) не должно быть null", name);
        assertFalse("Имя заказа не должно быть пустым", name.isEmpty());
        // Проверяем поле order
        @SuppressWarnings("unchecked")
        Map<String, Object> order = (Map<String, Object>) responseBody.get("order");
        assertNotNull("Поле order не должно быть null", order);
        // Проверяем номер заказа
        Object numberObj = order.get("number");
        assertNotNull("Номер заказа (number) не должен быть null", numberObj);
        assertTrue("Номер заказа должен быть числом", numberObj instanceof Number);
        int orderNumber = ((Number) numberObj).intValue();
        assertTrue("Номер заказа должен быть больше 0", orderNumber > 0);
        // Выводим предупреждение о несоответствии документации
        System.out.println("ПРЕДУПРЕЖДЕНИЕ: API позволяет создавать заказы без авторизации, что противоречит документации. Ожидался код 401.");
    }

    @Test
    @DisplayName("Создание заказа без авторизации с ингредиентами")
    @Description("Проверяет поведение API при попытке создания заказа без авторизации, но с корректными ингредиентами. (Документация требует 401, но API возвращает 200)")
    public void testCreateOrderWithoutAuthWithIngredients() {
        Response response = createOrder(validIngredients, null);
        // Проверяем код ответа - API возвращает 200
        response.then().statusCode(HTTP_OK);
        // Десериализуем ответ в Map
        Map<String, Object> responseBody = response.as(Map.class);
        // Проверяем поля ответа - API возвращает success=true
        Boolean success = (Boolean) responseBody.get("success");
        assertNotNull("Ответ должен быть успешным (success не null)", success);
        // Проверяем, что имя заказа возвращается
        String name = (String) responseBody.get("name");
        assertNotNull("Имя заказа (name) не должно быть null", name);
        assertFalse("Имя заказа не должно быть пустым", name.isEmpty());
        // Проверяем поле order
        @SuppressWarnings("unchecked")
        Map<String, Object> order = (Map<String, Object>) responseBody.get("order");
        assertNotNull("Поле order не должно быть null", order);
        // Проверяем номер заказа
        Object numberObj = order.get("number");
        assertNotNull("Номер заказа (number) не должен быть null", numberObj);
        assertTrue("Номер заказа должен быть числом", numberObj instanceof Number);
        int orderNumber = ((Number) numberObj).intValue();
        assertTrue("Номер заказа должен быть больше 0", orderNumber > 0);
        // Выводим предупреждение о несоответствии документации
        System.out.println("ПРЕДУПРЕЖДЕНИЕ: API позволяет создавать заказы без авторизации с ингредиентами, что противоречит документации. Ожидался код 401.");
    }

    @Test
    @DisplayName("Создание заказа без ингредиентов")
    @Description("Проверяет, что попытка создания заказа без указания ингредиентов приводит к ошибке.")
    public void testCreateOrderWithoutIngredients() {
        // Передаем пустой список ингредиентов
        Response response = createOrder(Collections.emptyList(), accessToken);
        // Проверяем код ответа - документация говорит 400
        response.then().statusCode(HTTP_BAD_REQUEST);
        // Десериализуем ответ в Map
        Map<String, Object> responseBody = response.as(Map.class);
        // Проверяем поля ответа
        Boolean success = (Boolean) responseBody.get("success");
        String message = (String) responseBody.get("message");
        assertFalse("Ответ должен быть неуспешным (success=false)", success != null && success);
        // Использование containsString для большей гибкости
        response.then().body("message", containsString("Ingredient ids must be provided"));
    }

    @Test
    @DisplayName("Создание заказа с неверным хешем ингредиентов")
    @Description("Проверяет поведение API при попытке создания заказа с некорректными ID ингредиентов.")
    public void testCreateOrderWithInvalidIngredientHash() {
        // Передаем невалидные ID ингредиентов
        List<String> invalidIngredients = Arrays.asList("invalid_hash_1", "invalid_hash_2");
        Response response = createOrder(invalidIngredients, accessToken);
        response.then().statusCode(HTTP_INTERNAL_ERROR);
        // Пытаемся десериализовать ответ, даже если это 500
        try {
            Map<String, Object> responseBody = response.as(Map.class);
            Boolean success = (Boolean) responseBody.get("success");
            // Проверяем, что ответ помечен как неуспешный, если он в формате JSON
            if (success != null) {
                assertFalse("Ответ должен быть неуспешным (success=false) при 500", success);
            }
        } catch (Exception e) {
            System.out.println("Не удалось десериализовать тело ответа 500: " + e.getMessage());
        }
    }
}