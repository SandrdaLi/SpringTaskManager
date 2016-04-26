package com.spring.task.service;

import java.util.List;

import com.spring.task.domain.File;
import com.spring.task.domain.User;

public interface UserService {
	User findById(Long userId);

	User findByUserName(String userName);

	User createNewUser(User user);

	void createUsers(List<User> users);

	void updateUser(User user);

	void deleteUser(User user);

	List<User> findAllUsers();

	File addProfileImage(Long userId, String fileName);

	void deleteProfileImage(Long userId);

}
