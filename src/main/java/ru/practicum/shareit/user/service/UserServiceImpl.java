package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.AlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<User> findAllUsers() {
        log.debug("UserService: executed findAllUsers.");
        return userRepository.findAllUsers();
    }

    @Override
    public User findUserById(Long id) {
        User user = userRepository.findUserById(id).orElseThrow(
                () -> new NotFoundException(User.class.toString(), id)
        );
        log.debug("UserService: executed findUserById with {}.", user);
        return user;
    }

    @Override
    public User createUser(User user) {
        if (user.getId() != null && userRepository.userExists(user.getId())) {
            throw new AlreadyExistsException(User.class.toString(), user.getId());
        }
        if (userRepository.findAllUsers().contains(user)) {
            throw new AlreadyExistsException(User.class.toString(), user.getEmail());
        }
        user = userRepository.createUser(user);
        log.debug("UserService: executed createUser with {}.", user);
        return user;
    }

    @Override
    public User updateUser(Long userId, User user) {
        if (!userRepository.userExists(userId)) {
            throw new NotFoundException(User.class.toString(), userId);
        }
        User oldUser = findUserById(userId);
        Optional<User> emailUser = userRepository.findUserByEmail(user.getEmail());
        if (emailUser.isPresent() && !emailUser.get().getId().equals(userId)) {
            throw new AlreadyExistsException(User.class.toString(), user.getEmail());
        }
        if (user.getName() != null) {
            oldUser.setName(user.getName());
        }
        if (user.getEmail() != null) {
            oldUser.setEmail(user.getEmail());
        }
        log.debug("UserService: executed updateUser with {}.", user);
        return userRepository.updateUser(userId, oldUser);
    }

    @Override
    public void deleteUserById(Long userId) {
        if (!userRepository.userExists(userId)) {
            throw new NotFoundException(User.class.toString(), userId);
        }
        userRepository.deleteUserById(userId);
        log.debug("UserService: executed deleteUserById with ID {}.", userId);
    }
}