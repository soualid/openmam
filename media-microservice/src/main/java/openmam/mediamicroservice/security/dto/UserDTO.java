package openmam.mediamicroservice.security.dto;

import java.util.List;

public class UserDTO {
    public Long id;
    public String email;
    public String firstName;
    public String lastName;
    public List<String> roles;
}
