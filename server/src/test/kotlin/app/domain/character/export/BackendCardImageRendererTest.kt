package app.domain.character.export

import com.ensodai.shared.avalon.character.spec.AvalonCardCustomization
import com.ensodai.shared.avalon.character.spec.CardAccentToken
import com.ensodai.shared.avalon.character.spec.CardBgPattern
import com.ensodai.shared.avalon.character.spec.CardFallbackGlyph
import com.ensodai.shared.avalon.character.spec.CardLayout
import com.ensodai.shared.avalon.character.spec.PngCharacterCardCodec
import kotlinx.coroutines.runBlocking
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BackendCardImageRendererTest {

    private val renderer = BackendCardImageRenderer()

    @Test
    fun testRenderMonolithPosterHdDimensionsAndSpecs() = runBlocking {
        // 1. Создаем тестовую аватарку
        val testAvatar = BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB)
        val g = testAvatar.createGraphics()
        g.color = Color(30, 40, 60)
        g.fillRect(0, 0, 400, 400)
        g.color = Color(0, 229, 255)
        g.fillOval(50, 50, 300, 300)
        g.dispose()

        val avatarBaos = ByteArrayOutputStream()
        ImageIO.write(testAvatar, "png", avatarBaos)
        val avatarBytes = avatarBaos.toByteArray()

        // 2. Рендерим постер Monolith
        val posterBytes = renderer.renderCardToPng(
            characterName = "Алиса Нео-Токио",
            nickname = "Кибер-спутница",
            creatorNick = "ensodai",
            shortDescription = "Автономный тактический компаньон со встроенным модулем анализа данных и нейроинтерфейсом.",
            tags = listOf("Cyberpunk", "AI Core", "Companion", "Tactics"),
            avatarBytes = avatarBytes,
            hasLorebook = true,
            versionText = "v1.2",
            specVersion = "V3",
            tokenCount = 2450,
            altGreetingsCount = 3,
            customization = AvalonCardCustomization(
                layout = CardLayout.MONOLITH_FRAME,
                accentToken = CardAccentToken.CYAN,
                bgPattern = CardBgPattern.MATTE_OBSIDIAN,
                fallbackGlyph = CardFallbackGlyph.AI_CORE
            )
        )

        // 3. Проверяем валидность PNG и точные размеры 1200x1800
        assertNotNull(posterBytes)
        assertTrue(posterBytes.isNotEmpty())
        assertTrue(PngCharacterCardCodec.isPng(posterBytes), "Сгенерированные байты должны быть валидным PNG")

        val decodedImage = ImageIO.read(ByteArrayInputStream(posterBytes))
        assertNotNull(decodedImage, "ImageIO должен успешно прочитать сгенерированный PNG")
        assertEquals(1200, decodedImage.width, "Ширина постера должна быть строго 1200 px")
        assertEquals(1800, decodedImage.height, "Высота постера должна быть строго 1800 px")

        // 4. Проверяем вшивание и чтение чанка ccv3
        val testJson = """
            {
                "spec": "chara_card_v3",
                "spec_version": "3.0",
                "data": {
                    "name": "Алиса Нео-Токио",
                    "description": "Автономный тактический компаньон",
                    "personality": "Преданная, умная",
                    "scenario": "Нео-Токио 2099",
                    "first_mes": "Системы онлайн. Готова к работе.",
                    "tags": ["Cyberpunk", "AI Core"]
                }
            }
        """.trimIndent()

        val embeddedPng = PngCharacterCardCodec.embedCardJson(
            originalPngBytes = posterBytes,
            cardJson = testJson,
            keyword = "ccv3"
        )

        assertTrue(embeddedPng.size > posterBytes.size)
        val parsed = PngCharacterCardCodec.extractCard(embeddedPng)
        assertNotNull(parsed)
        assertEquals("v3", parsed.specVersion)
        assertEquals("Алиса Нео-Токио", parsed.data.name)

        // 5. Сохраняем в build/reports/monolith_card_test.png для верификации
        val reportDir = File("build/reports")
        reportDir.mkdirs()
        val outputFile = File(reportDir, "monolith_card_test.png")
        outputFile.writeBytes(embeddedPng)
        assertTrue(outputFile.exists())
        assertTrue(outputFile.length() > 50_000, "Размер постера HD должен превышать 50 КБ")
    }

    @Test
    fun testRenderFallbackGlyphWhenAvatarNull() = runBlocking {
        val posterBytes = renderer.renderCardToPng(
            characterName = "Кибер Кортекс",
            nickname = null,
            creatorNick = "system",
            shortDescription = "Персонаж без загруженного аватара.",
            tags = listOf("Synthetic"),
            avatarBytes = null,
            hasLorebook = false,
            versionText = "v1.0",
            specVersion = "V3",
            tokenCount = 850,
            altGreetingsCount = 0,
            customization = AvalonCardCustomization(
                layout = CardLayout.MONOLITH_FRAME,
                accentToken = CardAccentToken.CRIMSON,
                bgPattern = CardBgPattern.HEX_GRID,
                fallbackGlyph = CardFallbackGlyph.INITIALS
            )
        )

        assertNotNull(posterBytes)
        val decodedImage = ImageIO.read(ByteArrayInputStream(posterBytes))
        assertNotNull(decodedImage)
        assertEquals(1200, decodedImage.width)
        assertEquals(1800, decodedImage.height)
    }

    @Test
    fun testRenderDepressiveSusanCard() = runBlocking {
        // Создаем изображение в монохромных тонах под Депрессивную Сьюзан
        val testAvatar = BufferedImage(500, 600, BufferedImage.TYPE_INT_ARGB)
        val g = testAvatar.createGraphics()
        g.color = Color(20, 24, 28)
        g.fillRect(0, 0, 500, 600)
        g.color = Color(70, 80, 95)
        g.fillRoundRect(80, 80, 340, 440, 40, 40)
        g.dispose()

        val avatarBaos = ByteArrayOutputStream()
        ImageIO.write(testAvatar, "png", avatarBaos)

        val posterBytes = renderer.renderCardToPng(
            characterName = "Депрессивная Сьюзан",
            nickname = null,
            creatorNick = "WhySoLir",
            shortDescription = "Она из вашей компании, грустная, застенчивая, закрытая",
            tags = emptyList(),
            avatarBytes = avatarBaos.toByteArray(),
            hasLorebook = false,
            versionText = "v1.0",
            specVersion = "V3",
            tokenCount = 1000,
            altGreetingsCount = 0,
            customization = AvalonCardCustomization(
                layout = CardLayout.MONOLITH_FRAME,
                accentToken = CardAccentToken.CYAN,
                bgPattern = CardBgPattern.MATTE_OBSIDIAN
            )
        )

        assertNotNull(posterBytes)
        val decodedImage = ImageIO.read(ByteArrayInputStream(posterBytes))
        assertNotNull(decodedImage)
        assertEquals(1200, decodedImage.width)
        assertEquals(1800, decodedImage.height)

        val reportDir = File("build/reports")
        reportDir.mkdirs()
        File(reportDir, "susan_card_test.png").writeBytes(posterBytes)
    }

    @Test
    fun testRenderQuietTownKregCard() = runBlocking {
        val avatarBytes: ByteArray? = null

        val posterBytes = renderer.renderCardToPng(
            characterName = "Тихий город Крег",
            nickname = null,
            creatorNick = "Cap",
            shortDescription = "Ты просыпаешься в тихом городе,но что случилось с ним?",
            tags = emptyList(),
            avatarBytes = avatarBytes,
            hasLorebook = false,
            versionText = "v1.0",
            specVersion = "V3",
            tokenCount = 1000,
            altGreetingsCount = 0,
            customization = AvalonCardCustomization(
                layout = CardLayout.MONOLITH_FRAME,
                accentToken = CardAccentToken.CYAN,
                bgPattern = CardBgPattern.MATTE_OBSIDIAN
            )
        )

        assertNotNull(posterBytes)
        val decodedImage = ImageIO.read(ByteArrayInputStream(posterBytes))
        assertNotNull(decodedImage)
        assertEquals(1200, decodedImage.width)
        assertEquals(1800, decodedImage.height)

        val reportDir = File("build/reports")
        reportDir.mkdirs()
        File(reportDir, "kreg_card_test.png").writeBytes(posterBytes)
    }
}
