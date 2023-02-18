import { Component, OnInit, Inject } from '@angular/core';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { Media, MediaElement, Audio } from 'src/app/models/media';
import { MediaService } from 'src/app/services/media.service';
import { MatDialog, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

declare var Hls:any;

export interface PlayerDialogData {
  url: string;
}
export interface MetadataDialogData {
  metadata: string;
}
export interface IngestDialogData {
  pattern: string;
}

@Component({
  selector: 'app-media-detail',
  templateUrl: './media-detail.component.html',
  styleUrls: ['./media-detail.component.sass']
})
export class MediaDetailComponent implements OnInit {

  constructor(private route: ActivatedRoute, 
    private mediaService: MediaService,
    public dialog: MatDialog) { }
  
  result: Media = {}

  ngOnInit(): void {
    
    this.route.paramMap.subscribe((params: ParamMap) => {
      const id:number = parseInt(params.get('id') ?? '-1')
      this.getMedia(id)
    });
  }

  openPlayer(): void {
    console.log('open player');

    const dialogRef = this.dialog.open(PlayerDialog, {
      data: { url: '' },
    });
    dialogRef.afterOpened().subscribe(() => {
      var video:HTMLVideoElement = document.getElementById('video') as HTMLVideoElement;
      // TODO dynamic
      var videoSrc = '/api/san/variants/master.m3u8';
      
      if (Hls.isSupported()) {
        var hls = new Hls();
        dialogRef.componentInstance.hls = hls;
        hls.on(Hls.Events.AUDIO_TRACKS_UPDATED, function(event:any, data:any) {
          console.log(event, data)
          for (const a of data.audioTracks) {
            dialogRef.componentInstance.audios.push({
              code: a.lang,
              name: a.name
            })
            if (a.autoselect) {
              dialogRef.componentInstance.selectedTrack = a.lang
            }
          }
        });
        hls.loadSource(videoSrc);
        hls.attachMedia(video);  
      } else {
        video.src = videoSrc;
      }
    })

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed', result);
    });
  }

  openIngestDialog(): void {
    console.log('openIngestDialog');

    const dialogRef = this.dialog.open(IngestDialog, {
      data: {},
    });

    dialogRef.afterClosed().subscribe(path => {
      console.log('The dialog was closed', path);
      this.mediaService.triggerMediaScan(this.result.id ?? -1, path, 1) // TODO dynamic
        .subscribe(result => this.getMedia(this.result.id ?? -1));
    });    
  }

  openMetadata(element:MediaElement): void {
    console.log('openMetadata');

    const dialogRef = this.dialog.open(MetadataDialog, {
      data: { metadata: element.fullMetadatas },
    });

    dialogRef.afterOpened().subscribe(() => {

    })

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed', result);
    });
  }  

  getMedia(id:number): void {
    this.mediaService.getMedia(id)
    .subscribe(result => this.result = result);
  }

}

@Component({
  selector: 'dialog-player',
  templateUrl: './dialog-player.html',
})
export class PlayerDialog {
  
  hls: any;

  constructor(
    public dialogRef: MatDialogRef<PlayerDialog>,
    @Inject(MAT_DIALOG_DATA) public data: PlayerDialogData,
  ) {}

  audios: Audio[] = []
  selectedTrack?: string = undefined
  
  onNoClick(): void {
    this.dialogRef.close();
  }

  audioTrackChanged(event:any): void {
    if (event.isUserInput) {
      console.log('audioTrackChanged', this.dialogRef.componentInstance.selectedTrack, this.hls.audioTracks);
      const selection = this.hls.audioTracks.find((a:any) => a.lang === this.dialogRef.componentInstance.selectedTrack)
      console.log(selection)
      this.hls.audioTrack = selection.id
    }
  }
}

@Component({
  selector: 'dialog-metadatas',
  templateUrl: './dialog-metadatas.html',
})
export class MetadataDialog {
  constructor(
    public dialogRef: MatDialogRef<MetadataDialog>,
    @Inject(MAT_DIALOG_DATA) public data: MetadataDialogData,
  ) {}

  onNoClick(): void {
    this.dialogRef.close();
  }
}

@Component({
  selector: 'dialog-ingest',
  templateUrl: './dialog-ingest.html',
})
export class IngestDialog {
  constructor(
    public dialogRef: MatDialogRef<IngestDialog>,
    @Inject(MAT_DIALOG_DATA) public data: IngestDialogData,
  ) {}

  onNoClick(): void {
    this.dialogRef.close();
  }
}