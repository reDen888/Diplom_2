package models;

import java.util.List;

public class OrderResponse {
    private boolean success;
    private String name;
    private Order order;

    // Вложенный класс Order
    public static class Order {
        private List<Object> ingredients;
        private String _id;
        private String status;
        private int number;
        private String createdAt;
        private String updatedAt;

        // Геттеры и сеттеры
        public List<Object> getIngredients() { return ingredients; }
        public void setIngredients(List<Object> ingredients) { this.ingredients = ingredients; }

        public String get_id() { return _id; }
        public void set_id(String _id) { this._id = _id; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public int getNumber() { return number; }
        public void setNumber(int number) { this.number = number; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    }

    // Геттеры и сеттеры
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
}