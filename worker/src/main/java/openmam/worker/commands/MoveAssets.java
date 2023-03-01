package openmam.worker.commands;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import openmam.worker.dto.Task;
import org.springframework.shell.standard.ShellComponent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

@ShellComponent
public class MoveAssets {
    private static Logger log = Logger.getLogger(MoveAssets.class.getName());

    public static void handleTask(Task result)
    {
        var destinationLocation = result.destinationLocation;
        var media = result.media;

        switch (result.destinationLocation.type) {
            case S3 -> moveFromLocalToS3(destinationLocation, media);
            case LOCAL -> moveToLocal(destinationLocation, media);
        }

    }

    private static void moveFromLocalToS3(Task.Location destinationLocation, Task.Media media) {
        System.setProperty("aws.accessKeyId", destinationLocation.s3accessKey);
        System.setProperty("aws.secretKey", destinationLocation.s3accessSecret);
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .withRegion(destinationLocation.s3region)
                .build();
        TransferManager tm = TransferManagerBuilder.standard()
                .withS3Client(s3Client)
                .build();
        for (var mediaElement: media.elements) {
            try {
                var sourceFile = new File(mediaElement.location.path + mediaElement.filename);
                var upload = tm.upload(destinationLocation.s3bucketName,
                        destinationLocation.path + media.id + "-" + mediaElement.id,
                        sourceFile);
                upload.waitForCompletion();
                sourceFile.delete();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private static void moveToLocal(Task.Location destinationLocation, Task.Media media) {
        // for now all elements reside on the same location
        var sourceLocation = media.elements.get(0).location;

        switch (sourceLocation.type) {
            case S3 -> moveFromS3ToLocal(media, sourceLocation, destinationLocation);
            case LOCAL -> moveFromLocalToLocal(media, sourceLocation, destinationLocation);
        }
    }

    private static void moveFromLocalToLocal(Task.Media media, Task.Location sourceLocation, Task.Location destinationLocation) {
        try {
            for (var mediaElement : media.elements) {

                Files.move(Path.of(sourceLocation.path, mediaElement.filename),
                        Path.of(destinationLocation.path, mediaElement.filename));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void moveFromS3ToLocal(Task.Media media, Task.Location sourceLocation, Task.Location destinationLocation) {
        System.setProperty("aws.accessKeyId", sourceLocation.s3accessKey);
        System.setProperty("aws.secretKey", sourceLocation.s3accessSecret);
        AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                .withRegion(sourceLocation.s3region)
                .build();
        TransferManager tm = TransferManagerBuilder.standard()
                .withS3Client(s3Client)
                .build();
        for (var mediaElement: media.elements) {
            try {
                var download = tm.download(sourceLocation.s3bucketName,
                        sourceLocation.path + media.id + "-" + mediaElement.id,
                        new File(destinationLocation.path + mediaElement.filename));
                download.waitForCompletion();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            // TODO delete from S3
        }
    }

}