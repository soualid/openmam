import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PartnerUploadComponent } from './partner-upload.component';

describe('PartnerUploadComponent', () => {
  let component: PartnerUploadComponent;
  let fixture: ComponentFixture<PartnerUploadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PartnerUploadComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PartnerUploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
