package com.manal.expensemanager.service;

import com.manal.expensemanager.dto.UserRequestDTO;
import com.manal.expensemanager.model.User;

import java.util.List;

public interface UserService {
    User createUser(UserRequestDTO dto);
    List<User> getAllUsers();
}
