import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { LocationsComponent } from './components/locations/locations.component';
import { MediaDetailComponent } from './components/media-detail/media-detail.component';
import { SearchComponent } from './components/search/search.component';


const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
  { path: 'media/:id', component: MediaDetailComponent },
  { path: 'search', component: SearchComponent },
  { path: 'locations', component: LocationsComponent },
];


@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
