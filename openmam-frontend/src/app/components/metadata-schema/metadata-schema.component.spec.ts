import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MetadataSchemaComponent } from './metadata-schema.component';

describe('MetadataSchemaComponent', () => {
  let component: MetadataSchemaComponent;
  let fixture: ComponentFixture<MetadataSchemaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MetadataSchemaComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MetadataSchemaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
