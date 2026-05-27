package com.myassistant.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.myassistant.app.data.local.entity.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {

    @Query("SELECT * FROM cards WHERE status != 'ARCHIVED' ORDER BY created_at DESC")
    fun getAllCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE category = :category AND status != 'ARCHIVED' ORDER BY created_at DESC")
    fun getCardsByCategory(category: String): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE status = 'PINNED' ORDER BY updated_at DESC")
    fun getPinnedCards(): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE status = 'ARCHIVED' ORDER BY updated_at DESC")
    fun getArchivedCards(): Flow<List<CardEntity>>

    @Query("SELECT c.* FROM cards c JOIN cards_fts ON c.rowid = cards_fts.rowid WHERE cards_fts MATCH :query ORDER BY c.created_at DESC")
    fun searchCards(query: String): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE id = :id LIMIT 1")
    suspend fun getCardById(id: String): CardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardEntity)

    @Update
    suspend fun updateCard(card: CardEntity)

    @Query("UPDATE cards SET status = :status, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateCardStatus(id: String, status: String, updatedAt: Long)

    @Query("DELETE FROM cards WHERE id = :id")
    suspend fun deleteCard(id: String)

    @Query("SELECT category, COUNT(*) as count FROM cards WHERE status != 'ARCHIVED' GROUP BY category")
    fun getCategoryStats(): Flow<List<CategoryCount>>

    @Query("SELECT COUNT(*) FROM cards WHERE status != 'ARCHIVED'")
    fun getTotalCount(): Flow<Int>
}

data class CategoryCount(val category: String, val count: Int)
