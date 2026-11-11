package app.data

import app.data.db.tables.CategoryDto

object CategoryFactory {

    private val categories = listOf(
        CategoryDto(
            categoryCode = "all",
            localizedNames = mapOf(
                "en" to "All",
                "ru" to "Все"
            ),
            sortOrder = 1,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "helper",
            localizedNames = mapOf(
                "en" to "Helper",
                "ru" to "Помощник"
            ),
            sortOrder = 2,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "character",
            localizedNames = mapOf(
                "en" to "Character",
                "ru" to "Персонаж"
            ),
            sortOrder = 3,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "anime_characters",
            localizedNames = mapOf(
                "en" to "Anime Characters",
                "ru" to "Персонажи аниме"
            ),
            sortOrder = 4,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "anime",
            localizedNames = mapOf(
                "en" to "Anime",
                "ru" to "Аниме"
            ),
            sortOrder = 5,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "game",
            localizedNames = mapOf(
                "en" to "Game",
                "ru" to "Игра"
            ),
            sortOrder = 6,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "game_characters",
            localizedNames = mapOf(
                "en" to "Game Characters",
                "ru" to "Персонажи игр"
            ),
            sortOrder = 7,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "books",
            localizedNames = mapOf(
                "en" to "Books",
                "ru" to "Книги"
            ),
            sortOrder = 8,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "comedies",
            localizedNames = mapOf(
                "en" to "Comedies",
                "ru" to "Комедии"
            ),
            sortOrder = 9,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "language_learning",
            localizedNames = mapOf(
                "en" to "Language Learning",
                "ru" to "Изучение языков"
            ),
            sortOrder = 10,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "discussions",
            localizedNames = mapOf(
                "en" to "Discussions",
                "ru" to "Обсуждения"
            ),
            sortOrder = 11,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "religion",
            localizedNames = mapOf(
                "en" to "Religion",
                "ru" to "Религия"
            ),
            sortOrder = 12,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "history",
            localizedNames = mapOf(
                "en" to "History",
                "ru" to "История"
            ),
            sortOrder = 13,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "animals",
            localizedNames = mapOf(
                "en" to "Animals",
                "ru" to "Животные"
            ),
            sortOrder = 14,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "philosophy",
            localizedNames = mapOf(
                "en" to "Philosophy",
                "ru" to "Философия"
            ),
            sortOrder = 15,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "politics",
            localizedNames = mapOf(
                "en" to "Politics",
                "ru" to "Политика"
            ),
            sortOrder = 16,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "scifi",
            localizedNames = mapOf(
                "en" to "Science Fiction",
                "ru" to "Научная_фантастика"
            ),
            sortOrder = 17,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "historical",
            localizedNames = mapOf(
                "en" to "Historical",
                "ru" to "Исторические"
            ),
            sortOrder = 18,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "monsters",
            localizedNames = mapOf(
                "en" to "Monsters",
                "ru" to "Монстры"
            ),
            sortOrder = 19,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "romance",
            localizedNames = mapOf(
                "en" to "Romance",
                "ru" to "Романтика"
            ),
            sortOrder = 20,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "adventure",
            localizedNames = mapOf(
                "en" to "Adventure",
                "ru" to "Приключения"
            ),
            sortOrder = 21,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "comedy",
            localizedNames = mapOf(
                "en" to "Comedy",
                "ru" to "Комедия"
            ),
            sortOrder = 22,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "horror",
            localizedNames = mapOf(
                "en" to "Horror",
                "ru" to "Ужасы"
            ),
            sortOrder = 23,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "superheroes",
            localizedNames = mapOf(
                "en" to "Superheroes",
                "ru" to "Супергерои"
            ),
            sortOrder = 24,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "cyberpunk",
            localizedNames = mapOf(
                "en" to "Cyberpunk",
                "ru" to "Киберпанк"
            ),
            sortOrder = 25,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "online",
            localizedNames = mapOf(
                "en" to "Online",
                "ru" to "Онлайн"
            ),
            sortOrder = 26,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "friends",
            localizedNames = mapOf(
                "en" to "Friends",
                "ru" to "Друзья"
            ),
            sortOrder = 27,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "villains",
            localizedNames = mapOf(
                "en" to "Villains",
                "ru" to "Злодеи"
            ),
            sortOrder = 28,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "slice_of_life",
            localizedNames = mapOf(
                "en" to "Slice of Life",
                "ru" to "Повседневность"
            ),
            sortOrder = 29,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "drama",
            localizedNames = mapOf(
                "en" to "Drama",
                "ru" to "Драма"
            ),
            sortOrder = 30,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "fluff",
            localizedNames = mapOf(
                "en" to "Fluff",
                "ru" to "Флаттер"
            ),
            sortOrder = 31,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "love_hate",
            localizedNames = mapOf(
                "en" to "Love/Hate",
                "ru" to "Любовь/Ненависть"
            ),
            sortOrder = 32,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "enemies",
            localizedNames = mapOf(
                "en" to "Enemies",
                "ru" to "Враги"
            ),
            sortOrder = 33,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "fantasy",
            localizedNames = mapOf(
                "en" to "Fantasy",
                "ru" to "Фентези"
            ),
            sortOrder = 34,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "post_apocalyptic",
            localizedNames = mapOf(
                "en" to "Post-Apocalyptic",
                "ru" to "Постапокалипсис"
            ),
            sortOrder = 35,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "isekai",
            localizedNames = mapOf(
                "en" to "Isekai",
                "ru" to "Попаданцы/Секай"
            ),
            sortOrder = 36,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "humor",
            localizedNames = mapOf(
                "en" to "Humor",
                "ru" to "Юмор"
            ),
            sortOrder = 37,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "sensitive_content",
            localizedNames = mapOf(
                "en" to "Sensitive Content",
                "ru" to "Чувствительный контент"
            ),
            sortOrder = 38,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "real_people",
            localizedNames = mapOf(
                "en" to "Real People",
                "ru" to "Реальные люди"
            ),
            sortOrder = 39,
            isSpecial = false
        ),
        CategoryDto(
            categoryCode = "fandom",
            localizedNames = mapOf(
                "en" to "Fandom",
                "ru" to "фандом"
            ),
            sortOrder = 40,
            isSpecial = false
        )
    )

    private val serverCategory = listOf(
        CategoryDto(
            categoryCode = "all",
            localizedNames = mapOf(
                "en" to "All",
                "ru" to "Все"
            ),
            sortOrder = 1,
            isSpecial = true
        ),
        CategoryDto(
            categoryCode = "new",
            localizedNames = mapOf(
                "en" to "New",
                "ru" to "Новые"
            ),
            sortOrder = 2,
            isSpecial = true
        ),
    )

    fun getCategories() = categories

    fun getServerCategory() = serverCategory

    fun getDefaultCategory() = categories.first()
}
