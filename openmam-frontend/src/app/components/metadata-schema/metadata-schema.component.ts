import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { MetadataGroup, MetadataType } from 'src/app/models/metadata';
import { Page } from 'src/app/models/page';
import { MetadataService } from 'src/app/services/metadata.service';

@Component({
  selector: 'app-metadata-schema',
  templateUrl: './metadata-schema.component.html',
  styleUrls: ['./metadata-schema.component.sass']
})
export class MetadataSchemaComponent {

  result: Page<MetadataGroup> = {}
  loading: boolean = true
  dataSource: MetadataGroup[] = []
  columnsToDisplay: string[] = ['id', 'name', 'attachmentType']
  metadataTypes:typeof MetadataType = MetadataType;
  
  constructor(private metadataService: MetadataService,
    private router: Router) { }

  ngOnInit(): void {
    this.getMetadataGroups();
  }

  clickedRow(row:any): void {
    console.log(row)
    this.router.navigate([`/metadata_schema/${row.id}`]);
  }
  
  getMetadataGroups(): void {
    this.metadataService.getMetadataGroups()
      .subscribe(result => {
        this.result = result
        this.loading = false
        this.dataSource = this.result.content ?? []
        console.log('set datasource to', this.dataSource)
      });
  }
}
