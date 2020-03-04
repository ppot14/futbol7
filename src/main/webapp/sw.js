//const version = "0.0.1-SNAPSHOT";
const cacheName = 'futbol7';
self.addEventListener('install', e => {
  e.waitUntil(
    caches.open(cacheName).then(cache => {
      return cache.addAll([
        '/'
//        '/index.jsp',
//        '/resources/styles/main.css',
//        '/resources/styles/lib/bootstrap-slider.min.css',
//        '/resources/styles/lib/bootstrap-table.min.css',
//        '/resources/styles/lib/bootstrap.min.css',
//        '/resources/styles/lib/font-awesome.min.css',
//        '/resources/scripts/login.js',
//        '/resources/scripts/main.js',
//        '/resources/scripts/lib/bootstrap-slider.min.js',
//        '/resources/scripts/lib/bootstrap-table-es-ES.min.js',
//        '/resources/scripts/lib/bootstrap-table.min.js',
//        '/resources/scripts/lib/bootstrap.min.js',
//        '/resources/scripts/lib/highcharts.js',
//        '/resources/scripts/lib/jquery-1.11.3.min.js',
//        '/resources/scripts/lib/jquery.timeago.min.js',
//        '/resources/scripts/lib/platform.js'
      ])
          .then(() => self.skipWaiting());
    })
  );
});

//self.addEventListener('activate', event => {
//  event.waitUntil(self.clients.claim());
//});

self.addEventListener('fetch', event => {
//	console.log(event.request.url);
  event.respondWith(
    fetch(event.request).catch(function() {
      return caches.match(event.request);
    })
  );
});