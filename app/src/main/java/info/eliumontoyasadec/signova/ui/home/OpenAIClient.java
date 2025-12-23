package info.eliumontoyasadec.signova.ui.home;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import org.json.JSONArray;
import org.json.JSONObject;

public class OpenAIClient {

    private static final String API_URL = "https://api.openai.com/v1/responses";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();
    private final String apiKey;

    public OpenAIClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public void generateImageViaResponses(String prompt, Callback callback) {
        try {
            JSONObject body = new JSONObject();
            body.put("model", "gpt-4.1-mini"); // ok, este modelo puede orquestar tools

            // input en formato simple
            body.put("input", prompt);

            // habilita tool de imagen
            JSONArray tools = new JSONArray();
            JSONObject tool = new JSONObject();
            tool.put("type", "image_generation");
            tools.put(tool);
            body.put("tools", tools);

            // fuerza el tool call de imagen
            JSONObject toolChoice = new JSONObject();
            toolChoice.put("type", "image_generation");
            body.put("tool_choice", toolChoice);

            // opcional: parámetros para la tool (tamaño, calidad, etc.)
            // (la guía indica que el tool soporta size/quality/background, etc.)  [oai_citation:2‡OpenAI Plataforma](https://platform.openai.com/docs/guides/tools-image-generation?utm_source=chatgpt.com)
            JSONObject toolParams = new JSONObject();
            toolParams.put("size", "1024x1024");
            body.put("image_generation", toolParams);

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(body.toString(), JSON))
                    .build();

            client.newCall(request).enqueue(callback);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}