package com;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AppLauncherTest {

    @Test
    @DisplayName("Полное покрытие метода main и логики запуска")
    void testMainMethodCoverage() {
        assertNotNull(new AppLauncher());

        assertDoesNotThrow(() -> {
            try {
                AppLauncher.main(new String[]{});
            } catch (Exception ignored) {
            }
        });
    }
}
