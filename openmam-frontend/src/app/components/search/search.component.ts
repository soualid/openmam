import { Component, Inject } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Router } from '@angular/router';
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
  
  dataSource: Media[] = [];
  columnsToDisplay: string[] = ['preview', 'name', 'elementCount', 'streamCount']
  constructor(private mediaService: MediaService,
              public dialog: MatDialog,
              private router: Router) { }
  
  result: Page<Media> = {}
  loading: boolean = true

  ngOnInit(): void {
    this.getMedias();
  }

  clickedRow(row:any): void {
    console.log(row)
    this.router.navigate([`/media/${row.id}`]);
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
        this.dataSource = this.result.content ?? []
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
