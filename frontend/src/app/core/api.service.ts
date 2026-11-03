import { Injectable, inject, signal, effect } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpHeaders, HttpInterceptorFn } from '@angular/common/http';
import { firstValueFrom, catchError, throwError } from 'rxjs';

export const authInterceptor:HttpInterceptorFn=(request,next)=>{
  const token=sessionStorage.getItem('po-token');
  const own=request.url.startsWith('/api/');
  const authorized=token&&own?request.clone({setHeaders:{Authorization:`Bearer ${token}`}}):request;
  return next(authorized).pipe(catchError((error:HttpErrorResponse)=>{
    if(error.status===401&&own&&!request.url.includes('/auth/entrar'))window.dispatchEvent(new Event('po-session-expired'));
    return throwError(()=>error);
  }));
};
export function errorMessage(error:unknown):string {
  if(error instanceof HttpErrorResponse){
    if(error.status===0||error.status===502||error.status===504)return 'No pudimos conectar con la tienda. Inténtalo de nuevo en un momento.';
    return error.error?.mensaje??(error.status===401?'Inicia sesión para continuar.':error.status===404?'No encontramos lo que buscas.':'No pudimos completar la solicitud. Inténtalo de nuevo.');
  }
  return error instanceof Error?error.message:'Ocurrió un problema. Inténtalo de nuevo.';
}
@Injectable({providedIn:'root'})
export class ApiService {
  private http=inject(HttpClient);
  get<T>(path:string,key?:string){return this.http.get<T>('/api'+path,{headers:key?new HttpHeaders({'X-Consulta-Pedido':key}):undefined});}
  post<T>(path:string,data:unknown={},headers?:Record<string,string>){return firstValueFrom(this.http.post<T>('/api'+path,data,{headers}));}
  put<T>(path:string,data:unknown={}){return firstValueFrom(this.http.put<T>('/api'+path,data));}
  delete<T=void>(path:string){return firstValueFrom(this.http.delete<T>('/api'+path));}
  async download(path:string){
    const blob=await firstValueFrom(this.http.get('/api'+path,{responseType:'blob'}));
    const url=URL.createObjectURL(blob);window.open(url,'_blank','noopener,noreferrer');setTimeout(()=>URL.revokeObjectURL(url),60000);
  }
}
export function resource<T>(path:()=>string|null,initial:T,key:()=>string|undefined=()=>undefined){
  const api=inject(ApiService),data=signal<T>(initial),loading=signal(true),error=signal(''),revision=signal(0);
  effect(onCleanup=>{
    revision();const url=path(),guest=key();
    if(!url){data.set(initial);loading.set(false);return;}
    loading.set(true);error.set('');
    const subscription=api.get<T>(url,guest).subscribe({next:value=>{data.set(value);loading.set(false);},error:e=>{error.set(errorMessage(e));loading.set(false);}});
    onCleanup(()=>subscription.unsubscribe());
  });
  return {data,loading,error,reload:()=>revision.update(x=>x+1)};
}
