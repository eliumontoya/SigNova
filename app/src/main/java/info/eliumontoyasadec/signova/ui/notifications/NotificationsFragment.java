package info.eliumontoyasadec.signova.ui.notifications;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import info.eliumontoyasadec.signova.BuildConfig;
import info.eliumontoyasadec.signova.R;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class NotificationsFragment extends Fragment {

    private ImageView ivSelected;
    private TextView tvResult;
    private LinearLayout loadingOverlay;

    private Uri selectedImageUri;

    private OpenAIClientVision client;
    private ActivityResultLauncher<String[]> pickImageLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔐 API Key desde BuildConfig
        String apiKey = BuildConfig.OPENAI_API_KEY;
        if (apiKey == null || apiKey.equals("null") || apiKey.trim().isEmpty()) {
            throw new IllegalStateException("OPENAI_API_KEY inválida. Revisa gradle.properties.");
        }
        client = new OpenAIClientVision(apiKey);

        // 📷 Selector de imagen (robusto)
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri == null) return;

                    // Intentar persistir permiso (si falla, seguimos)
                    try {
                        requireContext()
                                .getContentResolver()
                                .takePersistableUriPermission(
                                        uri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                );
                    } catch (SecurityException ignored) {
                        // Algunos proveedores no permiten persistir
                    } catch (Exception ignored) {
                    }

                    selectedImageUri = uri;
                    ivSelected.setImageURI(uri);
                    tvResult.setText("Imagen cargada. Listo para traducir.");
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        ivSelected = root.findViewById(R.id.ivSelected);
        tvResult = root.findViewById(R.id.tvResult);
        loadingOverlay = root.findViewById(R.id.loadingOverlay);

        Button btnPickImage = root.findViewById(R.id.btnPickImage);
        Button btnTranslate = root.findViewById(R.id.btnTranslateImage);

        btnPickImage.setOnClickListener(v ->
                pickImageLauncher.launch(new String[]{"image/*"})
        );

        btnTranslate.setOnClickListener(v -> {
            if (selectedImageUri == null) {
                tvResult.setText("Primero elige una imagen.");
                return;
            }

            showLoading(true);
            tvResult.setText("Analizando imagen…");

            try {
                ImagePayload payload = readUriAsBase64WithMime(selectedImageUri);

                client.translateImageToText(payload.mime, payload.base64, new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull java.io.IOException e) {
                        if (!isAdded()) return;
                        requireActivity().runOnUiThread(() -> {
                            showLoading(false);
                            tvResult.setText("Error de red: " + e.getMessage());
                        });
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response)
                            throws java.io.IOException {

                        String body = response.body() != null ? response.body().string() : "";
                        if (!isAdded()) return;

                        requireActivity().runOnUiThread(() -> {
                            showLoading(false);

                            if (!response.isSuccessful()) {
                                tvResult.setText("Error HTTP " + response.code() + "\n" + body);
                                return;
                            }

                            String extracted = client.extractTextFromResponse(body);
                            if (extracted == null || extracted.trim().isEmpty()) {
                                tvResult.setText("No pude interpretar la imagen.\n\nRespuesta:\n" + body);
                            } else {
                                tvResult.setText(extracted.trim());
                            }
                        });
                    }
                });

            } catch (Exception ex) {
                showLoading(false);
                tvResult.setText("No pude leer la imagen: " + ex.getMessage());
            }
        });

        return root;
    }

    // =========================
    // Utilidades
    // =========================

    private void showLoading(boolean show) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private ImagePayload readUriAsBase64WithMime(Uri uri) throws Exception {
        String mime = requireContext().getContentResolver().getType(uri);
        if (mime == null) mime = "image/jpeg"; // fallback

        InputStream inputStream = requireContext()
                .getContentResolver()
                .openInputStream(uri);

        if (inputStream == null) {
            throw new Exception("No se pudo abrir la imagen (URI inválido o sin permisos).");
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;
        while ((nRead = inputStream.read(data)) != -1) {
            buffer.write(data, 0, nRead);
        }
        inputStream.close();

        String base64 = Base64.encodeToString(buffer.toByteArray(), Base64.NO_WRAP);
        return new ImagePayload(mime, base64);
    }

    static class ImagePayload {
        final String mime;
        final String base64;

        ImagePayload(String mime, String base64) {
            this.mime = mime;
            this.base64 = base64;
        }
    }
}