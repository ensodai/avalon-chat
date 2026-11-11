package com.ensodai.avalon.data.db.models

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.ensodai.avalon.theme.models.AvalonThemePreset
import com.ensodai.avalon.theme.models.BubbleFillType
import com.ensodai.avalon.theme.models.BubbleShapeType
import com.ensodai.avalon.theme.models.ChatBackgroundType
import com.ensodai.avalon.theme.models.ChatPatternPreset
import com.ensodai.avalon.theme.models.ChatRoleplayPreset

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 1,
    @ColumnInfo(name = "auto_replace_api_keys")
    val autoReplaceAPIKeys: Boolean = true,
    @ColumnInfo(name = "local_mode")
    val localMode: Boolean = false,
    @ColumnInfo(name = "language")
    val language: String = "en",
    @ColumnInfo(name = "is_safety_reminder_accepted")
    val isSafetyReminderAccepted: Boolean = false,
    @ColumnInfo(name = "flow_mode")
    val flowMode: Boolean = false,
    @ColumnInfo(name = "theme_id")
    val themePreset: AvalonThemePreset = AvalonThemePreset.TechNoir,
    @ColumnInfo(name = "user_bubble_fill_type")
    val userBubbleFillType: BubbleFillType = BubbleFillType.Solid,
    @ColumnInfo(name = "custom_user_bubble_color")
    val userBubbleSolidColor: String? = null,
    @ColumnInfo(name = "user_bubble_gradient_start_color")
    val userBubbleGradientStartColor: String? = null,
    @ColumnInfo(name = "user_bubble_gradient_end_color")
    val userBubbleGradientEndColor: String? = null,
    @ColumnInfo(name = "bot_bubble_fill_type")
    val botBubbleFillType: BubbleFillType = BubbleFillType.Solid,
    @ColumnInfo(name = "custom_bot_bubble_color")
    val botBubbleSolidColor: String? = null,
    @ColumnInfo(name = "bot_bubble_gradient_start_color")
    val botBubbleGradientStartColor: String? = null,
    @ColumnInfo(name = "bot_bubble_gradient_end_color")
    val botBubbleGradientEndColor: String? = null,
    @ColumnInfo(name = "chat_background_type")
    val chatBackgroundType: ChatBackgroundType = ChatBackgroundType.Default,
    @ColumnInfo(name = "chat_background_color")
    val chatBackgroundColor: String? = null,
    @ColumnInfo(name = "chat_background_url")
    val chatBackgroundUrl: String? = null,
    @ColumnInfo(name = "chat_background_pattern_id")
    val chatBackgroundPatternPreset: ChatPatternPreset? = null,
    @ColumnInfo(name = "chat_background_blur")
    val chatBackgroundBlur: Int = 0,
    @ColumnInfo(name = "chat_background_dim_amount")
    val chatBackgroundDimAmount: Float = 0f,
    @ColumnInfo(name = "bubble_font_size")
    val bubbleFontSize: Int = 16,
    @ColumnInfo(name = "bubble_shape_type")
    val bubbleShapeType: BubbleShapeType = BubbleShapeType.Tail,
    @ColumnInfo(name = "bubble_corner_radius")
    val bubbleCornerRadius: Int = 16,
    @ColumnInfo(name = "show_ai_warning")
    val showAiWarning: Boolean = true,
    @ColumnInfo(name = "chat_roleplay_preset")
    val chatRoleplayPreset: ChatRoleplayPreset = ChatRoleplayPreset.STANDARD_BUBBLE
)
