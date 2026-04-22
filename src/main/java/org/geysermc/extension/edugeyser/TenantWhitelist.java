package org.geysermc.extension.edugeyser;

import org.geysermc.geyser.api.extension.Extension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class TenantWhitelist {

    private static final String FILE_NAME = "tenant_whitelist.yml";
    private static final String LOG_PREFIX = "[edu] [TenantWhitelist] ";

    private final Extension extension;
    private final Set<String> allowedTenants = new HashSet<>();

    public TenantWhitelist(Extension extension) {
        this.extension = extension;
    }

    public void load() {
        Path path = extension.dataFolder().resolve(FILE_NAME);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(extension.dataFolder());
                Files.writeString(path,
                        "# tenant_whitelist.yml\n" +
                        "# Add tenant IDs to restrict which schools can join.\n" +
                        "# When all entries are empty, all tenants are allowed.\n" +
                        "# When one or more tenants are listed, only those can join.\n" +
                        "# Example: \"03b5e7a1-cb09-4417-9e1a-c686b440b2c5\"\n" +
                        "tenants:\n" +
                        "  - \"\"\n" +
                        "  - \"\"\n" +
                        "  - \"\"\n");
            } catch (IOException e) {
                extension.logger().error(LOG_PREFIX + "Failed to create whitelist file: " + e.getMessage());
            }
            return;
        }

        try {
            var loader = org.spongepowered.configurate.yaml.YamlConfigurationLoader.builder()
                    .path(path).build();
            var root = loader.load();
            var tenantsList = root.node("tenants").getList(String.class);
            allowedTenants.clear();
            if (tenantsList != null) {
                for (String tenant : tenantsList) {
                    if (tenant != null && !tenant.isBlank()) {
                        allowedTenants.add(tenant.trim());
                    }
                }
            }
            if (!allowedTenants.isEmpty()) {
                extension.logger().info(LOG_PREFIX + "Tenant whitelist active with " + allowedTenants.size() + " tenant(s).");
            } else {
                extension.logger().info(LOG_PREFIX + "Tenant whitelist is empty. All tenants are allowed.");
            }
        } catch (Exception e) {
            extension.logger().error(LOG_PREFIX + "Failed to load whitelist: " + e.getMessage());
        }
    }

    public boolean isEnabled() {
        return !allowedTenants.isEmpty();
    }

    public boolean isAllowed(String tenantId) {
        if (!isEnabled()) return true;
        return tenantId != null && allowedTenants.contains(tenantId);
    }
}
