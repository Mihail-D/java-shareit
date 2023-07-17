package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.EmailExistException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.util.UnionService;

import java.util.List;

@Service
@Transactional(readOnly = true)
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UnionService unionService;
    private final UserMapper userMapper = UserMapper.getInstance();

    @Transactional
    @Override
    public UserDto addUser(UserDto userDto) {

        User user = userMapper.returnUser(userDto);
        userRepository.save(user);

        return userMapper.returnUserDto(user);
    }

    @Transactional
    @Override
    public UserDto updateUser(UserDto userDto, long userId) {

        User user = userMapper.returnUser(userDto);
        user.setId(userId);

        unionService.checkUser(userId);
        User newUser = userRepository.findById(userId).get();

        if (user.getName() != null) {
            newUser.setName(user.getName());
        }

        if (user.getEmail() != null) {
            List<User> findEmail = userRepository.findByEmail(user.getEmail());

            if (!findEmail.isEmpty() && findEmail.get(0).getId() != userId) {
                throw new EmailExistException("there is already a user with an email " + user.getEmail());
            }
            newUser.setEmail(user.getEmail());
        }

        userRepository.save(newUser);

        return userMapper.returnUserDto(newUser);
    }

    @Transactional
    @Override
    public void deleteUser(long userId) {

        unionService.checkUser(userId);
        userRepository.deleteById(userId);
    }

    @Transactional(readOnly = true)
    @Override
    public UserDto getUserById(long userId) {

        unionService.checkUser(userId);
        return userMapper.returnUserDto(userRepository.findById(userId).get());
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getAllUsers() {

        return userMapper.returnUserDtoList(userRepository.findAll());
    }
}