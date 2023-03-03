package openmam.mediamicroservice.entities;

import jakarta.persistence.*;

@Entity
public class Configuration {

    public enum Key {
        PARTNER_UPLOAD_LOCATION,
        PARTNER_UPLOAD_INGEST_LOCATION
    }

    private @Id
    @GeneratedValue Long id;

    public String value;

    @Enumerated(EnumType.STRING)
    public Key key;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Key getKey() {
        return key;
    }

    public void setKey(Key key) {
        this.key = key;
    }
}
