# ============================================================================
# Verifica la regla antisobreventa y la division de orden por vendedor
# ============================================================================
$B = 'http://localhost:8080'
$dir = Join-Path $env:TEMP 'mkstock'; New-Item -ItemType Directory -Force -Path $dir | Out-Null

$v = @{}
Get-Content backend\.env | ForEach-Object { $l=$_.Trim(); if ($l -and -not $l.StartsWith('#') -and $l.Contains('=')) { $i=$l.IndexOf('='); $v[$l.Substring(0,$i).Trim()]=$l.Substring($i+1).Trim() } }

function Call($method, $path, $obj, $token) {
  $bf = Join-Path $dir 'b.json'; $rf = Join-Path $dir 'r.json'
  $a = @('-s','-o',$rf,'-w','%{http_code}','-X',$method,"$B$path",'--max-time','30')
  if ($null -ne $obj) { [System.IO.File]::WriteAllText($bf, ($obj | ConvertTo-Json -Compress -Depth 6)); $a += @('-H','Content-Type: application/json','--data-binary',"@$bf") }
  if ($token) { $a += @('-H',"Authorization: Bearer $token") }
  $code = [int](& curl.exe @a)
  $raw = Get-Content $rf -Raw -ErrorAction SilentlyContinue
  $j = $null; if ($raw) { try { $j = $raw | ConvertFrom-Json } catch {} }
  return @{ code = $code; json = $j; raw = $raw }
}

$adminT = (Call POST '/api/v1/auth/login' @{email=$v['BOOTSTRAP_ADMIN_EMAIL'];password=$v['BOOTSTRAP_ADMIN_PASSWORD']} $null).json.accessToken
$suffix = Get-Random -Maximum 99999
$pw = 'Password123!'

# Dos vendedores distintos para forzar la division de la orden
$sA = (Call POST '/api/v1/auth/register/seller' @{email="stkA$suffix@m.com";password=$pw} $null).json
$sB = (Call POST '/api/v1/auth/register/seller' @{email="stkB$suffix@m.com";password=$pw} $null).json
Call PATCH "/api/v1/admin/sellers/$($sA.userId)/verify" @{approved=$true} $adminT | Out-Null
Call PATCH "/api/v1/admin/sellers/$($sB.userId)/verify" @{approved=$true} $adminT | Out-Null

# Producto del vendedor A con UNA sola unidad
$pA = (Call POST '/api/v1/seller/products' @{name="UnicoA $suffix";price=100;currencyCode='USD';stockQuantity=1;category='Test'} $sA.accessToken).json
Call PATCH "/api/v1/admin/products/$($pA.id)/approval" @{approved=$true} $adminT | Out-Null
# Producto del vendedor B
$pB = (Call POST '/api/v1/seller/products' @{name="Prod B $suffix";price=50;currencyCode='USD';stockQuantity=5;category='Test'} $sB.accessToken).json
Call PATCH "/api/v1/admin/products/$($pB.id)/approval" @{approved=$true} $adminT | Out-Null

$buyer1 = (Call POST '/api/v1/auth/register/buyer' @{email="b1-$suffix@m.com";password=$pw} $null).json
$buyer2 = (Call POST '/api/v1/auth/register/buyer' @{email="b2-$suffix@m.com";password=$pw} $null).json

Write-Host "=== ESCENARIO: stock de 1 unidad, dos compradores ===" -ForegroundColor Cyan
Write-Host "Producto A ($($pA.name)): 1 unidad | Producto B ($($pB.name)): 5 unidades`n"

# Comprador 1 se lleva la unica unidad del producto A
Call POST '/api/v1/cart/items' @{productId=$pA.id;quantity=1} $buyer1.accessToken | Out-Null
$co1 = Call POST '/api/v1/orders/checkout' @{notes='compra 1'} $buyer1.accessToken
Write-Host ("1) Comprador 1 compra la ultima unidad  -> HTTP {0}  {1}" -f $co1.code, $(if($co1.code -eq 201){'ORDEN CREADA'}else{'FALLO'})) -ForegroundColor $(if($co1.code -eq 201){'Green'}else{'Red'})
if ($co1.code -eq 201) {
  Write-Host ("   orden {0} | estado {1} | subOrdenes {2} | total {3}" -f $co1.json.order.orderNumber, $co1.json.order.status, $co1.json.order.subOrders.Count, $co1.json.order.totalAmount) -ForegroundColor Gray
}

# Comprador 2 intenta lo mismo: ya no hay stock
$add2 = Call POST '/api/v1/cart/items' @{productId=$pA.id;quantity=1} $buyer2.accessToken
Write-Host ("2) Comprador 2 intenta agregar la unidad ya vendida -> HTTP {0}" -f $add2.code) -ForegroundColor $(if($add2.code -eq 400){'Green'}else{'Red'})
if ($add2.code -eq 400) { Write-Host ("   motivo: {0}" -f $add2.json.detail) -ForegroundColor Gray }

# Estado final del stock
$chk = Call GET "/api/v1/inventory/check/$($pA.id)" $null $null
Write-Host ("`n3) Stock final del producto A -> HTTP {0}" -f $chk.code) -ForegroundColor Gray
Write-Host ("   disponible={0} reservado={1} vendible={2} bajoStock={3}" -f $chk.json.availableQuantity, $chk.json.reservedQuantity, $chk.json.sellableQuantity, $chk.json.lowStock)

Write-Host "`n=== ESCENARIO: carrito con productos de DOS vendedores ===" -ForegroundColor Cyan
Call POST '/api/v1/cart/items' @{productId=$pB.id;quantity=2} $buyer2.accessToken | Out-Null
$co2 = Call POST '/api/v1/orders/checkout' @{notes='multi-vendedor'} $buyer2.accessToken
Write-Host ("4) Checkout multi-vendedor -> HTTP {0}" -f $co2.code) -ForegroundColor $(if($co2.code -eq 201){'Green'}else{'Red'})
if ($co2.code -eq 201) {
  Write-Host ("   orden {0} | estado {1} | total {2}" -f $co2.json.order.orderNumber, $co2.json.order.status, $co2.json.order.totalAmount) -ForegroundColor Gray
  Write-Host ("   subOrdenes creadas: {0}" -f $co2.json.order.subOrders.Count) -ForegroundColor Gray
}

Write-Host "`n=== AUDITORIA DE MOVIMIENTOS (ledger) ===" -ForegroundColor Cyan
$aud = Call GET "/api/v1/admin/inventory/audit?productId=$($pA.id)&size=10" $null $adminT
Write-Host ("   HTTP {0} | movimientos registrados: {1}" -f $aud.code, $aud.json.totalElements) -ForegroundColor Gray
if ($aud.json.content) {
  $aud.json.content | ForEach-Object { Write-Host ("     - {0,-12} delta={1,-4} {2} -> {3}" -f $_.movementType, $_.quantityDelta, $_.quantityBefore, $_.quantityAfter) -ForegroundColor DarkGray }
}
