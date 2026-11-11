package app.service.lorebook.budget

import app.service.lorebook.model.BudgetPackingResult
import app.service.lorebook.model.LorebookEntrySpec

object TokenBudgetOptimizer {

    /**
     * Упаковывает активированные статьи в [tokenBudget], используя двухфазную стратегию:
     * 1. Безусловное включение статей с `ignoreBudget = true`.
     * 2. Резервация бюджета для постоянных (`constant = true`) статей в порядке убывания приоритета.
     * 3. 0/1 Knapsack (или Skip-Lookahead жадный алгоритм) для динамических статей, максимизирующий суммарный приоритет.
     * 4. Восстановление стабильного канонического порядка (position, insertionOrder, id) для защиты KV-кэша LLM.
     */
    fun packEntries(
        activated: List<LorebookEntrySpec>,
        tokenBudget: Int,
        useKnapsackForDynamic: Boolean = true
    ): BudgetPackingResult {
        if (activated.isEmpty()) {
            return BudgetPackingResult(emptyList(), emptyList(), 0, false)
        }

        val selected = ArrayList<LorebookEntrySpec>(activated.size)
        val dropped = ArrayList<LorebookEntrySpec>()
        var remainingBudget = tokenBudget.coerceAtLeast(0)
        var totalUsed = 0

        // Tier 0: Ignore-Budget статьи проходят вне конкурса
        val (bypassEntries, budgetedEntries) = activated.partition { it.ignoreBudget }
        selected.addAll(bypassEntries)

        // Tier 1: Constant статьи бронируют бюджет первыми по убыванию приоритета
        val (constantEntries, dynamicEntries) = budgetedEntries.partition { it.constant }
        for (entry in constantEntries.sortedByDescending { it.priority }) {
            val cost = entry.estimatedTokens.coerceAtLeast(1)
            if (cost <= remainingBudget) {
                selected.add(entry)
                remainingBudget -= cost
                totalUsed += cost
            } else {
                dropped.add(entry)
            }
        }

        // Tier 2: Динамические статьи пакуются через 0/1 Knapsack или Skip-Lookahead
        if (dynamicEntries.isNotEmpty()) {
            val totalDynamicTokens = dynamicEntries.sumOf { it.estimatedTokens.coerceAtLeast(1) }
            if (totalDynamicTokens <= remainingBudget) {
                selected.addAll(dynamicEntries)
                totalUsed += totalDynamicTokens
            } else if (useKnapsackForDynamic && remainingBudget in 1..8192) {
                val (knapsackKept, knapsackDropped) = solveKnapsack(dynamicEntries, remainingBudget)
                selected.addAll(knapsackKept)
                dropped.addAll(knapsackDropped)
                totalUsed += knapsackKept.sumOf { it.estimatedTokens.coerceAtLeast(1) }
            } else {
                // Skip-Lookahead Greedy: пропуск слишком крупных записей в пользу мелких с высоким приоритетом
                for (entry in dynamicEntries.sortedWith(
                    compareByDescending<LorebookEntrySpec> { it.priority }.thenBy { it.insertionOrder }
                )) {
                    val cost = entry.estimatedTokens.coerceAtLeast(1)
                    if (cost <= remainingBudget) {
                        selected.add(entry)
                        remainingBudget -= cost
                        totalUsed += cost
                    } else {
                        dropped.add(entry)
                    }
                }
            }
        }

        // Фаза 3: Восстановление канонического порядка сортировки для фиксации KV-кэша LLM
        val orderedSelection = selected.sortedWith(
            compareBy<LorebookEntrySpec> { it.position.ordinal }
                .thenBy { it.insertionOrder }
                .thenBy { it.id }
        )

        return BudgetPackingResult(
            selectedEntries = orderedSelection,
            droppedEntries = dropped,
            tokensUsed = totalUsed,
            budgetOverflowed = dropped.isNotEmpty()
        )
    }

    private fun solveKnapsack(
        candidates: List<LorebookEntrySpec>,
        budgetTokens: Int
    ): Pair<List<LorebookEntrySpec>, List<LorebookEntrySpec>> {
        val maxWeight = budgetTokens
        val n = candidates.size
        val dp = IntArray((n + 1) * (maxWeight + 1))

        for (i in 1..n) {
            val entry = candidates[i - 1]
            val weight = entry.estimatedTokens.coerceAtLeast(1)
            val value = entry.priority.coerceAtLeast(1) * 10
            val rowOffset = i * (maxWeight + 1)
            val prevRowOffset = (i - 1) * (maxWeight + 1)

            for (w in 0..maxWeight) {
                val withoutItem = dp[prevRowOffset + w]
                val withItem = if (weight <= w) dp[prevRowOffset + (w - weight)] + value else -1
                dp[rowOffset + w] = maxOf(withoutItem, withItem)
            }
        }

        val kept = ArrayList<LorebookEntrySpec>()
        val dropped = ArrayList<LorebookEntrySpec>()
        var w = maxWeight
        for (i in n downTo 1) {
            val entry = candidates[i - 1]
            val weight = entry.estimatedTokens.coerceAtLeast(1)
            val rowOffset = i * (maxWeight + 1)
            val prevRowOffset = (i - 1) * (maxWeight + 1)

            if (weight <= w && dp[rowOffset + w] != dp[prevRowOffset + w]) {
                kept.add(entry)
                w -= weight
            } else {
                dropped.add(entry)
            }
        }

        return kept to dropped
    }
}
