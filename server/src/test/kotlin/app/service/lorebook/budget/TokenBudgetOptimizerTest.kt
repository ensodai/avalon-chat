package app.service.lorebook.budget

import app.service.lorebook.model.InjectionPosition
import app.service.lorebook.model.LorebookEntrySpec
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TokenBudgetOptimizerTest {

    private fun createSpec(
        id: Int,
        content: String = "Content $id",
        constant: Boolean = false,
        ignoreBudget: Boolean = false,
        estimatedTokens: Int = 50,
        priority: Int = 100,
        insertionOrder: Int = 100,
        position: InjectionPosition = InjectionPosition.AFTER_CHAR
    ): LorebookEntrySpec {
        return LorebookEntrySpec(
            id = id,
            uid = "entry-$id",
            content = content,
            primaryPatternIds = intArrayOf(id),
            constant = constant,
            ignoreBudget = ignoreBudget,
            estimatedTokens = estimatedTokens,
            priority = priority,
            insertionOrder = insertionOrder,
            position = position
        )
    }

    @Test
    fun `test ignoreBudget entries bypass budget even with zero budget`() {
        val bypassEntry = createSpec(id = 1, ignoreBudget = true, estimatedTokens = 200)
        val normalEntry = createSpec(id = 2, ignoreBudget = false, estimatedTokens = 50)

        val result = TokenBudgetOptimizer.packEntries(
            activated = listOf(bypassEntry, normalEntry),
            tokenBudget = 0
        )

        assertEquals(1, result.selectedEntries.size)
        assertEquals(1, result.selectedEntries[0].id)
        assertEquals(1, result.droppedEntries.size)
        assertEquals(2, result.droppedEntries[0].id)
        assertTrue(result.budgetOverflowed)
    }

    @Test
    fun `test constant entries reserve budget before dynamic entries`() {
        // Константная запись стоит 80 токенов (приоритет 50)
        val constantEntry = createSpec(id = 1, constant = true, estimatedTokens = 80, priority = 50)
        // Динамическая запись стоит 80 токенов (приоритет выше: 100)
        val dynamicEntry = createSpec(id = 2, constant = false, estimatedTokens = 80, priority = 100)

        val result = TokenBudgetOptimizer.packEntries(
            activated = listOf(dynamicEntry, constantEntry),
            tokenBudget = 100
        )

        assertEquals(1, result.selectedEntries.size)
        assertEquals(1, result.selectedEntries[0].id, "Константная запись должна иметь приоритет по бронированию бюджета")
        assertEquals(1, result.droppedEntries.size)
        assertEquals(2, result.droppedEntries[0].id)
        assertEquals(80, result.tokensUsed)
    }

    @Test
    fun `test knapsack maximizes priority utility`() {
        // Запись A: 60 токенов, приоритет 50
        val entryA = createSpec(id = 1, estimatedTokens = 60, priority = 50)
        // Запись B: 50 токенов, приоритет 60
        val entryB = createSpec(id = 2, estimatedTokens = 50, priority = 60)
        // Запись C: 50 токенов, приоритет 60
        val entryC = createSpec(id = 3, estimatedTokens = 50, priority = 60)

        // Бюджет: 100 токенов
        // Рюкзак должен выбрать B + C (вес 100, суммарный приоритет 120), а не одну A
        val result = TokenBudgetOptimizer.packEntries(
            activated = listOf(entryA, entryB, entryC),
            tokenBudget = 100,
            useKnapsackForDynamic = true
        )

        assertEquals(2, result.selectedEntries.size)
        val selectedIds = result.selectedEntries.map { it.id }.toSet()
        assertTrue(selectedIds.contains(2))
        assertTrue(selectedIds.contains(3))
        assertFalse(selectedIds.contains(1))
        assertEquals(100, result.tokensUsed)
    }

    @Test
    fun `test skip lookahead greedy skips oversized entries`() {
        val hugeWhale = createSpec(id = 1, estimatedTokens = 500, priority = 100)
        val small1 = createSpec(id = 2, estimatedTokens = 50, priority = 80)
        val small2 = createSpec(id = 3, estimatedTokens = 50, priority = 70)

        val result = TokenBudgetOptimizer.packEntries(
            activated = listOf(hugeWhale, small1, small2),
            tokenBudget = 120,
            useKnapsackForDynamic = false // Тестируем жадный Skip-Lookahead
        )

        assertEquals(2, result.selectedEntries.size)
        val selectedIds = result.selectedEntries.map { it.id }.toSet()
        assertTrue(selectedIds.contains(2))
        assertTrue(selectedIds.contains(3))
        assertFalse(selectedIds.contains(1))
        assertEquals(100, result.tokensUsed)
    }

    @Test
    fun `test canonical order restoration for KV cache`() {
        // Создаем записи с разными позициями и insertionOrder
        val entry1 = createSpec(id = 1, position = InjectionPosition.AFTER_CHAR, insertionOrder = 20)
        val entry2 = createSpec(id = 2, position = InjectionPosition.BEFORE_CHAR, insertionOrder = 50)
        val entry3 = createSpec(id = 3, position = InjectionPosition.BEFORE_CHAR, insertionOrder = 10)
        val entry4 = createSpec(id = 4, position = InjectionPosition.AT_DEPTH, insertionOrder = 5)

        val result = TokenBudgetOptimizer.packEntries(
            activated = listOf(entry1, entry2, entry3, entry4),
            tokenBudget = 1000
        )

        assertEquals(4, result.selectedEntries.size)
        // Ожидаемый порядок:
        // BEFORE_CHAR (порядок 10 -> entry 3, порядок 50 -> entry 2)
        // AFTER_CHAR (порядок 20 -> entry 1)
        // AT_DEPTH (порядок 5 -> entry 4)
        val orderOfIds = result.selectedEntries.map { it.id }
        assertEquals(listOf(3, 2, 1, 4), orderOfIds)
    }

    @Test
    fun `test empty activated list returns empty result`() {
        val result = TokenBudgetOptimizer.packEntries(emptyList(), tokenBudget = 500)
        assertTrue(result.selectedEntries.isEmpty())
        assertTrue(result.droppedEntries.isEmpty())
        assertEquals(0, result.tokensUsed)
        assertFalse(result.budgetOverflowed)
    }
}
