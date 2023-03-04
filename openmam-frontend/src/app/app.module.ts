import { NgModule, CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { MatListModule } from '@angular/material/list';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { MenuComponent } from './components/menu/menu.component';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core'; 
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatIconModule } from "@angular/material/icon";
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { CreateDialog, SearchComponent } from './components/search/search.component';
import { HomeComponent } from './components/home/home.component';
import { HttpClientModule, HttpRequest, HTTP_INTERCEPTORS } from '@angular/common/http';
import { IngestDialog, CreateVersionDialog, MediaDetailComponent, MetadataDialog, MoveMediaDialog, PlayerDialog, UserMetadataDialog, RequestPartnerUploadDialog, OutgestDialog } from './components/media-detail/media-detail.component';
import { LocationsComponent } from './components/locations/locations.component';
import { NgHttpCachingConfig, NgHttpCachingModule, NgHttpCachingStrategy } from 'ng-http-caching';
import { TasksComponent } from './components/tasks/tasks.component';
import { MetadataSchemaComponent } from './components/metadata-schema/metadata-schema.component';
import { MetadataSchemaDetailComponent } from './components/metadata-schema-detail/metadata-schema-detail.component';
import { LoginComponent } from './components/login/login.component';
import { AuthInterceptor } from './auth/auth.interceptor';
import { UserService } from './services/user.service';
import { UsersComponent } from './components/users/users.component';
import { PartnerUploadComponent, UploadDialog } from './components/partner-upload/partner-upload.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';

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
    RequestPartnerUploadDialog,
    OutgestDialog,
    PlayerDialog,
    IngestDialog,
    CreateDialog,
    UserMetadataDialog,
    LoginComponent,
    LocationsComponent,
    MoveMediaDialog, 
    CreateVersionDialog, 
    UploadDialog,
    TasksComponent,
    MetadataSchemaComponent,
    MetadataSchemaDetailComponent,
    LoginComponent,
    UsersComponent,
    PartnerUploadComponent,
    DashboardComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    MatListModule,
    MatProgressBarModule,
    MatIconModule,
    MatCardModule,
    MatSlideToggleModule,
    MatTableModule,
    MatSidenavModule,
    MatDatepickerModule,
    BrowserAnimationsModule,
    MatTooltipModule,
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
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
    UserService
  ],
  schemas: [ CUSTOM_ELEMENTS_SCHEMA ],
  bootstrap: [AppComponent]
})
export class AppModule { }
