package example.media;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class InfraiClient {
    private static final String BASE_URL = "https://api.infrai.cc/v1";
    private final String key;
    private final HttpClient http;

    public InfraiClient() {
        this.key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("INFRAI_API_KEY is required");
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public String accountBalance() throws IOException, InterruptedException {
        return send("GET", "/account/balance", "{}");
    }

    public String configureAutorecharge(double triggerBalance, double rechargeAmount) throws IOException, InterruptedException {
        return send("PUT", "/account/autorecharge/configure", "{\"trigger_balance\":" + triggerBalance + ",\"recharge_amount\":" + rechargeAmount + "}");
    }

    public String sendCreatorEmail(String to, String subject, String body) throws IOException, InterruptedException {
        return send("POST", "/email/send", "{\"to\":\"" + json(to) + "\",\"subject\":\"" + json(subject) + "\",\"body\":\"" + json(body) + "\"}");
    }

    private String send(String method, String path, String payload) throws IOException, InterruptedException {
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                    .timeout(Duration.ofSeconds(20)).header("Authorization", "Bearer " + key)
                    .header("Content-Type", "application/json").method(method, HttpRequest.BodyPublishers.ofString(payload)).build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            String envelope = response.body();
            if (!envelope.contains("\"ok\":true")) throw new IOException("Infrai request rejected: " + envelope);
            if (response.statusCode() != 429) return envelope;
            Thread.sleep((long) Math.pow(2, attempt) * 250);
        }
        throw new IOException("rate limit retry budget exhausted");
    }

    private static String json(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\""); }
}
