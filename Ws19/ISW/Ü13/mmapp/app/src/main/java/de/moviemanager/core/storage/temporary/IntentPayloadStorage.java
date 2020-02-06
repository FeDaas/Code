package de.moviemanager.core.storage.temporary;

import android.content.Intent;

import java.util.HashMap;
import java.util.Map;

import de.moviemanager.data.proxy.TemporaryProxy;
import de.moviemanager.util.AndroidStringUtils;

import static de.moviemanager.ui.detail.PortrayableDetailEditActivity.RESULT_PROXY_KEY;

public class IntentPayloadStorage {
    public static String pushTemporaryProxy(final TemporaryProxy proxy) {
        return getInstance().push(proxy);
    }

    public static TemporaryProxy popTemporaryProxyFromSource(final Intent result, final String id) {
        return getInstance().pop(result, id);
    }

    public static TemporaryProxy popTemporaryProxyFromResult(final Intent result) {
        return getInstance().pop(result, RESULT_PROXY_KEY);
    }

    private static IntentPayloadStorage getInstance() {
        return INSTANCE;
    }

    private static final IntentPayloadStorage INSTANCE = new IntentPayloadStorage();

    private final Map<String, TemporaryProxy> proxies;

    private IntentPayloadStorage() {
        proxies = new HashMap<>();
    }

    private String push(final TemporaryProxy proxy) {
        final String key = AndroidStringUtils.generateIdentifier(proxies::containsKey);
        proxies.put(key, proxy);
        return key;
    }

    private TemporaryProxy pop(final Intent result, final String id) {
        return pop(result.getStringExtra(id));
    }

    private TemporaryProxy pop(final String key) {
        final TemporaryProxy result = proxies.get(key);
        proxies.remove(key);
        return result;
    }
}
