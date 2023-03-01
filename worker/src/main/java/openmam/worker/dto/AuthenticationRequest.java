package openmam.worker.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

public class AuthenticationRequest {

    public String login;
    public String password;

    public AuthenticationRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }
}


