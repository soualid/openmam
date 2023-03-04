# Open MAM
 
An open MAM based on Spring Boot, Angular and FFMpeg.

https://user-images.githubusercontent.com/1830223/219972423-debfca73-4d68-4d92-9cf0-9652fc1697ab.mp4

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
- UI for metadata management (only available through the API for now)
- Metadata schema security based on users, roles or privileges (read, write)
- Workflow management
- Setup instructions
- Documentation
- Error checks :)

