import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MetadataSchemaDetailComponent } from './metadata-schema-detail.component';

describe('MetadataSchemaDetailComponent', () => {
  let component: MetadataSchemaDetailComponent;
  let fixture: ComponentFixture<MetadataSchemaDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MetadataSchemaDetailComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MetadataSchemaDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
