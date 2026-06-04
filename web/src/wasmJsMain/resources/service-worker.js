// Basic service worker to enable PWA features on mobile browsers
self.addEventListener('install', (event) => {
    self.skipWaiting();
});

self.addEventListener('activate', (event) => {
    event.waitUntil(clients.claim());
});

self.addEventListener('fetch', (event) => {
    // Standard fetch - we're not caching for now to avoid dev issues
    event.respondWith(fetch(event.request));
});
