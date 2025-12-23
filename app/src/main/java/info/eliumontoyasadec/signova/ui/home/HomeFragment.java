package info.eliumontoyasadec.signova.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import info.eliumontoyasadec.signova.bd.AppDatabase;
import info.eliumontoyasadec.signova.bd.GenerationEntity;
import info.eliumontoyasadec.signova.bd.ImageStorage;
import okhttp3.Callback;
import okhttp3.Call;
import okhttp3.Response;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import info.eliumontoyasadec.signova.BuildConfig;


import info.eliumontoyasadec.signova.databinding.FragmentHomeBinding;


public class HomeFragment extends Fragment {

    boolean USE_MOCK = false;


    final private String apik = BuildConfig.OPENAI_API_KEY;

    Button btGenerarVideo;
    Button btLimpiarTexto;
    private String instruccionEjecucion = "";

      TextView text_home;
      EditText etInstruccionesTexto;

    public static final String MOCK_429 =
            "{\"error\":{\"message\":\"You exceeded your current quota, please check your plan and billing details.\",\"type\":\"insufficient_quota\",\"param\":null,\"code\":\"insufficient_quota\"}}";

    public static final String MOCK_500 =
            "{\"error\":{\"message\":\"Internal server error\",\"type\":\"server_error\",\"param\":null,\"code\":\"server_error\"}}";
    String mockChatGPTResponse =
            "{\n" +
                    "  \"id\": \"img-mock-001\",\n" +
                    "  \"object\": \"image\",\n" +
                    "  \"created\": 1734100000,\n" +
                    "  \"model\": \"gpt-image-1\",\n" +
                    "  \"data\": [\n" +
                    "    {\n" +
                    "      \"b64_json\": \"iVBORw0KGgoAAAANSUhEUgAAAAIAAAACCAIAAAD91JpzAAAAFklEQVR4nGP8z8DAwMDAxMDAwMDAAAANHQEDasKb6QAAAABJRU5ErkJggg==\",\n" +
                    "      \"description\": \"Ilustración de una persona señalando un reloj con gesto de disculpa, autos detenidos en fila, semáforo en rojo y símbolo de retraso. Comunicación visual clara sin texto.\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";


    private static int n = 0;


    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();



        text_home = binding.textHome;

        etInstruccionesTexto = binding.etInstruccionesTexto;

        etInstruccionesTexto.setText("Voy a llegar tarde, el tráfico está detenido");


        btGenerarVideo = binding.btGenerarVideo;
        btGenerarVideo.setOnClickListener(GenerarVideo());


        binding.btLimpiarTexto.setOnClickListener(LimpiarTexto());




        return root;
    }

    private View.OnClickListener LimpiarTexto() {
        return v -> {
             etInstruccionesTexto.setText("");
            etInstruccionesTexto.requestFocus();
            // forzamos que aparezca el teclado
            InputMethodManager imm =
                    (InputMethodManager) requireContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);

            imm.showSoftInput(etInstruccionesTexto, InputMethodManager.SHOW_IMPLICIT);
        };
    }

    private View.OnClickListener GenerarVideo() {
        return v -> {


                n++;
                Log.i("EMM", "generarVideo() llamada #" + n);



             btGenerarVideo.setEnabled(false);
            ActualizarEstado("Generando video remotamente");
            OpenAIClientImage openAIClient =
                    new OpenAIClientImage(apik);



            String instruction = generarInstrucciones();
            Log.d("EMM", "Prompt: "
             + instruction);


            if (USE_MOCK) {
                procesarRespuesta(mockChatGPTResponse);
            } else {
                hacerLlamadaChatGPT(openAIClient, instruction);
            }

        };
    }

    private void hacerLlamadaChatGPT(OpenAIClientImage openAIClient, String instruction) {

        openAIClient.sendInstruction(instruction, new Callback() {


            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response)
                    throws IOException {
                if (response.code() == 429) {
                    String retryAfter = response.header("Retry-After"); // puede venir null
                    Log.e("EMM", "429 Too Many Requests. Retry-After=" + retryAfter);
                    String body = response.body() != null ? response.body().string() : "";
                     Log.e("EMM", "429 body=" + body);


                }

                if (!response.isSuccessful()) {
                     String err = response.body() != null ? response.body().string() : "";
                    Log.e("EMM", "Error HTTP " + response.code() + " body=" + err);
                    throw new IOException("Error HTTP inesperado " + response);
                }

                // Respuesta Final Satisfactoria
                String responseBody = response.body().string();
                Log.i("EMM", responseBody);
                procesarRespuesta(responseBody);
            }
        });
    }

    private String generarInstrucciones() {
        instruccionEjecucion = etInstruccionesTexto.getText().toString();
        return getPrompt() + etInstruccionesTexto.getText().toString();
    }

    private void procesarRespuesta(String json) {
        Log.i("EMM", "Usando json " +json);

        try {
            // 1) Parsear JSON
            JSONObject root = new JSONObject(json);

            // Extraer el json de DATA
            JSONArray data = extraerJsonData(root);
            if (data == null) return;

            //Extraer de Data la parte del B64 donde esta la imagen
            JSONObject first = extraerJsonDataFirst(  data);
            String b64 = extraerJsonDataB64(first);
            if (b64 == null) return;
            Log.d("EMM", "Conviertiendo a b64: " + b64);

            // Convetimos a bytes de imagenes
            byte[] imageBytes = decodeB64(b64);
            Log.d("EMM", "Bytes decodificados: " + imageBytes.length);

            // 3) Convertimos Bytes -> Bitmap
            Bitmap bitmap = getBitmap(imageBytes);
            if (bitmap == null) return;

            // 4) Pintar en el ImageView (UI thread)
            if (!isAdded()) return;
            pintarBitmap(bitmap);

            // (Opcional) Log de descripción si viene
            String description = first.optString("description", "");
            if (!description.isEmpty()) {
                Log.i("EMM", "Descripción imagen: " + description);
            }

            // Guardar en background (muy importante NO en el hilo UI)
            guardarHistorial(bitmap);

        } catch (Exception e) {
            Log.e("EMM", "Error parseando/decodificando respuesta", e);
            mostrarErrorUI("Error al procesar respuesta");
        }


    }

    private void pintarBitmap(Bitmap bitmap) {
        requireActivity().runOnUiThread(() -> {
            binding.ivResultado.setImageBitmap(bitmap);
            binding.ivResultado.setVisibility(View.VISIBLE);

            // Si tienes un TextView de estado:
             text_home.setVisibility(View.GONE);

            // Si deshabilitaste el botón:
           binding.btGenerarVideo.setEnabled(true);
        });
    }

    private Bitmap getBitmap(byte[] imageBytes) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        if (bitmap == null) {
            String sig = bytesSignature(imageBytes);
            Log.e("EMM", "Bitmap=null. Signature bytes: " + sig);

            mostrarErrorUI("No se pudo convertir a Bitmap");
            return null;
        }
        return bitmap;
    }

    private byte[] decodeB64(String b64) {
        byte[] imageBytes;

        // 2) Decodificar Base64 -> bytes
        try {
            imageBytes = Base64.decode(b64, Base64.DEFAULT);
        } catch (IllegalArgumentException ex) {
            // a veces viene en formato URL_SAFE
            imageBytes = Base64.decode(b64, Base64.URL_SAFE);
        }
        return imageBytes;
    }

    private JSONObject extraerJsonDataFirst(JSONArray data) {
        JSONObject first = data.optJSONObject(0);
        if (first == null) {
            mostrarErrorUI("data[0] inválido");
            return null;
        }
        return first;
    }
    private String extraerJsonDataB64(JSONObject first) {


        String b64 = first.optString("b64_json", null);
        if (b64 == null || b64.trim().isEmpty()) {
            mostrarErrorUI("b64_json vacío");
            return null;
        }
        // Limpia prefijo tipo data URL si existiera
        if (b64.startsWith("data:")) {
            int comma = b64.indexOf(',');
            if (comma >= 0) b64 = b64.substring(comma + 1);
        }

        // (Opcional) limpia saltos de línea/espacios
        b64 = b64.trim();
        return b64;
    }

    private JSONArray extraerJsonData(JSONObject root) {
        JSONArray data = root.optJSONArray("data");
        if (data == null || data.length() == 0) {
            mostrarErrorUI("Respuesta sin data[]");
            return null;
        }
        return data;
    }

    private void guardarHistorial(Bitmap bitmap) {

        new Thread(() -> {
            try {
                Log.d("EMM","Guardando historial " + instruccionEjecucion);

                String path = ImageStorage.saveBitmapToInternal(requireContext(), bitmap);

                GenerationEntity entity = new GenerationEntity(
                        instruccionEjecucion,
                        path,
                        System.currentTimeMillis()
                );

                AppDatabase db = AppDatabase.getInstance(requireContext());
                long newId = db.generationDao().insert(entity);

                // Si necesitas notificar UI:
                requireActivity().runOnUiThread(() -> {
                    // actualizar UI, mostrar "Guardado", etc.
                });

            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    // mostrar error
                });
            }
        }).start();
    }

    private String bytesSignature(byte[] bytes) {
        // Muestra los primeros 8 bytes en hex para diagnóstico (PNG empieza con: 89 50 4E 47 0D 0A 1A 0A)
        int n = Math.min(bytes.length, 8);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(String.format("%02X ", bytes[i]));
        }
        return sb.toString().trim();
    }
    private void ActualizarEstado(String mensaje){
        text_home.setText(mensaje);
        text_home.setVisibility(View.VISIBLE);

    }


    private String getPrompt(){String prompt =
            "Convierte el siguiente texto en una representación visual clara, universal y " +
                    "fácil de interpretar para una persona sorda o con dificultades auditivas.\n\n" +

                    "Genera una sola imagen que:\n" +
                    "- No contenga texto escrito ni números\n" +
                    "- Use únicamente lenguaje visual, símbolos y acciones claras\n" +
                    "- Sea comprensible sin contexto adicional\n" +
                    "- Tenga un estilo de ilustración plana (flat illustration), simple y limpia\n" +
                    "- Use fondo claro y pocos colores\n" +
                    "- Muestre las acciones de forma evidente\n\n" +

                    "Especificaciones técnicas de la imagen:\n" +
                    "- Tamaño: 1024x1024 píxeles\n" +
                    "- Relación de aspecto: 1:1\n" +
                    "- Composición centrada\n" +
                    "- Alta claridad visual\n\n" +

                    "Texto a interpretar:\n"  ;


        return prompt;
    }
    private void mostrarErrorUI(String mensaje) {
        //  if (!isAdded()) return;
        //  requireActivity().runOnUiThread(() -> {
            // Tu TextView de estado si existe:
        // binding.tvEstadoGeneracion.setText(mensaje);
        //  binding.tvEstadoGeneracion.setVisibility(View.VISIBLE);

            // Si usas imagen, la puedes ocultar:
            // binding.ivResultado.setVisibility(View.GONE);

            // Si deshabilitaste el botón:
            // binding.btGenerarVideo.setEnabled(true);
        // });
        ActualizarEstado("Error al recibir imagen");
        Log.e("EMM", "Error en parsear imagen "+ mensaje);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}