package example.media;

import java.io.IOException;

public final class MediaDeliveryService {
    public interface AccountGateway { double balance(); void recharge(); String notifyCreator(String email, String title); }
    private final AccountGateway gateway;

    public MediaDeliveryService(AccountGateway gateway) { this.gateway = gateway; }

    public ProcessingJob deliver(MediaAsset asset) {
        if (gateway.balance() < asset.allowance()) gateway.recharge();
        String messageId = gateway.notifyCreator(asset.creatorEmail(), asset.lessonTitle());
        return new ProcessingJob(asset.assetId(), "DELIVERED", messageId);
    }

    public static AccountGateway live(InfraiClient client) {
        return new AccountGateway() {
            public double balance() { try { return number(client.accountBalance(), "balance"); } catch (Exception e) { throw new IllegalStateException(e); } }
            public void recharge() { try { client.configureAutorecharge(1.0, 10.0); } catch (Exception e) { throw new IllegalStateException(e); } }
            public String notifyCreator(String email, String title) { try { return value(client.sendCreatorEmail(email, "Lesson ready: " + title, "Your lesson is ready for review."), "message_id"); } catch (Exception e) { throw new IllegalStateException(e); } }
        };
    }
    private static double number(String json, String field) { return Double.parseDouble(value(json, field)); }
    private static String value(String json, String field) { int p = json.indexOf("\"" + field + "\""); int colon = json.indexOf(':', p); int end = json.indexOf(',', colon); if (end < 0) end = json.indexOf('}', colon); return json.substring(colon + 1, end).replace("\"", "").trim(); }
}
