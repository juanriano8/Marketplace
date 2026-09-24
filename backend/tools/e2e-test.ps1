# ============================================================================
# Prueba end-to-end de los endpoints contra Cloud SQL
# Ejecuta el mismo recorrido que hará el usuario en Postman.
# ============================================================================
$ErrorActionPreference = 'Continue'
$B = 'http://localhost:8080'
$dir = Join-Path $env:TEMP 'mke2e'; New-Item -ItemType Directory -Force -Path $dir | Out-Null
$global:ok = 0; $global:fail = 0

$v = @{}
Get-Content backend\.env | ForEach-Object { $l=$_.Trim(); if ($l -and -not $l.StartsWith('#') -and $l.Contains('=')) { $i=$l.IndexOf('='); $v[$l.Substring(0,$i).Trim()]=$l.Substring($i+1).Trim() } }

function Req {
  param([string]$Name, [string]$Method, [string]$Path, $Obj, [string]$Token, [int[]]$Expect)
  $bodyFile = Join-Path $dir 'body.json'; $respFile = Join-Path $dir 'resp.json'
  $a = @('-s','-o',$respFile,'-w','%{http_code}','-X',$Method,"$B$Path",'--max-time','30')
  if ($null -ne $Obj) {
    [System.IO.File]::WriteAllText($bodyFile, ($Obj | ConvertTo-Json -Compress -Depth 6))
    $a += @('-H','Content-Type: application/json','--data-binary',"@$bodyFile")
  }
  if ($Token) { $a += @('-H',"Authorization: Bearer $Token") }
  $code = [int](& curl.exe @a)
  $raw = Get-Content $respFile -Raw -ErrorAction SilentlyContinue
  $json = $null
  if ($raw) { try { $json = $raw | ConvertFrom-Json } catch { } }

  $pass = $Expect -contains $code
  if ($pass) { $global:ok++ } else { $global:fail++ }
  $mark = if ($pass) { 'OK  ' } else { 'FALLO' }
  $color = if ($pass) { 'Green' } else { 'Red' }
  Write-Host ("[{0}] {1,-46} HTTP {2}" -f $mark, $Name, $code) -ForegroundColor $color
  if (-not $pass) {
    Write-Host ("        esperaba: {0}" -f ($Expect -join '/')) -ForegroundColor Yellow
    if ($raw) { Write-Host ("        respuesta: {0}" -f $raw.Substring(0,[Math]::Min(200,$raw.Length))) -ForegroundColor Yellow }
  }
  return $json
}

Write-Host "`n================ 1. AUTENTICACION ================" -ForegroundColor Cyan
$admin = Req 'POST login admin' POST '/api/v1/auth/login' @{email=$v['BOOTSTRAP_ADMIN_EMAIL'];password=$v['BOOTSTRAP_ADMIN_PASSWORD']} $null @(200)
$adminT = $admin.accessToken

$suffix = (Get-Random -Maximum 99999)
$sellerEmail = "vendedor$suffix@marketplace.com"
$seller2Email = "vendedor2$suffix@marketplace.com"
$buyerEmail  = "comprador$suffix@marketplace.com"
$pw = 'Password123!'

$reg = Req 'POST register/buyer' POST '/api/v1/auth/register/buyer' @{email=$buyerEmail;password=$pw} $null @(201)
$buyerT = $reg.accessToken

$s1 = Req 'POST register/seller' POST '/api/v1/auth/register/seller' @{email=$sellerEmail;password=$pw} $null @(201)
$sellerT = $s1.accessToken; $sellerId = $s1.userId

$s2 = Req 'POST register/seller (2)' POST '/api/v1/auth/register/seller' @{email=$seller2Email;password=$pw} $null @(201)
$seller2Id = $s2.userId

Write-Host "`n================ 2. SEGURIDAD ================" -ForegroundColor Cyan
Req 'GET /admin/orders sin token'   GET '/api/v1/admin/orders' $null $null @(401) | Out-Null
Req 'GET /admin/orders con BUYER'   GET '/api/v1/admin/orders' $null $buyerT @(403) | Out-Null
Req 'GET /cart con SELLER'          GET '/api/v1/cart' $null $sellerT @(403) | Out-Null
Req 'POST /seller/products sin verificar' POST '/api/v1/seller/products' @{name='X';price=10;stockQuantity=1;category='T'} $sellerT @(400) | Out-Null

Write-Host "`n================ 3. ADMIN VERIFICA VENDEDOR ================" -ForegroundColor Cyan
Req 'PATCH verify vendedor 1' PATCH "/api/v1/admin/sellers/$sellerId/verify" @{approved=$true} $adminT @(200) | Out-Null
Req 'PATCH verify vendedor 2' PATCH "/api/v1/admin/sellers/$seller2Id/verify" @{approved=$true} $adminT @(200) | Out-Null

Write-Host "`n================ 4. CATALOGO ================" -ForegroundColor Cyan
$p1 = Req 'POST /seller/products' POST '/api/v1/seller/products' @{name="Audifonos Pro $suffix";description='Cancelacion de ruido';price=149.99;currencyCode='USD';stockQuantity=25;category='Electronics'} $sellerT @(201)
$p2 = Req 'POST /seller/products (2)' POST '/api/v1/seller/products' @{name="Teclado RGB $suffix";price=89.50;currencyCode='USD';stockQuantity=3;category='Electronics'} $sellerT @(201)
$prodId = $p1.id; $prod2Id = $p2.id; $slug = $p1.slug

Req 'GET /seller/products' GET '/api/v1/seller/products?page=0&size=20' $null $sellerT @(200) | Out-Null
Req 'PUT /seller/products/{id}' PUT "/api/v1/seller/products/$prodId" @{price=139.99} $sellerT @(200) | Out-Null
Req 'GET /admin/products?status=' GET '/api/v1/admin/products?status=PENDING_APPROVAL' $null $adminT @(200) | Out-Null
Req 'PATCH aprobar producto 1' PATCH "/api/v1/admin/products/$prodId/approval" @{approved=$true} $adminT @(200) | Out-Null
Req 'PATCH aprobar producto 2' PATCH "/api/v1/admin/products/$prod2Id/approval" @{approved=$true} $adminT @(200) | Out-Null

Write-Host "`n================ 5. INVENTARIO ================" -ForegroundColor Cyan
Req 'GET /inventory/check (publico)' GET "/api/v1/inventory/check/$prodId" $null $null @(200) | Out-Null
Req 'POST /seller/inventory/adjust delta' POST '/api/v1/seller/inventory/adjust' @{productId=$prodId;quantityDelta=-2;reason='Prueba e2e'} $sellerT @(200) | Out-Null
Req 'POST /seller/inventory/adjust absoluto' POST '/api/v1/seller/inventory/adjust' @{productId=$prodId;physicalQuantity=30;reason='Recuento'} $sellerT @(200) | Out-Null
Req 'GET /seller/inventory' GET '/api/v1/seller/inventory' $null $sellerT @(200) | Out-Null

Write-Host "`n================ 6. CARRITO Y CHECKOUT ================" -ForegroundColor Cyan
Req 'GET /cart' GET '/api/v1/cart' $null $buyerT @(200) | Out-Null
Req 'POST /cart/items' POST '/api/v1/cart/items' @{productId=$prodId;quantity=2} $buyerT @(200) | Out-Null
Req 'POST /cart/items (2)' POST '/api/v1/cart/items' @{productId=$prod2Id;quantity=1} $buyerT @(200) | Out-Null
Req 'PUT /cart/items/{id}' PUT "/api/v1/cart/items/$prodId" @{quantity=1} $buyerT @(200) | Out-Null
Req 'POST /cart/items stock excesivo' POST '/api/v1/cart/items' @{productId=$prod2Id;quantity=9999} $buyerT @(400) | Out-Null

$co = Req 'POST /orders/checkout' POST '/api/v1/orders/checkout' @{notes='Prueba e2e'} $buyerT @(201)
$orderId = $co.order.orderId
$subOrderId = $co.order.subOrders[0].subOrderId
Write-Host ("        orden={0} estado={1} pagado={2} subOrdenes={3}" -f $co.order.orderNumber, $co.order.status, $co.paymentCaptured, $co.order.subOrders.Count) -ForegroundColor Gray

Req 'GET /buyer/orders' GET '/api/v1/buyer/orders' $null $buyerT @(200) | Out-Null
Req 'GET /buyer/orders/{id}' GET "/api/v1/buyer/orders/$orderId" $null $buyerT @(200) | Out-Null
Req 'GET /seller/orders?status=PROCESSING' GET '/api/v1/seller/orders?status=PROCESSING' $null $sellerT @(200) | Out-Null
Req 'PATCH /seller/orders/{id}/ship' PATCH "/api/v1/seller/orders/$subOrderId/ship" @{trackingNumber='TRK-E2E-001';carrier='DHL'} $sellerT @(200) | Out-Null
Req 'GET /admin/orders' GET '/api/v1/admin/orders' $null $adminT @(200) | Out-Null

Write-Host "`n================ 7. RESENAS ================" -ForegroundColor Cyan
$rv = Req 'POST /buyer/reviews' POST '/api/v1/buyer/reviews' @{productId=$prodId;rating=5;title='Excelente';comment='Muy buen producto'} $buyerT @(201)
Req 'GET /reviews/product/{id}' GET "/api/v1/reviews/product/$prodId" $null $null @(200) | Out-Null
Req 'GET /admin/reviews' GET '/api/v1/admin/reviews' $null $adminT @(200) | Out-Null
Req 'DELETE /admin/reviews/{id}' DELETE "/api/v1/admin/reviews/$($rv.reviewId)" $null $adminT @(204) | Out-Null

Write-Host "`n================ 8. VALIDACION ================" -ForegroundColor Cyan
Req 'POST register/buyer email invalido' POST '/api/v1/auth/register/buyer' @{email='no-es-email';password=$pw} $null @(400) | Out-Null
Req 'POST review rating fuera de rango' POST '/api/v1/buyer/reviews' @{productId=$prodId;rating=9} $buyerT @(400) | Out-Null
Req 'PATCH ship sin tracking' PATCH "/api/v1/seller/orders/$subOrderId/ship" @{trackingNumber=''} $sellerT @(400) | Out-Null

Write-Host "`n==================================================" -ForegroundColor Cyan
Write-Host ("RESULTADO: {0} OK  /  {1} FALLOS" -f $global:ok, $global:fail) -ForegroundColor $(if ($global:fail -eq 0) { 'Green' } else { 'Red' })
