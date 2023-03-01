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

    @Column(columnDefinition = "boolean default false")
    private boolean hidden;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Enumerated(EnumType.STRING)
    private Type type;
    private String name;
    private String s3accessKey;
    private String s3accessSecret;

    public String getS3bucketName() {
        return s3bucketName;
    }

    public void setS3bucketName(String s3bucketName) {
        this.s3bucketName = s3bucketName;
    }

    private String s3bucketName;

    public String getS3accessKey() {
        return s3accessKey;
    }

    public void setS3accessKey(String s3accessKey) {
        this.s3accessKey = s3accessKey;
    }

    public String getS3accessSecret() {
        return s3accessSecret;
    }

    public void setS3accessSecret(String s3accessSecret) {
        this.s3accessSecret = s3accessSecret;
    }

    public String getS3region() {
        return s3region;
    }

    public void setS3region(String s3region) {
        this.s3region = s3region;
    }

    private String s3region;


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
