package openmam.worker.commands;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import openmam.worker.dto.Task;
import org.buildobjects.process.ProcBuilder;
import org.springframework.shell.standard.ShellComponent;

import java.io.File;
import java.nio.file.Paths;
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
            case LOCAL -> moveFromS3ToLocal(destinationLocation, media);
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


    private static void moveFromS3ToLocal(Task.Location destinationLocation, Task.Media media) {
        var sourceLocation = media.elements.get(0).location;
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