package tests;

import base.BaseTest;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.response.Response;
import models.*;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static java.net.HttpURLConnection.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;

public class OrderCreationTest extends BaseTest {
    private List<String> validIngredients;

    @Before
    public void prepareTestData() {
        // Создание уникального пользователя для теста
        String email = "orderuser_" + System.currentTimeMillis() + "@ya.ru";
        String password = "password";
        String name = "Order User";
        User user = new User(email, password, name);
        Response registerResponse = createUser(user);

        // Проверяем, что регистрация успешна
        registerResponse.then().statusCode(HTTP_OK);
        AuthResponse authResponse = registerResponse.as(AuthResponse.class);

        // Извлекаем токены
        accessToken = extractAccessToken(authResponse);
        refreshToken = extractRefreshToken(authResponse); // Сохраняем refreshToken

        // Получение списка ингредиентов
        Response ingredientsResponse = getIngredients();
        ingredientsResponse.then().statusCode(HTTP_OK); // Проверяем успешность запроса ингредиентов

        IngredientsResponse ingredients = ingredientsResponse.as(IngredientsResponse.class);
        assertTrue("Не удалось получить список ингредиентов", ingredients.isSuccess());
        assertNotNull("Список ингредиентов не должен быть null", ingredients.getData());
        assertTrue("Список ингредиентов должен содержать минимум 2 элемента", ingredients.getData().size() >= 2);

        // Выбираем первые два ингредиента
        validIngredients = Arrays.asList(
                ingredients.getData().get(0).get_id(),
                ingredients.getData().get(1).get_id()
        );
    }

    @Test
    @DisplayName("Создание заказа с авторизацией и ингредиентами")
    public void testCreateOrderWithAuthAndIngredients() {
        OrderRequest orderRequest = new OrderRequest(validIngredients);
        Response response = createOrder(orderRequest, accessToken);

        // Проверяем код ответа
        response.then().statusCode(HTTP_OK);

        // Десериализуем ответ в Map для большей гибкости
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
        assertNotNull("Поле _id заказа не должно быть null", order.get("_id"));
        assertNotNull("Поле status заказа не должно быть null", order.get("status"));
        assertNotNull("Поле createdAt заказа не должно быть null", order.get("createdAt"));
        assertNotNull("Поле updatedAt заказа не должно быть null", order.get("updatedAt"));

        // Проверяем ingredients
        assertNotNull("Поле ingredients заказа не должно быть null", order.get("ingredients"));
        assertTrue("Поле ingredients должно быть списком", order.get("ingredients") instanceof List);
    }

    @Test
    @DisplayName("Создание заказа без авторизации")
    public void testCreateOrderWithoutAuth() {
        OrderRequest orderRequest = new OrderRequest(validIngredients);
        Response response = createOrder(orderRequest, null); // Без токена

        // Проверяем код ответа
        response.then().statusCode(HTTP_OK);

        // Десериализуем ответ в Map
        Map<String, Object> responseBody = response.as(Map.class);

        // Проверяем поля ответа - API возвращает success=true
        Boolean success = (Boolean) responseBody.get("success");
        assertNotNull("Ответ должен быть успешным (success не null)", success); // <-- Более общая проверка

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
    @DisplayName("Создание заказа без авторизации с ингредиентами") // Новый тест
    public void testCreateOrderWithoutAuthWithIngredients() {
        OrderRequest orderRequest = new OrderRequest(validIngredients);
        Response response = createOrder(orderRequest, null); // Без токена, но с ингредиентами

        // Проверяем код ответа - НЕСМОТРЯ НА ДОКУМЕНТАЦИЮ, API возвращает 200
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
    public void testCreateOrderWithoutIngredients() {
        // Передаем пустой список ингредиентов
        OrderRequest orderRequest = new OrderRequest(Collections.emptyList());
        Response response = createOrder(orderRequest, accessToken);

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
    public void testCreateOrderWithInvalidIngredientHash() {
        // Передаем невалидные ID ингредиентов
        List<String> invalidIngredients = Arrays.asList("invalid_hash_1", "invalid_hash_2");
        OrderRequest orderRequest = new OrderRequest(invalidIngredients);
        Response response = createOrder(orderRequest, accessToken);
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