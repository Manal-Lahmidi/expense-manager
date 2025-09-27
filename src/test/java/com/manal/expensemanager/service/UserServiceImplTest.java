package com.manal.expensemanager.service;

import com.manal.expensemanager.dto.UserRequestDTO;
import com.manal.expensemanager.model.Role;
import com.manal.expensemanager.model.User;
import com.manal.expensemanager.repository.UserRepository;
import com.manal.expensemanager.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.mock;

class UserServiceImplTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl(userRepository);
    }

    @Test
    void createUser_shouldPersist_whenEmailNotUsed_andRoleValid() {
        // given
        var dto = UserRequestDTO.builder()
                .fullName("Alice Doe")
                .email("alice@test.io")
                .role("USER")
                .build();

        given(userRepository.existsByEmail("alice@test.io")).willReturn(false);

        // return value of save()
        var saved = User.builder()
                .id(10L)
                .fullName("Alice Doe")
                .email("alice@test.io")
                .role(Role.USER)
                .build();
        given(userRepository.save(any(User.class))).willReturn(saved);

        // when
        User result = service.createUser(dto);

        // then
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getRole()).isEqualTo(Role.USER);
        assertThat(result.getEmail()).isEqualTo("alice@test.io");

        // and: capture what we attempted to save
        var captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User toSave = captor.getValue();
        assertThat(toSave.getFullName()).isEqualTo("Alice Doe");
        assertThat(toSave.getRole()).isEqualTo(Role.USER);
    }

    @Test
    void createUser_shouldFail_whenEmailAlreadyExists() {
        // given
        var dto = UserRequestDTO.builder()
                .fullName("Bob")
                .email("bob@test.io")
                .role("ADMIN")
                .build();
        given(userRepository.existsByEmail("bob@test.io")).willReturn(true);

        // when / then
        assertThatThrownBy(() -> service.createUser(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_shouldFail_whenRoleInvalid() {
        // given
        var dto = UserRequestDTO.builder()
                .fullName("Cara")
                .email("cara@test.io")
                .role("BOSS") // not in enum
                .build();
        given(userRepository.existsByEmail("cara@test.io")).willReturn(false);

        // when / then
        assertThatThrownBy(() -> service.createUser(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid role");
        verify(userRepository, never()).save(any());
    }

    @Test
    void getAllUsers_shouldReturnRepositoryResult() {
        // given
        var u1 = User.builder().id(1L).fullName("A").email("a@x").role(Role.USER).build();
        var u2 = User.builder().id(2L).fullName("B").email("b@x").role(Role.ADMIN).build();
        given(userRepository.findAll()).willReturn(List.of(u1, u2));

        // when
        var list = service.getAllUsers();

        // then
        assertThat(list).hasSize(2).extracting(User::getId).containsExactly(1L, 2L);
        verify(userRepository).findAll();
    }
}
