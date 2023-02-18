import { Component, Inject } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Media } from 'src/app/models/media';
import { Page } from 'src/app/models/page';
import { MediaService } from 'src/app/services/media.service';

export interface CreateDialogData {
  title: string;
}

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.sass']
})
export class SearchComponent {

  constructor(private mediaService: MediaService,
              public dialog: MatDialog) { }
  
  result: Page<Media> = {}
  loading: boolean = true

  ngOnInit(): void {
    this.getMedias();
  }

  openCreateDialog(): void {
    console.log('openCreateDialog');

    const dialogRef = this.dialog.open(CreateDialog, {
      data: {},
    });

    dialogRef.afterClosed().subscribe(title => {
      console.log('The dialog was closed', title);
      this.mediaService.createMedia(title)
        .subscribe(result => this.getMedias());
    });    
  }

  getMedias(): void {
    this.mediaService.getMedias()
      .subscribe(result => {
        this.result = result
        this.loading = false
      });
  }

}


@Component({
  selector: 'dialog-create',
  templateUrl: './dialog-create.html',
})
export class CreateDialog {
  constructor(
    public dialogRef: MatDialogRef<CreateDialog>,
    @Inject(MAT_DIALOG_DATA) public data: CreateDialogData,
  ) {}

  onNoClick(): void {
    this.dialogRef.close();
  }
}
