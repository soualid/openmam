import { Component } from '@angular/core';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { MetadataGroup, MetadataType } from 'src/app/models/metadata';
import { MetadataService } from 'src/app/services/metadata.service';

@Component({
  selector: 'app-metadata-schema-detail',
  templateUrl: './metadata-schema-detail.component.html',
  styleUrls: ['./metadata-schema-detail.component.sass']
})
export class MetadataSchemaDetailComponent {

  constructor(private route: ActivatedRoute, 
    private metadataService: MetadataService) { }

    result: MetadataGroup = {id: -1}
    metadataTypes: typeof MetadataType = MetadataType;

  ngOnInit(): void {
    this.route.paramMap.subscribe((params: ParamMap) => {
      const id:number = parseInt(params.get('id') ?? '-1')
      this.getMetadataGroup(id)
    });
  }

  getMetadataGroup(id:number): void {
    this.metadataService.getMetadataGroup(id)
      .subscribe(result => this.result = result)
  }

}
