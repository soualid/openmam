# Open MAM
 
An open MAM based on Spring Boot, Angular and FFMpeg.

https://user-images.githubusercontent.com/1830223/219972423-debfca73-4d68-4d92-9cf0-9652fc1697ab.mp4

It contains the following components:

- A media microservice exposing medias related REST endpoints
- An asynchronous worker handling ingestion and transcoding jobs
- An Angular UI

Open MAM aims to provide an open and powerful ecosystem for your media assets, it actually supports:

- Ingestion of all major broadcast formats
- Multiple input files or essence per assets
- Multiple essence containers indexation
- Asynchronous generation of multi video/audio and subtitles HLS stream for low-res playback, compatible with mobile playback
- Secured API and UI using spring security users, roles and privileges management 
- Custom user-defined metadatas schema management
- S3 locations support
- Move assets and variants between locations
- Subtitle ingestion and playback support (EBU STL, TTML)

Next things to be implemented:

- Partner upload slot
- UI for users management (only available through the API for now)
- UI for metadata management (only available through the API for now)
- Metadata schema security based on users, roles or privileges (read, write)
- Setup instructions
- Documentation

