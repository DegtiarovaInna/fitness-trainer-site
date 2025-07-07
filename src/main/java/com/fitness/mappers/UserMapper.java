package com.fitness.mappers;

import com.fitness.dto.UserDTO;
import com.fitness.models.User;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO userToUserDTO(User user);

   // User userDTOToUser(UserDTO userDTO);
}
