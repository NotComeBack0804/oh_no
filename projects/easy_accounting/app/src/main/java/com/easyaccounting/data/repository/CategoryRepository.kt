package com.easyaccounting.data.repository

import com.easyaccounting.data.dao.CategoryDao
import com.easyaccounting.data.entity.Category
import com.easyaccounting.data.entity.CategoryType
import kotlinx.coroutines.flow.Flow

class CategoryRepository(private val categoryDao: CategoryDao) {

    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    fun getCategoriesByType(type: CategoryType): Flow<List<Category>> =
        categoryDao.getCategoriesByType(type)

    suspend fun getCategoriesByTypeSync(type: CategoryType): List<Category> =
        categoryDao.getCategoriesByTypeSync(type)

    suspend fun getCategoryById(id: Long): Category? = categoryDao.getCategoryById(id)

    fun getCustomCategories(): Flow<List<Category>> = categoryDao.getCustomCategories()

    suspend fun insertCategory(category: Category): Long = categoryDao.insert(category)

    suspend fun updateCategory(category: Category) = categoryDao.update(category)

    suspend fun deleteCategory(category: Category) = categoryDao.delete(category)

    suspend fun getCategoryCount(): Int = categoryDao.getCategoryCount()
}
