package com.bikangu.providers;

import com.bikangu.common.exception.ProviderException;
import com.bikangu.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProviderRegistry {

    private final Map<String, FinancialProvider> providers = new ConcurrentHashMap<>();
    private final List<FinancialProvider> providerList;

    public void register(FinancialProvider provider) {
        providers.put(provider.getProviderId(), provider);
        log.info("Registered financial provider: {} ({})", provider.getProviderName(), provider.getProviderId());
    }

    public FinancialProvider getProvider(String providerId) {
        FinancialProvider provider = providers.get(providerId);
        if (provider == null) {
            throw new ResourceNotFoundException("Financial Provider", providerId);
        }
        return provider;
    }

    public FinancialProvider getProviderByName(String name) {
        return providers.values().stream()
                .filter(p -> p.getProviderName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Financial Provider", name));
    }

    public List<FinancialProvider> getAllProviders() {
        return providerList;
    }

    public List<FinancialProvider> getActiveProviders() {
        return providerList.stream()
                .filter(FinancialProvider::isHealthy)
                .toList();
    }

    public void unregister(String providerId) {
        providers.remove(providerId);
        log.info("Unregistered financial provider: {}", providerId);
    }
}