package openmam.mediamicroservice.security.configuration;

import openmam.mediamicroservice.security.entities.Privilege;
import openmam.mediamicroservice.security.entities.Role;
import openmam.mediamicroservice.security.entities.User;
import openmam.mediamicroservice.security.repository.PrivilegeRepository;
import openmam.mediamicroservice.security.repository.RoleRepository;
import openmam.mediamicroservice.security.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;

@Configuration
public class LoadUsersAndRoles implements
        ApplicationListener<ContextRefreshedEvent> {

    boolean alreadySetup = false;

    @Autowired
    private UserRepository userRepository;
 
    @Autowired
    private RoleRepository roleRepository;
 
    @Autowired
    private PrivilegeRepository privilegeRepository;
 
    @Autowired
    private PasswordEncoder passwordEncoder;
 
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
 
        if (alreadySetup)
            return;

        var testRole = roleRepository.findByName("ROLE_USER");
        if (testRole != null) return;

        var readPrivilege = createPrivilegeIfNotFound("READ_PRIVILEGE");
        var writePrivilege = createPrivilegeIfNotFound("WRITE_PRIVILEGE");
        var locationPrivilege = createPrivilegeIfNotFound("ADMIN_LOCATION_PRIVILEGE");
        var metadataSchemaPrivilege = createPrivilegeIfNotFound("ADMIN_METADATA_SCHEMA_PRIVILEGE");
        var displayTasksPrivilege = createPrivilegeIfNotFound("DISPLAY_TASKS_PRIVILEGE");

        var adminPrivileges = Arrays.asList(readPrivilege, writePrivilege, locationPrivilege, metadataSchemaPrivilege, displayTasksPrivilege);
        createRoleIfNotFound("ROLE_ADMIN", adminPrivileges, 300, "status", "ACCEPTED");
        createRoleIfNotFound("ROLE_USER", Arrays.asList(readPrivilege), 200, "status", "REFUSED");
        createRoleIfNotFound("ROLE_PARTNER", Arrays.asList(readPrivilege), 100, "status", "null");

        var adminRole = roleRepository.findByName("ROLE_ADMIN");
        var userRole = roleRepository.findByName("ROLE_USER");
        var partnerRole = roleRepository.findByName("ROLE_PARTNER");

        var user = new User();
        user.setFirstName("Test");
        user.setLastName("Admin");
        user.setPassword(passwordEncoder.encode("test"));
        user.setEmail("admin@test.com");
        user.setRoles(Arrays.asList(adminRole));
        user.setEnabled(true);
        userRepository.save(user);

        user = new User();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword(passwordEncoder.encode("test"));
        user.setEmail("user@test.com");
        user.setRoles(Arrays.asList(userRole));
        user.setEnabled(true);
        userRepository.save(user);

        user = new User();
        user.setFirstName("Test");
        user.setLastName("Partner");
        user.setPassword(passwordEncoder.encode("test"));
        user.setEmail("partner@test.com");
        user.setRoles(Arrays.asList(partnerRole));
        user.setEnabled(true);
        userRepository.save(user);

        alreadySetup = true;
    }

    @Transactional
    Privilege createPrivilegeIfNotFound(String name) {
 
        var privilege = privilegeRepository.findByName(name);
        if (privilege == null) {
            privilege = new Privilege();
            privilege.setName(name);
            privilegeRepository.save(privilege);
        }
        return privilege;
    }

    @Transactional
    Role createRoleIfNotFound(
            String name, Collection<Privilege> privileges,
            long priority, String dashboardMetadataFilter, String dashboardMetadataFilterValue) {
 
        var role = roleRepository.findByName(name);
        if (role == null) {
            role = new Role();
            role.setName(name);
            role.setPrivileges(privileges);
            role.setPriority(priority);
            role.setDashboardMetadataFilter(dashboardMetadataFilter);
            role.setDashboardMetadataFilterValue(dashboardMetadataFilterValue);
            roleRepository.save(role);
        }
        return role;
    }
}
