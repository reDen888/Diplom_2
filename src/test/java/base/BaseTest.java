package base;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import models.AuthResponse;
import models.User;
import org.junit.After;
import org.junit.BeforeClass;

import java.util.HashMap;
import java.util.Map;

public class BaseTest {
    // Константы для эндпоинтов
    protected static final String REGISTER_ENDPOINT = "/api/auth/register";
    protected static final String LOGIN_ENDPOINT = "/api/auth/login";
    protected static final String LOGOUT_ENDPOINT = "/api/auth/logout";
    protected static final String USER_ENDPOINT = "/api/auth/user";
    protected static final String INGREDIENTS_ENDPOINT = "/api/ingredients";
    protected static final String ORDERS_ENDPOINT = "/api/orders";

    protected static RequestSpecification requestSpec;
    // Используем "чистые" токены (без "Bearer ")
    protected String accessToken;
    protected String refreshToken;
    protected User testUser;

    @BeforeClass
    public static void globalSetUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .build();
    }

    // Удаление "Bearer " из токена
    protected String extractAccessToken(AuthResponse authResponse) {
        if (authResponse.getAccessToken() != null && authResponse.getAccessToken().startsWith("Bearer ")) {
            return authResponse.getAccessToken().substring(7); // Убираем "Bearer "
        }
        return authResponse.getAccessToken();
    }

    protected String extractRefreshToken(AuthResponse authResponse) {
        return authResponse.getRefreshToken();
    }

    protected Response createUser(User user) {
        return io.restassured.RestAssured.given()
                .spec(requestSpec)
                .body(user)
                .when()
                .post(REGISTER_ENDPOINT);
    }

    protected Response loginUser(User user) {
        return io.restassured.RestAssured.given()
                .spec(requestSpec)
                .body(user)
                .when()
                .post(LOGIN_ENDPOINT);
    }

    // Метод для выхода из системы
    protected Response logoutUser(String refreshToken) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("token", refreshToken);
        return io.restassured.RestAssured.given()
                .spec(requestSpec)
                .body(requestBody)
                .when()
                .post(LOGOUT_ENDPOINT);
    }

    protected Response deleteUser(String token) {
        return io.restassured.RestAssured.given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + token)
                .when()
                .delete(USER_ENDPOINT);
    }

    protected Response getIngredients() {
        return io.restassured.RestAssured.given()
                .spec(requestSpec)
                .when()
                .get(INGREDIENTS_ENDPOINT);
    }

    protected Response createOrder(Object orderRequest, String token) {
        if (token != null && !token.isEmpty()) {
            return io.restassured.RestAssured.given()
                    .spec(requestSpec)
                    .header("Authorization", "Bearer " + token) // Добавляем "Bearer " только здесь
                    .body(orderRequest)
                    .when()
                    .post(ORDERS_ENDPOINT);
        } else {
            return io.restassured.RestAssured.given()
                    .spec(requestSpec)
                    .body(orderRequest)
                    .when()
                    .post(ORDERS_ENDPOINT);
        }
    }

    @After
    public void tearDown() {
        // Удаление пользователя и выход из системы
        try {
            if (accessToken != null) {
                deleteUser(accessToken);
            }
        } catch (Exception e) {
            System.err.println("Warning: Failed to delete user in tearDown: " + e.getMessage());
        }

        try {
            if (refreshToken != null) {
                logoutUser(refreshToken); // Выход из системы после удаления
            }
        } catch (Exception e) {
            System.err.println("Warning: Failed to logout user in tearDown: " + e.getMessage());
            // Не выбрасываем исключение
        }
    }
}