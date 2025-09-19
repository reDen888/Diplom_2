package models;

public class AuthResponse {
    private boolean success;
    private String accessToken;
    private String refreshToken;
    //User с заглавной буквы
    private User user;

    //Геттеры и сеттеры
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    //User с заглавной буквы
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}