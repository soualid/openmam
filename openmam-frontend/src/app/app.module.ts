import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { MatListModule } from '@angular/material/list';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { MenuComponent } from './components/menu/menu.component';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core'; 
import { MatIconModule } from "@angular/material/icon";
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatTableModule } from '@angular/material/table';
import { MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { CreateDialog, SearchComponent } from './components/search/search.component';
import { HomeComponent } from './components/home/home.component';
import { HttpClientModule, HttpRequest } from '@angular/common/http';
import { IngestDialog, CreateVersionDialog, MediaDetailComponent, MetadataDialog, MoveMediaDialog, PlayerDialog, UserMetadataDialog } from './components/media-detail/media-detail.component';
import { LocationsComponent } from './components/locations/locations.component';
import { NgHttpCachingConfig, NgHttpCachingModule, NgHttpCachingStrategy } from 'ng-http-caching';
import { TasksComponent } from './components/tasks/tasks.component';
import { MetadataSchemaComponent } from './components/metadata-schema/metadata-schema.component';
import { MetadataSchemaDetailComponent } from './components/metadata-schema-detail/metadata-schema-detail.component';

const ngHttpCachingConfig: NgHttpCachingConfig = {
  lifetime: Number.MAX_VALUE,
  cacheStrategy: NgHttpCachingStrategy.DISALLOW_ALL,
  isCacheable: (req: HttpRequest<any>) => {
    if (req.url === 'api/metadataGroups' || req.url === 'api/locations') {
      console.log('using cache /' + req.url)
      return true
    }
    return false
  }
};

@NgModule({
  declarations: [
    AppComponent,
    MenuComponent,
    SearchComponent,
    HomeComponent,
    MediaDetailComponent,
    MetadataDialog, 
    PlayerDialog,
    IngestDialog,
    CreateDialog,
    UserMetadataDialog,
    LocationsComponent,
    MoveMediaDialog, 
    CreateVersionDialog, 
    TasksComponent,
    MetadataSchemaComponent,
    MetadataSchemaDetailComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    MatListModule,
    MatIconModule,
    MatSlideToggleModule,
    MatTableModule,
    MatSidenavModule,
    MatDatepickerModule,
    BrowserAnimationsModule,
    HttpClientModule,
    NgHttpCachingModule.forRoot(ngHttpCachingConfig),
    MatDialogModule,
    MatButtonModule,
    MatSelectModule,
    MatNativeDateModule,
    MatMenuModule,
    FormsModule,
    MatAutocompleteModule,
    MatInputModule,
    MatProgressSpinnerModule,
    ReactiveFormsModule
  ],
  providers: [],
  schemas: [ CUSTOM_ELEMENTS_SCHEMA ],
  bootstrap: [AppComponent]
})
export class AppModule { }
