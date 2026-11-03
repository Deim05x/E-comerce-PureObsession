import {test} from 'node:test';
import assert from 'node:assert/strict';
import {sanitizeLocalCart,safeReturn,money} from '../src/app/core/domain.ts';
test('redirecciones de acceso permanecen dentro de la aplicación',()=>{
 assert.equal(safeReturn('https://evil.example'),'/');assert.equal(safeReturn('//evil.example'),'/');assert.equal(safeReturn('/acceso'),'/');assert.equal(safeReturn('/checkout'),'/checkout');
});
test('el carrito descarta cantidades inválidas y duplicados',()=>{
 const producto={id:'a',nombre:'Perfume',precio:100};
 assert.deepEqual(sanitizeLocalCart(null),[]);
 const result=sanitizeLocalCart([{producto,cantidad:2},{producto,cantidad:3},{producto:{...producto,id:'b'},cantidad:-1},{producto:{...producto,id:'c',precio:NaN},cantidad:1}]);
 assert.equal(result.length,1);assert.equal(result[0].cantidad,2);
});
test('el formato monetario conserva los decimales',()=>{assert.match(money(1234.56),/1\.234,56/);});
