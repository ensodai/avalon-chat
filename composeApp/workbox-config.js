module.exports = {
  // Директория, куда Gradle помещает готовую сборку (productionExecutable)
  globDirectory: "build/dist/wasmJs/productionExecutable/",
  
  // Паттерны файлов, формирующих ядро приложения (App Shell)
  globPatterns: [
    "**/*.{html,js,mjs,wasm,css,png,svg,woff,woff2,ttf,cvr,json,txt,ico}"
  ],
  
  // Исключаем из кэша карты кода, так как они нужны только для отладки
  globIgnores: [
    "**/*.map"
  ],
  
  // Увеличиваем лимит кэширования до 30 МБ для поддержки объемных Wasm-сборок
  maximumFileSizeToCacheInBytes: 30 * 1024 * 1024,
  
  // Указываем исходный шаблон нашего пользовательского Service Worker
  swSrc: "src/wasmJsMain/resources/sw.js",
  
  // Указываем путь назначения для финального файла, который будет отдан сервером
  swDest: "build/dist/wasmJs/productionExecutable/sw.js"
};
