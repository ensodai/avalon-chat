package com.ensodai.avalon.data.db

//import com.google.gson.Gson
//import com.google.gson.reflect.TypeToken
import androidx.room3.ColumnTypeConverter
import com.ensodai.avalon.data.db.models.ChatMessageAction
import com.ensodai.avalon.data.db.models.SyncAction
import com.ensodai.shared.avalon.chat.models.AvalonMessageContentType
import com.ensodai.shared.avalon.chat.models.MessageRole
import com.ensodai.shared.avalon.gifts.models.*
import com.ensodai.shared.avalon.chat.models.Rarity
import com.ensodai.shared.avalon.chat.models.MessageStatus
import com.ensodai.shared.avalon.liveCall.models.VoiceModel
import com.ensodai.shared.avalon.character.models.InteractionFormat
import com.ensodai.avalon.theme.models.AvalonThemePreset
import com.ensodai.avalon.theme.models.BubbleFillType
import com.ensodai.avalon.theme.models.BubbleShapeType
import com.ensodai.avalon.theme.models.ChatBackgroundType
import com.ensodai.avalon.theme.models.ChatPatternPreset
import com.ensodai.avalon.theme.models.ChatRoleplayPreset
import kotlinx.serialization.json.Json
import kotlin.jvm.JvmStatic
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
object ColumnTypeConverter {
    private val json = Json { ignoreUnknownKeys = true }

    @ColumnTypeConverter
    @JvmStatic
    fun fromBubbleShapeType(value: BubbleShapeType?): String? {
        return value?.storageKey
    }

    @ColumnTypeConverter
    @JvmStatic
    fun toBubbleShapeType(value: String?): BubbleShapeType? {
        return value?.let(BubbleShapeType::fromStorage)
    }

    @ColumnTypeConverter
    @JvmStatic
    fun fromAvalonThemePreset(value: AvalonThemePreset?): String? {
        return value?.storageKey
    }

    @ColumnTypeConverter
    @JvmStatic
    fun toAvalonThemePreset(value: String?): AvalonThemePreset? {
        return value?.let(AvalonThemePreset::fromStorage)
    }

    @ColumnTypeConverter
    @JvmStatic
    fun fromBubbleFillType(value: BubbleFillType?): String? {
        return value?.storageKey
    }

    @ColumnTypeConverter
    @JvmStatic
    fun toBubbleFillType(value: String?): BubbleFillType? {
        return value?.let(BubbleFillType::fromStorage)
    }

    @ColumnTypeConverter
    @JvmStatic
    fun fromChatBackgroundType(value: ChatBackgroundType?): String? {
        return value?.storageKey
    }

    @ColumnTypeConverter
    @JvmStatic
    fun toChatBackgroundType(value: String?): ChatBackgroundType? {
        return value?.let(ChatBackgroundType::fromStorage)
    }

    @ColumnTypeConverter
    @JvmStatic
    fun fromChatPatternPreset(value: ChatPatternPreset?): String? {
        return value?.storageKey
    }

    @ColumnTypeConverter
    @JvmStatic
    fun toChatPatternPreset(value: String?): ChatPatternPreset? {
        return value?.let(ChatPatternPreset::fromStorage)
    }

    @ColumnTypeConverter
    @JvmStatic
    fun fromGiftActionType(type: GiftActionType?): String? {
        return type?.name
    }

    @ColumnTypeConverter
    @JvmStatic
    fun toGiftActionType(value: String?): GiftActionType? {
        return value?.let { try { GiftActionType.valueOf(it) } catch(e: Exception) { GiftActionType.NONE } }
    }

    @ColumnTypeConverter
    @JvmStatic
    fun fromMessageRole(role: MessageRole?): String? {
        return role?.name  // Enum -> String
    }

    @ColumnTypeConverter
    @JvmStatic
    fun toMessageRole(roleString: String?): MessageRole? {
        return roleString?.let { try { MessageRole.valueOf(it) } catch(e: Exception) { MessageRole.USER } }  // String -> Enum
    }

    @ColumnTypeConverter
    @JvmStatic
    fun fromChatMessageAction(action: ChatMessageAction): String {
        return action.name
    }

    @ColumnTypeConverter
    @JvmStatic
    fun toChatMessageAction(value: String): ChatMessageAction {
        return try { ChatMessageAction.valueOf(value) } catch (e: Exception) { ChatMessageAction.UPGRADE_REQUIRED }
    }

    @ColumnTypeConverter
    @JvmStatic
    fun toRarity(value: String?) = value?.let { try { Rarity.valueOf(it) } catch(e: Exception) { Rarity.COMMON } }

    @ColumnTypeConverter
    @JvmStatic
    fun fromRarity(value: Rarity?) = value?.name

    @ColumnTypeConverter
    @JvmStatic
    fun fromMessageStatus(status: MessageStatus?): String? {
        return status?.let { json.encodeToString(it) }
    }


    @ColumnTypeConverter
    @JvmStatic
    fun toMessageStatus(value: String?): MessageStatus? {
        return value?.let {
            try {
                json.decodeFromString<MessageStatus>(it)
            } catch (e: Exception) {
                MessageStatus.FAILED("parsing_error")
            }
        }
    }

    @ColumnTypeConverter
    @JvmStatic
    fun fromUUID(uuid: Uuid?): String? = uuid?.toString()

    @ColumnTypeConverter
    @JvmStatic
    fun toUUID(uuid: String?): Uuid? = uuid?.let { try { Uuid.parse(it) } catch(e: Exception) { null } }

    @ColumnTypeConverter
    @JvmStatic
    fun fromInstant(date: Instant?): String? {
        return date?.toString()
    }

    @ColumnTypeConverter
    @JvmStatic
    fun toInstant(dateString: String?): Instant? {
        return dateString?.let { try { Instant.parse(it) } catch(e: Exception) { kotlin.time.Clock.System.now() } }
    }

    @ColumnTypeConverter
    @JvmStatic
    fun fromSyncState(syncAction: SyncAction?): String? {
        return syncAction?.name  // Enum -> String (UNSYNCED, SYNCED, FAILED)
    }

    @ColumnTypeConverter
    @JvmStatic
    fun toSyncState(syncStateString: String?): SyncAction? {
        return syncStateString?.let { try { SyncAction.valueOf(it) } catch(e: Exception) { SyncAction.NONE } }  // String -> Enum
    }

    @ColumnTypeConverter
    @JvmStatic
    fun fromContentType(type: AvalonMessageContentType?): String? {
        return type?.name
    }

    @ColumnTypeConverter
    @JvmStatic
    fun toContentType(typeString: String?): AvalonMessageContentType? {
        return typeString?.let { try { AvalonMessageContentType.valueOf(it) } catch(e: Exception) { AvalonMessageContentType.TEXT } }
    }

    @ColumnTypeConverter
    @JvmStatic
    fun fromVoiceModel(voice: VoiceModel?): String? {
        return voice?.name
    }

    @ColumnTypeConverter
    @JvmStatic
    fun toVoiceModel(value: String?): VoiceModel? {
        return value?.let { try { VoiceModel.valueOf(it) } catch(e: Exception) { null } }
    }

    @ColumnTypeConverter
    @JvmStatic
    fun fromInteractionFormat(format: InteractionFormat?): String? {
        return format?.name
    }

    @ColumnTypeConverter
    @JvmStatic
    fun toInteractionFormat(value: String?): InteractionFormat? {
        return value?.let { try { InteractionFormat.valueOf(it) } catch(e: Exception) { InteractionFormat.LITERARY_RPG } }
    }

    @ColumnTypeConverter
    @JvmStatic
    fun fromChatRoleplayPreset(preset: ChatRoleplayPreset?): String? {
        return preset?.storageKey
    }

    @ColumnTypeConverter
    @JvmStatic
    fun toChatRoleplayPreset(value: String?): ChatRoleplayPreset? {
        return value?.let { ChatRoleplayPreset.fromStorage(it) }
    }
}
