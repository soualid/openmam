import { Component, OnInit, Inject } from '@angular/core';
import { Observable, of, map } from 'rxjs';
import { startWith, switchMap, distinctUntilChanged, debounceTime } from 'rxjs/operators';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { Media, MediaElement, Audio, Location, MediaStream, MediaVersion } from 'src/app/models/media';
import { MediaService } from 'src/app/services/media.service';
import { MatDialog, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MetadataService } from 'src/app/services/metadata.service';
import { Page } from 'src/app/models/page';
import { MetadataType, MetadataGroup, MetadataAttachmentType, MetadataReference } from 'src/app/models/metadata';
import { FormBuilder } from '@angular/forms';
import { LocationService } from 'src/app/services/location.service';

declare var Hls:any;

export interface PlayerDialogData {
  url: string;
}
export interface MetadataDialogData {
  metadata: string;
}
export interface CreateVersionDialogData {
  availableLanguages: any[]
  availableAudios: any[]
  availableSubtitles: any[]
  selectedAudio: number
  name: string
  selectedLanguage: string
}
export interface MoveMediaDialogData {
  locationId: string;
  locations?: Location[];
}
export interface UserMetadataDialogData {
  metadataGroups: Page<MetadataGroup>;
  media: Media;
  metadataForm: any;
  metadataTypes: typeof MetadataType;
  metadataAttachmentTypes: typeof MetadataAttachmentType;
  filteredOptions: Map<string, Observable<Array<any>>>;
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
    private metadataService: MetadataService,
    private locationService: LocationService,
    public dialog: MatDialog,
    private fb: FormBuilder) { }
  
  result: Media = {}
  metadataGroups:Page<MetadataGroup> = {};
  metadataTypes:typeof MetadataType = MetadataType;
  metadataAttachmentTypes:typeof MetadataAttachmentType = MetadataAttachmentType;
  metadataForm:any;
  selectedVersion:MediaVersion = {};
  loading:boolean = true

  ngOnInit(): void {
    
    this.route.paramMap.subscribe((params: ParamMap) => {
      const id:number = parseInt(params.get('id') ?? '-1')
      this.getMedia(id)
      this.getMetadataGroups(id)
    });
  }

  selectVersion(version:MediaVersion) {
    this.selectedVersion = version
  }

  openPlayer(): void {
    console.log('open player');

    const dialogRef = this.dialog.open(PlayerDialog, {
      data: { url: '' },
    });
    dialogRef.afterOpened().subscribe(() => {
      var video:HTMLVideoElement = document.getElementById('video') as HTMLVideoElement;
      var videoSrc = `/api/san/variants/master_${this.result.id}.m3u8`;
      
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
  
  filterMetadataReference(val: string, metadataReferenceId:number): Observable<any[]> {
    console.log('filter', val)

    // call the service which makes the http-request
    // TODO pas en dur
    return this.metadataService.getAutocompleteData(metadataReferenceId)
     .pipe(
       map(response => response.filter(option => { 
        console.log('option', option)
         return option.representation?.toLowerCase().indexOf(val.toLowerCase()) === 0
       }))
     )
  }  

  openUserMetadataDialog(): void {
    console.log('openIngestDialog');

    // populate form
    console.log(this.metadataForm)
    const filteredOptions = new Map<string, any>()
    if (this.metadataGroups.content) {
      console.log('metadataGroups: ', this.metadataGroups)
      let group:{[k: string]: any} = {}
      if (this.metadataGroups.content) {
        for (const metadataGroup of this.metadataGroups.content) {
          let target = metadataGroup.attachmentType === MetadataAttachmentType.MEDIA ? this.result :
            metadataGroup.attachmentType === MetadataAttachmentType.VERSION ? this.selectedVersion : 
            this.selectedVersion
          if (metadataGroup.metadatas) {
            let subgroup:{[k: string]: any} = {}
            for (const metadataDefinition of metadataGroup.metadatas) {
              // preset values if set
              subgroup[`${metadataDefinition.name}`] = target.dynamicMetadatas != null ? 
                [target.dynamicMetadatas[metadataDefinition.name]] : ['']

              // prepare autocomplete references 
              if (metadataDefinition.type === MetadataType.REFERENCE) {
                console.log('REFERENCE', metadataDefinition)
                filteredOptions.set(metadataGroup.id + '.' + metadataDefinition.name, [])
              }
            }
            subgroup['targetType'] = metadataGroup.attachmentType
            subgroup['targetId'] = target.id
            group[metadataGroup.id] = this.fb.group(subgroup)
          }
        }
      }
      console.log(group)
      this.metadataForm = this.fb.group(group)
      console.log(this.metadataForm)
      
      console.log('setting autocomplete listeners')
      for (const key of filteredOptions.keys()) {
        const field = this.metadataForm.get(key)
        console.log('key', key, field)
        filteredOptions.set(key, field.valueChanges
          .pipe(
            startWith(''),
            debounceTime(400),
            distinctUntilChanged(),
            switchMap(val => {
              console.log('val', val)
              // TODO pas en dur
              const r = this.filterMetadataReference(val+'' || '', 52)
              console.log('r', r)
              return r
            })       
          ))
      }      
    }

    const dialogRef = this.dialog.open(UserMetadataDialog, {
      data: {
        filteredOptions,
        metadataGroups: this.metadataGroups,
        media: this.result,
        metadataForm: this.metadataForm,
        metadataTypes: this.metadataTypes,
        metadataAttachmentTypes: this.metadataAttachmentTypes,
      },
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed', result);
      if (result) {
        console.log(result.value);
        this.metadataService.persistMetadatasForEntity(
          this.metadataForm.value).subscribe(() => {
            this.getMedia(this.result.id ?? -1)
          });
      }
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

  openMoveMediaDialog(): void {
    console.log('openMoveMediaDialog');
    let locationId = null;
    if (this.result?.elements?.length) (
      // for now, all elements of a media share the same location
      locationId = this.result.elements[0].location?.id
    )
    const dialogRef = this.dialog.open(MoveMediaDialog, {
      data: {
        locationId
      },
    });
    dialogRef.afterOpened().subscribe(() => {
      this.locationService.getLocations().subscribe(locations => {
        dialogRef.componentInstance.data.locations = locations.content
      })
    })
    dialogRef.afterClosed().subscribe(locationId => {
      console.log('The dialog was closed', locationId);
      if (!locationId) return
      this.mediaService.triggerMediaMove(this.result.id ?? -1, locationId)
        .subscribe(() => this.getMedia(this.result.id ?? -1));
    });    
  }

  openCreateVersionDialog(): void {
    console.log('openCreateVersionDialog');
    const availableAudios:any[] = []
    const availableSubtitles:any[] = []
    if (this.result?.elements) {
      for (const element of this.result?.elements) {
        if (element.streams) {
          for (const stream of element.streams) {
            if (stream.type === "AUDIO")
              availableAudios.push({...stream, filename:element.filename})
            else if (stream.type === "SUBTITLE")
              availableSubtitles.push({...stream, filename:element.filename})
          }
        }
      }
    }
    
    // console.log(availableAudios) 
    if (availableAudios) {
      const dialogRef = this.dialog.open(CreateVersionDialog, {
        data: {
          availableAudios,
          availableLanguages: [
            {
              code: 'en',
              name: 'English'
            },
            {
              code: 'fr',
              name: 'French'
            }
          ]
        },
      });
      dialogRef.afterOpened().subscribe(() => {
        
      })
      dialogRef.afterClosed().subscribe(dialogResult => {
        console.log('The dialog was closed', dialogResult);
        if (dialogResult && this.result) {
          console.log(dialogResult.name);
          const version:MediaVersion = {
            name: dialogResult.name,
            language: dialogResult.selectedLanguage
          };
          this.mediaService.createVersion(version, this.result.id!, dialogResult.selectedAudio).subscribe(() => {
              this.getMedia(this.result.id ?? -1)
            });
        }
      });  
    }  
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
      .subscribe(result => {
        this.result = result
        // select first version automatically
        if (!this.selectedVersion.id && this.result?.versions?.length) {
          this.selectedVersion = this.result.versions[0]
        }
        this.loading = false;
      })
  }

  getMetadataGroups(mediaId:Number): void {
    this.metadataService.getMetadataGroups()
      .subscribe(metadataGroups => {
        this.metadataGroups = metadataGroups
      })
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
  selector: 'dialog-move-media',
  templateUrl: './dialog-move-media.html',
})
export class MoveMediaDialog {
  constructor(
    public dialogRef: MatDialogRef<MoveMediaDialog>,
    @Inject(MAT_DIALOG_DATA) public data: MoveMediaDialogData,
  ) {}

  onNoClick(): void {
    this.dialogRef.close();
  }
}

@Component({
  styleUrls: ['./dialog-user-metadatas.sass'],
  selector: 'dialog-user-metadatas',
  templateUrl: './dialog-user-metadatas.html',
})
export class UserMetadataDialog {
  constructor(
    public dialogRef: MatDialogRef<UserMetadataDialog>,
    @Inject(MAT_DIALOG_DATA) public data: UserMetadataDialogData,
  ) {}
  displayAutocomplete(value:MetadataReference) {
    console.log('displayAutocomplete', value)
    return value?.representation ?? ''
  }
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

@Component({
  selector: 'dialog-create-version',
  templateUrl: './dialog-create-version.html',
  styleUrls: ['./dialog-create-version.sass']
})
export class CreateVersionDialog {
  constructor(
    public dialogRef: MatDialogRef<CreateVersionDialog>,
    @Inject(MAT_DIALOG_DATA) public data: CreateVersionDialogData,
  ) {}

  onNoClick(): void {
    this.dialogRef.close();
  }
}