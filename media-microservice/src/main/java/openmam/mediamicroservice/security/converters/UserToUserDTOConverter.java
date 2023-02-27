package openmam.mediamicroservice.security.converters;

import openmam.mediamicroservice.security.dto.UserDTO;
import openmam.mediamicroservice.security.entities.User;
import org.springframework.core.convert.converter.Converter;

import java.util.stream.Collectors;

public class UserToUserDTOConverter {

    public static UserDTO convert(User from) {
        var dto = new UserDTO();
        dto.id = from.getId();
        dto.email = from.getEmail();
        dto.firstName = from.getFirstName();
        dto.lastName = from.getLastName();
        dto.roles = from.getRoles().stream().map(r -> r.getName()).collect(Collectors.toList());
        return dto;
    }
}
