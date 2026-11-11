package app.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*


fun Application.gzipPlugin() {

    install(Compression) {
        gzip {
            priority = 1.0

            // Сжимаем только если ответ больше 1 КБ.
            // Меньше сжимать смысла нет — заголовки сожрут выигрыш.
            minimumSize(1024)

            matchContentType(
                ContentType.Application.Json,  // основной
                ContentType.Text.Any,        // Текстовые ответы
                ContentType.Application.Xml  // На всякий случай
            )

            // Явно исключаем медиа (аватарки и прочее), чтобы не тратить CPU
            excludeContentType(
                ContentType.Image.Any,
                ContentType.Video.Any,
                ContentType.Audio.Any
            )
        }
    }
}
