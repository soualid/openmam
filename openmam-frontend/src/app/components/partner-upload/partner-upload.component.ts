import { HttpEvent, HttpEventType } from '@angular/common/http';
import { Component, Inject } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { tap } from 'rxjs';
import { Page } from 'src/app/models/page';
import { PartnerUploadService } from 'src/app/services/partner.upload.service';
import { S3UploaderService } from 'src/app/services/s3uploader.service';
import { UploadRequest, UploadRequestStatus } from '/Users/simon/arte/openmam/openmam-frontend/src/app/models/upload.request';


export interface UploadDialogData {
  upload: UploadRequest;
}

@Component({
  selector: 'app-partner-upload',
  templateUrl: './partner-upload.component.html',
  styleUrls: ['./partner-upload.component.sass']
})
export class PartnerUploadComponent {
  loading: boolean = true;
  result: Page<UploadRequest> = {};
  dataSource: UploadRequest[] = [];
  columnsToDisplay: string[] = ['id', 'media', 'status', 'creationDate' ]

  constructor(private partnerUploadService: PartnerUploadService,
              public dialog: MatDialog) {
  }


  ngOnInit(): void {
    this.getPartnerUploads();
  }
  
  getPartnerUploads(): void {
    this.partnerUploadService.getUploadRequestsForConnectedUser()
      .subscribe(result => {
        this.result = result
        this.dataSource = this.result.content ?? []
        this.loading = false
      });
  }

  clickedRow(row:UploadRequest): void {
    console.log(row)

    console.log('openUploadDialog');

    const dialogRef = this.dialog.open(UploadDialog, {
      data: {
        upload: row
      },
    });

    dialogRef.afterClosed().subscribe(path => {
      console.log('The dialog was closed', path);
    });        
  }

}

@Component({
  selector: 'dialog-upload',
  templateUrl: './dialog-upload.html',
  styleUrls: ['./dialog-upload.sass']
})
export class UploadDialog {
  constructor(
    public dialogRef: MatDialogRef<UploadDialog>,
    private s3uploaderService: S3UploaderService,
    private partnerUploadService: PartnerUploadService,
    @Inject(MAT_DIALOG_DATA) public data: UploadDialogData,
  ) {}

  onNoClick(): void {
    this.dialogRef.close();
  }

  uploading: boolean = false
  complete: boolean = false
  progress:number = 0

  uploadFile(event: Event) {
    this.uploading = true
    const element = event.currentTarget as HTMLInputElement;
    let fileList: FileList | null = element.files;
    const url = this.dialogRef.componentInstance.data.upload.presignedUploadURL
    if (fileList) {
      console.log("FileUpload -> files", fileList);
      this.s3uploaderService.uploadfileAWSS3(url, "application/octet-stream", fileList.item(0)!)
        .subscribe((event:HttpEvent<any>) => {
          console.log(JSON.stringify(event))
          if (event.type === HttpEventType.UploadProgress) {
            console.log('progress', event.loaded, event.total)
            this.progress = event.loaded/event.total!*100.0
          } else if (event.type === HttpEventType.Response) {
            console.log('complete')
            this.complete = true
            this.partnerUploadService.updateUploadRequestStatus(this.data.upload.id, UploadRequestStatus.UPLOADED).subscribe(r => {})
          }
        })
    }
  }

}
