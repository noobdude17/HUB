package com.fptu.hubcinemas.config;

public final class ApiEndpoints {

    private ApiEndpoints() { /* prevent instantiation */ }

    // API base
    private static final String API_BASE = "/api/v1";

    // Auth
    public static final String AUTH_BASE = API_BASE + "/auth";
    public static final String AUTH_LOGIN = AUTH_BASE + "/login";
    public static final String AUTH_REGISTER = AUTH_BASE + "/register";
    public static final String AUTH_VERIFY = AUTH_BASE + "/verify"; // e.g. ?token={token}
    public static final String AUTH_LOGOUT = AUTH_BASE + "/logout";
    public static final String AUTH_REFRESH = AUTH_BASE + "/refresh";

    // Password reset / recovery
    public static final String PASSWORD_BASE = AUTH_BASE + "/password";
    public static final String PASSWORD_RESET_REQUEST = PASSWORD_BASE + "/reset/request";
    public static final String PASSWORD_RESET = PASSWORD_BASE + "/reset"; // e.g. POST with token

    // Users
    public static final String USERS = API_BASE + "/users";
    public static final String USER_BY_ID = USERS + "/{id}";
    public static final String USER_PROFILE = USER_BY_ID + "/profile";
    public static final String USER_AVATAR = USER_BY_ID + "/avatar";
    public static final String USER_UPDATE_PROFILE = USER_PROFILE + "/update";

    // Admin
    public static final String ADMIN_BASE = API_BASE + "/admin";
    public static final String ADMIN_USERS = ADMIN_BASE + "/users";
    public static final String ADMIN_USER_BY_ID = ADMIN_USERS + "/{id}";
    public static final String ADMIN_DELETE_USER = ADMIN_USER_BY_ID + "/delete";
    public static final String ADMIN_STATS = ADMIN_BASE + "/stats";

    // Movies / Shows
    public static final String MOVIES = API_BASE + "/movies";
    public static final String MOVIE_BY_ID = MOVIES + "/{movieId}";
    public static final String SHOWS = API_BASE + "/shows";
    public static final String SHOW_BY_ID = SHOWS + "/{showId}";

    // Booking / Orders
    public static final String BOOKINGS = API_BASE + "/bookings";
    public static final String BOOKING_BY_ID = BOOKINGS + "/{bookingId}";
    public static final String BOOKING_CREATE = BOOKINGS + "/create";
    public static final String BOOKING_CANCEL = BOOKINGS + "/{bookingId}/cancel";

    // Payment
    public static final String PAYMENTS = API_BASE + "/payments";
    public static final String PAYMENT_CALLBACK = PAYMENTS + "/callback";

    // Health / Info
    public static final String HEALTH = API_BASE + "/health";
    public static final String INFO = API_BASE + "/info";
}

