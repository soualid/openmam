import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { LocationsComponent } from './components/locations/locations.component';
import { MediaDetailComponent } from './components/media-detail/media-detail.component';
import { MetadataSchemaDetailComponent } from './components/metadata-schema-detail/metadata-schema-detail.component';
import { MetadataSchemaComponent } from './components/metadata-schema/metadata-schema.component';
import { SearchComponent } from './components/search/search.component';
import { TasksComponent } from './components/tasks/tasks.component';


const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
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
