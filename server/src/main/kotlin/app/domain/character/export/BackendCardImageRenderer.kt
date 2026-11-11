package app.domain.character.export

import com.ensodai.shared.avalon.character.spec.AvalonCardCustomization
import com.ensodai.shared.avalon.character.spec.CardAccentToken
import com.ensodai.shared.avalon.character.spec.CardBgPattern
import com.ensodai.shared.avalon.character.spec.CardFallbackGlyph
import com.ensodai.shared.avalon.character.spec.CardImageRenderer
import com.ensodai.shared.avalon.character.spec.CardLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.GradientPaint
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.Path2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

@Factory
class BackendCardImageRenderer : CardImageRenderer {

    companion object {
        const val CARD_WIDTH = 1200
        const val CARD_HEIGHT = 1800
        private const val PADDING_X = 68
        private const val OUTER_CORNER_RADIUS = 96f
        private const val INNER_FRAME_CORNER_RADIUS = 68f
    }

    override suspend fun renderCardToPng(
        characterName: String,
        nickname: String?,
        creatorNick: String?,
        shortDescription: String,
        tags: List<String>,
        avatarBytes: ByteArray?,
        hasLorebook: Boolean,
        versionText: String,
        specVersion: String,
        tokenCount: Int,
        altGreetingsCount: Int,
        customization: AvalonCardCustomization
    ): ByteArray = withContext(Dispatchers.IO) {
        val image = BufferedImage(CARD_WIDTH, CARD_HEIGHT, BufferedImage.TYPE_INT_ARGB)
        val g2d = image.createGraphics()

        try {
            setupRenderingHints(g2d)

            val accentColor = customization.accentToken.toAwtColor()
            val cardShape = RoundRectangle2D.Float(
                4f,
                4f,
                (CARD_WIDTH - 8).toFloat(),
                (CARD_HEIGHT - 8).toFloat(),
                OUTER_CORNER_RADIUS,
                OUTER_CORNER_RADIUS
            )

            val decodedAvatar = avatarBytes?.let { bytes ->
                runCatching {
                    ByteArrayInputStream(bytes).use { ImageIO.read(it) }
                }.getOrNull()
            }

            renderCardBackground(g2d, cardShape, customization, decodedAvatar, accentColor)
            renderOuterBorder(g2d, cardShape)

            // 1. Header (y = 120, базовые линии текста y = 132)
            renderHeader(
                g2d = g2d,
                customization = customization,
                accentColor = accentColor,
                versionText = versionText,
                hasLorebook = hasLorebook
            )

            val contentWidth = CARD_WIDTH - (PADDING_X * 2) // 1064 px

            // 2. Окно аватара (Y: 224 .. 1024, высота 800 px, ширина 1064 px, соотношение 1.33:1)
            val frameTopY = 224
            val frameHeight = 800

            renderVisualCenter(
                g2d = g2d,
                customization = customization,
                accentColor = accentColor,
                avatar = decodedAvatar,
                characterName = characterName,
                frameX = PADDING_X,
                frameY = frameTopY,
                frameWidth = contentWidth,
                frameHeight = frameHeight
            )

            // 3. Инфоблок (Имя Y=1070, Автор Y=1190, Описание Y=1285)
            val nameFont = Font(Font.SANS_SERIF, Font.BOLD, 82)
            g2d.font = nameFont
            val nameFm = g2d.fontMetrics
            val nameText = characterName.ifBlank { "Без имени" }
            val nameLines = wrapText(nameText, nameFm, contentWidth, maxLines = 2)

            val subFont = Font(Font.MONOSPACED, Font.BOLD, 50)
            g2d.font = subFont
            val subFm = g2d.fontMetrics
            val subtitle = buildString {
                if (!nickname.isNullOrBlank()) append("// $nickname ")
                if (!creatorNick.isNullOrBlank()) append("@$creatorNick")
            }
            val hasSubtitle = subtitle.isNotBlank()

            val descFont = Font(Font.SANS_SERIF, Font.PLAIN, 56)
            g2d.font = descFont
            val descFm = g2d.fontMetrics
            val hasDesc = shortDescription.isNotBlank()
            // Перенос описания по ширине contentWidth (1020 px для строгого 2-строчного переноса)
            val descWrapWidth = 1020
            val descLines = if (hasDesc) wrapText(shortDescription, descFm, descWrapWidth, maxLines = 2) else emptyList()

            renderInfoBlock(
                g2d = g2d,
                startY = 1070,
                contentWidth = contentWidth,
                nameFont = nameFont,
                nameFm = nameFm,
                nameLines = nameLines,
                subFont = subFont,
                subFm = subFm,
                subtitle = subtitle,
                hasSubtitle = hasSubtitle,
                descFont = descFont,
                descFm = descFm,
                descLines = descLines,
                hasDesc = hasDesc,
                tags = tags.take(3),
                accentColor = accentColor
            )

            // 4. Разделитель (Y = 1610)
            val dividerY = 1610
            g2d.stroke = BasicStroke(1.5f)
            g2d.color = Color(255, 255, 255, 15)
            g2d.drawLine(PADDING_X, dividerY, CARD_WIDTH - PADDING_X, dividerY)

            // 5. Футер (Y = 1706)
            renderFooter(
                g2d = g2d,
                y = 1706,
                contentWidth = contentWidth,
                accentColor = accentColor,
                specVersion = specVersion,
                tokenCount = tokenCount,
                altGreetingsCount = altGreetingsCount,
                showTelemetry = customization.showTelemetry
            )
        } finally {
            g2d.dispose()
        }

        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "png", outputStream)
        outputStream.toByteArray()
    }

    private fun setupRenderingHints(g2d: Graphics2D) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
    }

    private fun renderCardBackground(
        g2d: Graphics2D,
        cardShape: RoundRectangle2D.Float,
        customization: AvalonCardCustomization,
        avatar: BufferedImage?,
        accentColor: Color
    ) {
        val origClip = g2d.clip
        g2d.clip = cardShape

        // Базовый глубокий темный монолит
        g2d.color = Color(0x09, 0x0A, 0x0E)
        g2d.fillRect(0, 0, CARD_WIDTH, CARD_HEIGHT)

        when (customization.layout) {
            CardLayout.FULL_BLEED -> {
                if (avatar != null) {
                    drawCenterCropImage(g2d, avatar, 0, 0, CARD_WIDTH, CARD_HEIGHT)
                    drawFullBleedScrim(g2d)
                } else {
                    renderPattern(g2d, customization.bgPattern, accentColor)
                }
            }
            CardLayout.MONOLITH_FRAME, CardLayout.HEX_CORE -> {
                renderPattern(g2d, customization.bgPattern, accentColor)
            }
        }

        g2d.clip = origClip
    }

    private fun drawFullBleedScrim(g2d: Graphics2D) {
        val topScrim = GradientPaint(
            0f, 0f, Color(0x09, 0x0A, 0x0E, 120),
            0f, 260f, Color(0x09, 0x0A, 0x0E, 0)
        )
        g2d.paint = topScrim
        g2d.fillRect(0, 0, CARD_WIDTH, 260)

        val bottomScrim = GradientPaint(
            0f, 950f, Color(0x09, 0x0A, 0x0E, 0),
            0f, 1380f, Color(0x09, 0x0A, 0x0E, 255)
        )
        g2d.paint = bottomScrim
        g2d.fillRect(0, 950, CARD_WIDTH, 430)

        g2d.color = Color(0x09, 0x0A, 0x0E)
        g2d.fillRect(0, 1380, CARD_WIDTH, CARD_HEIGHT - 1380)
    }

    private fun renderPattern(g2d: Graphics2D, pattern: CardBgPattern, accentColor: Color) {
        when (pattern) {
            CardBgPattern.MATTE_OBSIDIAN -> {
                // Чистый глубокий темный цвет без раздражающих подсветок
                g2d.color = Color(0x09, 0x0A, 0x0E)
                g2d.fillRect(0, 0, CARD_WIDTH, CARD_HEIGHT)
            }
            CardBgPattern.HEX_GRID -> {
                g2d.color = Color(0x09, 0x0A, 0x0E)
                g2d.fillRect(0, 0, CARD_WIDTH, CARD_HEIGHT)

                g2d.color = accentColor.withAlpha(0.04f)
                g2d.stroke = BasicStroke(1.2f)
                val hexR = 40f
                val stepX = hexR * 1.5f
                val stepY = hexR * 1.732f
                var x = 0f
                while (x < CARD_WIDTH + hexR) {
                    var y = 0f
                    val isOdd = ((x / stepX).toInt() % 2) != 0
                    if (isOdd) y += stepY / 2f
                    while (y < CARD_HEIGHT + hexR) {
                        val path = createHexagonPath(x, y, hexR)
                        g2d.draw(path)
                        y += stepY
                    }
                    x += stepX
                }
            }
            CardBgPattern.SCANLINES -> {
                g2d.color = Color(0x09, 0x0A, 0x0E)
                g2d.fillRect(0, 0, CARD_WIDTH, CARD_HEIGHT)

                g2d.color = Color(0x18, 0x1B, 0x28, 45)
                g2d.stroke = BasicStroke(1f)
                var y = 0
                while (y < CARD_HEIGHT) {
                    g2d.drawLine(0, y, CARD_WIDTH, y)
                    y += 6
                }
            }
        }
    }

    private fun renderOuterBorder(g2d: Graphics2D, cardShape: RoundRectangle2D.Float) {
        // Тонкая спокойная внешняя рамка карточки (без неонового свечения)
        g2d.color = Color(255, 255, 255, 20)
        g2d.stroke = BasicStroke(2f)
        g2d.draw(cardShape)
    }

    private fun renderHeader(
        g2d: Graphics2D,
        customization: AvalonCardCustomization,
        accentColor: Color,
        versionText: String,
        hasLorebook: Boolean
    ) {
        val hexCenterX = PADDING_X + 20f
        val hexCenterY = 125f
        val hexRadius = 21f

        val hexPath = createHexagonPath(hexCenterX, hexCenterY, hexRadius)
        g2d.color = accentColor
        g2d.stroke = BasicStroke(4f)
        g2d.draw(hexPath)

        // Текст бренда
        g2d.font = Font(Font.MONOSPACED, Font.BOLD, 36)
        g2d.color = Color(255, 255, 255, 217)
        val brand = customization.brandingText.uppercase()
        val spacedBrand = brand.toCharArray().joinToString(" ")
        g2d.drawString(spacedBrand, PADDING_X + 56, 136)

        // Справа: Версия и индикатор LB без рамочек
        var rightEdge = (CARD_WIDTH - PADDING_X).toFloat()

        if (hasLorebook) {
            g2d.font = Font(Font.MONOSPACED, Font.BOLD, 32)
            g2d.color = accentColor
            val lbFm = g2d.fontMetrics
            val lbWidth = lbFm.stringWidth("LB")
            val lbX = rightEdge - lbWidth
            g2d.drawString("LB", lbX.toInt(), 136)
            rightEdge = lbX - 24f
        }

        val vText = if (versionText.isNotBlank()) versionText else "v1.0"
        g2d.font = Font(Font.MONOSPACED, Font.PLAIN, 32)
        g2d.color = Color(255, 255, 255, 105)
        val fm = g2d.fontMetrics
        val vTextWidth = fm.stringWidth(vText)
        val vX = rightEdge - vTextWidth
        g2d.drawString(vText, vX.toInt(), 136)
    }

    private fun renderVisualCenter(
        g2d: Graphics2D,
        customization: AvalonCardCustomization,
        accentColor: Color,
        avatar: BufferedImage?,
        characterName: String,
        frameX: Int,
        frameY: Int,
        frameWidth: Int,
        frameHeight: Int
    ) {
        when (customization.layout) {
            CardLayout.FULL_BLEED -> {
                // Арт отрисован в фоне
            }
            CardLayout.MONOLITH_FRAME -> {
                val frameRect = RoundRectangle2D.Float(
                    frameX.toFloat(),
                    frameY.toFloat(),
                    frameWidth.toFloat(),
                    frameHeight.toFloat(),
                    INNER_FRAME_CORNER_RADIUS,
                    INNER_FRAME_CORNER_RADIUS
                )

                // Подложка фрейма
                g2d.color = Color(0x0C, 0x0D, 0x12)
                g2d.fill(frameRect)

                if (avatar != null) {
                    val origClip = g2d.clip
                    g2d.clip = frameRect
                    drawCenterCropImage(g2d, avatar, frameX, frameY, frameWidth, frameHeight)
                    g2d.clip = origClip
                } else {
                    renderFallbackGlyph(g2d, customization.fallbackGlyph, characterName, accentColor, frameX, frameY, frameWidth, frameHeight)
                }

                // Тонкая спокойная рамка окна
                g2d.color = Color(255, 255, 255, 20)
                g2d.stroke = BasicStroke(1.5f)
                g2d.draw(frameRect)
            }
            CardLayout.HEX_CORE -> {
                val centerX = CARD_WIDTH / 2f
                val centerY = frameY + (frameHeight / 2f)
                val outerRadius = min(frameWidth, frameHeight) * 0.44f
                val innerRadius = outerRadius * 0.90f

                val outerHex = createHexagonPath(centerX, centerY, outerRadius)
                g2d.color = accentColor.withAlpha(0.12f)
                g2d.fill(outerHex)

                val innerHex = createHexagonPath(centerX, centerY, innerRadius)
                g2d.color = Color(0x11, 0x13, 0x19)
                g2d.fill(innerHex)

                if (avatar != null) {
                    val origClip = g2d.clip
                    g2d.clip = innerHex
                    val cropW = (innerRadius * 2).toInt()
                    val cropH = (innerRadius * 2).toInt()
                    val cropX = (centerX - innerRadius).toInt()
                    val cropY = (centerY - innerRadius).toInt()
                    drawCenterCropImage(g2d, avatar, cropX, cropY, cropW, cropH)
                    g2d.clip = origClip
                } else {
                    renderFallbackGlyph(
                        g2d,
                        customization.fallbackGlyph,
                        characterName,
                        accentColor,
                        (centerX - innerRadius).toInt(),
                        (centerY - innerRadius).toInt(),
                        (innerRadius * 2).toInt(),
                        (innerRadius * 2).toInt()
                    )
                }

                g2d.color = accentColor.withAlpha(0.80f)
                g2d.stroke = BasicStroke(3.5f)
                g2d.draw(innerHex)
            }
        }
    }

    private fun renderFallbackGlyph(
        g2d: Graphics2D,
        glyph: CardFallbackGlyph,
        characterName: String,
        accentColor: Color,
        x: Int,
        y: Int,
        width: Int,
        height: Int
    ) {
        val cx = x + width / 2
        val cy = y + height / 2

        when (glyph) {
            CardFallbackGlyph.INITIALS -> {
                val initials = characterName.trim().take(2).uppercase().ifBlank { "AA" }
                g2d.font = Font(Font.MONOSPACED, Font.BOLD, 120)
                g2d.color = accentColor
                val fm = g2d.fontMetrics
                val textW = fm.stringWidth(initials)
                val textH = fm.ascent - fm.descent
                g2d.drawString(initials, cx - textW / 2, cy + textH / 2)
            }
            CardFallbackGlyph.AI_CORE,
            CardFallbackGlyph.TERMINAL,
            CardFallbackGlyph.BOOK,
            CardFallbackGlyph.SWORD -> {
                val coreRadius = min(width, height) * 0.22f
                val hex1 = createHexagonPath(cx.toFloat(), cy.toFloat(), coreRadius)
                val hex2 = createHexagonPath(cx.toFloat(), cy.toFloat(), coreRadius * 0.65f)

                g2d.color = accentColor.withAlpha(0.18f)
                g2d.fill(hex1)

                g2d.color = accentColor.withAlpha(0.80f)
                g2d.stroke = BasicStroke(3.5f)
                g2d.draw(hex1)

                g2d.color = accentColor.withAlpha(0.50f)
                g2d.stroke = BasicStroke(2f)
                g2d.draw(hex2)

                val innerCircle = RoundRectangle2D.Float(cx - 14f, cy - 14f, 28f, 28f, 28f, 28f)
                g2d.color = accentColor
                g2d.fill(innerCircle)
            }
        }
    }

    private fun renderInfoBlock(
        g2d: Graphics2D,
        startY: Int,
        contentWidth: Int,
        nameFont: Font,
        nameFm: FontMetrics,
        nameLines: List<String>,
        subFont: Font,
        subFm: FontMetrics,
        subtitle: String,
        hasSubtitle: Boolean,
        descFont: Font,
        descFm: FontMetrics,
        descLines: List<String>,
        hasDesc: Boolean,
        tags: List<String>,
        accentColor: Color
    ) {
        var curY = startY

        // 1. Имя персонажа
        g2d.font = nameFont
        g2d.color = Color.WHITE
        for (line in nameLines) {
            g2d.drawString(line, PADDING_X, curY + nameFm.ascent)
            curY += nameFm.height + 6
        }

        // 2. Подзаголовок (@creator // nickname)
        if (hasSubtitle) {
            curY += 10
            g2d.font = subFont
            g2d.color = accentColor.withAlpha(0.85f)
            val truncatedSub = truncateWithEllipsis(subtitle, subFm, contentWidth)
            g2d.drawString(truncatedSub, PADDING_X, curY + subFm.ascent)
            curY += subFm.height + 18
        }

        // 3. Краткое описание (в 2 строки крупным шрифтом 56px)
        if (hasDesc) {
            curY += 22
            g2d.font = descFont
            g2d.color = Color(255, 255, 255, 180)
            for (line in descLines) {
                g2d.drawString(line, PADDING_X, curY + descFm.ascent)
                curY += descFm.height + 18
            }
            curY += 12
        }

        // 4. Теги (без рамки, мягкие плоские плашки)
        if (tags.isNotEmpty()) {
            curY += 10
            val tagBadgeHeight = 48
            var tagX = PADDING_X.toFloat()

            g2d.font = Font(Font.SANS_SERIF, Font.BOLD, 30)
            val tagFm = g2d.fontMetrics

            for (tag in tags) {
                val tagWidth = tagFm.stringWidth(tag) + 36
                if (tagX + tagWidth > PADDING_X + contentWidth) break

                val tagRect = RoundRectangle2D.Float(tagX, curY.toFloat(), tagWidth.toFloat(), tagBadgeHeight.toFloat(), 14f, 14f)
                g2d.color = Color(255, 255, 255, 13) // ~0.05f alpha
                g2d.fill(tagRect)

                g2d.color = Color(255, 255, 255, 180)
                drawCenteredString(g2d, tag, tagX.toInt(), curY, tagWidth, tagBadgeHeight)

                tagX += tagWidth + 16f
            }
        }
    }

    private fun renderFooter(
        g2d: Graphics2D,
        y: Int,
        contentWidth: Int,
        accentColor: Color,
        specVersion: String,
        tokenCount: Int,
        altGreetingsCount: Int,
        showTelemetry: Boolean
    ) {
        val footerFont = Font(Font.MONOSPACED, Font.BOLD, 48)
        g2d.font = footerFont
        val footerFm = g2d.fontMetrics
        val textBaseY = y - footerFm.descent

        // 1. Телеметрия слева (векторная молния и облачко чата)
        if (showTelemetry) {
            val tokText = if (tokenCount >= 1000) "${tokenCount / 1000}k" else "$tokenCount"
            var curX = PADDING_X.toFloat() + 12f

            // Векторная желтая молния
            val boltWidth = 28f
            val boltHeight = 34f
            drawLightningBolt(g2d, curX, textBaseY - boltHeight + 4f, boltWidth, boltHeight, Color(0xFF, 0xCC, 0x00))
            curX += boltWidth + 14f

            val teleFont = Font(Font.MONOSPACED, Font.PLAIN, 40)
            g2d.font = teleFont
            g2d.color = Color(255, 255, 255, 120)
            val tokStr = "${tokText}t"
            g2d.drawString(tokStr, curX.toInt(), textBaseY)
            curX += g2d.fontMetrics.stringWidth(tokStr) + 18f

            // Точка
            g2d.color = Color(255, 255, 255, 65)
            g2d.drawString("•", curX.toInt(), textBaseY)
            curX += g2d.fontMetrics.stringWidth("•") + 18f

            // Векторное облачко сообщения
            val bubbleW = 38f
            val bubbleH = 32f
            drawChatBubble(g2d, curX, textBaseY - bubbleH + 4f, bubbleW, bubbleH, Color(255, 255, 255, 140))
            curX += bubbleW + 14f

            g2d.color = Color(255, 255, 255, 120)
            val altStr = "${altGreetingsCount}a"
            g2d.drawString(altStr, curX.toInt(), textBaseY)
        }

        // 2. Бейдж протокола справа (чистый текст без рамки)
        val isV3 = specVersion.contains("3", ignoreCase = true)
        val specText = if (isV3) "SPEC V3" else "SPEC V2"

        g2d.font = footerFont
        val specWidth = footerFm.stringWidth(specText)
        val specX = CARD_WIDTH - PADDING_X - specWidth

        g2d.color = if (isV3) accentColor else Color(255, 255, 255, 130)
        g2d.drawString(specText, specX, textBaseY)
    }

    private fun drawLightningBolt(g2d: Graphics2D, x: Float, y: Float, width: Float, height: Float, color: Color) {
        val origColor = g2d.color
        g2d.color = color
        val path = Path2D.Float()
        path.moveTo(x + width * 0.55f, y)
        path.lineTo(x + width * 0.12f, y + height * 0.56f)
        path.lineTo(x + width * 0.44f, y + height * 0.56f)
        path.lineTo(x + width * 0.35f, y + height)
        path.lineTo(x + width * 0.88f, y + height * 0.40f)
        path.lineTo(x + width * 0.54f, y + height * 0.40f)
        path.closePath()
        g2d.fill(path)
        g2d.color = origColor
    }

    private fun drawChatBubble(g2d: Graphics2D, x: Float, y: Float, width: Float, height: Float, color: Color) {
        val origColor = g2d.color
        g2d.color = color
        val body = RoundRectangle2D.Float(x, y, width, height * 0.80f, height * 0.35f, height * 0.35f)
        g2d.fill(body)
        val tail = Path2D.Float()
        tail.moveTo(x + width * 0.20f, y + height * 0.68f)
        tail.lineTo(x + width * 0.10f, y + height)
        tail.lineTo(x + width * 0.42f, y + height * 0.68f)
        tail.closePath()
        g2d.fill(tail)
        g2d.color = origColor
    }

    private fun drawCenterCropImage(
        g2d: Graphics2D,
        img: BufferedImage,
        targetX: Int,
        targetY: Int,
        targetWidth: Int,
        targetHeight: Int
    ) {
        val scale = max(targetWidth.toDouble() / img.width, targetHeight.toDouble() / img.height)
        val scaledW = (img.width * scale).toInt()
        val scaledH = (img.height * scale).toInt()
        val drawX = targetX + (targetWidth - scaledW) / 2
        val drawY = targetY + (targetHeight - scaledH) / 2

        g2d.drawImage(img, drawX, drawY, scaledW, scaledH, null)
    }

    private fun createHexagonPath(centerX: Float, centerY: Float, radius: Float): Path2D.Float {
        val path = Path2D.Float()
        var angle = 0.0 // лежит на ребре (flat-top)
        path.moveTo(
            (centerX + radius * cos(angle)).toFloat(),
            (centerY + radius * sin(angle)).toFloat()
        )
        for (i in 1..6) {
            angle += PI / 3.0
            path.lineTo(
                (centerX + radius * cos(angle)).toFloat(),
                (centerY + radius * sin(angle)).toFloat()
            )
        }
        path.closePath()
        return path
    }

    private fun drawCenteredString(g2d: Graphics2D, text: String, x: Int, y: Int, width: Int, height: Int) {
        val fm = g2d.fontMetrics
        val textX = x + (width - fm.stringWidth(text)) / 2
        val textY = y + (height - fm.height) / 2 + fm.ascent
        g2d.drawString(text, textX, textY)
    }

    private fun wrapText(text: String, fm: FontMetrics, maxWidth: Int, maxLines: Int): List<String> {
        val result = mutableListOf<String>()
        val paragraphs = text.split("\n")

        for (paragraph in paragraphs) {
            if (result.size >= maxLines) break
            val words = paragraph.split(Regex("\\s+")).filter { it.isNotBlank() }
            if (words.isEmpty()) continue

            var line = StringBuilder()
            for (word in words) {
                val candidate = if (line.isEmpty()) word else "$line $word"
                if (fm.stringWidth(candidate) <= maxWidth) {
                    line = StringBuilder(candidate)
                } else {
                    if (line.isNotEmpty()) {
                        result.add(line.toString())
                        if (result.size >= maxLines) break
                    }
                    line = StringBuilder(word)
                }
            }
            if (line.isNotEmpty() && result.size < maxLines) {
                result.add(line.toString())
            }
        }

        if (result.size == maxLines && text.length > result.joinToString(" ").length) {
            var last = result.last()
            while (fm.stringWidth("$last...") > maxWidth && last.isNotEmpty()) {
                last = last.dropLast(1).trimEnd()
            }
            result[result.size - 1] = "$last..."
        }

        return result
    }

    private fun truncateWithEllipsis(text: String, fm: FontMetrics, maxWidth: Int): String {
        if (fm.stringWidth(text) <= maxWidth) return text
        var truncated = text
        while (fm.stringWidth("$truncated...") > maxWidth && truncated.isNotEmpty()) {
            truncated = truncated.dropLast(1).trimEnd()
        }
        return "$truncated..."
    }

    private fun CardAccentToken.toAwtColor(): Color {
        return runCatching {
            val cleanHex = hexColor.removePrefix("#")
            val colorInt = cleanHex.toLong(16)
            Color(colorInt.toInt())
        }.getOrDefault(Color(0x00, 0xE5, 0xFF))
    }

    private fun Color.withAlpha(alpha: Float): Color {
        val a = (alpha.coerceIn(0f, 1f) * 255).toInt()
        return Color(red, green, blue, a)
    }
}
