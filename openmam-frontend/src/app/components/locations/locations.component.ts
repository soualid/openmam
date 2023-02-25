import { Component } from '@angular/core';
import { Page } from 'src/app/models/page';
import { Location } from 'src/app/models/media';
import { LocationService } from 'src/app/services/location.service';

@Component({
  selector: 'app-locations',
  templateUrl: './locations.component.html',
  styleUrls: ['./locations.component.sass']
})
export class LocationsComponent {

  result: Page<Location> = {}
  loading: boolean = true
  dataSource: Location[] = []
  columnsToDisplay: string[] = ['id', 'type', 'name']

  constructor(private taskService: LocationService) { }

  ngOnInit(): void {
    this.getLocations();
  }
  
  getLocations(): void {
    this.taskService.getLocations()
      .subscribe(result => {
        this.result = result
        this.loading = false
        this.dataSource = this.result.content ?? []
        console.log('set datasource to', this.dataSource)
      });
  }
}
