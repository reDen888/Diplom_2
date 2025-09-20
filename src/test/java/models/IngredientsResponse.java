package models;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IngredientsResponse {
    private boolean success;
    private List<Ingredient> data;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Ingredient {
        @JsonProperty("_id")
        private String id;
        private String name;
        private String type;
        private int proteins;
        private int fat;
        private int carbohydrates;
        private int calories;
        private int price;
        private String image;
        @JsonProperty("image_mobile")
        private String imageMobile;
        @JsonProperty("image_large")
        private String imageLarge;
        @JsonProperty("__v")
        private int v;
    }
}