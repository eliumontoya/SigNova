package info.eliumontoyasadec.signova.ui.home;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

public class OpenAIClientImage {

    private static final String API_URL =             "https://api.openai.com/v1/images/generations";

    private static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

   // private final  OkHttpClient client = new OkHttpClient();
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .callTimeout(150, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();
    private final String apiKey;

    public OpenAIClientImage(String apiKey) {
        this.apiKey = apiKey;
    }

    public void sendInstruction(
            String prompt,
            Callback callback
    ) {

        try {
            JSONObject body = new JSONObject();
            body.put("model", "gpt-image-1");
            body.put("prompt", prompt);
            body.put("size", "1024x1024");

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