import { Workbox } from 'workbox-window';

let wb = null;
let deferredInstallPrompt = null;

/**
* Инициализирует PWA, регистрирует Service Worker и перехватывает системные события.
* @param {Function} onUpdateAvailable Коллбек для Kotlin (появилось обновление)
* @param {Function} onInstallPromptAvailable Коллбек для Kotlin (приложение можно установить)
*/
export function initializePwaBridge(onUpdateAvailable, onInstallPromptAvailable) {
   if ('serviceWorker' in navigator) {
       // Указываем путь к сгенерированному скрипту
       wb = new Workbox('/sw.js');

       // Отслеживаем событие 'waiting' - новая версия скачана и ожидает активации
       wb.addEventListener('waiting', () => {
           console.log("PWA: Обновление загружено в фоне.");
           onUpdateAvailable();
       });

       // Отслеживаем событие 'controlling' - новый SW взял управление
       wb.addEventListener('controlling', () => {
           // Мгновенная перезагрузка для загрузки новой версии в память браузера
           window.location.reload();
       });

       wb.register().catch(error => console.error("PWA: Ошибка регистрации", error));
   }

   // Перехват системного события перед установкой приложения
   window.addEventListener('beforeinstallprompt', (event) => {
       // Предотвращаем стандартный показ мини-инфобара браузера
       event.preventDefault();
       // Сохраняем событие для последующего вызова из интерфейса Compose
       deferredInstallPrompt = event;
       console.log("PWA: Событие установки перехвачено.");
       onInstallPromptAvailable();
   });
}

/**
* Отправляет сообщение в Service Worker для применения новой версии
*/
export function applyPwaUpdate() {
   if (wb) {
       // Сигнал обработчику self.addEventListener('message') в sw-template.js
       wb.messageSW({ type: 'SKIP_WAITING' });
   }
}

/**
* Вызывает сохраненный системный промпт установки
* @returns {Promise<boolean>} Результат выбора пользователя
*/
export async function promptPwaInstall() {
   if (!deferredInstallPrompt) {
       return false;
   }
   // Показываем нативный системный диалог
   deferredInstallPrompt.prompt();
   // Ожидаем решения пользователя (установил или отменил)
   const { outcome } = await deferredInstallPrompt.userChoice;
   // Очищаем переменную, так как событие можно вызвать только один раз
   deferredInstallPrompt = null;
   return outcome === 'accepted';
}

/**
 * Проверяет, является ли устройство iOS (iPhone, iPad, iPod)
 * @returns {boolean}
 */
export function isIosDevice() {
    const userAgent = window.navigator.userAgent.toLowerCase();
    return /iphone|ipad|ipod/.test(userAgent);
}

/**
 * Проверяет, запущено ли приложение уже как PWA (на домашнем экране)
 * @returns {boolean}
 */
export function isStandaloneMode() {
    // В iOS safari есть нестандартное свойство navigator.standalone
    const isStandalone = (window.navigator.standalone === true) || window.matchMedia('(display-mode: standalone)').matches;
    return isStandalone;
}

/**
 * Сбрасывает Service Worker и блокировки для реабилитации зависших OPFS Handle
 */
export function hardResetPwa() {
    if ('serviceWorker' in navigator) {
        navigator.serviceWorker.getRegistrations().then(function(registrations) {
            for (let registration of registrations) {
                registration.unregister();
            }
        }).finally(() => {
            window.location.reload();
        });
    } else {
        window.location.reload();
    }
}

