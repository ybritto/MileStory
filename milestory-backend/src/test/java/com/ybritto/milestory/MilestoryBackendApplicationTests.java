package com.ybritto.milestory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
class MilestoryBackendApplicationTests {

	@Test
	void foundationResourcesExist() {
		assertNotNull(getClass().getResource("/db/changelog/db.changelog-master.yaml"));
		assertNotNull(getClass().getResource("/db/changelog/changes/001-foundation-baseline.yaml"));
	}

}
