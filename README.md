# Open MAM
 
An open MAM based on Spring Boot, Angular and FFMpeg.

[![Watch the video](https://user-images.githubusercontent.com/1830223/222972973-f903e17a-4205-46b1-9bad-9579d434932f.png)](https://youtu.be/q2l0poGq82s)

It contains the following components:

- A media microservice exposing medias related REST endpoints
- An asynchronous worker handling ingestion and transcoding jobs
- An Angular UI

Open MAM aims to provide an open and powerful ecosystem for your media assets, it actually supports:

- Ingestion of all major broadcast formats
- Can handle multiple input files per media, and properly indexes multiple stream per input file
- Asynchronous generation of multi video/audio and subtitles HLS stream for low-res playback, that are compatible with mobile playback
- Custom user-defined metadatas schema management, attached to either media or version
- Partner upload slot management (open an upload request, let a partner fullfil it through a cloud upload, then ingest the resulting file)
- Secured API and UI using JWT and Spring Security managed users, roles and privileges management 
- Locally attached, network, and cloud based (S3) locations support
- Move assets and variants between locations
- Subtitle ingestion, conversion and playback support (EBU STL, TTML)

Next things to be implemented:

- Better UI for users management (most of the features are only available through the API for now)
- Telestream Vantage workflow integration (for outgest and partner upload post-processing)
- Aspera server integration (for partner upload processing)
- UI for metadata management (only available through the API for now)
- Metadata schema security based on users, roles or privileges (read, write)
- Workflow management
- Setup instructions
- Documentation
- Error checks :)

