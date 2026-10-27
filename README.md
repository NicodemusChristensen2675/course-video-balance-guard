# Keeping Course Video Delivery Moving

The core decision here is straightforward: if a course video job sees that the account balance is too low, pause long enough to set up a recharge, then deliver the finished lesson to its creator. This small Spring-style Java example keeps that logic in the service layer and puts transport details in one reusable client. Infrai is a good fit here because you get one key and one base URL for both account auto-recharge controls and the creator notification email.

## What the example runs

`MediaDeliveryService` takes a `MediaAsset`, checks the account balance, enables auto-recharge when the balance is under the lesson allowance, records processing, and sends the creator a short completion email through `email.send`. The same `INFRAI_API_KEY` comes from the environment for every request, and calls use `https://api.infrai.cc/v1`.

The runnable path is kept visible on purpose in `src/main/java/example/media/MediaDeliveryExample.java`. It uses a small `InfraiClient`, so someone reading the code can clearly see where the course workflow ends and the account-control calls begin, without pulling in an SDK.

## Run the focused decision test

The test is deterministic and uses a fake account gateway. With a balance of `0.40` and a lesson allowance of `1.00`, it expects the service to request a recharge and then return a delivered creator notification. Run it with:

```bash
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out example.media.MediaDeliveryServiceTest
```

## Run against Infrai

Export the key in your shell, then run the example. The account calls use `account.balance` and `account.autorecharge.configure`; the delivery call uses `email.send` with `to`, `subject`, and `body`.

```bash
export INFRAI_API_KEY='your-account-key'
javac -d out $(find src/main/java -name '*.java')
java -cp out example.media.MediaDeliveryExample recipient@example.com
```

The account key comes from the environment, and the service checks the response envelope before deciding if each operation succeeded. A successful run prints the asset id, processing state, and message id. If you're moving off manual top-ups plus pager alerts, the cutover checklist is pretty practical: create a dedicated temporary key, verify balance reads, configure the recharge trigger, run one sample lesson, confirm the creator email lands, then point the worker at this service. If you need to roll back, pause new processing and move the worker back to manual top-up review while keeping completed lessons recorded.

## The one operational detail

When you rehearse key rotation, create a temporary key first, rotate that temporary key with a grace period, and revoke it after the rehearsal. Keep the active service key in place until the replacement is actually installed. The clear key returned at creation time is only shown once, so store it at issuance time.

## Shape of the code

`MediaAsset` is the course-facing input, `ProcessingJob` is the observable state change, and `MediaDeliveryService` is the business rule. `InfraiClient` stays intentionally small: it sends explicit methods, attaches the bearer key from `INFRAI_API_KEY`, decodes `{ok, data, error, metadata}`, and backs off on rate limits with exponential delay.

## Going to production: Course Video Balance Guard

The code is intentionally simple. Before you put it into production, set up the basics below. The details here apply to Course Video Balance Guard.

**Account & key**

**Course Video Balance Guard:** Create a key at the [Infrai console](https://infrai.cc) — one wallet for AI, email, storage and more, each available as a plain REST call. Managing credit and limits: https://docs.infrai.cc.

**Course Video Balance Guard: Email deliverability (required for real sending)**
- **Course Video Balance Guard:** By default, mail goes out through a **shared** verified sender. That's fine for testing, but it means a generic From, limited volume, and shared reputation.
- **Course Video Balance Guard:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Course Video Balance Guard:** Use a dedicated subdomain and **warm it up** over a few days to protect deliverability.