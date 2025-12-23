package info.eliumontoyasadec.signova.ui.notifications;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import org.json.JSONArray;
import org.json.JSONObject;

public class OpenAIClientVision {

    private static final String API_URL = "https://api.openai.com/v1/responses";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();
    private final String apiKey;

    public OpenAIClientVision(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * base64Image: solo el base64 (sin prefijo).
     */

    public void translateImageToText(
            String mime,
            String base64Image,
            Callback callback
    ) throws Exception {

        String instruction =
                "Eres un traductor de comunicación visual para accesibilidad. " +
                        "Analiza la imagen y produce un texto claro en español para que una persona " +
                        "que NO conoce lengua de señas entienda el mensaje.\n\n" +
                        "Reglas:\n" +
                        "1) Si la imagen contiene texto, transcríbelo y luego reexprésalo.\n" +
                        "2) Si la imagen muestra señas o gestos, interpreta con cautela.\n" +
                        "3) Si no hay suficiente información, dilo explícitamente.\n" +
                        "4) Responde en máximo 4 frases.";

        String dataUrl = "data:" + mime + ";base64," + base64Image;

        JSONObject body = new JSONObject();
        body.put("model", "gpt-4.1-mini");

        JSONArray input = new JSONArray();
        JSONObject msg = new JSONObject();
        msg.put("role", "user");

        JSONArray content = new JSONArray();

        JSONObject t = new JSONObject();
        t.put("type", "input_text");
        t.put("text", instruction);

        JSONObject i = new JSONObject();
        i.put("type", "input_image");
        i.put("image_url", dataUrl);

        content.put(t);
        content.put(i);

        msg.put("content", content);
        input.put(msg);

        body.put("input", input);

        Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(body.toString(), JSON))
                .build();

        client.newCall(request).enqueue(callback);
    }
    /**
     * Extrae texto de la respuesta. Lo hago robusto:
     * - Si existe "output_text", úsalo.
     * - Si no, busca en output[].content[].text
     */
    public String extractTextFromResponse(String responseJson) {
        try {
            JSONObject obj = new JSONObject(responseJson);

            if (obj.has("output_text")) {
                return obj.optString("output_text", "");
            }

            if (!obj.has("output")) return "";

            JSONArray output = obj.getJSONArray("output");
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < output.length(); i++) {
                JSONObject item = output.getJSONObject(i);
                if (!item.has("content")) continue;

                JSONArray content = item.getJSONArray("content");
                for (int j = 0; j < content.length(); j++) {
                    JSONObject c = content.getJSONObject(j);
                    // en Responses suele venir: {type:"output_text", text:"..."}
                    if (c.has("text")) {
                        sb.append(c.optString("text", "")).append("\n");
                    }
                }
            }
            return sb.toString().trim();

        } catch (Exception e) {
            return "";
        }
    }
}