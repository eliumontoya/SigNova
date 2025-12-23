package info.eliumontoyasadec.signova.ui.dashboard;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import info.eliumontoyasadec.signova.R;
import info.eliumontoyasadec.signova.bd.AppDatabase;
import info.eliumontoyasadec.signova.bd.GenerationEntity;

public class DetailDialogFragment extends DialogFragment {

    public static DetailDialogFragment newInstance(long generationId) {
        Bundle args = new Bundle();
        args.putLong("generation_id", generationId);
        DetailDialogFragment f = new DetailDialogFragment();
        f.setArguments(args);
        return f;
    }

    private TextView tvPrompt;
    private ImageView ivResult;

    @Override
    public void onStart() {
        super.onStart();
        // Opcional: dialog más ancho
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.dialog_detail, container, false);

        tvPrompt = root.findViewById(R.id.tvPromptDetail);
        ivResult = root.findViewById(R.id.ivResultDetail);

        long id = requireArguments().getLong("generation_id", -1);
        if (id != -1) loadItem(id);

        return root;
    }

    private void loadItem(long id) {
        new Thread(() -> {
            GenerationEntity item = AppDatabase.getInstance(requireContext())
                    .generationDao()
                    .getById(id);

            requireActivity().runOnUiThread(() -> {
                if (item == null) return;

                tvPrompt.setText(item.prompt);

                Bitmap bmp = BitmapFactory.decodeFile(item.imagePath);
                if (bmp != null) {
                    ivResult.setImageBitmap(bmp);
                } else {
                    // Si quieres, pon un placeholder
                    // ivResult.setImageResource(R.drawable.ic_broken_image);
                }
            });
        }).start();
    }
}