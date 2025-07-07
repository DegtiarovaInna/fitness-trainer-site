package com.fitness.mappers;

import com.fitness.dto.StudioDTO;
import com.fitness.models.Studio;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface StudioMapper {

    @Mapping(target = "adminId", expression = "java(studio.getAdmin() != null ? studio.getAdmin().getId() : null)")
    StudioDTO studioToStudioDTO(Studio studio);

}
