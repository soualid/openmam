package openmam.mediamicroservice.services;

import openmam.mediamicroservice.entities.Media;
import openmam.mediamicroservice.entities.MediaStream;
import openmam.mediamicroservice.repositories.LocationRepository;
import openmam.mediamicroservice.repositories.MediaRepository;
import openmam.mediamicroservice.repositories.MediaStreamRepository;
import openmam.mediamicroservice.repositories.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class MediaService {

    public void generateMasterPlaylist(Media media) {

        var buffer = """
        #EXTM3U
        #EXT-X-VERSION:4
                    
        # AUDIO groups
        """;
        MediaStream video = null;
        for (var element: media.getElements()) {
            for (var stream: element.getStreams()) {
                if (stream.getType() == MediaStream.Type.VIDEO) {
                    video = stream;
                    break;
                }
            }
        }
        for (var version: media.getVersions()) {
            var audio = version.getAudio();
            buffer += """
          #EXT-X-MEDIA:TYPE=AUDIO,GROUP-ID="audio",LANGUAGE="%s", NAME="%s",AUTOSELECT=YES,DEFAULT=YES,CHANNELS="2",URI="%s"
          """.formatted(version.getLanguage(), version.getName(),
                    "%s_audio_%s_%s/playlist.m3u8".formatted(media.getId(), audio.getMediaElement().getId(), audio.getStreamIndex()));
        }
        buffer += """
        #EXT-X-STREAM-INF:BANDWIDTH=6134000,RESOLUTION=1024x458,CODECS="avc1.4d001f,mp4a.40.2",AUDIO="audio"
        %s_video_%s_%s/playlist.m3u8            
        """.formatted(media.getId(), video.getMediaElement().getId(), video.getStreamIndex());

        try {
            // TODO Dynamic
            Files.write(Path.of("/Users/simon/san/variants/master_%s.m3u8".formatted(media.getId())), buffer.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
