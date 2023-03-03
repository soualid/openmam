package openmam.worker.commands;

import fr.noop.subtitle.model.SubtitleParsingException;
import fr.noop.subtitle.stl.StlParser;
import fr.noop.subtitle.vtt.VttWriter;
import org.buildobjects.process.ProcBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.web.util.UriComponentsBuilder;
import openmam.worker.dto.Task;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
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
            case SUBTITLE -> handleSubtitleTranscoding(result);
        }

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
    private static void handleSubtitleTranscoding(Task task)  {
        // TODO handle other subtitle format, only EBU STL is supported for now
        var stlParser = new StlParser();
        var sourcePath = Paths.get(task.mediaElement.location.path)
                .resolve(task.mediaElement.filename);
        var destinationPath = Paths.get(task.mediaElement.location.path)
                .resolve("variants")
                .resolve(task.media.id + "_subtitle_"+ task.mediaElement.id + "_0");
        destinationPath.toFile().mkdirs();
        var destinationPlaylistPath = destinationPath.resolve("playlist.m3u8");
        destinationPath = destinationPath.resolve("subtitle.vtt");
        try {
            // Create VTT variant for HLS playback
            var subtitle = stlParser.parse(new FileInputStream(sourcePath.toFile()));
            var vttWriter = new VttWriter("utf-8");
            vttWriter.write(subtitle, new FileOutputStream(destinationPath.toFile()));

            // Create HLS playlist
            // TODO dynamic duration
            var hlsPlaylist = """
                    #EXTM3U
                    #EXT-X-VERSION:4
                    #EXT-X-TARGETDURATION:%s
                    #EXT-X-MEDIA-SEQUENCE:0
                    #EXTINF:%s,
                    #EXT-X-BYTERANGE:%s@0
                    subtitle.vtt
                    #EXT-X-ENDLIST                    
                    """.formatted("20", "20.000000", "3866784");
            Files.write(destinationPlaylistPath, hlsPlaylist.getBytes("UTF-8"));
        } catch (SubtitleParsingException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static void handleVideoTranscoding(Task task)  {
        // TODO handle S3
        var sourcePath = Paths.get(task.mediaElement.location.path)
                .resolve(task.mediaElement.filename);
        var destinationPath = Paths.get(task.mediaElement.location.path)
                .resolve("variants");
        var thumbnailPath = destinationPath.resolve(task.media.id + ".png");
        destinationPath = destinationPath.resolve(task.media.id + "_video_"+ task.mediaElement.id + "_" + task.mediaStream.streamIndex);
        destinationPath.toFile().mkdirs();

        // extract a thumbnail from the first frame
        var process = new ProcBuilder("ffmpeg", "-i",
                sourcePath.toAbsolutePath().toFile().toString(),
                "-frames:v", "1",
                "-f", "image2",
                "-update", "1",
                "-y",
                thumbnailPath.toAbsolutePath().toString())
                .withWorkingDirectory(destinationPath.toFile());
        log.info("output: " + process.getCommandLine());
        var output = process.run();
        log.info("ok");

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