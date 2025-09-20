package data;

import net.datafaker.Faker;

import java.util.Locale;

public class TestData {
    private static final Faker faker = new Faker(new Locale("ru"));

    public static String generateEmail() {
        return "testuser_" + System.currentTimeMillis() + "_" + faker.internet().safeEmailAddress();
    }

    public static String generatePassword() {
        return faker.internet().password(8, 15, true, true, true);
    }

    public static String generateName() {
        return faker.name().fullName();
    }
}