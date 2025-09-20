package models;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private boolean success;
    private String name;
    private Order order;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Order {
        private List<Object> ingredients;
        @JsonProperty("_id")
        private String id;
        private String status;
        private int number;
        private String createdAt;
        private String updatedAt;
    }
}