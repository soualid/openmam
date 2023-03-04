import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { HomeComponent } from './components/home/home.component';
import { LocationsComponent } from './components/locations/locations.component';
import { LoginComponent } from './components/login/login.component';
import { MediaDetailComponent } from './components/media-detail/media-detail.component';
import { MetadataSchemaDetailComponent } from './components/metadata-schema-detail/metadata-schema-detail.component';
import { MetadataSchemaComponent } from './components/metadata-schema/metadata-schema.component';
import { PartnerUploadComponent } from './components/partner-upload/partner-upload.component';
import { SearchComponent } from './components/search/search.component';
import { TasksComponent } from './components/tasks/tasks.component';
import { UsersComponent } from './components/users/users.component';


const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'partner_upload', component: PartnerUploadComponent },
  { path: 'users', component: UsersComponent },
  { path: 'media/:id', component: MediaDetailComponent },
  { path: 'search', component: SearchComponent },
  { path: 'tasks', component: TasksComponent },
  { path: 'locations', component: LocationsComponent },
  { path: 'metadata_schema', component: MetadataSchemaComponent },
  { path: 'metadata_schema/:id', component: MetadataSchemaDetailComponent },
];


@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
