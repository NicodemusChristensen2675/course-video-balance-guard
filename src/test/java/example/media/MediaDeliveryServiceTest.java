package example.media;

public final class MediaDeliveryServiceTest {
    public static void main(String[] args) {
        int[] recharges = {0};
        MediaDeliveryService service = new MediaDeliveryService(new MediaDeliveryService.AccountGateway() {
            public double balance() { return 0.40; }
            public void recharge() { recharges[0]++; }
            public String notifyCreator(String email, String title) { return "msg-course-07"; }
        });
        ProcessingJob job = service.deliver(new MediaAsset("course-lesson-07", "Fractions", "teacher@example.edu", 1.0));
        if (recharges[0] != 1 || !"DELIVERED".equals(job.state()) || !"msg-course-07".equals(job.messageId())) throw new AssertionError("balance decision failed");
        System.out.println("MediaDeliveryServiceTest passed");
    }
}
