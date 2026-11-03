import { bootstrapApplication } from '@angular/platform-browser';
import { provideZonelessChangeDetection } from '@angular/core';
import { provideHttpClient,withInterceptors } from '@angular/common/http';
import { provideRouter,withInMemoryScrolling } from '@angular/router';
import { AppComponent } from './app/app.component';
import { routes } from './app/app.routes';
import { authInterceptor } from './app/core/api.service';

bootstrapApplication(AppComponent,{
  providers:[provideZonelessChangeDetection(),provideHttpClient(withInterceptors([authInterceptor])),provideRouter(routes,withInMemoryScrolling({scrollPositionRestoration:'enabled',anchorScrolling:'enabled'}))],
}).catch(error=>console.error('No se pudo iniciar la aplicación.',error));
