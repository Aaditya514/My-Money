$urls = @(
    'http://localhost:4200/',
    'http://localhost:4200/manifest.webmanifest',
    'http://localhost:4200/sw.js',
    'http://localhost:4200/offline.html',
    'http://localhost:4200/assets/icons/icon.svg',
    'http://localhost:4200/assets/icons/icon-192.png',
    'http://localhost:4200/assets/icons/icon-512.png',
    'http://localhost:4200/assets/icons/icon-maskable-512.png',
    'http://localhost:4200/assets/icons/icon-maskable.svg'
)

foreach ($u in $urls) {
    try {
        $res = Invoke-WebRequest -Uri $u -Method Head -UseBasicParsing
        Write-Host "$u -> StatusCode: $($res.StatusCode)"
    } catch {
        Write-Host "$u -> Error: $($_.Exception.Message)"
    }
}
