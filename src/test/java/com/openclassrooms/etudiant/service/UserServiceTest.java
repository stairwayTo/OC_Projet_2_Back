package com.openclassrooms.etudiant.service;

import com.openclassrooms.etudiant.entities.User;
import com.openclassrooms.etudiant.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String LOGIN = "LOGIN";
    private static final String PASSWORD = "PASSWORD";
    private static final String HASHED_PASSWORD = "HASHED_PASSWORD";
    private static final String TOKEN = "TOKEN";

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private UserService userService;

    private User buildUser() {
        User user = new User();
        user.setFirstName(FIRST_NAME);
        user.setLastName(LAST_NAME);
        user.setLogin(LOGIN);
        user.setPassword(PASSWORD);
        return user;
    }

    @Test
    public void register_null_user_throws_IllegalArgumentException() {
        // GIVEN

        // THEN
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.register(null));
    }

    @Test
    public void register_user_already_exist_throws_IllegalArgumentException() {
        // GIVEN
        User user = buildUser();
        when(userRepository.findByLogin(any())).thenReturn(Optional.of(user));

        // THEN
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.register(user));
    }

    @Test
    public void register_happy_path() {
        // GIVEN
        User user = buildUser();
        when(passwordEncoder.encode(PASSWORD)).thenReturn(HASHED_PASSWORD);
        when(userRepository.findByLogin(any())).thenReturn(Optional.empty());

        // WHEN
        userService.register(user);

        // THEN
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPassword()).isEqualTo(HASHED_PASSWORD);
        assertThat(userCaptor.getValue()).isEqualTo(user);
    }

    @Test
    public void login_null_login_throws_IllegalArgumentException() {
        // GIVEN

        // THEN
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.login(null, PASSWORD));
    }

    @Test
    public void login_null_password_throws_IllegalArgumentException() {
        // GIVEN

        // THEN
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.login(LOGIN, null));
    }

    @Test
    public void login_user_not_present_throws_IllegalArgumentException() {
        // GIVEN
        when(userRepository.findByLogin(any())).thenReturn(Optional.empty());

        // THEN
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.login(LOGIN, PASSWORD));
    }

    @Test
    public void login_password_not_matches_throws_IllegalArgumentException() {
        // GIVEN
        User user = buildUser();
        user.setPassword(HASHED_PASSWORD);
        when(userRepository.findByLogin(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, HASHED_PASSWORD)).thenReturn(false);

        // THEN
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> userService.login(LOGIN, PASSWORD));
    }

    @Test
    public void login_happy_path() {
        // GIVEN
        User user = buildUser();
        user.setPassword(HASHED_PASSWORD);
        when(userRepository.findByLogin(any())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(PASSWORD, HASHED_PASSWORD)).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn(TOKEN);

        //when
        String token = userService.login(LOGIN, PASSWORD);

        //then
        assertThat(token).isEqualTo(TOKEN);

    }
}