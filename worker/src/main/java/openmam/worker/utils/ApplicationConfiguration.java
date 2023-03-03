package openmam.worker.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class ApplicationConfiguration {

    @Value("${openmam.base.url}")
    public String mediaServiceHost;

    @Value("${openmam.login}")
    public String openMamLogin;

    @Value("${openmam.password}")
    public String openMamPassword;

}
