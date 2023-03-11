import { Component, OnInit, Inject, HostListener } from '@angular/core';
import { Observable, of, map } from 'rxjs';
import { startWith, switchMap, distinctUntilChanged, debounceTime, delay } from 'rxjs/operators';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { Media, MediaElement, Audio, Location, MediaStream, MediaVersion, Subtitle } from 'src/app/models/media';
import { MediaService } from 'src/app/services/media.service';
import { MatDialog, MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MetadataService } from 'src/app/services/metadata.service';
import { Page } from 'src/app/models/page';
import { MetadataType, MetadataGroup, MetadataAttachmentType, MetadataReference } from 'src/app/models/metadata';
import { FormBuilder, FormControl } from '@angular/forms';
import { LocationService } from 'src/app/services/location.service';
import { User } from 'src/app/models/user';
import { UserService } from 'src/app/services/user.service';
import { PartnerUploadService } from 'src/app/services/partner.upload.service';
import { UploadRequestStatus } from 'src/app/models/upload.request';
import { Task } from 'src/app/models/task';
import { OutgestService } from 'src/app/services/outgest.service';
import { OutgestProfile } from 'src/app/models/outgest.profile';

declare var Hls:any;

export interface RequestPartnerUploadData {
  partnerId: number;
}
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
export interface OutgestDialogData {
  locations: Location[];
  destinationLocation: Location;
  selectedOutgestProfile: OutgestProfile;
  outgestProfiles?: OutgestProfile[];
  availableVideoStreams: MediaStream[];
  selectedVideoStreams: MediaStream[];
  availableAudioStreams: MediaStream[];
  selectedAudioStreams: MediaStream[];
}
export interface UserMetadataDialogData {
  showVersionRelatedForm: boolean;
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
  pendingTasks: Task[] = [];
  currentUser: User | undefined;
  workflowImage: String | undefined;
  
  
  constructor(private route: ActivatedRoute, 
    private mediaService: MediaService,
    private userService: UserService, 
    private metadataService: MetadataService,
    private partnerUploadService: PartnerUploadService,
    private locationService: LocationService,
    private outgestService: OutgestService,
    public dialog: MatDialog,
    private fb: FormBuilder) { 
      this.currentUser = userService.currentUser
      userService.execChange.subscribe((value) => {
        this.currentUser = value
      })      
    }
  
  result: Media = {}
  metadataGroups:Page<MetadataGroup> = {};
  metadataTypes:typeof MetadataType = MetadataType;
  metadataAttachmentTypes:typeof MetadataAttachmentType = MetadataAttachmentType;
  metadataForm:any;
  selectedVersion:MediaVersion = {};
  loading:boolean = true
  showUploads: boolean = false

  ngOnInit(): void {
    
    this.route.paramMap.subscribe((params: ParamMap) => {
      const id:number = parseInt(params.get('id') ?? '-1')
      this.getMedia(id)
      this.getPendingTaskForMedia(id)
      this.getMetadataGroups(id)
    });
  }

  selectVersion(version:MediaVersion) {
    this.selectedVersion = version
    this.showUploads = false
  }

  acceptIngest(requestId:number) {
    console.log('acceptIngest')
    this.partnerUploadService.updateUploadRequestStatus(requestId, UploadRequestStatus.INGESTING).subscribe(r => {
      this.getMedia(this.result?.id!)
    })
  }

  displayUploadRequests() {
    this.showUploads = true
  }

  openPlayer(): void {
    console.log('open player');

    const dialogRef = this.dialog.open(PlayerDialog, {
      data: { url: '' },
    });
    dialogRef.afterOpened().subscribe(() => {
      var video:HTMLVideoElement = document.getElementById('video') as HTMLVideoElement;
      var videoSrc = `/api/static/san/variants/master_${this.result.id}.m3u8`;
      
      if (Hls.isSupported()) {
        var hls = new Hls();
        dialogRef.componentInstance.hls = hls;
        hls.on(Hls.Events.SUBTITLE_TRACKS_UPDATED, function(event:any, data:any) {
          console.log(event, data)

          for (const a of data.subtitleTracks) {
            dialogRef.componentInstance.subtitles.push({
              code: a.lang,
              name: a.name
            })
            if (a.autoselect) {
              dialogRef.componentInstance.selectedSubtitle = a.lang
            }
          }
        });
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
        video.play();
        setTimeout(() => { video.focus() }, 200);
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

  isRightGranted(a:string[]|undefined, b:string[]|undefined):boolean {
    if (!a || !b) return true
    var setB = new Set(b);
    return [...new Set(a)].filter(x => setB.has(x)).length > 0;
  }

  openUserMetadataDialog(): void {
    console.log('openUserMetadataDialog');

    // populate form
    console.log('metadataForm', this.metadataForm)
    const filteredOptions = new Map<string, any>()
    if (this.metadataGroups.content) {
      console.log('metadataGroups: ', this.metadataGroups)
      let group:{[k: string]: any} = {}
      if (this.metadataGroups.content) {
        for (const metadataGroup of this.metadataGroups.content) {
          let target = metadataGroup.attachmentType === MetadataAttachmentType.MEDIA ? this.result :
            metadataGroup.attachmentType === MetadataAttachmentType.MEDIA_VERSION ? this.selectedVersion : 
            this.selectedVersion
          console.log('target', target, 'for', metadataGroup.name)
          if (!target.id) {
            // skipping version's attached metadata if no version is selected
            continue
          }
          if (metadataGroup.metadatas) {
            let subgroup:{[k: string]: any} = {}
            for (const metadataDefinition of metadataGroup.metadatas) {
              const editable = this.isRightGranted(metadataDefinition.editingRestrictedToRoles, this.currentUser?.roles)

              // preset values if set
              subgroup[`${metadataDefinition.name}`] = target.dynamicMetadatas != null ? 
                [{value: target.dynamicMetadatas[metadataDefinition.name], disabled: !editable}] : 
                [{value: '', disabled: !editable}]

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
      console.log('form after', this.metadataForm)
      
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
        metadataAttachmentTypes: this.metadataAttachmentTypes
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

  openRequestPartnerUploadDialog(): void {
    console.log('openRequestPartnerUploadDialog');
    
    const dialogRef = this.dialog.open(RequestPartnerUploadDialog, {
      data: {},
    });

    dialogRef.afterClosed().subscribe(partner => {
      console.log('The dialog was closed', partner.value);
      this.partnerUploadService.createUploadRequest(partner.value.id, this.result.id!)
        .subscribe(() => {
          this.getMedia(this.result.id ?? -1)
        });
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
        .pipe(delay(200)).subscribe(result => this.getPendingTaskForMedia(this.result.id ?? -1));
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


  openOutgestDialog(): void {
    console.log('openOutgestDialog');
    let locationId = null;
    let availableStreams:any[] = this.result.elements?.flatMap(e => e.streams?.map(s => {
      s.fromFilename = e.filename
      return s
    })) as any[]
    const dialogRef = this.dialog.open(OutgestDialog, {
      data: {
        locationId,
        availableVideoStreams: availableStreams.filter(s => s.type === 'VIDEO'),
        availableAudioStreams: availableStreams.filter(s => s.type === 'AUDIO')
      },
    });
    dialogRef.afterOpened().subscribe(() => {
      this.locationService.getLocations().subscribe(locations => {
        dialogRef.componentInstance.data.locations = locations.content!
      })      
      this.outgestService.getOutgestProfiles().subscribe(profiles => {
        dialogRef.componentInstance.data.outgestProfiles = profiles.content
      })
    })
    dialogRef.afterClosed().subscribe(data => {
      console.log('The dialog was closed', data);
      if (!data) return
      const selectedVideoStreams = (data.selectedVideoStreams as MediaStream[]).map(s => s.id)
      const selectedAudioStreams = (data.selectedAudioStreams as MediaStream[]).map(s => s.id)
      this.outgestService.triggerOutgestTask(data.selectedOutgestProfile.id, 
        this.result.id!, 
        data.destinationLocation.id, 
        selectedVideoStreams, 
        selectedAudioStreams).pipe(delay(200)).subscribe(result => {
          console.log(result)
          this.getPendingTaskForMedia(this.result.id ?? -1)
        });
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
      data: { metadata: JSON.stringify(element.fullMetadatas, null, "  ") },
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
        // fetch workflow image
        if (result && result.activitiProcessId)
          this.getMediaWorkflowImage(result.activitiProcessId)
        // select first version automatically
        if (!this.selectedVersion.id && this.result?.versions?.length) {
          this.selectedVersion = this.result.versions[0]
        }
        this.loading = false;
      })
  }


  getMediaWorkflowImage(id:number): void {
    this.mediaService.getMediaWorkflowImage(id)
      .subscribe(result => {
        this.workflowImage = result
      })
  }  

  getPendingTaskForMedia(id:number): void {
    this.mediaService.getPendingTaskForMedia(id)
      .subscribe(result => {
        this.pendingTasks = result
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
  styleUrls: ['./dialog-player.sass'],
  templateUrl: './dialog-player.html',
})
export class PlayerDialog {
  
  hls: any;

  constructor(
    public dialogRef: MatDialogRef<PlayerDialog>,
    @Inject(MAT_DIALOG_DATA) public data: PlayerDialogData,
  ) {}

  audios: Audio[] = []
  subtitles: Subtitle[] = []
  selectedTrack?: string = undefined
  selectedSubtitle?: string = undefined
  
  onNoClick(): void {
    this.dialogRef.close();
  }

  @HostListener('window:keyup', ['$event'])
  keyEvent(event: KeyboardEvent) {
    console.log(event);
    if (event.key === 'j') this.nextFrame(event, true)
    else if (event.key === 'k') this.nextFrame(event)
  }

  nextFrame(event:any, backward:boolean=false): void {
    event.preventDefault();
    var video:HTMLVideoElement = document.getElementById('video') as HTMLVideoElement;
    let step = (1.0/25.0)
    if (backward) step = -step
    video.currentTime = video.currentTime + step
    console.log('next frame', video.currentTime)
  }

  subtitleTrackChanged(event:any): void {
    if (event.isUserInput) {
      console.log('subtitleTrackChanged', event, this.dialogRef.componentInstance.selectedSubtitle, this.hls.subtitleTracks);
      const selection = this.hls.subtitleTracks.find((a:any) => a.lang === event.source.value)
      console.log(selection)
      this.hls.subtitleTrack = selection.id
    }
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
  selector: 'dialog-outgest',
  templateUrl: './dialog-outgest.html',
  styleUrls: [ './dialog-outgest.sass' ]
})
export class OutgestDialog {
  constructor(
    public dialogRef: MatDialogRef<OutgestDialog>,
    @Inject(MAT_DIALOG_DATA) public data: OutgestDialogData,
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
  selector: 'dialog-request-partner-upload',
  templateUrl: './dialog-request-partner-upload.html',
})
export class RequestPartnerUploadDialog {
  partner = new FormControl('');
  filteredOptions: any = null;

  constructor(
    public dialogRef: MatDialogRef<RequestPartnerUploadDialog>,
    public userService: UserService, 
    @Inject(MAT_DIALOG_DATA) public data: RequestPartnerUploadData,
  ) {}
  ngOnInit() {
    this.filteredOptions = this.partner.valueChanges.pipe(
      startWith(''),
      debounceTime(400),
      distinctUntilChanged(),
      switchMap(val => {
        console.log('val', val)
        // TODO pas en dur
        const r = this.userService.getAutocompleteData('ROLE_PARTNER', val+'' || '')
        return r
      })       
    );
  }
  displayPartnerAutocomplete(value:User) {
    console.log('displayPartnerAutocomplete', value)
    return value?.email ?? ''
  }
  onNoClick(): void {
    this.dialogRef.close();
  }

  filterPartners(val: string): Observable<any[]> {
    console.log('filter', val)

    return this.userService.getAutocompleteData('ROLE_PARTNER', val)
     .pipe(
       map(response => response.filter(option => { 
        console.log('option', option)
         return option.email!.toLowerCase().indexOf(val.toLowerCase()) === 0
       }))
     )
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