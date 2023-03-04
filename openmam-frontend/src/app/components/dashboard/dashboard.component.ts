import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { Media } from 'src/app/models/media';
import { Page } from 'src/app/models/page';
import { MediaService } from 'src/app/services/media.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.sass']
})
export class DashboardComponent {

  result: Page<Media> = {}
  loading: boolean = true
  dataSource: Media[] = []
  columnsToDisplay: string[] = ['preview', 'name', 'elementCount', 'streamCount']

  constructor(private mediaService: MediaService,
    private router: Router) { }

  ngOnInit(): void {
    this.getDashboard();
  }
 
  clickedRow(row:any): void {
    console.log(row)
    this.router.navigate([`/media/${row.id}`]);
  }

  getDashboard(): void {
    this.mediaService.getDashboard()
      .subscribe(result => {
        this.result = result
        this.loading = false
        this.dataSource = this.result.content ?? []
        console.log('set datasource to', this.dataSource)
      });
  }
}
