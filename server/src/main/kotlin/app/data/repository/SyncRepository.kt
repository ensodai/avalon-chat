package app.data.repository

import org.koin.core.annotation.Single

@Single
class SyncRepository {

//    /**
//     * [ВРЕМЕННЫЙ МЕТОД] Получает пагинированный список чатов пользователя со всеми связанными данными
//     * для синхронизации.
//     *
//     * Этот метод оптимизирован для минимизации запросов к БД. Он выполняет
//     * "жадную" загрузку всех необходимых сущностей (персонажи, их владельцы, аватары,
//     * изображения, варианты чатов и их изображения) за один проход.
//     *
//     * @param userId UUID пользователя, чьи чаты нужно синхронизировать.
//     * @param page Номер страницы.
//     * @param size Размер страницы.
//     * @return Список [CharacterChatSyncResponse], готовый для отправки клиенту.
//     */
//    suspend fun getChatsForSync(userId: UUID, page: Int, size: Int): List<CharacterChatSyncResponse> {
//        return dbQuery {
//            val offset = ((page - 1) * size).toLong()
//
//            // --- Шаг 1: Получаем одну страницу СЕССИЙ ЧАТА и "жадно" загружаем ВСЁ ---
//            // UPDATED: Заменили UserCharacterChat на ChatSession
//            val chatSessions = ChatSession
//                .find { ChatSessions.userId eq userId }
//                // UPDATED: Сортируем по новой колонке `lastUpdatedAt` для релевантности
//                .orderBy(ChatSessions.updatedAt to SortOrder.DESC)
//                .limit(size)
//                .offset(offset)
//                // UPDATED: Цепочки `with` стали чище и логичнее
//                .with(
//                    ChatSession::aiCharacter, // Сначала грузим персонажа
//                    ChatSession::aiCharacter, AICharacter::ownerEntity, // Потом владельца персонажа
//                    ChatSession::aiCharacter, AICharacter::avatar,
//                    ChatSession::aiCharacter, AICharacter::images,
//                    ChatSession::aiCharacter, AICharacter::welcomeCard,
//                    ChatSession::aiCharacter, AICharacter::welcomeCardImages,
//                    ChatSession::variants, // Потом все варианты для сессии
//                    ChatSession::variants, ChatVariant::chatVariantImage // И изображения для каждого варианта
//                )
//
//            // --- Шаг 2: Преобразуем (мапим) загруженные данные в DTO ---
//            chatSessions.map { session -> // UPDATED: `chat` переименован в `session` для ясности
//                val character = session.aiCharacter
//                val owner = character.ownerEntity
//
//                // --- Логика "создать вариант, если его нет" ---
//                // UPDATED: `chat.variants` -> `session.variants`
//                val variants = if (session.variants.empty()) {
//                    // Если у сессии нет вариантов, создаем один дефолтный
//                    listOf(
//                        ChatVariant.new {
//                            this.name = character.characterName
//                            // UPDATED: `userCharacterChat` -> `chatSession`
//                            this.chatSession = session
//                            this.createdAt = Clock.System.now()
//                        }
//                    )
//                } else {
//                    session.variants.toList()
//                }
//
//                // --- Собираем DTO ---
//                CharacterChatSyncResponse(
//                    // ... все поля Character остаются без изменений ...
//                    publicCharacterId = character.id.value.toString(),
//                    owner = owner.id.value.toString(),
//                    ownerNick = owner.nickName ?: "user_${owner.id.value.toString().take(20)}",
//                    name = character.characterName,
//                    shortDescription = character.shortDescription,
//                    fullDescription = character.fullDescription,
//                    avatar = character.avatar?.let {
//                        CharacterAvatarResponse(
//                            id = it.id.value,
//                            imageUrl = it.imagePath.toServerUrl()
//                        )
//                    },
//                    imageUrls = character.images.map {
//                        CharacterImageResponse(
//                            id = it.id.value,
//                            imageUrl = it.imagePath.toServerUrl()
//                        )
//                    }.takeIf { it.isNotEmpty() },
//                    isCharacterPublic = character.isPublicCharacter,
//                    isPublicFullDescription = character.isPublicFullDescription,
//                    temperature = character.temperature,
//                    harassment = character.harassment,
//                    hate = character.hate,
//                    sexuallyExplicit = character.sexuallyExplicit,
//                    dangerousContent = character.dangerousContent,
//                    topK = character.topK,
//                    topP = character.topP,
//                    maxOutputTokens = character.maxOutputTokens,
//                    maxContextTokens = character.maxContextTokens,
//                    geminiModel = character.geminiModel,
//                    isArchived = session.isArchived,
//                    sessionId = session.id.value.toString(),
//                    chatVariants = variants.map { variant ->
//                        ChatVariantResponse(
//                            id = variant.id.value,
//                            name = variant.name,
//                            image = variant.chatVariantImage?.let {
//                                ChatVariantImageResponse(
//                                    it.id.value,
//                                    it.imagePath?.toServerUrl() ?: ""
//                                )
//                            },
//
//                            охsessionId = session.id.value // <-- Теперь берется из session
//                        )
//                    },
//                    firstMessage = character.firstMessage,
//                    welcomeCard = character.welcomeCard?.let { card ->
//                        WelcomeCardResponse(
//                            welcomeCardDescription = card.welcomeCardDescription,
//                            startColor = card.cardColorStart,
//                            endColor = card.cardColorEnd,
//                            cardColorLight = card.cardColorLight,
//                            isEnableLight = card.isEnableLight,
//                            welcomeCardImages = character.welcomeCardImages.map {
//                                WelcomeCardImageResponse(
//                                    it.id.value,
//                                    it.imagePath.toServerUrl()
//                                )
//                            }.takeIf { it.isNotEmpty() }
//                        )
//                    }
//                )
//            }
//        }
//    }
}