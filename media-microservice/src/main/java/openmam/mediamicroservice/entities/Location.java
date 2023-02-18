package openmam.mediamicroservice.entities;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Location {

    public enum Type {
        LOCAL, S3
    }

    private @Id
    @GeneratedValue Long id;

    private String path;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    private Type type;
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
