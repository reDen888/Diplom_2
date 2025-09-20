package base;

import api.ApiClient;
import io.qameta.allure.Step;
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

public class BaseTest {
    protected static RequestSpecification requestSpec;
    protected ApiClient apiClient;
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

    @Step("Извлечение access токена из ответа")
    protected String extractAccessToken(AuthResponse authResponse) {
        if (authResponse.getAccessToken() != null && authResponse.getAccessToken().startsWith("Bearer ")) {
            return authResponse.getAccessToken().substring(7); // Убираем "Bearer "
        }
        return authResponse.getAccessToken();
    }

    @Step("Извлечение refresh токена из ответа")
    protected String extractRefreshToken(AuthResponse authResponse) {
        return authResponse.getRefreshToken();
    }

    @Step("Создание пользователя")
    protected Response createUser(User user) {
        if (apiClient == null) {
            throw new IllegalStateException("ApiClient не инициализирован");
        }
        return apiClient.createUser(user);
    }

    @Step("Логин пользователя")
    protected Response loginUser(User user) {
        if (apiClient == null) {
            throw new IllegalStateException("ApiClient не инициализирован");
        }
        return apiClient.loginUser(user);
    }

    @Step("Логаут пользователя")
    protected Response logoutUser(String refreshToken) {
        if (apiClient == null) {
            throw new IllegalStateException("ApiClient не инициализирован");
        }
        return apiClient.logoutUser(refreshToken);
    }

    @Step("Удаление пользователя")
    protected Response deleteUser(String accessToken) {
        if (apiClient == null) {
            throw new IllegalStateException("ApiClient не инициализирован");
        }
        return apiClient.deleteUser(accessToken);
    }

    @Step("Получение списка ингредиентов")
    protected Response getIngredients() {
        if (apiClient == null) {
            throw new IllegalStateException("ApiClient не инициализирован");
        }
        return apiClient.getIngredients();
    }

    @Step("Создание заказа")
    protected Response createOrder(Object orderRequest, String accessToken) {
        if (apiClient == null) {
            throw new IllegalStateException("ApiClient не инициализирован. Убедитесь, что он создан в @Before методе теста.");
        }
        if (orderRequest instanceof java.util.List) {
            @SuppressWarnings("unchecked")
            java.util.List<String> ingredientsList = (java.util.List<String>) orderRequest;
            return apiClient.createOrder(ingredientsList, accessToken);
        } else if (orderRequest instanceof models.OrderRequest) {
            return apiClient.createOrder(((models.OrderRequest) orderRequest).getIngredients(), accessToken);
        } else {
            throw new IllegalArgumentException("Неподдерживаемый тип для orderRequest: " + (orderRequest != null ? orderRequest.getClass().getName() : "null"));
        }
    }

    @After
    public void tearDown() {
        try {
            if (accessToken != null) {
                deleteUser(accessToken);
            }
        } catch (Exception e) {
            System.err.println("Warning: Failed to delete user: " + e.getMessage());
        }
        try {
            if (refreshToken != null) {
                logoutUser(refreshToken);
            }
        } catch (Exception e) {
            System.err.println("Warning: Failed to logout user: " + e.getMessage());
        }
    }
}