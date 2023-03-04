package openmam.mediamicroservice.security.entities;

import jakarta.persistence.*;

import java.util.Collection;

@Entity
public class Role {

    private @Id
    @GeneratedValue Long id;

    private String name;
    private String dashboardMetadataFilter;

    public String getDashboardMetadataFilter() {
        return dashboardMetadataFilter;
    }

    public void setDashboardMetadataFilter(String dashboardMetadataFilter) {
        this.dashboardMetadataFilter = dashboardMetadataFilter;
    }

    public String getDashboardMetadataFilterValue() {
        return dashboardMetadataFilterValue;
    }

    public void setDashboardMetadataFilterValue(String dashboardMetadataFilterValue) {
        this.dashboardMetadataFilterValue = dashboardMetadataFilterValue;
    }

    private String dashboardMetadataFilterValue;

    public Long getPriority() {
        return priority;
    }

    public void setPriority(Long priority) {
        this.priority = priority;
    }

    private Long priority = 0L;
    @ManyToMany(mappedBy = "roles")
    private Collection<User> users;

    @ManyToMany
    @JoinTable(
        name = "roles_privileges", 
        joinColumns = @JoinColumn(
          name = "role_id", referencedColumnName = "id"), 
        inverseJoinColumns = @JoinColumn(
          name = "privilege_id", referencedColumnName = "id"))
    private Collection<Privilege> privileges;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Collection<User> getUsers() {
        return users;
    }

    public void setUsers(Collection<User> users) {
        this.users = users;
    }

    public Collection<Privilege> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(Collection<Privilege> privileges) {
        this.privileges = privileges;
    }
}