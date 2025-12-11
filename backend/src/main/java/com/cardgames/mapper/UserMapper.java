package com.cardgames.mapper;

import com.cardgames.model.User;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface UserMapper {
    List<User> findAll();

    User findById(Long id);

    User findByEmail(String email);

    void insert(User user);
}
