package app.service

import com.maxmind.geoip2.DatabaseReader
import com.maxmind.geoip2.exception.AddressNotFoundException
import com.maxmind.geoip2.model.CountryResponse
import java.net.InetAddress
import org.koin.core.annotation.Single

@Single
class GeoService {

    private val dbReader: DatabaseReader

    init {
        // Читаем файл из папки resources (работает и в IDE, и в собранном JAR)
        val stream = this::class.java.classLoader.getResourceAsStream("GeoLite2-Country.mmdb")
            ?: throw RuntimeException("GeoLite2-Country.mmdb not found in resources! Put it in src/main/resources/")

        // Строим ридер на основе потока
        dbReader = DatabaseReader.Builder(stream).build()
    }

    fun resolveCountry(ip: String): String? {
        return try {
            // 1. Отсеиваем локальные IP (их нет в базе)
            if (isLocalIp(ip)) return null

            val ipAddress = InetAddress.getByName(ip)

            // 2. Используем метод country(), так как база Country
            // (если бы была база City, использовали бы city())
            val response: CountryResponse = dbReader.country(ipAddress)

            // 3. НОВЫЙ СИНТАКСИС (Java Records):
            // Вызываем методы как функции со скобками ()
            response.country().isoCode()

        } catch (e: AddressNotFoundException) {
            null // IP не найден (нормальная ситуация)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun isLocalIp(ip: String): Boolean {
        return ip == "127.0.0.1" || ip == "localhost" ||
                ip == "0:0:0:0:0:0:0:1" ||
                ip.startsWith("192.168.") || ip.startsWith("10.")
    }
}
