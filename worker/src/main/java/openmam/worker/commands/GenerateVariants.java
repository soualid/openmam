package openmam.worker.commands;

import org.buildobjects.process.ProcBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.web.util.UriComponentsBuilder;
import openmam.worker.dto.Task;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.logging.Logger;

import static java.lang.String.format;

@ShellComponent
public class GenerateVariants {
    private static Logger log = Logger.getLogger(GenerateVariants.class.getName());


    public static void handleTask(Task result)
    {
        switch (result.mediaStream.type) {
            case AUDIO -> handleAudioTranscoding(result);
            case VIDEO -> handleVideoTranscoding(result);
        }

        // TODO Update master playlist
/*
#EXTM3U
#EXT-X-VERSION:4

# AUDIO groups
#EXT-X-MEDIA:TYPE=AUDIO,GROUP-ID="audio",LANGUAGE="en", NAME="English",AUTOSELECT=YES,DEFAULT=YES,CHANNELS="2",URI="audio_0/playlist.m3u8"

#EXT-X-STREAM-INF:BANDWIDTH=6134000,RESOLUTION=1024x458,CODECS="avc1.4d001f,mp4a.40.2",AUDIO="audio"
video_0-0/playlist.m3u8
 */
    }

    private static void handleAudioTranscoding(Task task)  {
        // TODO handle S3
        var sourcePath = Paths.get(task.mediaElement.location.path)
                .resolve(task.mediaElement.filename);
        var tag = task.media.id + "_audio_" + task.mediaElement.id + "_" + task.mediaStream.streamIndex;
        var destinationPath = Paths.get(task.mediaElement.location.path)
                .resolve("variants")
                .resolve(tag);
        destinationPath.toFile().mkdirs();
        var output = new ProcBuilder("ffmpeg", "-i",
                sourcePath.toAbsolutePath().toFile().toString(),
                "-map", "0:" + task.mediaStream.streamIndex,
                "-c:a:0", "aac",
                "-b:a:0", "128k",
                "-ac", "2",
                "-f", "hls",
                "-hls_time", "2",
                "-hls_playlist_type", "vod",
                "-hls_flags", "independent_segments",
                "-hls_segment_type", "mpegts",
                "-hls_segment_filename",
                destinationPath.toAbsolutePath() + "/data%02d.ts",
                "-var_stream_map", "a:0",
                "-y",
                destinationPath.resolve("playlist.m3u8").toAbsolutePath().toString())
                .withNoTimeout().run();
        log.info("output: " + output.getCommandLine());
        log.info("ended");
    }
    private static void handleVideoTranscoding(Task task)  {
        // TODO handle S3
        var sourcePath = Paths.get(task.mediaElement.location.path)
                .resolve(task.mediaElement.filename);
        var destinationPath = Paths.get(task.mediaElement.location.path)
                .resolve("variants");
        var thumbnailPath = destinationPath.resolve(task.media.id + ".png");
        destinationPath = destinationPath.resolve(task.media.id + "_video_"+ task.mediaElement.id + "_0");
        destinationPath.toFile().mkdirs();

        // extract a thumbnail from the first frame
        var process = new ProcBuilder("ffmpeg", "-i",
                sourcePath.toAbsolutePath().toFile().toString(),
                "-frames:v", "1",
                "-f", "image2",
                "-y",
                thumbnailPath.toAbsolutePath().toString())
                .withNoTimeout()
                .withWorkingDirectory(destinationPath.toFile());
        log.info("output: " + process.getCommandLine());
        var output = process.run();

        output = new ProcBuilder("ffmpeg", "-i",
                sourcePath.toAbsolutePath().toFile().toString(),
                "-y",
                "-strftime_mkdir", "1",
                "-filter_complex", "[0:v]split=1[v1];[v1]scale=w=-2:h=720[v1out]",
                "-map", "[v1out]",
                "-c:v:0", "libx264",
                "-x264-params", "nal-hrd=cbr:force-cfr=1",
                "-b:v:0", "2M",
                "-maxrate:v:0", "2M",
                "-minrate:v:0", "2M",
                "-bufsize:v:0", "4M",
                "-preset", "slow",
                "-g", "48",
                "-sc_threshold", "0",
                "-keyint_min", "48",
                "-f", "hls",
                "-hls_time", "2",
                "-hls_playlist_type", "vod",
                "-hls_flags", "independent_segments",
                "-hls_segment_type", "mpegts",
                "-hls_segment_filename",
                "data%02d.ts",  // TODO stream variants (bitrates)
                "-var_stream_map", "v:0",
                destinationPath.resolve("playlist.m3u8").toAbsolutePath().toString())
                .withNoTimeout()
                .withWorkingDirectory(destinationPath.toFile())
                .run();
        log.info("output: " + output.getCommandLine());
        log.info("ended");
    }
}