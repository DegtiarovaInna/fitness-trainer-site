package com.fitness.mappers;

import com.fitness.dto.UserDTO;
import com.fitness.models.User;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring") //позволяет MapStruct создать Spring Bean для этого интерфейса.
public interface UserMapper {


    // Маппинг из User в UserDTO

    UserDTO userToUserDTO(User user);

    // Маппинг из UserDTO в User
    User userDTOToUser(UserDTO userDTO);
}
