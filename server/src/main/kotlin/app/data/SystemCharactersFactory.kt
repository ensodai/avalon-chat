package app.data

import app.data.db.tables.aiCharacter.AICharacterData
import com.ensodai.shared.avalon.character.models.CharacterVisibility
import kotlin.uuid.Uuid

object SystemCharactersFactory {
    private fun createMockCharacter(index: Int, category: String, mockOwner: Uuid): AICharacterData {
        return AICharacterData(
            publicId = Uuid.random(),
            owner = mockOwner,
//            geminiModel = "gemini-2.0-flash-thinking-exp-01-21",
            characterName = "Test Character $index",
            fullDescription = "Description for character $index",
            shortDescription = "Char $index",
            temperature = 0.7f + (index % 3) * 0.1f,
//            topK = 40,
//            topP = 0.9f,
//            maxOutputTokens = 1024,
//            maxContextTokens = 2048,
//            lastTimeUpdate = Clock.System.now().toInstant().toEpochMilli(),
            visibility = CharacterVisibility.PUBLIC,
            isPublicFullDescription = index % 2 == 0,
            category = category,
            firstMessage = null,
            welcomeCard = null,
            isOutreachEnabled = false
        )
    }

    fun getMockCharacters(count: Int, category: String, mockOwner: Uuid): List<AICharacterData> {
        return (1..count).map { createMockCharacter(it, category, mockOwner) }
    }
}