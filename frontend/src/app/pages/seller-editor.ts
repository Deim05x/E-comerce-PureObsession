import {Component,inject,ChangeDetectorRef} from '@angular/core';
import {FormBuilder,ReactiveFormsModule,Validators} from '@angular/forms';
import {ActivatedRoute,Router,RouterLink} from '@angular/router';
import {errorMessage} from '../core/api.service';
import {SessionService,ToastService} from '../core/state.service';
import {SellerService} from '../core/services/seller.service';
import {IconComponent} from '../shared/ui';

@Component({
  standalone:true,
  imports:[ReactiveFormsModule,RouterLink,IconComponent],
  template:`
    <a routerLink="/vendedor/productos" class="text-link">← Mis productos</a>
    <header class="page-heading workspace-heading"><div><span class="eyebrow">CURADURÍA DE CATÁLOGO</span><h1>{{id?'Editar publicación':'Una nueva esencia'}}</h1><p>Completa la información real del producto. Los originales requieren certificado aprobado para publicarse.</p></div><span class="page-seal"><ui-icon name="shield"/> Datos verificables</span></header>
    <form class="panel form-stack" [formGroup]="form" (ngSubmit)="save()">
      <div class="panel-heading"><span class="step-number">01</span><div><span class="eyebrow">INFORMACIÓN PRINCIPAL</span><h2>Ficha de la fragancia</h2></div></div>
      <div class="form-grid row g-3">
        <label class="col-md-6">Nombre<input class="form-control" [class.is-invalid]="invalid('nombre')" formControlName="nombre">@if(invalid('nombre')){<small class="invalid-feedback d-block">El nombre es obligatorio.</small>}</label>
        <label class="col-md-6">Marca<input class="form-control" [class.is-invalid]="invalid('marca')" formControlName="marca">@if(invalid('marca')){<small class="invalid-feedback d-block">La marca es obligatoria.</small>}</label>
        <label class="col-md-6">Precio COP<input class="form-control" [class.is-invalid]="invalid('precio')" type="number" formControlName="precio">@if(invalid('precio')){<small class="invalid-feedback d-block">El precio debe ser mayor que cero.</small>}</label>
        <label class="col-md-6">Categoría<select class="form-select" formControlName="categoria">@for(c of session.options()?.categorias;track c){<option [value]="c">{{c}}</option>}</select></label>
      </div>
      <label>Descripción<textarea class="form-control" [class.is-invalid]="invalid('descripcion')" formControlName="descripcion"></textarea>@if(invalid('descripcion')){<small class="invalid-feedback d-block">Describe el producto.</small>}</label>
      @if(!id){
        <div class="form-grid row g-3">
          <label class="col-md-6">Familia<select class="form-select" formControlName="familia">@for(c of session.options()?.familias;track c){<option [value]="c">{{c}}</option>}</select></label>
          <label class="col-md-6">Concentración<select class="form-select" formControlName="concentracion">@for(c of session.options()?.concentraciones;track c){<option [value]="c">{{c}}</option>}</select></label>
          <label class="col-md-6">Presentación<select class="form-select" formControlName="tipo">@for(c of session.options()?.presentaciones;track c){<option [value]="c">{{c}}</option>}</select></label>
          <label class="col-md-6">Volumen ml<input class="form-control" [class.is-invalid]="invalid('volumenMl')" type="number" formControlName="volumenMl">@if(invalid('volumenMl')){<small class="invalid-feedback d-block">El volumen debe ser mayor que cero.</small>}</label>
          <label class="col-md-6">Existencias<input class="form-control" [class.is-invalid]="invalid('stock')" type="number" formControlName="stock">@if(invalid('stock')){<small class="invalid-feedback d-block">Las existencias no pueden ser negativas.</small>}</label>
          <label class="col-md-6">Origen<select class="form-select" formControlName="origen">@for(c of session.options()?.origenes;track c){<option [value]="c">{{c}}</option>}</select></label>
          <label class="col-md-6">País de origen<input class="form-control" formControlName="paisOrigen"></label>
          @if(form.controls.tipo.value==='DECANT'){<label class="col-md-6">ID de botella madre<input class="form-control" formControlName="botellaMadreId"></label>}
          <label class="col-md-4">Notas de salida<input class="form-control" [class.is-invalid]="invalid('notasSalida')" formControlName="notasSalida"></label>
          <label class="col-md-4">Notas de corazón<input class="form-control" [class.is-invalid]="invalid('notasCorazon')" formControlName="notasCorazon"></label>
          <label class="col-md-4">Notas de fondo<input class="form-control" [class.is-invalid]="invalid('notasFondo')" formControlName="notasFondo"></label>
        </div>
        <label class="checkbox"><input type="checkbox" formControlName="original"> Producto original (requiere certificado)</label>
      }
      @if(error){<p class="error" role="alert">{{error}}</p>}
      <button class="button btn btn-primary" type="submit" [disabled]="busy">{{busy?'Guardando…':'Guardar borrador'}} <ui-icon name="arrow"/></button>
    </form>
    @if(id){
      <section class="panel form-stack">
        <div class="panel-heading"><span class="step-number">02</span><div><span class="eyebrow">AUTENTICIDAD Y VISIBILIDAD</span><h2>Publicación y documentos</h2></div></div><p>{{published?'Publicada':'Borrador'}} · Certificado: {{certificate||'Sin certificado'}}</p>
        <label>Fotografía PNG o JPEG (máx. 5 MB)<input class="form-control" type="file" accept="image/png,image/jpeg" (change)="upload($event,false)"></label>
        <label>Certificado PDF, PNG o JPEG (máx. 5 MB)<input class="form-control" type="file" accept="application/pdf,image/png,image/jpeg" (change)="upload($event,true)"></label>
        <button class="button btn btn-outline-primary" type="button" [disabled]="busy" (click)="publish()">{{published?'Retirar publicación':'Publicar producto'}}</button>
      </section>
    }
  `
})
export class SellerEditor {
  private fb=inject(FormBuilder);private seller=inject(SellerService);private cdr=inject(ChangeDetectorRef);
  session=inject(SessionService);private toast=inject(ToastService);private router=inject(Router);
  id=inject(ActivatedRoute).snapshot.paramMap.get('id');busy=false;error='';published=false;certificate:string|null=null;
  form=this.fb.nonNullable.group({
    nombre:['',[Validators.required,Validators.maxLength(160)]],marca:['',[Validators.required,Validators.maxLength(120)]],descripcion:['',[Validators.required,Validators.maxLength(1000)]],categoria:['NICHO',Validators.required],
    familia:['AMADERADA',Validators.required],concentracion:['EAU_DE_PARFUM',Validators.required],tipo:['PERFUME',Validators.required],volumenMl:[100,[Validators.required,Validators.min(1)]],precio:[0,[Validators.required,Validators.min(1)]],stock:[0,[Validators.required,Validators.min(0)]],
    original:[false],origen:['NACIONAL',Validators.required],paisOrigen:['Colombia',[Validators.required,Validators.maxLength(100)]],botellaMadreId:[''],notasSalida:['',[Validators.required,Validators.maxLength(300)]],notasCorazon:['',[Validators.required,Validators.maxLength(300)]],notasFondo:['',[Validators.required,Validators.maxLength(300)]]
  });
  constructor(){if(this.id){this.form.controls.nombre.disable();void this.load();}}
  invalid(name:keyof typeof this.form.controls){const control=this.form.controls[name];return control.touched&&control.invalid;}
  async load(){if(!this.id)return;try{const p=await this.seller.product(this.id);this.form.patchValue({...p,stock:p.stockDisponible} as never);this.published=p.publicada;this.certificate=p.certificado;}catch(e){this.error=errorMessage(e);}finally{this.cdr.markForCheck();}}
  async save(){this.form.markAllAsTouched();this.error='';if(this.form.invalid){this.error='Revisa los campos marcados antes de guardar.';return;}this.busy=true;try{const data=this.form.getRawValue();if(this.id)await this.seller.updateProduct(this.id,data);else{const p=await this.seller.createProduct(data);this.id=p.id;await this.router.navigate(['/vendedor/productos',p.id]);await this.load();}this.toast.show('Producto guardado.');}catch(e){this.error=errorMessage(e);this.toast.show(this.error,true);}finally{this.busy=false;this.cdr.markForCheck();}}
  async upload(event:Event,certificate:boolean){const file=(event.target as HTMLInputElement).files?.[0];if(!file||!this.id)return;if(file.size>5*1024*1024){this.error='El archivo supera los 5 MB.';return;}this.busy=true;try{if(certificate)await this.seller.uploadCertificate(this.id,file);else await this.seller.uploadImage(this.id,file);this.toast.show('Archivo cargado.');await this.load();}catch(e){this.error=errorMessage(e);}finally{this.busy=false;this.cdr.markForCheck();}}
  async publish(){if(!this.id)return;this.busy=true;try{if(this.published)await this.seller.unpublish(this.id);else await this.seller.publish(this.id);await this.load();this.toast.show('Publicación actualizada.');}catch(e){this.error=errorMessage(e);}finally{this.busy=false;this.cdr.markForCheck();}}
}
