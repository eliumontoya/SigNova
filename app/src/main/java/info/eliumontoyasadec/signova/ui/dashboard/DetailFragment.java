package info.eliumontoyasadec.signova.ui.dashboard;
// DetailFragment.java
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import info.eliumontoyasadec.signova.R;
import info.eliumontoyasadec.signova.bd.AppDatabase;
import info.eliumontoyasadec.signova.bd.GenerationEntity;

public class DetailFragment extends Fragment {

    private TextView tvPrompt;
    private ImageView ivResult;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_detail, container, false);

        tvPrompt = root.findViewById(R.id.tvPromptDetail);
        ivResult = root.findViewById(R.id.ivResultDetail);

        long id = requireArguments().getLong("generation_id", -1);
        if (id != -1) loadItem(id);

        return root;
    }

    private void loadItem(long id) {
        new Thread(() -> {
            GenerationEntity item =
                    AppDatabase.getInstance(requireContext())
                            .generationDao()
                            .getById(id);

            requireActivity().runOnUiThread(() -> {
                if (item == null) return;

                tvPrompt.setText(item.prompt);

                Bitmap bmp = BitmapFactory.decodeFile(item.imagePath);
                if (bmp != null) {
                    ivResult.setImageBitmap(bmp);
                } else {
                    // Pon tu placeholder si quieres
                    // ivResult.setImageResource(R.drawable.ic_broken_image);
                }
            });
        }).start();
    }
}