package example.media;

public final class MediaDeliveryExample {
    public static void main(String[] args) {
        if (args.length != 1 || args[0].isBlank()) {
            throw new IllegalArgumentException("Usage: MediaDeliveryExample <recipient-email>");
        }
        MediaAsset asset = new MediaAsset("course-lesson-07", "Fractions on a Number Line", args[0], 1.0);
        ProcessingJob job = new MediaDeliveryService(MediaDeliveryService.live(new InfraiClient())).deliver(asset);
        System.out.println(job.assetId() + " " + job.state() + " " + job.messageId());
    }
}
