// Подключение библиотеки Workbox через CDN (для производственной среды)
importScripts('/workbox-sw.js');

workbox.setConfig({ debug: false, modulePathPrefix: '/' });

// 1. Инъекция манифеста (Precaching). 
// Вызов injectManifest от Workbox CLI заменит специальный токен на массив объектов.
// Это гарантирует, что Wasm-модуль и загрузочные JS-скрипты скачаются в фоне.
let manifest = [];
try {
    manifest = self.__WB_MANIFEST || [];
} catch (e) {
    console.log("PWA: __WB_MANIFEST не найден, используем пустой массив (режим разработчика)");
}
workbox.precaching.precacheAndRoute(manifest);

// 2. Обработка сигналов жизненного цикла для грациозного обновления (Graceful Update).
// Позволяет клиентскому коду (из Compose) форсировать активацию нового Service Worker,
// минуя фазу 'waiting', что необходимо для паттерна "Нажмите, чтобы обновить".
self.addEventListener('message', (event) => {
   if (event.data && event.data.type === 'SKIP_WAITING') {
       self.skipWaiting();
   }
});

// 3. Немедленный захват управления над вкладками браузера после активации
workbox.core.clientsClaim();

// 4. Кэширование навигационных запросов (поддержка Compose SPA роутинга)
// Любой прямой переход по ссылке (например, /profile) должен возвращать index.html
workbox.routing.registerRoute(
   ({request}) => request.mode === 'navigate',
   new workbox.strategies.NetworkFirst({
       cacheName: 'pages-cache',
       plugins: [
           new workbox.cacheableResponse.CacheableResponsePlugin({
               statuses: [200],
           })
       ]
   })
);

// 5. Кэширование внешних шрифтов (например, Google Fonts)
workbox.routing.registerRoute(
   ({url}) => url.origin === 'https://fonts.googleapis.com' || url.origin === 'https://fonts.gstatic.com',
   new workbox.strategies.StaleWhileRevalidate({
       cacheName: 'google-fonts-cache',
       plugins: [
           new workbox.expiration.ExpirationPlugin({
               maxEntries: 20,
               maxAgeSeconds: 60 * 60 * 24 * 365, // Кэшируем на 1 год
           }),
       ],
   })
);

// 6. Оптимизированное кэширование изображений
workbox.routing.registerRoute(
   ({request}) => request.destination === 'image',
   new workbox.strategies.CacheFirst({
       cacheName: 'images-cache',
       plugins: [
           new workbox.expiration.ExpirationPlugin({
               maxEntries: 100, // Ограничение на 100 изображений в кэше
               maxAgeSeconds: 30 * 24 * 60 * 60, // Кэшируем на 30 дней
               purgeOnQuotaError: true, // Очистка при нехватке места
           }),
       ],
   })
);
