package app.apiv4.mappers

import com.ensodai.shared.avalon.category.models.*
import app.data.network.models.response.SelectableCategoryResponse as BackSelectableCategoryResponse
import app.data.network.models.response.ServerCategoryResponse as BackServerCategoryResponse
import app.data.network.models.response.CombinedCategoriesResponse as BackCombinedCategoriesResponse

fun BackSelectableCategoryResponse.toSharedSelectableCategory(): SelectableCategoryResponse {
    return SelectableCategoryResponse(
        id = id,
        categoryCode = categoryCode,
        localizedNames = localizedNames
    )
}

fun BackServerCategoryResponse.toSharedServerCategory(): ServerCategoryResponse {
    return ServerCategoryResponse(
        id = id,
        categoryCode = categoryCode,
        localizedNames = localizedNames,
        count = count
    )
}

fun BackCombinedCategoriesResponse.toSharedCombinedCategories(): CombinedCategoriesResponse {
    return CombinedCategoriesResponse(
        selectableCategories = selectableCategories.map { it.toSharedSelectableCategory() },
        serverCategories = serverCategories.map { it.toSharedServerCategory() }
    )
}
