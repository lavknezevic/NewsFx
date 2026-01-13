package at.newsfx.fhtechnikum.newsfx.config;

import at.newsfx.fhtechnikum.newsfx.persistence.Database;
import at.newsfx.fhtechnikum.newsfx.persistence.FavoritesRepository;
import at.newsfx.fhtechnikum.newsfx.persistence.InternalNewsRepository;
import at.newsfx.fhtechnikum.newsfx.persistence.UserRepository;
import at.newsfx.fhtechnikum.newsfx.security.PasswordHasher;
import at.newsfx.fhtechnikum.newsfx.service.FavoritesService;
import at.newsfx.fhtechnikum.newsfx.service.auth.AuthService;
import at.newsfx.fhtechnikum.newsfx.service.news.internal.InternalNewsService;
import at.newsfx.fhtechnikum.newsfx.service.user.UserService;

public final class AppContext {

    private static volatile AppContext instance;

    private final UserRepository userRepository;
    private final InternalNewsRepository internalNewsRepository;
    private final FavoritesRepository favoritesRepository;
    private final AuthService authService;
    private final UserService userService;
    private final InternalNewsService internalNewsService;
    private final FavoritesService favoritesService;

    private AppContext() {
        Database.initSchema();

        this.userRepository = new UserRepository();
        this.internalNewsRepository = new InternalNewsRepository();
        this.favoritesRepository = new FavoritesRepository();
        this.authService = new AuthService(userRepository);
        this.userService = new UserService(userRepository, authService);
        this.internalNewsService = new InternalNewsService(authService, internalNewsRepository);
        this.favoritesService = new FavoritesService(favoritesRepository);

        seedUsersIfEmpty();
    }

    public static AppContext get() {
        AppContext local = instance;
        if (local == null) {
            synchronized (AppContext.class) {
                local = instance;
                if (local == null) {
                    local = new AppContext();
                    instance = local;
                }
            }
        }
        return local;
    }

    public AuthService authService() {
        return authService;
    }

    public UserService userService() {
        return userService;
    }

    public InternalNewsService internalNewsService() {
        return internalNewsService;
    }

    public FavoritesService favoritesService() {
        return favoritesService;
    }

    public FavoritesRepository favoritesRepository() {
        return favoritesRepository;
    }

    private void seedUsersIfEmpty() {
        if (userRepository.countUsers() > 0) {
            return;
        }

        // Demo accounts for local showcase
        userRepository.insert("admin", PasswordHasher.hash("admin"), at.newsfx.fhtechnikum.newsfx.auth.Role.ADMIN);
        userRepository.insert("editor", PasswordHasher.hash("editor"), at.newsfx.fhtechnikum.newsfx.auth.Role.EDITOR);
        userRepository.insert("user", PasswordHasher.hash("user"), at.newsfx.fhtechnikum.newsfx.auth.Role.USER);
    }
}
