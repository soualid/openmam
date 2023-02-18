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

Next things to be implemented:

- User management
- Custom user-defined metadatas schema management
- S3 locations support (ingest, playback)
- Move assets and variants between locations
- Globally improved UI
- Setup instructions
- Subtitle ingestion and playback support (EBU STL, TTML)
- Documentation

