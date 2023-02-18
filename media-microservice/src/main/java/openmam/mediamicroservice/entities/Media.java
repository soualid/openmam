package openmam.mediamicroservice.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.List;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Media {

    private @Id
    @GeneratedValue Long id;

    private String name;

    public List<MediaElement> getElements() {
        return elements;
    }

    public void setElements(List<MediaElement> elements) {
        this.elements = elements;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "media")
    private List<MediaElement> elements;

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

}
