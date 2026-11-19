package com.dp_ua.iksparser.dba.entity;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CompetitionEntityCanBeUpdatedTest {

    private CompetitionEntity withUrl(String url) {
        CompetitionEntity c = new CompetitionEntity();
        c.setUrl(url);
        return c;
    }

    @Test
    void normalUrl_canBeUpdated() {
        assertTrue(withUrl("https://example.com/competition/1").isCanBeUpdated());
    }

    @Test
    void emptyUrl_cannotBeUpdated() {
        assertFalse(withUrl("").isCanBeUpdated());
        assertFalse(withUrl(null).isCanBeUpdated());
    }

    @Test
    void pdfUrl_cannotBeUpdated() {
        assertFalse(withUrl("https://example.com/file.pdf").isCanBeUpdated());
    }
}
