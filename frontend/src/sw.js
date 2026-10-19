// Finance Tracker PWA Service Worker
// Version: 1.0.1 (Cache v3)
// STRICT SECURITY POLICY:
// 1. NEVER cache /api/** responses or sensitive financial records.
// 2. NEVER cache authentication tokens or private user data.
// 3. In local development (localhost / 127.0.0.1), always bypass cache.
// 4. In production, use network-first for unhashed bundles to guarantee fresh routing.

const CACHE_NAME = 'finance-tracker-shell-v3';
const STATIC_ASSETS = [
  '/',
  '/index.html',
  '/manifest.webmanifest',
  '/offline.html',
  '/assets/icons/icon.svg',
  '/assets/icons/icon-192.png',
  '/assets/icons/icon-512.png',
  '/assets/icons/icon-maskable-512.png',
  '/assets/icons/icon-maskable.svg'
];

// Install: Precache app shell
self.addEventListener('install', (event) => {
  self.skipWaiting();
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      return cache.addAll(STATIC_ASSETS);
    })
  );
});

// Activate: Purge obsolete caches
self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames
          .filter((name) => name !== CACHE_NAME)
          .map((name) => caches.delete(name))
      );
    }).then(() => self.clients.claim())
  );
});

// Fetch: Secure routing
self.addEventListener('fetch', (event) => {
  const request = event.request;
  const url = new URL(request.url);

  // 1. STRICT EXCLUSION: Never intercept or cache API endpoints or non-GET methods
  if (request.method !== 'GET' || url.pathname.startsWith('/api/')) {
    return; // Pass through directly to network
  }

  // 2. In local development or hot-reload dev servers, always pass through directly
  if (url.hostname === 'localhost' || url.hostname === '127.0.0.1') {
    return;
  }

  // 3. Navigation requests (HTML pages) -> Network first, fallback to cached index or offline.html
  if (request.mode === 'navigate') {
    event.respondWith(
      fetch(request).catch(async () => {
        const cache = await caches.open(CACHE_NAME);
        const cachedIndex = await cache.match('/index.html');
        if (cachedIndex) return cachedIndex;
        return cache.match('/offline.html');
      })
    );
    return;
  }

  // 4. Unhashed core bundles -> Network first
  if (url.pathname === '/main.js' || url.pathname === '/polyfills.js' || url.pathname === '/styles.css') {
    event.respondWith(
      fetch(request).catch(() => caches.match(request))
    );
    return;
  }

  // 5. Static assets: Cache-first with network fallback for hashed immutable assets
  if (
    url.pathname.endsWith('.js') ||
    url.pathname.endsWith('.css') ||
    url.pathname.endsWith('.woff2') ||
    url.pathname.endsWith('.png') ||
    url.pathname.endsWith('.svg')
  ) {
    event.respondWith(
      caches.match(request).then((cachedResponse) => {
        if (cachedResponse) {
          return cachedResponse;
        }
        return fetch(request).then((networkResponse) => {
          if (networkResponse && networkResponse.status === 200) {
            const responseToCache = networkResponse.clone();
            caches.open(CACHE_NAME).then((cache) => {
              cache.put(request, responseToCache);
            });
          }
          return networkResponse;
        });
      })
    );
    return;
  }

  // 6. Default: Network with offline fallback
  event.respondWith(
    fetch(request).catch(() => caches.match(request))
  );
});

// ==========================================
// Native Phone Notification Handling
// ==========================================

// Handle tapping on a system notification from phone lock screen or notification tray
self.addEventListener('notificationclick', (event) => {
  event.notification.close();

  const targetPath = event.notification.data?.url || '/dashboard';
  const targetUrl = new URL(targetPath, self.location.origin).href;

  event.waitUntil(
    self.clients.matchAll({ type: 'window', includeUncontrolled: true }).then((windowClients) => {
      // If a tab is already open, focus it and navigate
      for (const client of windowClients) {
        if (client.url.includes(self.location.origin) && 'focus' in client) {
          if ('navigate' in client) {
            client.navigate(targetUrl);
          }
          return client.focus();
        }
      }
      // Otherwise open a new window
      if (self.clients.openWindow) {
        return self.clients.openWindow(targetUrl);
      }
    })
  );
});

// Handle background push event if triggered from push server
self.addEventListener('push', (event) => {
  if (!event.data) return;

  try {
    const payload = event.data.json();
    const title = payload.title || '🔔 Finance Tracker Alert';
    const options = {
      body: payload.body || 'You have a new financial update.',
      icon: '/assets/icons/icon-192.png',
      badge: '/assets/icons/icon-192.png',
      tag: payload.tag || 'finance-alert',
      vibrate: [200, 100, 200],
      data: { url: payload.url || '/dashboard' }
    };
    event.waitUntil(self.registration.showNotification(title, options));
  } catch (err) {
    const title = '🔔 Finance Tracker Alert';
    const options = {
      body: event.data.text(),
      icon: '/assets/icons/icon-192.png',
      badge: '/assets/icons/icon-192.png',
      tag: 'finance-alert',
      vibrate: [200, 100, 200],
      data: { url: '/dashboard' }
    };
    event.waitUntil(self.registration.showNotification(title, options));
  }
});

