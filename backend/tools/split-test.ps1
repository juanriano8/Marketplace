# ============================================================================
# Prueba definitiva: carrito con productos de DOS vendedores -> 2 sub-ordenes
# ============================================================================
$B = 'http://localhost:8080'
$dir = Join-Path $env:TEMP 'mksplit'; New-Item -ItemType Directory -Force -Path $dir | Out-Null

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
$sfx = Get-Random -Maximum 99999
$pw = 'Password123!'

$mA = (Call POST '/api/v1/auth/register/seller' @{email="mA$sfx@m.com";password=$pw} $null).json
$mB = (Call POST '/api/v1/auth/register/seller' @{email="mB$sfx@m.com";password=$pw} $null).json
Call PATCH "/api/v1/admin/sellers/$($mA.userId)/verify" @{approved=$true} $adminT | Out-Null
Call PATCH "/api/v1/admin/sellers/$($mB.userId)/verify" @{approved=$true} $adminT | Out-Null

$pA = (Call POST '/api/v1/seller/products' @{name="Split A $sfx";price=100.00;currencyCode='USD';stockQuantity=10;category='Test'} $mA.accessToken).json
$pB = (Call POST '/api/v1/seller/products' @{name="Split B $sfx";price=50.00;currencyCode='USD';stockQuantity=10;category='Test'} $mB.accessToken).json
Call PATCH "/api/v1/admin/products/$($pA.id)/approval" @{approved=$true} $adminT | Out-Null
Call PATCH "/api/v1/admin/products/$($pB.id)/approval" @{approved=$true} $adminT | Out-Null

$buyer = (Call POST '/api/v1/auth/register/buyer' @{email="split$sfx@m.com";password=$pw} $null).json

Write-Host "Vendedor A: $($pA.name) ($($pA.price))  |  Vendedor B: $($pB.name) ($($pB.price))" -ForegroundColor Gray

$r1 = Call POST '/api/v1/cart/items' @{productId=$pA.id;quantity=2} $buyer.accessToken
Write-Host ("1) Agregar producto A (vendedor A) x2  -> HTTP {0}" -f $r1.code) -ForegroundColor $(if($r1.code -eq 200){'Green'}else{'Red'})
$r2 = Call POST '/api/v1/cart/items' @{productId=$pB.id;quantity=3} $buyer.accessToken
Write-Host ("2) Agregar producto B (vendedor B) x3  -> HTTP {0}" -f $r2.code) -ForegroundColor $(if($r2.code -eq 200){'Green'}else{'Red'})

$cart = Call GET '/api/v1/cart' $null $buyer.accessToken
Write-Host ("3) Carrito: {0} lineas, subtotal {1} {2}" -f $cart.json.items.Count, $cart.json.subtotal, $cart.json.currencyCode) -ForegroundColor Gray

$co = Call POST '/api/v1/orders/checkout' @{notes='split test'} $buyer.accessToken
Write-Host ("4) Checkout -> HTTP {0}" -f $co.code) -ForegroundColor $(if($co.code -eq 201){'Green'}else{'Red'})

if ($co.code -eq 201) {
  $o = $co.json.order
  Write-Host ""
  Write-Host "   Orden unica : $($o.orderNumber)" -ForegroundColor White
  Write-Host "   Total       : $($o.totalAmount) $($o.currencyCode)  (esperado 350.00 = 2x100 + 3x50)" -ForegroundColor White
  Write-Host "   Estado      : $($o.status)" -ForegroundColor White
  Write-Host "   Sub-ordenes : $($o.subOrders.Count)  (esperado 2, una por vendedor)" -ForegroundColor $(if($o.subOrders.Count -eq 2){'Green'}else{'Red'})
  Write-Host ""
  $i = 1
  foreach ($s in $o.subOrders) {
    $seller = if ($s.sellerId -eq $mA.userId) { 'Vendedor A' } elseif ($s.sellerId -eq $mB.userId) { 'Vendedor B' } else { $s.sellerId }
    Write-Host ("   sub-orden {0}: {1} | subtotal {2} | estado {3} | items: {4}" -f $i, $seller, $s.subtotal, $s.status, ($s.items | ForEach-Object { "$($_.productName) x$($_.quantity)" }) -join ', ') -ForegroundColor Gray
    $i++
  }

  # Cada vendedor ve SOLO su parte
  Write-Host ""
  $subA = ($o.subOrders | Where-Object { $_.sellerId -eq $mA.userId }).subOrderId
  $subB = ($o.subOrders | Where-Object { $_.sellerId -eq $mB.userId }).subOrderId
  $listA = Call GET '/api/v1/seller/orders?status=PROCESSING' $null $mA.accessToken
  Write-Host ("5) Vendedor A ve {0} sub-orden(es) en su cola de despacho" -f $listA.json.totalElements) -ForegroundColor Gray
  $cross = Call PATCH "/api/v1/seller/orders/$subB/ship" @{trackingNumber='HACK';carrier='X'} $mA.accessToken
  Write-Host ("6) Vendedor A intenta despachar la sub-orden de B -> HTTP {0} {1}" -f $cross.code, $(if($cross.code -eq 400){'(BLOQUEADO por BOLA)'}else{''})) -ForegroundColor $(if($cross.code -eq 400){'Green'}else{'Red'})

  $shipA = Call PATCH "/api/v1/seller/orders/$subA/ship" @{trackingNumber="TRK-A-$sfx";carrier='DHL'} $mA.accessToken
  Write-Host ("7) Vendedor A despacha su propia sub-orden -> HTTP {0} estado {1}" -f $shipA.code, $shipA.json.status) -ForegroundColor $(if($shipA.code -eq 200){'Green'}else{'Red'})
}
