package api;

import data.Endpoints;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import models.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiClient {
    private final RequestSpecification requestSpec;

    public ApiClient(RequestSpecification requestSpec) {
        this.requestSpec = requestSpec;
    }

    public Response createUser(User user) {
        return RestAssured.given()
                .spec(requestSpec)
                .body(user)
                .when()
                .post(Endpoints.REGISTER);
    }

    public Response loginUser(User user) {
        return RestAssured.given()
                .spec(requestSpec)
                .body(user)
                .when()
                .post(Endpoints.LOGIN);
    }

    public Response logoutUser(String refreshToken) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("token", refreshToken);
        return RestAssured.given()
                .spec(requestSpec)
                .body(requestBody)
                .when()
                .post(Endpoints.LOGOUT);
    }

    public Response deleteUser(String accessToken) {
        return RestAssured.given()
                .spec(requestSpec)
                .header("Authorization", "Bearer " + accessToken)
                .when()
                .delete(Endpoints.USER);
    }

    public Response getIngredients() {
        return RestAssured.given()
                .spec(requestSpec)
                .when()
                .get(Endpoints.INGREDIENTS);
    }

    public Response createOrder(List<String> ingredients, String accessToken) {
        Map<String, Object> orderRequestBody = new HashMap<>();
        orderRequestBody.put("ingredients", ingredients);
        if (accessToken != null && !accessToken.isEmpty()) {
            return RestAssured.given()
                    .spec(requestSpec)
                    .header("Authorization", "Bearer " + accessToken)
                    .body(orderRequestBody)
                    .when()
                    .post(Endpoints.ORDERS);
        } else {
            return RestAssured.given()
                    .spec(requestSpec)
                    .body(orderRequestBody)
                    .when()
                    .post(Endpoints.ORDERS);
        }
    }
}