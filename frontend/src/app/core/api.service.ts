import { Injectable, inject, signal, effect } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders, HttpInterceptorFn } from '@angular/common/http';
import { firstValueFrom, catchError, throwError, Observable } from 'rxjs';

export const authInterceptor:HttpInterceptorFn=(request,next)=>{
  const token=localStorage.getItem('po-token');
  const own=request.url.startsWith('/api/');
  const authorized=token&&own?request.clone({setHeaders:{Authorization:`Bearer ${token}`}}):request;
  return next(authorized).pipe(catchError((error:HttpErrorResponse)=>{
    if(error.status===401&&own&&!request.url.includes('/auth/entrar'))window.dispatchEvent(new Event('po-session-expired'));
    return throwError(()=>error);
  }));
};
export function errorMessage(error:unknown):string {
  if(error instanceof HttpErrorResponse){
    if(error.error?.mensaje)return error.error.mensaje;
    switch(error.status){
      case 0: case 502: case 504:return 'No pudimos conectar con la tienda. Inténtalo de nuevo en un momento.';
      case 400:return 'Revisa los datos enviados e inténtalo nuevamente.';
      case 401:return 'Tu sesión no es válida. Inicia sesión nuevamente.';
      case 403:return 'No tienes permisos para realizar esta operación.';
      case 404:return 'No encontramos lo que buscas.';
      case 409:return 'La operación entra en conflicto con el estado actual.';
      case 422:return 'La operación no cumple las reglas del negocio.';
      default:return 'No pudimos completar la solicitud. Inténtalo de nuevo.';
    }
  }
  return error instanceof Error?error.message:'Ocurrió un problema. Inténtalo de nuevo.';
}
@Injectable({providedIn:'root'})
export class ApiService {
  private http=inject(HttpClient);
  get<T>(path:string,key?:string,extraHeaders:Record<string,string>={}){const headers=new HttpHeaders({...extraHeaders,...(key?{'X-Consulta-Pedido':key}:{})});return this.http.get<T>('/api'+path,{headers});}
  post<T>(path:string,data:unknown={},headers?:Record<string,string>){return firstValueFrom(this.http.post<T>('/api'+path,data,{headers}));}
  put<T>(path:string,data:unknown={}){return firstValueFrom(this.http.put<T>('/api'+path,data));}
  delete<T=void>(path:string){return firstValueFrom(this.http.delete<T>('/api'+path));}
  async download(path:string){
    const blob=await firstValueFrom(this.http.get('/api'+path,{responseType:'blob'}));
    const url=URL.createObjectURL(blob);window.open(url,'_blank','noopener,noreferrer');setTimeout(()=>URL.revokeObjectURL(url),60000);
  }
}
export function resource<T>(request:()=>string|Observable<T>|null,initial:T,key:()=>string|undefined=()=>undefined){
  const api=inject(ApiService),data=signal<T>(initial),loading=signal(true),error=signal(''),revision=signal(0);
  effect(onCleanup=>{
    revision();const source=request(),guest=key();
    if(!source){data.set(initial);loading.set(false);return;}
    loading.set(true);error.set('');
    const observable=typeof source==='string'?api.get<T>(source,guest):source;
    const subscription=observable.subscribe({next:value=>{data.set(value);loading.set(false);},error:e=>{error.set(errorMessage(e));loading.set(false);}});
    onCleanup(()=>subscription.unsubscribe());
  });
  return {data,loading,error,reload:()=>revision.update(x=>x+1)};
}
