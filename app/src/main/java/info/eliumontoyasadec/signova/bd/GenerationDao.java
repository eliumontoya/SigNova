package info.eliumontoyasadec.signova.bd;
// GenerationDao.java
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import info.eliumontoyasadec.signova.bd.GenerationEntity;

@Dao
public interface GenerationDao {

    @Insert
    long insert(GenerationEntity entity);

    @Query("SELECT * FROM generations ORDER BY createdAtMillis DESC")
    List<GenerationEntity> getAll();

    @Query("SELECT * FROM generations WHERE id = :id LIMIT 1")
    GenerationEntity getById(long id);

    @Query("DELETE FROM generations")
    void deleteAll();
}