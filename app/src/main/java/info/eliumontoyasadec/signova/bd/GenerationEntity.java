package info.eliumontoyasadec.signova.bd;
// GenerationEntity.java
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "generations")
public class GenerationEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String prompt;

    // Ruta local del archivo (ej. /data/data/.../files/images/img_123.png)
    @NonNull
    public String imagePath;

    public long createdAtMillis;

    public GenerationEntity(@NonNull String prompt,
                            @NonNull String imagePath,
                            long createdAtMillis) {
        this.prompt = prompt;
        this.imagePath = imagePath;
        this.createdAtMillis = createdAtMillis;
    }
}